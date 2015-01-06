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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Services.AudioPlaybackService;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * Checks if the Google Play Music app is installed on the user's device. If it
 * is, grabs the user's song data from Google Play Music's content mApp.
 */
public class AsyncGetGooglePlayMusicMetadataTask extends AsyncTask<String, String, String> {
	
    private Context mContext;
	private Common mApp;
	
	private HashMap<String, String> playlistIdsNameMap = new HashMap<String, String>();
	private JSONArray playlistsJSONArray = new JSONArray();
	private JSONArray playlistEntriesJSONArray = new JSONArray();
	private ArrayList<String> genresList = new ArrayList<String>();
	
	private int targetVolume = 0;
	private int currentVolume;
	private int stepDownValue = 1;
	private AudioManager am;
	
	private String currentTask = "";
	private int currentProgressValue = 0;
	private int numberOfSongs = 0;
	private int numberOfPlaylists = 0;
	private Date date = new Date();
	
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;
    
    public AsyncGetGooglePlayMusicMetadataTask(Context context) {
    	mContext = context;
    	mApp = (Common) mContext;
    	
    }
 
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	//Hide the actionbar.
    	mApp.setIsBuildingLibrary(true);
    	
    	//Acquire a wakelock to prevent the CPU from sleeping while the process is running.
    	pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    	wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.aniruddhc.acemusic.player.AsyncTasks.AsyncGetGooglePlayMusicMetadata");
    	wakeLock.acquire();
    	
    	//Set the initial setting of the progressbar as indeterminate.
    	currentTask = mContext.getResources().getString(R.string.contacting_google_play_music);
    	
    }
    
    @Override
    protected String doInBackground(String... params) {
    	
    	//Check if any music is playing and fade it out.
    	am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    	if (mApp.isServiceRunning()) {
    		
    		if (mApp.getService().isPlayingMusic()) {
    			targetVolume = 0;
    			currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);		
    			while(currentVolume > targetVolume) {
    			    am.setStreamVolume(AudioManager.STREAM_MUSIC, (currentVolume - stepDownValue), 0);
    			    currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    			    
    			}
    			
    			mContext.stopService(new Intent(mContext, AudioPlaybackService.class));
    			
    		}
    		
    	}
    	
    	//Check if the Google Play Music app is installed.
    	PackageManager pm = mContext.getPackageManager();
    	boolean installed = false;
    	try {
			pm.getPackageInfo("com.google.android.music", PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (NameNotFoundException e1) {
			//The app isn't installed.
			installed = false;
		}
    	
    	String result = "GENERIC_EXCEPTION";
    	if (installed==false) {
    		//Can't do anything here anymore. Quit.
    		mApp.getSharedPreferences().edit().putBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false).commit();
    		return null;
    	} else {
    		//Grab music metadata from Google Play Music's public content mApp.
    		result = getMetadataFromGooglePlayMusicApp();
    	}
    	return result;
    }
    
    //Grab music metadata from Google Play Music's public content mApp.
	public String getMetadataFromGooglePlayMusicApp() {
    	
    	//Grab a handle on the mApp.
    	Uri googlePlayMusicContentProviderUri = Uri.parse("content://com.google.android.music.MusicContent/audio");
    	String[] projection = { "title", "artist", "album", "AlbumArtist",
    							"duration", "track", "year", "Genre", "TrackType AS track_type",
    							/*"_count",*/ "Rating", "AlbumArtLocation AS album_art", "SourceType AS source_type",
    							"SourceId", "ArtistArtLocation", /*, "artistId",*/ "StoreAlbumId" };
    	
    	/* source_type values:
    	 * 0: Local file (not used).
    	 * 1: Unknown.
    	 * 2: Personal, free GMusic library (used).
    	 * 3: All Access (not used).
    	 */
    	String selection = "source_type=2 AND track_type=0";
    	
    	//Catch any exceptions that may be thrown as a result of unknown columns in GMusic's content mApp.
    	Cursor cursor = null;
    	boolean projectionFailed = false;
    	try {
    		cursor = mContext.getContentResolver().query(googlePlayMusicContentProviderUri, projection, selection, null, null);
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    		
    		//Problematic columns are commented out here.
    		String[] failSafeProjection = { "title", "artist", "album", "AlbumArtist",
						   					"duration", "track", "year", "Genre", "TrackType AS track_type",
						   					/*"_count",*/ "Rating", "AlbumArtLocation AS album_art", /* "SourceType AS source_type", */
						   					"SourceId", /* "ArtistArtLocation", "artistId",*/ "StoreAlbumId" };
    		
    		cursor = mContext.getContentResolver().query(googlePlayMusicContentProviderUri, failSafeProjection, "track_type=0", null, null);
    		projectionFailed = true;
    		
    	}

    	//Clear out all the current Google Play Music songs in the database.
    	mApp.getDBAccessHelper().deleteAllGooglePlayMusicSongs();
    	
    	//Insert the songs and their metadata into Jams' local database.
    	/* To improve database insertion performance, we'll use a single transaction 
    	 * for the entire operation. SQLite journals each database insertion and 
    	 * creates a new transaction by default. We'll override this functionality 
    	 * and create a single transaction for all the database record insertions. 
    	 * In theory, this should reduce NAND memory overhead times and result in 
    	 * a 2x to 5x performance increase.
    	 */
    	try {
    		//We'll initialize the DB transaction manually.
    		mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();

    		//Avoid "Divide by zero" errors.
    		int scanningSongsIncrement;
    		if (cursor!=null) {
        		if (cursor.getCount()!=0) {
        			scanningSongsIncrement = 800000/cursor.getCount();
        		} else {
        			scanningSongsIncrement = 800000/1;
        		}
        		
    		} else {
    			return "FAIL";
    		}
    		
    		currentTask = mContext.getResources().getString(R.string.syncing_with_google_play_music);
            for (int i=0; i < cursor.getCount(); i++) {
            	
            	cursor.moveToPosition(i);
            	currentProgressValue = currentProgressValue + scanningSongsIncrement;
            	publishProgress();
            	
            	//Get the song's metadata.
            	String songTitle = cursor.getString(cursor.getColumnIndex("title"));
            	String songArtist = cursor.getString(cursor.getColumnIndex("Artist"));
            	String songAlbum = cursor.getString(cursor.getColumnIndex("Album"));
            	String songAlbumArtist = cursor.getString(cursor.getColumnIndex("AlbumArtist"));
            	String songDuration = cursor.getString(cursor.getColumnIndex("Duration"));
            	String songTrackNumber =cursor.getString(cursor.getColumnIndex("Track"));
            	String songYear = cursor.getString(cursor.getColumnIndex("Year"));
            	String songGenre = cursor.getString(cursor.getColumnIndex("Genre"));
            	//String songPlayCount = cursor.getString(cursor.getColumnIndex("_count"));
            	String songRating = cursor.getString(cursor.getColumnIndex("Rating"));
            	String songSource = DBAccessHelper.GMUSIC;
            	String songAlbumArtPath = cursor.getString(cursor.getColumnIndex("album_art"));
            	String songID = cursor.getString(cursor.getColumnIndex("SourceId"));
            	//String artistID = cursor.getString(cursor.getColumnIndex("artistId"));
            	String storeAlbumID = cursor.getString(cursor.getColumnIndex("StoreAlbumId"));
            	
            	String songArtistArtPath = "";
            	if (projectionFailed==false) {
            		songArtistArtPath = cursor.getString(cursor.getColumnIndex("ArtistArtLocation"));
            	} else {
            		//Fall back on album art.
            		songArtistArtPath = cursor.getString(cursor.getColumnIndex("album_art"));
            	}
            	
            	//Prepare the genres ArrayList.
            	if (!genresList.contains(songGenre)) {
            		genresList.add(songGenre);
            	}
            	
            	//Filter out track numbers and remove any bogus values.
            	if (songTrackNumber!=null) {
        			if (songTrackNumber.contains("/")) {
        				int index = songTrackNumber.lastIndexOf("/");
        				songTrackNumber = songTrackNumber.substring(0, index);
        			}
                	
            	}
            	
            	if (songYear.equals("0")) {
            		songYear = "";
            	}
            	
            	//Check if any of the other tags were empty/null and set them to "Unknown xxx" values.
            	if (songArtist==null || songArtist.isEmpty() || songArtist.equals(" ")) {
            		songArtist = "Unknown Artist";
            	}
            	
            	if (songAlbumArtist==null || songAlbumArtist.isEmpty() || songAlbumArtist.equals(" ")) {
            		songAlbumArtist = "Unknown Album Artist";
            	}
            	
            	if (songAlbum==null || songAlbum.isEmpty() || songAlbum.equals(" ")) {
            		songAlbum = "Unknown Album";
            	}
            	
            	if (songGenre==null || songGenre.isEmpty() || songGenre.equals(" ")) {
            		songGenre = "Unknown Genre";
            	}
            	
            	ContentValues values = new ContentValues();
            	values.put(DBAccessHelper.SONG_TITLE, songTitle);
            	values.put(DBAccessHelper.SONG_ARTIST, songArtist);
            	values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
            	values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
            	values.put(DBAccessHelper.SONG_DURATION, songDuration);
            	values.put(DBAccessHelper.SONG_FILE_PATH, songID);
            	values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
            	values.put(DBAccessHelper.SONG_GENRE, songGenre);
            	//values.put(DBAccessHelper.SONG_PLAY_COUNT, songPlayCount);
            	values.put(DBAccessHelper.SONG_YEAR, songYear);
            	values.put(DBAccessHelper.SONG_LAST_MODIFIED, "");
            	values.put(DBAccessHelper.BLACKLIST_STATUS, "FALSE"); //Keep the song whitelisted by default.
            	values.put(DBAccessHelper.ADDED_TIMESTAMP, date.getTime());
            	values.put(DBAccessHelper.RATING, songRating);
            	values.put(DBAccessHelper.SONG_SOURCE, songSource);
            	values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
            	values.put(DBAccessHelper.SONG_ID, songID);
            	values.put(DBAccessHelper.ARTIST_ART_LOCATION, songArtistArtPath);
            	//values.put(DBAccessHelper.ARTIST_ID, artistID);
            	values.put(DBAccessHelper.ALBUM_ID, storeAlbumID);
            	
            	/* We're gonna have to save the song ID into the SONG_FILE_PATH 
            	 * field. Google Play Music playlist songs don't have a file path, but we're using a 
            	 * JOIN in PlaylistsFlippedFragment that relies on this field, so we'll need to use the 
            	 * song ID as a placeholder instead.
            	 */
            	values.put(DBAccessHelper.SONG_FILE_PATH, songID);
            	
            	//Add all the entries to the database to build the songs library.
            	mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
            												 		  null, 
            												 		  values);	
            	
            }
            
            mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		//Close the transaction.
    		mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
    		
    		if (cursor!=null) {
    			cursor.close();
    			cursor = null;
    		}
    		
    	}
    	
    	/****************************************************************************
    	 * BUILD PLAYLISTS LIBRARY
    	 ****************************************************************************/
    	//getPlaylistsWebClient();
    	//getPlaylistsMobileClient();
    	
    	//Update the genres library.
    	updateGenreSongCount();
    	
    	return "SUCCESS";
    }
    
    /**
     * Downloads and stores song metadata from Google's servers.
     * 
     * @deprecated Grabbing metadata directly from Google's servers can potentially 
     * cause them to shutdown the "sj" service for this app. The official GMusic app
     * has a ContentProvider that can provide all the metadata for offline usage.
     */
     public String downloadMetadataFromGoogle() {
     /* //Retrieve a list of songs stored on Google Play Music and their metadata.
    	GMusicClientCalls gMusicClientCalls = GMusicClientCalls.getInstance(mContext);
    	try {
			songsList = gMusicClientCalls.getAllSongs(mContext.getApplicationContext());
		} catch (JSONException e) {
			return "GENERIC_EXCEPTION";
		}
    	
    	//Clear out all the current Google Play Music songs in the database.
    	DBAccessHelper libraryDBHelper = new DBAccessHelper(mContext);
    	libraryDBHelper.deleteAllGooglePlayMusicSongs();
    	
    	//Insert the songs and their metadata into Jams' local database.
    	/* To improve database insertion performance, we'll use a single transaction 
    	 * for the entire operation. SQLite journals each database insertion and 
    	 * creates a new transaction by default. We'll override this functionality 
    	 * and create a single transaction for all the database record insertions. 
    	 * In theory, this should reduce NAND memory overhead times and result in 
    	 * a 2x to 5x performance increase.
    	 *\/
    	try {
    		//We'll initialize the DB transaction manually.
    		libraryDBHelper.getWritableDatabase().beginTransaction();
    		
    		/* Now that we have a list of all the audio files that are either new or 
    		 * modified, we can just delete all the records of those files in the DB 
    		 * and re-add them.
    		 *\/
    		
    		//Avoid "Divide by zero" errors.
    		int scanningSongsIncrement;
    		if (songsList.size()!=0) {
    			scanningSongsIncrement = 800000/songsList.size();
    		} else {
    			scanningSongsIncrement = 800000/1;
    		}
    		
    		currentTask = mContext.getResources().getString(R.string.downloading_songs_info_from_google_play_music);
            for (int i=0; i < songsList.size(); i++) {
            	
            	WebClientSongsSchema song = songsList.get(i);
            	
            	currentProgressValue = currentProgressValue + scanningSongsIncrement;
            	publishProgress();
            	
            	//Get the song's metadata.
            	String songTitle = song.getTitle();
            	String songArtist = song.getArtist();
            	String songAlbum = song.getAlbum();
            	String songAlbumArtist = song.getAlbumArtist();
            	String songDuration = "" + song.getDurationMillis();
            	String songTrackNumber = "" + song.getTrack();
            	String songYear = "" + song.getYear();
            	String songGenre = song.getGenre();
            	String songPlayCount = "" + song.getPlayCount();
            	String songRating = "" + song.getRating();
            	String songSource = DBAccessHelper.GMUSIC;
            	String songAlbumArtPath = song.getAlbumArtUrl();
            	String songDeleted = "" + song.isDeleted();
            	String songLastPlayed = "" + song.getLastPlayed();
            	String songId = "" + song.getId();
            	
            	/* By default, Google will return an album art path that contains
            	 * an image that is 130x130 in size. The "=sxxx" parameter determines 
            	 * the size of the returned image. We'll replace all instances of "=s130" 
            	 * with "=s512" to get artwork that is 512x512 in size. Also, the "http://" 
            	 * part appears truncated, so add that into the beginning of the url.
            	 *\/
            	if (songAlbumArtPath!=null) {
            		
            		if (!songAlbumArtPath.isEmpty()) {
            			songAlbumArtPath = songAlbumArtPath.replace("=s130", "=s512");
            			songAlbumArtPath = "http:" + songAlbumArtPath;
            			
            		}
            		
            	}
            	
            	//Prepare the genres ArrayList.
            	if (!genresList.contains(songGenre)) {
            		genresList.add(songGenre);
            	}
            	
            	//Filter out track numbers and remove any bogus values.
            	if (songTrackNumber!=null) {
        			if (songTrackNumber.contains("/")) {
        				int index = songTrackNumber.lastIndexOf("/");
        				songTrackNumber = songTrackNumber.substring(0, index);
        			}
                	
            	}
            	
            	if (songYear.equals("0")) {
            		songYear = "";
            	}
            	
            	String songFilePath = "";
            	try {
            		URI uri = gMusicClientCalls.getSongStream(song.getId());
					songFilePath = uri.toURL().toString();
				} catch (JSONException e) {
					continue;
				} catch (URISyntaxException e) {
					continue;
				} catch (MalformedURLException e) {
					continue;
				} catch (Exception e) {
					continue;
				}
            	
            	ContentValues values = new ContentValues();
            	values.put(DBAccessHelper.SONG_TITLE, songTitle);
            	values.put(DBAccessHelper.SONG_ARTIST, songArtist);
            	values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
            	values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
            	values.put(DBAccessHelper.SONG_DURATION, songDuration);
            	values.put(DBAccessHelper.SONG_FILE_PATH, songFilePath);
            	values.put(DBAccessHelper.SONG_FOLDER_PATH, "");
            	values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
            	values.put(DBAccessHelper.SONG_GENRE, songGenre);
            	values.put(DBAccessHelper.SONG_PLAY_COUNT, songPlayCount);
            	values.put(DBAccessHelper.SONG_YEAR, songYear);
            	values.put(DBAccessHelper.SONG_LAST_MODIFIED, "");
            	values.put(DBAccessHelper.BLACKLIST_STATUS, "FALSE"); //Keep the song whitelisted by default.
            	values.put(DBAccessHelper.ADDED_TIMESTAMP, date.getTime());
            	values.put(DBAccessHelper.RATING, songRating);
            	values.put(DBAccessHelper.LAST_PLAYED_TIMESTAMP, songLastPlayed);
            	values.put(DBAccessHelper.SONG_SOURCE, songSource);
            	values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
            	values.put(DBAccessHelper.SONG_DELETED, songDeleted);
            	values.put(DBAccessHelper.SONG_ID, songId);
            	
            	/* We're gonna have to save the song ID into the SONG_FILE_PATH 
            	 * field. Google Play Music playlist songs don't have a file path, but we're using a 
            	 * JOIN in PlaylistsFlippedFragment that relies on this field, so we'll need to use the 
            	 * song ID as a placeholder instead.
            	 *\/
            	values.put(DBAccessHelper.SONG_FILE_PATH, songId);
            	
            	//Add all the entries to the database to build the songs library.
            	libraryDBHelper.getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
            												 null, 
            												 values);	
            	
            }
            
            libraryDBHelper.getWritableDatabase().setTransactionSuccessful();
    		
    	} catch (SQLException e) {
    		// TODO Auto-generated method stub.
    		return "GENERIC_EXCEPTION";
    	} finally {
    		//Close the transaction.
    		libraryDBHelper.getWritableDatabase().endTransaction();
    		libraryDBHelper.close();
    	}
    	
    	/**************************************************************************
    	 * BUILD PLAYLISTS LIBRARY.
    	 **************************************************************************\/
    	//Clear out all the current Google Play Music playlists in the database.
    	DBAccessHelper musicLibraryPlaylistsDBHelper = new DBAccessHelper(mContext);
    	musicLibraryPlaylistsDBHelper.deleteAllGooglePlayMusicPlaylists();
    	
    	//Insert the songs and their metadata into Jams' local database.
    	/* To improve database insertion performance, we'll use a single transaction 
    	 * for the entire operation. SQLite journals each database insertion and 
    	 * creates a new transaction by default. We'll override this functionality 
    	 * and create a single transaction for all the database record insertions. 
    	 * In theory, this should reduce NAND memory overhead times and result in 
    	 * a 2x to 5x performance increase.
    	 *\/
    	try {
    		//Open a connection to the database.
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().beginTransaction();
    		
        	try {
    			playlistsList = gMusicClientCalls.getAllPlaylists(mContext.getApplicationContext()).getPlaylists();
    		} catch (JSONException e) {
    			return "GENERIC_EXCEPTION";
    		}

    		//Avoid "Divide by zero" errors.
    		int scanningPlaylistsIncrement;
    		if (playlistsList.size()!=0) {
    			scanningPlaylistsIncrement = 100000/playlistsList.size();
    		} else {
    			scanningPlaylistsIncrement = 100000/1;
    		}
    		currentTask = mContext.getResources().getString(R.string.syncing_with_google_play_music);
    		
    		for (int i=0; i < playlistsList.size(); i++) {

            	currentProgressValue = currentProgressValue + scanningPlaylistsIncrement;
            	publishProgress();
            	
            	//Get the playlist's metadata.
            	String playlistName = playlistsList.get(i).getTitle();
            	String playlistID = playlistsList.get(i).getPlaylistId();
            	String playlistArtUrl = "";

            	ContentValues playlistValues = new ContentValues();
            	playlistValues.put(DBAccessHelper.PLAYLIST_NAME, playlistName);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ID, playlistID);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ART_URL, playlistArtUrl);
            	playlistValues.put(DBAccessHelper.PLAYLIST_SOURCE, DBAccessHelper.GMUSIC);
            	playlistValues.put(DBAccessHelper.PLAYLIST_BLACKLIST_STATUS, "FALSE");
            	
            	//Add all the entries to the database to build the songs library.
            	musicLibraryPlaylistsDBHelper.getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME, 
            												 			   null, 
            												 			   playlistValues);	
            	
            }
    		
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().setTransactionSuccessful();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		//Seal off all connections to the database.
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().endTransaction();
    		musicLibraryPlaylistsDBHelper.close();
    	}
    	
    	//Build the genres library.
    	updateGenreSongCount();
    	
    	return "SUCCESS"; */
    	return null;
    }
     
    /**
     * Retrieves the user's playlists and their contents using the WebClient protocol.
     */
    //@SuppressWarnings("static-access")
	/*private void getPlaylistsWebClient() {
    	//Clear out all the current Google Play Music playlists in the database.\
    	mApp.getDBAccessHelper().deleteAllGooglePlayMusicPlaylists();
    	
    	//Insert the songs and their metadata into Jams' local database.
    	 To improve database insertion performance, we'll use a single transaction 
    	 * for the entire operation. SQLite journals each database insertion and 
    	 * creates a new transaction by default. We'll override this functionality 
    	 * and create a single transaction for all the database record insertions. 
    	 * In theory, this should reduce NAND memory overhead times and result in 
    	 * a 2x to 5x performance increase.
    	 
    	GMusicClientCalls gMusicClientCalls = GMusicClientCalls.getInstance(mContext);
    	try {
    		//Open a connection to the database.
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getWritableDatabase().beginTransaction();

    		//Get a list of all playlists from Google's servers.
    		playlistsJSONArray = gMusicClientCalls.getUserPlaylistsMobileClient(mContext);

    		//Avoid "Divide by zero" errors.
    		int scanningPlaylistsIncrement;
    		if (playlistsJSONArray.length()!=0) {
    			scanningPlaylistsIncrement = 100000/playlistsJSONArray.length();
    		} else {
    			scanningPlaylistsIncrement = 100000/1;
    		}
    		currentTask = mContext.getResources().getString(R.string.syncing_with_google_play_music);
    		
    		MobileClientPlaylistsSchema currentPlaylist = new MobileClientPlaylistsSchema();
    		WebClientSongsSchema currentPlaylistSong = new WebClientSongsSchema();
    		for (int i=0; i < playlistsJSONArray.length(); i++) {
    			
    			currentPlaylist = currentPlaylist.fromJsonObject(playlistsJSONArray.getJSONObject(i));
            	currentProgressValue = currentProgressValue + scanningPlaylistsIncrement;
            	publishProgress();
            	
            	//Get the playlist's metadata.
            	String playlistName = currentPlaylist.getName();
            	String playlistId = currentPlaylist.getPlaylistId();
            	String playlistArtUrl = "";
            	
            	//Retrieve all the song's within the current playlist.
            	JSONArray songsArray = gMusicClientCalls.getPlaylistEntriesWebClient(mContext, playlistId);
            	
            	//Loop through the current playlist's songs array and retrieve each song's metadata.
            	for (int j=0; j < songsArray.length(); j++) {
            		try {
            			currentPlaylistSong = currentPlaylistSong.fromJsonObject(songsArray.getJSONObject(j));
                		//Extract the current playlist song's metadata.
                    	String songTrackId = currentPlaylistSong.getId();
                    	String playlistEntryId = currentPlaylistSong.getPlaylistEntryId();
                    	
                    	ContentValues playlistValues = new ContentValues();
                    	playlistValues.put(DBAccessHelper.PLAYLIST_NAME, playlistName);
                    	playlistValues.put(DBAccessHelper.PLAYLIST_ID, playlistId); 
                    	playlistValues.put(DBAccessHelper.PLAYLIST_ART_URL, playlistArtUrl);
                    	playlistValues.put(DBAccessHelper.PLAYLIST_SOURCE, DBAccessHelper.GMUSIC);
                    	playlistValues.put(DBAccessHelper.PLAYLIST_BLACKLIST_STATUS, "FALSE");
                    	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_FILE_PATH, songTrackId);
                    	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID, playlistEntryId);
                    	playlistValues.put(DBAccessHelper.PLAYLIST_ORDER, j);
                    	
                    	//Add all the entries to the database to build the songs library.
                    	mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME, 
                    												 			   null, 
                    												 			   playlistValues);
            		} catch (Exception e) {
            			e.printStackTrace();
            			continue;
            		}
            		
            	}
            	
            }
    		
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getWritableDatabase().setTransactionSuccessful();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		//Seal off all connections to the database.
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getWritableDatabase().endTransaction();
    	}
    	
    }*/
    
    /*************************************************************************************
     * Retrieves the user's playlists and their contents using the MobileClient protocol.
     * 
     * @deprecated The entryIds that we're fetching from the MobileClient protocol seem 
     * to be broken. They don't work with reordering playlist songs. I'll fix this if/when 
     * I have time. Until then, I'm gonna use getPlaylistsWebClient().
     *************************************************************************************/
    @SuppressWarnings("static-access")
	private void getPlaylistsMobileClient() {
    	
/*    	//Clear out all the current Google Play Music playlists in the database.
    	DBAccessHelper musicLibraryPlaylistsDBHelper = new DBAccessHelper(mContext);
    	musicLibraryPlaylistsDBHelper.deleteAllGooglePlayMusicPlaylists();
    	
    	//Insert the songs and their metadata into Jams' local database.
    	 To improve database insertion performance, we'll use a single transaction 
    	 * for the entire operation. SQLite journals each database insertion and 
    	 * creates a new transaction by default. We'll override this functionality 
    	 * and create a single transaction for all the database record insertions. 
    	 * In theory, this should reduce NAND memory overhead times and result in 
    	 * a 2x to 5x performance increase.
    	 
    	try {
    		//Open a connection to the database.
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().beginTransaction();
    		
    		*//*****************************************************************************
    		 * The following calls are based on the MobileClient endpoints. Unfortunately, 
    		 * we can't get the correct entryIds for each song with these calls, so we'll 
    		 * have to revert to using the WebClient calls.
    		 *****************************************************************************//*
    		//Instantiate the GMusic API and retrieve an array of all playlists.
        	GMusicClientCalls gMusicClientCalls = GMusicClientCalls.getInstance(mContext);
        	try {
    			playlistsJSONArray = gMusicClientCalls.getUserPlaylistsMobileClient(mContext);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
        	
        	//Retrieve an array of all song entries within every playlist.
        	try {
        		playlistEntriesJSONArray = gMusicClientCalls.getPlaylistEntriesMobileClient(mContext);
        	} catch (JSONException e) {
        		e.printStackTrace();
        	}
        	
        	 Now that we have a JSONArray with all the unique playlists, it's time to 
        	 * index the playlistIds and their name within the array. This will 
        	 * allow us to efficiently figure out the name of the playlist without 
        	 * going through the entire original array over and over again.
        	 
        	MobileClientPlaylistsSchema currentPlaylist = new MobileClientPlaylistsSchema();
        	for (int k=0; k < playlistsJSONArray.length(); k++) {
        		currentPlaylist = currentPlaylist.fromJsonObject(playlistsJSONArray.getJSONObject(k));
        		if (!playlistIdsNameMap.containsKey(currentPlaylist.getPlaylistId())) {
        			playlistIdsNameMap.put(currentPlaylist.getPlaylistId(), currentPlaylist.getName());
        		}
        		
        	}
        	

    		//Avoid "Divide by zero" errors.
    		int scanningPlaylistsIncrement;
    		if (playlistEntriesJSONArray.length()!=0) {
    			scanningPlaylistsIncrement = 100000/playlistEntriesJSONArray.length();
    		} else {
    			scanningPlaylistsIncrement = 100000/1;
    		}
    		currentTask = mContext.getResources().getString(R.string.syncing_with_google_play_music);
    		
    		MobileClientPlaylistEntriesSchema currentPlaylistEntry = new MobileClientPlaylistEntriesSchema();
    		for (int i=0; i < playlistEntriesJSONArray.length(); i++) {
            	currentProgressValue = currentProgressValue + scanningPlaylistsIncrement;
            	publishProgress();
            	
            	//Get the playlist's metadata.
            	currentPlaylistEntry = currentPlaylistEntry.fromJsonObject(playlistEntriesJSONArray.getJSONObject(i));
            	String playlistName = playlistIdsNameMap.get(currentPlaylistEntry.getPlaylistId());
            	String playlistId = currentPlaylistEntry.getPlaylistId();
            	String id = currentPlaylistEntry.getId();
            	String clientId = currentPlaylistEntry.getClientId();
            	String trackId = currentPlaylistEntry.getTrackId();
            	
            	 GMusic's backend server uses horribly misleading JSON key names. 
            	 * Each playlist entry has an entryId. When reordering songs, this 
            	 * entryId is actually the clientId of the song. The songId matches 
            	 * trackId. The "id" key in the JSON response is seemingly not 
            	 * used for anything in particular.
            	 
            	ContentValues playlistValues = new ContentValues();
            	playlistValues.put(DBAccessHelper.PLAYLIST_NAME, playlistName);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ID, playlistId);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ART_URL, "");
            	playlistValues.put(DBAccessHelper.PLAYLIST_SOURCE, DBAccessHelper.GMUSIC);
            	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_ID, id);
            	playlistValues.put(DBAccessHelper.PLAYLIST_BLACKLIST_STATUS, "FALSE");
            	playlistValues.put(DBAccessHelper.PLAYLIST_ORDER, i);
            	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID, clientId);
            	
            	Log.e("DEBUG", "--------------------PLAYLIST ENTRY--------------------");
            	Log.e("DEBUG", "playlistName: " + playlistName);
            	Log.e("DEBUG", "playlistId: " + playlistId);
            	Log.e("DEBUG", "id: " + id);
            	Log.e("DEBUG", "clientId (entryId): " + clientId);
            	Log.e("DEBUG", "trackId: " + trackId);
            	
            	 We're gonna have to save the playlist's song IDs into the PLAYLIST_SONG_FILE_PATH 
            	 * field. Google Play Music playlist songs don't have a file path, but we're using a 
            	 * JOIN in PlaylistsFlippedFragment that relies on this field, so we'll need to use the 
            	 * "songId" param as a placeholder instead. The "trackId" key corresponds to "songId"
            	 * in the songs table.
            	 
            	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_FILE_PATH, trackId);
            	
            	//Add all the entries to the database to build the songs library.
            	musicLibraryPlaylistsDBHelper.getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME, 
            												 			   null, 
            												 			   playlistValues);
            	
            }
    		
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().setTransactionSuccessful();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		//Seal off all connections to the database.
    		musicLibraryPlaylistsDBHelper.getWritableDatabase().endTransaction();
    		musicLibraryPlaylistsDBHelper.close();
    	}*/
    	
    }
    
    /****************************************************************************************
     * Scans the entire Music Library for songs, fetches their genres, and inputs the total 
     * count for that genre into each song's genre.
     ****************************************************************************************/
    public void updateGenreSongCount() {
    	
    	//We'll get the number of songs in a particular genre and apply that tag to all songs.
    	String genre = "";
    	int songCount = 0;
    	int buildingGenresIncrement;
    	currentTask = "Building Genres";
    	if (genresList.size()!=0) {
    		buildingGenresIncrement = 100000/genresList.size();
    	} else {
        	buildingGenresIncrement = 100000/1;
    	}

		//Open a single transaction connection to keep the operation as efficient as possible.    	
    	for (int i=0; i < genresList.size(); i++) {
    		
        	currentProgressValue = currentProgressValue + buildingGenresIncrement;
        	publishProgress();
    		
    		try {
        		genre = genresList.get(i);
        		
        		if (genre.contains("'")) {
        			genre = genre.replace("'", "''");
        		}
        		
        		//Get the number of songs in this genre.
        		songCount = mApp.getDBAccessHelper().getGenreSongCount(genre);
        		mApp.getDBAccessHelper().insertNumberOfSongsInGenre(genre, songCount);
        		
    		} catch (Exception e) {
    			e.printStackTrace();
    			continue;
    		}

    	}
    	
    }
    
    /**
     * Public method that provides access to the onProgressUpdate() method.
     * Used to update the progress bar from a different class/activity.
     * 
     * @param progressParams 
     */
    public void callOnProgressUpdate(String... progressParams) {
    	publishProgress(progressParams);
    }
    
    @Override
    protected void onProgressUpdate(String... progressParams) {
    	super.onProgressUpdate(progressParams);
    	
    	/*//Update the notification.
    	BuildMusicLibraryService.mBuilder.setTicker(null);
    	BuildMusicLibraryService.mBuilder.setContentTitle(mContext.getResources().getString(R.string.getting_google_play_music_library));
    	BuildMusicLibraryService.mBuilder.setContentText(currentTask);
    	BuildMusicLibraryService.mBuilder.setContentInfo(null);
    	BuildMusicLibraryService.mBuilder.setProgress(100000, currentProgressValue, false);
    	BuildMusicLibraryService.mNotification = BuildMusicLibraryService.mBuilder.build();
    	BuildMusicLibraryService.mNotifyManager.notify(BuildMusicLibraryService.mNotificationId, 
    												   BuildMusicLibraryService.mNotification);*/
    	
    }

    @Override
    protected void onPostExecute(String arg0) {
    	
    	//Release the wakelock.
    	wakeLock.release();
        	
    }

}
