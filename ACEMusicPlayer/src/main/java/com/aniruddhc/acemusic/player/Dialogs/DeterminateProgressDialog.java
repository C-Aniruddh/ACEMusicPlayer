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
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class DeterminateProgressDialog extends DialogFragment {

	private Activity parentActivity;
	public static DeterminateProgressDialog dialog;
	public static TextView progressText;
	public static ProgressBar progressBar;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
		dialog = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        String title = getArguments().getString("TITLE");
        String text = getArguments().getString("TEXT");
        
        View progressView = parentActivity.getLayoutInflater().inflate(R.layout.determinate_progress_dialog, null);
        progressText = (TextView) progressView.findViewById(R.id.determinate_progress_dialog_text);
        progressText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
        progressText.setPaintFlags(progressText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        progressText.setText(text);
        
        progressBar = (ProgressBar) progressView.findViewById(R.id.determinate_progress_dialog_bar);
        
        builder.setTitle(title);
        builder.setView(progressView);

        return builder.create();
    }
	
}
