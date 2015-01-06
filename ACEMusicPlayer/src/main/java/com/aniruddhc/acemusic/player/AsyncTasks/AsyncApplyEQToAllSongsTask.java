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
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncApplyEQToAllSongsTask extends AsyncTask<String, Void, Void> {
	
    private Context mContext;
    private Common mApp;
    private EqualizerActivity mEqualizerFragment;
    
    public AsyncApplyEQToAllSongsTask(Context context, EqualizerActivity fragment) {
    	mContext = context;
    	mEqualizerFragment = fragment;
    	mApp = (Common) context.getApplicationContext();
    }
    
    protected void onPreExecute() {
		Toast.makeText(mContext, R.string.applying_equalizer_to_all_songs, Toast.LENGTH_SHORT).show();

    }
 
    @Override
    protected Void doInBackground(String... params) {
    	
		//Get a cursor with all the songs in the library.
		Cursor songsCursor = mApp.getDBAccessHelper().getAllSongs();
    	
		//Loop through the songs and add them to the EQ settings DB with the current EQ settings.
		if (songsCursor!=null && songsCursor.getCount() > 0) {
			
			for (int j=0; j < songsCursor.getCount(); j++) {
				songsCursor.moveToPosition(j);

				String songId = songsCursor.getString(songsCursor.getColumnIndex(DBAccessHelper.SONG_ID));
				mEqualizerFragment.setEQValuesForSong(songId);

			}

		}
    	
		if (songsCursor!=null)
			songsCursor.close();
		
    	return null;
	    
    }

    @Override
    protected void onPostExecute(Void arg0) {
		Toast.makeText(mContext, R.string.equalizer_applied_to_all_songs, Toast.LENGTH_SHORT).show();
		
	}

}
