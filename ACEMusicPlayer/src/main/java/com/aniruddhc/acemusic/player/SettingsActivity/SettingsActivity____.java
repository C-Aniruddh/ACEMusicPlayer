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
package com.aniruddhc.acemusic.player.SettingsActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncGoogleMusicAuthenticationTask;
import com.aniruddhc.acemusic.player.BlacklistManagerActivity.BlacklistManagerActivity;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.InAppBilling.IabHelper;
import com.aniruddhc.acemusic.player.InAppBilling.IabResult;
import com.aniruddhc.acemusic.player.InAppBilling.Purchase;
import com.aniruddhc.acemusic.player.Services.AutoFetchAlbumArtService;
import com.aniruddhc.acemusic.player.Utils.Common;

public class SettingsActivity____ extends PreferenceActivity {
	 
	//Context.
	public static Context mContext;
	public static SettingsActivity____ mSettingsActivity;
	private Common mApp;
	
	//Preference Manager.
	private SharedPreferences sharedPreferences;
	private PreferenceManager preferenceManager;
	
	//Upgrade Preferences.
	private Preference upgradePreference;
	
	//Customization Preferences.
	private Preference appThemePreference;
	private Preference playerColorSchemePreference;
	private Preference trackChangeAnimationPreference;
	private Preference defaultScreenPreference;
	private Preference artistsLayoutPreference;
	private Preference albumArtistsLayoutPreference;
	private Preference albumsLayoutPreference;
	private Preference lockscreenControlsPreference;
	

	
	//Folder View Preferences.
	private Preference defaultFolderPreference;
	
	//Music Libraries Preferences.
	private Preference addMusicLibraryPreference;
	private Preference editMusicLibraryPreference;
	private Preference deleteMusicLibraryPreference;
	
	//Album Art Preferences.
	private Preference albumArtStylePreference;
	private Preference albumArtSourcesPreference;
	private Preference albumArtScanDownloadPreference;
	
	//Music Folders Preferences.
	private Preference selectFoldersPreference;
	private Preference rescanFoldersPreference;
	private Preference scanFrequencyPreference;
	
	//Blacklist Preferences.
	private Preference blacklistManagerPreference;
	private Preference unblacklistAllPreference;
	
	//Scrobbling Preference.
	private Preference scrobblingPreference;
	
	//Audio Settings Preferences.
	private Preference headphonesUnplugActionPreference;
	private Preference crossfadeTracksPreference;
	private Preference crossfadeTracksDurationPreference;
	private Preference equalizerPreference;
	
	//About & Contact Preferences.
	private Preference licensesPreference;
	private Preference contactUsPreference;
	
	public static Dialog dialogHolder;
	public static String mAccountName = "";
	public static boolean accountPicked = false;
	private IabHelper mHelper;
	static final String ITEM_SKU = "com.aniruddhc.acemusic.player.unlock";
    protected boolean mPurchased;
	
    //Misc.
	private AlertDialog mTrackChangeAnimationDialog;
    
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Context.
		mContext = this;
		mSettingsActivity = this;
		mApp = (Common) mContext.getApplicationContext();
		
		//Initialize SharedPreferences.
        sharedPreferences = this.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	
    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.LIGHT_THEME) {
    		this.setTheme(R.style.AppThemeNoActionBar);
    	} else {
    		this.setTheme(R.style.AppThemeNoActionBarLight);
    	}
		
		super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    	
    	preferenceManager = this.getPreferenceManager();
    	
    	//Retrieve the preferences based on their unique keys.
    	upgradePreference = preferenceManager.findPreference("preference_key_upgrade");
    	
    	appThemePreference = preferenceManager.findPreference("preference_key_app_theme");
    	playerColorSchemePreference = preferenceManager.findPreference("preference_key_player_color_scheme");
    	trackChangeAnimationPreference = preferenceManager.findPreference("preference_key_track_change_animation");
    	defaultScreenPreference = preferenceManager.findPreference("preference_key_startup_screen");
    	artistsLayoutPreference = preferenceManager.findPreference("preference_key_artists_layout");
    	albumArtistsLayoutPreference = preferenceManager.findPreference("preference_key_album_artists_layout");
    	albumsLayoutPreference = preferenceManager.findPreference("preference_key_albums_layout");
    	lockscreenControlsPreference = preferenceManager.findPreference("preference_key_lockscreen_controls");
    	
    	albumArtStylePreference = preferenceManager.findPreference("preference_key_album_art_style");
    	albumArtSourcesPreference = preferenceManager.findPreference("preference_key_album_art_sources");
    	albumArtScanDownloadPreference = preferenceManager.findPreference("preference_key_scan_download_album_art");

    	
    	defaultFolderPreference = preferenceManager.findPreference("preference_key_default_folder");
    	
    	addMusicLibraryPreference = preferenceManager.findPreference("preference_key_add_music_library");
    	editMusicLibraryPreference = preferenceManager.findPreference("preference_key_edit_music_library");
    	deleteMusicLibraryPreference = preferenceManager.findPreference("preference_key_delete_music_library");
    	
    	selectFoldersPreference = preferenceManager.findPreference("preference_key_select_folders");
    	rescanFoldersPreference = preferenceManager.findPreference("preference_key_rescan_folders");
    	scanFrequencyPreference = preferenceManager.findPreference("preference_key_scan_frequency");
    	
    	blacklistManagerPreference = preferenceManager.findPreference("preference_key_blacklist_manager");
    	unblacklistAllPreference = preferenceManager.findPreference("preference_key_unblacklist_all");
    	
    	scrobblingPreference = preferenceManager.findPreference("preference_key_scrobbling");
    	
    	headphonesUnplugActionPreference = preferenceManager.findPreference("preference_key_headphones_unplug_action");
    	crossfadeTracksPreference = preferenceManager.findPreference("preference_key_crossfade_tracks");
    	crossfadeTracksDurationPreference = preferenceManager.findPreference("preference_key_crossfade_tracks_duration");
    	equalizerPreference = preferenceManager.findPreference("preference_key_equalizer_toggle");
    	
    	licensesPreference = preferenceManager.findPreference("preference_key_licenses");
    	contactUsPreference = preferenceManager.findPreference("preference_key_contact_us");
    	
    	//Set the preference icons.
    	upgradePreference.setIcon(UIElementsHelper.getIcon(mContext, "checkmark"));
    	
    	appThemePreference.setIcon(UIElementsHelper.getIcon(mContext, "color_palette"));
    	playerColorSchemePreference.setIcon(UIElementsHelper.getIcon(mContext, "color_palette_play"));
    	trackChangeAnimationPreference.setIcon(UIElementsHelper.getIcon(mContext, "customize_screens"));
    	defaultScreenPreference.setIcon(UIElementsHelper.getIcon(mContext, "default_screen"));
    	artistsLayoutPreference.setIcon(UIElementsHelper.getIcon(mContext, "mic"));
    	albumArtistsLayoutPreference.setIcon(UIElementsHelper.getIcon(mContext, "album_artists"));
    	albumsLayoutPreference.setIcon(UIElementsHelper.getIcon(mContext, "albums"));
    	lockscreenControlsPreference.setIcon(UIElementsHelper.getIcon(mContext, "lockscreen_controls"));

    	
    	addMusicLibraryPreference.setIcon(UIElementsHelper.getIcon(mContext, "add_new_library"));
    	editMusicLibraryPreference.setIcon(UIElementsHelper.getIcon(mContext, "edit_library"));
    	deleteMusicLibraryPreference.setIcon(UIElementsHelper.getIcon(mContext, "delete_library"));
    	
    	albumArtStylePreference.setIcon(UIElementsHelper.getIcon(mContext, "cover_art_icon"));
    	albumArtSourcesPreference.setIcon(UIElementsHelper.getIcon(mContext, "album_art_source"));
    	albumArtScanDownloadPreference.setIcon(UIElementsHelper.getIcon(mContext, "auto_cover_fetch"));
    	
    	defaultFolderPreference.setIcon(UIElementsHelper.getIcon(mContext, "folders_settings"));
    	
    	selectFoldersPreference.setIcon(UIElementsHelper.getIcon(mContext, "folders_settings"));
    	rescanFoldersPreference.setIcon(UIElementsHelper.getIcon(mContext, "rescan"));
    	scanFrequencyPreference.setIcon(UIElementsHelper.getIcon(mContext, "scan_frequency"));
    	
    	blacklistManagerPreference.setIcon(UIElementsHelper.getIcon(mContext, "manage_blacklists"));
    	unblacklistAllPreference.setIcon(UIElementsHelper.getIcon(mContext, "unblacklist_all"));
    	
    	scrobblingPreference.setIcon(UIElementsHelper.getIcon(mContext, "scrobbling"));
    	
    	headphonesUnplugActionPreference.setIcon(UIElementsHelper.getIcon(mContext, "headphones"));
    	crossfadeTracksPreference.setIcon(UIElementsHelper.getIcon(mContext, "crossfade_tracks"));
    	crossfadeTracksDurationPreference.setIcon(UIElementsHelper.getIcon(mContext, "crossfade_tracks_duration"));
    	equalizerPreference.setIcon(UIElementsHelper.getIcon(mContext, "equalizer_settings"));
    	
    	licensesPreference.setIcon(UIElementsHelper.getIcon(mContext, "licenses"));
    	contactUsPreference.setIcon(UIElementsHelper.getIcon(mContext, "contact_us"));
    	
    	if (sharedPreferences.getBoolean("TRIAL", true)==false) {
    		PreferenceScreen screen = getPreferenceScreen();
    		PreferenceCategory upgradePrefCategory = (PreferenceCategory) preferenceManager.findPreference("upgrade_pref_category");
    		screen.removePreference(upgradePrefCategory);
    	}
    	
    	upgradePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				//initInAppBilling();
				Intent intent = new Intent(mContext, com.aniruddhc.acemusic.player.LauncherActivity.LauncherActivity.class);
				intent.putExtra("UPGRADE", true);
				startActivity(intent);
				finish();
				return false;
			}
    		
    	});
    	
    	//Set click listeners on each preference.
    	appThemePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {

				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 0);
				startActivity(intent);
		        
				return false;
			}
    		
    	});
    	
    	playerColorSchemePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 1);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	trackChangeAnimationPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				int selectedIndex = sharedPreferences.getInt("TRACK_CHANGE_ANIMATION", 0);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.track_change_animation);
				View layoutView = mSettingsActivity.getLayoutInflater().inflate(R.layout.generic_message_listview_dialog_layout, null);
				
				TextView message = (TextView) layoutView.findViewById(R.id.generic_message);
				ListView listView = (ListView) layoutView.findViewById(R.id.generic_listview);
				
				message.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
				message.setPaintFlags(message.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
				message.setText(R.string.track_change_animation_info);
				
				String[] values = { mSettingsActivity.getResources().getString(R.string.slide_away), 
									mSettingsActivity.getResources().getString(R.string.zoom_out_and_slide_away), 
									mSettingsActivity.getResources().getString(R.string.depth_transformer) };
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(mSettingsActivity, 
																		android.R.layout.simple_list_item_single_choice, 
																		android.R.id.text1, 
																		values);
		        listView.setAdapter(adapter);
		        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		        listView.setItemChecked(selectedIndex, true);
		        listView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int which, long arg3) {
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						sharedPreferences.edit().putInt("TRACK_CHANGE_ANIMATION", which).commit();
						mTrackChangeAnimationDialog.dismiss();
						
					}
		        	
		        });
				
		        builder.setView(layoutView);
		        mTrackChangeAnimationDialog = builder.create();
				mTrackChangeAnimationDialog.show();
				
				return false;
			}
    		
    	});
    	
    	defaultScreenPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//Get the current preference.
				int currentPreference = sharedPreferences.getInt("STARTUP_SCREEN", 0);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				//builder.setTitle(R.string.startup_screen);
				builder.setSingleChoiceItems(R.array.startup_screen_items, currentPreference, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPreferences.edit().putInt("STARTUP_SCREEN", which).commit();
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	artistsLayoutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//Get the current preference.
				int currentPreference = sharedPreferences.getInt("ARTISTS_LAYOUT_PREF", 0);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.artists_layout);
				builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPreferences.edit().putInt("ARTISTS_LAYOUT_PREF", which).commit();
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	albumArtistsLayoutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//Get the current preference.
				int currentPreference = sharedPreferences.getInt("ALBUM_ARTISTS_LAYOUT_PREF", 0);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.album_artists_layout);
				builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPreferences.edit().putInt("ALBUM_ARTISTS_LAYOUT_PREF", which).commit();
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	albumsLayoutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//Get the current preference.
				int currentPreference = sharedPreferences.getInt("ALBUMS_LAYOUT_PREF", 0);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.albums_layout);
				builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPreferences.edit().putInt("ALBUMS_LAYOUT_PREF", which).commit();
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	lockscreenControlsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//Get the current preference.
				int currentPreference = sharedPreferences.getInt(Common.SHOW_LOCKSCREEN_CONTROLS, 1);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.lockscreen_controls);
				builder.setSingleChoiceItems(R.array.enable_disable, currentPreference, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPreferences.edit().putInt(Common.SHOW_LOCKSCREEN_CONTROLS, which).commit();
						dialog.dismiss();

						//Enable/disable the lockscreen controls for this session.
						try {
							if (which==0) {
								Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
								if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true) {
								//	mApp.getService().initRemoteControlClient();
									mApp.getService().updateRemoteControlClients(mApp.getService().getCurrentSong());
								}
								
							} else if (which==1) {
								if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true) {
									Toast.makeText(mContext, R.string.lockscreen_controls_disabled_next_run, Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
								}
								
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	defaultFolderPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.default_folder_for_folders_view);
				builder.setMessage(R.string.default_folder_for_folders_view_info);
				
				builder.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	/*
    	chooseGooglePlayMusicAccountPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				final AccountManager accountManager = AccountManager.get(mContext);
		        final Account[] accounts = accountManager.getAccounts();
		        final int size = accounts.length;
		        final String[] accountNames = new String[size+1];
		        accountNames[0] = "Don't use Google Play Music";
		        
		        //Create a new array with a list of all account names on the device.
		        for (int i=0; i < accounts.length; i++) {
		        	accountNames[i+1] = accounts[i].name;
		        }
		        
		        //Get the current account name (if it exists) and get the index of the account name.
		        String currentAccountName = sharedPreferences.getString("GOOGLE_PLAY_MUSIC_ACCOUNT", "");
		        int accountIndex = -1;
		        if (!currentAccountName.isEmpty()) {
		        	for (int i=0; i < accounts.length; i++) {
		        		if (currentAccountName.equals(accountNames[i+1])) {
		        			accountIndex = i+1;
		        		}
		        		
		        	}
		        } else {
		        	accountIndex = 0;
		        }
		        
				
				//Display a list of all accounts that are currently on the device.
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.select_google_account);
				builder.setSingleChoiceItems(accountNames, accountIndex, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int position) {
						if (position!=0) {
							//Start the sign-in process.
							Toast.makeText(mContext, R.string.signing_in_dot_dot_dot, Toast.LENGTH_SHORT).show();
							
							sharedPreferences.edit().putString("GOOGLE_PLAY_MUSIC_ACCOUNT", accountNames[position]).commit();
							AsyncGoogleMusicAuthenticationTask task = new AsyncGoogleMusicAuthenticationTask(mContext.getApplicationContext(), 
																											 mSettingsActivity,
																											 false,
																											 accountNames[position]);
							
							task.execute();
						} else {
							Toast.makeText(mContext, R.string.google_play_music_disabled, Toast.LENGTH_SHORT).show();
							sharedPreferences.edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false).commit();
							sharedPreferences.edit().putString("GOOGLE_PLAY_MUSIC_ACCOUNT", "").commit();
							
							//Restart the app.
							final Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
							
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							finish();
							startActivity(i);
							
						}
						
						dialog.dismiss();
					}
					
				});

				AlertDialog dialog = builder.create();
				dialog.show();
				
				return false;
			}
    		
    	});
    	*/
    /*	getPinnedSongsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				mApp.queueSongsToPin(true, false, null);
				return false;
			}
    		
    	});
    	*/
    	addMusicLibraryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 13);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	editMusicLibraryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 14);
				intent.putExtra("OPERATION", "EDIT");
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	deleteMusicLibraryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 14);
				intent.putExtra("OPERATION", "DELETE");
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	albumArtStylePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 3);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	albumArtSourcesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 4);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	albumArtScanDownloadPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
		        //Start the service that will download album art.
                Intent intent = new Intent(mContext, AutoFetchAlbumArtService.class);
                intent.putExtra("INDEX", 5);
                startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	
    	selectFoldersPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 5);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	rescanFoldersPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				//Seting the "REBUILD_LIBRARY" flag to true will force MainActivity to rescan the folders.
				sharedPreferences.edit().putBoolean("REBUILD_LIBRARY", true).commit();
				
				//Restart the app.
				final Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
				
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(i);
				
				return false;
			}
    		
    	});
    	
    	scanFrequencyPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 7);
				startActivity(intent);
				
				return false;
			}
    		
    	});
    	
    	blacklistManagerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {

/*	=			Intent intent = new Intent(mContext, PreferenceDialogLauncherActivity.class);
				intent.putExtra("INDEX", 8);
				startActivity(intent);

				/*String[] blacklistManagerChoices = { mContext.getResources().getString(R.string.manage_blacklisted_artists),
						 							 mContext.getResources().getString(R.string.manage_blacklisted_albums), 
						 							 mContext.getResources().getString(R.string.manage_blacklisted_songs), 
						 							 mContext.getResources().getString(R.string.manage_blacklisted_playlists) };
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		        //Set the dialog title.
		        builder.setTitle(R.string.blacklist_manager);
		        builder.setItems(blacklistManagerChoices, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Bundle bundle = new Bundle();
						
						if (which==0) {
							bundle.putString("MANAGER_TYPE", "ARTISTS");
						} else if (which==1) {
							bundle.putString("MANAGER_TYPE", "ALBUMS");
						} else if (which==2) {
							bundle.putString("MANAGER_TYPE", "SONGS");
						} else if (which==3) {
							bundle.putString("MANAGER_TYPE", "PLAYLISTS");
						}
						
						dialog.dismiss();
						Intent intent = new Intent(mContext, BlacklistManagerActivity.class);
						intent.putExtras(bundle);
						startActivity(intent);
						
					}
		        	
		        });
		        
		        builder.create().show();*/
				
				Intent intent = new Intent(mContext, BlacklistManagerActivity.class);
				startActivity(intent);
				
				return false;
			}

    	});
    	
    	unblacklistAllPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AsyncUnblacklistAllSongsTask task = new AsyncUnblacklistAllSongsTask();
				task.execute();
				
				return false;
			}
    		
    	});
    	
/*    	bluetoothControlsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				return false;
			}
    		
    	});
    	
    	headphonesUnplugActionPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				return false;
			}
    		
    	});*/
    	
    	scrobblingPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				int currentSelection = 0;
				if (sharedPreferences.getInt("SCROBBLING", 0)==0) {
					currentSelection = 0;
				} else if (sharedPreferences.getInt("SCROBBLING", 0)==1) {
					currentSelection = 1;
				} else if (sharedPreferences.getInt("SCROBBLING", 0)==2) {
					currentSelection = 2;
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.scrobbling);
				builder.setSingleChoiceItems(R.array.scrobbling_options, currentSelection, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						sharedPreferences.edit().putInt("SCROBBLING", which).commit();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	headphonesUnplugActionPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {

				//Get the current selection.
				int currentSelection;
				if (sharedPreferences.getString("UNPLUG_ACTION", "DO_NOTHING").equals("PAUSE_MUSIC_PLAYBACK")) {
					currentSelection = 1;
				} else {
					currentSelection = 0;
				}
				
				String[] unplugActionsArray = mContext.getResources().getStringArray(R.array.headphones_unplug_actions);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.headphones_unplug_action);
				builder.setSingleChoiceItems(unplugActionsArray, currentSelection, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						if (which==0) {
							sharedPreferences.edit().putString("UNPLUG_ACTION", "PAUSE_MUSIC_PLAYBACK").commit();
							if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true) {
								mApp.getService().unregisterReceiver(mApp.getService().getHeadsetPlugReceiver());
							}
						} else {
							sharedPreferences.edit().putString("UNPLUG_ACTION", "PAUSE_MUSIC_PLAYBACK").commit();
							if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true) {
								mApp.getService().registerHeadsetPlugReceiver();
							}
							
						}
						
					}
					
				});
				
				builder.create().show();
				
				return false;
			}
    		
    	});
    	
    	crossfadeTracksPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				int currentSelection;
				if (sharedPreferences.getBoolean(Common.CROSSFADE_ENABLED, false)==true) {
					currentSelection = 1;
				} else {
					currentSelection = 0;
				}
				
				String[] enableDisableArray = mContext.getResources().getStringArray(R.array.enable_disable);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.crossfade_tracks);
				builder.setSingleChoiceItems(enableDisableArray, currentSelection, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						if (which==0) {
							sharedPreferences.edit().putBoolean(Common.CROSSFADE_ENABLED, true).commit();
							
							//Enable crossfade for the current queue.
							if (mApp.isServiceRunning() && mApp.getService().getHandler()!=null) {
								mApp.getService().getHandler().post(mApp.getService().startCrossFadeRunnable);
							}
							
						} else {
							sharedPreferences.edit().putBoolean(Common.CROSSFADE_ENABLED, false).commit();
							
							//Disable crossfade for the current queue.
							if (mApp.isServiceRunning() && mApp.getService().getHandler()!=null) {
								mApp.getService().getHandler().removeCallbacks(mApp.getService().startCrossFadeRunnable);
								mApp.getService().getHandler().removeCallbacks(mApp.getService().crossFadeRunnable);
							}
							
						}
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	crossfadeTracksDurationPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mSettingsActivity);
				builder.setTitle(R.string.crossfade_duration);
				
				RelativeLayout dialogView = (RelativeLayout) mSettingsActivity.getLayoutInflater().inflate(R.layout.dialog_crossfade_duration, null);
				final TextView durationText = (TextView) dialogView.findViewById(R.id.crossfade_duration_text);
				final SeekBar durationSeekBar = (SeekBar) dialogView.findViewById(R.id.crossfade_duration_seekbar);
				
				int currentSeekBarDuration = sharedPreferences.getInt(Common.CROSSFADE_DURATION, 5);
				durationSeekBar.setMax(14);
				durationSeekBar.setProgress(currentSeekBarDuration);
				durationText.setText(currentSeekBarDuration + " secs");
				
				durationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						sharedPreferences.edit().putInt(Common.CROSSFADE_DURATION, (progress+1)).commit();
						durationText.setText((progress + 1) + " secs");
						
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
				});
				
				builder.setView(dialogView);
				builder.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
				builder.create().show();			
				return false;
			}
    		
    	});
    	
    	equalizerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				boolean currentSetting = sharedPreferences.getBoolean("EQUALIZER_ENABLED", true);
				int intCurrentSetting = -1;
				if (currentSetting==true)
					intCurrentSetting = 0;
				else
					intCurrentSetting = 1;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mSettingsActivity);
				builder.setTitle(R.string.equalizer);
				builder.setSingleChoiceItems(R.array.enable_disable, intCurrentSetting, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						boolean isEnabled;
						if (which==0)
							isEnabled = true;
						else
							isEnabled = false;
						
						sharedPreferences.edit().putBoolean("EQUALIZER_ENABLED", isEnabled).commit();
						Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						
						if (Build.PRODUCT.contains("HTC") && which==0) {
							showHTCEqualizerIssueDialog();
						}
						
						if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true && which==0) {
							mApp.getService().initAudioFX();
						} else if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true && which==1) {
							try {
								mApp.getService().getEqualizerHelper().releaseEQObjects();
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
						
						dialog.dismiss();
					}

					private void showHTCEqualizerIssueDialog() {
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle(R.string.htc_devices);
						builder.setMessage(R.string.htc_devices_equalizer_issue);
						builder.setPositiveButton(R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
							}
							
						});
						
						builder.create().show();
						
					}
					
				});
				
				builder.create().show();
				return false;
			}
    		
    	});
    	
    	licensesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {

				Intent intent = new Intent(mContext, SettingsAboutFragment.class);
				intent.putExtra("INDEX", 12);
				startActivity(intent);

				return false;
			}
    		
    	});
    	
    	contactUsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","aniruddh.chandratre@gmail.com", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ACE Music Player Support");
				startActivity(Intent.createChooser(emailIntent, "Send email"));
				
				return false;
			}
    		
    	});
    	
	}





	/*private void initInAppBilling() {
		
		String base64EncodedPublicKey = "";
		base64EncodedPublicKey = Common.uid4 + 
								 Common.uid2 +
								 Common.uid6 +
								 Common.uid1 +
								 Common.uid3 +
								 Common.uid5;

    	mHelper = new IabHelper(this, base64EncodedPublicKey);
    	mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
    		
    		@Override
    		public void onIabSetupFinished(IabResult result) {
    			if (!result.isSuccess()) {
    				//In-app billing could not be initialized.
    				Toast.makeText(mSettingsActivity, R.string.unable_to_reach_google_play, Toast.LENGTH_LONG).show();
    			} else {             
    				//In-app billing was initialized successfully.
    				try {
    					mHelper.launchPurchaseFlow(mSettingsActivity, ITEM_SKU, 10001, mPurchaseFinishedListener, "");
    				} catch (Exception e) {
    					e.printStackTrace();
    					Toast.makeText(mContext, R.string.unable_to_reach_google_play, Toast.LENGTH_LONG).show();
    					finish();
    				}
    			}
    			
    		}
    		
    	});
    	
	}*/
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			
			if (result.isFailure()) {
				Toast.makeText(mContext, R.string.unable_to_purchase, Toast.LENGTH_LONG).show();
				sharedPreferences.edit().putBoolean("TRIAL", true).commit();
				return;
			} else if (purchase.getSku().equals(ITEM_SKU)) {

				Toast.makeText(mContext, R.string.jams_trial_time_removed, Toast.LENGTH_LONG).show();
				mApp.getSharedPreferences().edit().putBoolean("TRIAL", false).commit();
				PreferenceCategory upgradePrefCategory = (PreferenceCategory) preferenceManager.findPreference("upgrade_pref_category");
	    		upgradePrefCategory.removeAll();
	    		
			}
	      
		}
	   
	};

	class AsyncUnblacklistAllSongsTask extends AsyncTask<String, Void, Void> {

		private ProgressDialog pd;
		
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			
			pd = new ProgressDialog(mSettingsActivity);
			pd.setTitle(R.string.reset_blacklist);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setMessage(getResources().getString(R.string.resetting_blacklist));
			pd.show();
			
		}
		
		@Override
		protected Void doInBackground(String... params) {
			return null;
		}
		
		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			pd.dismiss();
			Toast.makeText(mContext, R.string.blacklist_reset, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
}
