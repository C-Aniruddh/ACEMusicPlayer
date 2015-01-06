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
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncApplyEQToArtistTask extends AsyncTask<String, Void, Void> {
	
    private Context mContext;
    private Common mApp;
    private String mArtist;
    
	int mFiftyHertzLevel; 
    int mOneThirtyHertzLevel; 
    int mThreeTwentyHertzLevel; 
    int mEightHundredHertzLevel; 
    int mTwoKilohertzLevel; 
    int mFiveKilohertzLevel; 
    int mTwelvePointFiveKilohertzLevel; 
    int mVirtualizerLevel; 
    int mBassBoostLevel; 
    int mReverbSetting;
	
    public AsyncApplyEQToArtistTask(Context context, 
    								String artistName, 
    								int fiftyHertzLevel, 
     							   	int oneThirtyHertzLevel, 
     							   	int threeTwentyHertzLevel, 
     							   	int eightHundredHertzLevel, 
     							   	int twoKilohertzLevel, 
     							   	int fiveKilohertzLevel, 
     							   	int twelvePointFiveKilohertzLevel, 
     							   	int virtualizerLevel, 
     							   	int bassBoostLevel, 
     							   	int reverbSetting) {
    	
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	mArtist = artistName;
    	
    	mFiftyHertzLevel = fiftyHertzLevel;
        mOneThirtyHertzLevel = oneThirtyHertzLevel;
        mThreeTwentyHertzLevel = threeTwentyHertzLevel;
        mEightHundredHertzLevel = eightHundredHertzLevel;
        mTwoKilohertzLevel = twoKilohertzLevel;
        mFiveKilohertzLevel = fiveKilohertzLevel;
        mTwelvePointFiveKilohertzLevel = twelvePointFiveKilohertzLevel;
        mVirtualizerLevel = virtualizerLevel;
        mBassBoostLevel = bassBoostLevel;
        mReverbSetting = reverbSetting;
        
    }
 
    @Override
    public void onPreExecute() {
    	super.onPreExecute();
    	Toast.makeText(mContext, R.string.applying_equalizer, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected Void doInBackground(String... params) {
    	
		Cursor songsCursor = mApp.getDBAccessHelper().getAllSongsByArtist(mArtist);
		
		//Loop through the songs and add them to the EQ settings DB with the current EQ settings.
		if (songsCursor!=null && songsCursor.getCount() > 0) {
			
			for (int j=0; j < songsCursor.getCount(); j++) {
				songsCursor.moveToPosition(j);
				
				String songId = songsCursor.getString(songsCursor.getColumnIndex(DBAccessHelper.SONG_ID));
				saveSettingsToDB(songId);
				
			}
			
		}

		if (songsCursor!=null)
			songsCursor.close();
		
    	return null;
	    
    }
    
	/**
	 * Commit the settings to the database 
	 */
    public void saveSettingsToDB(String songId) {
		
		//Check if a database entry already exists for this song.
		if (mApp.getDBAccessHelper().hasEqualizerSettings(songId)==false) {
			
			//Add a new DB entry.
			mApp.getDBAccessHelper().addSongEQValues(songId,
												 	 mFiftyHertzLevel, 
												 	 mOneThirtyHertzLevel, 
												 	 mThreeTwentyHertzLevel, 
												 	 mEightHundredHertzLevel, 
												 	 mTwoKilohertzLevel, 
												 	 mFiveKilohertzLevel,
												 	 mTwelvePointFiveKilohertzLevel,
												 	 mVirtualizerLevel, 
												 	 mBassBoostLevel, 
												 	 mReverbSetting);
		} else {
			//Update the existing entry.
			mApp.getDBAccessHelper().updateSongEQValues(songId,
												 	    mFiftyHertzLevel, 
												 	    mOneThirtyHertzLevel, 
												 	    mThreeTwentyHertzLevel, 
												 	    mEightHundredHertzLevel, 
												 	    mTwoKilohertzLevel, 
												 	    mFiveKilohertzLevel,
												 	    mTwelvePointFiveKilohertzLevel,
												 	    mVirtualizerLevel, 
												 	    mBassBoostLevel, 
												 	    mReverbSetting);
		}

    }
    
    @Override
    protected void onPostExecute(Void arg0) {
    	Toast.makeText(mContext, 
    				   mContext.getResources().getString(R.string.equalizer_applied_to_songs_by) + " " + mArtist + ".", 
    				   Toast.LENGTH_SHORT).show();
		
	}

}
