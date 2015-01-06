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
package com.aniruddhc.acemusic.player.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * BroadcastReceiver that handles and processes all headset 
 * button clicks/events.
 * 
 * @author Saravan Pantham
 */
public class HeadsetButtonsReceiver extends BroadcastReceiver {
	
	private Common mApp;
	
    public HeadsetButtonsReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    			
    	mApp = (Common) context.getApplicationContext();
    	
    	//There's no point in going any further if the service isn't running.
    	if (mApp.isServiceRunning()) {+
    			
			//Aaaaand there's no point in continuing if the intent doesn't contain info about headset control inputs.
			String intentAction = intent.getAction();
			if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
				return;
			}
			
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			int keycode = event.getKeyCode();
			int action = event.getAction();
			
			//Switch through each event and perform the appropriate action based on the intent that's ben
			if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
				
				if (action == KeyEvent.ACTION_DOWN) {
					//Toggle play/pause.
					Intent playPauseIntent = new Intent();
					playPauseIntent.setAction(AudioPlaybackService.PLAY_PAUSE_ACTION);
					context.sendBroadcast(playPauseIntent);
					
				}
				
			}
			
			if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
				
				if (action == KeyEvent.ACTION_DOWN) {
					//Fire a broadcast that skips to the next track.
					Intent nextIntent = new Intent();
					nextIntent.setAction("com.aniruddhc.acemusic.player.NEXT_ACTION");
					context.sendBroadcast(nextIntent);
					
				}
				
			}
			
			if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
				
				if (action == KeyEvent.ACTION_DOWN) {
					//Fire a broadcast that goes back to the previous track.
					Intent previousIntent = new Intent();
					previousIntent.setAction("com.aniruddhc.acemusic.player.PREVIOUS_ACTION");
					context.sendBroadcast(previousIntent);
					
				}
				
			}
	        
    	}
    	
    }
    
}
