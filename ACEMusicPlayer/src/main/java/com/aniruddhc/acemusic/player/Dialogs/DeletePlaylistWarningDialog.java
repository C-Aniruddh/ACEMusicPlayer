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
/*package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncDeletePlaylistTask;
import com.jams.music.player.Utils.TypefaceProvider;

public class DeletePlaylistWarningDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;
	private TextView cautionText;
	private String mPlaylistId;
	private String mPlaylistFilePath;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = this;
		
		mPlaylistId = getArguments().getString("PLAYLIST_ID");
		mPlaylistFilePath = getArguments().getString("PLAYLIST_FILE_PATH");
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.fragment_delete_playlists_warning, null);
		
		cautionText = (TextView) rootView.findViewById(R.id.warning_text);
		cautionText.setText(R.string.delete_playlist_warning);
		cautionText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		cautionText.setPaintFlags(cautionText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.warning);
        builder.setView(rootView);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				
			}
        	
        });
        
        builder.setPositiveButton(R.string.delete, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();

				if (mPlaylistId!=null && !mPlaylistId.isEmpty()) {
					try {
						deleteMediaStorePlaylist(mPlaylistId);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					AsyncDeletePlaylistTask task = new AsyncDeletePlaylistTask(parentActivity.getApplicationContext(), 
																			   mPlaylistId,
																			   mPlaylistFilePath);
					task.execute();
					
				} else {
					//Notify the user that they can't delete a smart playlist.
					Toast.makeText(parentActivity, R.string.playlist_cannot_be_deleted, Toast.LENGTH_LONG).show();
					
				}
				
			}
        	
        });

        return builder.create();
    }
	
    private void deleteMediaStorePlaylist(String playlistId) {
    	try {
    	    ContentResolver resolver = getActivity().getContentResolver();
    	    String where = MediaStore.Audio.Playlists._ID + "=?";
    	    String[] whereVal = { playlistId }; 
    	    resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);     
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
	
}
*/
