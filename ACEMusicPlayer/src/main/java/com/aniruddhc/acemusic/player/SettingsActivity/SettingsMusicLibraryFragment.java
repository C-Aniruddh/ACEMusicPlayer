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

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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

import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.WelcomeActivity.WelcomeActivity;

/**
 * @author Saravan Pantham
 */
public class SettingsMusicLibraryFragment extends PreferenceFragment {

    private Context mContext;
    private Common mApp;

    private View mRootView;
    private ListView mListView;

    private Preference mSelectMusicFoldersPreference;
    private Preference mRefreshMusicFoldersPreference;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_music_library);

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

        mSelectMusicFoldersPreference = getPreferenceManager().findPreference("preference_key_music_folders");
        mRefreshMusicFoldersPreference = getPreferenceManager().findPreference("preference_key_refresh_music_library");

        mSelectMusicFoldersPreference.setOnPreferenceClickListener(selectMusicFoldersClickListener);
        mRefreshMusicFoldersPreference.setOnPreferenceClickListener(refreshMusicFoldersClickListener);

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
     * Click listener for "Select Music Folders".
     */
    private Preference.OnPreferenceClickListener selectMusicFoldersClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            SettingsMusicFoldersDialog foldersDialog = new SettingsMusicFoldersDialog();
            foldersDialog.setArguments(bundle);
            foldersDialog.show(ft, "foldersDialog");

            return false;
        }

    };

    /**
     * Click listener for "Refresh Music Folders".
     */
    private Preference.OnPreferenceClickListener refreshMusicFoldersClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            getActivity().finish();

            Intent intent = new Intent(mContext, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("REFRESH_MUSIC_LIBRARY", true);
            mContext.startActivity(intent);

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
