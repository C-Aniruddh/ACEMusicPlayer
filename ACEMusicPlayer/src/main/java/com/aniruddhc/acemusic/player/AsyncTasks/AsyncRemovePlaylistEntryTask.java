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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class AsyncRemovePlaylistEntryTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private String mEntryId;
    
    public AsyncRemovePlaylistEntryTask(Context context, String entryId) {
    	mContext = context;
    	sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	mEntryId = entryId;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {

/*    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		try {
    			GMusicClientCalls.putDeletePlaylistEntryRequest(mEntryId);
    			GMusicClientCalls.modifyPlaylist(mContext);
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}

		//Update the playlists database.
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
		dbHelper.deleteSongFromPlaylist(mEntryId);
		dbHelper.close();
		dbHelper = null;*/
    	
    	return true;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    }

}
