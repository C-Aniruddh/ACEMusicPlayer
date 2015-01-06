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

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncCreateNewPlaylistTask;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncAddSongsToPlaylistTask;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;

public class AddToPlaylistDialog extends DialogFragment {

	private Context mContext;
	private String ADD_TYPE;
	private String ARTIST;
	private String ALBUM;
	private String ALBUM_ARTIST;
	private String SONG;
	private String GENRE;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mContext = getActivity().getApplicationContext();
		
		//Retrieve the arguments.
		ADD_TYPE = getArguments().getString("ADD_TYPE");
		
		if (ADD_TYPE.equals("ARTIST")) {
			ARTIST = getArguments().getString("ARTIST");
		} else if (ADD_TYPE.equals("ALBUM_ARTIST")) {
			ALBUM_ARTIST = getArguments().getString("ALBUM_ARTIST");
		} else if (ADD_TYPE.equals("ALBUM")) {
			ARTIST = getArguments().getString("ARTIST");
			ALBUM = getArguments().getString("ALBUM");
		} else if (ADD_TYPE.equals("SONG")) {
			ARTIST = getArguments().getString("ARTIST");
			ALBUM = getArguments().getString("ALBUM");
			SONG = getArguments().getString("SONG");
		} else if (ADD_TYPE.equals("GENRE")) {
			GENRE = getArguments().getString("GENRE");
		} else if (ADD_TYPE.equals("ALBUM_BY_ALBUM_ARTIST")) {
			ALBUM = getArguments().getString("ALBUM");
			ALBUM_ARTIST = getArguments().getString("ALBUM_ARTIST");
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String columns[] = { DBAccessHelper.PLAYLIST_NAME, DBAccessHelper._ID,
        					 DBAccessHelper.PLAYLIST_FILE_PATH, DBAccessHelper.PLAYLIST_SOURCE,
        					 DBAccessHelper.PLAYLIST_ID };
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new String[] { getActivity().getResources().getString(R.string.new_playlist), "0", "0", "0", "0" });
        
        DBAccessHelper playlistsDBHelper = new DBAccessHelper(getActivity()
        																				    .getApplicationContext());
        Cursor userPlaylistsCursor = playlistsDBHelper.getAllSongsInAlbum(null, null);
        final MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, userPlaylistsCursor });
        
        //Set the dialog title.
        builder.setTitle(R.string.add_to_playlist);
        builder.setCursor(mergeCursor, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Index 0 is the dummy playlist that will open the "New Playlist" dialog.
				if (which==0) {
					showNewPlaylistDialog();
				} else {
					mergeCursor.moveToPosition(which);
					String playlistName = mergeCursor.getString(mergeCursor.getColumnIndex(DBAccessHelper.PLAYLIST_NAME));
					String playlistId = mergeCursor.getString(mergeCursor.getColumnIndex(DBAccessHelper.PLAYLIST_ID));

					AsyncAddSongsToPlaylistTask task = new AsyncAddSongsToPlaylistTask(mContext,
																					   playlistName,
																					   playlistId,
																					   ARTIST,
																					   ALBUM,
																					   SONG,
																					   GENRE,
																					   ALBUM_ARTIST, 
																					   ADD_TYPE);
					task.execute();
					
				}
				
			}
        	
        }, DBAccessHelper.PLAYLIST_NAME);

        return builder.create();
    }

	//Displays the "Add New Playlist" dialog.
	public void showNewPlaylistDialog() {
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_new_playlist_dialog_layout, null);
		final EditText newPlaylistEditText = (EditText) dialogView.findViewById(R.id.new_playlist_name_text_field);
		newPlaylistEditText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		newPlaylistEditText.setPaintFlags(newPlaylistEditText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.new_playlist);
		builder.setView(dialogView);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Retrieve the name of the new playlist.
				String playlistName = newPlaylistEditText.getText().toString();
				AsyncCreateNewPlaylistTask task = new AsyncCreateNewPlaylistTask(mContext, 
																				 playlistName, 
																				 ARTIST, 
																				 ALBUM, 
																				 SONG, 
																				 GENRE, 
																				 ALBUM_ARTIST,
																				 ADD_TYPE);
				task.execute();
				dialog.dismiss();
				
			}
			
		});
		
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
			
		});
		
		builder.create().show();
	}
	
}
