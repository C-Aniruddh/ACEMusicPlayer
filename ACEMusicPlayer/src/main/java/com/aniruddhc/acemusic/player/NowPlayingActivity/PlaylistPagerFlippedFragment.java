/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aniruddhc.acemusic.player.NowPlayingActivity;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class PlaylistPagerFlippedFragment extends Fragment {

	private Context mContext;
	private Common mApp;
	private RatingBar ratingBar;
	public TextView lyricsTextView;
	public TextView headerTextView;
	public TextView noLyricsFoundText;
	public RelativeLayout lyricsRelativeLayout;
	private Cursor tempCursor;
	
	//Broadcast that notifies the PlaylistPager that it should flip the card back around to the album art.
	public static LocalBroadcastManager localBroadcastManager;
	public static final String broadcastMessage = "com.aniruddhc.acemusic.player.FLIP_BACK_TO_ALBUM_ART";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlist_pager_flipped, container, false);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        
        ratingBar = (RatingBar) rootView.findViewById(R.id.playlist_pager_flipped_rating_bar);
        lyricsRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.lyricsRelativeLayout);
        lyricsTextView = (TextView) rootView.findViewById(R.id.playlist_pager_flipped_lyrics);
        headerTextView = (TextView) rootView.findViewById(R.id.playlist_pager_flipped_title);
        noLyricsFoundText = (TextView) rootView.findViewById(R.id.no_embedded_lyrics_found_text);
        
        lyricsTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        lyricsTextView.setPaintFlags(lyricsTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        headerTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        headerTextView.setPaintFlags(headerTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        noLyricsFoundText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        noLyricsFoundText.setPaintFlags(noLyricsFoundText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        lyricsRelativeLayout.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				//Fire a broadcast that notifies the PlaylistPager to update flip back to the album art.
				Intent intent = new Intent(broadcastMessage);
				intent.putExtra("MESSAGE", broadcastMessage);
				
				//Initialize the local broadcast manager.
				localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
				localBroadcastManager.sendBroadcast(intent);
				
				return true;
			}
        	
        });
        
        //Get the file path of the current song.
        String updatedSongTitle = "";
		String updatedSongArtist = "";
		String songFilePath = "";
		String songId = "";
		MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
		tempCursor = mApp.getService().getCursor();
		tempCursor.moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
		if (tempCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)==-1) {
			//Retrieve the info from the file's metadata.
			songFilePath = tempCursor.getString(tempCursor.getColumnIndex(null));
			mmdr.setDataSource(songFilePath);
			
			updatedSongTitle = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			updatedSongArtist = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			
		} else {
			/* Check if the cursor has the SONG_FILE_PATH column. If it does, we're dealing 
			 * with the SONGS table. If not, we're dealing with the PLAYLISTS table. We'll 
			 * retrieve data from the appropriate columns using this info. */
			if (tempCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)==-1) {
				//We're dealing with the Playlists table.
				songFilePath = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.PLAYLIST_FILE_PATH));
				mmdr.setDataSource(songFilePath);
				
				updatedSongTitle = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				updatedSongArtist = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			} else {
				//We're dealing with the songs table.
				songFilePath = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
				updatedSongTitle = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_TITLE));
				updatedSongArtist = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
				songId = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ID));
			}
			
		}
		
		headerTextView.setText(updatedSongTitle + " - " + updatedSongArtist);
        ratingBar.setStepSize(1);
        int rating = mApp.getDBAccessHelper().getSongRating(songId);
        ratingBar.setRating(rating); 

        //Get the rating value for the song.
        AudioFile audioFile = null;
        File file = null;
        try {
          	audioFile = null;
            file = new File(songFilePath);
      		try {
      			audioFile = AudioFileIO.read(file);
      		} catch (CannotReadException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (IOException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (org.jaudiotagger.tag.TagException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (ReadOnlyFileException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (InvalidAudioFrameException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        try {
        	final AudioFile finalizedAudioFile = audioFile;
        	final String finalSongFilePath = songFilePath;
        	final String finalSongId = songId;
	        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	
				@Override
				public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
					//Change the rating in the DB and the actual audio file itself.
					
					Log.e("DEBUG", ">>>>>RATING: " + rating);
					
					try {
						Tag tag = finalizedAudioFile.getTag();
						tag.addField(FieldKey.RATING, "" + ((int) rating));
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FieldDataInvalidException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("DEBUG", ">>>>>>>RATING FIELD NOT FOUND");
					}
					
					try {
						finalizedAudioFile.commit();
					} catch (CannotWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					mApp.getDBAccessHelper().setSongRating(finalSongId, (int) rating);
					
				}
				
			});
	        
			//Check if the audio file has any embedded lyrics.
	        String lyrics = null;
	        try {
	        	Tag tag = audioFile.getTag();
	    		lyrics = tag.getFirst(FieldKey.LYRICS);
	    		
	    		if (lyrics==null || lyrics.isEmpty()) {
	    			lyricsTextView.setVisibility(View.GONE);
	    			noLyricsFoundText.setVisibility(View.VISIBLE);
	    			return rootView;
	    		}
	    		
	    		//Since the song has embedded lyrics, display them in the layout.
	    		lyricsTextView.setVisibility(View.VISIBLE);
	    		noLyricsFoundText.setVisibility(View.GONE);
	    		lyricsTextView.setText(lyrics);
	    	
	        } catch (Exception e) {
	    		e.printStackTrace();
	    		lyricsTextView.setVisibility(View.GONE);
    			noLyricsFoundText.setVisibility(View.VISIBLE);
    			return rootView;
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	//Can't do much here.
        }
		
        return rootView;
    }
    
}
