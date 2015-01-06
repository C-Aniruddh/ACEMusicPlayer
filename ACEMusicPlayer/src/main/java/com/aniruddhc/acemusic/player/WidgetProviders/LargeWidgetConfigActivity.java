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

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.aniruddhc.acemusic.player.R;

public class LargeWidgetConfigActivity extends Activity {

	private SharedPreferences sharedPreferences;
	private int mAppWidgetId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Holo_Dialog);
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, new Intent());
		sharedPreferences = this.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);

		//Retrieve the id of the widget that called this activity.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras!=null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            					 AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_widget_color);
		builder.setCancelable(false);
		builder.setSingleChoiceItems(R.array.widget_color_options, -1, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0) {
					//Light theme.
					sharedPreferences.edit().putString("" + mAppWidgetId, "LIGHT").commit();
				} else if (which==1) {
					//Dark theme.
					sharedPreferences.edit().putString("" + mAppWidgetId, "DARK").commit();
				}
				
				updateWidgetConfig();
			}
			
		});
		
		builder.create().show();
	}
	
	private void updateWidgetConfig() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews views = new RemoteViews(getPackageName(),
											R.layout.large_widget_layout);
											appWidgetManager.updateAppWidget(mAppWidgetId, views);
											
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		updateWidget();
		finish();
	}
	
	private void updateWidget() {
		try {
			Intent largeWidgetIntent = new Intent(this, LargeWidgetProvider.class);
			largeWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int largeWidgetIds[] = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, LargeWidgetProvider.class));
			largeWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, largeWidgetIds);
			this.sendBroadcast(largeWidgetIntent);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
