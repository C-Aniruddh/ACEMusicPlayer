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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.GMusicHelpers.GMusicClientCalls;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncGetSongStreamURLTask extends AsyncTask<String, Void, Boolean> {
    private Context mContext;
    private Common mApp;
    private String mSongID;
    
    public AsyncGetSongStreamURLTask(Context context, String songID) {
    	mContext = context;
    	mApp = (Common) mContext;
    	mSongID = songID;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
    	if (mSongID.equals(mApp.getService().getCurrentSong().getId())) {
    		try {
    			mApp.getService().getCurrentSong().setFilePath(GMusicClientCalls.getSongStream(mSongID).toURL().toString());
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    			return false;
    		} catch (JSONException e) {
    			e.printStackTrace();
    			return false;
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    			return false;
    		} catch (Exception e) {
    			e.printStackTrace();
    			return false;
    		}
        	
        	if (mApp.getService().getCurrentSong().getId()==null) {
        		return false;
        	} else {
        		return true;
        	}
        	
    	} else {
    		this.cancel(true);
    		return false;
    	}
    	
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
		try {
			if (result==true && mApp.getService().getCurrentSong().getId().equals(mSongID)) {
				//We got the right URL, so go ahead and prepare the media player.
				mApp.getService().startPlayback();
			} else if (result==false && mApp.getService().getCurrentSong().getId().equals(mSongID)) {
				//We were unable to get the url, so skip to the next song.
				mApp.getService().skipToNextTrack();
				Toast.makeText(mContext, R.string.song_failed_to_load, Toast.LENGTH_LONG).show();
			} else {
				//The song has been changed, so the URL is now useless. Exit this AsyncTask.
				return;
			}
			
		} catch (Exception e) {
			return;
		}

	}

}
