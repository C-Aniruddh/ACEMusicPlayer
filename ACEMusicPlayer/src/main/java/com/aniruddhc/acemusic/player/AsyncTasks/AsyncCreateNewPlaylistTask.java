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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;

public class AsyncCreateNewPlaylistTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private Cursor mCursor;
    private String mPlaylistName;
    private String mPlaylistId;
    private SharedPreferences sharedPreferences;
    private DBAccessHelper musicLibraryDBHelper;
    private DBAccessHelper musicLibraryPlaylistsDBHelper;
    
    private String mArtist;
    private String mAlbum;
    private String mSong;
    private String mGenre;
    private String mAlbumArtist;
    private String mAddType;
    
    private final String[] PROJECTION_PLAYLIST = new String[] {
        MediaStore.Audio.Playlists._ID,
        MediaStore.Audio.Playlists.NAME,
        MediaStore.Audio.Playlists.DATA
    };
    
    public AsyncCreateNewPlaylistTask(Context context, 
    								  String playlistName, 
    								  String ARTIST, 
    								  String ALBUM, 
    								  String SONG, 
    								  String GENRE, 
    								  String ALBUM_ARTIST,
    								  String ADD_TYPE) {
    	
    	mContext = context;
    	mPlaylistName = playlistName;
    	sharedPreferences = context.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	musicLibraryDBHelper = new DBAccessHelper(mContext);
    	musicLibraryPlaylistsDBHelper = new DBAccessHelper(mContext);
    	
    	mArtist = ARTIST;
    	mAlbum = ALBUM;
    	mSong = SONG;
    	mGenre = GENRE;
    	mAlbumArtist = ALBUM_ARTIST;
    	mAddType = ADD_TYPE;
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
		/*//Replace illegal characters in the playlistName.
		if (mPlaylistName.contains("/")) {
			mPlaylistName = mPlaylistName.replace("/", "_");
		}
		
		if (mPlaylistName.contains("\\")) {
			mPlaylistName = mPlaylistName.replace("\\", "_");
		}
    	
    	//Get the cursor with the playlist elements.
    	String playlistFolderPath = sharedPreferences.getString("PLAYLISTS_SAVE_FOLDER", 
    															Environment.getExternalStorageDirectory() + "/Playlists/");
    	
    	String playlistFilePath = playlistFolderPath + mPlaylistName + ".m3u";
    	mCursor = AddPlaylistUtils.getPlaylistElementsCursor(mContext, 
    														 musicLibraryDBHelper, 
    														 sharedPreferences, 
    														 mArtist, 
    														 mAlbum, 
    														 mSong, 
    														 mGenre, 
    														 mAlbumArtist,
    														 mAddType);
    	
		//If GMusic is enabled, create the playlist on Google's servers first.
    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        	String playlistId = null;
    		try {
    			playlistId = GMusicClientCalls.createPlaylist(mContext, mPlaylistName);
    			mPlaylistId = playlistId;
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    			return false;
    		} catch (JSONException e) {
    			e.printStackTrace();
    			return false;
    		} catch (Exception e) {
    			e.printStackTrace();
    			return false;
    		}
    		
    	} else {
    		//Create the playlist in Android's MediaStore. mPlaylistId will be assigned in this method.
    		createMediaStorePlaylist(mPlaylistName);
    	}
    	
    	Cursor gMusicCursor = null;
    	ArrayList<Integer> gMusicSongOrder = new ArrayList<Integer>();
    	for (int i=0; i < mCursor.getCount(); i++) {
    		try {
    			mCursor.moveToPosition(i);
    			String songSource = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_SOURCE));
    			
    			 This if/else block will handle the different operations that need to be 
    			 * done/ommitted for each different song type (Google Play Music vs local
    			 * song file).
    			 
    			String clientId = "";
    			if (songSource.equals(DBAccessHelper.GMUSIC)) {
    				
    				//Retrieve the clientId of the current song from GMusic's content mApp.
        	    	Uri googlePlayMusicContentProviderUri = Uri.parse("content://com.google.android.music.MusicContent/audio");
        	    	String[] projection = { "SourceId", "ClientId AS client_id" };
        			
        			String songId = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ID));
        			String selection = "SourceId=" + "'" + songId + "'";
        	    	gMusicCursor = mContext.getContentResolver().query(googlePlayMusicContentProviderUri, projection, selection, null, null);
        	    	gMusicCursor.moveToFirst();
        	    	clientId = gMusicCursor.getString(1);
        			
        	    	//Populate the mutations JSONArray.
        			JSONObject createObject = new JSONObject();

        			createObject.put("lastModifiedTimestamp", "0");
        			createObject.put("playlistId", mPlaylistId);
        			createObject.put("creationTimestamp", "-1");
        			createObject.put("type", "USER_GENERATED");
        			createObject.put("source", 1);
        			createObject.put("deleted", false);
        			createObject.put("trackId", songId);
        			createObject.put("clientId", clientId);
        			
        			//Add the request to the JSONArray queue that will update Google's servers.
        	    	GMusicClientCalls.putCreatePlaylistEntryRequest(createObject);	
        	    	gMusicSongOrder.add(i);
        	    	
    			} else {
    				
    				//Add the song to Android's MediaStore database.
    				String artist = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
    				String album = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM));
    				String title = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_TITLE));
    				
    				try {
    					long audioId = getSongAudioId(artist, album, title);
    					if (audioId!=-1) {
    						addSongToPlaylist(mContext.getContentResolver(), audioId, Long.parseLong(mPlaylistId));
    					}
        				
    				} catch (Exception e) {
    					//Just fail silently if this song can't be inserted into the playlist.
    					e.printStackTrace();
    				}

        			//Add the song to the playlists database.
        			musicLibraryPlaylistsDBHelper.addNewPlaylist(mPlaylistName, 
        														 playlistFilePath, 
        														 playlistFolderPath, 
        														 mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)), 
        														 "LOCAL", 
        														 mPlaylistId,
        														 mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ID)),
        														 new Date().getTime(),
        														 i,
        														 UUID.randomUUID().toString());
    			}
    		
    		} catch (Exception e) {
    			e.printStackTrace();
    			continue;
    		}
    		
    	}
    	
    	//Show a confirmation toast message.
    	publishProgress(new Integer[] {0});
    	
    	//Send the HTTP request that will update Google's servers.
    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		try {
        		if (GMusicClientCalls.getQueuedMutationsCount()!=0) {
        			GMusicClientCalls.modifyPlaylist(mContext);
        			
        			//Reload the entire playlist.
        			reloadPlaylistEntries(gMusicSongOrder);
        			
        		}
        		
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}

    	if (gMusicCursor!=null) {
    		gMusicCursor.close();
    		gMusicCursor = null;
    	}*/
    	
    	return true;
    }
    
    public void reloadPlaylistEntries(ArrayList<Integer> gMusicSongOrder) {
    	/*JSONArray jsonArray = null;
    	try {
			jsonArray = GMusicClientCalls.getPlaylistEntriesWebClient(mContext, mPlaylistId);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Remove all GMusic songs in the current playlist (within the local databse).
		musicLibraryPlaylistsDBHelper.removeGMusicSongsFromPlaylist(mContext, mPlaylistId);
    	
    	//Loop through the playlist's songs array and retrieve each song's metadata.
		WebClientSongsSchema currentPlaylistSong = new WebClientSongsSchema();
    	for (int j=0; j < jsonArray.length(); j++) {
    		try {
    			currentPlaylistSong = currentPlaylistSong.fromJsonObject(jsonArray.getJSONObject(j));
        		//Extract the current playlist song's metadata.
            	String songTrackId = currentPlaylistSong.getId();
            	String playlistEntryId = currentPlaylistSong.getPlaylistEntryId();
            	
            	ContentValues playlistValues = new ContentValues();
            	playlistValues.put(DBAccessHelper.PLAYLIST_NAME, mPlaylistName);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ID, mPlaylistId); 
            	playlistValues.put(DBAccessHelper.PLAYLIST_ART_URL, "");
            	playlistValues.put(DBAccessHelper.PLAYLIST_SOURCE, DBAccessHelper.GMUSIC);
            	playlistValues.put(DBAccessHelper.PLAYLIST_BLACKLIST_STATUS, "FALSE");
            	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_FILE_PATH, songTrackId);
            	playlistValues.put(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID, playlistEntryId);
            	playlistValues.put(DBAccessHelper.PLAYLIST_ORDER, gMusicSongOrder.get(j));
            	
            	//Add all the entries to the database to build the songs library.
            	musicLibraryPlaylistsDBHelper.getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME, 
            												 			   null, 
            												 			   playlistValues);
    		} catch (Exception e) {
    			e.printStackTrace();
    			continue;
    		}
    		
    	}*/
        	
    }
    
    private void createMediaStorePlaylist(String playlistName) {
    	ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistName);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        Uri mUri = mContext.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
        if (mUri != null) {
            Cursor c = mContext.getContentResolver().query(mUri, PROJECTION_PLAYLIST, null, null, null);
            if (c != null) {
                /* Save the newly created ID so it can be selected. Names are allowed to be duplicated,
                 * but IDs can never be. */
            	c.moveToFirst();
                mPlaylistId = "" + c.getLong(c.getColumnIndex(MediaStore.Audio.Playlists._ID));
                c.close();
            }

        }
        
    }
    
    private void addSongToPlaylist(ContentResolver resolver, long audioId, long playlistId) {

        String[] cols = new String[] {
                "count(*)"
        };
        
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Long.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
    }
    
    private long getSongAudioId(String artist, String album, String title) {
    	artist = artist.replace("'", "''");
    	album = album.replace("'", "''");
    	title = title.replace("'", "''");
    	
    	String selection = MediaStore.Audio.AudioColumns.ALBUM + "=" + "'" + album + "'" + " AND "
    					 + MediaStore.Audio.AudioColumns.ARTIST + "=" + "'" + artist + "'" + " AND "
    					 + MediaStore.Audio.AudioColumns.TITLE + "=" + "'" + title + "'";
    	
    	Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
    														null, 
    														selection, 
    														null,
    														null);
    	
    	if (cursor!=null && cursor.getCount() > 0) {
    		cursor.moveToFirst();
    		return cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
    	} else {
    		return -1;
    	}
    	
    }
    
    @Override
    protected void onProgressUpdate(Integer... values) {
    	super.onProgressUpdate(values);
    	switch(values[0]) {
    	case 0:
    		//Common.displayToast(R.string.playlist_created, Toast.LENGTH_SHORT);
    		Toast.makeText(mContext, R.string.playlist_created, Toast.LENGTH_SHORT).show();
    		break;
    	case 1:
    		//Common.displayToast(R.string.playlist_could_not_be_created, Toast.LENGTH_SHORT);
    		Toast.makeText(mContext, R.string.playlist_could_not_be_created, Toast.LENGTH_SHORT).show();
    		break;
    	}
    	
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
    	
    	if (mCursor!=null) {
    		mCursor.close();
        	mCursor = null;
    	}
    
	}

}
