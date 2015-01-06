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
package com.aniruddhc.acemusic.player.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.Drawers.NavigationDrawerFragment;
import com.aniruddhc.acemusic.player.Drawers.QueueDrawerFragment;
import com.aniruddhc.acemusic.player.FoldersFragment.FilesFoldersFragment;
import com.aniruddhc.acemusic.player.GridViewFragment.GridViewFragment;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.ListViewFragment.ListViewFragment;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

public class MainActivity extends FragmentActivity {

	//Context and Common object(s).
	private Context mContext;
	private Common mApp;

	//UI elements.
	private FrameLayout mDrawerParentLayout;
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mNavDrawerLayout;
	private RelativeLayout mCurrentQueueDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
    private QueueDrawerFragment mQueueDrawerFragment;
    private Menu mMenu;
	
	//Current fragment params.
	private Fragment mCurrentFragment;
	public static int mCurrentFragmentId;
	public static int mCurrentFragmentLayout;
	
	//Layout flags.
	public static final String CURRENT_FRAGMENT = "CurrentFragment";
	public static final String ARTISTS_FRAGMENT_LAYOUT = "ArtistsFragmentLayout";
	public static final String ALBUM_ARTISTS_FRAGMENT_LAYOUT = "AlbumArtistsFragmentLayout";
	public static final String ALBUMS_FRAGMENT_LAYOUT = "AlbumsFragmentLayout";
	public static final String PLAYLISTS_FRAGMENT_LAYOUT = "PlaylistsFragmentLayout";
	public static final String GENRES_FRAGMENT_LAYOUT = "GenresFragmentLayout";
	public static final String FOLDERS_FRAGMENT_LAYOUT = "FoldersFragmentLayout";
    public static final String FRAGMENT_HEADER = "FragmentHeader";
	public static final int LIST_LAYOUT = 0;
	public static final int GRID_LAYOUT = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Context and Common object(s).
        mContext = getApplicationContext();
        mApp = (Common) getApplicationContext();
        
        //Set the theme and inflate the layout.
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Init the UI elements.
        mDrawerParentLayout = (FrameLayout) findViewById(R.id.main_activity_root);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity_drawer_root);
        mNavDrawerLayout = (RelativeLayout) findViewById(R.id.nav_drawer_container);
        mCurrentQueueDrawerLayout = (RelativeLayout) findViewById(R.id.current_queue_drawer_container);

        //Load the drawer fragments.
        loadDrawerFragments();
		
        //KitKat specific translucency.
        applyKitKatTranslucency();
        
        //Load the fragment.
        loadFragment(savedInstanceState);
        
    	/**
    	 * Navigation drawer toggle.
    	 */
    	mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 
    			  								  R.drawable.ic_navigation_drawer, 
    			  								  0, 0) {

    		@Override
    		public void onDrawerClosed(View view) {
    			if (mQueueDrawerFragment!=null &&
                    view==mCurrentQueueDrawerLayout)
                    mQueueDrawerFragment.setIsDrawerOpen(false);
    		
    		}

    		@Override
    		public void onDrawerOpened(View view) {
                if (mQueueDrawerFragment!=null &&
                    view==mCurrentQueueDrawerLayout)
                    mQueueDrawerFragment.setIsDrawerOpen(true);

    		}

    	};

    	//Apply the drawer toggle to the DrawerLayout.
    	mDrawerLayout.setDrawerListener(mDrawerToggle);
    	getActionBar().setDisplayHomeAsUpEnabled(true);
    	getActionBar().setDisplayShowHomeEnabled(true);

        //Check if this is the first time the app is being started.
        if (mApp.getSharedPreferences().getBoolean(Common.FIRST_RUN, true)==true) {
            showAlbumArtScanningDialog();
            mApp.getSharedPreferences().edit().putBoolean(Common.FIRST_RUN, false).commit();
        }
    	
	}
	
	/**
	 * Sets the entire activity-wide theme.
	 */
	private void setTheme() {
    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}
    	
	}
	
	/**
	 * Apply KitKat specific translucency.
	 */
	private void applyKitKatTranslucency() {
		
		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        	//Set the window background.
        	getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        	
    		int topPadding = Common.getStatusBarHeight(mContext);
    		if (mDrawerLayout!=null) {
    			mDrawerLayout.setPadding(0, topPadding, 0, 0);
    			mNavDrawerLayout.setPadding(0, topPadding, 0, 0);
    			mCurrentQueueDrawerLayout.setPadding(0, topPadding, 0, 0);
    		}

            //Calculate ActionBar and navigation bar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

            if (mDrawerParentLayout!=null) {
            	mDrawerParentLayout.setPadding(0, actionBarHeight, 0, 0);
            	mDrawerParentLayout.setClipToPadding(false);
            }
            
        }
        
	}
	
	/**
	 * Loads the correct fragment based on the selected browser.
	 */
	public void loadFragment(Bundle savedInstanceState) {
		//Get the target fragment from savedInstanceState if it's not null (orientation changes?).
		if (savedInstanceState!=null) {
			mCurrentFragmentId = savedInstanceState.getInt(CURRENT_FRAGMENT);
            invalidateOptionsMenu();
			
		} else {
			//Set the current fragment based on the intent's extras.
    		if (getIntent().hasExtra(CURRENT_FRAGMENT)) {
    			mCurrentFragmentId = getIntent().getExtras().getInt(CURRENT_FRAGMENT);
    		}
    		
    		switch (mCurrentFragmentId) {
    		case Common.ARTISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ARTISTS_FRAGMENT);
    			break;
    		case Common.ALBUM_ARTISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ALBUM_ARTISTS_FRAGMENT);
    			break;
    		case Common.ALBUMS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ALBUMS_FRAGMENT);
    			break;
    		case Common.SONGS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.SONGS_FRAGMENT);
    			break;
    		case Common.PLAYLISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.PLAYLISTS_FRAGMENT);
    			break;
    		case Common.GENRES_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.GENRES_FRAGMENT);
    			break;
    		case Common.FOLDERS_FRAGMENT:
    			mCurrentFragment = new FilesFoldersFragment();
    			break;
    		}
    		
    		switchContent(mCurrentFragment);
		}
		
	}
	
	/**
	 * Retrieves the correct fragment based on the saved layout preference.
	 */
	private Fragment getLayoutFragment(int fragmentId) {
		
		//Instantiate a new bundle.
		Fragment fragment = null;
		Bundle bundle = new Bundle();
		
		//Retrieve layout preferences for the current fragment.
		switch (fragmentId) {
		case Common.ARTISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ARTISTS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ARTISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.artists));
			break;
		case Common.ALBUM_ARTISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ALBUM_ARTISTS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ALBUM_ARTISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.album_artists));
			break;
		case Common.ALBUMS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ALBUMS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ALBUMS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.albums));
			break;
		case Common.SONGS_FRAGMENT:
			mCurrentFragmentLayout = LIST_LAYOUT;
			bundle.putInt(Common.FRAGMENT_ID, Common.SONGS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.songs));
			break;
		case Common.PLAYLISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(PLAYLISTS_FRAGMENT_LAYOUT, LIST_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.PLAYLISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.playlists));
			break;
		case Common.GENRES_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(GENRES_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.GENRES_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.genres));
			break;
		case Common.FOLDERS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(FOLDERS_FRAGMENT_LAYOUT, LIST_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.FOLDERS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.folders));
			break;
		}		
				
		//Return the correct layout fragment.
		if (mCurrentFragmentLayout==GRID_LAYOUT) {
			fragment = new GridViewFragment();
			fragment.setArguments(bundle);
		} else {
			fragment = new ListViewFragment();
			fragment.setArguments(bundle);
		}
		
		return fragment;
	}
	
	/**
	 * Loads the specified fragment into the target layout.
	 */
	public void switchContent(Fragment fragment) {
        // Reset action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(false);

		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.mainActivityContainer, fragment)
								   .commit();
		
		//Close the drawer(s).
		mDrawerLayout.closeDrawer(Gravity.START);
        invalidateOptionsMenu();

	}
	
	/**
	 * Loads the drawer fragments.
	 */
	private void loadDrawerFragments() {
		//Load the navigation drawer.
		getSupportFragmentManager().beginTransaction()
		   						   .replace(R.id.nav_drawer_container, new NavigationDrawerFragment())
		   						   .commit();
		
		//Load the current queue drawer.
        mQueueDrawerFragment = new QueueDrawerFragment();
		getSupportFragmentManager().beginTransaction()
		   						   .replace(R.id.current_queue_drawer_container, mQueueDrawerFragment)
		   						   .commit();
		
	}

	/**
	 * Called when the user taps on the "Play all" or "Shuffle all" action button.
	 */
	public void playAll(boolean shuffle) {
		//Start the playback sequence.
		mApp.getPlaybackKickstarter().initPlayback(mContext, "", Common.PLAY_ALL_SONGS, 0, true, true);

	}

    /**
     * Displays the message dialog for album art processing.
     */
    private void showAlbumArtScanningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.album_art);
        builder.setMessage(R.string.scanning_for_album_art_details);
        builder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        builder.create().show();
    }

    /**
     * Inflates the generic MainActivity ActionBar layout.
     *
     * @param inflater The ActionBar's menu inflater.
     * @param menu The ActionBar menu to work with.
     */
    private void showMainActivityActionItems(MenuInflater inflater, Menu menu) {
        //Inflate the menu.
        getMenu().clear();
        inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        //Set the ActionBar background
        getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setHomeButtonEnabled(true);

        //Set the ActionBar text color.
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(0xFFFFFFFF);
            }

        }

    }

    /**
     * Displays the folder fragment's action items.
     *
     * @param filePath The file path to set as the ActionBar's title text.
     * @param inflater The ActionBar's menu inflater.
     * @param menu The ActionBar menu to work with.
     * @param showPaste Pass true if the ActionBar is being updated for a copy/move operation.
     */
    public void showFolderFragmentActionItems(String filePath,
                                              MenuInflater inflater,
                                              Menu menu,
                                              boolean showPaste) {
        getMenu().clear();
        inflater.inflate(R.menu.files_folders_fragment, menu);


        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setLogo(0);
        getActionBar().setIcon(0);

        if (showPaste) {
            //Change the ActionBar's background and show the Paste Here option.
            menu.findItem(R.id.action_paste).setVisible(true);
            menu.findItem(R.id.action_cancel).setVisible(true);
            getActionBar().setBackgroundDrawable(mContext.getResources()
                                                         .getDrawable(R.drawable.cab_background_top_apptheme));

            //Change the KitKat system bar color.
            if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT)
                getWindow().setBackgroundDrawable(new ColorDrawable(0xFF002E3E));

        } else {
            //Hide the Paste Here option and set the default ActionBar background.
            menu.findItem(R.id.action_paste).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(false);
            getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

            //Change the KitKat system bar color.
            if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT)
                getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

        }

        LayoutInflater inflator = LayoutInflater.from(this);
        View view = inflator.inflate(R.layout.custom_actionbar_layout, null);

        TextView titleText = (TextView) view.findViewById(R.id.custom_actionbar_title);
        titleText.setText(filePath);
        titleText.setSelected(true);
        titleText.setTextColor(0xFFFFFFFF);

        //Inject the custom view into the ActionBar.
        getActionBar().setCustomView(view);

    }
	
	/**
	 * Initializes the ActionBar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (mCurrentFragmentId==Common.FOLDERS_FRAGMENT)
            showFolderFragmentActionItems(FilesFoldersFragment.currentDir,
                                          getMenuInflater(), menu, false);
        else
            showMainActivityActionItems(getMenuInflater(), menu);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ActionBar item selection listener.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
	    }
		
		switch (item.getItemId()) {
		case R.id.action_search:
			//ArtistsFragment.showSearch();
			return true;
	    case R.id.action_queue_drawer:
	    	if (mDrawerLayout!=null && mCurrentQueueDrawerLayout!=null) {
		    	if (mDrawerLayout.isDrawerOpen(mCurrentQueueDrawerLayout)) {
		    		mDrawerLayout.closeDrawer(mCurrentQueueDrawerLayout);
		    	} else {
		    		mDrawerLayout.openDrawer(mCurrentQueueDrawerLayout);
		    	}
		    	
	    	}
	    	return true;
        case R.id.action_up:
            ((FilesFoldersFragment) mCurrentFragment).getParentDir();
            return true;
        case R.id.action_paste:
            ((FilesFoldersFragment) mCurrentFragment).pasteIntoCurrentDir(((FilesFoldersFragment) mCurrentFragment).copyMoveSourceFile);
            showMainActivityActionItems(getMenuInflater(), getMenu());
            return true;
        case R.id.action_cancel:
            ((FilesFoldersFragment) mCurrentFragment).copyMoveSourceFile = null;
            if (((FilesFoldersFragment) mCurrentFragment).shouldMoveCopiedFile)
                Toast.makeText(mContext, R.string.move_canceled, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(mContext, R.string.copy_canceled, Toast.LENGTH_LONG).show();
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
		
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(Gravity.START)) { // Close left drawer if opened
            mDrawerLayout.closeDrawer(Gravity.START);

        } else if (getCurrentFragmentId()==Common.FOLDERS_FRAGMENT) {
            if (((FilesFoldersFragment) mCurrentFragment).getCurrentDir().equals("/"))
                super.onBackPressed();
            else
                ((FilesFoldersFragment) mCurrentFragment).getParentDir();

        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {
            getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
            getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        }

    }
	
	/**
	 * Getters/Setters.
	 */
	
	public int getCurrentFragmentId() {
		return mCurrentFragmentId;
	}
	
	public void setCurrentFragmentId(int id) {
		mCurrentFragmentId = id;
	}

    public Menu getMenu() {
        return mMenu;
    }
	
}
