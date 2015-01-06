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

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

public class NowPlayingQueueFragment extends Fragment {

	private Context mContext;
	public NowPlayingQueueFragment nowPlayingQueueFragment = null;
	private SharedPreferences sharedPreferences;
	
	public DragSortListView nowPlayingQueueListView;
	public NowPlayingQueueListViewAdapter nowPlayingQueueListViewAdapter;
	public int contextMenuItemIndex;

	public TextView noMusicPlaying;
	public ImageView nowPlayingAlbumArt;
	public TextView nowPlayingSongTitle;
	public TextView nowPlayingSongArtist;
	public RelativeLayout nowPlayingSongContainer;
	
	public ProgressBar progressBar;
	public ImageButton playPauseButton;
	public ImageButton previousButton;
	public ImageButton nextButton;
	
	public int index;
	public View childView;
	public float progressFraction;
	public int currentProgress;
	public int totalDuration;
	public int currentProgressCountDown;
	public Handler mHandler = new Handler();
	private BroadcastReceiver receiver;
	private Common mApp;
	
	public DisplayMetrics displayMetrics;
	public int screenWidth;
	public int screenHeight;
	public static Cursor mCursor;
	private boolean CALLED_FROM_REMOVE = false;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	//Inflate the correct layout based on the selected theme.
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        nowPlayingQueueFragment = this;
        sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
        
        mCursor = mApp.getService().getCursor();
        View rootView = (ViewGroup) inflater.inflate(R.layout.now_playing_queue_layout, container, false);

        receiver = new BroadcastReceiver() {
        	
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSongInfo();
            }
            
        };
        
        //Notify the application that this fragment is now visible.
        sharedPreferences.edit().putBoolean("NOW_PLAYING_QUEUE_VISIBLE", true).commit();
        
    	//Get the screen's parameters.
	    displayMetrics = new DisplayMetrics();
	    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	    screenWidth = displayMetrics.widthPixels;
	    screenHeight = displayMetrics.heightPixels;
        
        noMusicPlaying = (TextView) rootView.findViewById(R.id.now_playing_queue_no_music_playing);
        nowPlayingAlbumArt = (ImageView) rootView.findViewById(R.id.now_playing_queue_album_art);
        nowPlayingSongTitle = (TextView) rootView.findViewById(R.id.now_playing_queue_song_title);
        nowPlayingSongArtist = (TextView) rootView.findViewById(R.id.now_playing_queue_song_artist);
        nowPlayingSongContainer = (RelativeLayout) rootView.findViewById(R.id.now_playing_queue_current_song_container);
        
        noMusicPlaying.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        nowPlayingSongTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        nowPlayingSongArtist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        
        nowPlayingQueueListView = (DragSortListView) rootView.findViewById(R.id.now_playing_queue_list_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.now_playing_queue_progressbar);
        playPauseButton = (ImageButton) rootView.findViewById(R.id.now_playing_queue_play);
        nextButton = (ImageButton) rootView.findViewById(R.id.now_playing_queue_next);
        previousButton = (ImageButton) rootView.findViewById(R.id.now_playing_queue_previous);
        
		//Apply the card layout's background based on the color theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			nowPlayingQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			nowPlayingQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			nowPlayingQueueListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF000000);
			nowPlayingQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			nowPlayingQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			nowPlayingQueueListView.setLayoutParams(layoutParams);
		}
        
        //Set the Now Playing container layout's background.
        nowPlayingSongContainer.setBackgroundColor(UIElementsHelper.getNowPlayingQueueBackground(mContext));
        
        //Loop through the service's cursor and retrieve the current queue's information.
        if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==false || mApp.getService().getCurrentMediaPlayer()==null) {
        	
        	//No audio is currently playing.
        	noMusicPlaying.setVisibility(View.VISIBLE);
        	nowPlayingAlbumArt.setImageBitmap(mApp.decodeSampledBitmapFromResource(R.drawable.default_album_art, screenWidth/3, screenWidth/3));
        	nowPlayingQueueListView.setVisibility(View.GONE);
        	nowPlayingSongTitle.setVisibility(View.GONE);
        	nowPlayingSongArtist.setVisibility(View.GONE);
        	progressBar.setVisibility(View.GONE);
        	
        } else {
        	
        	//Set the current play/pause conditions.
        	try {
        		
        		//Hide the progressBar and display the controls.
        		progressBar.setVisibility(View.GONE);
        		playPauseButton.setVisibility(View.VISIBLE);
        		nextButton.setVisibility(View.VISIBLE);
        		previousButton.setVisibility(View.VISIBLE);
        		
        		if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
        			playPauseButton.setImageResource(R.drawable.pause_holo_light);
        		} else {
        			playPauseButton.setImageResource(R.drawable.play_holo_light);
        		}
        	} catch (Exception e) {
        		/* The mediaPlayer hasn't been initialized yet, so let's just keep the controls 
        		 * hidden for now. Once the mediaPlayer is initialized and it starts playing, 
        		 * updateSongInfo() will be called, and we can show the controls/hide the progressbar 
        		 * there. For now though, we'll display the progressBar.
        		 */
        		progressBar.setVisibility(View.VISIBLE);
        		playPauseButton.setVisibility(View.GONE);
        		nextButton.setVisibility(View.GONE);
        		previousButton.setVisibility(View.GONE);
        	}
        	
    		//Retrieve and set the current title/artist/artwork.
    		mCursor.moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
    		String currentTitle = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_TITLE));
    		String currentArtist = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
    		
    		nowPlayingSongTitle.setText(currentTitle);
    		nowPlayingSongArtist.setText(currentArtist);
    		
    		File file = new File(mContext.getExternalCacheDir() + "/current_album_art.jpg");
    		Bitmap bm = null;
    		if (file.exists()) {
    			bm = mApp.decodeSampledBitmapFromFile(file, screenWidth, screenHeight);
    			nowPlayingAlbumArt.setScaleX(1.0f);
    			nowPlayingAlbumArt.setScaleY(1.0f);
    		} else {
    			int defaultResource = UIElementsHelper.getIcon(mContext, "default_album_art");
    			bm = mApp.decodeSampledBitmapFromResource(defaultResource, screenWidth, screenHeight);
    			nowPlayingAlbumArt.setScaleX(0.5f);
    			nowPlayingAlbumArt.setScaleY(0.5f);
    		}
    		
    		nowPlayingAlbumArt.setImageBitmap(bm);
            noMusicPlaying.setPaintFlags(noMusicPlaying.getPaintFlags() 
            							 | Paint.ANTI_ALIAS_FLAG
            							 | Paint.SUBPIXEL_TEXT_FLAG);
            
            nowPlayingSongTitle.setPaintFlags(nowPlayingSongTitle.getPaintFlags() 
    									 	  | Paint.ANTI_ALIAS_FLAG 
    									 	  | Paint.FAKE_BOLD_TEXT_FLAG
    									 	  | Paint.SUBPIXEL_TEXT_FLAG);
            
            nowPlayingSongArtist.setPaintFlags(nowPlayingSongArtist.getPaintFlags() 
    									 	   | Paint.ANTI_ALIAS_FLAG
    									 	   | Paint.SUBPIXEL_TEXT_FLAG);
            
            /* Set the adapter. We'll pass in playbackIndecesList as the adapter's data backend.
             * The array can then be manipulated (reordered, items removed, etc) with no restrictions. 
             * Each integer element in the array will be used as a pointer to a specific cursor row, 
             * so there's no need to fiddle around with the actual cursor itself. */
            nowPlayingQueueListViewAdapter = new NowPlayingQueueListViewAdapter(getActivity(), mApp.getService().getPlaybackIndecesList());
            
            nowPlayingQueueListView.setAdapter(nowPlayingQueueListViewAdapter);
    		nowPlayingQueueListView.setFastScrollEnabled(true);
    		nowPlayingQueueListView.setDropListener(onDrop);
    		nowPlayingQueueListView.setRemoveListener(onRemove);
    		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(nowPlayingQueueListView);
    		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
    		nowPlayingQueueListView.setFloatViewManager(simpleFloatViewManager);
            
    		//Scroll down to the current song.
    		nowPlayingQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
    		
            nowPlayingQueueListView.setOnItemClickListener(new OnItemClickListener() {

    			@Override
    			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
    				mApp.getService().skipToTrack(index);
    				
    			}
            	
            });
            
            playPauseButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mApp.getService().togglePlaybackState();
				}
            	
            });
            
            nextButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mApp.getService().skipToNextTrack();
				}
            	
            });
            
            previousButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mApp.getService().skipToPreviousTrack();
				}
            	
            });
            
        }
        		
        return rootView;
    }
    
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
    	
        @Override
        public void drop(int from, int to) {
            if (from!=to) {
                int fromItem = nowPlayingQueueListViewAdapter.getItem(from);
                int toItem = nowPlayingQueueListViewAdapter.getItem(to);
                nowPlayingQueueListViewAdapter.remove(fromItem);
                nowPlayingQueueListViewAdapter.insert(fromItem, to);
                
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
            
        }
        
    };
    
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
    	
        @Override
        public void remove(int which) {
        	CALLED_FROM_REMOVE = true;
        	//Stop the service if we just removed the last (and only) song.
        	if (mApp.getService().getPlaybackIndecesList().size()==1) {
        		getActivity().stopService(new Intent(getActivity(), AudioPlaybackService.class));
        		return;
        	}
        	
            //If the song that was removed is the next song, reload it.
            if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                if (nowPlayingQueueListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                } else if (nowPlayingQueueListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().incrementCurrentSongIndex();
                	mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
                	mApp.getService().decrementCurrentSongIndex();
                } else if (nowPlayingQueueListViewAdapter.getItem(which) < mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().decrementCurrentSongIndex();
                }
                
            } else {
            	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
            	mApp.getService().prepareAlternateMediaPlayer();
            	
            }
            
            //Remove the item from the adapter.
            nowPlayingQueueListViewAdapter.remove(nowPlayingQueueListViewAdapter.getItem(which));
            
        }
        
    };

    //Used by the service to update the album art when a song changes.
    public void updateSongInfo() {

    	if (mCursor!=null && (mApp.getService().getPlaybackIndecesList().size() > 0)) {
    		
    		if (CALLED_FROM_REMOVE) {
    			if ((mApp.getService().getCurrentSongIndex()-1) < mApp.getService().getPlaybackIndecesList().size() &&
    				 (mApp.getService().getCurrentSongIndex()-1) > -1) {
    				mCursor.moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()-1));
    			}
    		} else {
    			if (mApp.getService().getCurrentSongIndex() < mApp.getService().getPlaybackIndecesList().size()) {
    				mCursor.moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
    			}
    			
    		}
    		
    		//Retrieve and set the current title/artist/artwork.
    		
    		String currentTitle = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_TITLE));
    		String currentArtist = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
    		
    		nowPlayingSongTitle.setText(currentTitle);
    		nowPlayingSongArtist.setText(currentArtist);    		
    		
    		File file = new File(mContext.getExternalCacheDir() + "/current_album_art.jpg");
    		Bitmap bm = null;
    		if (file.exists()) {
    			bm = mApp.decodeSampledBitmapFromFile(file, screenWidth, screenHeight);
    			nowPlayingAlbumArt.setScaleX(1.0f);
    			nowPlayingAlbumArt.setScaleY(1.0f);
    		} else {
    			int defaultResource = UIElementsHelper.getIcon(mContext, "default_album_art");
    			bm = mApp.decodeSampledBitmapFromResource(defaultResource, screenWidth, screenHeight);
    			nowPlayingAlbumArt.setScaleX(0.5f);
    			nowPlayingAlbumArt.setScaleY(0.5f);
    		}

    		nowPlayingAlbumArt.setImageBitmap(bm);
			progressBar.setVisibility(View.GONE);
			playPauseButton.setVisibility(View.VISIBLE);
			previousButton.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);

    		//Set the controls.
    		if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
    			playPauseButton.setImageResource(R.drawable.pause_holo_light);
    		} else {
    			playPauseButton.setImageResource(R.drawable.play_holo_light);
    		}
    		
    	} else {
    		//The service is stopped, so reset the fragment back to its uninitialized state.
    		//nowPlayingAlbumArt.setImageBitmap(NowPlayingQueueActivity.defaultArtworkBitmap);
        	noMusicPlaying.setVisibility(View.VISIBLE);
        	nowPlayingQueueListView.setVisibility(View.GONE);
        	nowPlayingSongTitle.setVisibility(View.GONE);
        	nowPlayingSongArtist.setVisibility(View.GONE);
        	
			nowPlayingSongTitle.setText("");
			nowPlayingSongArtist.setText("");
			nowPlayingAlbumArt.setImageBitmap(mApp.decodeSampledBitmapFromResource(R.drawable.default_album_art, screenWidth, screenWidth));
			
			progressBar.setVisibility(View.GONE);
			playPauseButton.setVisibility(View.GONE);
			previousButton.setVisibility(View.GONE);
			nextButton.setVisibility(View.GONE);
			
    	}
    	
    	//Update the listview.
    	nowPlayingQueueListViewAdapter.notifyDataSetChanged();
    	
    	CALLED_FROM_REMOVE = false;
		
    }
    
    //Called every 100ms to update the progress bar/remaining time fields.
    public Runnable progressBarRunnable = new Runnable() {

		@Override
		public void run() {
			
			try {
				
				currentProgressCountDown =  (mApp.getService().getCurrentMediaPlayer().getDuration()) - (mApp.getService().getCurrentMediaPlayer().getCurrentPosition());
				currentProgress = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
				totalDuration = mApp.getService().getCurrentMediaPlayer().getDuration();
				progressFraction = (float) currentProgress/totalDuration;
				
				if (mApp.getService().getCurrentMediaPlayer()!=null) {
					mHandler.postDelayed(progressBarRunnable, 100);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
    	
    };
    
    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	
    	if (v.getId()==R.id.playlist_flipped_list_view) {
    		
    		menu.setHeaderTitle(R.string.song_actions);
    		String[] menuItems = getResources().getStringArray(R.array.playlist_songs_context_menu_items);
    
    		for (int i=0; i < menuItems.length; i++) {
    			menu.add(9383, i, i, menuItems[i]);
    		}
    		
    	}
    	
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        
    	if (item.getGroupId()==9383) {
    		
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        	index = info.position;
        	childView = info.targetView;
        	
        	String filePath = songFilePathsList.get(index);
    		
    		//Convert the filePath to an absolute file path.
        	File file = new File(filePath);
        	try {
				filePath = file.getCanonicalPath().toString();
			} catch (IOException e) {
				Toast.makeText(mContext, R.string.file_could_not_be_opened, Toast.LENGTH_SHORT).show();
				return super.onContextItemSelected(item);
			}
    		
    		switch(item.getItemId()) {
        	case 0:
        		//View Song Information.
        		break;
        	case 1:
        		//Enqueue.
        		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
        		
        		//Escape any rogue apostrophes.
        		if (filePath.contains("'")) {
        			filePath = filePath.replace("'", "''");
        		}
        		
        		String selection = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + filePath + "'";
        		Cursor cursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
        															 null, 
        															 selection, 
        															 null, 
        															 null, 
        															 null, 
        															 null);	
       
        		//Check if the service is currently active.
        		if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true && 
        			mApp.getService().getCursor()!=null && mApp.getService().getCurrentMediaPlayer()!=null) {
        			
        			//The service is running, so we can go ahead and append the new cursor to the old cursor.
        			AudioPlaybackService.enqueueCursor(cursor);
        			
        		} else {
        			//The service doesn't seem to be running. We'll explicitly stop it, just in case, and then launch NowPlayingActivity.class.
        			Intent serviceIntent = new Intent(mContext, AudioPlaybackService.class);
        			mContext.stopService(serviceIntent);
        			
        			Intent intent = new Intent(mContext, NowPlayingActivity.class);
        			
        			//Get the parameters for the first song.
        			if (cursor.getCount() > 0) {
        				cursor.moveToFirst();
        				
            			intent.putExtra("SELECTED_SONG_DURATION", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
        				intent.putExtra("SELECTED_SONG_TITLE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
        				intent.putExtra("SELECTED_SONG_ARTIST", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
        				intent.putExtra("SELECTED_SONG_ALBUM", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
        				intent.putExtra("SONG_SELECTED_INDEX", 0);
        				intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
        				intent.putExtra("SELECTED_SONG_GENRE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
        				intent.putExtra("NEW_PLAYLIST", true);
        				intent.putExtra("NUMBER_SONGS", cursor.getCount());
        				intent.putExtra("CALLED_FROM_FOOTER", false);
        				intent.putExtra("PLAY_ALL", "");
        				intent.putExtra("CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT");
            			
        			}
        			
        			startActivity(intent);
    				getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    				
    				cursor.close();
    				dbHelper.close();
        			
        		}
        		
        		//Unescape any rogue apostrophes.
        		if (filePath.contains("''")) {
        			filePath = filePath.replace("''", "'");
        		}
        		
        		int numberOfSongs = cursor.getCount();
        		String toastMessage = "";
        		if (numberOfSongs==1) {
        			toastMessage = numberOfSongs + " " + getResources().getString(R.string.song_enqueued_toast);
        		} else {
        			toastMessage = numberOfSongs + " " + getResources().getString(R.string.songs_enqueued_toast);
        		}
        		
        		Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
        		
        		break;
        	case 2:
        		//Remove from playlist.
        		break;
        	
    		}
    		
    	}

        return super.onContextItemSelected(item);
    }*/
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	if (mHandler!=null) {
        	mHandler.removeCallbacks(progressBarRunnable);
        	mHandler = null;
    	}
    	
    	if (this.isRemoving()) {
    		if (mCursor!=null) {
    			mCursor.close();
    			mCursor = null;
    		}
    		
    	}
    	
    	nowPlayingQueueFragment = null;
    	sharedPreferences.edit().putBoolean("NOW_PLAYING_QUEUE_VISIBLE", false).commit();
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (mHandler!=null) {
        	mHandler.removeCallbacks(progressBarRunnable);
        	mHandler = null;
    	}

    	nowPlayingQueueFragment = null;
    	sharedPreferences.edit().putBoolean("NOW_PLAYING_QUEUE_VISIBLE", false).commit();
    	
    }
    
	@Override
	public void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(mContext)
	    					 .registerReceiver((receiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
	
	}

	@Override
	public void onStop() {
	    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
	    super.onStop();
	    
	}

}
