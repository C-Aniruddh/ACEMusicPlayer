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

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.LibraryLabelsAdapter;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.MusicLibraryEditorActivity;

public class AddMusicLibraryDialog extends DialogFragment {

	private String libraryLabelID;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		libraryLabelID = "circle_blue_dark";
		View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_music_library, null);
		TextView instructions = (TextView) rootView.findViewById(R.id.add_music_library_instructions);
		instructions.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		instructions.setPaintFlags(instructions.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		final ImageButton labelButton = (ImageButton) rootView.findViewById(R.id.add_music_library_label_button);
		final EditText musicLibraryName = (EditText) rootView.findViewById(R.id.add_music_library_text_field);
		musicLibraryName.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		musicLibraryName.setPaintFlags(musicLibraryName.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		labelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				//Create a new array with a list of all the library labels.
				final Drawable[] labelsArray = { getActivity().getResources().getDrawable(R.drawable.circle_blue_dark),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_blue_light),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_green_dark),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_green_light),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_purple_dark),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_purple_light),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_red_dark),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_red_light),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_yellow_dark),
											   	 getActivity().getResources().getDrawable(R.drawable.circle_yellow_light) };
				
				//Create a new array with a list of all the library labels.
				final String[] labelsIdsArray = { "circle_blue_dark",
											   	  "circle_blue_light",
											   	  "circle_green_dark",
											   	  "circle_green_light",
											   	  "circle_purple_dark",
											   	  "circle_purple_light",
											   	  "circle_red_dark",
											   	  "circle_red_light",
											   	  "circle_yellow_dark",
											   	  "circle_yellow_light" };
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.music_library_label);
				ArrayList<String> labelTitles = new ArrayList<String>();
				labelTitles.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.library_labels)));
				builder.setAdapter(new LibraryLabelsAdapter(getActivity(), labelTitles), 
															new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						libraryLabelID = labelsIdsArray[which];
						labelButton.setImageDrawable(labelsArray[which]);
						
					}
					
				});
				
				builder.create().show();
				
			}

			
		});
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.add_music_library);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				String libraryName = musicLibraryName.getText().toString();
				
				Intent intent = new Intent(getActivity(), MusicLibraryEditorActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("LIBRARY_NAME", libraryName);
				bundle.putString("LIBRARY_ICON", libraryLabelID);
				intent.putExtras(bundle);
				startActivity(intent);
				
				dialog.dismiss();
				getActivity().finish();
				
			}
        	
        });
        
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				getActivity().finish();
				
			}
        	
        });
        
        return builder.create();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().finish();
		
	}
	
}
