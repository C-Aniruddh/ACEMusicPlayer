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

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class AsyncReorderPlaylistEntriesTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    
    private String mPlaylistId;
    private ArrayList<String> mSongId;
    private ArrayList<String> mSongEntryId;
    private String mAfterEntryId;
    private String mBeforeEntryId;
    private String mPlaylistName;
    private ArrayList<String> mEntryIds;
    private ArrayList<String> mSongIds;
    
    public AsyncReorderPlaylistEntriesTask(Context context, 
    								   	   String playlistId,
    								   	   String playlistName,
    								   	   ArrayList<String> songId,
    								   	   ArrayList<String> songEntryId,
    								   	   String afterEntryId,
    								   	   String beforeEntryId,
    								   	   ArrayList<String> entryIds,
    								   	   ArrayList<String> songIds) {
    	
    	mContext = context;
    	sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	
    	mPlaylistId = playlistId;
    	mSongId = songId;
    	mSongEntryId = songEntryId;
    	mAfterEntryId = afterEntryId;
    	mBeforeEntryId = afterEntryId;
    	mPlaylistName = playlistName;
    	mEntryIds = entryIds;
    	mSongIds = songIds;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
/*    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        	try {
    			GMusicClientCalls.reorderPlaylistEntryWebClient(mContext, 
    															mPlaylistId, 
    															mSongId, 
    															mSongEntryId, 
    															mAfterEntryId, 
    															mBeforeEntryId);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
    	}
    	
		//Update the playlists database.
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
		dbHelper.reorderSongInPlaylist(mContext, mPlaylistName, mPlaylistId, mEntryIds, mSongIds);
		dbHelper.close();
		dbHelper = null;*/
    	
    	return true;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    }

}
