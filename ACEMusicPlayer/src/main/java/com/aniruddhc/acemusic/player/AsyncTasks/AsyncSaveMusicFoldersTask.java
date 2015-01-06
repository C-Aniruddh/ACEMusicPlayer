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
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncSaveMusicFoldersTask extends AsyncTask<String, Void, Boolean> {
	
    private Context mContext;
    private Common mApp;
    private HashMap<String, Boolean> mMusicFolders;
    private List<String> mPathsList;
    
    public AsyncSaveMusicFoldersTask(Context context, HashMap<String, Boolean> musicFolders) {
    	mContext = context;
    	mApp = (Common) mContext;
    	mMusicFolders = musicFolders;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
		//Clear the DB and insert the new selections (along with the old ones).
		mApp.getDBAccessHelper().deleteAllMusicFolderPaths();
		try {
			mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
			
			//Retrieve a list of all keys in the hash map (key = music folder path).
			mPathsList = new ArrayList<String>(mMusicFolders.keySet());
				
			for (int i=0; i < mMusicFolders.size(); i++) {
				String path = mPathsList.get(i);
				boolean include = mMusicFolders.get(path);

				//Trim down the folder path to include only the folder and its parent.
				int secondSlashIndex = path.lastIndexOf("/", path.lastIndexOf("/")-1);
				if ((secondSlashIndex < path.length()) && secondSlashIndex!=-1)
					path = path.substring(secondSlashIndex, path.length());
				
		        ContentValues values = new ContentValues();
		        values.put(DBAccessHelper.FOLDER_PATH, path);
		        values.put(DBAccessHelper.INCLUDE, include);

		        mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_FOLDERS_TABLE, null, values);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
			mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
		}
		
		//Populate the UserLibraries table.
		try {
			mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
			
			//Insert the default libaries.
			ContentValues allLibrariesValues = new ContentValues();
			allLibrariesValues.put(DBAccessHelper.LIBRARY_NAME, mContext.getResources().getString(R.string.all_libraries));
			allLibrariesValues.put(DBAccessHelper.SONG_ID, "ALL_LIBRARIES");
			allLibrariesValues.put(DBAccessHelper.LIBRARY_TAG, "circle_blue_dark");
			mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.LIBRARIES_TABLE, null, allLibrariesValues);
			
			ContentValues googlePlayMusicLibrary = new ContentValues();
			googlePlayMusicLibrary.put(DBAccessHelper.LIBRARY_NAME, mContext.getResources().getString(R.string.google_play_music_no_asterisk));
			googlePlayMusicLibrary.put(DBAccessHelper.SONG_ID, DBAccessHelper.GMUSIC);
			googlePlayMusicLibrary.put(DBAccessHelper.LIBRARY_TAG, "circle_yellow_dark");
			mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.LIBRARIES_TABLE, null, googlePlayMusicLibrary);
			
			//Default to "All Libraries".
			mApp.getSharedPreferences().edit().putString(Common.CURRENT_LIBRARY, mContext.getResources().getString(R.string.all_libraries)).commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
			mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
			
			//Use "All Libraries" as the default library.
			mApp.getSharedPreferences().edit().putString(Common.CURRENT_LIBRARY, mContext.getResources().getString(R.string.all_libraries)).commit();
			
		}
    	
    	return true;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
	}

}
