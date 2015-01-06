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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class LicensesDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;
	private TextView creativeCommonsLink;
	private TextView creativeCommonsInfo;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("licensesDialog");
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.licenses_dialog_layout, null);
		
		creativeCommonsLink = (TextView) rootView.findViewById(R.id.creative_commons_link);
		creativeCommonsLink.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		creativeCommonsLink.setText(Html.fromHtml("<a href=\"http://creativecommons.org/licenses/by-sa/3.0/legalcode\">Creative Commons ShareALike 3.0 License</a> "));
		creativeCommonsLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		creativeCommonsInfo = (TextView) rootView.findViewById(R.id.licenses_text);
		creativeCommonsInfo.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.licenses);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				getActivity().finish();
				
			}
        	
        });

        return builder.create();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().finish();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().finish();
		
	}
	
}
