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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;

public class AsyncApplyEQToPlaylistTask extends AsyncTask<String, Void, Boolean> {
    private Context mContext;
    private EqualizerActivity mEqualizerFragment;
    private ProgressDialog pd;
    
	int max = 10000;
	int progressIncrement;
	int progress = 0;
	String songTitle = "";
	String songArtist = "";
	String titlePlaylist = "";
    String songAlbum = "";
	
    public AsyncApplyEQToPlaylistTask(Context context, EqualizerActivity fragment, String playlistName) {
    	mContext = context;
    	mEqualizerFragment = fragment;
    	titlePlaylist = playlistName;
    	
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mContext);
		pd.setIndeterminate(false);
		pd.setMax(max);
		pd.setCancelable(false);
		pd.setTitle(mContext.getResources().getString(R.string.applying_equalizer_to) + " " + titlePlaylist);
		pd.show();
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
    	/*int which = Integer.parseInt(params[0]);
    	
        //Get a cursor with the list of all user-created playlists.
        DBAccessHelper dbHelper = new DBAccessHelper(mContext);
        final Cursor cursor = dbHelper.getAllUniqueUserPlaylists();
    	
        //Get the file path of the selected playlist.
		cursor.moveToPosition(which);
		String playlistFilePath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_FILE_PATH));
		cursor.close();
		
		//Get a list of all songs in the playlist.
		Cursor playlistCursor = dbHelper.getPlaylistByFilePath(playlistFilePath);
		String songFilePath = "";
		
		//Loop through the songs and add them to the EQ settings DB with the current EQ settings.
		if (playlistCursor!=null && playlistCursor.getCount() > 0) {
			
			MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
			songTitle = "";
			songAlbum = "";
			String songArtist = "";
			for (int j=0; j < playlistCursor.getCount(); j++) {
				playlistCursor.moveToPosition(j);
				songFilePath = playlistCursor.getString(playlistCursor.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_FILE_PATH));
				
				try {
					mmdr.setDataSource(songFilePath);
				} catch (Exception e) {
					return false;
				}
				
				songTitle = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				songAlbum = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				songArtist = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);	
				
				mEqualizerFragment.saveSettingsToDB(songTitle, songAlbum, songArtist);
				publishProgress();
				
			}
			
		}
    	
		playlistCursor.close();*/
    	return true;
	    
    }
    
    @Override
    protected void onProgressUpdate(Void... v) {
		//Update the progress on the progress dialog.
		progress = progress + progressIncrement;
		pd.setProgress(progress);
		pd.setMessage(mContext.getResources().getString(R.string.applying_to) + " " + songTitle);
    	
    }

    @Override
    protected void onPostExecute(Boolean successStatus) {
    	pd.dismiss();
    	
    	if (successStatus==true) {
        	Toast.makeText(mContext, 
		 				   mContext.getResources().getString(R.string.equalizer_applied_to_songs_in) + " " + titlePlaylist + ".", 
		 				   Toast.LENGTH_LONG).show();
    	} else {
        	Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_LONG).show();
    	}
		
	}

}
