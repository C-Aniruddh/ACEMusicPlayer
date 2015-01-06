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
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.MusicFoldersSelectionFragment.MusicFoldersSelectionFragment;
import com.aniruddhc.acemusic.player.Utils.Common;

public class MusicFoldersFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	private FragmentManager mChildFragmentManager;
	private MusicFoldersSelectionFragment mMusicFoldersSelectionFragment = null;
	private TextView mWelcomeHeader;
	private RadioGroup mMusicFoldersOptions;
	private TranslateAnimation mSlideInAnimation;
	private TranslateAnimation mSlideOutAnimation;
	private RelativeLayout mFoldersLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext =  getActivity().getApplicationContext();
		mApp = (Common) mContext;
		View rootView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_welcome_screen_2, null);		
		
		mFoldersLayout = (RelativeLayout) rootView.findViewById(R.id.folders_fragment_holder);
		if (mApp.getSharedPreferences().getInt("MUSIC_FOLDERS_SELECTION", 0)==0) {
			mFoldersLayout.setVisibility(View.INVISIBLE);
			mFoldersLayout.setEnabled(false);
		} else {
			mFoldersLayout.setVisibility(View.VISIBLE);
			mFoldersLayout.setEnabled(true);
		}
		
		mSlideInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, 
				  								   Animation.RELATIVE_TO_SELF, 0.0f, 
				  								   Animation.RELATIVE_TO_SELF, 2.0f, 
				  								   Animation.RELATIVE_TO_SELF, 0.0f);

		mSlideInAnimation.setDuration(600);
		mSlideInAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mSlideInAnimation.setAnimationListener(slideInListener);
		
		mSlideOutAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, 
				   								    Animation.RELATIVE_TO_SELF, 0.0f, 
				   								    Animation.RELATIVE_TO_SELF, 0.0f, 
				   								    Animation.RELATIVE_TO_SELF, 2.0f);
		mSlideOutAnimation.setDuration(600);
		mSlideOutAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mSlideOutAnimation.setAnimationListener(slideOutListener);
		
		mChildFragmentManager = this.getChildFragmentManager();
		mChildFragmentManager.beginTransaction()
	 	 					 .add(R.id.folders_fragment_holder, getMusicFoldersSelectionFragment())
	 	 					 .commit();
		
		mWelcomeHeader = (TextView) rootView.findViewById(R.id.welcome_header);
		mWelcomeHeader.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
		
        mMusicFoldersOptions = (RadioGroup) rootView.findViewById(R.id.music_library_welcome_radio_group);
        RadioButton getAllSongsRadioButton = (RadioButton) mMusicFoldersOptions.findViewById(R.id.get_all_songs_radio);
        RadioButton letMePickFoldersRadioButton = (RadioButton) mMusicFoldersOptions.findViewById(R.id.pick_folders_radio);
        
        getAllSongsRadioButton.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        letMePickFoldersRadioButton.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        
        mMusicFoldersOptions.setOnCheckedChangeListener(onCheckedChangeListener);
        return rootView;
    }
	
	/**
	 * RadioButton selection listener.
	 */
	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup radioGroup, int radioButtonId) {
			switch(radioButtonId) {
			case R.id.get_all_songs_radio:
				mFoldersLayout.startAnimation(mSlideOutAnimation);
				mFoldersLayout.setEnabled(false);
				break;
			case R.id.pick_folders_radio:
				mFoldersLayout.startAnimation(mSlideInAnimation);
				mFoldersLayout.setEnabled(true);
				break;
			}
			
		}
		
	};
	
	/**
	 * Slide out animation listener.
	 */
	private AnimationListener slideOutListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mFoldersLayout.setVisibility(View.INVISIBLE);
			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mFoldersLayout.setVisibility(View.VISIBLE);
			
		}
		
	};
	
	/**
	 * Slide in animation listener.
	 */
	private AnimationListener slideInListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mFoldersLayout.setVisibility(View.VISIBLE);
			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mFoldersLayout.setVisibility(View.VISIBLE);
			
		}
		
	};	
	
	/**
	 * Instantiates a new fragment if mMusicFoldersSelectionFragment is null. 
	 * Returns the current fragment, otherwise.
	 */
	public MusicFoldersSelectionFragment getMusicFoldersSelectionFragment() {
		if (mMusicFoldersSelectionFragment==null) {
			mMusicFoldersSelectionFragment = new MusicFoldersSelectionFragment();
			
			Bundle bundle = new Bundle();
			bundle.putBoolean("com.aniruddhc.acemusic.player.WELCOME", true);
			mMusicFoldersSelectionFragment.setArguments(bundle);
		}
		
		return mMusicFoldersSelectionFragment;
	}
	
}

