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
package com.aniruddhc.acemusic.player.Drawers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

public class QueueDrawerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;

    private RelativeLayout mMiniPlayerLayout;
    private ImageView mMiniPlayerAlbumArt;
    private RelativeLayout mPlayPauseBackground;
    private ImageButton mPlayPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private TextView mTitleText;
    private TextView mSubText;

	private DragSortListView mListView;
    private QueueDrawerAdapter mListViewAdapter;
    private TextView mEmptyInfoText;

    private boolean mInitListViewParams = true;
    private boolean mDrawerOpen = false;

    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity();
		mApp = (Common) mContext.getApplicationContext();

		View rootView = inflater.inflate(R.layout.fragment_queue_drawer, null);
		if (mApp.getCurrentTheme()==Common.LIGHT_THEME) {
			rootView.setBackgroundColor(0xFFFFFFFF);
		} else {
			rootView.setBackgroundColor(0xFF191919);
		}

        mMiniPlayerLayout = (RelativeLayout) rootView.findViewById(R.id.queue_drawer_mini_player_layout);
        mMiniPlayerAlbumArt = (ImageView) rootView.findViewById(R.id.queue_drawer_album_art);
        mPlayPauseBackground = (RelativeLayout) rootView.findViewById(R.id.playPauseButtonBackground);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.playPauseButton);
        mNextButton = (ImageButton) rootView.findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.previousButton);
        mTitleText = (TextView) rootView.findViewById(R.id.songName);
        mSubText = (TextView) rootView.findViewById(R.id.artistAlbumName);
        mListView = (DragSortListView) rootView.findViewById(R.id.queue_drawer_list_view);
        mEmptyInfoText = (TextView) rootView.findViewById(R.id.queue_drawer_empty_text);

        mPlayPauseBackground.setBackgroundResource(UIElementsHelper.getShadowedCircle(mContext));
        mPlayPauseButton.setId(R.drawable.pause_light);

        mTitleText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mSubText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mEmptyInfoText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Regular"));

        //Set the click listeners.
        mMiniPlayerLayout.setOnClickListener(mOnClickMiniPlayer);
        mPlayPauseBackground.setOnClickListener(playPauseClickListener);
        mPlayPauseButton.setOnClickListener(playPauseClickListener);
        mNextButton.setOnClickListener(mOnClickNextListener);
        mPreviousButton.setOnClickListener(mOnClickPreviousListener);

        //Restrict all touch events to this fragment.
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }

        });

        //KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Common.getNavigationBarHeight(mContext);
            if (mListView!=null) {
                mListView.setPadding(0, 0, 0, navBarHeight);
                mListView.setClipToPadding(false);
            }

        }

		return rootView;
	}

    /**
     * Broadcast receiver interface that will update this activity as necessary.
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            if (bundle.containsKey(Common.UPDATE_PAGER_POSTIION)) {
                //Update the queue fragment with the new song info.
                initMiniPlayer();
                initListViewAdapter(false);

            }

            //Updates the playback control buttons.
            if (intent.hasExtra(Common.UPDATE_PLAYBACK_CONTROLS))
                setPlayPauseButton();

            if (bundle.containsKey(Common.SERVICE_STOPPING)) {
                showEmptyTextView();

            }

        }

    };

    /**
     * Helper method that checks whether the audio playback service
     * is running or not.
     */
    private void checkServiceRunning() {
        if (mApp.isServiceRunning() && mApp.getService().getCursor()!=null) {
            initMiniPlayer();
            setPlayPauseButton();
            initListViewAdapter(mInitListViewParams);
        } else {
            showEmptyTextView();
        }

    }

    /**
     * Initializes the mini player above the current queue.
     */
    private void initMiniPlayer() {
        mMiniPlayerLayout.setVisibility(View.VISIBLE);
        mMiniPlayerAlbumArt.setImageBitmap(mApp.getService().getCurrentSong().getAlbumArt());
        mTitleText.setText(mApp.getService().getCurrentSong().getTitle());
        mSubText.setText(mApp.getService().getCurrentSong().getAlbum() + " - " +
                         mApp.getService().getCurrentSong().getArtist());

    }

    /**
     * Initializes the drag sort list view.
     *
     * @param initViewParams Pass true if the ListView is being
     *                       initialized for the very first time
     *                       (dividers, background colors and other
     *                       layout settings will be applied). Pass
     *                       false if the list just needs to be updated
     *                       with the current song.
     */
	private void initListViewAdapter(boolean initViewParams) {

        if (initViewParams) {
            //Reset the initialization flag.
            mInitListViewParams = false;

            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider));
            } else {
                mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider_light));
            }

            mListView.setDividerHeight(1);
            mListView.setFastScrollEnabled(true);

            //KitKat ListView margins.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                //Calculate navigation bar height.
                int navigationBarHeight = 0;
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                mListView.setClipToPadding(false);
                mListView.setPadding(0, 0, 0, navigationBarHeight);

            }

        }

        mListViewAdapter = new QueueDrawerAdapter(mContext, mApp.getService().getPlaybackIndecesList());
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(onClick);
        mListView.setDropListener(onDrop);
        mListView.setRemoveListener(onRemove);

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(mListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mListView.setFloatViewManager(simpleFloatViewManager);

        mListView.setVisibility(View.VISIBLE);
        mEmptyInfoText.setVisibility(View.INVISIBLE);

        /*
         * If the drawer is open, the user is probably scrolling through
         * the list already, so don't move the list to the new position.
         */
        if (!isDrawerOpen())
            mListView.setSelection(mApp.getService().getCurrentSongIndex());

    }

    /**
     * Sets the play/pause button states.
     */
    private void setPlayPauseButton() {
        if (mApp.isServiceRunning()) {
            if (mApp.getService().isPlayingMusic())
                animatePlayToPause();
            else
                animatePauseToPlay();

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
     * Click listener for the play/pause button.
     */
    private View.OnClickListener playPauseClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            //BZZZT! Give the user a brief haptic feedback touch response.
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            //Update the playback UI elements.
            if (mApp.getService().isPlayingMusic())
                animatePauseToPlay();
            else
                animatePlayToPause();

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
    private View.OnClickListener mOnClickPreviousListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            mApp.getService().skipToPreviousTrack();

        }

    };

    /**
     * Click listener for the next button.
     */
    private View.OnClickListener mOnClickNextListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            mApp.getService().skipToNextTrack();

        }

    };

    /**
     * Called if the audio playback service is not running.
     */
    public void showEmptyTextView() {
        mMiniPlayerLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.INVISIBLE);
        mEmptyInfoText.setVisibility(View.VISIBLE);

    }

    /**
     * Click listener for the mini player.
     */
    private View.OnClickListener mOnClickMiniPlayer = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, NowPlayingActivity.class);
            startActivity(intent);
        }

    };

    /**
     * Click listener for the ListView.
     */
    private AdapterView.OnItemClickListener onClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mApp.isServiceRunning())
                mApp.getService().skipToTrack(position);

        }

    };

    /**
     * Drag and drop interface for the ListView.
     */
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {

        @Override
        public void drop(int from, int to) {
            if (from!=to) {
                int fromItem = mListViewAdapter.getItem(from);
                int toItem = mListViewAdapter.getItem(to);
                mListViewAdapter.remove(fromItem);
                mListViewAdapter.insert(fromItem, to);

                //If the current song was reordered, change currentSongIndex and update the next song.
                if (from==mApp.getService().getCurrentSongIndex()) {
                    mApp.getService().setCurrentSongIndex(to);

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from > mApp.getService().getCurrentSongIndex() && to <= mApp.getService().getCurrentSongIndex()) {
                    //One of the next songs was moved to a position before the current song. Move currentSongIndex forward by 1.
                    mApp.getService().incrementCurrentSongIndex();
                    mApp.getService().incrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from < mApp.getService().getCurrentSongIndex() && to==mApp.getService().getCurrentSongIndex()) {
                	/* One of the previous songs was moved to the current song's position (visually speaking,
                	 * the new song will look like it was placed right after the current song.
                	 */
                    mApp.getService().decrementCurrentSongIndex();
                    mApp.getService().decrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from < mApp.getService().getCurrentSongIndex() && to > mApp.getService().getCurrentSongIndex()) {
                    //One of the previous songs was moved to a position after the current song. Move currentSongIndex back by 1.
                    mApp.getService().decrementCurrentSongIndex();
                    mApp.getService().decrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                }

                //If the next song was reordered, reload it with the new index.
                if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                    if (fromItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1) ||
                            toItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                        //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                        mApp.getService().prepareAlternateMediaPlayer();

                    }

                } else {
                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();

                }

            }

            //Fire a broadcast that notifies all listeners that the current queue order has changed.
            String[] updateFlags = { Common.NEW_QUEUE_ORDER };
            String[] flagValues = { "" };
            mApp.broadcastUpdateUICommand(updateFlags, flagValues);

        }

    };

    /**
     * Click remove interface for the ListView.
     */
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {

        @Override
        public void remove(int which) {

            //Stop the service if we just removed the last (and only) song.
            if (mApp.getService().getPlaybackIndecesList().size()==1) {
                mContext.stopService(new Intent(mContext, AudioPlaybackService.class));
                return;
            }

            //If the song that was removed is the next song, reload it.
            if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                if (mListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();

                } else if (mListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                    mApp.getService().incrementCurrentSongIndex();
                    mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
                    mApp.getService().decrementCurrentSongIndex();
                } else if (mListViewAdapter.getItem(which) < mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                    mApp.getService().decrementCurrentSongIndex();
                }

            } else if (which==(mApp.getService().getPlaybackIndecesList().size()-1) &&
                    mApp.getService().getCurrentSongIndex()==(mApp.getService().getPlaybackIndecesList().size()-1)) {
                //The current song was the last one and it was removed. Time to back up to the previous song.
                mApp.getService().decrementCurrentSongIndex();
                mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
            } else {
                //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                mApp.getService().prepareAlternateMediaPlayer();

            }

            //Remove the item from the adapter.
            mListViewAdapter.remove(mListViewAdapter.getItem(which));

        }

    };

    @Override
    public void onResume() {
        super.onResume();
        checkServiceRunning();
        mPlayPauseBackground.setBackgroundResource(UIElementsHelper.getShadowedCircle(mContext));
        mPlayPauseButton.setId(R.drawable.pause_light);

    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(mContext)
                             .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));

    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        super.onStop();

    }

    public boolean isDrawerOpen() {
        return mDrawerOpen;
    }

    public void setIsDrawerOpen(boolean isOpen) {
        mDrawerOpen = isOpen;
    }

}
