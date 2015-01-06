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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncSaveMusicFoldersTask;
import com.aniruddhc.acemusic.player.MiscFragments.BuildingLibraryProgressFragment;
import com.aniruddhc.acemusic.player.Services.BuildMusicLibraryService;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.viewpagerindicator.LinePageIndicator;

public class WelcomeActivity extends FragmentActivity {
	
	private Context mContext;
	private Common mApp;
	private ViewPager welcomeViewPager;
	private LinePageIndicator indicator;
	private String mAccountName;
	
	private MusicFoldersFragment mMusicFoldersFragment;
	public static BuildingLibraryProgressFragment mBuildingLibraryProgressFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mApp = (Common) this.getApplicationContext();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

		setContentView(R.layout.activity_welcome);
		setTheme(R.style.AppThemeLight);
		
		if (getActionBar()!=null)
			getActionBar().hide();

		welcomeViewPager = (ViewPager) findViewById(R.id.welcome_pager);	
		
		FragmentManager fm = getSupportFragmentManager();
		welcomeViewPager.setAdapter(new WelcomePagerAdapter(fm));
		welcomeViewPager.setOffscreenPageLimit(6);
		
		indicator = (LinePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(welcomeViewPager);
		
        final float density = getResources().getDisplayMetrics().density;
        indicator.setSelectedColor(0x880099CC);
        indicator.setUnselectedColor(0xFF4F4F4F);
        indicator.setStrokeWidth(2 * density);
        indicator.setLineWidth(30 * density);
        indicator.setOnPageChangeListener(pageChangeListener);

        //Check if the library needs to be rebuilt and this isn't the first run.
        if (getIntent().hasExtra("REFRESH_MUSIC_LIBRARY"))
            showBuildingLibraryProgress();

	}
	
	/**
	 * Page scroll listener.
	 */
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int scrollState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int page) {

			/* If the user swiped away from the music folders 
			 * selection fragment, save the music folders to 
			 * the database.
			 */
			if (page==0 || page==2) {
				new AsyncSaveMusicFoldersTask(mContext.getApplicationContext(), 
											  mMusicFoldersFragment.getMusicFoldersSelectionFragment()
											  				       .getMusicFoldersHashMap())
											 .execute();
			}
			
			/* If the user scrolls away from the Google Play Music page and 
			 * they have selected an account, check if the default Google Play 
			 * Music app is installed. */
			if (page==3) {
				
				if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
					//Check if the Google Play Music app is installed.
			    	PackageManager pm = mContext.getPackageManager();
			    	boolean installed = false;
			    	try {
						pm.getPackageInfo("com.google.android.music", PackageManager.GET_ACTIVITIES);
						installed = true;
					} catch (NameNotFoundException e1) {
						//The app isn't installed.
						installed = false;
					}

			    	if (installed==false) {
			    		//Prompt the user to install Google Play Music.
			    		promptUserInstallGooglePlayMusic();
			    	}
				
				}
		    	
			}
			
			//Launch the scanning AsyncTask.
			if (page==5)
                showBuildingLibraryProgress();
			
		}
    	
    };

    private void showBuildingLibraryProgress() {

        //Disables swiping events on the pager.
        welcomeViewPager.setCurrentItem(5);
        welcomeViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }

        });

        //Fade out the ViewPager indicator.
        Animation fadeOutAnim = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
        fadeOutAnim.setDuration(600);
        fadeOutAnim.setAnimationListener(fadeOutListener);
        indicator.startAnimation(fadeOutAnim);

    }
    
    /**
     * Fade out animation listener.
     */
    private AnimationListener fadeOutListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			indicator.setVisibility(View.INVISIBLE);

            Intent intent = new Intent(mContext, BuildMusicLibraryService.class);
            startService(intent);
			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };
	
    /**
     * Asks the user to install the GMusic app.
     */
	private void promptUserInstallGooglePlayMusic() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog dialog;
		builder.setTitle(R.string.google_play_music_no_asterisk);
		builder.setMessage(R.string.prompt_user_install_google_play_music);
		builder.setPositiveButton(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.google.android.music"));
				startActivity(intent);

			}
			
		});
		
		builder.setNegativeButton(R.string.no, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mApp.getSharedPreferences().edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false).commit();
				Toast.makeText(mContext, R.string.google_play_music_disabled, Toast.LENGTH_LONG).show();
				dialog.dismiss();
				
			}
			
		});
		
		dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * Ask the user to set up GMusic.
	 */
	private void promptUserSetUpGooglePlayMusic() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog dialog;
		builder.setTitle(R.string.tip);
		builder.setMessage(R.string.prompt_user_set_up_google_play_music);
		builder.setPositiveButton(R.string.let_me_check, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.music");
				startActivity(intent);
				dialog.dismiss();
	
			}
			
		});
		
		builder.setNeutralButton(R.string.sync_manually, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
			
		});
		
		builder.setNegativeButton(R.string.set_up_already, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
			
		});
		
		dialog = builder.create();
		dialog.show();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//Result Code 45 = UserRecoverableAuthenticationException from GooglePlayMusicAuthenticationDialog.
    	if (requestCode==45) {
    		
    		final Intent finalData = data;
    		final int finalResultCode = resultCode;
    		
    		runOnUiThread(new Runnable() {

				@Override
				public void run() {
					//An unknown error occurred.
		            if (finalData==null) {
						Toast.makeText(mContext, R.string.unknown_error_google_music, Toast.LENGTH_LONG).show();
		                return;
		            }
		            
		            //The user handled the exception properly.
		            if (finalResultCode==RESULT_OK) {
		            	
		            	mApp.getSharedPreferences().edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", true).commit();
		            	mApp.getSharedPreferences().edit().putString("GOOGLE_PLAY_MUSIC_ACCOUNT", mAccountName).commit();

				        return;
		            }
		            
		            if (finalResultCode==RESULT_CANCELED) {
		            	finish();
		            }
		            
		            Toast.makeText(mContext, R.string.unknown_error_google_music, Toast.LENGTH_LONG).show();
				}
    			
    		});
    		
    	} else if (resultCode==10001) {
    		
    	}
    	
    }
	
	class WelcomePagerAdapter extends FragmentStatePagerAdapter {
		
        public WelcomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //This method controls which fragment should be shown on a specific screen.
        @Override
        public Fragment getItem(int position) {
        	
        	//Assign the appropriate screen to the fragment object, based on which screen is displayed.
        	switch (position) {
        	case 0:
        		return new WelcomeFragment();
        	case 1:
        		mMusicFoldersFragment = new MusicFoldersFragment();
        		return mMusicFoldersFragment;
        	case 2:
        		return new AlbumArtFragment();
        	case 3:
        		return new GooglePlayMusicFragment();
        	case 4:
        		return new ReadyToScanFragment();
        	case 5:
        		mBuildingLibraryProgressFragment = new BuildingLibraryProgressFragment();
        		return mBuildingLibraryProgressFragment;
        	default:
        		return null;
        	}
        	
        }

		@Override
		public int getCount() {
			return 6;
		}
        
	}
	
	@Override
	public void onPause() {
		super.onPause();
		finish();
	}
	
}
