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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.DBHelpers.MediaStoreAccessHelper;
import com.aniruddhc.acemusic.player.FoldersFragment.FileExtensionFilter;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Services.BuildMusicLibraryService;
import com.aniruddhc.acemusic.player.Utils.Common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * The Mother of all AsyncTasks in this app.
 *
 * @author Saravan Pantham
 */
public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void> {

	private Context mContext;
	private Common mApp;
    private BuildMusicLibraryService mService;
	public ArrayList<OnBuildLibraryProgressUpdate> mBuildLibraryProgressUpdate;
	
	private String mCurrentTask = "";
	private int mOverallProgress = 0;
	private Date date = new Date();

	private String mMediaStoreSelection = null;
	private HashMap<String, String> mGenresHashMap = new HashMap<String, String>();
    private HashMap<String, Integer> mGenresSongCountHashMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> mAlbumsCountMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> mSongsCountMap = new HashMap<String, Integer>();
    private HashMap<String, Uri> mMediaStoreAlbumArtMap = new HashMap<String, Uri>();
	private HashMap<String, String> mFolderArtHashMap = new HashMap<String, String>();
	private MediaMetadataRetriever mMMDR = new MediaMetadataRetriever();
	
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	public AsyncBuildLibraryTask(Context context, BuildMusicLibraryService service) {
		mContext = context;
		mApp = (Common) mContext;
        mService = service;
        mBuildLibraryProgressUpdate = new ArrayList<OnBuildLibraryProgressUpdate>();
	}
	
	/**
	 * Provides callback methods that expose this 
	 * AsyncTask's progress.
	 * 
	 * @author Saravan Pantham
	 */
	public interface OnBuildLibraryProgressUpdate {
		
		/**
		 * Called when this AsyncTask begins executing 
		 * its doInBackground() method.
		 */
		public void onStartBuildingLibrary();
		
		/**
		 * Called whenever mOverall Progress has been updated.
		 */
		public void onProgressUpdate(AsyncBuildLibraryTask task, String mCurrentTask,
                                     int overallProgress, int maxProgress,
                                     boolean mediaStoreTransferDone);
		
		/**
		 * Called when this AsyncTask finishes executing
		 * its onPostExecute() method.
		 */
		public void onFinishBuildingLibrary(AsyncBuildLibraryTask task);
		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mApp.setIsBuildingLibrary(true);
		mApp.setIsScanFinished(false);
		
		if (mBuildLibraryProgressUpdate!=null)
            for (int i=0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i)!=null)
			        mBuildLibraryProgressUpdate.get(i).onStartBuildingLibrary();
		
		// Acquire a wakelock to prevent the CPU from sleeping while the process is running.
		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
								  "com.aniruddhc.acemusic.player.AsyncTasks.AsyncBuildLibraryTask");
		wakeLock.acquire();

	}

	@Override
    protected Void doInBackground(String... params) {
		
		/* 
		 * Get a cursor of songs from MediaStore. The cursor 
		 * is limited by the folders that have been selected 
		 * by the user.
		 */
		mCurrentTask = mContext.getResources().getString(R.string.building_music_library);
		Cursor mediaStoreCursor = getSongsFromMediaStore();
		
		/* 
		 * Transfer the content in mediaStoreCursor over to 
		 * Jams' private database.
		 */
		if (mediaStoreCursor!=null) {
			saveMediaStoreDataToDB(mediaStoreCursor);
			mediaStoreCursor.close();
		}

    	//Save EQ presets to the database.
		saveEQPresets();

        //Notify all listeners that the MediaStore transfer is complete.
        publishProgress(new String[] { "MEDIASTORE_TRANSFER_COMPLETE" });

		//Save album art paths for each song to the database.
		getAlbumArt();
		
    	return null;
    }
	
	/**
	 * Retrieves a cursor of songs from MediaStore. The cursor 
	 * is limited to songs that are within the folders that the user 
	 * selected.
	 */
	private Cursor getSongsFromMediaStore() {
		//Get a cursor of all active music folders.
        Cursor musicFoldersCursor = mApp.getDBAccessHelper().getAllMusicFolderPaths();
        
        //Build the appropriate selection statement.
        Cursor mediaStoreCursor = null;
        String sortOrder = null;
        String projection[] = { MediaStore.Audio.Media.TITLE, 
        						MediaStore.Audio.Media.ARTIST,
        						MediaStore.Audio.Media.ALBUM, 
        						MediaStore.Audio.Media.ALBUM_ID,
        						MediaStore.Audio.Media.DURATION, 
        						MediaStore.Audio.Media.TRACK, 
        						MediaStore.Audio.Media.YEAR, 
        						MediaStore.Audio.Media.DATA, 
        						MediaStore.Audio.Media.DATE_ADDED, 
        						MediaStore.Audio.Media.DATE_MODIFIED, 
        						MediaStore.Audio.Media._ID,
        						MediaStoreAccessHelper.ALBUM_ARTIST };
        
        //Grab the cursor of MediaStore entries.
        if (musicFoldersCursor==null || musicFoldersCursor.getCount() < 1) {
        	//No folders were selected by the user. Grab all songs in MediaStore.
        	mediaStoreCursor = MediaStoreAccessHelper.getAllSongs(mContext, projection, sortOrder);
        } else {
        	//Build a selection statement for querying MediaStore.
            mMediaStoreSelection = buildMusicFoldersSelection(musicFoldersCursor);
            mediaStoreCursor = MediaStoreAccessHelper.getAllSongsWithSelection(mContext, 
            																   mMediaStoreSelection, 
            																   projection, 
            																   sortOrder);

            //Close the music folders cursor.
            musicFoldersCursor.close(); 
        }

    	return mediaStoreCursor;
	}
	
	/**
	 * Iterates through mediaStoreCursor and transfers its data 
	 * over to Jams' private database.
	 */
	private void saveMediaStoreDataToDB(Cursor mediaStoreCursor) {
		try {
    		//Initialize the database transaction manually (improves performance).
    		mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
    		
    		//Clear out the table.
    		mApp.getDBAccessHelper()
    			.getWritableDatabase()
    			.delete(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
    					null, 
    					null);
    		
    		//Tracks the progress of this method.
    		int subProgress = 0;
    		if (mediaStoreCursor.getCount()!=0) {
    			subProgress = 250000/(mediaStoreCursor.getCount());
    		} else {
    			subProgress = 250000/1;
    		}
    		
    		//Populate a hash of all songs in MediaStore and their genres.
    		buildGenresLibrary();

            //Populate a hash of all artists and their number of albums.
            buildArtistsLibrary();

            //Populate a hash of all albums and their number of songs.
            buildAlbumsLibrary();

            //Populate a has of all albums and their album art path.
            buildMediaStoreAlbumArtHash();

    		//Prefetch each column's index.
    		final int titleColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
    		final int artistColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    		final int albumColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            final int albumIdColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
    		final int durationColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
    		final int trackColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
    		final int yearColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
    		final int dateAddedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
    		final int dateModifiedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
    		final int filePathColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
    		final int idColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media._ID);
    		int albumArtistColIndex = mediaStoreCursor.getColumnIndex(MediaStoreAccessHelper.ALBUM_ARTIST);
    		
    		/* The album artist field is hidden by default and we've explictly exposed it.
    		 * The field may cease to exist at any time and if it does, use the artists 
    		 * field instead.
    		 */
    		if (albumArtistColIndex==-1) {
    			albumArtistColIndex = artistColIndex;
    		}
    		
    		//Iterate through MediaStore's cursor and save the fields to Jams' DB.
            for (int i=0; i < mediaStoreCursor.getCount(); i++) {
            	
            	mediaStoreCursor.moveToPosition(i);
            	mOverallProgress += subProgress;
            	publishProgress();
            	
            	String songTitle = mediaStoreCursor.getString(titleColIndex);
            	String songArtist = mediaStoreCursor.getString(artistColIndex);
            	String songAlbum = mediaStoreCursor.getString(albumColIndex);
                String songAlbumId = mediaStoreCursor.getString(albumIdColIndex);
            	String songAlbumArtist = mediaStoreCursor.getString(albumArtistColIndex);
            	String songFilePath = mediaStoreCursor.getString(filePathColIndex);
            	String songGenre = getSongGenre(songFilePath);
            	String songDuration = mediaStoreCursor.getString(durationColIndex);
            	String songTrackNumber = mediaStoreCursor.getString(trackColIndex);
            	String songYear = mediaStoreCursor.getString(yearColIndex);
            	String songDateAdded = mediaStoreCursor.getString(dateAddedColIndex);
            	String songDateModified = mediaStoreCursor.getString(dateModifiedColIndex);
            	String songId = mediaStoreCursor.getString(idColIndex);
                String numberOfAlbums = "" + mAlbumsCountMap.get(songArtist);
                String numberOfTracks = "" + mSongsCountMap.get(songAlbum + songArtist);
                String numberOfSongsInGenre = "" + getGenreSongsCount(songGenre);
            	String songSource = DBAccessHelper.LOCAL;
            	String songSavedPosition = "-1";

                String songAlbumArtPath = "";
                if (mMediaStoreAlbumArtMap.get(songAlbumId)!=null)
                    songAlbumArtPath = mMediaStoreAlbumArtMap.get(songAlbumId).toString();

                if (numberOfAlbums.equals("1"))
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.album_small);
                else
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.albums_small);

                if (numberOfTracks.equals("1"))
                    numberOfTracks += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfTracks += " " + mContext.getResources().getString(R.string.songs_small);

                if (numberOfSongsInGenre.equals("1"))
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.songs_small);

            	//Check if any of the other tags were empty/null and set them to "Unknown xxx" values.
            	if (songArtist==null || songArtist.isEmpty()) {
            		songArtist = mContext.getResources().getString(R.string.unknown_artist);
            	}
            	
            	if (songAlbumArtist==null || songAlbumArtist.isEmpty()) {
            		if (songArtist!=null && !songArtist.isEmpty()) {
            			songAlbumArtist = songArtist;
            		} else {
            			songAlbumArtist = mContext.getResources().getString(R.string.unknown_album_artist);
            		}
            		
            	}
            	
            	if (songAlbum==null || songAlbum.isEmpty()) {
            		songAlbum = mContext.getResources().getString(R.string.unknown_album);;
            	}

                if (songGenre==null || songGenre.isEmpty()) {
                    songGenre = mContext.getResources().getString(R.string.unknown_genre);
                }
            	
            	//Filter out track numbers and remove any bogus values.
            	if (songTrackNumber!=null) {
        			if (songTrackNumber.contains("/")) {
        				int index = songTrackNumber.lastIndexOf("/");
        				songTrackNumber = songTrackNumber.substring(0, index);            	
        			}

                    try {
                        if (Integer.parseInt(songTrackNumber) <= 0) {
                            songTrackNumber = "";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        songTrackNumber = "";
                    }
                	
            	}

                long durationLong = 0;
                try {
                    durationLong = Long.parseLong(songDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            	
            	ContentValues values = new ContentValues();
            	values.put(DBAccessHelper.SONG_TITLE, songTitle);
            	values.put(DBAccessHelper.SONG_ARTIST, songArtist);
            	values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
            	values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
            	values.put(DBAccessHelper.SONG_DURATION, convertMillisToMinsSecs(durationLong));
            	values.put(DBAccessHelper.SONG_FILE_PATH, songFilePath);
            	values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
            	values.put(DBAccessHelper.SONG_GENRE, songGenre);
            	values.put(DBAccessHelper.SONG_YEAR, songYear);
                values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
            	values.put(DBAccessHelper.SONG_LAST_MODIFIED, songDateModified);
                values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
            	values.put(DBAccessHelper.BLACKLIST_STATUS, false);
            	values.put(DBAccessHelper.ADDED_TIMESTAMP, date.getTime());
            	values.put(DBAccessHelper.RATING, 0);
            	values.put(DBAccessHelper.LAST_PLAYED_TIMESTAMP, songDateModified);
            	values.put(DBAccessHelper.SONG_SOURCE, songSource);
            	values.put(DBAccessHelper.SONG_ID, songId);
            	values.put(DBAccessHelper.SAVED_POSITION, songSavedPosition);
                values.put(DBAccessHelper.ALBUMS_COUNT, numberOfAlbums);
                values.put(DBAccessHelper.SONGS_COUNT, numberOfTracks);
                values.put(DBAccessHelper.GENRE_SONG_COUNT, numberOfSongsInGenre);
            	
            	//Add all the entries to the database to build the songs library.
            	mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
            												 		  null, 
            												 		  values);	
            	
            	
            }
    		
    	} catch (SQLException e) {
    		// TODO Auto-generated method stub.
    		e.printStackTrace();
    	} finally {
    		//Close the transaction.
            mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
    		mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
    	}

	}
	
	/**
	 * Constructs the selection string for limiting the MediaStore 
	 * query to specific music folders.
	 */
	private String buildMusicFoldersSelection(Cursor musicFoldersCursor) {
		String mediaStoreSelection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND (";
        int folderPathColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.FOLDER_PATH);
        int includeColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.INCLUDE);
        
        for (int i=0; i < musicFoldersCursor.getCount(); i++) {
        	musicFoldersCursor.moveToPosition(i);
        	boolean include = musicFoldersCursor.getInt(includeColIndex) > 0;
        	
        	//Set the correct LIKE clause.
        	String likeClause;
        	if (include)
        		likeClause = " LIKE ";
        	else
        		likeClause = " NOT LIKE ";
        	
        	//The first " AND " clause was already appended to mediaStoreSelection.
        	if (i!=0 && !include)
        		mediaStoreSelection += " AND ";
        	else if (i!=0 && include)
        		mediaStoreSelection += " OR ";
        	
        	mediaStoreSelection += MediaStore.Audio.Media.DATA + likeClause
								+ "'%" + musicFoldersCursor.getString(folderPathColIndex) 
								+ "/%'";

        }
        
        //Append the closing parentheses.
        mediaStoreSelection += ")";
        return mediaStoreSelection;
	}
	
	/**
	 * Builds a HashMap of all songs and their genres.
	 */
	private void buildGenresLibrary() {
		//Get a cursor of all genres in MediaStore.
		Cursor genresCursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
																  new String[] { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME },
		            								  			  null, 
		            								  			  null, 
		            								  			  null);
		 
		//Iterate thru all genres in MediaStore.
        for (genresCursor.moveToFirst(); !genresCursor.isAfterLast(); genresCursor.moveToNext()) {
        	String genreId = genresCursor.getString(0);
        	String genreName = genresCursor.getString(1);

            if (genreName==null || genreName.isEmpty() ||
                genreName.equals(" ") || genreName.equals("   ") ||
                genreName.equals("    "))
                genreName = mContext.getResources().getString(R.string.unknown_genre);

        	/* Grab a cursor of songs in the each genre id. Limit the songs to 
        	 * the user defined folders using mMediaStoreSelection.
        	 */
        	Cursor cursor = mContext.getContentResolver().query(makeGenreUri(genreId),
        														new String[] { MediaStore.Audio.Media.DATA },
				     											mMediaStoreSelection,
				     											null, 
				     											null);
        	 
        	//Add the songs' file paths and their genre names to the hash.
        	if (cursor!=null) {
        		for (int i=0; i < cursor.getCount(); i++) {
        			cursor.moveToPosition(i);
        			mGenresHashMap.put(cursor.getString(0), genreName);
                    mGenresSongCountHashMap.put(genreName, cursor.getCount());
            	}
            	 
            	cursor.close();
        	}
        	 
        }         
         
        if (genresCursor!=null)
        	genresCursor.close();
         
	}

    /**
     * Builds a HashMap of all artists and their individual albums count.
     */
    private void buildArtistsLibrary() {
        Cursor artistsCursor = mContext.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                                        new String[] { MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS },
                                        null,
                                        null,
                                        null);

        if (artistsCursor==null)
            return;

        for (int i=0; i < artistsCursor.getCount(); i++) {
            artistsCursor.moveToPosition(i);
            mAlbumsCountMap.put(artistsCursor.getString(0), artistsCursor.getInt(1));

        }

        artistsCursor.close();
    }

    /**
     * Builds a HashMap of all albums and their individual songs count.
     */
    private void buildAlbumsLibrary() {
        Cursor albumsCursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                       new String[] { MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS },
                                       null,
                                       null,
                                       null);

        if (albumsCursor==null)
            return;

        for (int i=0; i < albumsCursor.getCount(); i++) {
            albumsCursor.moveToPosition(i);
            mSongsCountMap.put(albumsCursor.getString(0) + albumsCursor.getString(1), albumsCursor.getInt(2));

        }

        albumsCursor.close();
    }

    /**
     * Builds a HashMap of all albums and their album art path.
     */
    private void buildMediaStoreAlbumArtHash() {
        Cursor albumsCursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.ALBUM_ID },
                MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                null);

        final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        if (albumsCursor==null)
            return;

        for (int i=0; i < albumsCursor.getCount(); i++) {
            albumsCursor.moveToPosition(i);
            Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, albumsCursor.getLong(0));
            mMediaStoreAlbumArtMap.put(albumsCursor.getString(0), albumArtUri);
        }

        albumsCursor.close();
    }
	
	/**
	 * Returns the genre of the song at the specified file path.
	 */
	private String getSongGenre(String filePath) {
        if (mGenresHashMap!=null)
		    return mGenresHashMap.get(filePath);
        else
            return mContext.getResources().getString(R.string.unknown_genre);
	}

    /**
     * Returns the number of songs in the specified genre.
     */
    private int getGenreSongsCount(String genre) {
        if (mGenresSongCountHashMap!=null)
            if (genre!=null)
                if (mGenresSongCountHashMap.get(genre)!=null)
                    return mGenresSongCountHashMap.get(genre);
                else
                    return 0;
            else
                if (mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre))!=null)
                    return mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre));
                else
                    return 0;
        else
            return 0;
    }
	
	/**
	 * Returns a Uri of a specific genre in MediaStore. 
	 * The genre is specified using the genreId parameter.
	 */
	private Uri makeGenreUri(String genreId) {
        String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
        return Uri.parse(new StringBuilder().append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
        									.append("/")
        									.append(genreId)
        									.append("/")
        									.append(CONTENTDIR)
        									.toString());
    }
	
	/**
	 * Saves premade equalizer presets to the database.
	 */
	private void saveEQPresets() {
		Cursor eqPresetsCursor = mApp.getDBAccessHelper().getAllEQPresets();
        
		//Check if this is the first startup (eqPresetsCursor.getCount() will be 0).
        if (eqPresetsCursor!=null && eqPresetsCursor.getCount()==0) {
        	mApp.getDBAccessHelper().addNewEQPreset("Flat", 16, 16, 16, 16, 16, 16, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Bass Only", 31, 31, 31, 0, 0, 0, 31, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Treble Only", 0, 0, 0, 31, 31, 31, 0, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Rock", 16, 18, 16, 17, 19, 20, 22, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Grunge", 13, 16, 18, 19, 20, 17, 13, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Metal", 12, 16, 16, 16, 20, 24, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Dance", 14, 18, 20, 17, 16, 20, 23, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Country", 16, 16, 18, 20, 17, 19, 20, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Jazz", 16, 16, 18, 18, 18, 16, 20, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Speech", 14, 16, 17, 14, 13, 15, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Classical", 16, 18, 18, 16, 16, 17, 18, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Blues", 16, 18, 19, 20, 17, 18, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Opera", 16, 17, 19, 20, 16, 24, 18, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Swing", 15, 16, 18, 20, 18, 17, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Acoustic", 17, 18, 16, 19, 17, 17, 14, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("New Age", 16, 19, 15, 18, 16, 16, 18, (short) 0, (short) 0, (short) 0);

        }
        
        //Close the cursor.
        if (eqPresetsCursor!=null)
        	eqPresetsCursor.close();
        
	}
	
	/**
	 * Loops through a cursor of all local songs in 
	 * the library and searches for their album art.
	 */
	private void getAlbumArt() {
		
		//Get a cursor with a list of all local music files on the device.
		Cursor cursor = mApp.getDBAccessHelper().getAllLocalSongs();
		mCurrentTask = mContext.getResources().getString(R.string.building_album_art);
		
		if (cursor==null || cursor.getCount() < 1)
			return;
		
		//Tracks the progress of this method.
		int subProgress = 0;
		if (cursor.getCount()!=0) {
			subProgress = 750000/(cursor.getCount());
		} else {
			subProgress = 750000/1;
		}

		try {
			mApp.getDBAccessHelper().getWritableDatabase().beginTransactionNonExclusive();
			
			//Loop through the cursor and retrieve album art.
			for (int i=0; i < cursor.getCount(); i++) {
				
				try {
	 				cursor.moveToPosition(i);
	 				mOverallProgress += subProgress;
					publishProgress();
					
					String filePath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
					String artworkPath = "";
					if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0 || 
						mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==1) {
						artworkPath = getEmbeddedArtwork(filePath);
					} else {
						artworkPath = getArtworkFromFolder(filePath);
					}
						
					String normalizedFilePath = filePath.replace("'", "''");
					
					//Store the artwork file path into the DB.
					ContentValues values = new ContentValues();
					values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, artworkPath);
					String where = DBAccessHelper.SONG_FILE_PATH + "='" + normalizedFilePath + "'";
					
					mApp.getDBAccessHelper().getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
                    mApp.getDBAccessHelper().getWritableDatabase().yieldIfContendedSafely();
                } catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}
			
			mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
			mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
			cursor.close();
			cursor = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Searchs for folder art within the specified file's 
	 * parent folder. Returns a path string to the artwork 
	 * image file if it exists. Returns an empty string 
	 * otherwise.
	 */
	public String getArtworkFromFolder(String filePath) {
		
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
			
		} else {
			//Create a File that points to the parent directory of the album.
			File directoryFile = file.getParentFile();
			String directoryPath = "";
			String albumArtPath = "";
			try {
				directoryPath = directoryFile.getCanonicalPath();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//Check if album art was already found in this directory.
			if (mFolderArtHashMap.containsKey(directoryPath))
				return mFolderArtHashMap.get(directoryPath);
			
			//Get a list of images in the album's folder.
			FileExtensionFilter IMAGES_FILTER = new FileExtensionFilter(new String[] {".jpg", ".jpeg", 
																					  ".png", ".gif"});
			File[] folderList = directoryFile.listFiles(IMAGES_FILTER);
			
			//Check if any image files were found in the folder.
			if (folderList.length==0) {
				//No images found.
				return "";
				
			} else {
				
				//Loop through the list of image files. Use the first jpeg file if it's found.
				for (int i=0; i < folderList.length; i++) {
					
					try {
						albumArtPath = folderList[i].getCanonicalPath();
						if (albumArtPath.endsWith("jpg") ||
							albumArtPath.endsWith("jpeg")) {
							
							//Add the folder's album art file to the hash.
							mFolderArtHashMap.put(directoryPath, albumArtPath);
							return albumArtPath;
						}
						
					} catch (Exception e) {
						//Skip the file if it's corrupted or unreadable.
						continue;
					}
					
				}
				
				//If an image was not found, check for gif or png files (lower priority).
				for (int i=0; i < folderList.length; i++) {
    				
    				try {
    					albumArtPath = folderList[i].getCanonicalPath();
						if (albumArtPath.endsWith("png") ||
							albumArtPath.endsWith("gif")) {

							//Add the folder's album art file to the hash.
							mFolderArtHashMap.put(directoryPath, albumArtPath);
							return albumArtPath;
						}
						
					} catch (Exception e) {
						//Skip the file if it's corrupted or unreadable.
						continue;
					}
    				
    			}
				
			}
    		
			//Add the folder's album art file to the hash.
			mFolderArtHashMap.put(directoryPath, albumArtPath);
			return "";
    	}
		
	}
		
	/**
	 * Searchs for embedded art within the specified file.
	 * Returns a path string to the artwork if it exists.
	 * Returns an empty string otherwise.
	 */
	public String getEmbeddedArtwork(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
				return getArtworkFromFolder(filePath);
			} else {
				return "";
			}
			
		} else {
        	mMMDR.setDataSource(filePath);
        	byte[] embeddedArt = mMMDR.getEmbeddedPicture();
        	
        	if (embeddedArt!=null) {
        		return "byte://" + filePath;
        	} else {
    			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
    				return getArtworkFromFolder(filePath);
    			} else {
    				return "";
    			}
    			
        	}
        	
		}
		
	}

    /**
     * Convert millisseconds to hh:mm:ss format.
     *
     * @param milliseconds The input time in milliseconds to format.
     * @return The formatted time string.
     */
    private String convertMillisToMinsSecs(long milliseconds) {

    	int secondsValue = (int) (milliseconds / 1000) % 60;
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

    	minutes = "" + minutesValue;
    	hours = "" + hoursValue;

    	String output = "";
    	if (hoursValue!=0) {
    		minutes = "0" + minutesValue;
        	hours = "" + hoursValue;
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		minutes = "" + minutesValue;
        	hours = "" + hoursValue;
    		output = minutes + ":" + seconds;
    	}

    	return output;
    }
	
	@Override
	protected void onProgressUpdate(String... progressParams) {
		super.onProgressUpdate(progressParams);

        if (progressParams.length > 0 && progressParams[0].equals("MEDIASTORE_TRANSFER_COMPLETE")) {
            for (int i=0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i)!=null)
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mCurrentTask, mOverallProgress,
                            1000000, true);

            return;
        }

		if (mBuildLibraryProgressUpdate!=null)
            for (int i=0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i)!=null)
			        mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mCurrentTask, mOverallProgress, 1000000, false);
		
	}

	@Override
	protected void onPostExecute(Void arg0) {
		//Release the wakelock.
		wakeLock.release();
		mApp.setIsBuildingLibrary(false);
		mApp.setIsScanFinished(true);

        Toast.makeText(mContext, R.string.finished_scanning_album_art, Toast.LENGTH_LONG).show();
		
		if (mBuildLibraryProgressUpdate!=null)
            for (int i=0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i)!=null)
			        mBuildLibraryProgressUpdate.get(i).onFinishBuildingLibrary(this);

	}
	
	/**
	 * Setter methods.
	 */
	public void setOnBuildLibraryProgressUpdate(OnBuildLibraryProgressUpdate 
												 buildLibraryProgressUpdate) {
        if (buildLibraryProgressUpdate!=null)
		    mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
	}

}
