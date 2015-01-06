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
package com.aniruddhc.acemusic.player.AsyncTasks;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.GMusicHelpers.GMusicClientCalls;
import com.aniruddhc.acemusic.player.LauncherActivity.LauncherActivity;
import com.aniruddhc.acemusic.player.SettingsActivity.SettingsActivity____;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncGoogleMusicAuthenticationTask extends AsyncTask<String, String, String> {
	
    private Context mContext;
    private Activity mActivity;
    
    private ProgressDialog pd;
	boolean dialogVisible = true;
	private boolean mFirstRun = false;
	private boolean mFirstRunFromSettings = false;
	private Common mApp;
	
	private String mAccountName;
	private String authToken = "";
	
	private int availabilityExceptionStatusCode;
	private Intent userRecoverableExceptionIntent;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    public AsyncGoogleMusicAuthenticationTask(Context context, FragmentActivity activity, boolean firstRun, String accountName) {
    	mContext = context;
    	mActivity = activity;
    	mAccountName = accountName;
    	mFirstRun = firstRun;
    	
		mApp = (Common) mContext.getApplicationContext();
    }
    
    public AsyncGoogleMusicAuthenticationTask(Context context, boolean firstRun, String accountName) {
    	mContext = context;
    	mAccountName = accountName;
    	mFirstRun = firstRun;
    	
		mApp = (Common) mContext.getApplicationContext();
    }
    
    public AsyncGoogleMusicAuthenticationTask(Context context, SettingsActivity____ activity, boolean firstRun, String accountName) {
    	mContext = context;
    	mActivity = activity;
    	mAccountName = accountName;
    	mFirstRun = firstRun;
    	mFirstRunFromSettings = true;
    	
		mApp = (Common) mContext.getApplicationContext();
    }
    
    protected void onPreExecute() {
    	
    	if (mFirstRun) {
        	pd = new ProgressDialog(mActivity);
    		pd.setCancelable(false);
    		pd.setIndeterminate(true);
    		pd.setTitle(R.string.signing_in);
    		pd.setMessage(mContext.getResources().getString(R.string.contacting_google_play_music));
    		pd.show();
    	}

    }
 
    @SuppressWarnings("static-access")
	@Override
    protected String doInBackground(String... params) {

		try {
			authToken = GoogleAuthUtil.getToken(mContext, mAccountName, "sj");
		} catch (GooglePlayServicesAvailabilityException e) {
			e.printStackTrace();
			availabilityExceptionStatusCode = e.getConnectionStatusCode();
			return "GOOGLE_PLAY_SERVICES_AVAILABILITY_EXCEPTION";
		} catch (UserRecoverableAuthException e) {
			e.printStackTrace();
			userRecoverableExceptionIntent = e.getIntent();
			return "USER_RECOVERABLE_AUTH_EXCEPTION";
		} catch (GoogleAuthException e) {
			e.printStackTrace();
			return "GOOGLE_AUTH_EXCEPTION";
		} catch (Exception e) {
			e.printStackTrace();
			return "GENERIC_EXCEPTION";
		}
		
		if (mFirstRun) {
			publishProgress(mContext.getResources().getString(R.string.signing_in_to_google_play_music));
		}
		
		//Login to Google Play Music using the unofficial API.
		mApp.setGMusicClientCalls(GMusicClientCalls.getInstance(mContext));
		boolean loginResult = mApp.getGMusicClientCalls().login(mContext, authToken);
		
		if (loginResult==true) {			
	    	return "AUTHENTICATED";
		} else {
	    	return "GENERIC_EXCEPTION";
		}
		
    }

    @Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		
		String message = values[0];
		pd.setMessage(message);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
    	
		if (mFirstRun) {
			pd.dismiss();
		}
    	
    	//Perform an action based on the operation's result code.
    	if (result.equals("GOOGLE_PLAY_SERVICES_AVAILABILITY_EXCEPTION")) {
    		Dialog d = GooglePlayServicesUtil.getErrorDialog(availabilityExceptionStatusCode,
					 mActivity,
					 REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
    		d.show();
    	
    	} else if (result.equals("USER_RECOVERABLE_AUTH_EXCEPTION")) {
    		//45 is an arbitrary value that identifies this activity's result.
    		LauncherActivity.mAccountName = mAccountName;
    		SettingsActivity____.mAccountName = mAccountName;
    		
    		if (mActivity!=null) {
    			mActivity.startActivityForResult(userRecoverableExceptionIntent, 45);
    		}
			
    	} else if (result.equals("GOOGLE_AUTH_EXCEPTION") || result.equals("GENERIC_EXCEPTION")) {
    		Toast.makeText(mContext, R.string.unknown_error_google_music, Toast.LENGTH_LONG).show();
    	} else if (result.equals("AUTHENTICATED")) {
    		if (mFirstRun) {
        		String text = mContext.getResources().getString(R.string.signed_in_as) + " " + mAccountName;
        		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    		} else if (mFirstRunFromSettings) {
    			//Start scanning the library to add GMusic songs.
    			String text = mContext.getResources().getString(R.string.signed_in_as) + " " + mAccountName;
        		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        		
        		//Seting the "REBUILD_LIBRARY" flag to true will force MainActivity to rescan the folders.
				mApp.getSharedPreferences().edit().putBoolean("REBUILD_LIBRARY", true).commit();
				
				//Restart the app.
				final Intent i = mActivity.getBaseContext().getPackageManager().getLaunchIntentForPackage(mActivity.getBaseContext().getPackageName());
				
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mActivity.finish();
				mActivity.startActivity(i);
    		}
    		mApp.getSharedPreferences().edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", true).commit();
    	} else {
    		Toast.makeText(mContext, R.string.unknown_error_google_music, Toast.LENGTH_LONG).show();
    	}
    	
	}

}
