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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncGoogleMusicAuthenticationTask;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class GooglePlayMusicAuthenticationDialog extends DialogFragment {

	private FragmentActivity parentActivity;
	private SharedPreferences sharedPreferences;
	private Account account;
	private boolean mFirstRun;
	private TextView infoText;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		parentActivity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		sharedPreferences = parentActivity.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		View rootView = parentActivity.getLayoutInflater().inflate(R.layout.dialog_google_authentication_layout, null);
		
		//Check if this dialog was called from the Welcome sequence.
		mFirstRun = getArguments().getBoolean(Common.FIRST_RUN);
		
		infoText = (TextView) rootView.findViewById(R.id.google_authentication_dialog_text);
		infoText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		infoText.setPaintFlags(infoText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        final AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
        final Account[] accounts = accountManager.getAccountsByType("com.google");
        final int size = accounts.length;
        String[] accountNames = new String[size];
        
        for (int i=0; i < size; i++) {
        	accountNames[i] = accounts[i].name;
        }
        
        //Set the dialog title.
        builder.setTitle(R.string.sign_in_google_play_music);
        builder.setCancelable(false);
        builder.setItems(accountNames, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				account = accounts[which];
				sharedPreferences.edit().putString("GOOGLE_PLAY_MUSIC_ACCOUNT", account.name).commit();
				AsyncGoogleMusicAuthenticationTask task = new AsyncGoogleMusicAuthenticationTask(parentActivity.getApplicationContext(), 
																								 parentActivity,
																								 mFirstRun,
																								 account.name);
				
				task.execute();

			}
			
		});
        
        

        return builder.create();
    }
	
}
