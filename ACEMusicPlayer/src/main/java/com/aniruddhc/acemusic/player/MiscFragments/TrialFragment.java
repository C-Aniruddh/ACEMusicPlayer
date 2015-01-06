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
package com.aniruddhc.acemusic.player.MiscFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.InAppBilling.IabHelper;
import com.aniruddhc.acemusic.player.InAppBilling.IabResult;
import com.aniruddhc.acemusic.player.InAppBilling.Purchase;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;

public class TrialFragment extends Fragment {
	
	private Context mContext;
	private SharedPreferences sharedPreferences;
	
	protected static final String ITEM_SKU = "com.aniruddhc.acemusic.player.unlock";
    protected static final String ITEM_SKU_PROMO = "com.aniruddhc.acemusic.player.unlock.promo";
	
	private int numDaysRemaining;
	private boolean expired;
	
	private TextView daysRemaining;
	private TextView infoText;
	private Button laterButton;
	private Button upgradeNowButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_trial_version, container, false);
		mContext = getActivity().getApplicationContext();
		sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
		numDaysRemaining = getArguments().getInt("NUM_DAYS_REMAINING");
		expired = getArguments().getBoolean("EXPIRED");

        //Circumvent the trial check since the app is no longer paid.
        getActivity().finish();
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
        return rootView;

		/*daysRemaining = (TextView) rootView.findViewById(R.id.trial_days_remaining);
		infoText = (TextView) rootView.findViewById(R.id.trial_message);
		laterButton = (Button) rootView.findViewById(R.id.upgrade_later);
		upgradeNowButton = (Button) rootView.findViewById(R.id.upgrade_now);
		
		daysRemaining.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		daysRemaining.setPaintFlags(daysRemaining.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		infoText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		infoText.setPaintFlags(infoText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		laterButton.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		laterButton.setPaintFlags(laterButton.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		upgradeNowButton.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		upgradeNowButton.setPaintFlags(upgradeNowButton.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		if (expired) {
			daysRemaining.setText(R.string.expired);
			infoText.setText(R.string.trial_expired);
			daysRemaining.setTextColor(0xFFCC0000);
		} else {
			infoText.setText(R.string.trial_running);
			if (numDaysRemaining==1) {
				daysRemaining.setText(numDaysRemaining + " " + mContext.getResources().getString(R.string.day_remaining));
			} else {
				daysRemaining.setText(numDaysRemaining + " " + mContext.getResources().getString(R.string.days_remaining));
			}
			
			daysRemaining.setTextColor(0xFF0099CC);
		}
		
		laterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (expired) {
					getActivity().finish();
				} else {
					launchMainActivity();
				}
				
			}
			
		});
		
		upgradeNowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initUpgradeProcessWithPromo();
				
			}
			
		});
		
		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);

        	//Calculate ActionBar height
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
        	
            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            rootView.setClipToPadding(false);
            rootView.setPadding(0, topPadding + actionBarHeight, 0, navigationBarHeight);
        }
		
		return rootView;*/
	}
	
	private void initUpgradeProcessWithPromo() {
/*		//Load the special offer fragment into the activity.
		SpecialUpgradeOfferFragment fragment = new SpecialUpgradeOfferFragment();
		
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
	    transaction.replace(R.id.launcher_root_view, fragment, "specialUpgradeOfferFragment");
	    transaction.commit();*/
	    
	}
	
	private void launchMainActivity() {
		Intent intent = new Intent(mContext, MainActivity.class);
		int startupScreen = sharedPreferences.getInt("STARTUP_SCREEN", 0);
		
		switch (startupScreen) {
		case 0:
			intent.putExtra("TARGET_FRAGMENT", "ARTISTS");
			break;
		case 1:
			intent.putExtra("TARGET_FRAGMENT", "ALBUM_ARTISTS");
			break;
		case 2:
			intent.putExtra("TARGET_FRAGMENT", "ALBUMS");
			break;
		case 3:
			intent.putExtra("TARGET_FRAGMENT", "SONGS");
			break;
		case 4:
			intent.putExtra("TARGET_FRAGMENT", "PLAYLISTS");
			break;
		case 5:
			intent.putExtra("TARGET_FRAGMENT", "GENRES");
			break;
		case 6:
			intent.putExtra("TARGET_FRAGMENT", "FOLDERS");
			break;
		}
		
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		getActivity().finish();
		
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			
			if (result.isFailure()) {	
				Toast.makeText(mContext, R.string.unable_to_purchase, Toast.LENGTH_LONG).show();
				sharedPreferences.edit().putBoolean("TRIAL", true).commit();
				return;
			} else if (purchase.getSku().equals(ITEM_SKU) || purchase.getSku().equals(ITEM_SKU_PROMO)) {
				Toast.makeText(mContext, R.string.jams_trial_time_removed, Toast.LENGTH_LONG).show();
				sharedPreferences.edit().putBoolean("TRIAL", false).commit();
				launchMainActivity();
				
			}
	      
		}
	   
	};
	
}
