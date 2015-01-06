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
package com.aniruddhc.acemusic.player.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncGetSongStreamURLTask;
import com.aniruddhc.acemusic.player.BroadcastReceivers.HeadsetButtonsReceiver;
import com.aniruddhc.acemusic.player.BroadcastReceivers.HeadsetPlugBroadcastReceiver;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.AudioManagerHelper;
import com.aniruddhc.acemusic.player.Helpers.EqualizerHelper;
import com.aniruddhc.acemusic.player.Helpers.SongHelper;
import com.aniruddhc.acemusic.player.PlaybackKickstarter.PlaybackKickstarter.BuildCursorListener;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.RemoteControlClient.RemoteControlClientCompat;
import com.aniruddhc.acemusic.player.RemoteControlClient.RemoteControlHelper;
import com.aniruddhc.acemusic.player.Scrobbling.ScrobbleDroidHelper;
import com.aniruddhc.acemusic.player.Scrobbling.SimpleLastFMHelper;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.aniruddhc.acemusic.player.WidgetProviders.AlbumArtWidgetProvider;
import com.aniruddhc.acemusic.player.WidgetProviders.BlurredWidgetProvider;
import com.aniruddhc.acemusic.player.WidgetProviders.LargeWidgetProvider;
import com.aniruddhc.acemusic.player.WidgetProviders.SmallWidgetProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The meat and potatoes of the entire app. Manages 
 * playback, equalizer effects, and all other audio 
 * related operations.
 * 
 * @author Saravan Pantham
 */
public class AudioPlaybackService extends Service {
	
	//Context and Intent.
	private Context mContext;
	private Service mService;
	
	//Global Objects Provider.
	private Common mApp;
	
	//PrepareServiceListener instance.
	private PrepareServiceListener mPrepareServiceListener;
	
	//MediaPlayer objects and flags.
	private MediaPlayer mMediaPlayer;
	private MediaPlayer mMediaPlayer2;
	private int mCurrentMediaPlayer = 1;
	private boolean mFirstRun = true;
	
	//AudioManager.
	private AudioManager mAudioManager;
	private AudioManagerHelper mAudioManagerHelper;
	
	//Flags that indicate whether the mediaPlayers have been initialized.
	private boolean mMediaPlayerPrepared = false;
	private boolean mMediaPlayer2Prepared = false;
	
	//Cursor object(s) that will guide the rest of this queue.
	private Cursor mCursor;
	private MergeCursor mMergeCursor;
	
	//Holds the indeces of the current cursor, in the order that they'll be played.
	private ArrayList<Integer> mPlaybackIndecesList = new ArrayList<Integer>();
	
	//Holds the indeces of songs that were unplayable.
	private ArrayList<Integer> mFailedIndecesList = new ArrayList<Integer>();
	
	//Song data helpers for each MediaPlayer object.
	private SongHelper mMediaPlayerSongHelper;
	private SongHelper mMediaPlayer2SongHelper;

	//Pointer variable.
	private int mCurrentSongIndex;

	//Equalizer/Audio FX helpers.
	private EqualizerHelper mEqualizerHelper;
	
	//Notification elements.
	private NotificationCompat.Builder mNotificationBuilder;
	public static final int mNotificationId = 1080; //NOTE: Using 0 as a notification ID causes Android to ignore the notification call.
	
	//Custom actions for media player controls via the notification bar.
    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.aniruddhc.acemusic.player.LAUNCH_NOW_PLAYING_ACTION";
	public static final String PREVIOUS_ACTION = "com.aniruddhc.acemusic.player.PREVIOUS_ACTION";
	public static final String PLAY_PAUSE_ACTION = "com.aniruddhc.acemusic.player.PLAY_PAUSE_ACTION";
	public static final String NEXT_ACTION = "com.aniruddhc.acemusic.player.NEXT_ACTION";
	public static final String STOP_SERVICE = "com.aniruddhc.acemusic.player.STOP_SERVICE";
	
	//Indicates if an enqueue/queue reordering operation was performed on the original queue.
	private boolean mEnqueuePerformed = false;
	
	//Handler object.
	private Handler mHandler;
	
	//Volume variables that handle the crossfade effect.
	private float mFadeOutVolume = 1.0f;
	private float mFadeInVolume = 1.0f;
	
	//Headset plug receiver.
	private HeadsetPlugBroadcastReceiver mHeadsetPlugReceiver;
	
	//Crossfade.
	private int mCrossfadeDuration;
	
	//A-B Repeat variables.
	private int mRepeatSongRangePointA = 0;
	private int mRepeatSongRangePointB = 0;
	
	//Indicates if the user changed the track manually.
	private boolean mTrackChangedByUser = false;
	
	//RemoteControlClient for use with remote controls and ICS+ lockscreen controls.
	private RemoteControlClientCompat mRemoteControlClientCompat;
	private ComponentName mMediaButtonReceiverComponent;
	
	//Enqueue reorder scalar.
	private int mEnqueueReorderScalar = 0;
	
	//Temp placeholder for GMusic Uri.
	public static final Uri URI_BEING_LOADED = Uri.parse("uri_being_loaded");
	
	//Google Analytics.
	private GoogleAnalytics mGAInstance;
	private Tracker mTracker;
	private long mServiceStartTime;
	
	/**
	 * Constructor that should be used whenever this 
	 * service is being explictly created.
	 * 
	 * @param context The context being passed in.
	 */
	public AudioPlaybackService(Context context) {
		mContext = context;
	}
	
	/**
	 * Empty constructor. Required if a custom constructor 
	 * was explicitly declared (see above).
	 */
	public AudioPlaybackService() {
		super();
	}

	/**
	 * Prepares the MediaPlayer objects for first use 
	 * and starts the service. The workflow of the entire 
	 * service starts here.
	 * 
	 * @param intent Calling intent.
	 * @param flags Service flags.
	 * @param startId Service start ID.
	 */
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
		//Context.
		mContext = getApplicationContext();
		mService = this;
		mHandler = new Handler();
		
        mApp = (Common) getApplicationContext();
        mApp.setService((AudioPlaybackService) this);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
  
        //Initialize Google Analytics.
        initGoogleAnalytics();
        
        //Initialize the MediaPlayer objects.
        initMediaPlayers();
		
		//Time to play nice with other music players (and audio apps) and request audio focus.
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManagerHelper = new AudioManagerHelper();
		
		// Request audio focus for playback
		mAudioManagerHelper.setHasAudioFocus(requestAudioFocus());

        //Grab the crossfade duration for this session.
        mCrossfadeDuration = mApp.getCrossfadeDuration();

		//Initialize audio effects (equalizer, virtualizer, bass boost) for this session.
		initAudioFX();
		
	    mMediaButtonReceiverComponent = new ComponentName(this.getPackageName(), HeadsetButtonsReceiver.class.getName());
	    mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
	    
	    //if (mApp.getSharedPreferences().getBoolean(Common.SHOW_LOCKSCREEN_CONTROLS, true)==true) {
	    //	initRemoteControlClient();
	    //}
	    
		mApp.getPlaybackKickstarter().setBuildCursorListener(buildCursorListener);
		
		//The service has been successfully started.
		setPrepareServiceListener(mApp.getPlaybackKickstarter());
	    getPrepareServiceListener().onServiceRunning(this);
	    
		return START_STICKY;
	}
	
	/**
	 * Public interface that provides access to 
	 * major events during the service startup 
	 * process.
	 * 
	 * @author Saravan Pantham
	 */
	public interface PrepareServiceListener {

		/**
		 * Called when the service is up and running.
		 */
		public void onServiceRunning(AudioPlaybackService service);
		
		/**
		 * Called when the service failed to start. 
		 * Also returns the failure reason via the exception 
		 * parameter.
		 */
		public void onServiceFailed(Exception exception);
	
	}
	
	/**
	 * Initializes Google Analytics.
	 */
	private void initGoogleAnalytics() {
        try {
        	if (mApp.isGoogleAnalyticsEnabled()) {
        		String gaTrackingId = getResources().getString(R.string.ga_trackingId);
            	mServiceStartTime = System.currentTimeMillis();
            	
            	mGAInstance = GoogleAnalytics.getInstance(getApplicationContext());
                mTracker = mGAInstance.getTracker(gaTrackingId);
                
                mTracker.set(Fields.SESSION_CONTROL, "start");
                mTracker.send(MapBuilder.createEvent("Jams Service", 
    					 							 "Service started!", 
    					 							 "User is playing music.", 
    					 							 null).build());
        	}

        } catch (Exception e) {
        	e.printStackTrace();
        }
        
	}

	/**
	 * Initializes remote control clients for this service session. 
	 * Currently used for lockscreen controls.
     * 	public void initRemoteControlClient() {
     if (mRemoteControlClientCompat==null) {
     Intent remoteControlIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
     remoteControlIntent.setComponent(mMediaButtonReceiverComponent);

     mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(mContext, 0, remoteControlIntent, 0));
     RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);

     }

     mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
     mRemoteControlClientCompat.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
     RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
     RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
     RemoteControlClient.FLAG_KEY_MEDIA_STOP |
     RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);

     }
	 */


	/**
	 * Initializes the MediaPlayer objects for this service session.
	 */
	private void initMediaPlayers() {
		
		/*
		 * Release the MediaPlayer objects if they are still valid.
		 */
		if (mMediaPlayer!=null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		
		if (mMediaPlayer2!=null) {
			getMediaPlayer2().release();
			mMediaPlayer2 = null;
		}
		
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer2 = new MediaPlayer();
		setCurrentMediaPlayer(1);
		
		getMediaPlayer().reset();
		getMediaPlayer2().reset();
		
		//Loop the players if the repeat mode is set to repeat the current song.
		if (getRepeatMode()==Common.REPEAT_SONG) {
			getMediaPlayer().setLooping(true);
			getMediaPlayer2().setLooping(true);
		}
		
		try {
			mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
			getMediaPlayer2().setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
		} catch (Exception e) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer2 = new MediaPlayer();
			setCurrentMediaPlayer(1);
		}

		//Set the mediaPlayers' stream sources.
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		getMediaPlayer2().setAudioStreamType(AudioManager.STREAM_MUSIC);
		
	}
	
	/**
	 * Initializes the list of pointers to each cursor row.
	 */
	private void initPlaybackIndecesList(boolean playAll) {
		if (getCursor()!=null && getPlaybackIndecesList()!=null) {
			getPlaybackIndecesList().clear();
			for (int i=0; i < getCursor().getCount(); i++) {
				getPlaybackIndecesList().add(i);
			}
			
			if (isShuffleOn() && !playAll) {
                //Build a new list that doesn't include the current song index.
                ArrayList<Integer> newList = new ArrayList<Integer>(getPlaybackIndecesList());
                newList.remove(getCurrentSongIndex());

               //Shuffle the new list.
               Collections.shuffle(newList, new Random(System.nanoTime()));

                //Plug in the current song index back into the new list.
                newList.add(getCurrentSongIndex(), getCurrentSongIndex());
                mPlaybackIndecesList = newList;

			} else if (isShuffleOn() && playAll) {
                //Shuffle all elements.
                Collections.shuffle(getPlaybackIndecesList(), new Random(System.nanoTime()));
            }
			
		} else {
			stopSelf();
		}
		
	}
	
	/**
	 * Requests AudioFocus from the OS.
	 * 
	 * @return True if AudioFocus was gained. False, otherwise.
	 */
	private boolean requestAudioFocus() {
		int result = mAudioManager.requestAudioFocus(audioFocusChangeListener,
     												AudioManager.STREAM_MUSIC,
     												AudioManager.AUDIOFOCUS_GAIN);

		if (result!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			//Stop the service.
			mService.stopSelf();
			Toast.makeText(mContext, R.string.close_other_audio_apps, Toast.LENGTH_LONG).show();
			return false;
		} else {
			return true;
		}
		
	}
	
	/**
	 * Initializes the equalizer and audio effects for this service session.
	 */
	public void initAudioFX() {

		try {	
	    	//Instatiate the equalizer helper object.
			mEqualizerHelper = new EqualizerHelper(mContext, mMediaPlayer.getAudioSessionId(), 
												   getMediaPlayer2().getAudioSessionId(), 
												   mApp.isEqualizerEnabled());

		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			mEqualizerHelper.setIsEqualizerSupported(false);
		} catch (Exception e) {
			e.printStackTrace();
			mEqualizerHelper.setIsEqualizerSupported(false);
		}
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Retrieves the EQ values for mMediaPlayer's current song and 
	 * applies them to the EQ engine.
	 * 
	 * @param songId The id of the song that mMediaPlayer is current handling.
	 */
	private void applyMediaPlayerEQ(String songId) {
		
		if (mEqualizerHelper==null)
			return;
		
		short fiftyHertzBand = mEqualizerHelper.getEqualizer().getBand(50000);
		short oneThirtyHertzBand = mEqualizerHelper.getEqualizer().getBand(130000);
		short threeTwentyHertzBand = mEqualizerHelper.getEqualizer().getBand(320000);
		short eightHundredHertzBand = mEqualizerHelper.getEqualizer().getBand(800000);
		short twoKilohertzBand = mEqualizerHelper.getEqualizer().getBand(2000000);
		short fiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(5000000);
		short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(9000000);

		//Get the equalizer/audioFX settings for this specific song.
		int[] eqValues = mApp.getDBAccessHelper().getSongEQValues(songId);

		//50Hz Band.
		if (eqValues[0]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) 0);
		} else if (eqValues[0] < 16) {
			
			if (eqValues[0]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) (-(16-eqValues[0])*100));
			}
			
		} else if (eqValues[0] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) ((eqValues[0]-16)*100));
		}
		
		//130Hz Band.
		if (eqValues[1]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) 0);
		} else if (eqValues[1] < 16) {
			
			if (eqValues[1]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) (-(16-eqValues[1])*100));
			}
			
		} else if (eqValues[1] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) ((eqValues[1]-16)*100));
		}
		
		//320Hz Band.
		if (eqValues[2]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) 0);
		} else if (eqValues[2] < 16) {
			
			if (eqValues[2]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) (-(16-eqValues[2])*100));
			}
			
		} else if (eqValues[2] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) ((eqValues[2]-16)*100));
		}
		
		//800Hz Band.
		if (eqValues[3]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) 0);
		} else if (eqValues[3] < 16) {
			
			if (eqValues[3]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) (-(16-eqValues[3])*100));
			}
			
		} else if (eqValues[3] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) ((eqValues[3]-16)*100));
		}
		
		//2kHz Band.
		if (eqValues[4]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) 0);
		} else if (eqValues[4] < 16) {
			
			if (eqValues[4]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) (-(16-eqValues[4])*100));
			}
			
		} else if (eqValues[4] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) ((eqValues[4]-16)*100));
		}
		
		//5kHz Band.
		if (eqValues[5]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) 0);
		} else if (eqValues[5] < 16) {
			
			if (eqValues[5]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) (-(16-eqValues[5])*100));
			}
			
		} else if (eqValues[5] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) ((eqValues[5]-16)*100));
		}
		
		//12.5kHz Band.
		if (eqValues[6]==16) {
			mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) 0);
		} else if (eqValues[6] < 16) {
			
			if (eqValues[6]==0) {
				mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) (-(16-eqValues[6])*100));
			}
			
		} else if (eqValues[6] > 16) {
			mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) ((eqValues[6]-16)*100));
		}
		
		//Set the audioFX values.
		mEqualizerHelper.getVirtualizer().setStrength((short) eqValues[7]);
		mEqualizerHelper.getBassBoost().setStrength((short) eqValues[8]);
		
		if (eqValues[9]==0) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_NONE);
		} else if (eqValues[9]==1) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
		} else if (eqValues[9]==2) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
		} else if (eqValues[9]==3) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
		} else if (eqValues[9]==4) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
		} else if (eqValues[9]==5) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
		} else if (eqValues[9]==6) {
			mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_PLATE);
		}
		
	}
	
	/**
	 * Retrieves the EQ values for mMediaPlayer2's current song and 
	 * applies them to the EQ engine.
	 * 
	 * @param songId The id of the song that mMediaPlayer is current handling.
	 */
	private void applyMediaPlayer2EQ(String songId) {

		if (mEqualizerHelper==null)
			return;
		
		short fiftyHertzBand = mEqualizerHelper.getEqualizer2().getBand(50000);
		short oneThirtyHertzBand = mEqualizerHelper.getEqualizer2().getBand(130000);
		short threeTwentyHertzBand = mEqualizerHelper.getEqualizer2().getBand(320000);
		short eightHundredHertzBand = mEqualizerHelper.getEqualizer2().getBand(800000);
		short twoKilohertzBand = mEqualizerHelper.getEqualizer2().getBand(2000000);
		short fiveKilohertzBand = mEqualizerHelper.getEqualizer2().getBand(5000000);
		short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer2().getBand(9000000);

		//Get the mEqualizerHelper.getEqualizer()/audioFX settings for this specific song.
		int[] eqValues = mApp.getDBAccessHelper().getSongEQValues(songId);
		
		//50Hz Band.
		if (eqValues[0]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(fiftyHertzBand, (short) 0);
		} else if (eqValues[0] < 16) {
			
			if (eqValues[0]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(fiftyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(fiftyHertzBand, (short) (-(16-eqValues[0])*100));
			}
			
		} else if (eqValues[0] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(fiftyHertzBand, (short) ((eqValues[0]-16)*100));
		}
		
		//130Hz Band.
		if (eqValues[1]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(oneThirtyHertzBand, (short) 0);
		} else if (eqValues[1] < 16) {
			
			if (eqValues[1]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(oneThirtyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(oneThirtyHertzBand, (short) (-(16-eqValues[1])*100));
			}
			
		} else if (eqValues[1] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(oneThirtyHertzBand, (short) ((eqValues[1]-16)*100));
		}
		
		//320Hz Band.
		if (eqValues[2]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(threeTwentyHertzBand, (short) 0);
		} else if (eqValues[2] < 16) {
			
			if (eqValues[2]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(threeTwentyHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(threeTwentyHertzBand, (short) (-(16-eqValues[2])*100));
			}
			
		} else if (eqValues[2] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(threeTwentyHertzBand, (short) ((eqValues[2]-16)*100));
		}
		
		//800Hz Band.
		if (eqValues[3]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(eightHundredHertzBand, (short) 0);
		} else if (eqValues[3] < 16) {
			
			if (eqValues[3]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(eightHundredHertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(eightHundredHertzBand, (short) (-(16-eqValues[3])*100));
			}
			
		} else if (eqValues[3] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(eightHundredHertzBand, (short) ((eqValues[3]-16)*100));
		}
		
		//2kHz Band.
		if (eqValues[4]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(twoKilohertzBand, (short) 0);
		} else if (eqValues[4] < 16) {
			
			if (eqValues[4]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(twoKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(twoKilohertzBand, (short) (-(16-eqValues[4])*100));
			}
			
		} else if (eqValues[4] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(twoKilohertzBand, (short) ((eqValues[4]-16)*100));
		}
		
		//5kHz Band.
		if (eqValues[5]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(fiveKilohertzBand, (short) 0);
		} else if (eqValues[5] < 16) {
			
			if (eqValues[5]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(fiveKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(fiveKilohertzBand, (short) (-(16-eqValues[5])*100));
			}
			
		} else if (eqValues[5] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(fiveKilohertzBand, (short) ((eqValues[5]-16)*100));
		}
		
		//12.5kHz Band.
		if (eqValues[6]==16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(twelvePointFiveKilohertzBand, (short) 0);
		} else if (eqValues[6] < 16) {
			
			if (eqValues[6]==0) {
				mEqualizerHelper.getEqualizer2().setBandLevel(twelvePointFiveKilohertzBand, (short) -1500);
			} else {
				mEqualizerHelper.getEqualizer2().setBandLevel(twelvePointFiveKilohertzBand, (short) (-(16-eqValues[6])*100));
			}
			
		} else if (eqValues[6] > 16) {
			mEqualizerHelper.getEqualizer2().setBandLevel(twelvePointFiveKilohertzBand, (short) ((eqValues[6]-16)*100));
		}
		
		//Set the audioFX values.
		mEqualizerHelper.getVirtualizer2().setStrength((short) eqValues[7]);
		mEqualizerHelper.getBassBoost2().setStrength((short) eqValues[8]);
		
		if (eqValues[9]==0) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_NONE);
		} else if (eqValues[9]==1) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_LARGEHALL);
		} else if (eqValues[9]==2) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_LARGEROOM);
		} else if (eqValues[9]==3) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_MEDIUMHALL);
		} else if (eqValues[9]==4) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_MEDIUMROOM);
		} else if (eqValues[9]==5) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_SMALLROOM);
		} else if (eqValues[9]==6) {
			mEqualizerHelper.getReverb2().setPreset(PresetReverb.PRESET_PLATE);
		}
		
	}
	
	/**
	 * Builds and returns a fully constructed Notification for devices 
	 * on Jelly Bean and above (API 16+).
	 */
	@SuppressLint("NewApi") 
	private Notification buildJBNotification(SongHelper songHelper) {
		mNotificationBuilder = new NotificationCompat.Builder(mContext);
		mNotificationBuilder.setOngoing(true);
		mNotificationBuilder.setAutoCancel(false);
		mNotificationBuilder.setSmallIcon(R.drawable.notif_icon);
		
		//Open up the player screen when the user taps on the notification.
        Intent launchNowPlayingIntent = new Intent();
        launchNowPlayingIntent.setAction(AudioPlaybackService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, launchNowPlayingIntent, 0);
        mNotificationBuilder.setContentIntent(launchNowPlayingPendingIntent);
		
        //Grab the notification layouts.
		RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_layout);
		RemoteViews expNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_expanded_layout);
		
		//Initialize the notification layout buttons.
    	Intent previousTrackIntent = new Intent();
    	previousTrackIntent.setAction(AudioPlaybackService.PREVIOUS_ACTION);
    	PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, previousTrackIntent, 0);
    	
    	Intent playPauseTrackIntent = new Intent();
    	playPauseTrackIntent.setAction(AudioPlaybackService.PLAY_PAUSE_ACTION);
    	PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, playPauseTrackIntent, 0);
    	
    	Intent nextTrackIntent = new Intent();
    	nextTrackIntent.setAction(AudioPlaybackService.NEXT_ACTION);
    	PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, nextTrackIntent, 0);
    	
    	Intent stopServiceIntent = new Intent();
    	stopServiceIntent.setAction(AudioPlaybackService.STOP_SERVICE);
    	PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, stopServiceIntent, 0);
		
    	//Check if audio is playing and set the appropriate play/pause button.
    	if (mApp.getService().isPlayingMusic()) {
			notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_pause_light);
        	expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
		} else {
			notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_play_light);
        	expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
		}
    	
    	//Set the notification content.
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, songHelper.getTitle());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, songHelper.getArtist());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_three, songHelper.getAlbum());
        
        notificationView.setTextViewText(R.id.notification_base_line_one, songHelper.getTitle());
        notificationView.setTextViewText(R.id.notification_base_line_two, songHelper.getArtist());
        
        //Set the states of the next/previous buttons and their pending intents.
        if (mApp.getService().isOnlySongInQueue()) {
        	//This is the only song in the queue, so disable the previous/next buttons.
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.INVISIBLE);
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.INVISIBLE);
        	expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
        	
        	notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
        	
        } else if (mApp.getService().isFirstSongInQueue()) {
        	//This is the the first song in the queue, so disable the previous button.
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.INVISIBLE);
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);
        	expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
            
            notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        	
        } else if (mApp.getService().isLastSongInQueue()) {
        	//This is the last song in the cursor, so disable the next button.
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.VISIBLE);
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.INVISIBLE);
        	expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
            
            notificationView.setViewVisibility(R.id.notification_base_previous, View.VISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        	
        } else {
        	//We're smack dab in the middle of the queue, so keep the previous and next buttons enabled.
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous,View.VISIBLE);
        	expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);
        	expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, previousTrackPendingIntent);
            
            notificationView.setViewVisibility(R.id.notification_base_previous,View.VISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);
        	
        }
        
        //Set the "Stop Service" pending intents.
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, stopServicePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, stopServicePendingIntent);
        
        //Set the album art.
        expNotificationView.setImageViewBitmap(R.id.notification_expanded_base_image, songHelper.getAlbumArt());
        notificationView.setImageViewBitmap(R.id.notification_base_image, songHelper.getAlbumArt());
        
        //Attach the shrunken layout to the notification.
        mNotificationBuilder.setContent(notificationView);
        
        //Build the notification object.
        Notification notification = mNotificationBuilder.build();
        
        //Attach the expanded layout to the notification and set its flags.
        notification.bigContentView = expNotificationView;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE | 
				 			 Notification.FLAG_NO_CLEAR | 
				 			 Notification.FLAG_ONGOING_EVENT;
        
        return notification;
	}
	
	/**
	 * Builds and returns a fully constructed Notification for devices 
	 * on Ice Cream Sandwich (APIs 14 & 15).
	 */
	private Notification buildICSNotification(SongHelper songHelper) {
		mNotificationBuilder = new NotificationCompat.Builder(mContext);
		mNotificationBuilder.setOngoing(true);
		mNotificationBuilder.setAutoCancel(false);
		mNotificationBuilder.setSmallIcon(R.drawable.notif_icon);
		
		//Open up the player screen when the user taps on the notification.
        Intent launchNowPlayingIntent = new Intent();
        launchNowPlayingIntent.setAction(AudioPlaybackService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, launchNowPlayingIntent, 0);
        mNotificationBuilder.setContentIntent(launchNowPlayingPendingIntent);
		
        //Grab the notification layout.
		RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_layout);
		
		//Initialize the notification layout buttons.
    	Intent previousTrackIntent = new Intent();
    	previousTrackIntent.setAction(AudioPlaybackService.PREVIOUS_ACTION);
    	PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, previousTrackIntent, 0);
    	
    	Intent playPauseTrackIntent = new Intent();
    	playPauseTrackIntent.setAction(AudioPlaybackService.PLAY_PAUSE_ACTION);
    	PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, playPauseTrackIntent, 0);
    	
    	Intent nextTrackIntent = new Intent();
    	nextTrackIntent.setAction(AudioPlaybackService.NEXT_ACTION);
    	PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, nextTrackIntent, 0);
    	
    	Intent stopServiceIntent = new Intent();
    	stopServiceIntent.setAction(AudioPlaybackService.STOP_SERVICE);
    	PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, stopServiceIntent, 0);
		
    	//Check if audio is playing and set the appropriate play/pause button.
    	if (mApp.getService().isPlayingMusic()) {
			notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_pause_light);
		} else {
			notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_play_light);
		}
    	
    	//Set the notification content.    
        notificationView.setTextViewText(R.id.notification_base_line_one, songHelper.getTitle());
        notificationView.setTextViewText(R.id.notification_base_line_two, songHelper.getArtist());
        
        //Set the states of the next/previous buttons and their pending intents.
        if (mApp.getService().isOnlySongInQueue()) {
        	//This is the only song in the queue, so disable the previous/next buttons.
        	notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
        	
        } else if (mApp.getService().isFirstSongInQueue()) {
        	//This is the the first song in the queue, so disable the previous button. 
            notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        	
        } else if (mApp.getService().isLastSongInQueue()) {
        	//This is the last song in the cursor, so disable the next button.    
            notificationView.setViewVisibility(R.id.notification_base_previous, View.VISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        	
        } else {
        	//We're smack dab in the middle of the queue, so keep the previous and next buttons enabled.       
            notificationView.setViewVisibility(R.id.notification_base_previous,View.VISIBLE);
        	notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
        	notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);
        	
        }
        
        //Set the "Stop Service" pending intent.
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, stopServicePendingIntent);
        
        //Set the album art.
        notificationView.setImageViewBitmap(R.id.notification_base_image, songHelper.getAlbumArt());
        
        //Attach the shrunken layout to the notification.
        mNotificationBuilder.setContent(notificationView);
        
        //Build the notification object and set its flags.
        Notification notification = mNotificationBuilder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE | 
				 			 Notification.FLAG_NO_CLEAR | 
				 			 Notification.FLAG_ONGOING_EVENT;
        
        return notification;
	}
	
	/**
	 * Returns the appropriate notification based on the device's 
	 * API level.
	 */
	private Notification buildNotification(SongHelper songHelper) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return buildJBNotification(songHelper);
		else
			return buildICSNotification(songHelper);
	}
	
	/**
	 * Updates the current notification with info from the specified 
	 * SongHelper object.
	 */
	public void updateNotification(SongHelper songHelper) {
		Notification notification = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			notification = buildJBNotification(songHelper);
		else
			notification = buildICSNotification(songHelper);
		
		//Update the current notification.
		NotificationManager notifManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(mNotificationId, notification);
		
	}
	
	/**
	 * Updates all remote control clients (including the lockscreen controls).
	 */
	public void updateRemoteControlClients(SongHelper songHelper) {
		try {
            //Update the remote controls

            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getCurrentSong().getArtist())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getCurrentSong().getTitle())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getCurrentSong().getAlbum())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, getCurrentMediaPlayer().getDuration())
                    .putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK, getCurrentSong().getAlbumArt())

                    .apply();

            if (mRemoteControlClientCompat!=null) {

                if (getCurrentMediaPlayer().isPlaying())
                    mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                else
                    mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);

            }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * This method combines mCursor with the specified newCursor.
	 * 
	 * @param newCursor The new cursor to append to mCursor.
	 * @param playNext Pass true if newCursor should be appeneded after the current song.
	 */
	public void enqueueCursor(Cursor newCursor, boolean playNext) {
		
		Cursor[] cursorArray = { getCursor(), newCursor };
		mMergeCursor = new MergeCursor(cursorArray);
		setCursor(mMergeCursor);
		getCursor().moveToPosition(mPlaybackIndecesList.get(mCurrentSongIndex));
		mEnqueuePerformed = true;
		
		if (playNext) {
        	//Check which mMediaPlayer is currently playing, and prepare the other mediaPlayer.
        	prepareAlternateMediaPlayer();
        	
		}
			
	}
	
	/**
	 * This method combines the current cursor with the specified playlist cursor.
	 * @param newCursor
	 */
	public void enqueuePlaylistCursor(Cursor newCursor) {
		
		String[] matrixCursorColumns = { DBAccessHelper.SONG_ARTIST,  
										 DBAccessHelper.SONG_ALBUM, 
										 DBAccessHelper.SONG_TITLE, 
										 DBAccessHelper.SONG_FILE_PATH,
										 DBAccessHelper.SONG_DURATION, 
										 DBAccessHelper.SONG_GENRE, 
										 DBAccessHelper.SONG_ID, 
										 DBAccessHelper.SONG_ALBUM_ART_PATH, 
										 DBAccessHelper.SONG_SOURCE };
		
		//Create an empty matrix getCursor() with the specified columns.
		MatrixCursor mMatrixCursor = new MatrixCursor(matrixCursorColumns);
		
		//Make a copy of the old getCursor() and copy it's contents over to the matrix getCursor().
		Cursor tempCursor = getCursor();
		
		tempCursor.moveToFirst();
		MediaMetadataRetriever mMMDR = new MediaMetadataRetriever();
		for (int i=0; i < tempCursor.getCount(); i++) {
			tempCursor.moveToPosition(i);
			
			//Check which type of getCursor() the service currently has.
			if (getCursor().getColumnIndex(DBAccessHelper.SONG_FILE_PATH)==-1) {
				
				//We'll have to manually extract the info from the audio file.
/*				String songFilePath = tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_FILE_PATH));
				
				try {
					mMMDR.setDataSource(songFilePath);
				} catch (Exception e) {
					//Skip the song if there's a problem with reading it.
					continue;
				}*/
				
				String songArtist = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				String songAlbum = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				String songTitle = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				String songDuration = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				String songGenre = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
				
				mMatrixCursor.addRow(new Object[] { songArtist, 
												   songAlbum,
												   songTitle, 
												   "",
												   songDuration, 
												   songGenre });

			} else {
				
				mMatrixCursor.addRow(new Object[] { tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)), 
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)),
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_TITLE)), 
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)),
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_DURATION)),
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_GENRE)), 
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ID)), 
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH)), 
												   tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)) });

			}
		
		}
		
		tempCursor.close();
		
		//Copy the contents of the new getCursor() over to the MatrixCursor.
		if (newCursor.getCount() > 0) {

			String songArtist = "";
			String songAlbum = "";
			String songTitle = "";
			String filePath = "";
			String songDuration = "";
			for (int j=0; j < newCursor.getCount(); j++) {
/*				newCursor.moveToPosition(j);
				filePath = newCursor.getString(newCursor.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_FILE_PATH));
				
				try {
					mMMDR.setDataSource(filePath);
				} catch (Exception e) {
					continue;
				}*/
				
				//Get the metadata from the song file.
				songArtist = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				songAlbum = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				songTitle = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				songDuration = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				String songGenre = mMMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
				
				mMatrixCursor.addRow(new Object[] { songArtist, 
						   						   songAlbum,
						   						   songTitle, 
						   						   filePath,
						   						   songDuration,
						   						   songGenre });
				
			}
			
		}
		
		mEnqueuePerformed = true;
		newCursor.close();
		mCursor = (Cursor) mMatrixCursor;
		mMatrixCursor.close();
		
	}

	/**
	 * Listens for audio focus changes and reacts accordingly.
	 */
	private OnAudioFocusChangeListener audioFocusChangeListener = new OnAudioFocusChangeListener() {
		
		@Override
	    public void onAudioFocusChange(int focusChange) {
	        if (focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
	            //We've temporarily lost focus, so pause the mMediaPlayer, wherever it's at.
	        	try {
	        		getCurrentMediaPlayer().pause();
	        		updateNotification(mApp.getService().getCurrentSong());
	        		updateWidgets();
	        		scrobbleTrack(SimpleLastFMHelper.PAUSE);
	        		mAudioManagerHelper.setHasAudioFocus(false);
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        	
	        } else if (focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
	        	//Lower the current mMediaPlayer volume.
	        	mAudioManagerHelper.setAudioDucked(true);
	        	mAudioManagerHelper.setTargetVolume(5);
    			mAudioManagerHelper.setStepDownIncrement(1);
    			mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    			mAudioManagerHelper.setOriginalVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    			mHandler.post(duckDownVolumeRunnable);
	        
			} else if (focusChange==AudioManager.AUDIOFOCUS_GAIN) {
				
				if (mAudioManagerHelper.isAudioDucked()) {
					//Crank the volume back up again.
					mAudioManagerHelper.setTargetVolume(mAudioManagerHelper.getOriginalVolume());
	    			mAudioManagerHelper.setStepUpIncrement(1);
	    			mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
	    					
	    			mHandler.post(duckUpVolumeRunnable);
					mAudioManagerHelper.setAudioDucked(false);
				} else {
		            //We've regained focus. Update the audioFocus tag, but don't start the mMediaPlayer.
					mAudioManagerHelper.setHasAudioFocus(true);
		        	
				}
	        	
	        } else if (focusChange==AudioManager.AUDIOFOCUS_LOSS) {
	        	//We've lost focus permanently so pause the service. We'll have to request focus again later.
	        	getCurrentMediaPlayer().pause();
        		updateNotification(mApp.getService().getCurrentSong());
        		updateWidgets();
        		scrobbleTrack(SimpleLastFMHelper.PAUSE);
        		mAudioManagerHelper.setHasAudioFocus(false);
	            
	        }
	        
	    }
	    
	};
	
	/**
	 * Fades out volume before a duck operation.
	 */
	private Runnable duckDownVolumeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mAudioManagerHelper.getCurrentVolume() > mAudioManagerHelper.getTargetVolume()) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
											  (mAudioManagerHelper.getCurrentVolume() - mAudioManagerHelper.getStepDownIncrement()), 
											  0);
				
				mAudioManagerHelper.setCurrentVolume( mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			    mHandler.postDelayed(this, 50);
			}
			
		}
		
	};
	
	/**
	 * Fades in volume after a duck operation.
	 */
	private Runnable duckUpVolumeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mAudioManagerHelper.getCurrentVolume() < mAudioManagerHelper.getTargetVolume()) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
											  (mAudioManagerHelper.getCurrentVolume() + mAudioManagerHelper.getStepUpIncrement()), 
											  0);
				
			    mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			    mHandler.postDelayed(this, 50);
			}
			
		}
		
	};
	
	/**
	 * Called once mMediaPlayer is prepared.
	 */
    public OnPreparedListener mediaPlayerPrepared = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mediaPlayer) {
			
			//Update the prepared flag.
			setIsMediaPlayerPrepared(true);
			
			//Set the completion listener for mMediaPlayer.
			getMediaPlayer().setOnCompletionListener(onMediaPlayerCompleted);

			//Check to make sure we have AudioFocus.
			if (checkAndRequestAudioFocus()==true) {
				
				//Check if the the user saved the track's last playback position.
				if (getMediaPlayerSongHelper().getSavedPosition()!=-1) {
					//Seek to the saved track position.
					mMediaPlayer.seekTo((int) getMediaPlayerSongHelper().getSavedPosition());
					mApp.broadcastUpdateUICommand(new String[] { Common.SHOW_AUDIOBOOK_TOAST }, 
												  new String[] { "" + getMediaPlayerSongHelper().getSavedPosition() });

				}
				
				//This is the first time mMediaPlayer has been prepared, so start it immediately.
				if (mFirstRun) {
					startMediaPlayer();
					mFirstRun = false;
				}
				
			} else {
				return;
			}

		}
    	
    };
	
	/**
	 * Called once mMediaPlayer2 is prepared.
	 */
    public OnPreparedListener mediaPlayer2Prepared = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mediaPlayer) {
			
			//Update the prepared flag.
			setIsMediaPlayer2Prepared(true);
			
			//Set the completion listener for mMediaPlayer2.
			getMediaPlayer2().setOnCompletionListener(onMediaPlayer2Completed);

			//Check to make sure we have AudioFocus.
			if (checkAndRequestAudioFocus()==true) {
				
				//Check if the the user saved the track's last playback position.
				if (getMediaPlayer2SongHelper().getSavedPosition()!=-1) {
					//Seek to the saved track position.
					mMediaPlayer2.seekTo((int) getMediaPlayer2SongHelper().getSavedPosition());
					mApp.broadcastUpdateUICommand(new String[] { Common.SHOW_AUDIOBOOK_TOAST }, 
												  new String[] { "" + getMediaPlayer2SongHelper().getSavedPosition() });

				}
				
			} else {
				return;
			}
			
		}
    	
    };
    
    /**
     * Completion listener for mMediaPlayer.
     */
	private OnCompletionListener onMediaPlayerCompleted = new OnCompletionListener() { 
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			
			//Remove the crossfade playback.
            mHandler.removeCallbacks(startCrossFadeRunnable);
			mHandler.removeCallbacks(crossFadeRunnable);
			
			//Set the track position handler (notifies the handler when the track should start being faded).
			if (mHandler!=null && mApp.isCrossfadeEnabled()) {
				mHandler.post(startCrossFadeRunnable);
			}

			//Reset the fadeVolume variables.
			mFadeInVolume = 0.0f;
			mFadeOutVolume = 1.0f;
			
			//Reset the volumes for both mediaPlayers.
			getMediaPlayer().setVolume(1.0f, 1.0f);
			getMediaPlayer2().setVolume(1.0f, 1.0f);

			try {
				if (isAtEndOfQueue() && getRepeatMode()!=Common.REPEAT_PLAYLIST) {
					stopSelf();
				} else if (isMediaPlayer2Prepared()) {
					startMediaPlayer2();
				} else {
					//Check every 100ms if mMediaPlayer2 is prepared.
					mHandler.post(startMediaPlayer2IfPrepared);
				}
				
			} catch (IllegalStateException e) {
				//mMediaPlayer2 isn't prepared yet.
				mHandler.post(startMediaPlayer2IfPrepared);
			}

		}
		
	};
	
	/**
	 * Completion listener for mMediaPlayer2.
	 */
	private OnCompletionListener onMediaPlayer2Completed = new OnCompletionListener() { 
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			
			//Remove the crossfade playback.
            mHandler.removeCallbacks(startCrossFadeRunnable);
			mHandler.removeCallbacks(crossFadeRunnable);
			
			//Set the track position handler (notifies the handler when the track should start being faded).
			if (mHandler!=null && mApp.isCrossfadeEnabled()) {
				mHandler.post(startCrossFadeRunnable);
			}

			//Reset the fadeVolume variables.
			mFadeInVolume = 0.0f;
			mFadeOutVolume = 1.0f;
			
			//Reset the volumes for both mediaPlayers.
			getMediaPlayer().setVolume(1.0f, 1.0f);
			getMediaPlayer2().setVolume(1.0f, 1.0f);

			try {
				if (isAtEndOfQueue() && getRepeatMode()!=Common.REPEAT_PLAYLIST) {
					stopSelf();
				} else if (isMediaPlayerPrepared()) {
					startMediaPlayer();
				} else {
					//Check every 100ms if mMediaPlayer is prepared.
					mHandler.post(startMediaPlayerIfPrepared);
				}
				
			} catch (IllegalStateException e) {
				//mMediaPlayer isn't prepared yet.
				mHandler.post(startMediaPlayerIfPrepared);
			}

		}
		
	};
	
    /**
     * Buffering listener.
     */
    public OnBufferingUpdateListener bufferingListener = new OnBufferingUpdateListener() {

		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			
			if (mApp.getSharedPreferences().getBoolean("NOW_PLAYING_ACTIVE", false)==true) {
				
				if (mp==getCurrentMediaPlayer()) {
					float max = mp.getDuration()/1000;
					float maxDividedByHundred = max/100;
					mApp.broadcastUpdateUICommand(new String[] { Common.UPDATE_BUFFERING_PROGRESS }, 
												  new String[] { "" + (int) (percent*maxDividedByHundred) });
				}

			}
			
		}
    	
    };
    
    /**
     * Error listener for mMediaPlayer.
     */
    public OnErrorListener onErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mMediaPlayer, int what, int extra) {
			/* This error listener might seem like it's not doing anything. 
			 * However, removing this will cause the mMediaPlayer object to go crazy 
			 * and skip around. The key here is to make this method return true. This 
			 * notifies the mMediaPlayer object that we've handled all errors and that 
			 * it shouldn't do anything else to try and remedy the situation. 
			 * 
			 * TL;DR: Don't touch this interface. Ever.
			 */
			return true;
		}
    	
    };
    
	/**
	 * Starts mMediaPlayer if it is prepared and ready for playback. 
	 * Otherwise, continues checking every 100ms if mMediaPlayer is prepared.
	 */
	private Runnable startMediaPlayerIfPrepared = new Runnable() {

		@Override
		public void run() {
			if (isMediaPlayerPrepared())
				startMediaPlayer();
			else
				mHandler.postDelayed(this, 100);
			
			
		}
		
	};
	
	/**
	 * Starts mMediaPlayer if it is prepared and ready for playback.
	 * Otherwise, continues checking every 100ms if mMediaPlayer2 is prepared.
	 */
	private Runnable startMediaPlayer2IfPrepared = new Runnable() {

		@Override
		public void run() {
			if (isMediaPlayer2Prepared())
				startMediaPlayer2();
			else
				mHandler.postDelayed(this, 100);
			
			
		}
		
	};

	/**
	 * First runnable that handles the cross fade operation between two tracks.
	 */
	public Runnable startCrossFadeRunnable = new Runnable() {

		@Override
		public void run() {

			//Check if we're in the last part of the current song.
			try {
				if (getCurrentMediaPlayer().isPlaying()) {

					int currentTrackDuration = getCurrentMediaPlayer().getDuration();
					int currentTrackFadePosition = currentTrackDuration - (mCrossfadeDuration*1000);
					if (getCurrentMediaPlayer().getCurrentPosition() >= currentTrackFadePosition) {
						//Launch the next runnable that will handle the cross fade effect.
						mHandler.postDelayed(crossFadeRunnable, 100);

					} else {
						mHandler.postDelayed(startCrossFadeRunnable, 1000);
					}

				} else {
					mHandler.postDelayed(startCrossFadeRunnable, 1000);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	};

	/**
	 * Crossfade runnable.
	 */
	public Runnable crossFadeRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				
				//Do not crossfade if the current song is set to repeat itself.
				if (getRepeatMode()!=Common.REPEAT_SONG) {
					
					//Do not crossfade if this is the last track in the queue.
					if (getCursor().getCount() > (mCurrentSongIndex+1)) {
						
						//Set the next mMediaPlayer's volume and raise it incrementally.
						if (getCurrentMediaPlayer()==getMediaPlayer()) {
							
							getMediaPlayer2().setVolume(mFadeInVolume, mFadeInVolume);
							getMediaPlayer().setVolume(mFadeOutVolume, mFadeOutVolume);
							
							//If the mMediaPlayer is already playing or it hasn't been prepared yet, we can't use crossfade.
							if (!getMediaPlayer2().isPlaying()) {
								
								if (mMediaPlayer2Prepared==true) {

									if (checkAndRequestAudioFocus()==true) {
										
										//Check if the the user requested to save the track's last playback position.
										if (getMediaPlayer2SongHelper().getSavedPosition()!=-1) {
											//Seek to the saved track position.
											getMediaPlayer2().seekTo((int) getMediaPlayer2SongHelper().getSavedPosition());
											mApp.broadcastUpdateUICommand(new String[] { Common.SHOW_AUDIOBOOK_TOAST }, 
																		  new String[] { "" + getMediaPlayer2SongHelper().getSavedPosition() });
											
										}
										
										getMediaPlayer2().start();
									} else {
										return;
									}
									
								}
								
							}
							
						} else {
							
							getMediaPlayer().setVolume(mFadeInVolume, mFadeInVolume);
							getMediaPlayer2().setVolume(mFadeOutVolume, mFadeOutVolume);
							
							//If the mMediaPlayer is already playing or it hasn't been prepared yet, we can't use crossfade.
							if (!getMediaPlayer().isPlaying()) {
								
								if (mMediaPlayerPrepared==true) {

									if (checkAndRequestAudioFocus()==true) {
										
										//Check if the the user requested to save the track's last playback position.
										if (getMediaPlayerSongHelper().getSavedPosition()!=-1) {
											//Seek to the saved track position.
											getMediaPlayer().seekTo((int) getMediaPlayerSongHelper().getSavedPosition());
											mApp.broadcastUpdateUICommand(new String[] { Common.SHOW_AUDIOBOOK_TOAST }, 
																		  new String[] { "" + getMediaPlayerSongHelper().getSavedPosition() });
											
										}
										
										getMediaPlayer().start();
									} else {
										return;
									}
									
								}
								
							}
							
						}

						mFadeInVolume = mFadeInVolume + (float) (1.0f/(((float) mCrossfadeDuration)*10.0f));
						mFadeOutVolume = mFadeOutVolume - (float) (1.0f/(((float) mCrossfadeDuration)*10.0f));

						mHandler.postDelayed(crossFadeRunnable, 100);
					}
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	};
	
	/**
	 * Grabs the song parameters at the specified index, retrieves its 
	 * data source, and beings to asynchronously prepare mMediaPlayer. 
	 * Once mMediaPlayer is prepared, mediaPlayerPrepared is called.
	 * 
	 * @return True if the method completed with no exceptions. False, otherwise.
	 */
	public boolean prepareMediaPlayer(int songIndex) {
		
		try {

            //Stop here if we're at the end of the queue.
            if (songIndex==-1)
                return true;

			//Reset mMediaPlayer to it's uninitialized state.
	    	getMediaPlayer().reset();
	    	
    		//Loop the player if the repeat mode is set to repeat the current song.
    		if (getRepeatMode()==Common.REPEAT_SONG) {
    			getMediaPlayer().setLooping(true);
    		}

	    	//Set mMediaPlayer's song data.
	    	SongHelper songHelper = new SongHelper();
	    	if (mFirstRun) {
	    		/*
	    		 * We're not preloading the next song (mMediaPlayer2 is not 
	    		 * playing right now). mMediaPlayer's song is pointed at 
	    		 * by mCurrentSongIndex.
	    		 */
	    		songHelper.populateSongData(mContext, songIndex);
                setMediaPlayerSongHelper(songHelper);
	    		//Set this service as a foreground service.
	    		startForeground(mNotificationId, buildNotification(songHelper));
	    	} else {
	    		songHelper.populateSongData(mContext, songIndex);
                setMediaPlayerSongHelper(songHelper);
	    	}

    		/*
    		 * Set the data source for mMediaPlayer and start preparing it 
    		 * asynchronously.
    		 */
	    	getMediaPlayer().setDataSource(mContext, getSongDataSource(getMediaPlayerSongHelper()));
			getMediaPlayer().setOnPreparedListener(mediaPlayerPrepared);
            getMediaPlayer().setOnErrorListener(onErrorListener);
            getMediaPlayer().prepareAsync();

        } catch (Exception e) {
            Log.e("DEBUG", "MESSAGE", e);
            e.printStackTrace();

            //Display an error toast to the user.
            showErrorToast();

            //Add the current song index to the list of failed indeces.
            getFailedIndecesList().add(songIndex);

            //Start preparing the next song.
            if (!isAtEndOfQueue() || mFirstRun)
                prepareMediaPlayer(songIndex+1);
            else
                return false;

            return false;
        }

		return true;
	}
	
	/**
	 * Grabs the song parameters at the specified index, retrieves its 
	 * data source, and beings to asynchronously prepare mMediaPlayer2. 
	 * Once mMediaPlayer2 is prepared, mediaPlayer2Prepared is called.
	 * 
	 * @return True if the method completed with no exceptions. False, otherwise.
	 */
	public boolean prepareMediaPlayer2(int songIndex) {
		
		try {

            //Stop here if we're at the end of the queue.
            if (songIndex==-1)
                return true;
			
			//Reset mMediaPlayer2 to its uninitialized state.
	    	getMediaPlayer2().reset();
	    	
    		//Loop the player if the repeat mode is set to repeat the current song.
    		if (getRepeatMode()==Common.REPEAT_SONG) {
    			getMediaPlayer2().setLooping(true);
    		}
	    	
	    	//Set mMediaPlayer2's song data.
	    	SongHelper songHelper = new SongHelper();
	    	songHelper.populateSongData(mContext, songIndex);
	    	setMediaPlayer2SongHelper(songHelper);

    		/*
    		 * Set the data source for mMediaPlayer and start preparing it 
    		 * asynchronously.
    		 */
	    	getMediaPlayer2().setDataSource(mContext, getSongDataSource(getMediaPlayer2SongHelper()));
			getMediaPlayer2().setOnPreparedListener(mediaPlayer2Prepared);
			getMediaPlayer2().setOnErrorListener(onErrorListener);
			getMediaPlayer2().prepareAsync();

		} catch (Exception e) {
			e.printStackTrace();

            //Display an error toast to the user.
            showErrorToast();

			//Add the current song index to the list of failed indeces.
			getFailedIndecesList().add(songIndex);
			
			//Start preparing the next song.
			if (!isAtEndOfQueue())
				prepareMediaPlayer2(songIndex+1);
			else
				return false;
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the Uri of a song's data source. 
	 * If the song is a local file, its file path is 
	 * returned. If the song is from GMusic, its local 
	 * copy path is returned (if it exists). If no local 
	 * copy exists, the song's remote URL is requested 
	 * from Google's servers and a temporary placeholder 
	 * (URI_BEING_LOADED) is returned.
	 */
	private Uri getSongDataSource(SongHelper songHelper) {

		if (songHelper.getSource().equals(DBAccessHelper.GMUSIC)) {
			
			//Check if a local copy of the song exists.
			if (songHelper.getLocalCopyPath()!=null && 
				songHelper.getLocalCopyPath().length() > 2) {
				
				//Double check to make sure that the local copy file exists.
				if (new File(songHelper.getLocalCopyPath()).exists()) {
					//The local copy exists. Return its path.
					return Uri.parse(songHelper.getLocalCopyPath());
				} else {
					//The local copy doesn't exist. Request the remote URL and return a placeholder Uri.
					AsyncGetSongStreamURLTask task = new AsyncGetSongStreamURLTask(mContext, songHelper.getId());
					task.execute();
					
					return URI_BEING_LOADED;
				}
				
			} else {
				//Request the remote URL and return a placeholder Uri.
				AsyncGetSongStreamURLTask task = new AsyncGetSongStreamURLTask(mContext, songHelper.getId());
				task.execute();
				
				return URI_BEING_LOADED;			
			}
			
		} else {
			//Return the song's file path.
			return Uri.parse(songHelper.getFilePath());
		}
		
	}
	
	/**
	 * Updates all open homescreen/lockscreen widgets.
	 */
	public void updateWidgets() {
		try {
			//Fire a broadcast message to the widget(s) to update them.
			Intent smallWidgetIntent = new Intent(mContext, SmallWidgetProvider.class);
			smallWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int smallWidgetIds[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, SmallWidgetProvider.class));
			smallWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds);
			mContext.sendBroadcast(smallWidgetIntent);
			
			Intent largeWidgetIntent = new Intent(mContext, LargeWidgetProvider.class);
			largeWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int largeWidgetIds[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, LargeWidgetProvider.class));
			largeWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, largeWidgetIds);
			mContext.sendBroadcast(largeWidgetIntent);
			
			Intent blurredWidgetIntent = new Intent(mContext, BlurredWidgetProvider.class);
			blurredWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int blurredWidgetIds[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, BlurredWidgetProvider.class));
			blurredWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, blurredWidgetIds);
			mContext.sendBroadcast(blurredWidgetIntent);
			
			Intent albumArtWidgetIntent = new Intent(mContext, AlbumArtWidgetProvider.class);
			albumArtWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int albumArtWidgetIds[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, AlbumArtWidgetProvider.class));
			albumArtWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, albumArtWidgetIds);
			mContext.sendBroadcast(albumArtWidgetIntent);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Sets the A-B Repeat song markers.
	 * 
	 * @param pointA The duration to repeat from (in millis).
	 * @param pointB The duration to repeat until (in millis).
	 */
	public void setRepeatSongRange(int pointA, int pointB) {
		mRepeatSongRangePointA = pointA;
		mRepeatSongRangePointB = pointB;
		getCurrentMediaPlayer().seekTo(pointA);
		mHandler.postDelayed(checkABRepeatRange, 100);
	}
	
	/**
	 * Clears the A-B Repeat song markers.
	 */
	public void clearABRepeatRange() {
		mHandler.removeCallbacks(checkABRepeatRange);
		mRepeatSongRangePointA = 0;
		mRepeatSongRangePointB = 0;
		mApp.getSharedPreferences().edit().putInt(Common.REPEAT_MODE, Common.REPEAT_OFF);
	}
	
	/**
	 * Called repetitively to check for A-B repeat markers.
	 */
	private Runnable checkABRepeatRange = new Runnable() {

		@Override
		public void run() {
			try {
				if (getCurrentMediaPlayer().isPlaying()) {
					
					if (getCurrentMediaPlayer().getCurrentPosition()>= (mRepeatSongRangePointB)) {
						getCurrentMediaPlayer().seekTo(mRepeatSongRangePointA);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (mApp.getSharedPreferences().getInt(Common.REPEAT_MODE, Common.REPEAT_OFF)==Common.A_B_REPEAT) {
				mHandler.postDelayed(checkABRepeatRange, 100);
			}
			
		}
		
	};
	
	/**
	 * Fix for KitKat error where the service is killed as soon 
	 * as the app is swiped away from the Recents menu.
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent) {
	   Intent intent = new Intent(this, KitKatFixActivity.class);
	   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   startActivity(intent);
	   
	}

    /**
     * Displays an error toast.
     */
    private void showErrorToast() {
        Toast.makeText(mContext, R.string.song_failed_to_load, Toast.LENGTH_SHORT).show();
    }
	
	/**
	 * Deploys the current track's data to the specified 
	 * scrobbler.
	 * 
	 * @param state The scrobble state.
	 */
	public void scrobbleTrack(int state) {
		
		//If scrobbling is enabled, send out the appropriate action events.
		if (mApp.getSharedPreferences().getInt("SCROBBLING", 0)==0) {
			//Scrobbling is disabled.
			return;
		} 
		
		//Get the metadata of the track.
		getCursor().moveToPosition(mPlaybackIndecesList.get(mCurrentSongIndex));
		String songTitle = getCursor().getString(getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE));
		String songArtist = getCursor().getString(getCursor().getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songAlbum = getCursor().getString(getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM));
		
		int songDurationInSecs;
		try {
			songDurationInSecs = getCursor().getInt(getCursor().getColumnIndex(DBAccessHelper.SONG_DURATION)) / 1000;
		} catch (Exception e) {
			songDurationInSecs = 0;
		}
		
		
		if (mApp.getSharedPreferences().getInt("SCROBBLING", 0)==1) {
			//Simple LastFM Helper.
			SimpleLastFMHelper.initializeActionIntent();
			SimpleLastFMHelper.attachMetadata(state, songArtist, songAlbum, songTitle, songDurationInSecs);
			SimpleLastFMHelper.sendBroadcast(mContext);
			
		} else if (mApp.getSharedPreferences().getInt("SCROBBLING", 0)==2) {
			//Scrobble Droid.
			ScrobbleDroidHelper.initializeActionIntent();
			if (state==SimpleLastFMHelper.START || state==SimpleLastFMHelper.RESUME) {
				ScrobbleDroidHelper.attachMetadata(true, songArtist, songAlbum, songTitle, songDurationInSecs);
			} else if (state==SimpleLastFMHelper.PAUSE) {
				ScrobbleDroidHelper.attachMetadata(false, songArtist, songAlbum, songTitle, songDurationInSecs);
			}
			
			ScrobbleDroidHelper.sendBroadcast(mContext);
		}
		
	}
	
	/**
	 * Checks if we have AudioFocus. If not, it explicitly requests it.
	 * 
	 * @return True if we have AudioFocus. False, otherwise.
	 */
	private boolean checkAndRequestAudioFocus() {
		if (mAudioManagerHelper.hasAudioFocus()==false) {
			if (requestAudioFocus()==true) {
				return true;
			} else {
				//Unable to get focus. Notify the user.
				Toast.makeText(mContext, R.string.unable_to_get_audio_focus, Toast.LENGTH_LONG).show();
				return false;
			}
			
		} else {
			return true;
		}
		
	}
	
	/**
	 * Registers the headset plug receiver.
	 */
	public void registerHeadsetPlugReceiver() {
		//Register the headset plug receiver.
		if (mApp.getSharedPreferences().getString("UNPLUG_ACTION", "PAUSE_MUSIC_PLAYBACK").equals("PAUSE_MUSIC_PLAYBACK")) {
			IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			mHeadsetPlugReceiver = new HeadsetPlugBroadcastReceiver();
		    mService.registerReceiver(mHeadsetPlugReceiver, filter);
		}
		
	}
	
    /**
     * Increments mCurrentSongIndex based on mErrorCount. 
     * Returns the new value of mCurrentSongIndex.
     */
    public int incrementCurrentSongIndex() {
    	if ((getCurrentSongIndex()+1) < getCursor().getCount())
    		mCurrentSongIndex++;

    	return mCurrentSongIndex;
    }
    
    /**
     * Decrements mCurrentSongIndex by one. Returns the new value 
     * of mCurrentSongIndex.
     */
    public int decrementCurrentSongIndex() {
    	if ((getCurrentSongIndex()-1) > -1)
    		mCurrentSongIndex--;
    	
    	return mCurrentSongIndex;
    }
    
    /**
     * Increments mEnqueueReorderScalar. Returns the new value 
     * of mEnqueueReorderScalar.
     */
    public int incrementEnqueueReorderScalar() {
    	mEnqueueReorderScalar++;
    	return mCurrentSongIndex;
    }
    
    /**
     * Decrements mEnqueueReorderScalar. Returns the new value 
     * of mEnqueueReorderScalar.
     */
    public int decrementEnqueueReorderScalar() {
    	mEnqueueReorderScalar--;
    	return mCurrentSongIndex;
    }
    
    /**
     * Starts playing mMediaPlayer and sends out the update UI broadcast, 
     * and updates the notification and any open widgets.
     *  
     * Do NOT call this method before mMediaPlayer has been prepared.
     */
    private void startMediaPlayer() throws IllegalStateException {
    	
    	//Set the media player's equalizer/audio fx.
    	applyMediaPlayerEQ(getMediaPlayerSongHelper().getId());
    	
    	//Aaaaand let the show begin!
    	setCurrentMediaPlayer(1);
    	getMediaPlayer().start();
    	
    	//Set the new value for mCurrentSongIndex.
    	if (mFirstRun==false) {
        	do {
        		setCurrentSongIndex(determineNextSongIndex());
        	} while (getFailedIndecesList().contains(getCurrentSongIndex()));
    		
    		getFailedIndecesList().clear();
    		
    	} else {
    		while (getFailedIndecesList().contains(getCurrentSongIndex())) {
    			setCurrentSongIndex(determineNextSongIndex());
    		}

            //Initialize the crossfade runnable.
            if (mHandler!=null && mApp.isCrossfadeEnabled()) {
                mHandler.post(startCrossFadeRunnable);
            }
    		
    	}

    	
    	//Update the UI.
		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION, 
											  Common.UPDATE_PLAYBACK_CONTROLS, 
											  Common.HIDE_STREAMING_BAR, 
											  Common.UPDATE_SEEKBAR_DURATION, 
											  Common.UPDATE_EQ_FRAGMENT };
		
		String[] flagValues = new String[] { getCurrentSongIndex() + "", 
											 "",
											 "", 
											 getMediaPlayer().getDuration() + "", 
											 "" };
		
		mApp.broadcastUpdateUICommand(updateFlags, flagValues); 
		setCurrentSong(getCurrentSong());
		
		//Start preparing the next song.
		prepareMediaPlayer2(determineNextSongIndex());
    }
    
    /**
     * Starts playing mMediaPlayer2, sends out the update UI broadcast,
     * and updates the notification and any open widgets.
     *
     * Do NOT call this method before mMediaPlayer2 has been prepared.
     */
    private void startMediaPlayer2() throws IllegalStateException {
    	
    	//Set the media player's equalizer/audio fx.
    	applyMediaPlayer2EQ(getMediaPlayer2SongHelper().getId());
    	
    	//Aaaaaand let the show begin!
    	setCurrentMediaPlayer(2);
    	getMediaPlayer2().start();
    	
    	//Set the new value for mCurrentSongIndex.
    	do {
    		setCurrentSongIndex(determineNextSongIndex());
    	} while (getFailedIndecesList().contains(getCurrentSongIndex()));
    	
		getFailedIndecesList().clear();
    	
    	//Update the UI.
		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION, 
											  Common.UPDATE_PLAYBACK_CONTROLS, 
											  Common.HIDE_STREAMING_BAR, 
											  Common.UPDATE_SEEKBAR_DURATION, 
											  Common.UPDATE_EQ_FRAGMENT };
		
		String[] flagValues = new String[] { getCurrentSongIndex() + "", 
											 "",
											 "", 
											 getMediaPlayer2().getDuration() + "", 
											 "" };
		
		mApp.broadcastUpdateUICommand(updateFlags, flagValues); 
		setCurrentSong(getCurrentSong());
    	
		//Start preparing the next song.
		prepareMediaPlayer(determineNextSongIndex());
    }
    
    /**
     * Starts/resumes the current media player. Returns true if 
     * the operation succeeded. False, otherwise.
     */
    public boolean startPlayback() {
    	
    	try {
    		//Check to make sure we have audio focus.
    		if (checkAndRequestAudioFocus()) {
        		getCurrentMediaPlayer().start();
        		
        		//Update the UI and scrobbler.
        		String[] updateFlags = new String[] { Common.UPDATE_PLAYBACK_CONTROLS };
        		String[] flagValues = new String[] { "" };
        		
        		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
        		updateNotification(mApp.getService().getCurrentSong());
    			updateWidgets();
    			scrobbleTrack(SimpleLastFMHelper.START);
    			
    		} else {
    			return false;
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Pauses the current media player. Returns true if 
     * the operation succeeded. False, otherwise.
     */
    public boolean pausePlayback() {
    	
    	try {
    		getCurrentMediaPlayer().pause();
    		
    		//Update the UI and scrobbler.
    		String[] updateFlags = new String[] { Common.UPDATE_PLAYBACK_CONTROLS };
    		String[] flagValues = new String[] { "" };
    		
    		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    		updateNotification(mApp.getService().getCurrentSong());
			updateWidgets();
			scrobbleTrack(SimpleLastFMHelper.PAUSE);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Stops the current media player. Returns true if 
     * the operation succeeded. False, otherwise.
     */
    public boolean stopPlayback() {
    	
    	try {
    		getCurrentMediaPlayer().stop();
    		
    		//Update the UI and scrobbler.
    		String[] updateFlags = new String[] { Common.UPDATE_PLAYBACK_CONTROLS };
    		String[] flagValues = new String[] { "" };
    		
    		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    		updateNotification(mApp.getService().getCurrentSong());
			updateWidgets();
			scrobbleTrack(SimpleLastFMHelper.PAUSE);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }

    /**
     * Skips to the next track (if there is one) and starts 
     * playing it. Returns true if the operation succeeded. 
     * False, otherwise.
     */
    public boolean skipToNextTrack() {
    	try {
    		//Reset both MediaPlayer objects.
    		getMediaPlayer().reset();
    		getMediaPlayer2().reset();
            clearCrossfadeCallbacks();
    		
    		//Loop the players if the repeat mode is set to repeat the current song.
    		if (getRepeatMode()==Common.REPEAT_SONG) {
    			getMediaPlayer().setLooping(true);
    			getMediaPlayer2().setLooping(true);
    		}
    		
			//Remove crossfade runnables and reset all volume levels.
			getHandler().removeCallbacks(crossFadeRunnable);
			getMediaPlayer().setVolume(1.0f, 1.0f);
			getMediaPlayer2().setVolume(1.0f, 1.0f);
    		
    		//Increment the song index.
    		incrementCurrentSongIndex();
    		
    		//Update the UI.
    		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION };
    		String[] flagValues = new String[] { getCurrentSongIndex() + "" };
    		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    		
    		//Start the playback process.
    		mFirstRun = true;
    		prepareMediaPlayer(getCurrentSongIndex());
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Skips to the previous track (if there is one) and starts 
     * playing it. Returns true if the operation succeeded. 
     * False, otherwise.
     */
    public boolean skipToPreviousTrack() {

        /*
         * If the current track is not within the first three seconds,
         * reset it. If it IS within the first three seconds, skip to the
         * previous track.
         */
        try {
            if (getCurrentMediaPlayer().getCurrentPosition() > 3000) {
                getCurrentMediaPlayer().seekTo(0);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    	try {
    		//Reset both MediaPlayer objects.
    		getMediaPlayer().reset();
    		getMediaPlayer2().reset();
            clearCrossfadeCallbacks();
    		
    		//Loop the players if the repeat mode is set to repeat the current song.
    		if (getRepeatMode()==Common.REPEAT_SONG) {
    			getMediaPlayer().setLooping(true);
    			getMediaPlayer2().setLooping(true);
    		}
    		
    		//Remove crossfade runnables and reset all volume levels.
			getHandler().removeCallbacks(crossFadeRunnable);
			getMediaPlayer().setVolume(1.0f, 1.0f);
			getMediaPlayer2().setVolume(1.0f, 1.0f);
    		
    		//Decrement the song index.
    		decrementCurrentSongIndex();
    		
    		//Update the UI.
    		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION };
    		String[] flagValues = new String[] { getCurrentSongIndex() + "" };
    		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    		
    		//Start the playback process.
    		mFirstRun = true;
    		prepareMediaPlayer(getCurrentSongIndex());
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Skips to the specified track index (if there is one) and starts 
     * playing it. Returns true if the operation succeeded. 
     * False, otherwise.
     */
    public boolean skipToTrack(int trackIndex) {
    	try {
    		//Reset both MediaPlayer objects.
    		getMediaPlayer().reset();
    		getMediaPlayer2().reset();
            clearCrossfadeCallbacks();
    		
    		//Loop the players if the repeat mode is set to repeat the current song.
    		if (getRepeatMode()==Common.REPEAT_SONG) {
    			getMediaPlayer().setLooping(true);
    			getMediaPlayer2().setLooping(true);
    		}
    		
    		//Remove crossfade runnables and reset all volume levels.
			getHandler().removeCallbacks(crossFadeRunnable);
			getMediaPlayer().setVolume(1.0f, 1.0f);
			getMediaPlayer2().setVolume(1.0f, 1.0f);
    		
    		//Update the song index.
    		setCurrentSongIndex(trackIndex);
    		
    		//Update the UI.
    		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION };
    		String[] flagValues = new String[] { getCurrentSongIndex() + "" };
    		mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    		
    		//Start the playback process.
    		mFirstRun = true;
    		prepareMediaPlayer(trackIndex);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Toggles the playback state between playing and paused and 
     * returns whether the current media player is now playing 
     * music or not.
     */
    public boolean togglePlaybackState() {
    	if (isPlayingMusic())
    		pausePlayback();
    	else
    		startPlayback();
    	
    	return isPlayingMusic();
    }
    
    /**
     * Determines the next song's index based on the repeat 
     * mode and current song index. Returns -1 if we're at 
     * the end of the queue.
     */
    private int determineNextSongIndex() {
		if (isAtEndOfQueue() && getRepeatMode()==Common.REPEAT_PLAYLIST)
			return 0;
		else if (!isAtEndOfQueue() && getRepeatMode()==Common.REPEAT_SONG)
			return getCurrentSongIndex();
		else if (isAtEndOfQueue())
			return -1;
		else
			return (getCurrentSongIndex() + 1);
		
    }
    
    /**
     * Checks which MediaPlayer object is currently in use, and 
     * starts preparing the other one.
     */
    public void prepareAlternateMediaPlayer() {
    	if (mCurrentMediaPlayer==1)
    		prepareMediaPlayer2(determineNextSongIndex());
    	else
    		prepareMediaPlayer(determineNextSongIndex());
    	
    }
    
    /**
     * Toggles shuffle mode and returns whether shuffle is now on or off.
	 */
    public boolean toggleShuffleMode() {
    	if (isShuffleOn()) {
    		//Set shuffle off.
    		mApp.getSharedPreferences().edit().putBoolean(Common.SHUFFLE_ON, false).commit();

    		//Save the element at the current index.
    		int currentElement = getPlaybackIndecesList().get(getCurrentSongIndex());
    		
    		//Reset the cursor pointers list.
    		Collections.sort(getPlaybackIndecesList());
    		
    		//Reset the current index to the index of the old element.
    		setCurrentSongIndex(getPlaybackIndecesList().indexOf(currentElement));
    		
    		
    	} else {
    		//Set shuffle on.
    		mApp.getSharedPreferences().edit().putBoolean(Common.SHUFFLE_ON, true).commit();

            //Build a new list that doesn't include the current song index.
            ArrayList<Integer> newList = new ArrayList<Integer>(getPlaybackIndecesList());
            newList.remove(getCurrentSongIndex());

            //Shuffle the new list.
            Collections.shuffle(newList, new Random(System.nanoTime()));

            //Plug in the current song index back into the new list.
            newList.add(getCurrentSongIndex(), getCurrentSongIndex());
            mPlaybackIndecesList = newList;

            //Collections.shuffle(getPlaybackIndecesList().subList(0, getCurrentSongIndex()));
            //Collections.shuffle(getPlaybackIndecesList().subList(getCurrentSongIndex()+1, getPlaybackIndecesList().size()));
    		
    	}
    	
    	/* Since the queue changed, we're gonna have to update the 
    	 * next MediaPlayer object with the new song info.
    	 */
    	prepareAlternateMediaPlayer();
    	
    	//Update all UI elements with the new queue order.
    	mApp.broadcastUpdateUICommand(new String[] { Common.NEW_QUEUE_ORDER }, new String[] { "" });
    	return isShuffleOn();
    }
    
    /**
     * Applies the specified repeat mode.
     */
    public void setRepeatMode(int repeatMode) {
    	if (repeatMode==Common.REPEAT_OFF || repeatMode==Common.REPEAT_PLAYLIST ||
    		repeatMode==Common.REPEAT_SONG || repeatMode==Common.A_B_REPEAT) {
    		//Save the repeat mode.
    		mApp.getSharedPreferences().edit().putInt(Common.REPEAT_MODE, repeatMode).commit();
    	} else {
    		//Just in case a bogus value is passed in.
    		mApp.getSharedPreferences().edit().putInt(Common.REPEAT_MODE, Common.REPEAT_OFF).commit();
    	}
    	
    	/* 
    	 * Set the both MediaPlayer objects to loop if the repeat mode 
    	 * is Common.REPEAT_SONG.
    	 */
    	try {
    		if (repeatMode==Common.REPEAT_SONG) {
    			getMediaPlayer().setLooping(true);
    			getMediaPlayer2().setLooping(true);
    		} else {
    			getMediaPlayer().setLooping(false);
    			getMediaPlayer2().setLooping(false);
    		}
    		
    		//Prepare the appropriate next song.
    		prepareAlternateMediaPlayer();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        /*
         * Remove the crossfade callbacks and reinitalize them
         * only if the user didn't select A-B repeat.
         */
        clearCrossfadeCallbacks();

        if (repeatMode!=Common.A_B_REPEAT)
            if (mHandler!=null && mApp.isCrossfadeEnabled())
                mHandler.post(startCrossFadeRunnable);
    	
    }
    
    /**
     * Returns the current active MediaPlayer object.
     */
    public MediaPlayer getCurrentMediaPlayer() {
    	if (mCurrentMediaPlayer==1)
    		return mMediaPlayer;
    	else
    		return mMediaPlayer2;
    }
    
    /**
     * Returns the primary MediaPlayer object. Don't 
     * use this method directly unless you have a good 
     * reason to explicitly call mMediaPlayer. Use 
     * getCurrentMediaPlayer() whenever possible.
     */
    public MediaPlayer getMediaPlayer() {
    	return mMediaPlayer;
    }
    
    /**
     * Returns the secondary MediaPlayer object. Don't 
     * use this method directly unless you have a good 
     * reason to explicitly call mMediaPlayer2. Use 
     * getCurrentMediaPlayer() whenever possible.
     */
    public MediaPlayer getMediaPlayer2() {
    	return mMediaPlayer2;
    }
    
    /**
     * Indicates if mMediaPlayer is prepared and 
     * ready for playback.
     */
    public boolean isMediaPlayerPrepared() {
    	return mMediaPlayerPrepared;
    }
    
    /**
     * Indicates if mMediaPlayer2 is prepared and 
     * ready for playback.
     */
    public boolean isMediaPlayer2Prepared() {
    	return mMediaPlayer2Prepared;
    }
    
    /**
     * Indicates if music is currently playing.
     */
    public boolean isPlayingMusic() {
    	try {
        	if (getCurrentMediaPlayer().isPlaying())
        		return true;
        	else
        		return false;
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}

    }
    
    /**
     * Returns an instance of SongHelper. This 
     * object can be used to pull details about 
     * the current song.
     */
    public SongHelper getCurrentSong() {
    	if (getCurrentMediaPlayer()==mMediaPlayer) {
    		return mMediaPlayerSongHelper;
    	} else {
    		return mMediaPlayer2SongHelper;
    	}
    	
    }

    /**
     * Removes all crossfade callbacks on the current
     * Handler object. Also resets the volumes of the
     * MediaPlayer objects to 1.0f.
     */
    private void clearCrossfadeCallbacks() {
        if (mHandler==null)
            return;

        mHandler.removeCallbacks(startCrossFadeRunnable);
        mHandler.removeCallbacks(crossFadeRunnable);

        try {
            getMediaPlayer().setVolume(1.0f, 1.0f);
            getMediaPlayer2().setVolume(1.0f, 1.0f);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Returns mMediaPlayer's SongHelper instance.
     */
    public SongHelper getMediaPlayerSongHelper() {
    	return mMediaPlayerSongHelper;
    }
    
    /**
     * Returns mMediaPlayer2's SongHelper instance.
     */
    public SongHelper getMediaPlayer2SongHelper() {
    	return mMediaPlayer2SongHelper;
    }
    
    /**
     * Returns the service's cursor object.
     */
    public Cursor getCursor() {
    	return mCursor;
    }
    
    /**
     * Returns the list of playback indeces that are used 
     * to traverse the cursor object.
     */
    public ArrayList<Integer> getPlaybackIndecesList() {
    	return mPlaybackIndecesList;
    }
    
    /**
     * Returns the list of playback indeces that could 
     * not be played.
     */
    public ArrayList<Integer> getFailedIndecesList() {
    	return mFailedIndecesList;
    }
    
    /**
     * Returns the current value of mCurrentSongIndex.
     */
    public int getCurrentSongIndex() {
    	return mCurrentSongIndex;
    }
    
    /**
     * Indicates if the track was changed by the user.
     */
    public boolean getTrackChangedByUser() {
    	return mTrackChangedByUser;
    }
    
    /**
     * Indicates if an enqueue operation was performed.
     */
    public boolean getEnqueuePerformed() {
    	return mEnqueuePerformed;
    }
    
    /**
     * Returns the EqualizerHelper instance. This 
     * can be used to modify equalizer settings and 
     * toggle them on/off.
     */
    public EqualizerHelper getEqualizerHelper() {
    	return mEqualizerHelper;
    }
    
    /**
     * Returns the mAudioManagerHelper instance. This 
     * can be used to modify AudioFocus states.
     */
    public AudioManagerHelper getAudioManagerHelper() {
    	return mAudioManagerHelper;
    }
    
    /**
     * Returns the mHandler object.
     */
    public Handler getHandler() {
    	return mHandler;
    }
    
    /**
     * Returns the headset plug receiver object.
     */
    public HeadsetPlugBroadcastReceiver getHeadsetPlugReceiver() {
    	return mHeadsetPlugReceiver;
    }
    
    /**
     * Returns the current enqueue reorder scalar.
     */
    public int getEnqueueReorderScalar() {
    	return mEnqueueReorderScalar;
    }
    
    /**
     * Returns point A in milliseconds for A-B repeat.
     */
    public int getRepeatSongRangePointA() {
    	return mRepeatSongRangePointA;
    }
    
    /**
     * Returns point B in milliseconds for A-B repeat.
     */
    public int getRepeatSongRangePointB() {
    	return mRepeatSongRangePointB;
    }
    
    /**
     * Returns the current repeat mode. The repeat mode 
     * is determined based on the value that is saved in 
     * SharedPreferences.
     */
    public int getRepeatMode() {
    	return mApp.getSharedPreferences().getInt(Common.REPEAT_MODE, Common.REPEAT_OFF);
    }
    
    /**
     * Indicates if shuffle mode is turned on or off.
     */
    public boolean isShuffleOn() {
    	return mApp.getSharedPreferences().getBoolean(Common.SHUFFLE_ON, false);
    }
    
    /**
     * Indicates if mCurrentSongIndex points to the last 
     * song in the current queue.
     */
    public boolean isAtEndOfQueue() {
    	return (getCurrentSongIndex()==(getPlaybackIndecesList().size()-1));
    }
    
    /**
     * Indicates if mCurrentSongIndex points to the first 
     * song in the current queue.
     */
    public boolean isAtStartOfQueue() {
    	return getCurrentSongIndex()==0;
    }
    
	/**
	 * Sets the current active media player. Note that this 
	 * method does not modify the MediaPlayer objects in any 
	 * way. It simply changes the int variable that points to 
	 * the new current MediaPlayer object.
	 */
	public void setCurrentMediaPlayer(int currentMediaPlayer) {
		mCurrentMediaPlayer = currentMediaPlayer;
	}
	
	/**
	 * Sets the prepared flag for mMediaPlayer.
	 */
	public void setIsMediaPlayerPrepared(boolean prepared) {
		mMediaPlayerPrepared = prepared;
	}
	
	/**
	 * Sets the prepared flag for mMediaPlayer2.
	 */
	public void setIsMediaPlayer2Prepared(boolean prepared) {
		mMediaPlayer2Prepared = prepared;
	}

	/**
	 * Changes the value of mCurrentSongIndex.
	 */
	public void setCurrentSongIndex(int currentSongIndex) {
		mCurrentSongIndex = currentSongIndex;
	}
	
	/**
	 * Sets whether the track was changed by the user or not.
	 */
	public void setTrackChangedByUser(boolean trackChangedByUser) {
		mTrackChangedByUser = trackChangedByUser;
	}
	
	/**
	 * Sets whether an enqueue operation was performed or not.
	 */
	public void setEnqueuePerformed(boolean enqueuePerformed) {
		mEnqueuePerformed = enqueuePerformed;
	}
	
	/**
	 * Sets the new enqueue reorder scalar value.
	 */
	public void setEnqueueReorderScalar(int scalar) {
		mEnqueueReorderScalar = scalar;
	}
	
    /**
     * Sets point A in milliseconds for A-B repeat.
     */
    public void setRepeatSongRangePointA(int value) {
    	mRepeatSongRangePointA = value;
    }
    
    /**
     * Returns point B in milliseconds for A-B repeat.
     */
    public void getRepeatSongRangePointB(int value) {
    	mRepeatSongRangePointB = value;
    }
	
	/**
	 * Replaces the current cursor object with the new one.
	 */
	public void setCursor(Cursor cursor) {
		mCursor = cursor;
	}
	
	/**
	 * Moves the cursor back to the first song in the queue. 
	 * This does not necessarily move the cursor to index 0. 
	 * It moves it to the element at index 0 in 
	 * mPlaybackIndecesList.
	 */
	public void moveCursorToQueueStart() {
		getCursor().moveToPosition(getPlaybackIndecesList().get(0));
	}
	
	/**
	 * Moves the cursor forward to the last song in the queue. 
	 * This does not necessarily move the cursor to index 
	 * {cursorSize-1}. It moves it to the element at index 
	 * {cursorSize-1} in mPlaybackIndecesList.
	 */
	public void moveCursorToQueueEnd() {
		getCursor().moveToPosition(getPlaybackIndecesList().get(getPlaybackIndecesList().size()-1));
	}
	
	/**
	 * Moves the cursor to the specified index in the queue.
	 * Returns true if the index was valid and the cursor 
	 * position was moved successfully. False, otherwise.
	 */
	public boolean moveCursorToIndex(int index) {
		if (index < getCursor().getCount() && index > -1) {
			getCursor().moveToPosition(getPlaybackIndecesList().get(index));
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Returns true if there's only one song in the current queue.
	 * False, otherwise.
	 */
	public boolean isOnlySongInQueue() {
		if (getCurrentSongIndex()==0 && getCursor().getCount()==1)
			return true;
		else
			return false;
		
	}
	
	/**
	 * Returns true if mCurrentSongIndex is pointing at the first 
	 * song in the queue and there is more than one song in the 
	 * queue. False, otherwise.
	 */
	public boolean isFirstSongInQueue() {
		if (getCurrentSongIndex()==0 && getCursor().getCount() > 1)
			return true;
		else
			return false;
		
	}
	
	/**
	 * Returns true if mCurrentSongIndex is pointing at the last 
	 * song in the queue. False, otherwise.
	 */
	public boolean isLastSongInQueue() {
		if (getCurrentSongIndex()==(getCursor().getCount()-1))
			return true;
		else
			return false;
		
	}
	
	/**
	 * Returns an instance of the PrepareServiceListener.
	 */
	public PrepareServiceListener getPrepareServiceListener() {
		return mPrepareServiceListener;
	}
	
	/**
	 * Sets the mPrepareServiceListener object.
	 */
	public void setPrepareServiceListener(PrepareServiceListener listener) {
		mPrepareServiceListener = listener;
	}
	
	/**
	 * Sets mMediaPlayerSongHelper.
	 */
	public void setMediaPlayerSongHelper(SongHelper songHelper) {
		mMediaPlayerSongHelper = songHelper;
	}
	
	/**
	 * Sets mMediaPlayer2SongHelper.
	 */
	public void setMediaPlayer2SongHelper(SongHelper songHelper) {
		mMediaPlayer2SongHelper = songHelper;
	}
	
	/**
	 * Sets the current MediaPlayer's SongHelper object. Also 
	 * indirectly calls the updateNotification() and updateWidgets() 
	 * methods via the [CURRENT SONG HELPER].setIsCurrentSong() method.
	 */
	private void setCurrentSong(SongHelper songHelper) {
		if (getCurrentMediaPlayer()==mMediaPlayer) {
			mMediaPlayerSongHelper = songHelper;
			mMediaPlayerSongHelper.setIsCurrentSong();
		} else {
			mMediaPlayer2SongHelper = songHelper;
			mMediaPlayer2SongHelper.setIsCurrentSong();
		}
		
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {

		//Notify the UI that the service is about to stop.
		mApp.broadcastUpdateUICommand(new String[] { Common.SERVICE_STOPPING }, 
									  new String[] { "" });
		
		//Fire a broadcast message to the widget(s) to update them.
		updateWidgets();
		
		//Send service stop event to GAnalytics.
		try {
			if (mApp.isGoogleAnalyticsEnabled()) {
				mTracker.set(Fields.SESSION_CONTROL, "end");
				mTracker.send(MapBuilder.createTiming("ACE Service",
													  System.currentTimeMillis() - mServiceStartTime, 
													  "Service duration.", 
													  "User stopped music playback.")
						.build());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Save the last track's info within the current queue.
		try {
			mApp.getSharedPreferences().edit().putLong("LAST_SONG_TRACK_POSITION", getCurrentMediaPlayer().getCurrentPosition());
		} catch (Exception e) {
			e.printStackTrace();
			mApp.getSharedPreferences().edit().putLong("LAST_SONG_TRACK_POSITION", 0);
		}
		
		//If the current song is repeating a specific range, reset the repeat option.
		if (getRepeatMode()==Common.REPEAT_SONG) {
			setRepeatMode(Common.REPEAT_OFF);
		}
		
		mFadeInVolume = 0.0f;
		mFadeOutVolume = 1.0f;
		
		//Unregister the headset plug receiver and RemoteControlClient.
		try {
			RemoteControlHelper.unregisterRemoteControlClient(mAudioManager, mRemoteControlClientCompat);
			unregisterReceiver(mHeadsetPlugReceiver);
		} catch (Exception e) {
			//Just null out the receiver if it hasn't been registered yet.
			mHeadsetPlugReceiver = null;
		}
		
		//Remove the notification.
		NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(mNotificationId);
		
		try {
			mEqualizerHelper.releaseEQObjects();
			mEqualizerHelper = null;
		} catch (Exception e1) {
			e1.printStackTrace();
			mEqualizerHelper = null;
		}

		if (mMediaPlayer!=null)
			mMediaPlayer.release();
		
		if (mMediaPlayer2!=null)
			getMediaPlayer2().release();
		
		mMediaPlayer = null;
		mMediaPlayer2 = null;
		
		//Close the cursor(s).
		try {
			getCursor().close();
			setCursor(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Final scrobbling.
		scrobbleTrack(SimpleLastFMHelper.PAUSE);

        /*
         * If A-B repeat is enabled, disable it to prevent the
         * next service instance from repeating the same section
         * over and over on the new track.
         */
        if (getRepeatMode()==Common.A_B_REPEAT)
            setRepeatMode(Common.REPEAT_OFF);

		//Remove audio focus and unregister the audio buttons receiver.
		mAudioManagerHelper.setHasAudioFocus(false);
		mAudioManager.abandonAudioFocus(audioFocusChangeListener);
		mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), HeadsetButtonsReceiver.class.getName()));
		mAudioManager = null;
		mMediaButtonReceiverComponent = null;
		mRemoteControlClientCompat = null;
		
		//Nullify the service object.
		mApp.setService(null);
		mApp.setIsServiceRunning(false);
		mApp = null;
		
	}

    /**
     * Interface implementation to listen for service cursor events.
     */
    public BuildCursorListener buildCursorListener = new BuildCursorListener() {

        @Override
        public void onServiceCursorReady(Cursor cursor, int currentSongIndex, boolean playAll) {

            if (cursor.getCount()==0) {
                Toast.makeText(mContext, R.string.no_audio_files_found, Toast.LENGTH_SHORT).show();
                if (mApp.getNowPlayingActivity()!=null)
                    mApp.getNowPlayingActivity().finish();

                return;
            }

            setCursor(cursor);
            setCurrentSongIndex(currentSongIndex);
            getFailedIndecesList().clear();
            initPlaybackIndecesList(playAll);
            mFirstRun = true;
            prepareMediaPlayer(currentSongIndex);
            //Notify NowPlayingActivity to initialize its ViewPager.
            mApp.broadcastUpdateUICommand(new String[] { Common.INIT_PAGER },
                    new String[] { "" });
        }

        @Override
        public void onServiceCursorFailed(String exceptionMessage) {
            //We don't have a valid cursor, so stop the service.
            Log.e("SERVICE CURSOR EXCEPTION", "onServiceCursorFailed(): " + exceptionMessage);
            Toast.makeText(mContext, R.string.unable_to_start_playback, Toast.LENGTH_SHORT).show();
            stopSelf();

        }

        @Override
        public void onServiceCursorUpdated(Cursor cursor) {
            //Make sure the new cursor and the old cursor are the same size.
            if (getCursor().getCount()==cursor.getCount()) {
                setCursor(cursor);
            }

        }

    };

}
