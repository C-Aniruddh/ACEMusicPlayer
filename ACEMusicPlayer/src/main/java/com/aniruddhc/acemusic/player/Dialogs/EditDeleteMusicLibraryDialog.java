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
package com.aniruddhc.acemusic.player.Dialogs;

import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.EditDeleteMusicLibraryAdapter;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.MusicLibraryEditorActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class EditDeleteMusicLibraryDialog extends DialogFragment {
	
	private static Common mApp;
	private String operation;
	private Cursor cursor;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mApp = (Common) getActivity().getApplicationContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		//Get the operation that needs to be performed (edit vs. delete).
		operation = getArguments().getString("OPERATION");
		if (operation.equals("EDIT")) {
			builder.setTitle(R.string.edit_music_library);
		} else {
			builder.setTitle(R.string.delete_music_library);
		}
		
		//Get a cursor with a list of all the music libraries on the device.
		cursor = mApp.getDBAccessHelper().getAllUniqueUserLibraries(getActivity().getApplicationContext());
		
		if (cursor.getCount()==0) {
			getActivity().finish();
			Toast.makeText(getActivity(), R.string.no_music_libraries_found, Toast.LENGTH_SHORT).show();
		}
		
		builder.setAdapter(new EditDeleteMusicLibraryAdapter(getActivity(), cursor), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Get the name of the library that has just been clicked. Also get its lable color code.
				cursor.moveToPosition(which);
				String libraryName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LIBRARY_NAME));
				String libraryColorCode = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LIBRARY_TAG));
				
				if (operation.equals("DELETE")) {
					//Loop through the DB and look for entries that have the specified name and color code. Delete those entries.
					mApp.getDBAccessHelper().deleteLibrary(libraryName, libraryColorCode);
					
					//Display a toast message.
					String toastMessage = getActivity().getResources().getString(R.string.deleted) + " " + libraryName;
					Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
					
				} else {
					AsyncGetLibrarySongIdsTask task = new AsyncGetLibrarySongIdsTask(getActivity().getApplicationContext(),
																					 libraryName,
																					 libraryColorCode);
					task.execute();
					
				}
				
				dialog.dismiss();
			}
			
		});
        
        return builder.create();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (cursor!=null) {
			cursor.close();
			cursor = null;
		}
		
		getActivity().finish();
		
	}

	/******************************************************************************
	 * This asynchronous task retrieves the list of all song IDs within a library.
	 ******************************************************************************/
	static class AsyncGetLibrarySongIdsTask extends AsyncTask<String, String, String> {
		
		private Context mContext;
		private String mLibraryName;
		private String mLibraryColorCode;
		private HashSet<String> songIdsHashSet = new HashSet<String>();
		
		public AsyncGetLibrarySongIdsTask(Context context, String libraryName, String libraryColorCode) {
			mContext = context;
			mLibraryName = libraryName;
			mLibraryColorCode = libraryColorCode;
			
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			songIdsHashSet = mApp.getDBAccessHelper().getAllSongIdsInLibrary(mLibraryName, mLibraryColorCode);
			return null;
		}
		
		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);
			//Launch the music library editor activity.
			Intent intent = new Intent(mContext, MusicLibraryEditorActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("LIBRARY_NAME", mLibraryName);
			bundle.putString("LIBRARY_ICON", mLibraryColorCode);
			bundle.putSerializable("SONG_IDS_HASH_SET", songIdsHashSet);
			intent.putExtras(bundle);
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			
		}
		
	}
	
}
