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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * @author Saravan Pantham
 */
public class SettingsLayoutsFragment extends PreferenceFragment {

    private Context mContext;
    private Common mApp;

    private View mRootView;
    private ListView mListView;

    private Preference mArtistsPreference;
    private Preference mAlbumArtistsPreference;
    private Preference mAlbumsPreference;
    private Preference mGenresPreference;
    private Preference mPlaylistsPreference;
    private Preference mFoldersPreference;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_layouts);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceState) {
        mRootView = super.onCreateView(inflater, container, onSavedInstanceState);

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mListView = (ListView) mRootView.findViewById(android.R.id.list);

        //Set the ActionBar background and text color.
        applyKitKatTranslucency();
        getActivity().getActionBar().setTitle(R.string.settings);
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarText = (TextView) getActivity().findViewById(titleId);
        actionBarText.setTextColor(0xFFFFFFFF);

        mArtistsPreference = getPreferenceManager().findPreference("preference_key_artists_layout");
        mAlbumArtistsPreference = getPreferenceManager().findPreference("preference_key_album_artists_layout");
        mAlbumsPreference = getPreferenceManager().findPreference("preference_key_albums_layout");
        mPlaylistsPreference = getPreferenceManager().findPreference("preference_key_playlists_layout");
        mGenresPreference = getPreferenceManager().findPreference("preference_key_genres_layout");
        mFoldersPreference = getPreferenceManager().findPreference("preference_key_folders_layout");

        //Apply the click listeners.
        mArtistsPreference.setOnPreferenceClickListener(artistsLayoutClickListener);
        mAlbumArtistsPreference.setOnPreferenceClickListener(albumArtistsClickListener);
        mAlbumsPreference.setOnPreferenceClickListener(albumsLayoutClickListener);
        mPlaylistsPreference.setOnPreferenceClickListener(playlistsClickListener);
        mGenresPreference.setOnPreferenceClickListener(genresClickListener);
        mFoldersPreference.setOnPreferenceClickListener(foldersClickListener);

        return mRootView;
    }

    /**
     * Applies KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {

            //Calculate ActionBar and navigation bar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

            mListView.setBackgroundColor(0xFFEEEEEE);
            mRootView.setPadding(0, actionBarHeight + mApp.getStatusBarHeight(mContext),
                    0, 0);
            mListView.setPadding(10, 0, 10, mApp.getNavigationBarHeight(mContext));
            mListView.setClipToPadding(false);

            //Set the window color.
            getActivity().getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

        }

    }

    /**
     * Click listener for Artists Layout.
     */
    private Preference.OnPreferenceClickListener artistsLayoutClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.ARTISTS_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.artists_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.ARTISTS_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for Albums Layout.
     */
    private Preference.OnPreferenceClickListener albumsLayoutClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.ALBUMS_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.albums_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.ALBUMS_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for Album Artists layout.
     */
    private Preference.OnPreferenceClickListener albumArtistsClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.ALBUM_ARTISTS_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.album_artists_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.ALBUM_ARTISTS_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for Playlists layout.
     */
    private Preference.OnPreferenceClickListener playlistsClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            
            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.PLAYLISTS_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.playlists_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.PLAYLISTS_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for Genres layout.
     */
    private Preference.OnPreferenceClickListener genresClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            
            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.GENRES_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.genres_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.GENRES_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for Folders layout.
     */
    private Preference.OnPreferenceClickListener foldersClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            
            //Get the current preference.
            int currentPreference = mApp.getSharedPreferences().getInt(Common.FOLDERS_LAYOUT, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.folders_layout);
            builder.setSingleChoiceItems(R.array.layout_preference_items, currentPreference, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getSharedPreferences().edit().putInt(Common.FOLDERS_LAYOUT, which).commit();
                    dialog.dismiss();
                    Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();

                }

            });

            builder.create().show();
            return false;
        }

    };

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT)
            getActivity().getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

    }

}
