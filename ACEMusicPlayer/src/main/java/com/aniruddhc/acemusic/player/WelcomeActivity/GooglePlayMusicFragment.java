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
package com.aniruddhc.acemusic.player.WelcomeActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncGoogleMusicAuthenticationTask;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class GooglePlayMusicFragment extends Fragment {

	private Context mContext;
	private Common mApp;

	private Account account;
	private RadioGroup radioGroup;
	private TextView welcomeHeader;
	private TextView welcomeText1;
	private TextView googlePlayMusicDisclaimer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext =  getActivity().getApplicationContext();
		mApp = (Common) mContext;
		View rootView = (View) inflater.inflate(R.layout.fragment_welcome_screen_5, null);
		
		welcomeHeader = (TextView) rootView.findViewById(R.id.welcome_header);
		welcomeHeader.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Light"));
		
		welcomeText1 = (TextView) rootView.findViewById(R.id.welcome_text_1);
		welcomeText1.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Regular"));
        
		googlePlayMusicDisclaimer = (TextView) rootView.findViewById(R.id.google_play_music_disclaimer);
		googlePlayMusicDisclaimer.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Regular"));

        radioGroup = (RadioGroup) rootView.findViewById(R.id.google_play_music_radio_group);
        
        final AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
        final Account[] accounts = accountManager.getAccountsByType("com.google");
        final int size = accounts.length+1; //We're adding 1 here to account (no pun intended) for the extra "Don't use Google Play Music" option.
        
        final RadioButton[] radioButton = new RadioButton[size];
        
        //Add a new radio button the group for each username.
        for (int i=0; i < size; i++) {
        	radioButton[i] = new RadioButton(getActivity());
        	radioGroup.addView(radioButton[i]);
        	
        	//The first radio button will always be "Don't use Google Play Music".
        	if (i==0) {
        		radioButton[i].setChecked(true);
        		radioButton[i].setText(R.string.dont_use_google_play_music);
        	} else {
        		radioButton[i].setText(accounts[i-1].name);
        	}
        	
        	radioButton[i].setTag(i);
        	radioButton[i].setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        	
        }
        
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				int radioButtonID = group.getCheckedRadioButtonId();
				View radioButton = group.findViewById(radioButtonID);
				int index = group.indexOfChild(radioButton);
				
				if (index!=0) {
					
					account = accounts[index-1];
					mApp.getSharedPreferences()
						.edit()
						.putString("GOOGLE_PLAY_MUSIC_ACCOUNT", account.name)
						.commit();
					
					AsyncGoogleMusicAuthenticationTask task = new AsyncGoogleMusicAuthenticationTask(mContext, 
																									 getActivity(),
																									 true,
																									 account.name);
					
					task.execute();
				} else {
					mApp.getSharedPreferences().edit().putString("GOOGLE_PLAY_MUSIC_ACCOUNT", "").commit();
					mApp.getSharedPreferences().edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false).commit();
				}
				
			}
        	
        });
		
		return rootView;
	}
	
}
