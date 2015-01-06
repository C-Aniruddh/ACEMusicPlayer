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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AddEQPresetDialog extends DialogFragment {

	private Common mApp;
	private AddEQPresetDialog dialog;
	private View dialogView;
	private EditText newPresetNameField;
	private EqualizerActivity mEqualizerFragment;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		mEqualizerFragment = (EqualizerActivity) getActivity();
		dialog = this;
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_new_equalizer_preset_dialog_layout, null);
        
        newPresetNameField = (EditText) dialogView.findViewById(R.id.new_preset_name_text_field);
        newPresetNameField.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Light"));
        newPresetNameField.setPaintFlags(newPresetNameField.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Set the dialog title.
        builder.setTitle(R.string.new_eq_preset);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialog.dismiss();
				
			}
        	
        });
        
        builder.setPositiveButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Get the preset name from the text field.
				String presetName = newPresetNameField.getText().toString();
				
				//Add the preset and it's values to the DB.
				mApp.getDBAccessHelper().addNewEQPreset(presetName, 
									  					mEqualizerFragment.getFiftyHertzLevel(), 
									  					mEqualizerFragment.getOneThirtyHertzLevel(), 
									  					mEqualizerFragment.getThreeTwentyHertzLevel(), 
									  					mEqualizerFragment.getEightHundredHertzLevel(), 
									  					mEqualizerFragment.getTwoKilohertzLevel(), 
									  					mEqualizerFragment.getFiveKilohertzLevel(), 
									  					mEqualizerFragment.getTwelvePointFiveKilohertzLevel(), 
									  					(short) mEqualizerFragment.getVirtualizerSeekBar().getProgress(), 
									  					(short) mEqualizerFragment.getBassBoostSeekBar().getProgress(), 
									  					(short) mEqualizerFragment.getReverbSpinner().getSelectedItemPosition());
				
				Toast.makeText(getActivity(), R.string.preset_saved, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
        	
        });

        return builder.create();
    }

}
