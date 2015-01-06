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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class ViewPlaylistInformationDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;

	private TextView playlistNameText;
	private TextView playlistFormatText;
	private TextView playlistLocationText;
	private TextView playlistNumberOfSongsText;
	private TextView playlistLastModifiedText;
	private TextView playlistAddedToLibraryText;
	private TextView playlistCreatedText;
	
	private TextView playlistNameValue;
	private TextView playlistFormatValue;
	private TextView playlistLocationValue;
	private TextView playlistNumberOfSongsValue;
	private TextView playlistLastModifiedValue;
	private TextView playlistAddedToLibraryValue;
	private TextView playlistCreatedValue;
	
	private String playlistName = "";
	private String playlistFilePath = "";
	private String playlistFormat = "";
	private String playlistNumberOfSongs = "";
	private String playlistLastModified = "";
	private String playlistAddedToLibrary = "";
	private String playlistCreated = "";
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = this;
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.dialog_playlist_information, null);
		
		playlistName = getArguments().getString("PLAYLIST_NAME");
		playlistFilePath = getArguments().getString("PLAYLIST_FILE_PATH");
		
		//Header text declarations.
		playlistNameText = (TextView) rootView.findViewById(R.id.playlist_name_text);
		playlistNameText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistNameText.setPaintFlags(playlistNameText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistFormatText = (TextView) rootView.findViewById(R.id.playlist_format_text);
		playlistFormatText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistFormatText.setPaintFlags(playlistFormatText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistLocationText = (TextView) rootView.findViewById(R.id.playlist_location_text);
		playlistLocationText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistLocationText.setPaintFlags(playlistLocationText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistNumberOfSongsText = (TextView) rootView.findViewById(R.id.playlist_number_of_songs_text);
		playlistNumberOfSongsText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistNumberOfSongsText.setPaintFlags(playlistNumberOfSongsText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistLastModifiedText = (TextView) rootView.findViewById(R.id.playlist_last_modified_text);
		playlistLastModifiedText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistLastModifiedText.setPaintFlags(playlistLastModifiedText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistAddedToLibraryText = (TextView) rootView.findViewById(R.id.playlist_added_to_library_text);
		playlistAddedToLibraryText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistAddedToLibraryText.setPaintFlags(playlistAddedToLibraryText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		playlistCreatedText = (TextView) rootView.findViewById(R.id.playlist_created_text);
		playlistCreatedText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistCreatedText.setPaintFlags(playlistCreatedText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		//Values declarations.
		playlistNameValue = (TextView) rootView.findViewById(R.id.playlist_name_value);
		playlistNameValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistNameValue.setPaintFlags(playlistNameValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistFormatValue = (TextView) rootView.findViewById(R.id.playlist_format_value);
		playlistFormatValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistFormatValue.setPaintFlags(playlistFormatValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistLocationValue = (TextView) rootView.findViewById(R.id.playlist_location_value);
		playlistLocationValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistLocationValue.setPaintFlags(playlistLocationValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistNumberOfSongsValue = (TextView) rootView.findViewById(R.id.playlist_number_of_songs_value);
		playlistNumberOfSongsValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistNumberOfSongsValue.setPaintFlags(playlistNumberOfSongsValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistLastModifiedValue = (TextView) rootView.findViewById(R.id.playlist_last_modified_value);
		playlistLastModifiedValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistLastModifiedValue.setPaintFlags(playlistLastModifiedValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistAddedToLibraryValue = (TextView) rootView.findViewById(R.id.playlist_added_to_library_value);
		playlistAddedToLibraryValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistAddedToLibraryValue.setPaintFlags(playlistAddedToLibraryValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		playlistCreatedValue = (TextView) rootView.findViewById(R.id.playlist_created_value);
		playlistCreatedValue.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		playlistCreatedValue.setPaintFlags(playlistCreatedValue.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		//Set the playlist name and the file format.
		playlistNameValue.setText(playlistName);
		
		int extensionDotIndex = playlistFilePath.lastIndexOf(".");
		
		String extension = "";
		if (!playlistFilePath.isEmpty()) {
			extension = playlistFilePath.substring(extensionDotIndex, playlistFilePath.length());
		} else {
			extension = "Unknown";
		}
		playlistFormatValue.setText(extension);
		
		/*//Retrieve the information that needs to tbe displayed in the dialog.
		DBAccessHelper dbHelper = new DBAccessHelper(parentActivity);
		Cursor cursor = dbHelper.getPlaylistByFilePath(playlistFilePath);
		
		if (cursor.getCount() > 0) {
			//Get the number of songs in the playlist.
			playlistNumberOfSongs = cursor.getCount() + "";
			playlistNumberOfSongsValue.setText(playlistNumberOfSongs);
			
			//Get the date that the playlist was added to the library.
			cursor.moveToFirst();
			
			//Note that addTime isn't the actual "Last modification" date. It's actually the date the playlist was added to the library.
			long addTime = cursor.getLong(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_LAST_MODIFIED));
			Date addDate = new Date(addTime);
			
			SimpleDateFormat addDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm aa", Locale.getDefault());
			addDateFormat.setTimeZone(TimeZone.getDefault());
			playlistAddedToLibrary = addDateFormat.format(addDate);
			
			playlistAddedToLibraryValue.setText(playlistAddedToLibrary);
			
		}
		
		//Get a File that points to the playlist file on the filesystem.
		File file = new File(playlistFilePath);
		long lastModifiedTime = file.lastModified();
		
		if (lastModifiedTime==0) {
			playlistLastModifiedValue.setText("Unknown");
		} else {
			Date lastModifiedDate = new Date(lastModifiedTime);
			
			SimpleDateFormat lastModifiedDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm aa", Locale.getDefault());
			lastModifiedDateFormat.setTimeZone(TimeZone.getDefault());
			
			playlistLastModified = lastModifiedDateFormat.format(lastModifiedDate);
			playlistLastModifiedValue.setText(playlistLastModified);
			playlistCreatedValue.setText(playlistLastModified);
		}*/
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(playlistName);
        builder.setView(rootView);
        builder.setNegativeButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				
			}
        	
        });

        return builder.create();
    }
	
}
