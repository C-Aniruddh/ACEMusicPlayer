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
package com.aniruddhc.acemusic.player.PlaylistEditorActivity;

import java.util.HashSet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PlaylistEditorActivity extends FragmentActivity {
	
	private Context mContext;
	private Common mApp;
	private SharedPreferences sharedPreferences;
	private String libraryName;
	private String libraryIconName;
	public static String currentTab = "Artists";
	public static DisplayImageOptions displayImageOptions;
	public static HashSet<String> songDBIdsList = new HashSet<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Initialize Context and SharedPreferences.
		mContext = this;
		mApp = (Common) this.getApplicationContext();
		sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}
		super.onCreate(savedInstanceState);

		//Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
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
		
		//Attach tabs to the ActionBar.
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//Add the artists tab.
		String artistsLabel = getResources().getString(R.string.artists);
		Tab tab = actionBar.newTab();
		tab.setText(artistsLabel);
		TabListener<ArtistsPickerFragment> artistsTabListener = new TabListener<ArtistsPickerFragment>(this, 
																					   				   artistsLabel, 
																					   				   ArtistsPickerFragment.class);
		
		tab.setTabListener(artistsTabListener);
		actionBar.addTab(tab);

		//Add the albums tab.
		String albumsLabel = getResources().getString(R.string.albums);
		tab = actionBar.newTab();
		tab.setText(albumsLabel);
		TabListener<AlbumsPickerFragment> albumsTabListener = new TabListener<AlbumsPickerFragment>(this,
																					  				albumsLabel, 
																					  				AlbumsPickerFragment.class);
		
		tab.setTabListener(albumsTabListener);
		actionBar.addTab(tab);
		
		//Add the songs tab.
		String songsLabel = getResources().getString(R.string.songs);
		tab = actionBar.newTab();
		tab.setText(songsLabel);
		TabListener<SongsPickerFragment> songsTabListener = new TabListener<SongsPickerFragment>(this,
																								 songsLabel, 
																								 SongsPickerFragment.class);
		
		tab.setTabListener(songsTabListener);
		actionBar.addTab(tab);
		
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
				ft.replace(android.R.id.content, mFragment, mTag);
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
	
	public void createPlaylist() {
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.add_to_music_library, menu);
	    
	    ActionBar actionBar = getActionBar();
		SpannableString s = new SpannableString(getResources().getString(R.string.create_playlist));
	    s.setSpan(new TypefaceSpan(this, "RobotoCondensed-Light"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    actionBar.setTitle(s);
	    
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.select_all_music_library_editor:
	        /* DB IDs are sequential, so to save CPU cycles, 
	         * we'll just get the size of the DB (the number of 
	         * rows) and add that many entries to the HashSet.
	         */
	    	int songCount = mApp.getDBAccessHelper().getAllSongs().getCount();
	    	for (int i=0; i < songCount+1; i++) {
	    		songDBIdsList.add("" + i);
	    	}
	    	
	    	//Refresh the current fragment's listview.
	    	if (ArtistsPickerFragment.listView!=null) {
	    		ArtistsPickerFragment.listView.setAdapter(null);
	    		ArtistsPickerFragment.listView.setAdapter(new PlaylistEditorArtistsMultiselectAdapter(this, 
	    																								  ArtistsPickerFragment.cursor));
	    		ArtistsPickerFragment.listView.invalidate();
	    	}
	    	
	    	if (AlbumsPickerFragment.listView!=null) {
	    		AlbumsPickerFragment.listView.setAdapter(null);
	    		AlbumsPickerFragment.listView.setAdapter(new PlaylistEditorAlbumsMultiselectAdapter(this, 
	    																								  AlbumsPickerFragment.cursor));
	    		AlbumsPickerFragment.listView.invalidate();
	    	}
	    	
	    	if (SongsPickerFragment.listView!=null) {
	    		SongsPickerFragment.listView.setAdapter(null);
	    		SongsPickerFragment.listView.setAdapter(new PlaylistEditorSongsMultiselectAdapter(this, 
	    																								  SongsPickerFragment.cursor));
	    		SongsPickerFragment.listView.invalidate();
	    	}
	    	
	        return true;
	    case R.id.done_music_library_editor:
	    	createPlaylist();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	public void onPause() {
		super.onPause();
		songDBIdsList.clear();
		
		if (isFinishing()) {
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
		
	}
	
}
