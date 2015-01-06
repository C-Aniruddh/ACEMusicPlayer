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
package com.aniruddhc.acemusic.player.WidgetProviders;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.aniruddhc.acemusic.player.AsyncTasks.AsyncUpdateLargeWidgetTask;

public class LargeWidgetProvider extends AppWidgetProvider {

	private Context mContext;
	
	public static final String PREVIOUS_ACTION = "com.aniruddhc.acemusic.player.PREVIOUS_ACTION";
	public static final String PLAY_PAUSE_ACTION = "com.aniruddhc.acemusic.player.PLAY_PAUSE_ACTION";
	public static final String NEXT_ACTION = "com.aniruddhc.acemusic.player.NEXT_ACTION";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent); 
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), LargeWidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		onUpdate(context, appWidgetManager, appWidgetIds);

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	mContext = context;
        final int N = appWidgetIds.length;
        
        AsyncUpdateLargeWidgetTask task = new AsyncUpdateLargeWidgetTask(mContext, N, appWidgetIds, appWidgetManager);
        task.execute();
        
    }
    
}
