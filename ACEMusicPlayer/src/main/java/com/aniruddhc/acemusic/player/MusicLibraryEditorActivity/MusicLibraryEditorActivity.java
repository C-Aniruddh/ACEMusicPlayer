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
package com.aniruddhc.acemusic.player.MusicLibraryEditorActivity;

import java.util.HashSet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncCreateMusicLibraryTask;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MusicLibraryEditorActivity extends FragmentActivity {
	
	private Context mContext;
	private Common mApp;
	private String libraryName;
	private String libraryIconName;
	public static DBAccessHelper dbHelper;
	public static String currentTab = "Artists";
	public static DisplayImageOptions displayImageOptions;
	public static HashSet<String> songDBIdsList = new HashSet<String>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Initialize Context and SharedPreferences.
		mContext = this;
        mApp = (Common) mContext.getApplicationContext();
		
		//Retrieve the name/icon of the library from the arguments.
		libraryName = getIntent().getExtras().getString("LIBRARY_NAME");
		libraryIconName = getIntent().getExtras().getString("LIBRARY_ICON");
		
		if (getIntent().getExtras().getSerializable("SONG_IDS_HASH_SET")!=null) {
			songDBIdsList = (HashSet<String>) getIntent().getExtras().getSerializable("SONG_IDS_HASH_SET");
		}

    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}
		super.onCreate(savedInstanceState);
		
		//Initialize the database helper.
		dbHelper = new DBAccessHelper(mContext.getApplicationContext());

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
	
	public void createMusicLibrary() {
		//We're done with the database helper, so go ahead and close it.
		dbHelper.close();
		dbHelper = null;
		
		//Launch the AsyncTask that will create the new music library.
		AsyncCreateMusicLibraryTask task = new AsyncCreateMusicLibraryTask(this, this, songDBIdsList, libraryName, libraryIconName);
		task.execute();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.add_to_music_library, menu);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
		actionBar.setIcon(mContext.getResources().getIdentifier(libraryIconName, "drawable", mContext.getPackageName()));
		SpannableString s = new SpannableString(libraryName);
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
	    	Cursor cursor = null;
	    	if (dbHelper!=null) {
	    		cursor = dbHelper.getAllSongs();
	    	}
	    	
	    	int songCount = 0;
	    	if (cursor!=null) {
	    		songCount = cursor.getCount();
	    	} else {
	    		Toast.makeText(mContext, R.string.no_songs_to_select, Toast.LENGTH_SHORT).show();
	    		return true;
	    	}
	    	
	    	for (int i=0; i < songCount+1; i++) {
	    		songDBIdsList.add("" + i);
	    	}
	    	
	    	//Refresh the current fragment's listview.
	    	if (ArtistsPickerFragment.listView!=null) {
	    		ArtistsPickerFragment.listView.setAdapter(null);
	    		ArtistsPickerFragment.listView.setAdapter(new MusicLibraryEditorArtistsMultiselectAdapter(this, 
	    																								  ArtistsPickerFragment.cursor));
	    		ArtistsPickerFragment.listView.invalidate();
	    	}
	    	
	    	if (AlbumsPickerFragment.listView!=null) {
	    		AlbumsPickerFragment.listView.setAdapter(null);
	    		AlbumsPickerFragment.listView.setAdapter(new MusicLibraryEditorAlbumsMultiselectAdapter(this, 
	    																								  AlbumsPickerFragment.cursor));
	    		AlbumsPickerFragment.listView.invalidate();
	    	}
	    	
	    	if (SongsPickerFragment.listView!=null) {
	    		SongsPickerFragment.listView.setAdapter(null);
	    		SongsPickerFragment.listView.setAdapter(new MusicLibraryEditorSongsMultiselectAdapter(this, 
	    																								  SongsPickerFragment.cursor));
	    		SongsPickerFragment.listView.invalidate();
	    	}
	    	
	        return true;
	    case R.id.done_music_library_editor:
	    	createMusicLibrary();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	public void onPause() {
		super.onPause();
		songDBIdsList.clear();
		
		if (dbHelper!=null) {
			dbHelper.close();
			dbHelper = null;
		}
		
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
