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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;

import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.R;

/**
 * @author Saravan Pantham
 */
public class SettingsAudioFragment extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Thread timer = new Thread() {
            public void run() {
                try {
                    // sleep(R.integer.SplashActivityTime);
                    sleep(1000);
                } catch (InterruptedException iEx) {
                    iEx.printStackTrace();
                } finally {
                    Intent mainActivity = new Intent(SettingsAudioFragment.this,
                            EqualizerActivity.class);

                    startActivity(mainActivity);
                    finish();
                }
            }
        };
        timer.start();
    }
}
