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

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;

public class AsyncRemovePinnedSongsTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private String mSelection;
    private Cursor mSmartPlaylistCursor;
    
    public AsyncRemovePinnedSongsTask(Context context, String selection, Cursor smartPlaylistCursor) {
    	//Context.
    	mContext = context;
    	mSelection = selection;
    	mSmartPlaylistCursor = smartPlaylistCursor;
    }
    
    protected void onPreExecute() {
    	super.onPreExecute();
    	Toast.makeText(mContext, R.string.removing_pinned_songs, Toast.LENGTH_SHORT).show();
    }
 
	@Override
    protected Boolean doInBackground(String... params) {
		
		//Delete the specified local copies of the song(s) and remove the local copy reference from the DB.
		Cursor cursor = null;
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
		if (mSmartPlaylistCursor==null) {
			mSelection += " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
			cursor = dbHelper.getAllSongsSearchable(mSelection);
		} else {
			cursor = mSmartPlaylistCursor;
		}
		
		if (cursor!=null) {
			for (int i=0; i < cursor.getCount(); i++) {
				try {
					cursor.moveToPosition(i);
					String localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
					String songID = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ID));
					
					File file = new File(localCopyPath);
					if (file!=null && file.exists()) {
						file.delete();
					}
					
					String selection = DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
					ContentValues values = new ContentValues();
					values.put(DBAccessHelper.LOCAL_COPY_PATH, "");
					dbHelper.getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, selection, null);
					
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}
			
		}
		
		if (dbHelper!=null) {
			dbHelper.close();
			dbHelper = null;
		}

    	return true;
    }
   
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);	
		Toast.makeText(mContext, R.string.pinned_songs_removed, Toast.LENGTH_SHORT).show();
		
	}

}
