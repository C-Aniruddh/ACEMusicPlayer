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
/*package com.jams.music.player.AsyncTasks;

import java.io.File;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.GMusicHelpers.GMusicClientCalls;
import com.jams.music.player.Utils.Common;

public class AsyncDeletePlaylistTask extends AsyncTask<String, Void, Boolean> {
	
    private Context mContext;
    private Common mApp;
    
    private String mPlaylistId;
    private String mPlaylistFilePath;
    
    public AsyncDeletePlaylistTask(Context context, String playlistId, String playlistFilePath) {
    	
    	mContext = context;
    	mApp = (Common) mApp;
    	mPlaylistId = playlistId;
    	mPlaylistFilePath = playlistFilePath;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
    	String result = null;
    	if (mApp.isGooglePlayMusicEnabled()) {
        	try {
    			result = GMusicClientCalls.deletePlaylist(mContext, mPlaylistId);
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
    	}
    	
    	if (result!=null || mApp.isGooglePlayMusicEnabled()==false) {
			mApp.getDBAccessHelper().deletePlaylistById(mPlaylistId);
			
			if (mPlaylistFilePath!=null) {
				File file = new File(mPlaylistFilePath);
				if (file.exists()) {
					file.delete();
				}
				
			}
			
    	}
    	
    	return true;
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Toast.makeText(mContext, R.string.playlist_deleted, Toast.LENGTH_SHORT).show();

		//Update the playlists UI.
		mApp.broadcastUpdateUICommand();
		
	}

}*/
