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
package com.aniruddhc.acemusic.player.AsyncTasks;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Utils.GaussianBlur;

public class AsyncUpdateBlurredWidgetTask extends AsyncTask<String, Integer, Boolean> {
	
    private Context mContext;
    private Common mApp;
    private int mNumWidgets;
    private int mAppWidgetIds[];
    private AppWidgetManager mAppWidgetManager;
    
    private int currentAppWidgetId;
    private RemoteViews views;
    
	private SharedPreferences sharedPreferences;
    
	public static final String PREVIOUS_ACTION = "com.aniruddhc.acemusic.player.PREVIOUS_ACTION";
	public static final String PLAY_PAUSE_ACTION = "com.aniruddhc.acemusic.player.PLAY_PAUSE_ACTION";
	public static final String NEXT_ACTION = "com.aniruddhc.acemusic.player.NEXT_ACTION";
    
    public AsyncUpdateBlurredWidgetTask(Context context, int numWidgets, int appWidgetIds[], AppWidgetManager appWidgetManager) {
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	mAppWidgetIds = appWidgetIds;
    	mAppWidgetManager = appWidgetManager;
    	mNumWidgets = numWidgets;
    }
 
    @SuppressLint("NewApi")
	@Override
    protected Boolean doInBackground(String... params) {

    	//Perform this loop procedure for each App Widget that belongs to this mApp
        for (int i=0; i < mNumWidgets; i++) {
            currentAppWidgetId = mAppWidgetIds[i];
            
            Intent playPauseIntent = new Intent();
        	playPauseIntent.setAction(PLAY_PAUSE_ACTION);
        	PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, playPauseIntent, 0);
        	
        	Intent nextIntent = new Intent();
        	nextIntent.setAction(NEXT_ACTION);
        	PendingIntent nextPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, nextIntent, 0);
        	
        	Intent previousIntent = new Intent();
        	previousIntent.setAction(PREVIOUS_ACTION);
        	PendingIntent previousPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, previousIntent, 0);

            //Get the layout of the widget and attach a click listener to each element.
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.blurred_widget_layout);
            views.setOnClickPendingIntent(R.id.widget_play, playPausePendingIntent);
            views.setOnClickPendingIntent(R.id.widget_previous_track, previousPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_next_track, nextPendingIntent);
            
            //Get the downsampled image of the current song's album art.
            views.setImageViewBitmap(R.id.widget_album_art, getAlbumArt());
            views.setTextViewText(R.id.widget_song_title_text, mApp.getService().getCurrentSong().getTitle());
            views.setTextViewText(R.id.widget_artist_album_text, mApp.getService().getCurrentSong().getAlbum() 
            												   + mApp.getService().getCurrentSong().getArtist());

            if (mApp.isServiceRunning()) {
            	
            	try {
                	if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
                		views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_pause_light);
                	} else {
                		views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_play_light);
                	}
            	} catch (Exception e) {
            		// TODO Auto-generated method stub
            		e.printStackTrace();
            	}
            	
            }
            
            //Tapping the album art should open up the app's NowPlayingActivity.
            if (mApp.isServiceRunning()) {
            	final Intent notificationIntent = new Intent(mContext, NowPlayingActivity.class);
                notificationIntent.putExtra("CALLED_FROM_FOOTER", true);
                notificationIntent.putExtra("CALLED_FROM_NOTIF", true);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                views.setOnClickPendingIntent(R.id.app_widget_small_image, pendingIntent);
                
            }

            //Tell the AppWidgetManager to perform an update on the current app widget\
            try {
            	mAppWidgetManager.updateAppWidget(currentAppWidgetId, views);
            } catch (Exception e) {
            	continue;
            }
            
        }
    	
    	return true;
    }
    
    private Bitmap getAlbumArt() {
    	mApp = (Common) mContext.getApplicationContext();
    	
        //Check if the album art has been cached for this song.
        File albumArtFile = new File(mContext.getExternalCacheDir() + "/current_album_art.jpg");
        Bitmap bm = null;
        if (albumArtFile.exists() && mApp.isServiceRunning()) {
        	//Decode a subsampled version of the cached album art.
        	bm = mApp.decodeSampledBitmapFromFile(albumArtFile, 150, 150);
        } else if (!albumArtFile.exists() && mApp.isServiceRunning()) {
        	//Decode a subsampled version of the default album art.
            bm = mApp.decodeSampledBitmapFromResource(R.drawable.transparent_drawable, 450, 450);
        } else {
        	return null;
        }
        
        return GaussianBlur.fastblur(mContext, bm, 5);
    }

    @Override
    public void onProgressUpdate(Integer... values) {
    	super.onProgressUpdate(values);
    	switch(values[0]) {
    	case 0:
    		try {
    			mAppWidgetManager.updateAppWidget(currentAppWidgetId, views);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    		break;
    	}
    	
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
        
    }
    
}
