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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;

public class ScanFrequencyDialog extends DialogFragment {

	private Activity parentActivity;
	private int selectedIndex;
	
	private static final String SCAN_FREQUENCY = "SCAN_FREQUENCY";
	
	private static boolean CALLED_FROM_WELCOME = false;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
		
		final SharedPreferences sharedPreferences = parentActivity.
											  getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        CALLED_FROM_WELCOME = this.getArguments().getBoolean("CALLED_FROM_WELCOME");
        
        //Check which theme is currently selected and set the appropriate flag.
        if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==0) {
        	selectedIndex = 0;
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==1) {
        	selectedIndex = 1;
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==2) {
        	selectedIndex = 2;
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==3) {
        	selectedIndex = 3;
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==4) {
        	selectedIndex = 4;
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==5) {
        	selectedIndex = 5;
        }

        /*************************************************************************************************
         * Scan Frequency Settings:
         * 
         * 0: Scan at every startup.
         * 1: Scan at every 3 startups.
         * 2: Scan at every 5 startups.
         * 3: Scan at every 10 startups.
         * 4: Scan at every 20 startups.
         * 5: Scan Manually.
         *************************************************************************************************/
        
        //Set the dialog title.
        builder.setTitle(R.string.scan_frequency);
        builder.setSingleChoiceItems(R.array.scan_frequency_choices, selectedIndex, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (which==0) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 5).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				} else if (which==1) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 1).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				} else if (which==2) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 2).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				} else if (which==3) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 3).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				} else if (which==4) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 4).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				} else if (which==5) {
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 5).commit();
					
					dialog.dismiss();
					getActivity().finish();
					Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					
				}
				
			}
        	
        });

        return builder.create();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (CALLED_FROM_WELCOME==false) {
			getActivity().finish();
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (CALLED_FROM_WELCOME==false) {
			getActivity().finish();
		}
		
	}
	
}
