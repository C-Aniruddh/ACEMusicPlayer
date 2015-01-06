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
package com.aniruddhc.acemusic.player.BlacklistManagerActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class BlacklistedArtistsMultiselectAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private static Common mApp;
	
    public BlacklistedArtistsMultiselectAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        final Cursor c = (Cursor) getItem(position);
	    SongsListViewHolder holder = null;

		if (convertView == null) {
			
			convertView = LayoutInflater.from(mContext).inflate(R.layout.music_library_editor_artists_layout, parent, false);
			holder = new SongsListViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.artistThumbnailMusicLibraryEditor);
			holder.title = (TextView) convertView.findViewById(R.id.artistNameMusicLibraryEditor);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.artistCheckboxMusicLibraryEditor);

			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}
		
		final View finalConvertView = convertView;
		final String songId = c.getString(c.getColumnIndex(DBAccessHelper._ID));
		final String songArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		final String songBlacklistStatus = c.getString(c.getColumnIndex(DBAccessHelper.BLACKLIST_STATUS));
		String songAlbumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		
		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		
		//Set the song title.
		holder.title.setText(songArtist);
        mApp.getImageLoader().displayImage(songAlbumArtPath, holder.image, BlacklistManagerActivity.displayImageOptions);

        //Check if the song's DB ID exists in the HashSet and set the appropriate checkbox status.
        try {
            if (BlacklistManagerActivity.songIdBlacklistStatusPair.get(songId).equals("TRUE")) {
            	holder.checkBox.setChecked(true);
            	convertView.setBackgroundColor(0xCCFF4444);
            } else {
            	holder.checkBox.setChecked(false);
            	convertView.setBackgroundColor(0x00000000);
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        //Set a tag to the row that will attach the artist's name to it.
        convertView.setTag(R.string.artist, songArtist);
        
        holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {
				
				if (isChecked==true) {
					
					//Only receive inputs by the user and ignore any system-made changes to the checkbox state.
					if (checkbox.isPressed()) {
						finalConvertView.setBackgroundColor(0xCCFF4444);
						AsyncBlacklistArtistTask task = new AsyncBlacklistArtistTask(songArtist);
						task.execute(new String[] {"ADD"});
					}
					
				} else if (isChecked==false) {
					
					//Only receive inputs by the user and ignore any system-made changes to the checkbox state.
					if (checkbox.isPressed()) {
						finalConvertView.setBackgroundColor(0x00000000);
						AsyncBlacklistArtistTask task = new AsyncBlacklistArtistTask(songArtist);
						task.execute(new String[] {"REMOVE"});
						
					}

				}
				
			}
			
        });
 
		return convertView;
	}
    
	static class SongsListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public CheckBox checkBox;
	}
	
	/***************************************************************
	 * This AsyncTask goes through a specified artist and retrieves 
	 * every song by the artist and its ID. It then inserts the ID(s) 
	 * into a HashSet.
	 ***************************************************************/
	static class AsyncBlacklistArtistTask extends AsyncTask<String, String, String> {

		private String mArtistName;
		
		public AsyncBlacklistArtistTask(String artistName) {
			mArtistName = artistName;
		}
		
		@Override
		protected String doInBackground(String... params) {
			//Check if the user is adding or removing an artist from the blacklist.
			String operation = params[0];
			if (operation.equals("ADD")) {
				
				//Get a list of all songs in the album.
				Cursor cursor = mApp.getDBAccessHelper().getAllSongsByArtist(mArtistName);
				if (cursor.getCount() > 0) {
					for (int i=0; i < cursor.getCount(); i++) {
						cursor.moveToPosition(i);
						String songId = cursor.getString(cursor.getColumnIndex(DBAccessHelper._ID));
						
						//Update the HashMap.
						BlacklistManagerActivity.songIdBlacklistStatusPair.remove(songId);
						BlacklistManagerActivity.songIdBlacklistStatusPair.put(songId, true);
						
					}
					
				}
				
				if (cursor!=null) {
					cursor.close();
					cursor = null;
				}
				
			} else {
				//Get a list of all songs in the album.
				Cursor cursor = mApp.getDBAccessHelper().getAllSongsByArtist(mArtistName);
				if (cursor.getCount() > 0) {
					for (int i=0; i < cursor.getCount(); i++) {
						cursor.moveToPosition(i);
						String songId = cursor.getString(cursor.getColumnIndex(DBAccessHelper._ID));
						
						//Update the HashMap.
						BlacklistManagerActivity.songIdBlacklistStatusPair.remove(songId);
						BlacklistManagerActivity.songIdBlacklistStatusPair.put(songId, false);
						
					}
					
				}
				
				if (cursor!=null) {
					cursor.close();
					cursor = null;
				}
				
			}
			
			return null;
		}
		
	}
	
}
