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
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.LauncherActivity.LauncherActivity;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.Services.LargeWidgetAdapterService;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncUpdateLargeWidgetTask extends AsyncTask<String, Integer, Boolean> {
	
    private Context mContext;
    private Common mApp;
    private int mNumWidgets;
    private int mAppWidgetIds[];
    private AppWidgetManager mAppWidgetManager;
    
    private int currentAppWidgetId;
    private RemoteViews views;
    
	private String songTitle = "";
	private String albumName = "";
	private String artistName = "";
    
	public static final String PREVIOUS_ACTION = "com.aniruddhc.acemusic.player.PREVIOUS_ACTION";
	public static final String PLAY_PAUSE_ACTION = "com.aniruddhc.acemusic.player.PLAY_PAUSE_ACTION";
	public static final String NEXT_ACTION = "com.aniruddhc.acemusic.player.NEXT_ACTION";
    
    public AsyncUpdateLargeWidgetTask(Context context, int numWidgets, int appWidgetIds[], AppWidgetManager appWidgetManager) {
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
            
            //Initialize the RemoteView object to gain access to the widget's UI elements.
            views = new RemoteViews(mContext.getPackageName(), R.layout.large_widget_layout);

            if (widgetColor.equals("DARK")) {
            	views.setInt(R.id.large_widget_parent_layout, "setBackgroundResource", R.drawable.appwidget_dark_bg);
            	views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_play_light);
            	views.setImageViewResource(R.id.widget_previous_track, R.drawable.btn_playback_previous_light);
            	views.setImageViewResource(R.id.widget_next_track, R.drawable.btn_playback_next_light);
            } else if (widgetColor.equals("LIGHT")) {
            	views.setInt(R.id.large_widget_parent_layout, "setBackgroundResource", R.drawable.appwidget_bg);
            	views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_play);
            	views.setImageViewResource(R.id.widget_previous_track, R.drawable.btn_playback_previous);
            	views.setImageViewResource(R.id.widget_next_track, R.drawable.btn_playback_next);
            }
            
            /* Create a pendingIntent that will serve as a general template for the clickListener.
             * We'll create a fillInIntent in LargeWidgetAdapterService.java that will provide the 
             * index of the listview item that's been clicked. */
            Intent intent = new Intent();
            intent.setAction("com.jams.music.player.WIDGET_CHANGE_TRACK");
            PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            views.setPendingIntentTemplate(R.id.widget_listview, pendingIntentTemplate);
            
            //Create the intent to fire up the service that will back the adapter of the listview.
            Intent serviceIntent = new Intent(mContext, LargeWidgetAdapterService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            serviceIntent.putExtra("WIDGET_COLOR", widgetColor);
            
            views.setRemoteAdapter(R.id.widget_listview, serviceIntent);
            mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetIds, R.id.widget_listview);
            
            //Check if the service is running and update the widget elements.
            if (mApp.isServiceRunning()) {
            	
            	//Set the album art.
        		views.setViewVisibility(R.id.widget_listview, View.VISIBLE);
        		views.setImageViewBitmap(R.id.widget_album_art, getAlbumArt());
        		
        		final Intent notificationIntent = new Intent(mContext, NowPlayingActivity.class);
                notificationIntent.putExtra("CALLED_FROM_FOOTER", true);
                notificationIntent.putExtra("CALLED_FROM_NOTIF", true);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                views.setOnClickPendingIntent(R.id.widget_album_art, pendingIntent);
            	
            } else {
            	songTitle = "";
            	albumName = mContext.getResources().getString(R.string.no_music_playing);
            	
            	//Set the default album art.
        		views.setImageViewResource(R.id.widget_album_art, R.drawable.default_album_art);
        		views.setViewVisibility(R.id.widget_listview, View.INVISIBLE);
        		
        		if (widgetColor.equals("DARK")) {
        			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play_light);
        		} else if (widgetColor.equals("LIGHT")) {
        			views.setImageViewResource(R.id.app_widget_small_play, R.drawable.btn_playback_play);
        		}
        		
        		final Intent notificationIntent = new Intent(mContext, LauncherActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                views.setOnClickPendingIntent(R.id.widget_album_art, pendingIntent);
        		
            }
            
            //Set the song title, artist name, and album name.
            views.setTextViewText(R.id.widget_song_title_text, songTitle);
            views.setTextViewText(R.id.widget_album_text, albumName);
            views.setTextViewText(R.id.widget_artist_text, artistName);
            
            if (widgetColor.equals("LIGHT")) {
    			views.setTextColor(R.id.widget_song_title_text, Color.BLACK);
    			views.setTextColor(R.id.widget_album_text, Color.BLACK);
    			views.setTextColor(R.id.widget_artist_text, Color.BLACK);
    		}
            
            //Attach PendingIntents to the widget controls.
        	Intent previousTrackIntent = new Intent();
        	previousTrackIntent.setAction(PREVIOUS_ACTION);
        	PendingIntent previousPendingIntent = PendingIntent.getBroadcast(mContext, 0, previousTrackIntent, 0);
        	
        	Intent playPauseTrackIntent = new Intent();
        	playPauseTrackIntent.setAction(PLAY_PAUSE_ACTION);
        	PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(mContext, 0, playPauseTrackIntent, 0);
        	
        	Intent nextTrackIntent = new Intent();
        	nextTrackIntent.setAction(NEXT_ACTION);
        	PendingIntent nextPendingIntent = PendingIntent.getBroadcast(mContext, 0, nextTrackIntent, 0);
        	
        	//Set the pending intents on the buttons.
            views.setOnClickPendingIntent(R.id.widget_play, playPausePendingIntent);
            views.setOnClickPendingIntent(R.id.widget_previous_track, previousPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_next_track, nextPendingIntent);
            
            if (mApp.isServiceRunning()) {
            	try {
                	if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
                		if (widgetColor.equals("DARK")) {
                			views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_pause_light);
                		} else if (widgetColor.equals("LIGHT")) {
                			views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_pause);
                		}
                		
                	} else {
                		if (widgetColor.equals("DARK")) {
                			views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_play_light);
                		} else if (widgetColor.equals("LIGHT")) {
                			views.setImageViewResource(R.id.widget_play, R.drawable.btn_playback_play);
                		}
                		
                	}
            	} catch (Exception e) {
            		// TODO Auto-generated method stub
            		e.printStackTrace();
            	}
            	
            }
            
            //Tell the AppWidgetManager to perform an update on the current app widget.
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
    		mAppWidgetManager.updateAppWidget(currentAppWidgetId, views);
    		break;
    	}
    	
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
        
    }
    
}
