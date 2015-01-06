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
import android.content.Context;
import android.content.Intent;

import com.aniruddhc.acemusic.player.AsyncTasks.AsyncUpdateBlurredWidgetTask;

public class BlurredWidgetProvider extends AppWidgetProvider {
	
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		mContext = context;
        final int N = appWidgetIds.length;

        AsyncUpdateBlurredWidgetTask task = new AsyncUpdateBlurredWidgetTask(mContext, N, appWidgetIds, appWidgetManager);
        task.execute();
 
    }
    
}
