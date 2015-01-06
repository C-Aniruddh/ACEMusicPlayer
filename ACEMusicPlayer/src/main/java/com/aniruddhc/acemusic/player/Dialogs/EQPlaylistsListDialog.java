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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class EQPlaylistsListDialog extends DialogFragment {

	private Common mApp;
	private Activity parentActivity;
	private EqualizerActivity mFragment;
	
	public EQPlaylistsListDialog() {
		super();
	}
	
	public EQPlaylistsListDialog(EqualizerActivity fragment) {
		mFragment = fragment;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		parentActivity = getActivity();		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*//Get a cursor with the list of all user-created playlists.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueUserPlaylists();
        
        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				cursor.moveToPosition(which);
				AsyncApplyEQToPlaylistTask task = new AsyncApplyEQToPlaylistTask(parentActivity, mFragment, cursor.getString(
																								 			cursor.getColumnIndex(
																								 			DBAccessHelper.PLAYLIST_NAME)));
				
				task.execute(new String[] { "" + which });
				
			}
			
		}, DBAccessHelper.PLAYLIST_NAME);
*/
        return builder.create();
    }
	
}
