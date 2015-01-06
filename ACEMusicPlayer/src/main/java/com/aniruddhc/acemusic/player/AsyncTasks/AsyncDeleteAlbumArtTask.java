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
package com.aniruddhc.acemusic.player.AsyncTasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncDeleteAlbumArtTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private Common mApp;
    
    private Activity mActivity;
    private String artist = "";
    private String album = "";
    private String mCallingFragment = null;
    private View mViewItem;
    private int mImageID;
    private ImageView frontImage;
    
	public static ArrayList<String> dataURIsList = new ArrayList<String>();
	public static ArrayList<String> albumArtPathsList = new ArrayList<String>();
    
    public AsyncDeleteAlbumArtTask(Context context, View viewItem, int imageID, Activity activity, String callingFragment) {
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	mViewItem = viewItem;
    	mImageID = imageID;
    	mActivity = activity;
    	mCallingFragment = callingFragment;
    }
 
    @Override
    protected Void doInBackground(String... params) {
    	
    	if (params.length==2) {
    		artist = params[0];
    		album = params[1];
    	}
    	
		/*
		 * Loop through the songs table and retrieve the data paths of all the songs (used to embed the artwork).
		 */
		
		//Remove the + and replace them back with spaces. Also replace any rogue apostrophes.
		try {
			if (album.contains("+")) {
				album = album.replace("+", " ");
			}
			
			if (album.contains("'")) {
				album = album.replace("'", "''");
			}
		
			if (artist.contains("+")) {
				artist = artist.replace("+", " ");
			}
			
			if (artist.contains("'")) {
				artist = artist.replace("'", "''");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		String selection = DBAccessHelper.SONG_ALBUM + "=" + "'" + album + "'" + " AND "
						 + DBAccessHelper.SONG_ARTIST + "=" + "'" + artist + "'";
		
		String[] projection = { DBAccessHelper._ID, 
								DBAccessHelper.SONG_FILE_PATH, 
								DBAccessHelper.SONG_ALBUM_ART_PATH };
		
		Cursor cursor = mApp.getDBAccessHelper().getWritableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
											 				 				 projection, 
											 				 				 selection, 
											 				 				 null, 
											 				 				 null,
											 				 				 null, 
											 				 				 null);
		
		cursor.moveToFirst();
		if (cursor.getCount()!=0) {
			dataURIsList.add(cursor.getString(1));
			albumArtPathsList.add(cursor.getString(2));
		}
		
		while(cursor.moveToNext()) {
			dataURIsList.add(cursor.getString(1));
			albumArtPathsList.add(cursor.getString(2));
		}
		
		for (int i=0; i < dataURIsList.size(); i++) {
	       	
	       	File audioFile = new File(dataURIsList.get(i));
    		AudioFile f = null;
    		
			try {
				f = AudioFileIO.read(audioFile);
			} catch (CannotReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadOnlyFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAudioFrameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Tag tag = null;
			if (f!=null) {
				tag = f.getTag();
			} else {
				continue;
			}
			
    		try {
    			tag.deleteArtworkField();
    		} catch (KeyNotFoundException e) {
    			Toast.makeText(mContext, R.string.album_doesnt_have_artwork, Toast.LENGTH_LONG).show();
    		}
    		
    		try {
				f.commit();
			} catch (CannotWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		//Check if the current song's album art is a JPEG file.
    		if (albumArtPathsList.get(i).startsWith("/")) {
    			File file = new File(albumArtPathsList.get(i));
    			if (file!=null) {
    				if (file.exists()) {
    					file.delete();
    				}
    				
    			}
    			
    		}
    		
    		//Remove the album art from the album art database.
    		String filePath = dataURIsList.get(i);
    		filePath = filePath.replace("'", "''");
    		String where = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + filePath + "'";
    		
    		ContentValues values = new ContentValues();
    		values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, "");
    		mApp.getDBAccessHelper().getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
    											  				  values, 
    											  				  where, 
    											  				  null);
    		
		}
		
		//Refresh the memory/disk cache.
		mApp.getImageLoader().clearDiscCache();
		mApp.getImageLoader().clearMemoryCache();
		
		cursor.close();
		cursor = null;
		
    	return null;
	    
    }

    @Override
    protected void onPostExecute(Void arg0) {
    	super.onPostExecute(arg0);
    	
		Toast.makeText(mContext, R.string.album_art_deleted, Toast.LENGTH_LONG).show();
		
		//Update the UI.
		mApp.broadcastUpdateUICommand(new String[] {  }, new String[] {  });
		
	}

}
