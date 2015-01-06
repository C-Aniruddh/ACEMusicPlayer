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
package com.aniruddhc.acemusic.player.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class DashClockExtensionService extends DashClockExtension {
	
	private Common mApp;
	private BroadcastReceiver receiver;
	
	private String status;
	private String expandedTitle;
	private String expandedBody;

	@Override
	public void onCreate() {
		super.onCreate();
		
    	mApp = (Common) this.getApplicationContext();
		
		//Register a broadcast listener to listen for track updates.
        receiver = new BroadcastReceiver() {
        	
            @Override
            public void onReceive(Context context, Intent intent) {
                updateExtensionData();
                
            }
            
        };
        
    	LocalBroadcastManager.getInstance(this)
		 					 .registerReceiver((receiver), 
		 							 			new IntentFilter("com.aniruddhc.acemusic.player.NEW_SONG_UPDATE_UI"));
		
	}
	
    @Override
    protected void onUpdateData(int reason) {
        //Nope.
    }
    
    private void updateExtensionData() {
    	ExtensionData data = new ExtensionData();
    	
        //Publish the extension data update.
    	if (mApp.isServiceRunning()) {
    		//Show the extension with updated data.
    		try {
    			
    			status = "Playing";
    			expandedTitle = mApp.getService().getCurrentSong().getTitle();
    			expandedBody = mApp.getService().getCurrentSong().getAlbum() 
    						 + " - " 
    						 + mApp.getService().getCurrentSong().getArtist(); 
    			
    			Intent notificationIntent = new Intent(this, NowPlayingActivity.class);
    	        notificationIntent.putExtra("CALLED_FROM_FOOTER", true);
    	        notificationIntent.putExtra("CALLED_FROM_NOTIF", true);
    			
    			//Publish the extension data update.
    	        publishUpdate(data.visible(true)
        						  .icon(R.drawable.dashclock_icon)
        						  .status(status)
        						  .expandedTitle(expandedTitle)
        						  .expandedBody(expandedBody)
        						  .clickIntent(notificationIntent));
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    			//Hide the extension.
        		publishUpdate(data.visible(false));
    		}
    	} else {
    		//Hide the extension.
    		publishUpdate(data.visible(false));
    	}
    	 
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    	
    }
    
}
