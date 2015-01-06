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
package com.aniruddhc.acemusic.player.PlaybackKickstarter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.DBHelpers.MediaStoreAccessHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity.NowPlayingActivityListener;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService.PrepareServiceListener;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * Initiates the playback sequence and 
 * starts AudioPlaybackService.
 * 
 * @author Saravan Pantham
 */
public class PlaybackKickstarter implements NowPlayingActivityListener, PrepareServiceListener {

	private Context mContext;
	private Common mApp;
	
	private String mQuerySelection;
	private int mPlaybackRouteId;
	private int mCurrentSongIndex;
    private boolean mPlayAll;

    public PlaybackKickstarter(Context context) {
        mContext = context;
    }

	private BuildCursorListener mBuildCursorListener;
	
	/**
	 * Public interface that provides access to 
	 * major events during the cursor building 
	 * process.
	 * 
	 * @author Saravan Pantham
	 */
	public interface BuildCursorListener {
		
		/**
		 * Called when the service cursor has been prepared successfully.
		 */
		public void onServiceCursorReady(Cursor cursor, int currentSongIndex, boolean playAll);
		
		/**
		 * Called when the service cursor failed to be built. 
		 * Also returns the failure reason via the exception's
         * message parameter.
		 */
		public void onServiceCursorFailed(String exceptionMessage);

        /**
         * Called when/if the service is already running and
         * should update its cursor. The service's cursor may
         * need to be updated if the user tapped on "Save
         * current position", etc.
         */
        public void onServiceCursorUpdated(Cursor cursor);
	
	}
	
	/**
	 * Helper method that calls all the required method(s) 
	 * that initialize music playback. This method should 
	 * always be called when the cursor for the service 
	 * needs to be changed.
	 */
	public void initPlayback(Context context, 
						     String querySelection,
							 int playbackRouteId,
							 int currentSongIndex,
							 boolean showNowPlayingActivity,
                             boolean playAll) {

		mApp = (Common) mContext.getApplicationContext();
		mQuerySelection = querySelection;
		mPlaybackRouteId = playbackRouteId;
		mCurrentSongIndex = currentSongIndex;
        mPlayAll = playAll;
		
		if (showNowPlayingActivity) {
			//Launch NowPlayingActivity.
			Intent intent = new Intent(mContext, NowPlayingActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(NowPlayingActivity.START_SERVICE, true);
			mContext.startActivity(intent);
			
		} else {
			//Start the playback service if it isn't running.
			if (!mApp.isServiceRunning()) {
				startService();
			} else {
				//Call the callback method that will start building the new cursor.
				mApp.getService()
					.getPrepareServiceListener()
					.onServiceRunning(mApp.getService());
			}
			
		}
		
	}
	
	/**
	 * Starts AudioPlaybackService. Once the service is running, we get a
	 * callback to onServiceRunning() (see below). That's where the method to 
	 * build the cursor is called.
	 */
	private void startService() {
		Intent intent = new Intent(mContext, AudioPlaybackService.class);
		mContext.startService(intent);
	}

    /**
     * Requeries the database to update the current
     * service cursor.
     */
    public void updateServiceCursor() {
        new AsyncBuildCursorTask(true).execute();

    }

	/**
	 * Builds the cursor that will be used for playback. Once the cursor 
	 * is built, AudioPlaybackService receives a callback via
	 * onServiceCursorReady() (see below). The service then takes over 
	 * the rest of the process.
	 */
	class AsyncBuildCursorTask extends AsyncTask<Boolean, String, Cursor> {

        private boolean mIsUpdating = false;

        public AsyncBuildCursorTask(boolean isUpdating) {
            mIsUpdating = isUpdating;
        }

		@Override
		protected Cursor doInBackground(Boolean... params) {
			


            if (mPlaybackRouteId==Common.PLAY_ALL_IN_FOLDER)
                //Return a cursor directly from MediaStore.
                return MediaStoreAccessHelper.getAllSongsWithSelection(mContext,
                                                                       mQuerySelection,
                                                                       null,
                                                                       MediaStore.Audio.Media.DATA + " ASC");

            else
                return mApp.getDBAccessHelper().getPlaybackCursor(mContext, mQuerySelection, mPlaybackRouteId);

			
		}

        @Override
        public void onProgressUpdate(String... params) {
            getBuildCursorListener().onServiceCursorFailed(params[0]);
        }
		
		@Override
		public void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			if (cursor!=null) {
                if (!mIsUpdating)
                    getBuildCursorListener().onServiceCursorReady(cursor, mCurrentSongIndex, mPlayAll);
                else
                    getBuildCursorListener().onServiceCursorUpdated(cursor);

            } else {
                getBuildCursorListener().onServiceCursorFailed("Playback cursor null.");
            }

		}
		
	}

	@Override
	public void onServiceRunning(AudioPlaybackService service) {
		//Build the cursor and pass it on to the service.
		mApp = (Common) mContext.getApplicationContext();
		mApp.setIsServiceRunning(true);
		mApp.setService(service);
		mApp.getService().setPrepareServiceListener(this);
		mApp.getService().setCurrentSongIndex(mCurrentSongIndex);
		new AsyncBuildCursorTask(false).execute();
		
	}

	@Override
	public void onServiceFailed(Exception exception) {
		//Can't move forward from this point.
		exception.printStackTrace();
		Toast.makeText(mContext, R.string.unable_to_start_playback, Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onNowPlayingActivityReady() {
		//Start the playback service if it isn't running.
		if (!mApp.isServiceRunning()) {
			startService();
		} else {
			//Call the callback method that will start building the new cursor.
			mApp.getService()
				.getPrepareServiceListener()
				.onServiceRunning(mApp.getService());
		}
		
	}

    public BuildCursorListener getBuildCursorListener() {
        return mBuildCursorListener;
    }

    public void setBuildCursorListener(BuildCursorListener listener) {
        mBuildCursorListener = listener;
    }

    public String getPreviousQuerySelection() {
        return mQuerySelection;
    }

    public int getPreviousPlaybackRouteId() {
        return mPlaybackRouteId;
    }

    public int getPreviousCurrentSongIndex() {
        return mCurrentSongIndex;
    }

}
