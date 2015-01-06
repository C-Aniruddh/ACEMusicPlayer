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
package com.aniruddhc.acemusic.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Views.RangeSeekBar;
import com.aniruddhc.acemusic.player.Views.RangeSeekBar.OnRangeSeekBarChangeListener;

public class ABRepeatDialog extends DialogFragment {
	
	private Context mContext;
	private Common mApp;
	
	private int repeatPointA;
	private int repeatPointB;
	private int currentSongIndex;
	private int currentSongDurationMillis;
    private int currentSongDurationSecs;
	private SharedPreferences sharedPreferences;
	private BroadcastReceiver receiver;
	
	private TextView repeatSongATime;
	private TextView repeatSongBTime;
	private SeekBar seekBar;
	private RangeSeekBar<Integer> rangeSeekBar;
	private ViewGroup viewGroup;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mContext = getActivity().getApplicationContext();
		mApp = (Common) mContext;
		
        receiver = new BroadcastReceiver() {
        	
            @Override
            public void onReceive(Context context, Intent intent) {
            	initRepeatSongRangeDialog();
            	
            }
            
        };
		
		sharedPreferences = getActivity().getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_repeat_song_range_dialog, null);
        currentSongIndex = mApp.getService().getCurrentSongIndex();

        repeatSongATime = (TextView) view.findViewById(R.id.repeat_song_range_A_time);
        repeatSongBTime = (TextView) view.findViewById(R.id.repeat_song_range_B_time);

        currentSongDurationMillis = (int) mApp.getService().getCurrentMediaPlayer().getDuration();
        currentSongDurationSecs = (int) currentSongDurationMillis/1000;
        
        //Remove the placeholder seekBar and replace it with the RangeSeekBar.
        seekBar = (SeekBar) view.findViewById(R.id.repeat_song_range_placeholder_seekbar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
        viewGroup = (ViewGroup) seekBar.getParent();
        viewGroup.removeView(seekBar);
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, currentSongDurationSecs, getActivity());
        rangeSeekBar.setLayoutParams(params);
        viewGroup.addView(rangeSeekBar);
		
        if (sharedPreferences.getInt(Common.REPEAT_MODE, Common.REPEAT_OFF)==Common.A_B_REPEAT) {
        	repeatSongATime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointA()));
        	repeatSongBTime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointB()));

        	rangeSeekBar.setSelectedMinValue(mApp.getService().getRepeatSongRangePointA());
        	rangeSeekBar.setSelectedMaxValue(mApp.getService().getRepeatSongRangePointB());

            repeatPointA = mApp.getService().getRepeatSongRangePointA();
        	repeatPointB = mApp.getService().getRepeatSongRangePointB();
        } else {
        	repeatSongATime.setText("0:00");
        	repeatSongBTime.setText(convertMillisToMinsSecs(currentSongDurationMillis));
        	repeatPointA = 0;
        	repeatPointB = currentSongDurationMillis;
        }
        
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
            	repeatPointA = minValue*1000;
            	repeatPointB = maxValue*1000;
            	repeatSongATime.setText(convertMillisToMinsSecs(minValue*1000));
            	repeatSongBTime.setText(convertMillisToMinsSecs(maxValue*1000));
            }
            
        });
        
        //Set the dialog title.
        builder.setTitle(R.string.a_b_repeat);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

        });
        
        builder.setPositiveButton(R.string.repeat, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if ((currentSongDurationSecs - repeatPointB) < mApp.getCrossfadeDuration()) {
					//Remove the crossfade handler.
					mApp.getService().getHandler().removeCallbacks(mApp.getService().startCrossFadeRunnable);
					mApp.getService().getHandler().removeCallbacks(mApp.getService().crossFadeRunnable);
				}
				
				mApp.broadcastUpdateUICommand(new String[] { Common.UPDATE_PLAYBACK_CONTROLS }, 
											  new String[] { "" });
				mApp.getService().setRepeatSongRange(repeatPointA, repeatPointB);
				mApp.getService().setRepeatMode(Common.A_B_REPEAT);
				
			}
        	
        });

        return builder.create();
    }
	
	public void initRepeatSongRangeDialog() {

        currentSongDurationMillis = (int) mApp.getService().getCurrentMediaPlayer().getDuration();
        currentSongDurationSecs = (int) currentSongDurationMillis/1000;
        
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rangeSeekBar.getLayoutParams();
        viewGroup = (ViewGroup) rangeSeekBar.getParent();
        viewGroup.removeView(rangeSeekBar);
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, currentSongDurationSecs, getActivity());
        rangeSeekBar.setLayoutParams(params);
        viewGroup.addView(rangeSeekBar);
		
        if (sharedPreferences.getInt(Common.REPEAT_MODE, Common.REPEAT_OFF)==Common.A_B_REPEAT) {
        	repeatSongATime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointA()));
        	repeatSongBTime.setText(convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointB()));
        	rangeSeekBar.setSelectedMinValue(mApp.getService().getRepeatSongRangePointA());
        	rangeSeekBar.setSelectedMaxValue(mApp.getService().getRepeatSongRangePointB());
        	repeatPointA = mApp.getService().getRepeatSongRangePointA();
        	repeatPointB = mApp.getService().getRepeatSongRangePointB();
        } else {
        	repeatSongATime.setText("0:00");
        	repeatSongBTime.setText(convertMillisToMinsSecs(currentSongDurationMillis));
        	repeatPointA = 0;
        	repeatPointB = currentSongDurationMillis;
        }
        
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
            	repeatPointA = minValue*1000;
            	repeatPointB = maxValue*1000;
            	repeatSongATime.setText(convertMillisToMinsSecs(minValue*1000));
            	repeatSongBTime.setText(convertMillisToMinsSecs(maxValue*1000));
            }
            
        });
        
	}
	
	//Convert millisseconds to hh:mm:ss format.
    public static String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60 ;
    	int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
    	int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);
    	
    	String seconds = "";
    	String minutes = "";
    	String hours = "";
    	
    	if (secondsValue < 10) {
    		seconds = "0" + secondsValue;
    	} else {
    		seconds = "" + secondsValue;
    	}

    	if (minutesValue < 10) {
    		minutes = "0" + minutesValue;
    	} else {
    		minutes = "" + minutesValue;
    	}
    	
    	if (hoursValue < 10) {
    		hours = "0" + hoursValue;
    	} else {
    		hours = "" + hoursValue;
    	}
    	
    	String output = "";
    	
    	if (hoursValue!=0) {
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		output = minutes + ":" + seconds;
    	}
    	
    	return output;
    }
    
	@Override
	public void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
	    					 .registerReceiver((receiver), new IntentFilter("com.aniruddhc.acemusic.player.UPDATE_NOW_PLAYING"));
	
	}

	@Override
	public void onStop() {
	    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
	    super.onStop();
	    
	}
	
}
