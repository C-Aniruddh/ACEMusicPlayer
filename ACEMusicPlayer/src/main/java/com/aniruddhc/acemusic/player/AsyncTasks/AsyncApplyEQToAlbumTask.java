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

public class AsyncApplyEQToAlbumTask extends AsyncTask<String, Void, Void> {
	
    private Context mContext;
    private Common mApp;
	private String titleAlbum = "";
    
	private int mFiftyHertzLevel; 
	private int mOneThirtyHertzLevel; 
	private int mThreeTwentyHertzLevel; 
	private int mEightHundredHertzLevel; 
	private int mTwoKilohertzLevel; 
	private int mFiveKilohertzLevel; 
	private int mTwelvePointFiveKilohertzLevel; 
	private int mVirtualizerLevel; 
    private int mBassBoostLevel; 
    private int mReverbSetting;
	
    public AsyncApplyEQToAlbumTask(Context context, 
    							   String albumName, 
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
    	
    	mContext = context.getApplicationContext();
    	mApp = (Common) mContext;
    	titleAlbum = albumName;
    	
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
    
    protected void onPreExecute() {
    	Toast.makeText(mContext, R.string.applying_equalizer, Toast.LENGTH_SHORT).show();
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {
    	
    	int which = Integer.parseInt(params[0]);
    	
        //Get a cursor with the list of all albums.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueAlbums("");
    	
        //Get a list of all songs in the album.
		cursor.moveToPosition(which);
		String albumName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		String artistName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		
		Cursor songsCursor = mApp.getDBAccessHelper().getAllSongsInAlbum(albumName, artistName);
		
		//Loop through the songs and add them to the EQ settings DB with the current EQ settings.
		if (songsCursor!=null && songsCursor.getCount() > 0) {
			
			for (int j=0; j < songsCursor.getCount(); j++) {
				songsCursor.moveToPosition(j);
				String songId = songsCursor.getString(songsCursor.getColumnIndex(DBAccessHelper.SONG_ID));
				
				saveSettingsToDB(songId);
				
			}
			
		}
		
		cursor.close();
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
    				   mContext.getResources().getString(R.string.equalizer_applied_to_songs_in) + " " + titleAlbum + ".", 
    				   Toast.LENGTH_SHORT).show();
		
	}

}
