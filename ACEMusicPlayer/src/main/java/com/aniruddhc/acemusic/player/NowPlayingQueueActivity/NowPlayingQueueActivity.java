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
package com.aniruddhc.acemusic.player.NowPlayingQueueActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Utils.TypefaceSpan;

public class NowPlayingQueueActivity extends FragmentActivity {

	public Context mContext;
	public SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mContext = this;
		sharedPreferences = getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
    	//Get the screen's parameters.
	    DisplayMetrics displayMetrics = new DisplayMetrics();
	    this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	    int screenWidth = displayMetrics.widthPixels;
		
    	//Set the UI theme.
    	if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME") ||
    		sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}

    	super.onCreate(savedInstanceState);
    	
    	if (getOrientation().equals("PORTRAIT")) {

    		//Finish this activity and relaunch the activity that called this one.
    		Intent intent = new Intent(this, (Class<?>) getIntent().getSerializableExtra("CALLING_CLASS"));
    		intent.putExtras(getIntent());
    		intent.putExtra("NEW_PLAYLIST", false);
    		intent.putExtra("CALLED_FROM_FOOTER", true);
    		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		finish();
    		startActivity(intent);
    		
    		return;
    		
    	} else {
    		
    		setContentView(R.layout.activity_now_playing_queue);
    		
        	final Fragment nowPlayingQueueFragment = new NowPlayingQueueFragment();
    	    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	    transaction.add(R.id.now_playing_queue_container, nowPlayingQueueFragment, "nowPlayingQueueFragment");
    	    transaction.commit();
    		
    	    SpannableString s = new SpannableString(getResources().getString(R.string.current_queue));
    	    s.setSpan(new TypefaceSpan(this, "RobotoCondensed-Light"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    	    // Update the action bar title with the TypefaceSpan instance.
    	    ActionBar actionBar = getActionBar();
    	    actionBar.setTitle(s);
    	    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.holo_gray_selector));
    	    
    	}
    	
	}

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
/*    	NowPlayingQueueFragment nowPlayingQueueFragment = 
    	
    	if (nowPlayingQueueFragment.bm!=null) {
    		
        	if (!nowPlayingQueueFragment.bm.isRecycled()) {
        		nowPlayingQueueFragment.bm.recycle();
        	}
        	
    	}

    	nowPlayingQueueFragment.bm = null;
    	nowPlayingQueueFragment.embeddedArt = null;
    	nowPlayingQueueFragment.is = null;*/

    	overridePendingTransition(R.anim.scale_and_fade_in, R.anim.fade_out);
    	
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
        	
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
            
        }

        return inSampleSize;
    }
    
    //Resamples a resource image to avoid OOM errors.
    public Bitmap decodeSampledBitmapFromResource(int resID, int reqWidth, int reqHeight) {

	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	
	    return BitmapFactory.decodeResource(getBaseContext().getResources(), resID, options);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	//Kill the activity to free up memory. We can restart the activity later.
    	finish();
    	
    }
    
    //Retrieves the orientation of the device.
    public String getOrientation() {

        if(getResources().getDisplayMetrics().widthPixels > 
           getResources().getDisplayMetrics().heightPixels) { 
            return "LANDSCAPE";
        } else {
            return "PORTRAIT";
        }     
        
    }
    
    @Override
    public void onBackPressed() {
    	//Ask the user to rotate the device to get back to the previous activity.
    	Toast.makeText(mContext, R.string.rotate_device_to_go_back, Toast.LENGTH_LONG).show();
    }

}
