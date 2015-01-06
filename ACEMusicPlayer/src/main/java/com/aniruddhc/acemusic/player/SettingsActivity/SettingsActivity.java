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

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import java.util.List;

/**
 *
 *
 * @author Saravan Pantham
 */
public class SettingsActivity extends PreferenceActivity {

    private Context mContext;
    private SettingsActivity mActivity;
    private Common mApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this.getApplicationContext();
        mActivity = this;
        mApp = (Common) mContext;

        setTheme(R.style.SettingsThemeLight);
        super.onCreate(savedInstanceState);

        //Set the ActionBar background and text color.
        getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        getActionBar().setTitle(R.string.settings);
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarText = (TextView) findViewById(titleId);
        actionBarText.setTextColor(0xFFFFFFFF);

    }

    /**
     * Applies KitKat specific translucency.
     */
    public void applyKitKatTranslucency() {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {

            //Calculate ActionBar and navigation bar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

            ((View) this.getListView().getParent()).setPadding(0, actionBarHeight + mApp.getStatusBarHeight(mContext),
                                                               0, 0);

            this.getListView().setBackgroundColor(0xFFEEEEEE);
            this.getListView().setPadding(0, 0, 0, mApp.getNavigationBarHeight(mContext));
            this.getListView().setClipToPadding(false);

            //Set the window color.
            getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

        }

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
        applyKitKatTranslucency();

    }

    @Override
    protected boolean isValidFragment (String fragmentName) {
       return true;

    }

    @Override
    public void onResume() {
        super.onResume();
        applyKitKatTranslucency();
        getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

    }

}
