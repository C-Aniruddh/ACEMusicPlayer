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
package com.aniruddhc.acemusic.player.EqualizerActivity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Paint;
import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncApplyEQToAllSongsTask;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Dialogs.EQAlbumsListDialog;
import com.aniruddhc.acemusic.player.Dialogs.EQArtistsListDialog;
import com.aniruddhc.acemusic.player.Dialogs.EQGenresListDialog;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Views.VerticalSeekBar;

public class EqualizerActivity extends FragmentActivity {

	//Context.
	protected Context mContext;
	private Common mApp;
    private EqualizerActivity mFragment;

    //Equalizer container elements.
    private ScrollView mScrollView;

	// 50Hz equalizer controls.
	private VerticalSeekBar equalizer50HzSeekBar;
	private TextView text50HzGainTextView;
	private TextView text50Hz;

	// 130Hz equalizer controls.
	private VerticalSeekBar equalizer130HzSeekBar;
	private TextView text130HzGainTextView;
	private TextView text130Hz;

	// 320Hz equalizer controls.
	private VerticalSeekBar equalizer320HzSeekBar;
	private TextView text320HzGainTextView;
	private TextView text320Hz;

	// 800 Hz equalizer controls.
	private VerticalSeekBar equalizer800HzSeekBar;
	private TextView text800HzGainTextView;
	private TextView text800Hz;

	// 2 kHz equalizer controls.
	private VerticalSeekBar equalizer2kHzSeekBar;
	private TextView text2kHzGainTextView;
	private TextView text2kHz;

	// 5 kHz equalizer controls.
	private VerticalSeekBar equalizer5kHzSeekBar;
	private TextView text5kHzGainTextView;
	private TextView text5kHz;

	// 12.5 kHz equalizer controls.
	private VerticalSeekBar equalizer12_5kHzSeekBar;
	private TextView text12_5kHzGainTextView;
	private TextView text12_5kHz;

	// Equalizer preset controls.
	private RelativeLayout loadPresetButton;
	private RelativeLayout saveAsPresetButton;
	private RelativeLayout resetAllButton;
	private TextView loadPresetText;
	private TextView savePresetText;
	private TextView resetAllText;

	// Temp variables that hold the equalizer's settings.
	private int fiftyHertzLevel = 16;
	private int oneThirtyHertzLevel = 16;
	private int threeTwentyHertzLevel = 16;
	private int eightHundredHertzLevel = 16;
	private int twoKilohertzLevel = 16;
	private int fiveKilohertzLevel = 16;
	private int twelvePointFiveKilohertzLevel = 16;

	// Temp variables that hold audio fx settings.
	private int virtualizerLevel;
	private int bassBoostLevel;
	private int reverbSetting;
	
	//Audio FX elements.
	private SeekBar virtualizerSeekBar;
	private SeekBar bassBoostSeekBar;
	private Spinner reverbSpinner;
	private TextView virtualizerTitle;
	private TextView bassBoostTitle;
	private TextView reverbTitle;

    //Misc flags.
    private boolean mDoneButtonPressed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Context.
        mContext = getApplicationContext();
        mApp = (Common) mContext.getApplicationContext();
        mFragment = this;

        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_equalizer);

        //Equalizer container elements.
        mScrollView = (ScrollView) findViewById(R.id.equalizerScrollView);
        mScrollView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

        //50Hz equalizer controls.
        equalizer50HzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer50Hz);
        text50HzGainTextView = (TextView) findViewById(R.id.text50HzGain);
        text50Hz = (TextView) findViewById(R.id.text50Hz);

        //130Hz equalizer controls.
        equalizer130HzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer130Hz);
        text130HzGainTextView = (TextView) findViewById(R.id.text130HzGain);
        text130Hz = (TextView) findViewById(R.id.text130Hz);

        //320Hz equalizer controls.
        equalizer320HzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer320Hz);
        text320HzGainTextView = (TextView) findViewById(R.id.text320HzGain);
        text320Hz = (TextView) findViewById(R.id.text320Hz);

        //800Hz equalizer controls.
        equalizer800HzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer800Hz);
        text800HzGainTextView = (TextView) findViewById(R.id.text800HzGain);
        text800Hz = (TextView) findViewById(R.id.text800Hz);

        //2kHz equalizer controls.
        equalizer2kHzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer2kHz);
        text2kHzGainTextView = (TextView) findViewById(R.id.text2kHzGain);
        text2kHz = (TextView) findViewById(R.id.text2kHz);

        //5kHz equalizer controls.
        equalizer5kHzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer5kHz);
        text5kHzGainTextView = (TextView) findViewById(R.id.text5kHzGain);
        text5kHz = (TextView) findViewById(R.id.text5kHz);

        //12.5kHz equalizer controls.
        equalizer12_5kHzSeekBar = (VerticalSeekBar) findViewById(R.id.equalizer12_5kHz);
        text12_5kHzGainTextView = (TextView) findViewById(R.id.text12_5kHzGain);
        text12_5kHz = (TextView) findViewById(R.id.text12_5kHz);

        //Equalizer preset controls.
        loadPresetButton = (RelativeLayout) findViewById(R.id.loadPresetButton);
        saveAsPresetButton = (RelativeLayout) findViewById(R.id.saveAsPresetButton);
        resetAllButton = (RelativeLayout) findViewById(R.id.resetAllButton);
        loadPresetText = (TextView) findViewById(R.id.load_preset_text);
        savePresetText = (TextView) findViewById(R.id.save_as_preset_text);
        resetAllText = (TextView) findViewById(R.id.reset_all_text);

        //Audio FX elements.
        virtualizerSeekBar = (SeekBar) findViewById(R.id.virtualizer_seekbar);
        bassBoostSeekBar = (SeekBar) findViewById(R.id.bass_boost_seekbar);
        reverbSpinner = (Spinner) findViewById(R.id.reverb_spinner);
        virtualizerTitle = (TextView) findViewById(R.id.virtualizer_title_text);
        bassBoostTitle = (TextView) findViewById(R.id.bass_boost_title_text);
        reverbTitle = (TextView) findViewById(R.id.reverb_title_text);

        text50HzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text130HzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text320HzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text800HzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text2kHzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text5kHzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text12_5kHzGainTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text50Hz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text130Hz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text320Hz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text800Hz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text2kHz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text5kHz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        text12_5kHz.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));

        loadPresetText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));
        savePresetText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));
        resetAllText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));

        text50HzGainTextView.setPaintFlags(text50HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text130HzGainTextView.setPaintFlags(text130HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text320HzGainTextView.setPaintFlags(text320HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text800HzGainTextView.setPaintFlags(text130HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text2kHzGainTextView.setPaintFlags(text320HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text5kHzGainTextView.setPaintFlags(text130HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text12_5kHzGainTextView.setPaintFlags(text320HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text50Hz.setPaintFlags(text50Hz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text130Hz.setPaintFlags(text130Hz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text320Hz.setPaintFlags(text320Hz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text800Hz.setPaintFlags(text800Hz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text2kHz.setPaintFlags(text2kHz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text5kHz.setPaintFlags(text5kHz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        text12_5kHz.setPaintFlags(text12_5kHz.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        loadPresetText.setPaintFlags(text50HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        savePresetText.setPaintFlags(text50HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        resetAllText.setPaintFlags(text50HzGainTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        text50HzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text130HzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text320HzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text800HzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text2kHzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text5kHzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text12_5kHzGainTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text50Hz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text130Hz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text320Hz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text800Hz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text2kHz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text5kHz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        text12_5kHz.setTextColor(UIElementsHelper.getSmallTextColor(mContext));

        //Init reverb presets.
        ArrayList<String> reverbPresets = new ArrayList<String>();
        reverbPresets.add("None");
        reverbPresets.add("Large Hall");
        reverbPresets.add("Large Room");
        reverbPresets.add("Medium Hall");
        reverbPresets.add("Medium Room");
        reverbPresets.add("Small Room");
        reverbPresets.add("Plate");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reverbPresets);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverbSpinner.setAdapter(dataAdapter);

        //Set the max values for the seekbars.
        virtualizerSeekBar.setMax(1000);
        bassBoostSeekBar.setMax(1000);

        virtualizerTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        virtualizerTitle.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        virtualizerTitle.setPaintFlags(virtualizerTitle.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        bassBoostTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        bassBoostTitle.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        bassBoostTitle.setPaintFlags(bassBoostTitle.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        reverbTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        reverbTitle.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
        reverbTitle.setPaintFlags(reverbTitle.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        resetAllButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Reset all sliders to 0.
                equalizer50HzSeekBar.setProgressAndThumb(16);
                equalizer130HzSeekBar.setProgressAndThumb(16);
                equalizer320HzSeekBar.setProgressAndThumb(16);
                equalizer800HzSeekBar.setProgressAndThumb(16);
                equalizer2kHzSeekBar.setProgressAndThumb(16);
                equalizer5kHzSeekBar.setProgressAndThumb(16);
                equalizer12_5kHzSeekBar.setProgressAndThumb(16);
                virtualizerSeekBar.setProgress(0);
                bassBoostSeekBar.setProgress(0);
                reverbSpinner.setSelection(0, false);

                //Apply the new setings to the service.
                applyCurrentEQSettings();

                //Show a confirmation toast.
                Toast.makeText(mContext, R.string.eq_reset, Toast.LENGTH_SHORT).show();

            }

        });

        loadPresetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buildLoadPresetDialog().show();

            }

        });

        saveAsPresetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buildSavePresetDialog().show();

            }

        });

        equalizer50HzSeekBar.setOnSeekBarChangeListener(equalizer50HzListener);
        equalizer130HzSeekBar.setOnSeekBarChangeListener(equalizer130HzListener);
        equalizer320HzSeekBar.setOnSeekBarChangeListener(equalizer320HzListener);
        equalizer800HzSeekBar.setOnSeekBarChangeListener(equalizer800HzListener);
        equalizer2kHzSeekBar.setOnSeekBarChangeListener(equalizer2kHzListener);
        equalizer5kHzSeekBar.setOnSeekBarChangeListener(equalizer5kHzListener);
        equalizer12_5kHzSeekBar.setOnSeekBarChangeListener(equalizer12_5kHzListener);

        virtualizerSeekBar.setOnSeekBarChangeListener(virtualizerListener);
        bassBoostSeekBar.setOnSeekBarChangeListener(bassBoostListener);
        reverbSpinner.setOnItemSelectedListener(reverbListener);

        //Get the saved equalizer settings and apply them to the UI elements.
        new AsyncInitSlidersTask().execute();

    }

    /**
     * Sets the activity theme based on the user preference.
     */
    private void setTheme() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                setTheme(R.style.AppThemeNoTranslucentNav);
            } else {
                setTheme(R.style.AppThemeNoTranslucentNavLight);
            }

        } else {
            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppThemeLight);
            }

        }

    }
	
	/**
	 * 50 Hz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer50HzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short sixtyHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(50000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text50HzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(sixtyHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text50HzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(sixtyHertzBand, (short) (-1500));
					} else {
						text50HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(sixtyHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text50HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(sixtyHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				fiftyHertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * 130 Hz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer130HzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short twoThirtyHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(130000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text130HzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twoThirtyHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text130HzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twoThirtyHertzBand, (short) (-1500));
					} else {
						text130HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twoThirtyHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text130HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twoThirtyHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				oneThirtyHertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};

	/**
	 * 320 Hz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer320HzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short nineTenHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(320000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text320HzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(nineTenHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text320HzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(nineTenHertzBand, (short) (-1500));
					} else {
						text320HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(nineTenHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text320HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(nineTenHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				threeTwentyHertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * 800 Hz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer800HzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short threeKiloHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(800000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text800HzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(threeKiloHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text800HzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(threeKiloHertzBand, (short) (-1500));
					} else {
						text800HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(threeKiloHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text800HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(threeKiloHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				eightHundredHertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * 2 kHz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer2kHzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short fourteenKiloHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(2000000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text2kHzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fourteenKiloHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text2kHzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fourteenKiloHertzBand, (short) (-1500));
					} else {
						text2kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fourteenKiloHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text2kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fourteenKiloHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				twoKilohertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * 5 kHz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer5kHzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short fiveKiloHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(5000000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text5kHzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fiveKiloHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text5kHzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fiveKiloHertzBand, (short) (-1500));
					} else {
						text5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(fiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				fiveKilohertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * 12.5 kHz equalizer seekbar listener.
	 */
	private OnSeekBarChangeListener equalizer12_5kHzListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			
			try {
				//Get the appropriate equalizer band.
				short twelvePointFiveKiloHertzBand = mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand(9000000);
				
				//Set the gain level text based on the slider position.
				if (seekBarLevel==16) {
					text12_5kHzGainTextView.setText("0 dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) 0);
				} else if (seekBarLevel < 16) {
					
					if (seekBarLevel==0) {
						text12_5kHzGainTextView.setText("-" + "15 dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) (-1500));
					} else {
						text12_5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
						mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
					}
					
				} else if (seekBarLevel > 16) {
					text12_5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
					mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
				}
				
				twelvePointFiveKilohertzLevel = seekBarLevel;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * Spinner listener for reverb effects.
	 */
	private OnItemSelectedListener reverbListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
			
			if (mApp.isServiceRunning())
				if (index==0) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_NONE);
					reverbSetting = 0;
				} else if (index==1) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
					reverbSetting = 1;
				} else if (index==2) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
					reverbSetting = 2;
				} else if (index==3) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
					reverbSetting = 3;
				} else if (index==4) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
					reverbSetting = 4;
				} else if (index==5) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
					reverbSetting = 5;
				} else if (index==6) {
					mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset(PresetReverb.PRESET_PLATE);
					reverbSetting = 6;
				}
			
			else
				reverbSetting = 0;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    /**
     * Bass boost listener.
     */
    private OnSeekBarChangeListener bassBoostListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			mApp.getService().getEqualizerHelper().getCurrentBassBoost().setStrength((short) arg1);
			bassBoostLevel = (short) arg1;
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
	   
    };
    
    /**
     * Virtualizer listener.
     */
    private OnSeekBarChangeListener virtualizerListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			mApp.getService().getEqualizerHelper().getCurrentVirtualizer().setStrength((short) arg1);
			virtualizerLevel = (short) arg1;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
	   
    };
    
	/**
	 * Builds the "Save Preset" dialog. Does not call the show() method, so you 
	 * should do this manually when calling this method.
	 * 
	 * @return A fully built AlertDialog reference.
	 */
	private AlertDialog buildSavePresetDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_new_equalizer_preset_dialog_layout, null);
        
        final EditText newPresetNameField = (EditText) dialogView.findViewById(R.id.new_preset_name_text_field);
        newPresetNameField.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
        newPresetNameField.setPaintFlags(newPresetNameField.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Set the dialog title.
        builder.setTitle(R.string.save_preset);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				
			}
        	
        });
        
        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Get the preset name from the text field.
				String presetName = newPresetNameField.getText().toString();
				
				//Add the preset and it's values to the DB.
				mApp.getDBAccessHelper().addNewEQPreset(presetName, 
									  				    fiftyHertzLevel, 
									  				    oneThirtyHertzLevel, 
									  				    threeTwentyHertzLevel, 
									  				    eightHundredHertzLevel, 
									  				    twoKilohertzLevel, 
									  				    fiveKilohertzLevel, 
									  				    twelvePointFiveKilohertzLevel, 
									  				    (short) virtualizerSeekBar.getProgress(), 
									  				    (short) bassBoostSeekBar.getProgress(), 
									  				    (short) reverbSpinner.getSelectedItemPosition());
				
				Toast.makeText(mContext, R.string.preset_saved, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
        	
        });

        return builder.create();
        
	}
	
	/**
	 * Builds the "Load Preset" dialog. Does not call the show() method, so this 
	 * should be done manually after calling this method.
	 * 
	 * @return A fully built AlertDialog reference.
	 */
	private AlertDialog buildLoadPresetDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Get a cursor with the list of EQ presets.
        final Cursor cursor = mApp.getDBAccessHelper().getAllEQPresets();
        
        //Set the dialog title.
        builder.setTitle(R.string.load_preset);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cursor.moveToPosition(which);
				
				//Close the dialog.
				dialog.dismiss();
				
				//Pass on the equalizer values to the appropriate fragment.
				fiftyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_50_HZ));
				oneThirtyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_130_HZ));
				threeTwentyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_320_HZ));
				eightHundredHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_800_HZ));
				twoKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_2000_HZ));
				fiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_5000_HZ));
				twelvePointFiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_12500_HZ));
				virtualizerLevel = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.VIRTUALIZER));
				bassBoostLevel = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.BASS_BOOST));
				reverbSetting = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.REVERB));
				
				//Save the new equalizer settings to the DB.
				@SuppressWarnings({ "rawtypes" })
				AsyncTask task = new AsyncTask() {

					@Override
					protected Object doInBackground(Object... arg0) {
						setEQValuesForSong(mApp.getService().getCurrentSong().getId());
						return null;
					}
					
					@Override
					public void onPostExecute(Object result) {
						super.onPostExecute(result);
						
						//Reinitialize the UI elements to apply the new equalizer settings.
						new AsyncInitSlidersTask().execute();
					}
					
				};
				task.execute();

				if (cursor!=null)
					cursor.close();
				
			}
			
		}, DBAccessHelper.PRESET_NAME);

        return builder.create();
        
	}
	
	/**
	 * Builds the "Apply To" dialog. Does not call the show() method, so you 
	 * should do this manually when calling this method.
	 * 
	 * @return A fully built AlertDialog reference.
	 */
	public AlertDialog buildApplyToDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCancelable(false);
        builder.setItems(R.array.apply_equalizer_to_array, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (which==0) {
					setEQValuesForSong(mApp.getService().getCurrentSong().getId());
					Toast.makeText(mContext, R.string.eq_applied_to_current_song, Toast.LENGTH_SHORT).show();
					
					//Finish this activity.
                    finish();
					
				} else if (which==1) {	
					AsyncApplyEQToAllSongsTask task = new AsyncApplyEQToAllSongsTask(mContext, mFragment);
					task.execute();
					dialog.dismiss();

                    //Finish this activity.
                    finish();

				} else if (which==2) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					EQArtistsListDialog artistDialog = new EQArtistsListDialog();
					artistDialog.show(ft, "eqArtistsListDialog");
					
					dialog.dismiss();

				} else if (which==3) {	
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					EQAlbumsListDialog albumsDialog = new EQAlbumsListDialog();
					albumsDialog.show(ft, "eqAlbumsListDialog");
					
					dialog.dismiss();

				} else if (which==4) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					EQGenresListDialog genresDialog = new EQGenresListDialog();
					genresDialog.show(ft, "eqGenresListDialog");
					
					dialog.dismiss();
					
				}
				
			}
        	
        });

        return builder.create(); 
	}
    
    /**
     * Saves the EQ settings to the database for the specified song.
     */
    public void setEQValuesForSong(String songId) {
    	
    	//Grab the EQ values for the specified song.
    	int[] currentEqValues = mApp.getDBAccessHelper().getSongEQValues(songId);
		
		//Check if a database entry already exists for this song.
		if (currentEqValues[10]==0) {
			//Add a new DB entry.
			mApp.getDBAccessHelper().addSongEQValues(songId, 
									 				 fiftyHertzLevel, 
									 				 oneThirtyHertzLevel, 
									 				 threeTwentyHertzLevel, 
									 				 eightHundredHertzLevel, 
									 				 twoKilohertzLevel, 
									 				 fiveKilohertzLevel,
									 				 twelvePointFiveKilohertzLevel,
									 				 virtualizerLevel, 
									 				 bassBoostLevel, 
									 				 reverbSetting);
		} else {
			//Update the existing entry.
			mApp.getDBAccessHelper().updateSongEQValues(songId, 
									 			   		fiftyHertzLevel, 
									 			   		oneThirtyHertzLevel, 
									 			   		threeTwentyHertzLevel, 
									 			   		eightHundredHertzLevel, 
									 			   		twoKilohertzLevel, 
									 			   		fiveKilohertzLevel, 
									 			   		twelvePointFiveKilohertzLevel, 
									 			   		virtualizerLevel, 
									 			   		bassBoostLevel, 
									 			   		reverbSetting);
		}

    }

    /**
     * Applies the current EQ settings to the service.
     */
    public void applyCurrentEQSettings() {
    	if (!mApp.isServiceRunning())
    		return;
		
		equalizer50HzListener.onProgressChanged(equalizer50HzSeekBar, equalizer50HzSeekBar.getProgress(), true);
		equalizer130HzListener.onProgressChanged(equalizer130HzSeekBar, equalizer130HzSeekBar.getProgress(), true);
		equalizer320HzListener.onProgressChanged(equalizer320HzSeekBar, equalizer320HzSeekBar.getProgress(), true);
		equalizer800HzListener.onProgressChanged(equalizer800HzSeekBar, equalizer800HzSeekBar.getProgress(), true);
		equalizer2kHzListener.onProgressChanged(equalizer2kHzSeekBar, equalizer2kHzSeekBar.getProgress(), true);
		equalizer5kHzListener.onProgressChanged(equalizer5kHzSeekBar, equalizer5kHzSeekBar.getProgress(), true);
		equalizer12_5kHzListener.onProgressChanged(equalizer12_5kHzSeekBar, equalizer12_5kHzSeekBar.getProgress(), true);
		
		virtualizerListener.onProgressChanged(virtualizerSeekBar, virtualizerSeekBar.getProgress(), true);
		bassBoostListener.onProgressChanged(bassBoostSeekBar, bassBoostSeekBar.getProgress(), true);
		reverbListener.onItemSelected(reverbSpinner, null, reverbSpinner.getSelectedItemPosition(), 0l);

    }
	
	/**
	 * Broadcast receiver that calls the methods that update the sliders with the 
	 * current song's EQ.
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(Common.UPDATE_EQ_FRAGMENT)) {
				new AsyncInitSlidersTask().execute();
				
			}

            if (intent.hasExtra(Common.SERVICE_STOPPING)) {
                finish();
            }
			
		}
		
	};

    /**
     * Initializes the ActionBar.
     */
    private void showEqualizerActionBar(Menu menu) {

        //Set the Actionbar color.
        getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

        //Hide all menu items except the toggle button and "done" icon.
        menu.findItem(R.id.action_equalizer).setVisible(false);
        menu.findItem(R.id.action_pin).setVisible(false);
        menu.findItem(R.id.action_queue_drawer).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_done).setVisible(true);

        /**
         * The Toggle button in the actionbar doesn't work at this point. The setChecked()
         * method doesn't do anything, so there's no way to programmatically set the
         * switch to its correct position when the equalizer fragment is first shown.
         * Users will just have to rely on the "Reset" button in the equalizer fragment
         * to effectively switch off the equalizer.
         */
        menu.findItem(R.id.action_equalizer_toggle).setVisible(false); //Hide the toggle for now.

		//Set the toggle listener.
		ToggleButton equalizerToggle = (ToggleButton) menu.findItem(R.id.action_equalizer_toggle)
									 		  			  .getActionView()
									 		  			  .findViewById(R.id.actionbar_toggle_switch);

		//Set the current state of the toggle.
		boolean toggleSetting = true;
		if (mApp.isEqualizerEnabled())
			toggleSetting = true;
		else
			toggleSetting = false;

        //Set the ActionBar title text color.
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView abTitle = (TextView) findViewById(titleId);
        abTitle.setTextColor(0xFFFFFFFF);

		equalizerToggle.setChecked(toggleSetting);
		equalizerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean state) {
				mApp.setIsEqualizerEnabled(state);

				if (state==true)
					applyCurrentEQSettings();

			}

		});

        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.now_playing, menu);

        showEqualizerActionBar(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_done:
                mDoneButtonPressed = true;
                buildApplyToDialog().show();
                return true;
            default:
                //Return false to allow the activity to handle the item click.
                return false;
        }

    }

    @Override
    public void onPause() {
    	super.onPause();

        //Save the EQ values for the current song.
        if (!mDoneButtonPressed) {
           setEQValuesForSong(mApp.getService().getCurrentSong().getId());
           Toast.makeText(mContext, R.string.eq_applied_to_current_song, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		//Initialize the broadcast manager that will listen for track changes.
    	LocalBroadcastManager.getInstance(mContext)
		 					 .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//Unregister the broadcast receivers.
    	LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
	
	}
	
    /**
    * Retrieves the saved equalizer settings for the current song 
    * and applies them to the UI elements.
    */
	public class AsyncInitSlidersTask extends AsyncTask<Boolean, Boolean, Boolean> {
		
		int[] eqValues;
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			eqValues = mApp.getDBAccessHelper()
				 	   .getSongEQValues(mApp.getService()
						 			  		.getCurrentSong()
						 			  		.getId());
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			fiftyHertzLevel = eqValues[0];
			oneThirtyHertzLevel = eqValues[1];
			threeTwentyHertzLevel = eqValues[2];
			eightHundredHertzLevel = eqValues[3];
			twoKilohertzLevel = eqValues[4];
			fiveKilohertzLevel = eqValues[5];
			twelvePointFiveKilohertzLevel = eqValues[6];
			virtualizerLevel = eqValues[7];
			bassBoostLevel = eqValues[8];
			reverbSetting = eqValues[9];
			
			//Move the sliders to the equalizer settings.
			equalizer50HzSeekBar.setProgressAndThumb(fiftyHertzLevel);
			equalizer130HzSeekBar.setProgressAndThumb(oneThirtyHertzLevel);
			equalizer320HzSeekBar.setProgressAndThumb(threeTwentyHertzLevel);
			equalizer800HzSeekBar.setProgressAndThumb(eightHundredHertzLevel);
			equalizer2kHzSeekBar.setProgressAndThumb(twoKilohertzLevel);
			equalizer5kHzSeekBar.setProgressAndThumb(fiveKilohertzLevel);
			equalizer12_5kHzSeekBar.setProgressAndThumb(twelvePointFiveKilohertzLevel);
	        virtualizerSeekBar.setProgress(virtualizerLevel);
	        bassBoostSeekBar.setProgress(bassBoostLevel);
	        reverbSpinner.setSelection(reverbSetting, false);

			//50Hz Band.
			if (fiftyHertzLevel==16) {
				text50HzGainTextView.setText("0 dB");
			} else if (fiftyHertzLevel < 16) {
				
				if (fiftyHertzLevel==0) {
					text50HzGainTextView.setText("-" + "15 dB");
				} else {
					text50HzGainTextView.setText("-" + (16-fiftyHertzLevel) + " dB");
				}
				
			} else if (fiftyHertzLevel > 16) {
				text50HzGainTextView.setText("+" + (fiftyHertzLevel-16) + " dB");
			}
			
			//130Hz Band.
			if (oneThirtyHertzLevel==16) {
				text130HzGainTextView.setText("0 dB");
			} else if (oneThirtyHertzLevel < 16) {
				
				if (oneThirtyHertzLevel==0) {
					text130HzGainTextView.setText("-" + "15 dB");
				} else {
					text130HzGainTextView.setText("-" + (16-oneThirtyHertzLevel) + " dB");
				}
				
			} else if (oneThirtyHertzLevel > 16) {
				text130HzGainTextView.setText("+" + (oneThirtyHertzLevel-16) + " dB");
			}
			
			//320Hz Band.
			if (threeTwentyHertzLevel==16) {
				text320HzGainTextView.setText("0 dB");
			} else if (threeTwentyHertzLevel < 16) {
				
				if (threeTwentyHertzLevel==0) {
					text320HzGainTextView.setText("-" + "15 dB");
				} else {
					text320HzGainTextView.setText("-" + (16-threeTwentyHertzLevel) + " dB");
				}
				
			} else if (threeTwentyHertzLevel > 16) {
				text320HzGainTextView.setText("+" + (threeTwentyHertzLevel-16) + " dB");
			}
			
			//800Hz Band.
			if (eightHundredHertzLevel==16) {
				text800HzGainTextView.setText("0 dB");
			} else if (eightHundredHertzLevel < 16) {
				
				if (eightHundredHertzLevel==0) {
					text800HzGainTextView.setText("-" + "15 dB");
				} else {
					text800HzGainTextView.setText("-" + (16-eightHundredHertzLevel) + " dB");
				}
				
			} else if (eightHundredHertzLevel > 16) {
				text800HzGainTextView.setText("+" + (eightHundredHertzLevel-16) + " dB");
			}
			
			//2kHz Band.
			if (twoKilohertzLevel==16) {
				text2kHzGainTextView.setText("0 dB");
			} else if (twoKilohertzLevel < 16) {
				
				if (twoKilohertzLevel==0) {
					text2kHzGainTextView.setText("-" + "15 dB");
				} else {
					text2kHzGainTextView.setText("-" + (16-twoKilohertzLevel) + " dB");
				}
				
			} else if (twoKilohertzLevel > 16) {
				text2kHzGainTextView.setText("+" + (twoKilohertzLevel-16) + " dB");
			}
			
			//5kHz Band.
			if (fiveKilohertzLevel==16) {
				text5kHzGainTextView.setText("0 dB");
			} else if (fiveKilohertzLevel < 16) {
				
				if (fiveKilohertzLevel==0) {
					text5kHzGainTextView.setText("-" + "15 dB");
				} else {
					text5kHzGainTextView.setText("-" + (16-fiveKilohertzLevel) + " dB");
				}
				
			} else if (fiveKilohertzLevel > 16) {
				text5kHzGainTextView.setText("+" + (fiveKilohertzLevel-16) + " dB");
			}
			
			//12.5kHz Band.
			if (twelvePointFiveKilohertzLevel==16) {
				text12_5kHzGainTextView.setText("0 dB");
			} else if (twelvePointFiveKilohertzLevel < 16) {
				
				if (twelvePointFiveKilohertzLevel==0) {
					text12_5kHzGainTextView.setText("-" + "15 dB");
				} else {
					text12_5kHzGainTextView.setText("-" + (16-twelvePointFiveKilohertzLevel) + " dB");
				}
				
			} else if (twelvePointFiveKilohertzLevel > 16) {
				text12_5kHzGainTextView.setText("+" + (twelvePointFiveKilohertzLevel-16) + " dB");
			}
			
		}
		
	}

    /**
     * Getter methods.
     */
    
	public int getFiftyHertzLevel() {
		return fiftyHertzLevel;
	}

	public int getOneThirtyHertzLevel() {
		return oneThirtyHertzLevel;
	}

	public int getThreeTwentyHertzLevel() {
		return threeTwentyHertzLevel;
	}

	public int getEightHundredHertzLevel() {
		return eightHundredHertzLevel;
	}

	public int getTwoKilohertzLevel() {
		return twoKilohertzLevel;
	}

	public int getFiveKilohertzLevel() {
		return fiveKilohertzLevel;
	}

	public int getTwelvePointFiveKilohertzLevel() {
		return twelvePointFiveKilohertzLevel;
	}

	public int getVirtualizerLevel() {
		return virtualizerLevel;
	}

	public int getBassBoostLevel() {
		return bassBoostLevel;
	}
	
	public SeekBar getVirtualizerSeekBar() {
		return virtualizerSeekBar;
	}
	
	public SeekBar getBassBoostSeekBar() {
		return bassBoostSeekBar;
	}
	
	public Spinner getReverbSpinner() {
		return reverbSpinner;
	}

	/**
	 * Setter methods.
	 */
	
	public void setFiftyHertzLevel(int fiftyHertzLevel) {
		this.fiftyHertzLevel = fiftyHertzLevel;
	}

	public void setOneThirtyHertzLevel(int oneThirtyHertzLevel) {
		this.oneThirtyHertzLevel = oneThirtyHertzLevel;
	}

	public void setThreeTwentyHertzLevel(int threeTwentyHertzLevel) {
		this.threeTwentyHertzLevel = threeTwentyHertzLevel;
	}

	public void setEightHundredHertzLevel(int eightHundredHertzLevel) {
		this.eightHundredHertzLevel = eightHundredHertzLevel;
	}

	public void setTwoKilohertzLevel(int twoKilohertzLevel) {
		this.twoKilohertzLevel = twoKilohertzLevel;
	}

	public void setFiveKilohertzLevel(int fiveKilohertzLevel) {
		this.fiveKilohertzLevel = fiveKilohertzLevel;
	}

	public void setTwelvePointFiveKilohertzLevel(int twelvePointFiveKilohertzLevel) {
		this.twelvePointFiveKilohertzLevel = twelvePointFiveKilohertzLevel;
	}

	public void setVirtualizerLevel(int virtualizerLevel) {
		this.virtualizerLevel = virtualizerLevel;
	}

	public void setBassBoostLevel(int bassBoostLevel) {
		this.bassBoostLevel = bassBoostLevel;
	}
    
}
