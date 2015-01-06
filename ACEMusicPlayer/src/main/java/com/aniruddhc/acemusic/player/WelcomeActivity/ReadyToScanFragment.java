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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class ReadyToScanFragment extends Fragment {
	
	private Context mContext;
	private TextView welcomeHeader;
	private TextView welcomeText1;
	private TextView swipeLeftToContinue;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity().getApplication();
		View rootView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_welcome_screen_6, null);		
		
		welcomeHeader = (TextView) rootView.findViewById(R.id.welcome_header);
		welcomeHeader.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
		
		welcomeText1 = (TextView) rootView.findViewById(R.id.welcome_text_1);
		welcomeText1.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        
		swipeLeftToContinue = (TextView) rootView.findViewById(R.id.swipe_left_to_continue);
		swipeLeftToContinue.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

        return rootView;
    }
	
}

