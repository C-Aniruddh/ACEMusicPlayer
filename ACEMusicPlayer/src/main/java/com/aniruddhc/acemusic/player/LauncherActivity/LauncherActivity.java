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
package com.aniruddhc.acemusic.player.LauncherActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.InAppBilling.IabHelper;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;
import com.aniruddhc.acemusic.player.MiscFragments.TrialFragment;
import com.aniruddhc.acemusic.player.Services.BuildMusicLibraryService;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.WelcomeActivity.WelcomeActivity;

public class LauncherActivity extends FragmentActivity {

	public Context mContext;
	private Common mApp;
	public Activity mActivity;
	
	public static String mAccountName;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static IabHelper mHelper;
    protected static final String ITEM_SKU = "com.aniruddhc.acemusic.player.unlock";
    protected static final String ITEM_SKU_PROMO = "com.aniruddhc.acemusic.player.unlock.promo";
    private static final String CAMPAIGN_SOURCE_PARAM = "utm_source";
    protected boolean mPurchased;
    protected boolean mPurchasedPromo;
    private boolean mExplicitShowTrialFragment;
    
    public static TextView buildingLibraryMainText;
    public static TextView buildingLibraryInfoText;
    private RelativeLayout buildingLibraryLayout;
    private Handler mHandler;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		setTheme(R.style.AppThemeNoActionBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		
		mContext = this;
		mActivity = this;
		mApp = (Common) mContext.getApplicationContext();
		mHandler = new Handler();
		
		//Increment the start count. This value will be used to determine when the library should be rescanned.
    	int startCount = mApp.getSharedPreferences().getInt("START_COUNT", 1);
    	mApp.getSharedPreferences().edit().putInt("START_COUNT", startCount+1).commit();
		
		//Save the dimensions of the layout for later use on KitKat devices.
		final RelativeLayout launcherRootView = (RelativeLayout) findViewById(R.id.launcher_root_view);
		launcherRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				
				try {
					
					int screenDimens[] = new int[2];
					int screenHeight = 0;
					int screenWidth = 0;
		            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
		            	//API levels 14, 15 and 16.
		            	screenDimens = getTrueDeviceResolution();
		            	screenWidth = screenDimens[0];
		            	screenHeight = screenDimens[1];
		            	
		            } else {
		            	//API levels 17+.
		            	Display display = getWindowManager().getDefaultDisplay();
		            	DisplayMetrics metrics = new DisplayMetrics();
		            	display.getRealMetrics(metrics);
		            	screenHeight = metrics.heightPixels;
		            	screenWidth = metrics.widthPixels;
		            	
		            }
					
					int layoutHeight = launcherRootView.getHeight();
					int layoutWidth = launcherRootView.getWidth();
					
					int extraHeight = screenHeight - layoutHeight;
					int extraWidth = screenWidth = layoutWidth;
					
					mApp.getSharedPreferences().edit().putInt("KITKAT_HEIGHT", layoutHeight).commit();
					mApp.getSharedPreferences().edit().putInt("KITKAT_WIDTH", layoutWidth).commit();
					mApp.getSharedPreferences().edit().putInt("KITKAT_HEIGHT_LAND", layoutWidth - extraHeight).commit();
					mApp.getSharedPreferences().edit().putInt("KITKAT_WIDTH_LAND", screenHeight).commit();

				} catch (Exception e) {
					e.printStackTrace();
				}
				
		    }
			
		});
		
		//Build the music library based on the user's scan frequency preferences.
        int scanFrequency = mApp.getSharedPreferences().getInt("SCAN_FREQUENCY", 5);
        int updatedStartCount = mApp.getSharedPreferences().getInt("START_COUNT", 1);
		
		//Launch the appropriate activity based on the "FIRST RUN" flag.
		if (mApp.getSharedPreferences().getBoolean(Common.FIRST_RUN, true)==true) {
			
        	//Create the default Playlists directory if it doesn't exist.
        	File playlistsDirectory = new File(Environment.getExternalStorageDirectory() + "/Playlists/");
        	if (!playlistsDirectory.exists() || !playlistsDirectory.isDirectory()) {
        		playlistsDirectory.mkdir();
        	}
			
			//Disable equalizer for HTC devices by default.
			if (mApp.getSharedPreferences().getBoolean(Common.FIRST_RUN, true)==true &&
				Build.PRODUCT.contains("HTC")) {
				mApp.getSharedPreferences().edit().putBoolean("EQUALIZER_ENABLED", false).commit();
			}
			
        	//Send out a test broadcast to initialize the homescreen/lockscreen widgets.
        	sendBroadcast(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
			
			Intent intent = new Intent(this, WelcomeActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			
		} else if (mApp.isBuildingLibrary()) {
			buildingLibraryMainText = (TextView) findViewById(R.id.building_music_library_text);
	        buildingLibraryInfoText = (TextView) findViewById(R.id.building_music_library_info);
	        buildingLibraryLayout = (RelativeLayout) findViewById(R.id.building_music_library_layout);
	        
	        buildingLibraryInfoText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryInfoText.setPaintFlags(buildingLibraryInfoText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        buildingLibraryMainText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryMainText.setPaintFlags(buildingLibraryMainText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        buildingLibraryMainText.setText(R.string.jams_is_building_library);
	        buildingLibraryLayout.setVisibility(View.VISIBLE);
	        
	        //Initialize the runnable that will fire once the scan process is complete.
			mHandler.post(scanFinishedCheckerRunnable);
		
		} else if (mApp.getSharedPreferences().getBoolean("RESCAN_ALBUM_ART", false)==true) {

			buildingLibraryMainText = (TextView) findViewById(R.id.building_music_library_text);
	        buildingLibraryInfoText = (TextView) findViewById(R.id.building_music_library_info);
	        buildingLibraryLayout = (RelativeLayout) findViewById(R.id.building_music_library_layout);
	        
	        buildingLibraryInfoText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryInfoText.setPaintFlags(buildingLibraryInfoText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        buildingLibraryMainText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryMainText.setPaintFlags(buildingLibraryMainText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        buildingLibraryMainText.setText(R.string.jams_is_caching_artwork);
	        initScanProcess(0);
	        
		} else if ((mApp.getSharedPreferences().getBoolean("REBUILD_LIBRARY", false)==true) || 
				   (scanFrequency==0 && mApp.isScanFinished()==false) || 
				   (scanFrequency==1 && mApp.isScanFinished()==false && updatedStartCount%3==0) || 
				   (scanFrequency==2 && mApp.isScanFinished()==false && updatedStartCount%5==0) || 
				   (scanFrequency==3 && mApp.isScanFinished()==false && updatedStartCount%10==0) || 
				   (scanFrequency==4 && mApp.isScanFinished()==false && updatedStartCount%20==0)) {
			
			buildingLibraryMainText = (TextView) findViewById(R.id.building_music_library_text);
	        buildingLibraryInfoText = (TextView) findViewById(R.id.building_music_library_info);
	        buildingLibraryLayout = (RelativeLayout) findViewById(R.id.building_music_library_layout);
	        
	        buildingLibraryInfoText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryInfoText.setPaintFlags(buildingLibraryInfoText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        buildingLibraryMainText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	        buildingLibraryMainText.setPaintFlags(buildingLibraryMainText.getPaintFlags() |
	        								  	  Paint.ANTI_ALIAS_FLAG |
	        								  	  Paint.SUBPIXEL_TEXT_FLAG);
	        
	        initScanProcess(1);
	        
		} else {
			
			//Check if this activity was called from Settings.
			if (getIntent().hasExtra("UPGRADE")) {
				if (getIntent().getExtras().getBoolean("UPGRADE")==true) {
					mExplicitShowTrialFragment = true;
				} else {
					mExplicitShowTrialFragment = false;
				}
				
			}

			//initInAppBilling();
            launchMainActivity();
		}

		//Fire away a report to Google Analytics.
		try {
			if (mApp.isGoogleAnalyticsEnabled()==true) {
				EasyTracker easyTracker = EasyTracker.getInstance(this);
		    	easyTracker.send(MapBuilder.createEvent("ACE startup.",     // Event category (required)
			                   						  	"User started ACE.",  // Event action (required)
			                   						  	"User started ACE.",   // Event label
			                   						  	null)            // Event value
						   .build());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private int[] getTrueDeviceResolution() {
    	
    	int[] resolution = new int[2];
    	try {
    		Display display = getWindowManager().getDefaultDisplay();   
    		
        	Method mGetRawH = Display.class.getMethod("getRawHeight");
        	Method mGetRawW = Display.class.getMethod("getRawWidth");
        	
        	int rawWidth = (Integer) mGetRawW.invoke(display);
        	int rawHeight = (Integer) mGetRawH.invoke(display);
        	
        	resolution[0] = rawWidth;
        	resolution[1] = rawHeight;
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return resolution;
    }
	
	private void initScanProcess(int scanCode) {
		
		//Start the service that will start scanning the user's library/caching album art.
		mApp.setIsBuildingLibrary(true);
		buildingLibraryLayout.setVisibility(View.VISIBLE);
		if (scanCode==0) {
        	Intent intent = new Intent(this, BuildMusicLibraryService.class);
        	intent.putExtra("SCAN_TYPE", "RESCAN_ALBUM_ART");
			startService(intent);
			
	        mApp.getSharedPreferences().edit().putBoolean("RESCAN_ALBUM_ART", false).commit();
			
		} else if (scanCode==1) {
	    	Intent intent = new Intent(this, BuildMusicLibraryService.class);
	    	intent.putExtra("SCAN_TYPE", "FULL_SCAN");
			startService(intent);
			
			mApp.getSharedPreferences().edit().putBoolean("REBUILD_LIBRARY", false).commit();
		}
		
		//Initialize the runnable that will fire once the scan process is complete.
		mHandler.post(scanFinishedCheckerRunnable);
		
	}
	
	private Runnable scanFinishedCheckerRunnable = new Runnable() {

		@Override
		public void run() {
			
			if (mApp.isBuildingLibrary()==false) {
				launchMainActivity();
			} else {
				mHandler.postDelayed(this, 100);
			}

		}
		
	};
	
	/*private void initInAppBilling() {
		String base64EncodedPublicKey = "";
		
		base64EncodedPublicKey = Common.uid4 + 
								 Common.uid2 +
								 Common.uid6 +
								 Common.uid1 +
								 Common.uid3 +
								 Common.uid5;

    	mHelper = new IabHelper(this, base64EncodedPublicKey);
    	
    	try {
    		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
        		
        		@Override
        		public void onIabSetupFinished(IabResult result) {
        			if (!result.isSuccess()) {
        				//In-app billing could not be initialized.
        				mApp.getSharedPreferences().edit().putBoolean("TRIAL", true).commit();
        				checkTrialStatus();
        			} else {             
        				//In-app billing was initialized successfully.
        				mHelper.queryInventoryAsync(mGotInventoryListener);
        			}
        			
        		}
        		
        	});
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		checkTrialStatus();
    	}
    	
	}*/
	
	/*IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			if (result.isFailure()) {
				//Couldn't query in-app purchases.
				checkTrialStatus();
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", true).commit();
				
			} else {
				//Check if the user bought the unlocker.
				mPurchased = inventory.hasPurchase(ITEM_SKU);
				mPurchasedPromo = inventory.hasPurchase(ITEM_SKU_PROMO);
			}
			
			//If the developer (yours truly) is using the app, skip the trial process.
			final AccountManager accountManager = AccountManager.get(mContext);
	        final Account[] accounts = accountManager.getAccountsByType("com.google");
	        boolean developerEdition = false;
	        
	        for (int i=0; i < accounts.length; i++) {
	        	if (accounts[i].name.equals("jamsmusicplayer@gmail.com")) {
	        		developerEdition = true;
	        	}
	        }
			
			if (mPurchased==true || mPurchasedPromo==true || developerEdition==true) {
				//The user bought the app.
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", false).commit();
				launchMainActivity();
				
			} else {
				//The user is still running the trial version.
				checkTrialStatus();
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", true).commit();
			}
			
		}
		
	};
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			
			if (result.isFailure()) {	
				Toast.makeText(mContext, R.string.unable_to_purchase, Toast.LENGTH_LONG).show();
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", true).commit();
				return;
			} else if (purchase.getSku().equals(ITEM_SKU)) {
				Toast.makeText(mContext, R.string.jams_trial_time_removed, Toast.LENGTH_LONG).show();
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", false).commit();
				launchMainActivity();
				
			}
	      
		}
	   
	};*/
	
	/* Checks the trial version status of the app. The first install time of the app is 
	 * retrieved and then compared against the current time. If the current time is more 
	 * than 7 days away from the first install time, the trial version has expired and the 
	 * user will be prompted to buy the full version unlocker. If the current time is less 
	 * than 7 days away from the first install time of the app, the trial version is still 
	 * valid and the app will continue running normally.
	 * 
	 * This approach has a major drawback: The user can simply uninstall the app at the end 
	 * of the trial period, reinstall it, and he/she will get a full 7 day trial again. To 
	 * avoid this issue, we'll create a hidden file called ".jams_info" in the user's external 
	 * storage directory (Environment.getExternalStorageDir()). The file will contain one 
	 * line: The time in millis that is 10 days in the future from the first install time. 
	 * The trial expires in 7 days, so the extra 3 days will be a "buffer" time period that 
	 * will discourage free-loaders from simply reinstalling the app and reusing it. If a 
	 * user wants to try the app again, they can do so 3 days after their trial expires.
	 * 
	 * Every time the app starts up, it will first check for the ".jams_info" file. If it exists, 
	 * Jams will check to make sure that the current time is earlier/before the time that has 
	 * just been read in. If it is, the app will open up. If it isn't, the user will get the 
	 * trial expiry dialog.
	 */
	/*public void checkTrialStatus() {
		
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = pm.getPackageInfo("com.jams.music.player", PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long firstInstallTime = packageInfo.firstInstallTime;
		long currentTime = System.currentTimeMillis();
		long expiryTime = firstInstallTime + 604800000l;
		long numDaysRemainingMillis = expiryTime - currentTime;

		if (currentTime >= expiryTime) {
			//The trial version has expired. 
			
			//Write a file to the sdcard that stores the trial's reactivation time (3 days in the future).
			long trialReactivationTime = System.currentTimeMillis() + 259200000l;
			File file = new File(Environment.getExternalStorageDirectory() + "/.jams_info");
			try {
				if (file.exists()) {
					file.delete();
				}
				
				FileUtils.write(file, 
								"" + trialReactivationTime, 
								false);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Prompt the user to buy the full version of the app.
			showTrialFragment(true, 0);
			//showTrialDialog(true, 0);
		} else {
			
			//The trial version is still valid.
			boolean trialValid = checkTrialReactivationTime();
			if (trialValid) {
				//The trial is still valid. Show the trial dialog every 5 startups.
				long numDaysRemaining = (long) (numDaysRemainingMillis / (1000*60*60*24));
				float updatedStartCount = mApp.getSharedPreferences().getInt("START_COUNT", 1);
				if ((updatedStartCount/5)==(Math.round(updatedStartCount/5)) || mExplicitShowTrialFragment) {
					showTrialFragment(false, (int) numDaysRemaining);
					//showTrialDialog(false, (int) numDaysRemaining);
				} else {
					launchMainActivity();
				}
				
			} else {
				//The trial has expired.
				showTrialFragment(true, 0);
				//showTrialDialog(true, 0);
			}
			
		}
		
	}*/
	
	/* This method checks for the /sdcard/.jams_info file. If it exists, 
	 * it tries to read the reactivation time from the file.
	 * Checks if the current time is 3 days (or more) in the future 
	 * than the time that was just read. If so, returns true. If not, 
	 * returns false.
	 */
	private boolean checkTrialReactivationTime() {
		
		File file = new File(Environment.getExternalStorageDirectory() + "/.jams_info");
		if (file.exists()) {
			//The file exists. Try reading the reactivation time from it.
			String time = null;
			long reactivationTime = -1;
			try {
				time = FileUtils.readFileToString(file);
				reactivationTime = Long.parseLong(time);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				reactivationTime = -1;
			} catch (Exception e) {
				e.printStackTrace();
				reactivationTime = -1;
			}
			
			if (reactivationTime!=-1) {
				//Check if the current time is later than the reactivation time.
				long currentTime = System.currentTimeMillis();
				if (currentTime >= reactivationTime) {
					//Yeap, the trial's been reactivated.
					return true;
				} else {
					//Nope, the trial's still in the expired state.
					return false;
				}
				
			} else {
				//We were unable to read the activation time. Continue the trial.
				return true;
			}
			
		} else {
			//The file doesn't exist, so the trial is active.
			return true;
		}
		
	}
	
	private void showTrialFragment(final boolean expired, int numDaysRemaining) {
		
		//Load the trial fragment into the activity.
		TrialFragment fragment = new TrialFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("NUM_DAYS_REMAINING", numDaysRemaining);
		bundle.putBoolean("EXPIRED", expired);
		fragment.setArguments(bundle);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	    transaction.replace(R.id.launcher_root_view, fragment, "trialFragment");
	    transaction.commit();

	}

	private void showUpgradeFragmentWithPromo() {
		/*SpecialUpgradeOfferFragment fragment = new SpecialUpgradeOfferFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	    transaction.replace(R.id.launcher_root_view, fragment, "specialUpgradeOfferFragment");
	    transaction.commit();*/
	    
	}
	
	public void showTrialDialog(final boolean expired, int numDaysRemaining) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.app_name);
		builder.setCancelable(false);

		View view = this.getLayoutInflater().inflate(R.layout.trial_expiry_dialog, null);
		TextView trialExpiredText = (TextView) view.findViewById(R.id.trial_message);
		TextView trialDaysRemaining = (TextView) view.findViewById(R.id.trial_days_remaining);
		TextView trialDaysCaps = (TextView) view.findViewById(R.id.days_caps);
		
		trialExpiredText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		trialExpiredText.setPaintFlags(trialExpiredText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		trialDaysRemaining.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		
		trialDaysCaps.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		trialDaysCaps.setPaintFlags(trialDaysCaps.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		if (expired) {
			trialDaysRemaining.setText(R.string.expired);
			trialExpiredText.setText(R.string.trial_expired);
			trialDaysRemaining.setTextColor(0xFFFF8800);
			trialDaysCaps.setVisibility(View.GONE);
			trialDaysRemaining.setTextSize(36);
			trialDaysRemaining.setPaintFlags(trialDaysRemaining.getPaintFlags() 
											 | Paint.ANTI_ALIAS_FLAG 
											 | Paint.SUBPIXEL_TEXT_FLAG);
		} else {
			trialExpiredText.setText(R.string.trial_running);
			trialDaysRemaining.setText("" + numDaysRemaining);
			trialDaysCaps.setVisibility(View.VISIBLE);
			trialDaysRemaining.setTextColor(0xFF0099CC);
			trialDaysRemaining.setPaintFlags(trialDaysRemaining.getPaintFlags() 
											 | Paint.ANTI_ALIAS_FLAG 
											 | Paint.SUBPIXEL_TEXT_FLAG 
											 | Paint.FAKE_BOLD_TEXT_FLAG);
		}
		
		builder.setView(view);
		builder.setPositiveButton(R.string.upgrade, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				showUpgradeFragmentWithPromo();

			}
			
		});
		
		builder.setNegativeButton(R.string.later, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (expired) {
					finish();
				} else {
					launchMainActivity();
				}
				
				
			}
			
		});
		
		builder.create().show();
	}
	
	private void launchMainActivity() {
		Intent intent = new Intent(mContext, MainActivity.class);
		int startupScreen = mApp.getSharedPreferences().getInt("STARTUP_SCREEN", 0);
		
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
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		finish();
		
	}
	
	public void showErrorDialog(final int code) {
        runOnUiThread(new Runnable() {
        	
            @Override
            public void run() {
              Dialog d = GooglePlayServicesUtil.getErrorDialog(
		                 code,
		                 LauncherActivity.this,
		                 REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
              d.show();
            }
            
        });
        
    }
	
	/** Given a URI, returns a map of campaign data that can be sent with
	 * any GA hit.
	 *
	 * @param uri A hierarchical URI that may or may not have campaign data
	 *     stored in query parameters.
	 *
	 * @return A map that may contain campaign or referrer
	 *     that may be sent with any Google Analytics hit.
   	 */
	private Map<String,String> getReferrerMapFromUri(Uri uri) {
		 MapBuilder paramMap = new MapBuilder();

		 //If no URI, return an empty Map.
		 if (uri==null) { 
			 return paramMap.build(); 
		 }

		 /* Source is the only required campaign field. No need to continue if not
		  * present. */
		 if (uri.getQueryParameter(CAMPAIGN_SOURCE_PARAM)!=null) {

			 /* MapBuilder.setCampaignParamsFromUrl parses Google Analytics campaign
			  * ("UTM") parameters from a string URL into a Map that can be set on
			  * the Tracker. */
			 paramMap.setCampaignParamsFromUrl(uri.toString());

			 /* If no source parameter, set authority to source and medium to
			  * "referral". */
		 } else if (uri.getAuthority()!=null) {
			 paramMap.set(Fields.CAMPAIGN_MEDIUM, "referral");
			 paramMap.set(Fields.CAMPAIGN_SOURCE, uri.getAuthority());
		 }

		 return paramMap.build();
	}

	@Override
	public void onPause() {
		super.onPause();

		try {
			if (isFinishing() && mHelper!=null) {
				mHelper.dispose();
				mHelper = null;	
			}
			
			finish();			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	 
	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			if (mHelper!=null) {
		 		mHelper.dispose();
		 		mHelper = null;
		 	}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	 	
	}

	@Override
	public void onStart() {
		super.onStart();
		//GAnalytics.
		try {
			if (mApp.isGoogleAnalyticsEnabled()==true) {
				EasyTracker.getInstance(this).activityStart(this);
				
				//Get the intent that started this Activity.
			    Intent intent = this.getIntent();
			    Uri uri = intent.getData();

			    //Send a screenview using any available campaign or referrer data.
			    MapBuilder.createAppView().setAll(getReferrerMapFromUri(uri));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	}

	@Override
	public void onStop() {
		super.onStop();
		//GAnalytics.
		try {
			if (mApp.isGoogleAnalyticsEnabled()==true) {
				EasyTracker.getInstance(this).activityStop(this);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
