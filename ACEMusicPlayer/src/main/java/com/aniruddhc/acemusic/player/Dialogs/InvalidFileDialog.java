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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.aniruddhc.acemusic.player.R;

public class InvalidFileDialog extends DialogFragment {

	private String fileName;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		fileName = getArguments().getString("FILE_NAME");
		
		final DialogFragment dialog = this;
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.error);
        builder.setMessage(fileName + " " + getResources().getString(R.string.invalid_file_message));
        builder.setNegativeButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialog.dismiss();
				
			}
        	
        });
        
        return builder.create();
    }
	
}
