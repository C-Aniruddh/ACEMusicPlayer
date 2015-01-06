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

import java.util.HashSet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

/**************************************************************************************
 * This AsyncTask creates the specified music library.
 * 
 * @author Saravan Pantham
 **************************************************************************************/
public class AsyncCreateMusicLibraryTask extends AsyncTask<String, Void, Void> {
	
	private Activity mActivity;
    private Context mContext;
    private Common mApp;
    private HashSet<String> mSongDBIds = new HashSet<String>();
    private String mLibraryName;
    private String mLibraryColorCode;
    
    public AsyncCreateMusicLibraryTask(Activity activity,
    								   Context context, 
    								   HashSet<String> songDBIds, 
    								   String libraryName, 
    								   String libraryColorCode) {
    	mActivity = activity;
    	mContext = context;
    	mApp = (Common) context.getApplicationContext();
    	mSongDBIds = songDBIds;
    	mLibraryName = libraryName;
    	mLibraryColorCode = libraryColorCode;
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {

    	//Delete the library if it currently exists.
    	mApp.getDBAccessHelper().deleteLibrary(mLibraryName, mLibraryColorCode);
    	
    	try {
    		mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
    		
    		//HashSets aren't meant to be browsable, so convert it into an array.
    		String[] songIdsArray = new String[mSongDBIds.size()];
    		mSongDBIds.toArray(songIdsArray);

    		//Loop through the array and add the songIDs to the library.
    		for (int i=0; i < songIdsArray.length; i++) {
    			ContentValues values = new ContentValues();
    			values.put(DBAccessHelper.LIBRARY_NAME, mLibraryName);
    			values.put(DBAccessHelper.SONG_ID, songIdsArray[i]);
    			values.put(DBAccessHelper.LIBRARY_TAG, mLibraryColorCode);
    			
        		mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.LIBRARIES_TABLE, null, values);
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	} finally {
    		mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
    		mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
    	}
    	
    	return null;
	    
    }

    @Override
    protected void onPostExecute(Void arg0) {
    	mActivity.finish();
    	Toast.makeText(mContext, R.string.done_creating_library, Toast.LENGTH_LONG).show();
       
    }

}
