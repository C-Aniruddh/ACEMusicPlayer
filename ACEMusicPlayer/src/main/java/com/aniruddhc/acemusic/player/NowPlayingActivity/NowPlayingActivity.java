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
package com.aniruddhc.acemusic.player.NowPlayingActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.Animations.FadeAnimation;
import com.aniruddhc.acemusic.player.Drawers.QueueDrawerFragment;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.Transformers.ZoomOutPageTransformer;
import com.velocity.view.pager.library.VelocityViewPager;

import java.util.HashMap;

public class NowPlayingActivity extends FragmentActivity {

    //Common objects.
	private Context mContext;
    private Common mApp;

    //Layouts.
	private DrawerLayout mDrawerLayout;
	private FrameLayout mDrawerParentLayout;
    private QueueDrawerFragment mQueueDrawerFragment;
    private RelativeLayout mCurrentQueueLayout;
	
	//Song info/seekbar elements.
	private SeekBar mSeekbar;
	private ProgressBar mStreamingProgressBar;
	
	//Playback Controls.
    private RelativeLayout mControlsLayoutHeaderParent;
	private RelativeLayout mControlsLayoutHeader;
	private ImageButton mPlayPauseButton;
    private RelativeLayout mPlayPauseButtonBackground;
	private ImageButton mNextButton;
	private ImageButton mPreviousButton;
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;

    //Seekbar indicator.
    private RelativeLayout mSeekbarIndicatorLayoutParent;
    private RelativeLayout mSeekbarIndicatorLayout;
    private TextView mSeekbarIndicatorText;

    //Seekbar strobe effect.
    private AlphaAnimation mSeekbarStrobeAnim;
    private static final int SEEKBAR_STROBE_ANIM_REPEAT = Animation.INFINITE;

	//Playlist pager.
    private VelocityViewPager mViewPager;
    private PlaylistPagerAdapter mViewPagerAdapter;

    //Handler object.
    private Handler mHandler = new Handler();
    
    //Differentiates between a user's scroll input and a programmatic scroll.
    private boolean USER_SCROLL = true;

    //HashMap that passes on song information to the "Download from Cloud" dialog.
    private HashMap<String, String> metadata;
    
    //Interface instance and flags.
    private NowPlayingActivityListener mNowPlayingActivityListener;
    public static final String START_SERVICE = "StartService";
    private boolean mIsCreating = true;
    private Animation mFadeInAnimation;
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	mContext = getApplicationContext();
    	mApp = (Common) getApplicationContext();
    	mApp.setNowPlayingActivity(this);
    	setNowPlayingActivityListener(mApp.getPlaybackKickstarter());
        mFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_top);
        mFadeInAnimation.setDuration(700);
    	
    	//Set the UI theme.
        setTheme();
        
        super.onCreate(savedInstanceState);	
    	setContentView(R.layout.activity_now_playing);
    	
    	//Set the volume stream for this activity.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        //Drawer layout.
        if (!mApp.isTabletInLandscape()) {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity_drawer_root);
            mDrawerParentLayout = (FrameLayout) findViewById(R.id.now_playing_drawer_frame_root);
            mCurrentQueueLayout = (RelativeLayout) findViewById(R.id.queue_drawer);
            mDrawerLayout.setDrawerListener(mDrawerListener);
            mDrawerLayout.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

        } else {
            mCurrentQueueLayout = (RelativeLayout) findViewById(R.id.queue_drawer);
        }
        
        //ViewPager.
        mViewPager = (VelocityViewPager) findViewById(R.id.nowPlayingPlaylistPager);

        //Seekbar indicator.
        mSeekbarIndicatorLayoutParent = (RelativeLayout) findViewById(R.id.seekbarIndicatorParent);
        mSeekbarIndicatorLayout = (RelativeLayout) findViewById(R.id.seekbarIndicator);
        mSeekbarIndicatorText = (TextView) findViewById(R.id.seekbarIndicatorText);

        mSeekbarIndicatorLayoutParent.setVisibility(View.GONE);
        mSeekbarIndicatorLayout.setBackgroundResource(UIElementsHelper.getGridViewCardBackground(mContext));
        mSeekbarIndicatorText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mSeekbarIndicatorText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
    	
    	//Playback Controls.
        mControlsLayoutHeaderParent = (RelativeLayout) findViewById(R.id.now_playing_controls_header_parent);
        mControlsLayoutHeader = (RelativeLayout) findViewById(R.id.now_playing_controls_header);
        mPlayPauseButtonBackground = (RelativeLayout) findViewById(R.id.playPauseButtonBackground);
    	mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
    	mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
    	mShuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
    	mRepeatButton = (ImageButton) findViewById(R.id.repeatButton);

    	//Song info/seekbar elements.
    	mSeekbar = (SeekBar) findViewById(R.id.nowPlayingSeekBar);
    	mStreamingProgressBar = (ProgressBar) findViewById(R.id.startingStreamProgressBar);
    	mStreamingProgressBar.setVisibility(View.GONE);

    	try {
    		mSeekbar.setThumb(getResources().getDrawable(R.drawable.transparent_drawable));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        mPlayPauseButtonBackground.setBackgroundResource(UIElementsHelper.getShadowedCircle(mContext));
        mPlayPauseButton.setImageResource(R.drawable.pause_light);
        mPlayPauseButton.setId(R.drawable.pause_light);
    	mNextButton.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_next"));
    	mPreviousButton.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_previous"));
    	
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		mNextButton.setAlpha(1f);
    		mPreviousButton.setAlpha(1f);
        }

        //KitKat specific layout code.
        setKitKatTranslucentBars();

    	//Set the control buttons and background.
        setControlButtonsBackground();
    	setPlayPauseButton();
        setShuffleButtonIcon();
        setRepeatButtonIcon();
    	
        //Set the click listeners.
    	mSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
    	mNextButton.setOnClickListener(mOnClickNextListener);
    	mPreviousButton.setOnClickListener(mOnClickPreviousListener);
    	mPlayPauseButton.setOnClickListener(playPauseClickListener);
        mPlayPauseButtonBackground.setOnClickListener(playPauseClickListener);
    	mShuffleButton.setOnClickListener(shuffleButtonClickListener);
    	mRepeatButton.setOnClickListener(repeatButtonClickListener);

        //Apply haptic feedback to the play/pause button.
        mPlayPauseButtonBackground.setHapticFeedbackEnabled(true);
        mPlayPauseButton.setHapticFeedbackEnabled(true);

    }
    
    /**
     * Updates this activity's UI elements based on the passed intent's 
     * update flag(s).
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	//Grab the bundle from the intent.
        	Bundle bundle = intent.getExtras();

        	//Initializes the ViewPager.
        	if (intent.hasExtra(Common.INIT_PAGER) || 
        		intent.hasExtra(Common.NEW_QUEUE_ORDER))
                initViewPager();

        	//Updates the ViewPager's current page/position.
        	if (intent.hasExtra(Common.UPDATE_PAGER_POSTIION)) {
                int currentPosition = mViewPager.getCurrentItem();
                int newPosition = Integer.parseInt(bundle.getString(Common.UPDATE_PAGER_POSTIION));
                if (currentPosition!=newPosition) {

                    if (newPosition > 0 && Math.abs(newPosition - currentPosition) <= 5) {
                        //Smooth scroll to the new index.
                        scrollViewPager(newPosition, true, 1, false);
                    } else {
                        //The new index is too far away, so avoid smooth scrolling to it.
                        mViewPager.setCurrentItem(newPosition, false);
                    }

                    //Reinit the seekbar update handler.
                    mHandler.post(seekbarUpdateRunnable);

                }

        	}
        		
        	//Updates the playback control buttons.
        	if (intent.hasExtra(Common.UPDATE_PLAYBACK_CONTROLS)) {
        		setPlayPauseButton();
        		setRepeatButtonIcon();
        		setShuffleButtonIcon();
        		
        	}
        	
            //Displays the audibook toast.
        	if (intent.hasExtra(Common.SHOW_AUDIOBOOK_TOAST))
        		displayAudiobookToast(Long.parseLong(
        							  bundle.getString(
        							  Common.SHOW_AUDIOBOOK_TOAST)));
        	
        	//Updates the duration of the SeekBar.
        	if (intent.hasExtra(Common.UPDATE_SEEKBAR_DURATION))
        		setSeekbarDuration(Integer.parseInt(
        						   bundle.getString(
        						   Common.UPDATE_SEEKBAR_DURATION)));
        	
        	//Hides the seekbar and displays the streaming progress bar.
        	if (intent.hasExtra(Common.SHOW_STREAMING_BAR)) {
        		mSeekbar.setVisibility(View.INVISIBLE);
        		mStreamingProgressBar.setVisibility(View.VISIBLE);
        		mHandler.removeCallbacks(seekbarUpdateRunnable);
        		
        	}
        	
        	//Shows the seekbar and hides the streaming progress bar.
        	if (intent.hasExtra(Common.HIDE_STREAMING_BAR)) {
        		mSeekbar.setVisibility(View.VISIBLE);
        		mStreamingProgressBar.setVisibility(View.INVISIBLE);
        		mHandler.postDelayed(seekbarUpdateRunnable, 100);
        	}
        	
        	//Updates the buffering progress on the seekbar.
        	if (intent.hasExtra(Common.UPDATE_BUFFERING_PROGRESS))
        		mSeekbar.setSecondaryProgress(Integer.parseInt(
        									  bundle.getString(
        									  Common.UPDATE_BUFFERING_PROGRESS)));

        	//Close this activity if the service is about to stop running.
        	if (intent.hasExtra(Common.SERVICE_STOPPING)) {
        		mHandler.removeCallbacks(seekbarUpdateRunnable);
        		finish();
        	}
        	
        }
        
    };
    
    /**
     * Sets the activity's theme based on user preferences.
     */
    private void setTheme() {
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            this.setTheme(R.style.AppThemeNoActionBar);
        } else {
            this.setTheme(R.style.AppThemeLightNoActionBar);
        }
        
    }
    
    /**
     * Initializes the view pager.
     */
    private void initViewPager() {

        try {
            mViewPager.setVisibility(View.INVISIBLE);
            mViewPagerAdapter = new PlaylistPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mViewPagerAdapter);
            mViewPager.setOffscreenPageLimit(0);
            mViewPager.setOnPageChangeListener(mPageChangeListener);
            mViewPager.setCurrentItem(mApp.getService().getCurrentSongIndex(), false);

            FadeAnimation fadeAnimation = new FadeAnimation(mViewPager, 600, 0.0f,
                                                            1.0f, new DecelerateInterpolator(2.0f));

            fadeAnimation.animate();

        } catch (IllegalStateException e) {
            /*
             * Catches any exceptions that may occur
             * as a result of the user rapidly changing
             * their device's orientation.
             */
        }

        //Delay loading extra fragments by 1000ms.
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mViewPager.setOffscreenPageLimit(10);
            }

        }, 1000);

    }

    /**
     * Initializes the current queue drawer/layout.
     */
    private void initDrawer() {
        //Load the current queue drawer.
        mQueueDrawerFragment = new QueueDrawerFragment();

        try {
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.queue_drawer, mQueueDrawerFragment)
                                       .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                       .commit();

        } catch (IllegalStateException e) {
            /*
             * Catches any exceptions that may occur if the
             * user rapidly changes the device's orientation.
             */
            e.printStackTrace();
        }

    }

    /**
     * Slides in the controls bar from the bottom along with a
     * slight rotation.
     */
    private void animateInControlsBar() {
        android.view.animation.TranslateAnimation slideUp =
                new android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 2.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f);
        slideUp.setDuration(300);
        slideUp.setInterpolator(new DecelerateInterpolator(2.0f));

        slideUp.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mControlsLayoutHeaderParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mControlsLayoutHeaderParent.startAnimation(slideUp);
    }
    
    /**
     * Scrolls the ViewPager programmatically. If dispatchToListener 
     * is true, USER_SCROLL will be set to true.
     */
    private void scrollViewPager(int newPosition, 
    							 boolean smoothScroll, 
    							 int velocity, 
    							 boolean dispatchToListener) {
    	
    	USER_SCROLL = dispatchToListener;
    	mViewPager.scrollToItem(newPosition, 
                                smoothScroll,
                                velocity,
                                dispatchToListener);
    	
    }

    /**
     * Sets the background for the control buttons based on the selected theme.
     */
    private void setControlButtonsBackground() {
        mControlsLayoutHeader.setBackgroundResource(UIElementsHelper.getGridViewCardBackground(mContext));
    }
    
    /**
     * Sets the play/pause button states.
     */
    private void setPlayPauseButton() {
        if (mApp.isServiceRunning()) {
            if (mApp.getService().isPlayingMusic()) {
                animatePlayToPause();
                stopSeekbarStrobeEffect();
            } else {
                animatePauseToPlay();
                initSeekbarStrobeEffect();
            }

        }

    }

    /**
     * Animates the play button to a pause button.
     */
    private void animatePlayToPause() {

        //Check to make sure the current icon is the play icon.
        if (mPlayPauseButton.getId()!=R.drawable.play_light)
            return;

        //Fade out the play button.
        final ScaleAnimation scaleOut = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                                                           mPlayPauseButton.getWidth()/2,
                                                           mPlayPauseButton.getHeight()/2);
        scaleOut.setDuration(150);
        scaleOut.setInterpolator(new AccelerateInterpolator());


        //Scale in the pause button.
        final ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                                                          mPlayPauseButton.getWidth()/2,
                                                          mPlayPauseButton.getHeight()/2);
        scaleIn.setDuration(150);
        scaleIn.setInterpolator(new DecelerateInterpolator());

        scaleOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setImageResource(R.drawable.pause_light);
                mPlayPauseButton.setPadding(0, 0, 0, 0);
                mPlayPauseButton.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        scaleIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setScaleX(1.0f);
                mPlayPauseButton.setScaleY(1.0f);
                mPlayPauseButton.setId(R.drawable.pause_light);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mPlayPauseButton.startAnimation(scaleOut);
    }

    /**
     * Animates the pause button to a play button.
     */
    private void animatePauseToPlay() {

        //Check to make sure the current icon is the pause icon.
        if (mPlayPauseButton.getId()!=R.drawable.pause_light)
            return;

        //Scale out the pause button.
        final ScaleAnimation scaleOut = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                                                           mPlayPauseButton.getWidth()/2,
                                                           mPlayPauseButton.getHeight()/2);
        scaleOut.setDuration(150);
        scaleOut.setInterpolator(new AccelerateInterpolator());


        //Scale in the play button.
        final ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                                                          mPlayPauseButton.getWidth()/2,
                                                          mPlayPauseButton.getHeight()/2);
        scaleIn.setDuration(150);
        scaleIn.setInterpolator(new DecelerateInterpolator());

        scaleOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setImageResource(R.drawable.play_light);
                mPlayPauseButton.setPadding(0, 0, -5, 0);
                mPlayPauseButton.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        scaleIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setScaleX(1.0f);
                mPlayPauseButton.setScaleY(1.0f);
                mPlayPauseButton.setId(R.drawable.play_light);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mPlayPauseButton.startAnimation(scaleOut);
    }

    /**
     * Sets the repeat button icon based on the current repeat mode.
     */
    private void setRepeatButtonIcon() {
    	if (mApp.isServiceRunning())
	    	if (mApp.getService().getRepeatMode()==Common.REPEAT_OFF) {
	    		mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
	    	} else if (mApp.getService().getRepeatMode()==Common.REPEAT_PLAYLIST) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_highlighted);
	    	} else if (mApp.getService().getRepeatMode()==Common.REPEAT_SONG) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_song);
	    	} else if (mApp.getService().getRepeatMode()==Common.A_B_REPEAT) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_song_range);
	    	}
    	
	    else
	    	mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
    	
    }
    
    /**
     * Sets the shuffle button icon based on the current shuffle mode.
     */
    private void setShuffleButtonIcon() {
    	if (mApp.isServiceRunning())
	        if (mApp.getService().isShuffleOn()==true) {
	        	mShuffleButton.setImageResource(R.drawable.shuffle_highlighted);
	        } else {
	        	mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
	        }
    	
    	else
    		mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
        
    }
    
    /**
     * Sets the seekbar's duration. Also updates the 
     * elapsed/remaining duration text.
     */
    private void setSeekbarDuration(int duration) {
    	mSeekbar.setMax(duration);
        mSeekbar.setProgress(mApp.getService().getCurrentMediaPlayer().getCurrentPosition()/1000);
    	mHandler.postDelayed(seekbarUpdateRunnable, 100);
    }
    
    /**
     * Sets the KitKat translucent status/nav bar and adjusts 
     * the views' boundaries.
     */
    private void setKitKatTranslucentBars() {
    	//KitKat translucent status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    		int statusBarHeight = Common.getStatusBarHeight(mContext);
            int navigationBarHeight = Common.getNavigationBarHeight(mContext);
            
            if (mDrawerParentLayout!=null) {
                mDrawerParentLayout.setClipToPadding(false);
                mDrawerParentLayout.setPadding(0, 0, 0, 0);
            }

            if (mControlsLayoutHeaderParent!=null) {
                int bottomPadding = mControlsLayoutHeaderParent.getPaddingBottom();
                mControlsLayoutHeaderParent.setClipToPadding(false);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mControlsLayoutHeaderParent.getLayoutParams();

                if (navigationBarHeight > 0) {
                    /* The nav bar already has padding, so remove the extra 15dp
                     * margin that was applied in the layout file.
                     */
                    params.bottomMargin = 0;
                }

                params.bottomMargin += navigationBarHeight;
                mControlsLayoutHeaderParent.setLayoutParams(params);

            }

        }
        
    }

    /**
     * Seekbar change listener.
     */
    private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int seekBarPosition, boolean changedByUser) {

            try {
                long currentSongDuration = mApp.getService().getCurrentMediaPlayer().getDuration();
                seekBar.setMax((int) currentSongDuration / 1000);

                if (changedByUser)
                    mSeekbarIndicatorText.setText(mApp.convertMillisToMinsSecs(seekBar.getProgress()*1000));
            } catch (Exception e) {
                e.printStackTrace();
            }

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mHandler.removeCallbacks(seekbarUpdateRunnable);
            mHandler.removeCallbacks(fadeOutSeekbarIndicator);

            mSeekbarIndicatorLayoutParent.setVisibility(View.VISIBLE);
            mSeekbarIndicatorLayout.setAlpha(0.8f);

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int seekBarPosition = seekBar.getProgress();
			mApp.getService().getCurrentMediaPlayer().seekTo(seekBarPosition*1000);

            //Reinitiate the handler.
            mHandler.post(seekbarUpdateRunnable);

            //Fade out the indicator after 1000ms.
            mHandler.postDelayed(fadeOutSeekbarIndicator, 1000);
			
		}
		
	};

    /**
     * Seekbar change indicator.
     */
    private Runnable fadeOutSeekbarIndicator = new Runnable() {

        @Override
        public void run() {
            FadeAnimation fadeOut = new FadeAnimation(mSeekbarIndicatorLayoutParent,
                                                      300, 0.9f, 0.0f, null);
            fadeOut.animate();
        }

    };
	
	/**
	 * Repeat button click listener.
	 */
	private OnClickListener repeatButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			
			mApp.getService().clearABRepeatRange();
			if (mApp.getService().getRepeatMode()==Common.REPEAT_OFF) {
				mRepeatButton.setImageResource(R.drawable.repeat_highlighted);
				mApp.getService().setRepeatMode(Common.REPEAT_PLAYLIST);
				
			} else if (mApp.getService().getRepeatMode()==Common.REPEAT_PLAYLIST) {
				mRepeatButton.setImageResource(R.drawable.repeat_song);
				mApp.getService().setRepeatMode(Common.REPEAT_SONG);
				
			} else {
				mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
				mApp.getService().setRepeatMode(Common.REPEAT_OFF);
				
			}
			
		}
		
	};

	/**
	 * Shuffle button click listener.
	 */
	private OnClickListener shuffleButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			//Toggle shuffle on/off.
			boolean shuffleOn = mApp.getService().toggleShuffleMode();
			
			if (shuffleOn)
				mShuffleButton.setImageResource(R.drawable.shuffle_highlighted);
			else
				mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
			
		}
		
	};
    
	/**
	 * Click listener for the play/pause button.
	 */
    private OnClickListener playPauseClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {

            //BZZZT! Give the user a brief haptic feedback touch response.
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            //Update the playback UI elements.
            if (mApp.getService().isPlayingMusic()) {
                animatePauseToPlay();
                mHandler.removeCallbacks(seekbarUpdateRunnable);
            } else {
                animatePlayToPause();
                mHandler.post(seekbarUpdateRunnable);
            }

            /*
             * Toggle the playback state in a separate thread. This
             * will allow the play/pause button animation to remain
             * buttery smooth.
             */
            new AsyncTask() {

                @Override
                protected Object doInBackground(Object[] params) {
                    mApp.getService().togglePlaybackState();
                    return null;
                }

            }.execute();

		}
		
	};
	
	/**
	 * Click listener for the previous button.
	 */
	private OnClickListener mOnClickPreviousListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {

            //Remove the seekbar update runnable.
            mHandler.removeCallbacks(seekbarUpdateRunnable);

			/*
			 * Scrolling the pager will automatically call the skipToTrack() method. 
			 * Since we're passing true for the dispatchToListener parameter, the 
			 * onPageSelected() listener will receive a callback once the scrolling 
			 * animation completes. This has the side-benefit of letting the animation 
			 * finish before starting playback (keeps the animation buttery smooth).
			 */
			int newPosition = mViewPager.getCurrentItem() - 1;
			if (newPosition > -1) {
				scrollViewPager(newPosition, true, 1, true);
			} else {
				mViewPager.setCurrentItem(0, false);
			}
			
		}
		
	};
    
	/**
	 * Click listener for the next button.
	 */
	private OnClickListener mOnClickNextListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {

            //Remove the seekbar update runnable.
            mHandler.removeCallbacks(seekbarUpdateRunnable);

			/*
			 * Scrolling the pager will automatically call the skipToTrack() method. 
			 * Since we're passing true for the dispatchToListener parameter, the 
			 * onPageSelected() listener will receive a callback once the scrolling 
			 * animation completes. This has the side-benefit of letting the animation 
			 * finish before starting playback (keeps the animation buttery smooth).
			 */
			int newPosition = mViewPager.getCurrentItem() + 1;
			if (newPosition < mViewPagerAdapter.getCount()) {
				scrollViewPager(newPosition, true, 1, true);
			} else {
				if (mApp.getService().getRepeatMode()==Common.REPEAT_PLAYLIST)
					mViewPager.setCurrentItem(0, false);
				else
					Toast.makeText(mContext, R.string.no_songs_to_skip_to, Toast.LENGTH_SHORT).show();
			}

			//mApp.getService().skipToNextTrack();
			
		}
		
	};

	/**
	 * Downloads a GMusic song for local playback.
	 */
    /*private void pinSong() {
    	
    	//Check if the app is getting pinned songs from the official GMusic app.
		if (mApp.isFetchingPinnedSongs()==false) {
			
			//Retrieve the name of the song.
			mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
			
			//Get the song's ID/title.
			String songID = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ID));
			String songTitle = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE));

    		//Check if a local copy of the song exists.
    		String localCopyPath = mApp.getDBAccessHelper().getLocalCopyPath(songID);
    		
			if (localCopyPath!=null) {
    			if (localCopyPath.isEmpty() || localCopyPath.equals("")) {
    				if (mMenu!=null) {
    					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_highlighted);
    				}
    				
    				//Get a mCursor with the current song and initiate the download process.
    				String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
    				mApp.queueSongsToPin(false, false, selection);
    				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + songTitle + ".";
					Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
    			} else {
    				if (mMenu!=null) {
    					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_light);
    				}
    				
    	    		String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
    	    		AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, selection, null);
    	    		task.execute();
    			}
    		} else {
				if (mMenu!=null) {
					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_highlighted);
				}
    			
				//Get a mCursor with the current song and initiate the download process.
    			String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
				mApp.queueSongsToPin(false, false, selection);
    		}
			
		} else {
			Toast.makeText(mContext, R.string.wait_until_pinning_complete, Toast.LENGTH_SHORT).show();
		}
		
    }*/

    /**
     * Drawer open/close listener.
     */
    private DrawerListener mDrawerListener = new DrawerListener() {

		@Override
		public void onDrawerClosed(View drawer) {
			if (mQueueDrawerFragment!=null &&
                drawer==mCurrentQueueLayout) {
                mQueueDrawerFragment.setIsDrawerOpen(false);
            }
			
		}

		@Override
		public void onDrawerOpened(View drawer) {
            if (mQueueDrawerFragment!=null &&
                drawer==mCurrentQueueLayout) {
                mQueueDrawerFragment.setIsDrawerOpen(true);
            }
			
		}

		@Override
		public void onDrawerSlide(View drawer, float arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			// TODO Auto-generated method stub
			
		}
    	
    };

    /**
     * Provides callback methods when the ViewPager's position/current page has changed.
     */
    private VelocityViewPager.OnPageChangeListener mPageChangeListener = new VelocityViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int scrollState) {
			if (scrollState==VelocityViewPager.SCROLL_STATE_DRAGGING)
				USER_SCROLL = true;
			
		}

		@Override
		public void onPageScrolled(final int pagerPosition, float swipeVelocity, int offsetFromCurrentPosition) {
			
			/* swipeVelocity determines whether the viewpager has finished scrolling or not.
			 * Throw in an if statement that only allows the track to change when
			 * swipeVelocity is 0 (which means the page is done scrolling). This ensures
			 * that the tracks don't jump around or get truncated while the user is 
			 * swiping between different pages.
			 */

			if (mApp.isServiceRunning() && mApp.getService().getCursor().getCount()!=1) {
				
				/* Change tracks ONLY when the user has finished the swiping gesture (swipeVelocity will be zero).
				 * Also, don't skip tracks if the new pager position is the same as the current mCursor position (indicates 
				 * that the starting and ending position of the pager is the same).
				 */
				if (swipeVelocity==0.0f && pagerPosition!=mApp.getService().getCurrentSongIndex()) {
					if (USER_SCROLL) {
                        mHandler.removeCallbacks(seekbarUpdateRunnable);
                        smoothScrollSeekbar(0);

                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mApp.getService().skipToTrack(pagerPosition);
                            }

                        }, 200);

					}

				}
				
			}
			
		}

		@Override
		public void onPageSelected(int newPosition) {
            //TODO Auto-generated method stub.

		}
		
	};
    
    /**
     * @deprecated
     * Applies the correct transformer effect to the ViewPager.
     */
   @Deprecated
	private void setPlaylistPagerAnimation() {
    	if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==0) {
            mViewPager.setPageTransformer(true, new ZoomOutPageTransformer(0.85f));
    	} else if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==1) {
    		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer(0.85f));
    	} else if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==2) {
            mViewPager.setPageTransformer(true, new ZoomOutPageTransformer(0.85f));
    	}

    }
	
    /**
     * Create a new Runnable to update the seekbar and time every 100ms.
     */
    public Runnable seekbarUpdateRunnable = new Runnable() {
    	
    	public void run() {
    		
    		try {
                long currentPosition = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
                int currentPositionInSecs = (int) currentPosition/1000;
                smoothScrollSeekbar(currentPositionInSecs);

                //mSeekbar.setProgress(currentPositionInSecs);
                mHandler.postDelayed(seekbarUpdateRunnable, 100);

    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    	}
    	
    };

    /**
     * Smoothly scrolls the seekbar to the indicated position.
     */
    private void smoothScrollSeekbar(int progress) {
        ObjectAnimator animation = ObjectAnimator.ofInt(mSeekbar, "progress", progress);
        animation.setDuration(200);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

    }

    /**
     * Initiates the strobe effect on the seekbar.
     */
    private void initSeekbarStrobeEffect() {
        mSeekbarStrobeAnim = new AlphaAnimation(1.0f, 0.0f);
        mSeekbarStrobeAnim.setRepeatCount(SEEKBAR_STROBE_ANIM_REPEAT);
        mSeekbarStrobeAnim.setDuration(700);
        mSeekbarStrobeAnim.setRepeatMode(Animation.REVERSE);

        mSeekbar.startAnimation(mSeekbarStrobeAnim);

    }

    /**
     * Stops the seekbar strobe effect.
     */
    private void stopSeekbarStrobeEffect() {
        mSeekbarStrobeAnim = new AlphaAnimation(mSeekbar.getAlpha(), 1.0f);
        mSeekbarStrobeAnim.setDuration(700);
        mSeekbar.startAnimation(mSeekbarStrobeAnim);
        
    }
    
    public class PlaylistPagerAdapter extends FragmentStatePagerAdapter {

        public PlaylistPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
          
        }

        //This method controls the layout that is shown on each screen.
        @Override
        public Fragment getItem(int position) {

        	/* PlaylistPagerFragment.java will be shown on every pager screen. However, 
        	 * the fragment will check which screen (position) is being shown, and will
        	 * update its TextViews and ImageViews to match the song that's being played. */
    		Fragment fragment = new PlaylistPagerFragment();
    		
    		Bundle bundle = new Bundle();
    		bundle.putInt("POSITION", position);
    		fragment.setArguments(bundle);
    		return fragment;

        }

        @Override
        public int getCount() {
        	
        	try {
            	if (mApp.getService().getPlaybackIndecesList()!=null) {
            		return mApp.getService().getPlaybackIndecesList().size();
            	} else {
            		mApp.getService().stopSelf();
            		return 0;
            	}
        	} catch (Exception e) {
        		e.printStackTrace();
        		return 0;
        	}

        }

    }
	
	/**
	 * Displays the "Resuming from xx:xx" toast.
	 */
	public void displayAudiobookToast(long resumePlaybackPosition) {
		try {
			String resumingFrom = mContext.getResources().getString(R.string.resuming_from) 
								+ " " + mApp.convertMillisToMinsSecs(resumePlaybackPosition) + ".";
			
			Toast.makeText(mContext, resumingFrom, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

    /**
     * Toggles the open/closed state of the current queue drawer.
     */
	public void toggleCurrentQueueDrawer() {
        if (mDrawerLayout==null)
            return;

        if (mDrawerLayout.isDrawerOpen(Gravity.END))
            mDrawerLayout.closeDrawer(Gravity.END);
        else
            mDrawerLayout.openDrawer(Gravity.END);

    }

	/*
	 * Getter and setter methods.
	 */
	
	public SeekBar getSeekbar() {
		return mSeekbar;
	}
	
	public ProgressBar getStreamingProgressBar() {
		return mStreamingProgressBar;
	}
	
	public VelocityViewPager getPlaylistViewPager() {
		return mViewPager;
	}

	public NowPlayingActivityListener getNowPlayingActivityListener() {
		return mNowPlayingActivityListener;
	}
	
	public void setNowPlayingActivityListener(NowPlayingActivityListener listener) {
		mNowPlayingActivityListener = listener;
	}
	
	/**
	 * Interface that provides callbacks once this activity is 
	 * up and running.
	 */
	public interface NowPlayingActivityListener {
		
		/**
		 * Called once this activity's onResume() method finishes 
		 * executing.
		 */
		public void onNowPlayingActivityReady();
		
	}

	@Override
	public void onResume() {
		super.onResume();

        if (mIsCreating==false) {
            setKitKatTranslucentBars();
            mHandler.postDelayed(seekbarUpdateRunnable, 100);
            mIsCreating = false;
        }

        //Animate the controls bar in.
        //animateInControlsBar();
        //Update the seekbar.
        try {
            setSeekbarDuration(mApp.getService().getCurrentMediaPlayer().getDuration()/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Load the drawer 1000ms after the activity is loaded.
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                initDrawer();
            }

        }, 1000);

		if (getIntent().hasExtra(START_SERVICE) &&
			getNowPlayingActivityListener()!=null) {
			getNowPlayingActivityListener().onNowPlayingActivityReady();

			/**
			 * To prevent the service from being restarted every time this
			 * activity is resume, we're gonna have to remove the "START_SERVICE"
			 * extra from the intent.
			 */
			getIntent().removeExtra(START_SERVICE);

		}

	}

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing())
            mApp.setNowPlayingActivity(null);

    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	//Initialize the broadcast manager that will listen for track changes.
    	LocalBroadcastManager.getInstance(mContext)
		 					 .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
    	
    	/* Check if the service is up and running. If so, send out a broadcast message 
    	 * that will initialize this activity fully. This code block is what will 
    	 * initialize this activity fully if it is opened after the service is already 
    	 * up and running (the onServiceRunning() callback isn't available at this point).
    	 */
    	if (mApp.isServiceRunning() && mApp.getService().getCursor()!=null) {
    		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION, 
    											  Common.UPDATE_SEEKBAR_DURATION, 
    											  Common.HIDE_STREAMING_BAR, 
    											  Common.INIT_PAGER, 
    											  Common.UPDATE_PLAYBACK_CONTROLS, 
    											  Common.UPDATE_EQ_FRAGMENT };
    		
        	String[] flagValues = new String[] { "" + mApp.getService().getCurrentSongIndex(), 
        										 "" + mApp.getService().getCurrentMediaPlayer().getDuration(), 
        										 "", "", "", "" };
        	mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    	}
    	
    }
    
    @Override
    public void onStop() {
    	//Unregister the broadcast receivers.
    	LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    	super.onStop();
    	
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putBoolean("CALLED_FROM_FOOTER", true);
    	savedInstanceState.putBoolean("CALLED_FROM_NOTIF", true);
    }
	
}
