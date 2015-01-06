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
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.Dialogs.ApplicationThemeDialog;
import com.aniruddhc.acemusic.player.Dialogs.NowPlayingColorSchemesDialog;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * @author Saravan Pantham
 */
public class SettingsAboutFragment extends PreferenceFragment {

    private Context mContext;
    private Common mApp;

    private View mRootView;
    private ListView mListView;

    private Preference mAboutAppPreference;
    private Preference mBasePreference;
    private Preference mThanksPreference;
    private Preference mDevPreference;
    private Preference mLicensePreference;
    private Preference mNoticePreference;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_about);

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

        mAboutAppPreference = getPreferenceManager().findPreference("preference_key_about_app");
        mBasePreference = getPreferenceManager().findPreference("preference_key_player_base_license");
        mThanksPreference = getPreferenceManager().findPreference("preference_key_special_thanks");
        mDevPreference = getPreferenceManager().findPreference("preference_key_about_dev");
        mLicensePreference = getPreferenceManager().findPreference("preference_key_license");
        mNoticePreference = getPreferenceManager().findPreference("preference_key_license_short");

        //Apply the click listeners.
        mAboutAppPreference.setOnPreferenceClickListener(appAboutClickListener);
        mBasePreference.setOnPreferenceClickListener(BaseClickListener);
        mThanksPreference.setOnPreferenceClickListener(ThanksScreenClickListener);
        mDevPreference.setOnPreferenceClickListener(DevClickListener);
        mLicensePreference.setOnPreferenceClickListener(LicenseClickListener);
        mNoticePreference.setOnPreferenceClickListener(NoticeClickListener);

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
     * Click listener for the app theme preference.
     */
    private Preference.OnPreferenceClickListener appAboutClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            Uri webpage = Uri.parse("market://details?id=com.aniruddhc.acemusic.player");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            {
                startActivity(intent);
                return false;
            }
        }

    };

    /**
     * Click listener for the color preference.
     */
    private Preference.OnPreferenceClickListener DevClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Uri webpage = Uri.parse("http://c-aniruddh.us.to");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            {
                startActivity(intent);
                return false;
            }
        }
    };
    /**
     * Click listener for the color preference.
     */
    private Preference.OnPreferenceClickListener BaseClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:c.aniruddh98@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ACEMusic Support");

            startActivity(Intent.createChooser(emailIntent, "ACEMusic Support"));

            return false;
        }

    };
    private Preference.OnPreferenceClickListener LicenseClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Uri webpage = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            {
                startActivity(intent);
                return false;
            }

        }

    };
    /**
     * Click listener for the default browser preference.
     */
    private Preference.OnPreferenceClickListener ThanksScreenClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            Uri webpage = Uri.parse("https://github.com/psaravan/JamsMusicPlayer");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            {
                startActivity(intent);
                return false;
            }

        }

    };

    private Preference.OnPreferenceClickListener NoticeClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            Uri webpage = Uri.parse("https://raw.githubusercontent.com/C-Aniruddh/ACE/master/LICENSE.md");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            {
                startActivity(intent);
                return false;
            }

        }

    };


    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT)
            getActivity().getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

    }

}
