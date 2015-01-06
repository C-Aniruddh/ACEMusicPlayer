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
package com.aniruddhc.acemusic.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;

public class NowPlayingColorSchemesDialog extends DialogFragment {

	private Activity parentActivity;
	private int selectedThemeIndex;
	private DialogFragment dialogFragment;
	
	private static final String NOW_PLAYING_COLOR = "NOW_PLAYING_COLOR";
	private static final String BLUE = "BLUE";
	private static final String RED = "RED";
	private static final String GREEN = "GREEN";
	private static final String ORANGE = "ORANGE";
	private static final String PURPLE = "PURPLE";
	private static final String MAGENTA = "MAGENTA";
	private static final String GRAY = "GRAY";
	private static final String WHITE = "WHITE";
	private static final String BLACK = "BLACK";
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        parentActivity = getActivity();
        dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("colorSchemesDialog");

        final SharedPreferences sharedPreferences = parentActivity.
                getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Check which theme is currently selected and set the appropriate flag.
        if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(WHITE)) {
            selectedThemeIndex = 0;
        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(GRAY)) {
            selectedThemeIndex = 1;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
            selectedThemeIndex = 2;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
            selectedThemeIndex = 3;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
            selectedThemeIndex = 4;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
            selectedThemeIndex = 5;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
            selectedThemeIndex = 6;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
            selectedThemeIndex = 7;

        } else if (sharedPreferences.getString(NOW_PLAYING_COLOR, BLUE).equals(BLACK)) {
            selectedThemeIndex = 8;

        } else {
            selectedThemeIndex = 0;
        }

        //Set the dialog title.
        builder.setTitle(R.string.now_playing_color_scheme);
        builder.setSingleChoiceItems(R.array.now_playing_color_schemes, selectedThemeIndex, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, WHITE).commit();
                    dialog.dismiss();
                } else if (which == 1) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, GRAY).commit();
                    dialog.dismiss();
                } else if (which == 2) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, BLUE).commit();
                    dialog.dismiss();
                } else if (which == 3) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, RED).commit();
                    dialog.dismiss();
                } else if (which == 4) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, GREEN).commit();
                    dialog.dismiss();
                } else if (which == 5) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, ORANGE).commit();
                    dialog.dismiss();
                } else if (which == 6) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, PURPLE).commit();
                    dialog.dismiss();
                } else if (which == 7) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, MAGENTA).commit();
                    dialog.dismiss();
                } else if (which == 8) {
                    sharedPreferences.edit().putString(NOW_PLAYING_COLOR, BLACK).commit();
                    dialog.dismiss();
                }

                getActivity().getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(getActivity()));
                getActivity().getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(getActivity()));

            }

        });

        return builder.create();
    }
	
}
