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
/* package com.jams.music.player.AsyncTasks;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.LastCursorInfoDBHelper;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

*//**
 * Builds the cursor that will be used by the app's main service.
 * 
 * @author Saravan Pantham
 *//*
public class AsyncBuildServiceCursorTask extends AsyncTask<String, Integer, Boolean> {

	private Context mContext;
	private Common mApp;
	
	private String mCallingFragment;
	private Bundle mBundle;
	private String mCurrentLibrary;
	private ArrayList<String> mAudioFilePathsInFolder = new ArrayList<String>();
	private String mSongTitle;
	private String mSongArtist;
	private String mSongAlbum;
	private String mSongGenre;
	private String mSongAlbumArtist;
	private Cursor mCursor;
	
	public AsyncBuildServiceCursorTask(Context context, 
									   String callingFragment, 
									   Bundle bundle,
									   String currentLibrary, 
									   ArrayList<String> audioFilePathsInFolder,
									   String songTitle,
									   String songArtist,
									   String songAlbum,
									   String songGenre,
									   String songAlbumArtist) {
		
		mContext = context;
		mApp = (Common) mContext;
		
		mCallingFragment = callingFragment;
		mBundle = bundle;
		mCurrentLibrary = currentLibrary;
		mAudioFilePathsInFolder = audioFilePathsInFolder;
		mSongTitle = songTitle;
		mSongArtist = songArtist;
		mSongAlbum = songAlbum;
		mSongGenre = songGenre;
		mSongAlbumArtist = songAlbumArtist;
		
	}
	
	@Override
	public void onPreExecute() {
		super.onPreExecute();
		
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		initializeDatabaseCursor();
		
		 Save the current cursor parameters to mApp.getSharedPreferences().
		 * This will allow the user to resume their previous playlist
		 * even if they manually kill it.
		 
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_CALLING_FRAGMENT", mCallingFragment).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_CURRENT_LIBRARY", mCurrentLibrary).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_SONG_TITLE", mSongTitle).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_SONG_ARTIST", mSongArtist).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_SONG_ALBUM", mSongAlbum).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_SONG_GENRE", mSongGenre).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_SONG_ALBUM_ARTIST", mSongAlbumArtist).commit();
		mApp.getSharedPreferences().edit().putString("LAST_CURSOR_PLAY_ALL", mBundle.getString("PLAY_ALL")).commit();
		mApp.getSharedPreferences().edit().putBoolean("LAST_CURSOR_SEARCHED", mBundle.getBoolean("SEARCHED")).commit();
		
		if (mAudioFilePathsInFolder!=null) {
			
			for (int i=0; i < mAudioFilePathsInFolder.size(); i++) {
				
				try {
					ContentValues values = new ContentValues();
					values.put(LastCursorInfoDBHelper.AUDIO_FILE_PATH, mAudioFilePathsInFolder.get(i));
					mApp.getDBAccessHelper().getWritableDatabase().insert(LastCursorInfoDBHelper.LAST_CURSOR_INFO, null, values);
					
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
			
		}
		
		return null;
	}
	
	//Constructs the DB mCursor for the music player service, based on the calling fragment.
    public void initializeDatabaseCursor() {

    	if (mSongTitle!=null) {
    		
    		if (mSongTitle.contains("'")) {
    			mSongTitle = mSongTitle.replace("'", "''");
    		}
    		
    	}
    	
    	if (mSongArtist!=null) {
    		
    	    if (mSongArtist.contains("'")) {
    	    	mSongArtist = mSongArtist.replace("'", "''");
    	    }
    	    
    	}

    	if (mSongAlbum!=null) {
    		
    	    if (mSongAlbum.contains("'")) {
    	    	mSongAlbum = mSongAlbum.replace("'", "''");
    	    }
    	    
    	}
	    
    	if (mSongGenre!=null) {
    		
    	    if (mSongGenre.contains("'")) {
    	    	mSongGenre = mSongGenre.replace("'", "''");
    	    }
    	    
    	}
    	
    	if (mSongAlbumArtist!=null) {
    		
    		if (mSongAlbumArtist.contains("'")) {
    			mSongAlbumArtist = mSongAlbumArtist.replace("'", "''");
    		}
    		
    	}
    	
    	if (mCurrentLibrary!=null) {
    		
    		if (mCurrentLibrary.contains("'")) {
    			mCurrentLibrary = mCurrentLibrary.replace("'", "''");
    		}
    		
    	}
    	
    	if (mBundle.getBoolean("SEARCHED")==true) {
    		String selection = DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'" + " AND "
    				  + DBAccessHelper.SONG_ARTIST + "= " + "'" + mSongArtist + "'" + " AND "
    				  + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'" + " AND "
    				  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'";
    		mCursor = mApp.getDBAccessHelper().getSongsInAlbum(selection, null);
    		mApp.getService().setCursor(mCursor);
    		
        	if (mSongArtist!=null) {
        		
        	    if (mSongArtist.contains("''")) {
        	    	mSongArtist = mSongArtist.replace("''", "'");
        	    }
        	    
        	}
    	    
        	if (mSongAlbum!=null) {
        		
        	    if (mSongAlbum.contains("'")) {
        	    	mSongAlbum = mSongAlbum.replace("''", "'");
        	    }
        	    
        	}
    	    
        	if (mSongGenre!=null) {
        		
        	    if (mSongGenre.contains("''")) {
        	    	mSongGenre = mSongGenre.replace("''", "'");
        	    }
        	    
        	}
    	    
        	if (mSongTitle!=null) {
        		
        		if (mSongTitle.contains("''")) {
        			mSongTitle = mSongTitle.replace("''", "'");
        		}
        		
        	}
        	
    		return;
    	}
    	
    	if (mCurrentLibrary!=null) {
            mCurrentLibrary = mCurrentLibrary.replace("'", "''");
    	}
    	
    	if (mCallingFragment.equals("SONGS_FRAGMENT")) {
            
    		String selection = "";
            if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
            	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
            		selection = "";
            	} else {
            		selection = " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
            	}
            	mCursor = mApp.getDBAccessHelper().getAllSongsSearchable(selection);
            	mApp.getNowPlayingActivity().setCursor(mCursor);
    	    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
    		    selection = " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
    	        mCursor = mApp.getDBAccessHelper().getAllSongsSearchable(selection);
    	        mApp.getNowPlayingActivity().setCursor(mCursor);
    	    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    	    	//Check if Google Play Music is enabled.
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    	    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    	    		mCursor = mApp.getDBAccessHelper().getAllSongsSearchable(selection);
    	    		mApp.getNowPlayingActivity().setCursor(mCursor);
    	    	} else {
    	    		selection = " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
    		        mCursor = mApp.getDBAccessHelper().getAllSongsSearchable(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    	    	}
    	    	
        	} else {
            	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
            		selection = " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
            	} else {
            		selection = " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'"
            	              + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
            	}
    		    
    	        mCursor = mApp.getDBAccessHelper().getAllSongsInLibrarySearchable(selection);
    	        mApp.getNowPlayingActivity().setCursor(mCursor);
    	    }

    		mApp.getService().setCursor(mCursor);
    	
    	} else if (mCallingFragment.equals("ALBUM_ARTISTS_FLIPPED_FRAGMENT")) {
            
    		String selection = "";
    		if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist  + "'";
    	    	} else {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist  + "'"
    						  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    	    	}
    		    
    	        mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtist(selection);
    	    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" 
	    				  + " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
	    		mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtist(selection);
    		    
    	    } else {
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist  + "'" 
    	    				  + " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    	    	} else {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist  + "'" 
    	    				  + " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'" 
    	    				  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    	    	}
    	        mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistInLibrary(selection);
    	    }

    		mApp.getService().setCursor(mCursor);
    		AudioPlaybackService.originalCursor = mCursor;
    	
    	} else if (mCallingFragment.equals("PLAYLISTS_FLIPPED_FRAGMENT")) {
    		String playlistName = mBundle.getString("PLAYLIST_NAME");
    		
            //Retrieve the appropriate mCursor for the playlist.
            playlistName = playlistName.replace("'", "''");
            String selection = " AND " + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + "." 
      			  	  		 + DBAccessHelper.PLAYLIST_NAME 
      			  	  		 + "=" + "'" + playlistName + "'";
            playlistName = playlistName.replace("''", "'");
            
            if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
            	mCursor = mApp.getDBAccessHelper().getAllSongsInPlaylistSearchable(selection);
            	mApp.getNowPlayingActivity().setCursor(mCursor);
            } else {
            	mCursor = mApp.getDBAccessHelper().getLocalSongsInPlaylistSearchable(selection);
            	mApp.getNowPlayingActivity().setCursor(mCursor);
            }
            
    		mApp.getService().setCursor(mCursor);

    	} else if (mCallingFragment.equals("GENRES_FLIPPED_SONGS_FRAGMENT")) {
    		//The cursor needs to be built from the songs table.
    		
    		String selection = "";
    		if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    						  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'";
    	    	} else {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    						  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    						  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    	    	}
    		    
    	        mCursor = mApp.getDBAccessHelper().getAllSongsInAlbumInGenre(selection);
    	        mApp.getNowPlayingActivity().setCursor(mCursor);
    	    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
    	    	selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
						  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'" + " AND " + 
						  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
	    		mCursor = mApp.getDBAccessHelper().getAllSongsInAlbumInGenre(selection);
	    		mApp.getNowPlayingActivity().setCursor(mCursor);
	    		
    	    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    	    	//Check if Google Play Music is enabled.
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
    	    			      + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    	    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    	    		mCursor = mApp.getDBAccessHelper().getAllSongsInAlbumInGenre(selection);
    	    		mApp.getNowPlayingActivity().setCursor(mCursor);
    	    		
    	    	} else {
    	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
    	    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
    	    		mCursor = mApp.getDBAccessHelper().getAllSongsInAlbumInGenre(selection);
    	    		mApp.getNowPlayingActivity().setCursor(mCursor);
    	    		
    	    	}
    	    	
        	} else {
    	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    						  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'" + " AND " 
    						  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    	    	} else {selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    					  + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'" + " AND " 
    					  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'" + " AND "
    					  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    	    	}
    	        mCursor = mApp.getDBAccessHelper().getAllSongsByInAlbumInGenreInLibrary(selection);
    	        mApp.getNowPlayingActivity().setCursor(mCursor);
    	        
    	    }

    		mApp.getService().setCursor(mCursor);
    		
    	} else if (mCallingFragment.equals("TOP_25_PLAYED_SONGS")) {
    		String selection = "";
        	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        		selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        	} else {
        		selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'"
        	              + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
        	}
    		
			mCursor = mApp.getDBAccessHelper().getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
														  null, 
														  selection, 
														  null, 
														  null, 
														  null, 
														  DBAccessHelper.SONG_PLAY_COUNT + "*1 DESC", 
														  "25");
			
			mApp.getNowPlayingActivity().setCursor(mCursor);
			mApp.getService().setCursor(mCursor);
    	
    	} else if (mCallingFragment.equals("RECENTLY_ADDED")) {
    		String selection = "";
    		
        	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        		selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        	} else {
        		selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'"
        	              + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
        	}
        	
			mCursor = mApp.getDBAccessHelper().getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
																			  null, 
																			  selection, 
																			  null, 
																			  null, 
																			  null, 
																			  DBAccessHelper.ADDED_TIMESTAMP + "*1 DESC");

			mApp.getNowPlayingActivity().setCursor(mCursor);
			mApp.getService().setCursor(mCursor);
    		
    	} else if (mCallingFragment.equals("TOP_RATED")) {
    		String selection = "";
        	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        		selection = DBAccessHelper.RATING + "<>" + "'0'" + " AND "
   					      + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        	} else {
        		selection = DBAccessHelper.RATING + "<>" + "'0'" + " AND "
   					      + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'"
        	              + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
        	}
    		
			mCursor = mApp.getDBAccessHelper().getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
					  null, 
					  selection, 
					  null, 
					  null, 
					  null, 
					  DBAccessHelper.RATING + "*1 DESC");

			mApp.getNowPlayingActivity().setCursor(mCursor);
			mApp.getService().setCursor(mCursor);
    	
    	} else if (mCallingFragment.equals("RECENTLY_PLAYED")) {
    		String selection = "";
        	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        		selection = DBAccessHelper.LAST_PLAYED_TIMESTAMP + "<>" + "'0'" + " AND "
   					      + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        	} else {
        		selection = DBAccessHelper.LAST_PLAYED_TIMESTAMP + "<>" + "'0'" + " AND "
   					      + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'"
        	              + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
        	}
        	
			mCursor = mApp.getDBAccessHelper().getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
					  null, 
					  selection, 
					  null, 
					  null, 
					  null, 
					  DBAccessHelper.LAST_PLAYED_TIMESTAMP + "*1 DESC");

			mApp.getNowPlayingActivity().setCursor(mCursor);
			mApp.getService().setCursor(mCursor);
			
    	} else if (mCallingFragment.equals("FOLDERS_FRAGMENT")) {
    		
    		//We'll create a matrix mCursor that includes all the audio files within the specified folder.
    		String[] foldersCursorColumns = {  DBAccessHelper.SONG_ARTIST,  
											   DBAccessHelper.SONG_ALBUM, 
											   DBAccessHelper.SONG_TITLE, 
											   DBAccessHelper.SONG_FILE_PATH,
											   DBAccessHelper.SONG_DURATION, 
											   DBAccessHelper.SONG_GENRE, 
											   DBAccessHelper.SONG_SOURCE, 
											   DBAccessHelper.SONG_ALBUM_ART_PATH, 
											   DBAccessHelper.SONG_ID, 
											   DBAccessHelper.LOCAL_COPY_PATH, 
											   DBAccessHelper.LAST_PLAYBACK_POSITION };
						    		
    		MatrixCursor foldersCursor = new MatrixCursor(foldersCursorColumns, mAudioFilePathsInFolder.size());
    		MediaMetadataRetriever mmdr = new MediaMetadataRetriever();

    		String genre = "";
    		String songSource = "LOCAL_FILE";
    		String songAlbumArtPath = "";
    		String songId = "FOLDER";
    		boolean cursorChanged = false;
    		
    		for (int i=0; i < mAudioFilePathsInFolder.size(); i++) {

    			try {
    				
    				//Extract metadata from the file, (if it exists).
        			try {
        				mmdr.setDataSource(mAudioFilePathsInFolder.get(i));
        			} catch (Exception e) {
        				//Just keep going. The service will handle the resulting error from this file.
        			}
        			
        			String artist = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        			if (artist==null || artist.isEmpty()) {
        				artist = "Unknown Artist";
        			}
        			
        			String album = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        			if (album==null || album.isEmpty()) {
        				album = "Unknown Album";
        			}
        			
        			String title = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        			if (title==null || title.isEmpty()) {
        				title = mAudioFilePathsInFolder.get(i);
        			}
        			
        			String filePath = mAudioFilePathsInFolder.get(i);
        			String duration = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        			if (duration==null || duration.isEmpty()) {
        				duration = "0";
        			}
        			
    				filePath = mAudioFilePathsInFolder.get(i);
        			foldersCursor.addRow(new Object[] { artist,
        												album,
        												title,
        												filePath,
        												duration,
        												genre,
        												songSource,
        												songAlbumArtPath,
        												songId, 
        												"", 
        												"" });
        			
    				if (i >= 5 && cursorChanged==false) {
    					mApp.getNowPlayingActivity().setIsCursorLoaded(true);
    					
    					mCursor = (Cursor) foldersCursor;
    					mApp.getNowPlayingActivity().setCursor(mCursor);
    		    		mApp.getService().setCursor((Cursor) foldersCursor);
    		    		cursorChanged = true;
    				}
        			
    			} catch (Exception e) {
    				e.printStackTrace();
    				continue;
    			}

    		}
    		
    		mCursor = (Cursor) foldersCursor;
    		mApp.getNowPlayingActivity().setCursor(mCursor);
    		mApp.getService().setCursor((Cursor) foldersCursor);
    		
    	} else {

    		//Check if the "Play all" button was pressed (or the song was selected from an album).
    		if (mBundle.getString("PLAY_ALL").equals("ARTIST")) {

    			String selection = "";
    		    if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {

    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		    	}

    		        mCursor = mApp.getDBAccessHelper().playAllArtistsFlippedAllLibraries(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
    			    selection = " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'" + " AND "
    			    		  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'";
    		        mCursor = mApp.getDBAccessHelper().getAllSongsByArtistSearchable(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    		    	//Check if Google Play Music is enabled.
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
    		    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    		    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    		    	
    		    	mCursor = mApp.getDBAccessHelper().getAllSongsByArtistSearchable(selection);
    		    	mApp.getNowPlayingActivity().setCursor(mCursor);
    	    	} else {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND "
      		    			      + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND "
    		    			      + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    		    	
    		        mCursor = mApp.getDBAccessHelper().getAllSongsByArtistInLibrary(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    }
    			
    			mApp.getService().setCursor(mCursor);
    			
    		} else if (mBundle.getString("PLAY_ALL").equals("ALBUM_ARTIST")) {
    			
    			String selection = "";
    		    if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {

    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		    	}

    		        mCursor = mApp.getDBAccessHelper().playAllAlbumArtistsFlippedAllLibraries(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
    			    selection = " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'" + " AND "
    			    		  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'";
    		        mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistSearchable(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    		    	//Check if Google Play Music is enabled.
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    		    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    		    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    		    		mCursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByAlbumArtist(selection);
    		    		mApp.getNowPlayingActivity().setCursor(mCursor);
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
    			        mCursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByAlbumArtist(selection);
    			        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    	}
    		    	
    	    	} else {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" + " AND "
      		    			      + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" + " AND "
    		    			      + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    		    	
    		        mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistInLibrary(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    }
    			
    			mApp.getService().setCursor(mCursor);
    			
    		} else if (mBundle.getString("PLAY_ALL").equals("ALBUM") && 
    				   mBundle.getString("CALLING_FRAGMENT").equals("ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT")) {
    			
    			String selection = "";
    			if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    							  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    							  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    							  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    			    
    		        mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistAlbum(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		        
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
							  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" + " AND " + 
							  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
		    		mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistAlbum(selection);
		    		mApp.getNowPlayingActivity().setCursor(mCursor);
    			    
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    		    	//Check if Google Play Music is enabled.
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
    		    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    		    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    		    		mCursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByAlbumArtist(selection);
    		    		mApp.getNowPlayingActivity().setCursor(mCursor);
    		    		
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'"
    		    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
    			        mCursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByAlbumArtist(selection);
    			        mApp.getNowPlayingActivity().setCursor(mCursor);
    		    	}
    		    	
    	    	} else {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    							  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" + " AND " 
    							  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    		    	} else {selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
    						  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mSongAlbumArtist + "'" + " AND " 
    						  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'" + " AND "
    						  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    		    	mCursor = mApp.getDBAccessHelper().getAllSongsByAlbumArtistAlbumInLibrary(selection);
    		    	mApp.getNowPlayingActivity().setCursor(mCursor);
    		    }
    			
    			mApp.getService().setCursor(mCursor);
    			
    		} else if (mBundle.getString("PLAY_ALL").equals("ALBUM")) {
    	    				 
    	    	String selection = "";
			    if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
			    	
			    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
								  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'";
			    	} else {
			    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
								  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
								  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
			    	}

			        mCursor = mApp.getDBAccessHelper().getAllSongsByArtistAlbum(selection);
			        mApp.getNowPlayingActivity().setCursor(mCursor);
			    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
				    selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
							  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND " + 
							  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
			        mCursor = mApp.getDBAccessHelper().getAllSongsByArtistAlbum(selection);
			        mApp.getNowPlayingActivity().setCursor(mCursor);
			        
			    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
			    	//Check if Google Play Music is enabled.
			    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
			    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
			    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
			    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
			    	} else {
			    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'"
			    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'"
			    				  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
			    	}
			    	
			    	mCursor = mApp.getDBAccessHelper().getAllSongsByArtistAlbum(selection);
			    	mApp.getNowPlayingActivity().setCursor(mCursor);
			    	
		    	} else {
			    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
								  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND " 
								  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
			    	} else {
			    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum  + "'" + " AND "
								  + DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND " 
								  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'"
								  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
			    	}
				    
			        mCursor = mApp.getDBAccessHelper().getAllSongsByArtistAlbumInLibrary(selection);
			        mApp.getNowPlayingActivity().setCursor(mCursor);
			    }
    			
    			mApp.getService().setCursor(mCursor);
    			
    		} else if (mBundle.getString("PLAY_ALL").equals("GENRE")) {
				 
    			String selection = "";
    			if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    							  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    			    
    		        mCursor = mApp.getDBAccessHelper().getAllSongsInGenre(selection);
    		        mApp.getNowPlayingActivity().setCursor(mCursor);
    		        
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
    		    	selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre  + "'"
		    				  + " AND " +  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
    		    	mCursor = mApp.getDBAccessHelper().getAllSongsInGenre(selection);
    		    	mApp.getNowPlayingActivity().setCursor(mCursor);		    		
    		    	
    		    } else if (mCurrentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
    		    	//Check if Google Play Music is enabled.
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    		    			      + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
    		    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
    		    		mCursor = mApp.getDBAccessHelper().getAllSongsInGenre(selection);
    		    		mApp.getNowPlayingActivity().setCursor(mCursor);
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre + "'"
    		    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
    		    		mCursor = mApp.getDBAccessHelper().getAllSongsInGenre(selection);
    		    		mApp.getNowPlayingActivity().setCursor(mCursor);
    		    	}
    		    	
    	    	} else {
    		    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre  + "'" + " AND " 
    							  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'";
    		    	} else {
    		    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + mSongGenre  + "'" + " AND " 
    		    				  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + mCurrentLibrary + "'" + " AND "
    		    				  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
    		    	}
    		    	
    		    	mCursor = mApp.getDBAccessHelper().getAllSongsInGenreInLibrary(selection);
    		    	mApp.getNowPlayingActivity().setCursor(mCursor);
    		    }
	
    			mApp.getService().setCursor(mCursor);
	
    		} else {

    			if (mSongTitle!=null) {
    				
        			if (mSongTitle.contains("'")) {
        				mSongTitle = mSongTitle.replace("'", "''");
        			}
        			
    			}

    			String selection = "";
    			
    			if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND "
      					      + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'" + " AND "
     						  + DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'" + " AND "
     		    			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
    			} else {
        			selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mSongArtist + "'" + " AND "
      					      + DBAccessHelper.SONG_ALBUM + "=" + "'" + mSongAlbum + "'" + " AND "
     						  + DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'" + " AND "
     		    			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
     						  + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    			}

    			
    			mCursor = mApp.getDBAccessHelper().getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
		   													  null, 
		   													  selection, 
		   													  null, 
		   													  null, 
		   													  null, 
		   													  DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");
    			
    			mApp.getService().setCursor(mCursor);
    			
    		}
    		
    	}
    	
    	if (mCurrentLibrary!=null) {
    		
    		if (mCurrentLibrary.contains("'")) {
    			mCurrentLibrary = mCurrentLibrary.replace("''", "'");
    		}
    		
    	}
    	
    	if (mSongArtist!=null) {
    		
    	    if (mSongArtist.contains("''")) {
    	    	mSongArtist = mSongArtist.replace("''", "'");
    	    }
    	    
    	}
    	
    	if (mSongAlbumArtist!=null) {
    		
    		if (mSongAlbumArtist.contains("''")) {
    			mSongAlbumArtist = mSongAlbumArtist.replace("''", "'");
    		}
    		
    	}
	    
    	if (mSongAlbum!=null) {
    		
    	    if (mSongAlbum.contains("'")) {
    	    	mSongAlbum = mSongAlbum.replace("''", "'");
    	    }
    	    
    	}
	    
    	if (mSongGenre!=null) {
    		
    	    if (mSongGenre.contains("''")) {
    	    	mSongGenre = mSongGenre.replace("''", "'");
    	    }
    	    
    	}
	    
    	if (mSongTitle!=null) {
    		
    		if (mSongTitle.contains("''")) {
    			mSongTitle = mSongTitle.replace("''", "'");
    		}
    		
    	}

    }
	
	@Override
	public void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		
	}
	
	@Override
	public void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
		try {
			mApp.getNowPlayingActivity().setIsCursorLoaded(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
*/
