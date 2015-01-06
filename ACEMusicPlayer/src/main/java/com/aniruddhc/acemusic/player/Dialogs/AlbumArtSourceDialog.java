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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;

/*********************************************************
 * Allows the user to select where they want to load album 
 * art images from.
 * 
 * @author Saravan Pantham
 *********************************************************/
public class AlbumArtSourceDialog extends DialogFragment {

	private Context mContext;
	private AlbumArtSourceDialog dialog;
	private SharedPreferences sharedPreferences;
	private int userSelection = 0;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mContext = getActivity().getApplicationContext();
		dialog = this;
		sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
		String[] albumArtSources = getActivity().getResources().getStringArray(R.array.album_art_sources);
		
        //Check which frequency is currently selected and set the appropriate flag.
        if (sharedPreferences.getInt("ALBUM_ART_SOURCE", 0)==0) {
        	userSelection = 0;
        } else if (sharedPreferences.getInt("ALBUM_ART_SOURCE", 0)==1) {
        	userSelection = 1;
        } else if (sharedPreferences.getInt("ALBUM_ART_SOURCE", 0)==2) {
        	userSelection = 2;
        } else if (sharedPreferences.getInt("ALBUM_ART_SOURCE", 0)==3) {
        	userSelection = 3;
        }
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.album_art_sources);
        builder.setSingleChoiceItems(albumArtSources, userSelection, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int which) {

				//Update the SharedPreferences entry based on the user's selection.
				if (which==0) {
					//"Prefer embedded art"
					sharedPreferences.edit().putInt("ALBUM_ART_SOURCE", 0).commit();
				} else if (which==1) {
					//"Prefer folder art"
					sharedPreferences.edit().putInt("ALBUM_ART_SOURCE", 1).commit();
				} else if (which==2) {
					//"Use embedded art only"
					sharedPreferences.edit().putInt("ALBUM_ART_SOURCE", 2).commit();
				} else if (which==3) {
					//"User folder art only"
					sharedPreferences.edit().putInt("ALBUM_ART_SOURCE", 3).commit();
				}
				
				//Rescan for album art.
				//Seting the "RESCAN_ALBUM_ART" flag to true will force MainActivity to rescan the folders.
				sharedPreferences.edit().putBoolean("RESCAN_ALBUM_ART", true).commit();
				
				//Restart the app.
				final Intent i = getActivity().getBaseContext()
						                      .getPackageManager()
						                      .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
				
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().finish();
				startActivity(i);
				
				dialog.dismiss();
				getActivity().finish();
				Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_LONG).show();
				
			}
        	
        });

        return builder.create();
        
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().finish();
		
	}
	
}
