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

/*****************************************************
 * Contains public methods that connect with Scrobble 
 * Droid.
 * 
 * @author Saravan Pantham
 *****************************************************/
public class ScrobbleDroidHelper {

	//Action Intent.
	public static Intent mScrobbleDroidIntent;
	
	/**
	 * Initializes the action intent that will be sent to 
	 * Scrobble Droid. This method should always 
	 * be called when sending a new set of data to the 
	 * scrobbling app.
	 */
	public static void initializeActionIntent() {
		mScrobbleDroidIntent = null;
		mScrobbleDroidIntent = new Intent("net.jjc1138.android.scrobbler.action.MUSIC_STATUS");
	}
	
	/**
	 * Attaches the song's metadata to the intent that was initialized in 
	 * <i>initializeActionIntent()</i>.
	 */
	public static void attachMetadata(boolean playing,
									  String artist,
									  String album,
									  String track,
									  int durationInSecs) {
		
		mScrobbleDroidIntent.putExtra("playing", playing);
		mScrobbleDroidIntent.putExtra("artist", artist);
		mScrobbleDroidIntent.putExtra("album", album);
		mScrobbleDroidIntent.putExtra("track", track);
		mScrobbleDroidIntent.putExtra("secs", durationInSecs);
		
	}
	
	/**
	 * Fires the broadcast intent that connects to Scrobble Droid.
	 */
	public static void sendBroadcast(Context mContext) {
		mContext.sendBroadcast(mScrobbleDroidIntent);
	}
	
}
