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
package com.aniruddhc.acemusic.player.DBHelpers;

import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * SQLite database implementation. Note that this class 
 * only contains methods that access Jams' private 
 * database. For methods that access Android's 
 * MediaStore database, see MediaStoreAccessHelper.
 * 
 * @author Saravan Pantham
 */
public class DBAccessHelper extends SQLiteOpenHelper {

	//Database instance. Will last for the lifetime of the application.
	private static DBAccessHelper sInstance;
	
	//Writable database instance.
	private SQLiteDatabase mDatabase;
	
	//Commmon utils object.
	private Common mApp;
	
	//Database Version.
    private static final int DATABASE_VERSION = 1;
 
    //Database Name.
    private static final String DATABASE_NAME = "Jams.db";
    
    //Common fields.
    public static final String _ID = "_id";
    public static final String SONG_ID = "song_id";
    public static final String EQ_50_HZ = "eq_50_hz";
    public static final String EQ_130_HZ = "eq_130_hz";
    public static final String EQ_320_HZ = "eq_320_hz";
    public static final String EQ_800_HZ = "eq_800_hz";
    public static final String EQ_2000_HZ = "eq_2000_hz";
    public static final String EQ_5000_HZ = "eq_5000_hz";
    public static final String EQ_12500_HZ = "eq_12500_hz";
    public static final String VIRTUALIZER = "eq_virtualizer";
    public static final String BASS_BOOST = "eq_bass_boost";
    public static final String REVERB = "eq_reverb";
    
    //Music folders table.
    public static final String MUSIC_FOLDERS_TABLE = "MusicFoldersTable";
    public static final String FOLDER_PATH = "folder_path";
    public static final String INCLUDE = "include";
    
    //Equalizer settings table for individual songs.
    public static final String EQUALIZER_TABLE = "EqualizerTable";
    
    //Equalizer presets table.
    public static final String EQUALIZER_PRESETS_TABLE = "EqualizerPresetsTable";
    public static final String PRESET_NAME = "preset_name";
    
    //Custom libraries table.
    public static final String LIBRARIES_TABLE = "LibrariesTable";
    public static final String LIBRARY_NAME = "library_name";
    public static final String LIBRARY_TAG = "library_tag";
	
    //Music library table.
    public static final String MUSIC_LIBRARY_TABLE = "MusicLibraryTable";
    public static final String SONG_TITLE = "title";
    public static final String SONG_ARTIST = "artist";
    public static final String SONG_ALBUM = "album";
    public static final String SONG_ALBUM_ARTIST = "album_artist";
    public static final String SONG_DURATION = "duration";
    public static final String SONG_FILE_PATH = "file_path";
    public static final String SONG_TRACK_NUMBER = "track_number";
    public static final String SONG_GENRE = "genre";
    public static final String SONG_PLAY_COUNT = "play_count";
    public static final String SONG_YEAR = "year";
    public static final String SONG_LAST_MODIFIED = "last_modified";
    public static final String SONG_SCANNED = "scanned";
    public static final String SONG_RATING = "rating";
    public static final String BLACKLIST_STATUS = "blacklist_status";
    public static final String ADDED_TIMESTAMP = "added_timestamp";
    public static final String RATING = "rating";
    public static final String LAST_PLAYED_TIMESTAMP = "last_played_timestamp";
    public static final String SONG_SOURCE = "source";
    public static final String SONG_ALBUM_ART_PATH = "album_art_path";
    public static final String SONG_DELETED = "deleted";
    public static final String ARTIST_ART_LOCATION = "artist_art_location";
    public static final String ALBUM_ID = "album_id";
    public static final String ARTIST_ID = "artist_id";
    public static final String GENRE_ID = "genre_id";
    public static final String GENRE_SONG_COUNT = "genre_song_count";
    public static final String LOCAL_COPY_PATH = "local_copy_path";
    public static final String LIBRARIES = "libraries";
    public static final String SAVED_POSITION = "saved_position";
    public static final String ALBUMS_COUNT = "albums_count";
    public static final String SONGS_COUNT = "songs_count";
    public static final String GENRES_SONG_COUNT = "genres_song_count";
    
    //Playlist fields.
    public static final String PLAYLIST_ID = "playlist_id";
    public static final String PLAYLIST_NAME = "playlist_name";
    public static final String PLAYLIST_SOURCE = "playlist_source";
    public static final String PLAYLIST_FILE_PATH = "playlist_file_path";
    public static final String PLAYLIST_FOLDER_PATH = "playlist_folder_path";
    public static final String PLAYLIST_SONG_ENTRY_ID = "song_entry_id";
    public static final String PLAYLIST_ORDER = "order";
    
    //Song source values.
    public static final String GMUSIC = "gmusic";
    public static final String LOCAL = "local";
    
	public DBAccessHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mApp = (Common) context.getApplicationContext();
		
	}
	
	/**
	 * Returns a singleton instance for the database. 
	 * @param context
	 * @return
	 */
	public static synchronized DBAccessHelper getInstance(Context context) {
	    if (sInstance==null)
	    	sInstance = new DBAccessHelper(context.getApplicationContext());
	    
	    return sInstance;
	}
	
	/**
	 * Returns a writable instance of the database. Provides an additional 
	 * null check for additional stability.
	 */
	private synchronized SQLiteDatabase getDatabase() {
		if (mDatabase==null)
			mDatabase = getWritableDatabase();

		return mDatabase;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//Music folders table.
		String[] musicFoldersTableCols = { FOLDER_PATH, INCLUDE };
		String[] musicFoldersTableColTypes = { "TEXT", "TEXT" };
		String createMusicFoldersTable = buildCreateStatement(MUSIC_FOLDERS_TABLE, 
																musicFoldersTableCols, 
																musicFoldersTableColTypes);
		
		//Equalizer table.
		String[] equalizerTableCols = { SONG_ID, EQ_50_HZ, EQ_130_HZ, 
										EQ_320_HZ, EQ_800_HZ, EQ_2000_HZ, 
										EQ_5000_HZ, EQ_12500_HZ, VIRTUALIZER, 
										BASS_BOOST, REVERB };
		
		String[] equalizerTableColTypes = { "TEXT", "TEXT", "TEXT", 
											"TEXT", "TEXT", "TEXT", 
											"TEXT", "TEXT", "TEXT", 
											"TEXT", "TEXT" };
		
		String createEqualizerTable = buildCreateStatement(EQUALIZER_TABLE, 
															 equalizerTableCols,
															 equalizerTableColTypes);
		
		//Equalizer presets table.
		String[] equalizerPresetsTableCols = { PRESET_NAME, EQ_50_HZ, EQ_130_HZ, 
											   EQ_320_HZ, EQ_800_HZ, EQ_2000_HZ, 
											   EQ_5000_HZ, EQ_12500_HZ, VIRTUALIZER, 
											   BASS_BOOST, REVERB };
		
		String[] equalizerPresetsTableColTypes = { "TEXT", "TEXT", "TEXT", 
												   "TEXT", "TEXT", "TEXT", 
												   "TEXT", "TEXT", "TEXT", 
												   "TEXT", "TEXT" };
		
		String createEqualizerPresetsTable = buildCreateStatement(EQUALIZER_PRESETS_TABLE, 
															 		equalizerPresetsTableCols,
															 		equalizerPresetsTableColTypes);
		
		//Custom libraries table.
		String[] librariesTableCols = { LIBRARY_NAME, LIBRARY_TAG, SONG_ID };
		String[] librariesTableColTypes = { "TEXT", "TEXT", "TEXT" };
		String createLibrariesTable = buildCreateStatement(LIBRARIES_TABLE, 
															 librariesTableCols,
															 librariesTableColTypes);
		
		//Music library table.
		String[] musicLibraryTableCols = { SONG_ID, SONG_TITLE, SONG_ARTIST, 
			    						   SONG_ALBUM, SONG_ALBUM_ARTIST, 
			    						   SONG_DURATION, SONG_FILE_PATH, 
			    						   SONG_TRACK_NUMBER, SONG_GENRE, 
			    						   SONG_PLAY_COUNT, SONG_YEAR, ALBUMS_COUNT,
			    						   SONGS_COUNT, GENRES_SONG_COUNT, SONG_LAST_MODIFIED, SONG_SCANNED,
			    						   BLACKLIST_STATUS, ADDED_TIMESTAMP, RATING, 
			    						   LAST_PLAYED_TIMESTAMP, SONG_SOURCE, SONG_ALBUM_ART_PATH,
			    						   SONG_DELETED, ARTIST_ART_LOCATION, ALBUM_ID, 
			    						   ARTIST_ID, GENRE_ID, GENRE_SONG_COUNT, 
			    						   LOCAL_COPY_PATH, LIBRARIES, SAVED_POSITION };
		
		String[] musicLibraryTableColTypes = new String[musicLibraryTableCols.length];
		for (int i=0; i < musicLibraryTableCols.length; i++)
			musicLibraryTableColTypes[i] = "TEXT";
			
		String createMusicLibraryTable = buildCreateStatement(MUSIC_LIBRARY_TABLE, 
															  musicLibraryTableCols,
															  musicLibraryTableColTypes);	

		//Execute the CREATE statements.
		db.execSQL(createMusicFoldersTable);
		db.execSQL(createEqualizerTable);
		db.execSQL(createEqualizerPresetsTable);
		db.execSQL(createLibrariesTable);
		db.execSQL(createMusicLibraryTable);
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void finalize() {
		try {
			getDatabase().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Constructs a fully formed CREATE statement using the input 
	 * parameters.
	 */
	private String buildCreateStatement(String tableName, String[] columnNames, String[] columnTypes) {
		String createStatement = "";
		if (columnNames.length==columnTypes.length) {
			createStatement += "CREATE TABLE IF NOT EXISTS " + tableName + "("
							 + _ID + " INTEGER PRIMARY KEY, ";
			
			for (int i=0; i < columnNames.length; i++) {
				
				if (i==columnNames.length-1) {
					createStatement += columnNames[i] 
							 		+ " "
							 		+ columnTypes[i] 
							 		+ ")";
				} else {
					createStatement += columnNames[i] 
									 + " "
									 + columnTypes[i] 
							 		 + ", ";
				}

			}
			
		}
		
		return createStatement;
	}

	/***********************************************************
	 * MUSIC FOLDERS TABLE METHODS.
	 ***********************************************************/
	
	/**
	 * Adds a music folder to the table.
	 */
    public void addMusicFolderPath(String folderPath) {
        //Escape any rogue apostrophes.
        if (folderPath.contains("'")) {
        	folderPath = folderPath.replace("'", "''");
        }
        
        ContentValues values = new ContentValues();
        values.put(FOLDER_PATH, folderPath);

        getDatabase().insert(MUSIC_FOLDERS_TABLE, null, values);
        
    }
    
    /**
     * Deletes the specified music folder from the table.
     */
    public void deleteMusicFolderPath(String folderPath) {
        String condition = FOLDER_PATH + " = '" + folderPath + "'";
        getDatabase().delete(MUSIC_FOLDERS_TABLE, condition, null);
    }
    
   /**
    * Deletes all music folders from the table.
    */
    public void deleteAllMusicFolderPaths() {
        getDatabase().delete(MUSIC_FOLDERS_TABLE, null, null);
    }
    
    /**
     * Returns a cursor with all music folder paths in the table.
     */
    public Cursor getAllMusicFolderPaths() {
        String selectQuery = "SELECT  * FROM " + MUSIC_FOLDERS_TABLE 
        				   + " ORDER BY " + INCLUDE + "*1 DESC";
        
        return getDatabase().rawQuery(selectQuery, null);
    }
	
	/***********************************************************
	 * EQUALIZER TABLE METHODS.
	 ***********************************************************/
	
    /**
	 * Returns an integer array with EQ values for the specified song. 
	 * The final array index (10) indicates whether the specified song 
	 * has any saved EQ values (0 for false, 1 for true).
	 * 
	 * @param songId The id of the song to retrieve EQ values for.
     */
    public int[] getSongEQValues(String songId) {

        String condition = SONG_ID + "=" + "'" + songId + "'";
        String[] columnsToReturn = { _ID, EQ_50_HZ, EQ_130_HZ, EQ_320_HZ, 
        							 EQ_800_HZ, EQ_2000_HZ, EQ_5000_HZ, 
        							 EQ_12500_HZ, VIRTUALIZER, BASS_BOOST, REVERB };
        
        Cursor cursor = getDatabase().query(EQUALIZER_TABLE, columnsToReturn, condition, null, null, null, null);
        int[] eqValues = new int[11];
        
        if (cursor!=null && cursor.getCount()!=0)  {
        	cursor.moveToFirst();
			eqValues[0] = cursor.getInt(cursor.getColumnIndex(EQ_50_HZ));
			eqValues[1] = cursor.getInt(cursor.getColumnIndex(EQ_130_HZ));
			eqValues[2] = cursor.getInt(cursor.getColumnIndex(EQ_320_HZ));
			eqValues[3] = cursor.getInt(cursor.getColumnIndex(EQ_800_HZ));
			eqValues[4] = cursor.getInt(cursor.getColumnIndex(EQ_2000_HZ));
			eqValues[5] = cursor.getInt(cursor.getColumnIndex(EQ_5000_HZ));
			eqValues[6] = cursor.getInt(cursor.getColumnIndex(EQ_12500_HZ));
			eqValues[7] = cursor.getInt(cursor.getColumnIndex(VIRTUALIZER));
			eqValues[8] = cursor.getInt(cursor.getColumnIndex(BASS_BOOST));
			eqValues[9] = cursor.getInt(cursor.getColumnIndex(REVERB));
			eqValues[10] = 1; //The song id exists in the EQ table.
			
			cursor.close();
			
		} else {
			eqValues[0] = 16;
			eqValues[1] = 16;
			eqValues[2] = 16;
			eqValues[3] = 16;
			eqValues[4] = 16;
			eqValues[5] = 16;
			eqValues[6] = 16;
			eqValues[7] = 0;
			eqValues[8] = 0;
			eqValues[9] = 0;
			eqValues[10] = 0; //The song id doesn't exist in the EQ table.
			
		}
        
        return eqValues;
    }
    
    /**
     * Saves a song's equalizer/audio effect settings to the database.
     */
    public void addSongEQValues(String songId, 
						 	    int fiftyHertz, 
						 	    int oneThirtyHertz, 
						 	    int threeTwentyHertz, 
						 	    int eightHundredHertz, 
						 	    int twoKilohertz, 
						 	    int fiveKilohertz, 
						 	    int twelvePointFiveKilohertz, 
						 	    int virtualizer,
						 	    int bassBoost, 
						 	    int reverb) {

		ContentValues values = new ContentValues();
		values.put(SONG_ID, songId);
		values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, threeTwentyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
		values.put(VIRTUALIZER, virtualizer);
		values.put(BASS_BOOST, bassBoost);
		values.put(REVERB, reverb);

        getDatabase().insert(EQUALIZER_TABLE, null, values);
        
    }
    
    /**
     * Checks if equalizer settings already exist for the given song.
     */
    public boolean hasEqualizerSettings(String songId) {
    	
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	Cursor cursor = getDatabase().query(EQUALIZER_TABLE, 
    							 new String[] { SONG_ID }, 
    							 where, 
    							 null, 
    							 null, 
    							 null, 
    							 null);
    	
    	if (cursor!=null) {
    		if (cursor.getCount() > 0) {
    			cursor.close();
    			return true;
    		} else {
    			cursor.close();
    			return false;
    		}
    		
    	} else {
    		return false;
    	}
    	
    }
    
    /**
     * Updates the equalizer/audio effects for the specified song.
     */
    public void updateSongEQValues(String songId, 
							 	   int fiftyHertz, 
							 	   int oneThirtyHertz, 
							 	   int threeTwentyHertz, 
							 	   int eightHundredHertz, 
							 	   int twoKilohertz, 
							 	   int fiveKilohertz, 
							 	   int twelvePointFiveKilohertz, 
							 	   int virtualizer,
							 	   int bassBoost, 
							 	   int reverb) {
     
        ContentValues values = new ContentValues();
        values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, threeTwentyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
		values.put(VIRTUALIZER, virtualizer);
		values.put(BASS_BOOST, bassBoost);
		values.put(REVERB, reverb);
     
        String condition = SONG_ID + " = " + "'" + songId + "'" ;
        getDatabase().update(EQUALIZER_TABLE, values, condition, null);
        
    }
	
	/***********************************************************
	 * EQUALIZER PRESETS TABLE METHODS.
	 ***********************************************************/
	
    /**
     * Adds a new EQ preset to the table.
     */
    public void addNewEQPreset(String presetName, 
					 	     int fiftyHertz, 
					 	     int oneThirtyHertz, 
					 	     int threeTwentyHertz, 
					 	     int eightHundredHertz, 
					 	     int twoKilohertz, 
					 	     int fiveKilohertz, 
					 	     int twelvePointFiveKilohertz, 
					 	     short virtualizer,
					 	     short bassBoost, 
					 	     short reverb) {
    	
		ContentValues values = new ContentValues();
		values.put(PRESET_NAME, presetName);
		values.put(EQ_50_HZ, fiftyHertz);
		values.put(EQ_130_HZ, threeTwentyHertz);
		values.put(EQ_320_HZ, threeTwentyHertz);
		values.put(EQ_800_HZ, eightHundredHertz);
		values.put(EQ_2000_HZ, twoKilohertz);
		values.put(EQ_5000_HZ, fiveKilohertz);
		values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
		values.put(VIRTUALIZER, virtualizer);
		values.put(BASS_BOOST, bassBoost);
		values.put(REVERB, reverb);

        getDatabase().insert(EQUALIZER_PRESETS_TABLE, null, values);
        
    }
  
    /**
     * This method returns the specified eq preset.
     */
    public int[] getPresetEQValues(String presetName) {
    	
        String condition = PRESET_NAME + "=" + "'" + presetName.replace("'", "''") + "'";
        String[] columnsToReturn = { _ID, EQ_50_HZ, EQ_130_HZ, EQ_320_HZ, 
        							 EQ_800_HZ, EQ_2000_HZ, EQ_5000_HZ, 
        							 EQ_12500_HZ, VIRTUALIZER, BASS_BOOST, REVERB };
        
        Cursor cursor = getDatabase().query(EQUALIZER_PRESETS_TABLE, columnsToReturn, condition, null, null, null, null);
        int[] eqValues = new int[10];
        
        if (cursor!=null && cursor.getCount()!=0)  {
			eqValues[0] = cursor.getInt(cursor.getColumnIndex(EQ_50_HZ));
			eqValues[1] = cursor.getInt(cursor.getColumnIndex(EQ_130_HZ));
			eqValues[2] = cursor.getInt(cursor.getColumnIndex(EQ_320_HZ));
			eqValues[3] = cursor.getInt(cursor.getColumnIndex(EQ_800_HZ));
			eqValues[4] = cursor.getInt(cursor.getColumnIndex(EQ_2000_HZ));
			eqValues[5] = cursor.getInt(cursor.getColumnIndex(EQ_5000_HZ));
			eqValues[6] = cursor.getInt(cursor.getColumnIndex(EQ_12500_HZ));
			eqValues[7] = cursor.getInt(cursor.getColumnIndex(VIRTUALIZER));
			eqValues[8] = cursor.getInt(cursor.getColumnIndex(BASS_BOOST));
			eqValues[9] = cursor.getInt(cursor.getColumnIndex(REVERB));
			
			cursor.close();
			
		} else {
			eqValues[0] = 16;
			eqValues[1] = 16;
			eqValues[2] = 16;
			eqValues[3] = 16;
			eqValues[4] = 16;
			eqValues[5] = 16;
			eqValues[6] = 16;
			eqValues[7] = 16;
			eqValues[8] = 16;
			eqValues[9] = 16;
			
		}
        
        return eqValues;
    }
    
    /**
     * This method updates the specified EQ preset.
     */
    public void updateEQPreset(String presetName, 
					 	       int fiftyHertz, 
					 	       int oneThirtyHertz, 
					 	       int threeTwentyHertz, 
					 	       int eightHundredHertz, 
					 	       int twoKilohertz, 
					 	       int fiveKilohertz, 
					 	       int twelvePointFiveKilohertz, 
					 	       short virtualizer,
					 	       short bassBoost, 
					 	       short reverb) {

    	//Escape any rogue apostrophes.
        if (presetName!=null) {
        	
            if (presetName.contains("'")) {
            	presetName = presetName.replace("'", "''");
            }
            
        }
        
        ContentValues values = new ContentValues();
        values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, threeTwentyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
		values.put(VIRTUALIZER, virtualizer);
		values.put(BASS_BOOST, bassBoost);
		values.put(REVERB, reverb);
     
        String condition = PRESET_NAME + " = " + "'" + presetName + "'";
        getDatabase().update(EQUALIZER_PRESETS_TABLE, values, condition, null);
        
    }
    
    /**
     * Returns a cursor with all EQ presets in the table.
     */
    public Cursor getAllEQPresets() {
    	String query = "SELECT * FROM " + EQUALIZER_PRESETS_TABLE;
    	return getDatabase().rawQuery(query, null);
    	
    }
    
    //Deletes the specified preset.
    public void deletePreset(String presetName) {
        String condition = PRESET_NAME + " = " + "'" + presetName.replace("'", "''") + "'";
        getDatabase().delete(EQUALIZER_PRESETS_TABLE, condition, null);
        
    }
	
	/***********************************************************
	 * LIBRARIES TABLE METHODS.
	 ***********************************************************/
	
    /** 
     * Returns a cursor with a list of all unique libraries within the database.
     * @return
     */
    public Cursor getAllUniqueLibraries() {
    	String rawQuery = "SELECT DISTINCT(" + LIBRARY_NAME + "), " + 
						  _ID + ", " + LIBRARY_TAG +
						  " FROM " + LIBRARIES_TABLE + " GROUP BY " + 
						  LIBRARY_NAME + " ORDER BY " + _ID
						  + " ASC";
    	
    	Cursor cursor = getDatabase().rawQuery(rawQuery, null);
    	return cursor;
    }
    
    /**
     * Deletes the specified library by its name and tag.
     */
    public void deleteLibrary(String libraryName, String tag) {
    	
    	//Escape any rogue apostrophes.
    	libraryName = libraryName.replace("'", "''");
    	tag = tag.replace("'", "''");
    	
    	//Perform the delete operation.
    	String where = LIBRARY_NAME + "=" + "'" + libraryName + "'" + " AND " 
    				 + LIBRARY_TAG + "=" + "'" + tag + "'";
    	
    	getDatabase().delete(LIBRARIES_TABLE, where, null);
    }
    
    /**
     * Returns a cursor with all libraries except the default 
     * ones ("All Libraries" and "Google Play Music").
     */
    public Cursor getAllUniqueUserLibraries(Context context) {
    	String allLibraries = context.getResources().getString(R.string.all_libraries);
    	String googlePlayMusic = context.getResources().getString(R.string.google_play_music_no_asterisk);
    	allLibraries = allLibraries.replace("'", "''");
    	googlePlayMusic = googlePlayMusic.replace("'", "''");
    	
    	String rawQuery = "SELECT DISTINCT(" + LIBRARY_NAME + "), " + 
						  _ID + ", " + LIBRARY_TAG +
						  " FROM " + LIBRARIES_TABLE + " WHERE " + 
						  LIBRARY_NAME + "<>" + "'" + allLibraries + "'" + " AND " + 
						  LIBRARY_NAME + "<>" + "'" + googlePlayMusic + "'" + 
						  " GROUP BY " + LIBRARY_NAME + " ORDER BY " + _ID
						  + " ASC";
    	
    	Cursor cursor = getDatabase().rawQuery(rawQuery, null);
    	return cursor;
    }
    
    /**
     * Retrieves a HashSet of all the song ids within a particular music library.
     */
    public HashSet<String> getAllSongIdsInLibrary(String libraryName, String tag) {
    	HashSet<String> songIdsHashSet = new HashSet<String>();
    	
    	libraryName = libraryName.replace("'", "''");
    	tag = tag.replace("'", "''");
    	
    	String where = LIBRARY_NAME + "=" + "'" + libraryName + "'" + " AND "
    				 + LIBRARY_TAG + "=" + "'" + tag + "'";
    	
    	Cursor cursor = getDatabase().query(LIBRARIES_TABLE, null, where, null, null, null, SONG_ID);
    	if (cursor.getCount() > 0) {
    		for (int i=0; i < cursor.getCount(); i++) {
    			cursor.moveToPosition(i);
    			songIdsHashSet.add(cursor.getString(cursor.getColumnIndex(SONG_ID)));
    		}
    		
    	}
    	
    	if (cursor!=null) {
    		cursor.close();
    		cursor = null;
    	}
    	
    	return songIdsHashSet;
    }
	
    /***********************************************************
     * MUSIC LIBRARY TABLE METHODS.
     ***********************************************************/
	
    /**
     * Returns the cursor based on the specified fragment.
     */
    public Cursor getFragmentCursor(Context context, String querySelection, int fragmentId) {
    	String currentLibrary = mApp.getCurrentLibraryNormalized();

	    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		querySelection += "";
	    	} else {
	    		querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
	    	}

	    	return getFragmentCursorHelper(querySelection, fragmentId);
	    	
	    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
	    	//Check to make sure that Google Play Music is enabled.
	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
	    		return getFragmentCursorHelper(querySelection, fragmentId);
	    	} else {
	    		return null;
	    	}
	    	
	    } else if (currentLibrary.equals(context.getResources().getString(R.string.on_this_device))) { 
	    	//Check if Google Play Music is enabled.
	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		querySelection += " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
	    				  		 + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
	    	} else {
	    		querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
	    	}
	    	
	    	return getFragmentCursorHelper(querySelection, fragmentId);
	    	
    	} else {
	    	if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			    querySelection += " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
	    	} else {
			    querySelection += " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'"
	    	              	   + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
	    	}
	    	
	    	return getFragmentCursorInLibraryHelper(querySelection, fragmentId);
	    }
	    
    }
    
    /**
     * Helper method for getFragmentCursor(). Returns the correct 
     * cursor retrieval method for the specified fragment.
     */
    private Cursor getFragmentCursorHelper(String querySelection, int fragmentId) {
    	switch (fragmentId) {
    	case Common.ARTISTS_FRAGMENT:
    		return getAllUniqueArtists(querySelection);
    	case Common.ALBUM_ARTISTS_FRAGMENT:
    		return getAllUniqueAlbumArtists(querySelection);
    	case Common.ALBUMS_FRAGMENT:
    		return getAllUniqueAlbums(querySelection);
    	case Common.SONGS_FRAGMENT:
            querySelection += " ORDER BY " + SONG_TITLE + " ASC";
    		return getAllSongsSearchable(querySelection);
    	case Common.PLAYLISTS_FRAGMENT:
            //TODO case stub.
    	case Common.GENRES_FRAGMENT:
    		return getAllUniqueGenres(querySelection);
    	case Common.FOLDERS_FRAGMENT:
    		//TODO case stub.
    	case Common.ARTISTS_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsByArtist(querySelection);
    	case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsInAlbumByArtist(querySelection);
    	case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsByAlbumArtist(querySelection);
    	case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsInAlbumByAlbumArtist(querySelection);
    	case Common.ALBUMS_FLIPPED_FRAGMENT:
    		return getAllSongsInAlbumByArtist(querySelection);
    	case Common.GENRES_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsInGenre(querySelection);
    	case Common.GENRES_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsInAlbumInGenre(querySelection);
    	default:
    		return null;
    	}
    	
    }
    
    /**
     * Helper method for getFragmentCursor(). Returns the correct 
     * cursor retrieval method for the specified fragment in the 
     * specified library.
     */
    private Cursor getFragmentCursorInLibraryHelper(String querySelection, int fragmentId) {
    	switch (fragmentId) {
    	case Common.ARTISTS_FRAGMENT:
    		return getAllUniqueArtistsInLibrary(querySelection);
    	case Common.ALBUM_ARTISTS_FRAGMENT:
    		return getAllUniqueAlbumArtistsInLibrary(querySelection);
    	case Common.ALBUMS_FRAGMENT:
    		return getAllUniqueAlbumsInLibrary(querySelection);
    	case Common.SONGS_FRAGMENT:
    		return getAllSongsInLibrarySearchable(querySelection);
    	case Common.PLAYLISTS_FRAGMENT:
    		//TODO case stub.
    	case Common.GENRES_FRAGMENT:
    		return getAllUniqueGenresInLibrary(querySelection);
    	case Common.FOLDERS_FRAGMENT:
    		//TODO case stub.
    	case Common.ARTISTS_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsByArtistInLibrary(querySelection);
    	case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsInAlbumByArtistInLibrary(querySelection);
    	case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsByAlbumArtistInLibrary(querySelection);
    	case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsInAlbumByAlbumArtistInLibrary(querySelection);
    	case Common.ALBUMS_FLIPPED_FRAGMENT:
    		return getAllSongsInAlbumByArtistInLibrary(querySelection);
    	case Common.GENRES_FLIPPED_FRAGMENT:
    		return getAllUniqueAlbumsInGenreInLibrary(querySelection);
    	case Common.GENRES_FLIPPED_SONGS_FRAGMENT:
    		return getAllSongsByInAlbumInGenreInLibrary(querySelection);
    	default:
    		return null;
    	}
    	
    }

    /**
     * Returns the playback cursor based on the specified query selection.
     */
    public Cursor getPlaybackCursor(Context context, String querySelection, int fragmentId) {
        String currentLibrary = mApp.getCurrentLibraryNormalized();

        if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
            if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
                querySelection += "";
            } else {
                querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
            }

            return getPlaybackCursorHelper(querySelection, fragmentId);

        } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
            //Check to make sure that Google Play Music is enabled.
            if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
                querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
                return getPlaybackCursorHelper(querySelection, fragmentId);
            } else {
                return null;
            }

        } else if (currentLibrary.equals(context.getResources().getString(R.string.on_this_device))) {
            //Check if Google Play Music is enabled.
            if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
                querySelection += " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
                        + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
            } else {
                querySelection += " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
            }

            return getPlaybackCursorHelper(querySelection, fragmentId);

        } else {
            if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
                querySelection += " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
            } else {
                querySelection += " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'"
                        + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
            }

            return getPlaybackCursorInLibraryHelper(querySelection, fragmentId);
        }

    }

    /**
     * Helper method for getPlaybackCursor(). Returns the correct
     * cursor retrieval method for the specified playback/fragment route.
     */
    private Cursor getPlaybackCursorHelper(String querySelection, int fragmentId) {
        switch (fragmentId) {
            case Common.PLAY_ALL_BY_ARTIST:
            case Common.PLAY_ALL_BY_ALBUM_ARTIST:
            case Common.PLAY_ALL_BY_ALBUM:
            case Common.PLAY_ALL_IN_GENRE:
            case Common.PLAY_ALL_IN_FOLDER:
                querySelection +=  " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
                break;
            case Common.PLAY_ALL_SONGS:
                querySelection +=  " ORDER BY " + SONG_TITLE + " ASC";
                break;
            case Common.PLAY_ALL_IN_PLAYLIST:
                //TODO Must order the cursor by the order of the playlist's track arrangement.
        }
        return getAllSongsSearchable(querySelection);
    }

    /**
     * Helper method for getPlaybackCursor(). Returns the correct
     * cursor retrieval method for the specified playback/fragment
     * route in the specified library.
     */
    private Cursor getPlaybackCursorInLibraryHelper(String querySelection, int fragmentId) {
        switch (fragmentId) {
            case Common.PLAY_ALL_BY_ARTIST:
            case Common.PLAY_ALL_BY_ALBUM_ARTIST:
            case Common.PLAY_ALL_BY_ALBUM:
            case Common.PLAY_ALL_IN_GENRE:
            case Common.PLAY_ALL_IN_FOLDER:
                querySelection += " ORDER BY " + MUSIC_LIBRARY_TABLE + "." + SONG_TRACK_NUMBER + "*1 ASC";
                break;
            case Common.PLAY_ALL_SONGS:
                querySelection += " ORDER BY " + MUSIC_LIBRARY_TABLE + "." + SONG_TITLE + " ASC";
                break;
            case Common.PLAY_ALL_IN_PLAYLIST:
                //TODO Must order the cursor by the order of the playlist's track arrangement.
        }
        return getAllSongsInLibrarySearchable(querySelection);
    }
    /**
     * Returns a cursor of songs sorted by their track number. Used for
     */
    /**
     * Returns a selection cursor of all unique artists.
     */
    public Cursor getAllUniqueArtists(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ARTIST + "), " 
    								 + _ID + ", " + SONG_FILE_PATH + ", " + ARTIST_ART_LOCATION 
    								 + ", " + BLACKLIST_STATUS + ", " + ALBUMS_COUNT + ", "
    								 + SONG_SOURCE + ", " + SONG_ALBUM_ART_PATH + ", " 
    								 + SONG_DURATION + " FROM " + MUSIC_LIBRARY_TABLE 
    								 + " WHERE " + BLACKLIST_STATUS + "=" + "'" 
    								 + "0" + "'" + selection + " GROUP BY " 
    								 + SONG_ARTIST + " ORDER BY " + SONG_ARTIST
    								 + " ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique artists in the 
     * specified library. The library should be specified in the 
     * selection parameter.
     */
    public Cursor getAllUniqueArtistsInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ARTIST + "), " 
    								 + MUSIC_LIBRARY_TABLE + "." + _ID + ", " 
    								 + SONG_FILE_PATH + ", " + ARTIST_ART_LOCATION + ", "
    								 + SONG_SOURCE + ", " + ALBUMS_COUNT + ", " + SONG_DURATION + ", "
    								 + SONG_ALBUM_ART_PATH + " FROM " + MUSIC_LIBRARY_TABLE 
    								 + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME 
    								 + " ON (" + MUSIC_LIBRARY_TABLE + "." + _ID + "=" 
    								 + DBAccessHelper.LIBRARY_NAME + "." 
    								 + DBAccessHelper.SONG_ID + ") WHERE " 
    								 + MUSIC_LIBRARY_TABLE + "." + BLACKLIST_STATUS + "=" 
    								 + "'" + "0" + "'" + selection + " GROUP BY " 
    								 + MUSIC_LIBRARY_TABLE + "." + SONG_ARTIST + " ORDER BY " 
    								 + MUSIC_LIBRARY_TABLE + "." + SONG_ARTIST
    								 + " ASC" ;
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor of all songs by the specified artist.
     */
    public Cursor getAllSongsByArtist(String artistName) {
    	String selection = SONG_ARTIST + "=" + "'" + artistName.replace("'", "''") + "'";
    	return getDatabase().query(MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique album artists.
     */
    public Cursor getAllUniqueAlbumArtists(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM_ARTIST + "), " 
    								 + _ID + ", " + SONG_FILE_PATH + ", " + ARTIST_ART_LOCATION 
    								 + ", " + BLACKLIST_STATUS + ", " + ALBUMS_COUNT + ", " + SONG_SOURCE + ", "
    								 + SONG_ALBUM_ART_PATH + ", " + SONG_DURATION + " FROM " 
    								 + MUSIC_LIBRARY_TABLE + " WHERE " + BLACKLIST_STATUS 
    								 + "=" + "'" + "0" + "'" + selection + " GROUP BY " 
    								 + SONG_ALBUM_ARTIST + " ORDER BY " + SONG_ALBUM_ARTIST
    								 + " ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique album artists in the 
     * specified library. The library should be specified in the 
     * selection parameter.
     */
    public Cursor getAllUniqueAlbumArtistsInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM_ARTIST + "), " 
    								 + MUSIC_LIBRARY_TABLE + "." + _ID + ", " 
    								 + SONG_FILE_PATH + ", " + ARTIST_ART_LOCATION + ", "
    								 + SONG_SOURCE + ", " + SONG_DURATION + ", " + ALBUMS_COUNT + ", "
    								 + SONG_ALBUM_ART_PATH + " FROM " + MUSIC_LIBRARY_TABLE 
    								 + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
    								 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
    								 + DBAccessHelper.SONG_ID + ") WHERE " 
    								 + MUSIC_LIBRARY_TABLE + "." + BLACKLIST_STATUS + "=" + "'" 
    								 + "0" + "'" + selection + " GROUP BY " + MUSIC_LIBRARY_TABLE 
    								 + "." + SONG_ALBUM_ARTIST + " ORDER BY " + MUSIC_LIBRARY_TABLE 
    								 + "." + SONG_ALBUM_ARTIST
    								 + " ASC" ;
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with all unique albums by an album artist.
     */
    public Cursor getAllUniqueAlbumsByAlbumArtist(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
    								 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ARTIST +
    								 ", " + SONG_YEAR + ", " + SONG_SOURCE + ", " + SONG_DURATION + ", " + SONG_ID + ", " +
    								 LOCAL_COPY_PATH + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_TITLE +
    	    						 ", " + SONG_ALBUM + ", " + SONG_GENRE + ", " + SONGS_COUNT + " FROM " +
    								 MUSIC_LIBRARY_TABLE +" WHERE " + BLACKLIST_STATUS + "=" + "'" + 
    								 "0" + "'" + selection + " GROUP BY " + 
    								 SONG_ALBUM + " ORDER BY " + SONG_YEAR
    								 + "*1 ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with all unique albums by an album artist, in the specified library.
     */
    public Cursor getAllUniqueAlbumsByAlbumArtistInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + MUSIC_LIBRARY_TABLE + "." +
									 _ID + ", " + SONG_ARTIST + ", " + SONG_ALBUM_ARTIST + ", " + SONG_FILE_PATH + ", " + LOCAL_COPY_PATH +
									 ", " + SONG_YEAR + ", " + SONG_SOURCE + ", " + SONG_DURATION + ", " + SONGS_COUNT + ", " +
									 SONG_ALBUM_ART_PATH + ", " + SONG_TITLE + ", " + SONG_ALBUM + ", " + SONG_GENRE + " FROM " + 
									 MUSIC_LIBRARY_TABLE + " INNER JOIN " + LIBRARIES_TABLE + " ON (" 
									 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARIES_TABLE + "." 
									 + SONG_ID + ") WHERE " + BLACKLIST_STATUS + "=" + "'" + 
									 "0" + "'" + selection + " GROUP BY " + SONG_ALBUM + " ORDER BY " + SONG_YEAR
									 + "*1 ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique albums.
     */
    public Cursor getAllUniqueAlbums(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
    								 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + BLACKLIST_STATUS + ", " + 
    								 SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST + ", " + SONG_DURATION +
    								 " FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
    								 BLACKLIST_STATUS + "=" + "'" + 
    								 "0" + "'" + selection + " GROUP BY " + 
    								 SONG_ALBUM + " ORDER BY " + SONG_ALBUM
    								 + " ASC";
    			
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique albums in the 
     * specified library. The library should be specified in the 
     * selection parameter.
     */
    public Cursor getAllUniqueAlbumsInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
									 MUSIC_LIBRARY_TABLE + "." + _ID + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ARTIST + ", "
									 + SONG_SOURCE + ", " + SONG_DURATION + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_ARTIST + " FROM " + MUSIC_LIBRARY_TABLE 
									 + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
									 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
									 + DBAccessHelper.SONG_ID + ") WHERE " + MUSIC_LIBRARY_TABLE + "." + 
									 BLACKLIST_STATUS + "=" + "'" + "0" + "'" + selection + " GROUP BY " + 
									 MUSIC_LIBRARY_TABLE + "." + SONG_ALBUM + " ORDER BY " + MUSIC_LIBRARY_TABLE + "." + SONG_ALBUM
									 + " ASC" ;
    			
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    }
    
    /** 
     * Returns a cursor of all songs in an album. The album 
     * should be passed in via the selection parameter.
     */
    public Cursor getSongsInAlbum(String selection, String[] projection) {
        return getDatabase().query(MUSIC_LIBRARY_TABLE, projection, selection, null, null, null, SONG_YEAR);
    }
    
    /**
     * Returns a selection cursor of all songs in the database. 
     * This method can also be used to search all songs if a 
     * valid selection parameter is passed.
     */
    public Cursor getAllSongsSearchable(String selection) {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
                BLACKLIST_STATUS + "=" + "'0'" + selection;

        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a selection cursor of all songs in the 
     * specified library. The library should be specified in the 
     * selection parameter.
     */
    public Cursor getAllSongsInLibrarySearchable(String selection) {
    	String selectQuery = "SELECT * FROM " + MUSIC_LIBRARY_TABLE 
							  + " INNER JOIN " + LIBRARY_NAME + " ON (" 
							  + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARY_NAME + "." 
							  + SONG_ID + ") WHERE " + MUSIC_LIBRARY_TABLE + "." +
                                BLACKLIST_STATUS + "=" + "'" + "0" + "'" + selection;
    	
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a cursor of all songs in the specified album by the 
     * specified artist.
     */
    public Cursor getAllSongsInAlbum(String albumName, String artistName) {
    	String selection = SONG_ALBUM + "=" + "'" 
    					 + albumName.replace("'", "''") 
    					 + "'" + " AND " + SONG_ARTIST 
    					 + "=" + "'" + artistName.replace("'", "''") 
    					 + "'";
    	
    	return getDatabase().query(MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique genres.
     */
    public Cursor getAllUniqueGenres(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_GENRE + "), " + 
    								 _ID + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ART_PATH
    								 + ", " + SONG_DURATION + ", " + SONG_SOURCE + ", " + GENRE_SONG_COUNT + " FROM " + 
    								 MUSIC_LIBRARY_TABLE + " WHERE " + 
    								 BLACKLIST_STATUS + "=" + "'" + 
    								 "0" + "'" + selection + " GROUP BY " + 
    								 SONG_GENRE + " ORDER BY " + SONG_GENRE 
    								 + " ASC";
    			
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a selection cursor of all unique genres in the 
     * specified library. The library should be specified in the 
     * selection parameter.
     */
    public Cursor getAllUniqueGenresInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_GENRE + "), "+ MUSIC_LIBRARY_TABLE + "." +
									 _ID + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_DURATION
									 + ", " + SONG_SOURCE + ", " + GENRE_SONG_COUNT + " FROM " + MUSIC_LIBRARY_TABLE 
									 + " INNER JOIN " + LIBRARIES_TABLE + " ON (" 
									 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARIES_TABLE + "." 
									 + SONG_ID + ") WHERE " + 
    								 BLACKLIST_STATUS + "=" + "'" + 
    								 "0" + "'" + selection + " GROUP BY " + 
    								 SONG_GENRE + " ORDER BY " + SONG_GENRE 
    								 + " ASC";
    			
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with all the songs in the specified genre.
     */
    public Cursor getAllSongsInGenre(String selection) {
    	String selectQuery = "SELECT * FROM " + MUSIC_LIBRARY_TABLE
    					   + " WHERE " + BLACKLIST_STATUS + "=" + "'"
    					   + "0" + "'" + selection + " ORDER BY " + SONG_ALBUM + " ASC, " 
    					   + SONG_TRACK_NUMBER + "*1 ASC";

    	return getDatabase().rawQuery(selectQuery, null);
    	
    }
    
    /**
     * Returns a cursor of all the songs in an album by a specific artist.
     */
    public Cursor getAllSongsInAlbumByArtist(String selection) {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
        					 BLACKLIST_STATUS + "=" + "'0'" + selection +
        					 " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
        
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a cursor of all the songs in an album by a specific artist, within the specified library.
     */
    public Cursor getAllSongsInAlbumByArtistInLibrary(String selection) {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " INNER JOIN " + LIBRARIES_TABLE + " ON (" 
				 		   + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARIES_TABLE + "." 
				 		   + SONG_ID + ") WHERE " +
				 		   BLACKLIST_STATUS + "=" + "'0'" + selection +
				 		   " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
        
        return getDatabase().rawQuery(selectQuery, null);
     
    }
    
    /**
     * Returns a list of all the songs in an album within a specific genre.
     */
    public Cursor getAllSongsInAlbumInGenre(String selection) {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
        					 BLACKLIST_STATUS + "=" + "'0'" + selection +
        					 " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
        					 
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a list of all the songs in an album by a specific artist, within the specified library.
     */
    public Cursor getAllSongsByInAlbumInArtistInLibrary(String selection) {
        String selectQuery = "SELECT  * FROM " +  LIBRARIES_TABLE + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
				 		   + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
				 		   + DBAccessHelper.SONG_ID + ") WHERE " +
				 		   BLACKLIST_STATUS + "=" + "'0'" + selection +
				 		   " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a list of all the songs in an album in a genre, within the specified library.
     */
    public Cursor getAllSongsByInAlbumInGenreInLibrary(String selection) {

        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
				 		   + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
				 		   + DBAccessHelper.SONG_ID + ") WHERE " +
				 		   BLACKLIST_STATUS + "=" + "'0'" + selection +
				 		   " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a list of all the songs in an album by a specific album artist.
     */
    public Cursor getAllSongsInAlbumByAlbumArtist(String selection) {

        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
        					 BLACKLIST_STATUS + "=" + "'0'" + selection +
        					 " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
        
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    /**
     * Returns a cursor of all the songs in an album by a specific artist, within the specified library.
     */
    public Cursor getAllSongsInAlbumByAlbumArtistInLibrary(String selection) {

        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
				 		   + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
				 		   + DBAccessHelper.SONG_ID + ") WHERE " +
				 		   BLACKLIST_STATUS + "=" + "'0'" + selection +
				 		   " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    /**
     * Returns a list of all the songs in an album by an album artist, within the specified library.
     */
    public Cursor getAllSongsByAlbumArtistInLibrary(String selection) {

        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " INNER JOIN " + DBAccessHelper.LIBRARY_NAME + " ON (" 
				 		   + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + DBAccessHelper.LIBRARY_NAME + "." 
				 		   + DBAccessHelper.SONG_ID + ") WHERE " +
				 		   BLACKLIST_STATUS + "=" + "'0'" + selection +
				 		   " ORDER BY " + SONG_ALBUM + " ASC, " + SONG_TRACK_NUMBER + "*1 ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
    }
    
    /**
     * Returns a cursor of all locally stored files on the device.
     */
    public Cursor getAllLocalSongs() {
    	String where = SONG_SOURCE + "='local'";
    	String[] columns = { SONG_FILE_PATH };
    	
    	return getDatabase().query(MUSIC_LIBRARY_TABLE, columns, where, null, null, null, null);
    	
    }
    
    /**
     * Deletes all Google Play Music entries in the table.
     */
    public void deleteAllGooglePlayMusicSongs() {
    	String where = SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
    	getDatabase().delete(MUSIC_LIBRARY_TABLE, where, null);
    }
    
    /**
     * Returns the number of songs in the specified genre.
     */
    public int getGenreSongCount(String genreName) {
    	String selection = SONG_GENRE + "=" + "'" + genreName.replace("'", "''") + "'";
    	Cursor cursor = getDatabase().query(MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null);
    	
    	int songCount = cursor.getCount();
    	cursor.close();
    	return songCount;
    	
    }
    
    /**
     * Insert the number of songs within a specifed genre.
     */
    public void insertNumberOfSongsInGenre(String genre, int songCount) {
    	ContentValues values = new ContentValues();
		values.put(DBAccessHelper.GENRE_SONG_COUNT, songCount);
		String where = DBAccessHelper.SONG_GENRE + "=" + "'" + genre + "'";
		
		getDatabase().update(MUSIC_LIBRARY_TABLE, 
				  				 values, 
				  				 where, 
				  				 null);
		
    }
    
    /**
     * Returns a song based on its file path.
     */
    public Cursor getSongFromFilePath(String filePath) {
        String selection = SONG_FILE_PATH + "=" + "'" + filePath.replace("'", "''") + "'";
        return getDatabase().query(MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null);
        
    }
    
    /**
     * Updates a song's "scanned" flag during the scanning process.
     */
    public void updateScannedFlag(String filePath) {
        String selection = SONG_FILE_PATH + "=" + "'" + filePath.replace("'", "''") + "'";
        
        ContentValues values = new ContentValues();
        values.put(SONG_SCANNED, "TRUE");
        
        getDatabase().update(MUSIC_LIBRARY_TABLE, values, selection, null);
     
    }
    
    /**
     * Deletes all songs whose "scanned" flag is false.
     */
    public void deleteAllUnscannedSongs() {
        String selection = SONG_SCANNED + "=" + "'FALSE'";
        getDatabase().delete(MUSIC_LIBRARY_TABLE, selection, null);
        
    }
    
    /**
     * Deletes a song that has the specified file path.
     */
    public void deleteSongByFilePath(String filePath) {
        String selection = SONG_FILE_PATH + "=" + "'" + filePath.replace("'", "''") +"'";
        getDatabase().delete(MUSIC_LIBRARY_TABLE, selection, null);
        
    }
    
    /**
     * Resets the SONG_SCANNED flag for all songs.
     */
    public void resetSongScannedFlags() {
    	ContentValues values = new ContentValues();
    	values.put(SONG_SCANNED, "FALSE");
    	
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, null, null);

    }
    
    /**
     * Returns a cursor of all songs in the specified playlist, with an additional selection parameter.
     */
    public Cursor getAllSongsInPlaylistSearchable(String selection) {
    	/*String selectQuery = "SELECT * FROM " + MUSIC_LIBRARY_TABLE
							  + " INNER JOIN " + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + " ON (" 
							  + MUSIC_LIBRARY_TABLE + "." + SONG_FILE_PATH + "=" 
							  + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + "." 
							  + DBAccessHelper.PLAYLIST_SONG_FILE_PATH + ") WHERE " + MUSIC_LIBRARY_TABLE + "."
							  + BLACKLIST_STATUS + "=" + "'" + "0" + "'" + selection + " ORDER BY " 
							  + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME 
							  + "." + DBAccessHelper.PLAYLIST_ORDER + "*1 ASC" ;
    	
        return getDatabase().rawQuery(selectQuery, null);*/
    	return null;

    }
    
    /**
     * Returns a cursor with the top 25 played tracks in the library.
     */
    public Cursor getTop25PlayedTracks(String selection) {
    	return getDatabase().query(MUSIC_LIBRARY_TABLE, 
								 	   null, 
								 	   selection, 
								 	   null, 
								 	   null,
								 	   null,
								 	   SONG_PLAY_COUNT + "*1 DESC",
								 	   "25");
    	
    }
    
    /**
     * Returns a cursor with all songs, ordered by their add date.
     */
    public Cursor getRecentlyAddedSongs(String selection) {
    	return getDatabase().query(MUSIC_LIBRARY_TABLE,
    							 	   null,
    							 	   selection,
    							 	   null,
    							 	   null,
    							 	   null,
    							 	   ADDED_TIMESTAMP + "*1 DESC",
    								   "25");
    	
    }
    
    /**
     * Returns a cursor with all songs, ordered by their rating.
     */
    public Cursor getTopRatedSongs(String selection) {
    	return getDatabase().query(MUSIC_LIBRARY_TABLE,
    							 	   null,
    							 	   selection,
    							 	   null,
    							 	   null,
    							 	   null,
    							 	   RATING + "*1 DESC",
    							 	   "25");
    	
    }
    
    /**
     * Returns a cursor with all songs, ordered by their last played timestamp.
     */
    public Cursor getRecentlyPlayedSongs(String selection) {
    	return getDatabase().query(MUSIC_LIBRARY_TABLE,
    							 	   null,
    							 	   selection,
    							 	   null,
    							 	   null,
    							 	   null,
    							 	   LAST_PLAYED_TIMESTAMP + "*1 DESC",
    							 	   "25");

    }
    
    /**
     * Returns the local copy path for the specified song.
     */
    public String getLocalCopyPath(String songID) {
    	String[] columns = { _ID, LOCAL_COPY_PATH };
    	String where = SONG_ID + "=" + "'" + songID.replace("'", "''") + "'" + " AND " +
    				   SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
    	
    	Cursor cursor = getDatabase().query(MUSIC_LIBRARY_TABLE, columns, where, null, null, null, null);
    	String localCopyPath = null;
    	if (cursor!=null) {
    		if (cursor.getCount() > 0) {
    			cursor.moveToFirst();
    			localCopyPath = cursor.getString(cursor.getColumnIndex(LOCAL_COPY_PATH));
    			
    		}
    		
    	}
    	
    	return localCopyPath;
    			 
    }
    
    /**
     * Saves the last playback position for the specified song.
     */
    public void setLastPlaybackPosition(String songId, long lastPlaybackPosition) {
    	if (songId!=null) {
    		songId = songId.replace("'", "''");
    	} else {
    		return;
    	}
    	
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	ContentValues values = new ContentValues();
    	values.put(SAVED_POSITION, lastPlaybackPosition);
    	
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Sets the blacklist status of the specified artist.
     */
    public void setBlacklistForArtist(String artistName, boolean blacklist) {
    	String where = SONG_ARTIST + "=" + "'" + artistName.replace("'", "''") + "'";
    	ContentValues values = new ContentValues();
    	values.put(BLACKLIST_STATUS, blacklist);
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Sets the blacklist status of the specified album artist.
     */
    public void setBlacklistForAlbumArtist(String albumArtistName, boolean blacklist) {
    	String where = SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName.replace("'", "''") + "'";
    	ContentValues values = new ContentValues();
    	values.put(BLACKLIST_STATUS, blacklist);
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Sets the blacklist status of the specified album.
     */
    public void setBlacklistForAlbum(String albumName, String artistName, boolean blacklist) {
    	String where = SONG_ALBUM + "=" + "'" + albumName.replace("'", "''") + "'"
    				 + " AND " + SONG_ARTIST + "=" + "'" + artistName.replace("'", "''");
    	ContentValues values = new ContentValues();
    	values.put(BLACKLIST_STATUS, blacklist);
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Sets the blacklist status of the specified song.
     */
    public void setBlacklistForSong(String songId, boolean blacklist) {
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	ContentValues values = new ContentValues();
    	values.put(BLACKLIST_STATUS, blacklist);
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Returns the album art path of the specified song.
     */
    public String getAlbumArtBySongId(String songId) {
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	Cursor cursor = getDatabase().query(MUSIC_LIBRARY_TABLE, 
    											new String[] { _ID, SONG_ALBUM_ART_PATH }, 
    											where, 
    											null, 
    											null, 
    											null, 
    											null);
    	
    	if (cursor!=null) {
    		cursor.moveToFirst();
    		String albumArtPath = cursor.getString(cursor.getColumnIndex(SONG_ALBUM_ART_PATH));
    		cursor.close();
    		return albumArtPath;
    	} else {    		
    		return null;
    	}
    	
    }
    
    /**
     * Returns a cursor of all the songs in the current table.
     */
    public Cursor getAllSongs() {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
        					 BLACKLIST_STATUS + "=" + "'0'" + " ORDER BY " + SONG_TITLE + " ASC";
        
        return getDatabase().rawQuery(selectQuery, null);
     
    }
    
    /**
     * Returns the rating for the specified song.
     */
    public int getSongRating(String songId) {
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	Cursor cursor = getDatabase().query(MUSIC_LIBRARY_TABLE, 
    										    new String[] { _ID, SONG_RATING }, 
    										    where, 
    										    null, 
    										    null, 
    										    null, 
    										    null);
    	
    	int songRating = 0;
    	if (cursor!=null) {
    		songRating = cursor.getInt(cursor.getColumnIndex(SONG_RATING));
    		cursor.close();
    	}
    	
    	return songRating;
    	
    }
    
    /**
     * Sets the rating for the specified song.
     */
    public void setSongRating(String songId, int rating) {
    	String where = SONG_ID + "=" + "'" + songId + "'";
    	ContentValues values = new ContentValues();
    	values.put(SONG_RATING, rating);
    	getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    	
    }
    
    /**
     * Returns a cursor with all the albums in the specified genre.
     */
    public Cursor getAllUniqueAlbumsInGenre(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
				 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + 
    			 BLACKLIST_STATUS + ", " + SONG_GENRE + ", " + SONG_YEAR + ", " +
				 SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONGS_COUNT + ", " +
    			 SONG_ALBUM_ARTIST + ", " + SONG_DURATION + ", " + LOCAL_COPY_PATH
				 + " FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
				 BLACKLIST_STATUS + "=" + "'" + 
				 "0" + "'" + selection + " GROUP BY " + 
				 SONG_ALBUM + " ORDER BY " + SONG_ALBUM
				 + " ASC";

    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with unique albums in the specified genre, within the specified library.
     */
    public Cursor getAllUniqueAlbumsInGenreInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
									 MUSIC_LIBRARY_TABLE + "." + _ID + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ARTIST + ", "
									 + SONG_SOURCE + ", " + SONG_DURATION + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_ARTIST 
									 + ", " + SONG_GENRE + ", " + SONG_YEAR + ", " + SONGS_COUNT + ", " + LOCAL_COPY_PATH + " FROM " + MUSIC_LIBRARY_TABLE
									 + " INNER JOIN " + LIBRARIES_TABLE + " ON (" 
									 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARIES_TABLE + "." 
									 + SONG_ID + ") WHERE " + MUSIC_LIBRARY_TABLE + "." + 
									 BLACKLIST_STATUS + "=" + "'" + "0" + "'" + selection + " GROUP BY " + 
									 MUSIC_LIBRARY_TABLE + "." + SONG_ALBUM + " ORDER BY " + MUSIC_LIBRARY_TABLE + "." + SONG_ALBUM
									 + " ASC";

    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor of all blacklisted artists.
     */
    public Cursor getBlacklistedArtists() {
    	String query = "SELECT DISTINCT(" + SONG_ARTIST + "), " + 
						_ID + ", " + SONG_FILE_PATH + ", " + 
						SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST +
						" FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
						BLACKLIST_STATUS + "=" + "'" + 
						"1" + "'" + " GROUP BY " + 
						SONG_ALBUM + " ORDER BY " + SONG_ALBUM
						+ " ASC";
    	
    	return getDatabase().rawQuery(query, null);
    	
    }
    
    /**
     * Returns a cursor of all blacklisted albmums.
     */
    public Cursor getBlacklistedAlbums() {
    	String query = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
						_ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + 
						SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST +
						" FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
						BLACKLIST_STATUS + "=" + "'" + 
						"1" + "'" + " GROUP BY " + 
						SONG_ALBUM + " ORDER BY " + SONG_ALBUM
						+ " ASC";
    	
    	return getDatabase().rawQuery(query, null);
    	
    }
    
    /**
     * Returns a list of all blacklisted songs.
     */
    public Cursor getAllBlacklistedSongs() {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + 
        					 " WHERE " + BLACKLIST_STATUS + "=" + "'1'" + 
        					 " ORDER BY " + SONG_TITLE + " ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    /**
     * Returns a list of all the albums sorted by name.
     */
    public Cursor getAllAlbumsOrderByName() {
        String selectQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
							 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + 
							 SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST +
							 " FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
							 BLACKLIST_STATUS + "=" + "'" + 
							 "0" + "'" + " GROUP BY " + 
							 SONG_ALBUM + " ORDER BY " + SONG_ALBUM
							 + " ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    /**
     * Returns a list of all the artists sorted by name.
     */
    public Cursor getAllArtistsOrderByName() {

        String selectQuery = "SELECT DISTINCT(" + SONG_ARTIST + "), " + 
							 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + 
							 SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST +
							 " FROM " + MUSIC_LIBRARY_TABLE + " WHERE " + 
							 BLACKLIST_STATUS + "=" + "'" + 
							 "0" + "'" + " GROUP BY " + 
							 SONG_ARTIST + " ORDER BY " + SONG_ARTIST
							 + " ASC";
     
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    /**
     * Returns a cursor with the specified song.
     */
    public Cursor getSongById(String songID) {
    	String selection = SONG_ID + "=" + "'" +  songID + "'";
    	return getDatabase().query(MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null);
	 
    }
    
    /**
     * Returns a list of all the songs by an album artist.
     */
    public Cursor getAllSongsByAlbumArtist(String selection) {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " WHERE " +
        					 BLACKLIST_STATUS + "=" + "'0'" + selection +
        					 " ORDER BY " + SONG_TRACK_NUMBER + "*1 ASC";
        
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
    //Updates the blacklist statuses of the songs specified in the HashMap.
    public void batchUpdateSongBlacklist(HashMap<String, Boolean> songIdBlacklistStatusPair) {
    	
    	//Retrieve the list of all keys (songIds) within the HashMap.
    	String[] songIdsArray = new String[songIdBlacklistStatusPair.size()];
    	songIdBlacklistStatusPair.keySet().toArray(songIdsArray);
    	
    	for (int i=0; i < songIdsArray.length; i++) {
    		String songId = songIdsArray[i];
    		boolean blacklistStatus = songIdBlacklistStatusPair.get(songId);
    		
    		ContentValues values = new ContentValues();
    		values.put(BLACKLIST_STATUS, blacklistStatus);
    		
    		String where = _ID + "=" + "'" + songId + "'";
    		getDatabase().update(MUSIC_LIBRARY_TABLE, values, where, null);
    		
    	}

    }
    
    /**
     * Returns a HashMap of all the songIds and their blacklist status.
     */
    public HashMap<String, Boolean> getAllSongIdsBlacklistStatus() {
    	HashMap<String, Boolean> songIdBlacklistStatusPair = new HashMap<String, Boolean>();
    	
    	String[] columns = { _ID, BLACKLIST_STATUS };
    	Cursor cursor = getDatabase().query(MUSIC_LIBRARY_TABLE, columns, null, null, null, null, null);

        if (cursor==null)
            return null;

    	if (cursor.getCount() > 0) {
    		for (int i=0; i < cursor.getCount(); i++) {
    			cursor.moveToPosition(i);
    			String songId = cursor.getString(cursor.getColumnIndex(_ID));
    			boolean blacklistStatus = cursor.getString(cursor.getColumnIndex(BLACKLIST_STATUS)).equals("true");
    			songIdBlacklistStatusPair.put(songId, blacklistStatus);
    		}
    		
    	}
    	

    	cursor.close();
    	return songIdBlacklistStatusPair;
    }
    
    /**
     * Returns a cursor with unique albums within the database, regardless of the blacklist status.
     */
    public Cursor getAllUniqueAlbumsNoBlacklist(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
    								 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + BLACKLIST_STATUS + ", " + 
    								 SONG_ALBUM_ART_PATH + ", " + SONG_SOURCE + ", " + SONG_ALBUM_ARTIST +
    								 " FROM " + MUSIC_LIBRARY_TABLE + " GROUP BY " + 
    								 SONG_ALBUM + " ORDER BY " + SONG_ALBUM
    								 + " ASC";
    			
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with unique artists within the database, regardless of the blacklist status.
     */
    public Cursor getAllUniqueArtistsNoBlacklist(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ARTIST + "), " + 
    								 _ID + ", " + SONG_FILE_PATH + ", " + ARTIST_ART_LOCATION + ", " + BLACKLIST_STATUS + ", "
    								 + SONG_SOURCE + ", " + SONG_ALBUM_ART_PATH + " FROM " + MUSIC_LIBRARY_TABLE 
    								 + " GROUP BY " + SONG_ARTIST + " ORDER BY " + SONG_ARTIST + " ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a list of all songs irrespective of the blacklist status.
     */
    public Cursor getAllSongsNoBlacklist() {
        String selectQuery = "SELECT  * FROM " +  MUSIC_LIBRARY_TABLE + " ORDER BY " + SONG_TITLE + " ASC";
        return getDatabase().rawQuery(selectQuery, null);
        
    }
    
   /**
    * Returns a cursor with unique albums by an artist.
    */
    public Cursor getAllUniqueAlbumsByArtist(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + 
    								 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + SONGS_COUNT +
    								 ", " + SONG_YEAR + ", " + SONG_SOURCE + ", " + SONG_DURATION + ", " +
    								 LOCAL_COPY_PATH + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_TITLE +
    	    						 ", " + SONG_ALBUM + ", " + SONG_GENRE + " FROM " + 
    								 MUSIC_LIBRARY_TABLE +" WHERE " + BLACKLIST_STATUS + "=" + "'" + 
    								 "0" + "'" + selection + " GROUP BY " + 
    								 SONG_ALBUM + " ORDER BY " + SONG_YEAR
    								 + "*1 ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    	
    }
    
    /**
     * Returns a cursor with unique albums by an artist within the specified library.
     */
    public Cursor getAllUniqueAlbumsByArtistInLibrary(String selection) {
    	String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " + MUSIC_LIBRARY_TABLE + "." +
									 _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " + LOCAL_COPY_PATH +
									 ", " + SONG_YEAR + ", " + SONG_SOURCE + ", " + SONG_DURATION + ", " + SONGS_COUNT + ", " +
									 SONG_ALBUM_ART_PATH + ", " + SONG_TITLE + ", " + SONG_ALBUM + ", " + SONG_GENRE + " FROM " + 
									 MUSIC_LIBRARY_TABLE + " INNER JOIN " + LIBRARIES_TABLE + " ON (" 
									 + MUSIC_LIBRARY_TABLE + "." + _ID + "=" + LIBRARIES_TABLE + "." 
									 + SONG_ID + ") WHERE " + BLACKLIST_STATUS + "=" + "'" + 
									 "0" + "'" + selection + " GROUP BY " + SONG_ALBUM + " ORDER BY " + SONG_YEAR
									 + "*1 ASC";
    	
    	return getDatabase().rawQuery(selectDistinctQuery, null);
    }
    
}
