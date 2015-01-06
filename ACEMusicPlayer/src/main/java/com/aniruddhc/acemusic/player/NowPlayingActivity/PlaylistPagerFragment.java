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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.Animations.TranslateAnimation;
import com.aniruddhc.acemusic.player.Dialogs.ABRepeatDialog;
import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Helpers.SongHelper;
import com.aniruddhc.acemusic.player.Helpers.SongHelper.AlbumArtLoadedListener;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.ImageTransformers.PicassoMirrorReflectionTransformer;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class PlaylistPagerFragment extends Fragment implements AlbumArtLoadedListener {

	private Context mContext;
	private Common mApp;
	private ViewGroup mRootView;
	private int mPosition;
	private SongHelper mSongHelper;
	private PopupMenu popup;

    private RelativeLayout bottomDarkPatch;
    private RelativeLayout songInfoLayout;
	private TextView songNameTextView;
	private TextView artistAlbumNameTextView;
	private ImageView coverArt;
	private ImageView overflowIcon;
	
	private boolean mAreLyricsVisible = false;
	private ScrollView mLyricsScrollView;
	private TextView mLyricsTextView;
	private TextView mLyricsEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        mContext = getActivity();
        mApp = (Common) mContext.getApplicationContext();

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlist_pager_fill, container, false);
        mPosition = getArguments().getInt("POSITION");

        overflowIcon = (ImageView) mRootView.findViewById(R.id.now_playing_overflow_icon);
    	coverArt = (ImageView) mRootView.findViewById(R.id.coverArt);
        bottomDarkPatch = (RelativeLayout) mRootView.findViewById(R.id.bottomDarkPatch);
        songInfoLayout = (RelativeLayout) mRootView.findViewById(R.id.songInfoLayout);
        songNameTextView = (TextView) mRootView.findViewById(R.id.songName);
    	artistAlbumNameTextView = (TextView) mRootView.findViewById(R.id.artistAlbumName);
    	
    	mLyricsScrollView = (ScrollView) mRootView.findViewById(R.id.lyrics_scroll_view);
    	mLyricsTextView = (TextView) mRootView.findViewById(R.id.lyrics);
    	mLyricsEmptyTextView = (TextView) mRootView.findViewById(R.id.lyrics_empty);
        
    	mLyricsTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	mLyricsEmptyTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	songNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	artistAlbumNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

        //Allow the TextViews to scroll if they extend beyond the layout margins.
        songNameTextView.setSelected(true);
        artistAlbumNameTextView.setSelected(true);
    	
    	//Initialize the pop up menu.
    	popup = new PopupMenu(getActivity(), overflowIcon);
		popup.getMenuInflater().inflate(R.menu.now_playing_overflow_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItemClickListener);

        mSongHelper = new SongHelper();
        mSongHelper.setAlbumArtLoadedListener(this);

        if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE)
            mSongHelper.populateSongData(mContext, mPosition);
		else
            mSongHelper.populateSongData(mContext, mPosition, new PicassoMirrorReflectionTransformer());

    	songNameTextView.setText(mSongHelper.getTitle());
    	artistAlbumNameTextView.setText(mSongHelper.getAlbum() + " - " + mSongHelper.getArtist());
        overflowIcon.setOnClickListener(overflowClickListener);

        //Kitkat padding.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navigationBarHeight = Common.getNavigationBarHeight(mContext);
            int bottomPadding = songInfoLayout.getPaddingBottom();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomDarkPatch.getLayoutParams();

            if (navigationBarHeight > 0) {
                /* The nav bar already has padding, so remove the extra 15dp
                 * padding that was applied in the layout file.
                 */
                int marginPixelsValue = (int) mApp.convertDpToPixels(15, mContext);
                bottomPadding -= marginPixelsValue;
                params.height -= marginPixelsValue;
            }

            bottomPadding += navigationBarHeight;
            songInfoLayout.setPadding(0, 0, 0, bottomPadding);

            params.height += navigationBarHeight;
            bottomDarkPatch.setLayoutParams(params);

        }
    	
        return mRootView;
    }

    /**
     * Overflow button click listener.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            //Hide the "Current queue" item if it's already visible.
            if (mApp.isTabletInLandscape())
                popup.getMenu().findItem(R.id.current_queue).setVisible(false);

            popup.show();
        }

    };

    /**
     * Menu item click listener for the overflow pop up menu.
     */
    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.equalizer:
                    Intent intent = new Intent(getActivity(), EqualizerActivity.class);
                    startActivity(intent);
                    break;
                case R.id.save_clear_current_position:
                    String songId = mApp.getService().getCurrentSong().getId();
                    if (item.getTitle().equals(mContext.getResources().getString(R.string.save_current_position))) {
                        item.setTitle(R.string.clear_saved_position);

                        long currentPositionMillis = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
                        String message = mContext.getResources().getString(R.string.track_will_resume_from);
                        message += " " + mApp.convertMillisToMinsSecs(currentPositionMillis);
                        message += " " + mContext.getResources().getString(R.string.next_time_you_play_it);

                        mApp.getDBAccessHelper().setLastPlaybackPosition(songId, currentPositionMillis);
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();

                    } else {
                        item.setTitle(R.string.save_current_position);
                        mApp.getDBAccessHelper().setLastPlaybackPosition(songId, -1);
                        Toast.makeText(mContext, R.string.track_start_from_beginning_next_time_play, Toast.LENGTH_LONG).show();

                    }

                    //Requery the database and update the service cursor.
                    mApp.getPlaybackKickstarter().updateServiceCursor();

                    break;
                case R.id.show_embedded_lyrics:
                    if (item.getTitle().equals(mContext.getResources().getString(R.string.show_embedded_lyrics))) {
                        AsyncLoadLyricsTask task = new AsyncLoadLyricsTask();
                        task.execute();
                        item.setTitle(R.string.hide_lyrics);
                    } else {
                        hideLyrics();
                        item.setTitle(R.string.show_embedded_lyrics);
                    }

                    break;
                case R.id.a_b_repeat:
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ABRepeatDialog dialog = new ABRepeatDialog();
                    dialog.show(ft, "repeatSongRangeDialog");
                    break;
                case R.id.current_queue:
                    ((NowPlayingActivity) getActivity()).toggleCurrentQueueDrawer();
                    break;
                case R.id.go_to:
                    PopupMenu goToPopupMenu = new PopupMenu(getActivity(), overflowIcon);
                    goToPopupMenu.inflate(R.menu.show_more_menu);
                    goToPopupMenu.setOnMenuItemClickListener(goToMenuClickListener);
                    goToPopupMenu.show();
                    break;
            }

            return false;
        }

    };

    /**
     * "Go to" popup menu item click listener.
     */
    private PopupMenu.OnMenuItemClickListener goToMenuClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.go_to_this_artist:
                    break;
                case R.id.go_to_this_album_artist:
                    break;
                case R.id.go_to_this_album:
                    break;
                case R.id.go_to_this_genre:
                    break;
            }

            return false;
        }

    };

    /**
     * Callback method for album art loading.
     */
	@Override
	public void albumArtLoaded() {
		coverArt.setImageBitmap(mSongHelper.getAlbumArt());
	}

    /**
     * Reads lyrics from the audio file's tag and displays them.
     */
	class AsyncLoadLyricsTask extends AsyncTask<Boolean, Boolean, Boolean> {

		String mLyrics = "";
		
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			String songFilePath = mApp.getService().getCurrentSong().getFilePath();
			AudioFile audioFile = null;
			Tag tag = null;
			try {
				audioFile = AudioFileIO.read(new File(songFilePath));
				
				if (audioFile!=null)
					tag = audioFile.getTag();
				else
					return false;
				
				if (tag!=null)
					mLyrics = tag.getFirst(FieldKey.LYRICS);
				else
					return false;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (mLyrics!=null && !mLyrics.isEmpty()) {
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.VISIBLE);
				mLyricsEmptyTextView.setVisibility(View.INVISIBLE);
			} else {
				mLyrics = mContext.getResources().getString(R.string.no_embedded_lyrics_found);
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.INVISIBLE);
				mLyricsEmptyTextView.setVisibility(View.VISIBLE);
			}
			
			//Slide up the album art to show the lyrics.
	    	TranslateAnimation slideUpAnimation = new TranslateAnimation(coverArt, 400, new AccelerateInterpolator(), 
					 													 View.INVISIBLE,
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, -2.0f);

	    	slideUpAnimation.animate();
	    	
		}
		
	}

    /**
     * Slides down the album art to hide lyrics.
     */
    private void hideLyrics() {
        TranslateAnimation slideDownAnimation = new TranslateAnimation(coverArt, 400, new DecelerateInterpolator(2.0f),
                                                                       View.VISIBLE,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f,
                                                                       Animation.RELATIVE_TO_SELF, -2.0f,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f);

        slideDownAnimation.animate();
    }

}
