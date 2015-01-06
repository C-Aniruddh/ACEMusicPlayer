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
package com.aniruddhc.acemusic.player.Scrobbling;

import android.content.Context;
import android.content.Intent;

/***************************************************
 * Contains public methods that connect with Simple 
 * lastFM Scrobbler.
 * 
 * @author Saravan Pantham
 ***************************************************/
public class SimpleLastFMHelper {

	//Action intent.
	public static Intent mSimpleLastFMIntent;
	
	//Play state constants
	public static int START = 0;
	public static int RESUME = 1;
	public static int PAUSE = 2;
	public static int COMPLETE = 3;
	
	/**
	 * Initializes the action intent that will be sent to 
	 * Simple lastFM Scrobbler. This method should always 
	 * be called when sending a new set of data to the 
	 * scrobbling app.
	 */
	public static void initializeActionIntent() {
		mSimpleLastFMIntent = null;
		mSimpleLastFMIntent = new Intent("com.adam.aslfms.notify.playstatechanged");
	}
	
	/**
	 * Attaches the song's metadata to the intent that was initialized in 
	 * <i>initializeActionIntent()</i>.
	 */
	public static void attachMetadata(int state,
													String artist,
													String album,
													String track,
													int durationInSecs) {
		mSimpleLastFMIntent.putExtra("state", state);
		mSimpleLastFMIntent.putExtra("app-name", "ACE Music Player");
		mSimpleLastFMIntent.putExtra("app-package", "com.aniruddhc.acemusic.player");
		mSimpleLastFMIntent.putExtra("artist", artist);
		mSimpleLastFMIntent.putExtra("album", album);
		mSimpleLastFMIntent.putExtra("track", track);
		mSimpleLastFMIntent.putExtra("duration", durationInSecs);
		
	}
	
	/**
	 * Fires the broadcast intent that connects to Simple lastFM Scrobbler.
	 */
	public static void sendBroadcast(Context mContext) {
		mContext.sendBroadcast(mSimpleLastFMIntent);
	}
	
}
