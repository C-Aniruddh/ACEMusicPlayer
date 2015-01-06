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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncPinSongsTask;

public class PinGMusicSongsService extends Service {
	
	private Context mContext;
	public static Notification notification;
	
	public static NotificationManager mNotifyManager;
    public static NotificationCompat.Builder mBuilder;
    public static int notificationID = 233235;
	
	//Prepare the media player for first use.
	@Override
	public void onCreate() {
		
        mContext = this;
		super.onCreate();
		
	}
	
	//This method is called when the service is created.
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

		//The initial notification will display a "Starting download" message + indeterminate progress bar.
    	mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    	mBuilder = new NotificationCompat.Builder(mContext);
    	mBuilder.setContentTitle(mContext.getResources().getString(R.string.starting_download));
    	mBuilder.setTicker(mContext.getResources().getString(R.string.starting_download));
    	mBuilder.setSmallIcon(R.drawable.pin_light);
    	mBuilder.setProgress(0, 0, true);
    	mNotifyManager.notify(notificationID, mBuilder.build());
        
        //Call the AsyncTask that kicks off the pinning process.
        AsyncPinSongsTask task = new AsyncPinSongsTask(mContext);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String[]) null);
        
        return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

}
