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
package com.aniruddhc.acemusic.player.AsyncTasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncAddToQueueTask extends AsyncTask<Boolean, Integer, Boolean> {
	
    private Context mContext;
    private Common mApp;
    
    private String mArtistName;
    private String mAlbumName;
    private String mSongTitle;
    private String mGenreName;
    private String mPlaylistId;
    private String mPlaylistName;
    private String mAlbumArtistName;
    
    private Fragment mFragment;
    private Cursor mCursor;
    private String mEnqueueType;
    private int originalPlaybackIndecesSize = 0;
    private boolean mPlayNext = false;
    private String mPlayingNext = "";
    
    public AsyncAddToQueueTask(Context context,
    						   Fragment fragment,
    						   String enqueueType,
    						   String artistName,
    						   String albumName,
    						   String songTitle,
    						   String genreName,
    						   String playlistId, 
    						   String playlistName,
    						   String albumArtistName) {
    	
    	mContext = context;
    	mApp = (Common) mContext;
    	
    	mArtistName = artistName;
    	mAlbumName = albumName;
    	mSongTitle = songTitle;
    	mGenreName = genreName;
    	mPlaylistId = playlistId;
    	mPlaylistName = playlistName;
    	mAlbumArtistName = albumArtistName;
    	
    	mFragment = fragment;
    	mEnqueueType = enqueueType;
    	
    	if (mApp.getService().getPlaybackIndecesList()!=null) {
    		originalPlaybackIndecesSize = mApp.getService().getPlaybackIndecesList().size();
    	}
    	
    }
    
    protected void onPreExecute() {
		super.onPreExecute();
    }
 
    @Override
    protected Boolean doInBackground(Boolean... params) {
		
    	//Specifies if the user is trying to add song(s) to play next.
    	if (params.length > 0) {
    		mPlayNext = params[0];
    	}
    	
    	
		//Escape any rogue apostrophes.
		if (mArtistName!=null && mArtistName.contains("'")) {
			mArtistName = mArtistName.replace("'", "''");
		}
		
		if (mAlbumName!=null && mAlbumName.contains("'")) {
			mAlbumName = mAlbumName.replace("'", "''");
		}
		
		if (mSongTitle!=null && mSongTitle.contains("'")) {
			mSongTitle = mSongTitle.replace("'", "''");
		}
		
		if (mGenreName!=null && mGenreName.contains("''")) {
			mGenreName = mGenreName.replace("'", "''");
		}
		
		if (mAlbumArtistName!=null && mAlbumArtistName.contains("'")) {
			mAlbumArtistName = mAlbumArtistName.replace("'", "''");
		}
		
		//Fetch the cursor based on the type of set of songs that are being enqueued.
		assignCursor();

		//Check if the service is currently active.
		if (mApp.isServiceRunning()) {
			
			if (mPlayNext) {
				/* Loop through the mCursor of the songs that will be enqueued and add the 
				 * loop's counter value to the size of the current mCursor. This will add 
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList. 
				 * The new indeces must be placed after the current song's index.
				 */
				int playNextIndex = 0;
				if (mApp.isServiceRunning()) {
					playNextIndex = mApp.getService().getCurrentSongIndex() + 1;
				}
				
				for (int i=0; i < mCursor.getCount(); i++) {
					mApp.getService().getPlaybackIndecesList().add(playNextIndex + i, 
																   mApp.getService().getCursor().getCount() + i);
				}
				
			} else {
				/* Loop through the mCursor of the songs that will be enqueued and add the 
				 * loop's counter value to the size of the current mCursor. This will add 
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList.
				 */
				for (int i=0; i < mCursor.getCount(); i++) {
					mApp.getService().getPlaybackIndecesList().add(mApp.getService().getCursor().getCount() + i);
				}
				
			}
			
			mApp.getService().enqueueCursor(mCursor, mPlayNext);
			
		} else {
			//The service doesn't seem to be running. We'll explicitly stop it, just in case, and then launch NowPlayingActivity.class.
			Intent serviceIntent = new Intent(mContext, AudioPlaybackService.class);
			mContext.stopService(serviceIntent);
			
			publishProgress(new Integer[] {0});
		}
		
		publishProgress(new Integer[] {1});
		
    	return true;
    }
    
    //Retrieves and assigns the cursor based on the set of song(s) that are being enqueued.
    private void assignCursor() {
    	
    	DBAccessHelper dbHelper = new DBAccessHelper(mContext);
    	
    	if (mEnqueueType.equals("SONG")) {
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
    					  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
    					  + DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
  					  	  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
  					  	  + DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}

    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
   													   	   null, 
   													   	   selection, 
   													   	   null, 
   													   	   null, 
   													   	   null, 
   													   	   DBAccessHelper.SONG_TITLE + " ASC");
    		
    		mPlayingNext = mSongTitle;
    	} else if (mEnqueueType.equals("ARTIST")) {
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}
    		
    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
    													   null, 
    													   selection, 
    													   null, 
    													   null, 
    													   null, 
    													   DBAccessHelper.SONG_ALBUM + " ASC" + ", " + DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");	
    		
    		mPlayingNext = mArtistName;
    	} else if (mEnqueueType.equals("ALBUM")) {
    		
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
    					  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
  					  	  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}
    		
    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
														   null, 
														   selection, 
														   null, 
														   null, 
														   null, 
														   DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");
    		
    		mPlayingNext = mAlbumName;
    	} else if (mEnqueueType.equals("ALBUM_BY_ALBUM_ARTIST")) { 
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
    					  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
    					  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}
    		
    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
														   null, 
														   selection, 
														   null, 
														   null, 
														   null, 
														   DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");
    		
    		mPlayingNext = mAlbumName;
    	} else if (mEnqueueType.equals("ALBUM_ARTIST")) {
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}
    		
    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
														   null, 
														   selection, 
														   null, 
														   null, 
														   null, 
														   DBAccessHelper.SONG_ALBUM + " ASC, " + DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");
    	
    		mPlayingNext = mAlbumArtistName;
    	} else if (mEnqueueType.equals("TOP_25_PLAYED")) {
    		
    		String selection = null;
            if (mApp.isGooglePlayMusicEnabled()==false) {
            	selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
            			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            } else {
            	selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            }
            
            mCursor = dbHelper.getTop25PlayedTracks(selection);
            mPlayingNext = mContext.getResources().getString(R.string.the_top_25_played_tracks);
    	} else if (mEnqueueType.equals("RECENTLY_ADDED")) {
    		String selection = null;
            if (mApp.isGooglePlayMusicEnabled()==false) {
            	selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
            			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            } else {
            	selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            }
            
            mCursor = dbHelper.getRecentlyAddedSongs(selection);
            mPlayingNext =  mContext.getResources().getString(R.string.the_most_recently_added_songs);
    	} else if (mEnqueueType.equals("TOP_RATED")) {
    		String selection = null;
            if (mApp.isGooglePlayMusicEnabled()==false) {
            	selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
            			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            } else {
            	selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            }
            
            mCursor = dbHelper.getTopRatedSongs(selection);
            mPlayingNext =  mContext.getResources().getString(R.string.the_top_rated_songs);
    	} else if (mEnqueueType.equals("RECENTLY_PLAYED")) {
    		String selection = null;
            if (mApp.isGooglePlayMusicEnabled()==false) {
            	selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
            			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            } else {
            	selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
            }
            
            mCursor = dbHelper.getRecentlyPlayedSongs(selection);
            mPlayingNext =  mContext.getResources().getString(R.string.the_most_recently_played_songs);
    	} else if (mEnqueueType.equals("PLAYLIST")) {
           /* String selection = " AND " + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + "." 
            				 + DBAccessHelper.PLAYLIST_ID + "=" + "'" + mPlaylistId + "'";
            
            if (mApp.isGooglePlayMusicEnabled()) {
            	mCursor = dbHelper.getAllSongsInPlaylistSearchable(selection);
            } else {
            	mCursor = dbHelper.getLocalSongsInPlaylistSearchable(selection);
            }
    		
            mPlayingNext = mPlaylistName;*/
    	} else if (mEnqueueType.equals("GENRE")) {
    		
    		String selection = null;
    		if (mApp.isGooglePlayMusicEnabled()) {
    			selection = DBAccessHelper.SONG_GENRE + "=" + "'" + mGenreName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    		} else {
    			selection = DBAccessHelper.SONG_GENRE + "=" + "'" + mGenreName + "'" + " AND "
	   				 	  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
  					  	  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		}
    		
    		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
												   		   null, 
												   		   selection, 
												   		   null, 
												   		   null, 
												   		   null, 
												   		   DBAccessHelper.SONG_ALBUM + " ASC, " + 
												   		   DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");
    		
    		mPlayingNext = mGenreName;
    	}
    	
    	
    }
    
    @Override
    protected void onProgressUpdate(Integer... values) {
    	super.onProgressUpdate(values);
    	int value = values[0];
    	
    	switch(value) {
    	case 0:
			Intent intent = new Intent(mContext, NowPlayingActivity.class);
			
			//Get the parameters for the first song.
			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				
				if (mEnqueueType.equals("ARTIST")) {
					intent.putExtra("PLAY_ALL", "ARTIST");
					intent.putExtra("CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT");
				} else if (mEnqueueType.equals("ALBUM_ARTIST")) {
					intent.putExtra("PLAY_ALL", "ALBUM_ARTIST");
					intent.putExtra("CALLING_FRAGMENT", "ALBUM_ARTISTS_FLIPPED_FRAGMENT");
				} else if (mEnqueueType.equals("ALBUM")) {
					intent.putExtra("PLAY_ALL", "ALBUM");
					intent.putExtra("CALLING_FRAGMENT", "ALBUMS_FLIPPED_FRAGMENT");
				} else if (mEnqueueType.equals("PLAYLIST")) {
					intent.putExtra("CALLING_FRAGMENT", "PLAYLISTS_FLIPPED_FRAGMENT");
					intent.putExtra("PLAYLIST_NAME", mPlaylistName);
				} else if (mEnqueueType.equals("GENRE")) {
					intent.putExtra("PLAY_ALL", "GENRE");
					intent.putExtra("CALLING_FRAGMENT", "GENRES_FLIPPED_FRAGMENT");
				} else if (mEnqueueType.equals("ALBUM_ARTIST")) {
					intent.putExtra("CALLING_FRAGMENT", "ALBUM_ARTISTS_FLIPPED_FRAGMENT");
					intent.putExtra("PLAY_ALL", "ALBUM_ARTIST");
				} else if (mEnqueueType.equals("SONG")) {
					intent.putExtra("CALLING_FRAGMENT", "SONGS_FRAGMENT");
					intent.putExtra("SEARCHED", true);
				} else if (mEnqueueType.equals("ALBUM_BY_ALBUM_ARTIST")) {
					intent.putExtra("CALLING_FRAGMENT", "ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT");
					intent.putExtra("PLAY_ALL", "ALBUM");
				}

    			intent.putExtra("SELECTED_SONG_DURATION", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
				intent.putExtra("SELECTED_SONG_TITLE", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
				intent.putExtra("SELECTED_SONG_ARTIST", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
				intent.putExtra("SELECTED_SONG_ALBUM", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
				intent.putExtra("SELECTED_SONG_ALBUM_ARTIST",  mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST)));
				intent.putExtra("SONG_SELECTED_INDEX", 0);
				intent.putExtra("SELECTED_SONG_DATA_URI", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
				intent.putExtra("SELECTED_SONG_GENRE", mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
				intent.putExtra("NEW_PLAYLIST", true);
				intent.putExtra("NUMBER_SONGS", mCursor.getCount());
				intent.putExtra("CALLED_FROM_FOOTER", false);
				intent.putExtra(Common.CURRENT_LIBRARY, mApp.getCurrentLibrary());
				
			} else {
				Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_LONG).show();
				break;
			}
			
			mFragment.getActivity().startActivity(intent);
			mFragment.getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    		break;
    	case 1:
    		int numberOfSongs = mCursor.getCount();
    		String toastMessage = "";
    		if (numberOfSongs==1) {
    			if (mPlayNext) {
    				toastMessage = mPlayingNext + " " + mContext.getResources().getString(R.string.will_be_played_next);
    			} else {
    				toastMessage = numberOfSongs + " " + mContext.getResources().getString(R.string.song_enqueued_toast);
    			}
    			
    		} else {
    			if (mPlayNext) {
    				toastMessage = mPlayingNext + " " + mContext.getResources().getString(R.string.will_be_played_next);
    			} else {
    				toastMessage = numberOfSongs + " " + mContext.getResources().getString(R.string.songs_enqueued_toast);
    			}
    			
    		}
    		
    		Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
    		break;
    	}
    	
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
		//Send out a broadcast that loads the new queue across the app.
		Intent intent = new Intent("com.aniruddhc.acemusic.player.NEW_SONG_UPDATE_UI");
		intent.putExtra("MESSAGE", "com.aniruddhc.acemusic.player.NEW_SONG_UPDATE_UI");
		intent.putExtra("INIT_QUEUE_DRAWER_ADAPTER", true);
		
    	//Start preparing the next song if the current song is the last track.
		if (mApp.getService().getCurrentSongIndex()==(originalPlaybackIndecesSize - 1)) {

			//Check if the service is running.
			if (mApp.isServiceRunning()) {
				mApp.getService().prepareAlternateMediaPlayer();
				
			}
			
		}
		
	}

}
