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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.LauncherActivity.LauncherActivity;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncUpdateSmallWidgetTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private Common mApp;
    private int mNumWidgets;
    private int mAppWidgetIds[];
    private AppWidgetManager mAppWidgetManager;
    
    private int currentAppWidgetId;
    private RemoteViews views;
    
	public static final String PREVIOUS_ACTION = "com.aniruddhc.acemusic.player.PREVIOUS_ACTION";
	public static final String PLAY_PAUSE_ACTION = "com.aniruddhc.acemusic.player.PLAY_PAUSE_ACTION";
	public static final String NEXT_ACTION = "com.aniruddhc.acemusic.player.NEXT_ACTION";
    
    public AsyncUpdateSmallWidgetTask(Context context, int numWidgets, int appWidgetIds[], AppWidgetManager appWidgetManager) {
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
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
            String widgetColor = mApp.getSharedPreferences().getString("" + currentAppWidgetId, "DARK");
            views = new RemoteViews(mContext.getPackageName(), R.layout.small_widget_layout);
            
            if (widgetColor.equals("DARK")) {
            	views.setInt(R.id.small_widget_parent_layout, "setBackgroundResource", R.drawable.appwidget_dark_bg);
            	views.setImageViewResource(R.id.app_widget_small_previous, R.drawable.btn_playback_previous_light);
            	views.setImageViewResource(R.id.app_widget_small_next, R.drawable.btn_playback_next_light);
            } else if (widgetColor.equals("LIGHT")) {
            	views.setInt(R.id.small_widget_parent_layout, "setBackgroundResource", R.drawable.appwidget_bg);
            	views.setImageViewResource(R.id.app_widget_small_previous, R.drawable.btn_playback_previous);
            	views.setImageViewResource(R.id.app_widget_small_next, R.drawable.btn_playback_next);
            }
            
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
            views.setOnClickPendingIntent(R.id.app_widget_small_play, playPausePendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_small_previous, previousPendingIntent);
            views.setOnClickPendingIntent(R.id.app_widget_small_next, nextPendingIntent);
            
            //Get the downsampled image of the current song's album art.
            views.setImageViewBitmap(R.id.app_widget_small_image, getAlbumArt());
            
            if (mApp.isServiceRunning()) {
            	
            	final Intent notificationIntent = new Intent(mContext, NowPlayingActivity.class);
                notificationIntent.putExtra("CALLED_FROM_FOOTER", true);
                notificationIntent.putExtra("CALLED_FROM_NOTIF", true);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                views.setOnClickPendingIntent(R.id.app_widget_small_image, pendingIntent);
            	
            } else {
            	views.setImageViewResource(R.id.app_widget_small_image, R.drawable.default_album_art);
            	
            	if (widgetColor.equals("DARK")) {
        			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play_light);
        		} else if (widgetColor.equals("LIGHT")) {
        			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play);
        		}
            	
            	final Intent intent = new Intent(mContext, LauncherActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.app_widget_small_image, pendingIntent);
            	
            }
            
            views.setTextViewText(R.id.app_widget_small_line_one, mApp.getService().getCurrentSong().getTitle());
            views.setTextViewText(R.id.app_widget_small_line_two, mApp.getService().getCurrentSong().getAlbum() + 
            													  mApp.getService().getCurrentSong().getArtist());

            if (widgetColor.equals("LIGHT")) {
    			views.setTextColor(R.id.app_widget_small_line_one, Color.BLACK);
    			views.setTextColor(R.id.app_widget_small_line_two, Color.BLACK);
    		}
            
            if (mApp.isServiceRunning()) {
            	
            	try {
            		if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
                		if (widgetColor.equals("DARK")) {
                			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_pause_light);
                		} else if (widgetColor.equals("LIGHT")) {
                			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_pause);
                		}
                		
                	} else {
                		if (widgetColor.equals("DARK")) {
                			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play_light);
                		} else if (widgetColor.equals("LIGHT")) {
                			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play);
                		}
                		
                	}
            	} catch (Exception e) {
            		// TODO Auto-generated method stub
            		e.printStackTrace();
            	}
            	
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
        if (albumArtFile.exists()) {
        	//Decode a subsampled version of the cached album art.
        	bm = mApp.decodeSampledBitmapFromFile(albumArtFile, 150, 150);
        } else {
        	//Decode a subsampled version of the default album art.
            bm = mApp.decodeSampledBitmapFromResource(R.drawable.default_album_art, 150, 150);
        }
        
        return bm;
    }

    @Override
    public void onProgressUpdate(Integer... values) {
    	super.onProgressUpdate(values);
    	switch(values[0]) {
    	case 0:
    		try {
    			mAppWidgetManager.updateAppWidget(currentAppWidgetId, views);
    		} catch (Exception e) {
    			Log.e("DEBUG", ">>>exception");
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
