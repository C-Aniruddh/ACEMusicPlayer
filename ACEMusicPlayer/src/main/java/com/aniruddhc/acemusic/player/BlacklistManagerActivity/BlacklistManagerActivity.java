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
package com.aniruddhc.acemusic.player.BlacklistManagerActivity;

import java.util.HashMap;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.AlbumsPickerFragment;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.ArtistsPickerFragment;
import com.aniruddhc.acemusic.player.MusicLibraryEditorActivity.SongsPickerFragment;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class BlacklistManagerActivity extends FragmentActivity {

	private Context mContext;
	private Common mApp;
	private Activity mActivity;
	private SharedPreferences sharedPreferences;
	private String libraryName;
	private String libraryIconName;
	public static ActionBar actionBar;
	public static String currentTab = "Artists";
	public static DisplayImageOptions displayImageOptions;
	public static HashMap<String, Boolean> songIdBlacklistStatusPair = new HashMap<String, Boolean>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Initialize Context and SharedPreferences.
		mContext = this;
		mActivity = this;
		mApp = (Common) this.getApplicationContext();
		sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}
		super.onCreate(savedInstanceState);
		
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	    	getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
	    	int topPadding = Common.getStatusBarHeight(mContext);
	    	View activityView = (View) findViewById(android.R.id.content);
	    	
	    	//Calculate ActionBar height
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
            
            if (activityView!=null) {
            	activityView.setPadding(0, topPadding + actionBarHeight, 0, 0);
            }
            
	    }

		//Retrieve the actionbar.
		actionBar = getActionBar();
		
		//Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
        int defaultArt = UIElementsHelper.getIcon(mContext, "default_album_art_padded");
        displayImageOptions = new DisplayImageOptions.Builder()
        						  .showImageForEmptyUri(R.drawable.default_album_art)
        						  .showImageOnFail(R.drawable.default_album_art)
        						  .showStubImage(R.drawable.transparent_drawable)
        						  .cacheInMemory(false)
        						  .cacheOnDisc(true)
        						  .decodingOptions(options)
        						  .imageScaleType(ImageScaleType.EXACTLY)
        						  .bitmapConfig(Bitmap.Config.RGB_565)
        						  .displayer(new FadeInBitmapDisplayer(400))
        						  .delayBeforeLoading(100)
        						  .build();
		
		//Retrieve a list of blacklisted songs.
		AsyncGetAllSongIdsBlacklistStatusTask task = new AsyncGetAllSongIdsBlacklistStatusTask();
		task.execute();
			
	}
	
	private class TabListener<T extends android.app.Fragment> implements ActionBar.TabListener {
		private android.app.Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		@Override
		public void onTabReselected(Tab arg0, android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			
			currentTab = (String) tab.getText();
			//Check if the fragment is already initialized
			if (mFragment==null) {
				//If not, instantiate and add it to the activity
				mFragment = android.app.Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				//If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
			
		}

		@Override
		public void onTabUnselected(Tab arg0, android.app.FragmentTransaction ft) {
			if (mFragment!=null) {
				ft.detach(mFragment);
			}
			
		}
	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.blacklist_manager, menu);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
		SpannableString s = new SpannableString(getResources().getString(R.string.blacklist_manager));
	    s.setSpan(new TypefaceSpan(this, "RobotoCondensed-Light"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    actionBar.setTitle(s);
	    
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.done_blacklist_manager:
	    	AsyncBlacklistSongsTask task = new AsyncBlacklistSongsTask();
	    	task.execute();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	public void onPause() {
		super.onPause();
		songIdBlacklistStatusPair.clear();
		
		if (SongsPickerFragment.cursor!=null) {
			SongsPickerFragment.cursor.close();
			SongsPickerFragment.cursor = null;
		}
		
		if (AlbumsPickerFragment.cursor!=null) {
			AlbumsPickerFragment.cursor.close();
			AlbumsPickerFragment.cursor = null;
		}
		
		if (ArtistsPickerFragment.cursor!=null) {
			ArtistsPickerFragment.cursor.close();
			ArtistsPickerFragment.cursor = null;
		}
		
	}
	
	class AsyncBlacklistSongsTask extends AsyncTask<String, String, String> {
		 
		private ProgressDialog pd;
		
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			
			pd = new ProgressDialog(mContext);
			pd.setTitle(R.string.blacklist_manager);
			pd.setIndeterminate(true);
			pd.setMessage(mContext.getResources().getString(R.string.updating_blacklists));
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);
			pd.show();
			
		}
		
		@Override
		protected String doInBackground(String... params) {
			mApp.getDBAccessHelper().batchUpdateSongBlacklist(songIdBlacklistStatusPair);
			return null;
		}
		
		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);
			
			//Dismiss the progress dialog.
			pd.dismiss();
			mActivity.finish();
			Toast.makeText(mContext, R.string.done_updating_blacklists, Toast.LENGTH_LONG).show();
		}
		
	}
	
	class AsyncGetAllSongIdsBlacklistStatusTask extends AsyncTask<String, String, String> {
		 
		private ProgressDialog pd;
		
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			
			pd = new ProgressDialog(mContext);
			pd.setTitle(R.string.blacklist_manager);
			pd.setIndeterminate(true);
			pd.setMessage(mContext.getResources().getString(R.string.fetching_blacklists));
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);
			pd.show();
			
		}
		
		@Override
		protected String doInBackground(String... params) {
			songIdBlacklistStatusPair = mApp.getDBAccessHelper().getAllSongIdsBlacklistStatus();
			return null;
		}
		
		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);
			
			//Dismiss the progress dialog.
			pd.dismiss();
			
			//Initialize the tabs.
			//Attach tabs to the ActionBar.
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			//Add the artists tab.
			String artistsLabel = getResources().getString(R.string.artists);
			Tab tab = actionBar.newTab();
			tab.setText(artistsLabel);
			TabListener<BlacklistedArtistsPickerFragment> artistsTabListener = new TabListener<BlacklistedArtistsPickerFragment>(mActivity, 
																						   				   artistsLabel, 
																						   				   BlacklistedArtistsPickerFragment.class);
			
			tab.setTabListener(artistsTabListener);
			actionBar.addTab(tab);

			//Add the albums tab.
			String albumsLabel = getResources().getString(R.string.albums);
			tab = actionBar.newTab();
			tab.setText(albumsLabel);
			TabListener<BlacklistedAlbumsPickerFragment> albumsTabListener = new TabListener<BlacklistedAlbumsPickerFragment>(mActivity,
																						  				albumsLabel, 
																						  				BlacklistedAlbumsPickerFragment.class);
			
			tab.setTabListener(albumsTabListener);
			actionBar.addTab(tab);
			
			//Add the songs tab.
			String songsLabel = getResources().getString(R.string.songs);
			tab = actionBar.newTab();
			tab.setText(songsLabel);
			TabListener<BlacklistedSongsPickerFragment> songsTabListener = new TabListener<BlacklistedSongsPickerFragment>(mActivity,
																									 songsLabel, 
																									 BlacklistedSongsPickerFragment.class);
			
			tab.setTabListener(songsTabListener);
			actionBar.addTab(tab);
			
		}
		
	}

}
