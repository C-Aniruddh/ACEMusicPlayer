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
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncBuildLibraryTask;
import com.aniruddhc.acemusic.player.WelcomeActivity.WelcomeActivity;

public class BuildMusicLibraryService extends Service implements AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate {
	
	private Context mContext;
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;
	private NotificationManager mNotifyManager;
	public static int mNotificationId = 92713;
	
	@Override
	public void onCreate() {
		mContext = this.getApplicationContext();
	}
	
	@Override
	public int onStartCommand(Intent intent, int startId, int flags) {
		
		//Create a persistent notification that keeps this service running and displays the scan progress.
		mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setSmallIcon(R.drawable.notif_icon);
		mBuilder.setContentTitle(getResources().getString(R.string.building_music_library));
		mBuilder.setTicker(getResources().getString(R.string.building_music_library));
		mBuilder.setContentText("");
		mBuilder.setProgress(0, 0, true);
		
		mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = mBuilder.build();
		mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
		
		startForeground(mNotificationId, mNotification);	

        //Go crazy with a full-on scan.
        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext, this);
        task.setOnBuildLibraryProgressUpdate(WelcomeActivity.mBuildingLibraryProgressFragment);
        task.setOnBuildLibraryProgressUpdate(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void onStartBuildingLibrary() {

    }

    @Override
    public void onProgressUpdate(AsyncBuildLibraryTask task, String mCurrentTask, int overallProgress,
                                 int maxProgress, boolean mediaStoreTransferDone) {
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.notif_icon);
        mBuilder.setContentTitle(mCurrentTask);
        mBuilder.setTicker(mCurrentTask);
        mBuilder.setContentText("");
        mBuilder.setProgress(maxProgress, overallProgress, false);

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(mNotificationId, mNotification);

    }

    @Override
    public void onFinishBuildingLibrary(AsyncBuildLibraryTask task) {
        mNotifyManager.cancel(mNotificationId);
        stopSelf();

        Toast.makeText(mContext, R.string.finished_scanning_album_art, Toast.LENGTH_LONG).show();

    }

}
