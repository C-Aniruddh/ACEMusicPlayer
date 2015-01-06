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
package com.aniruddhc.acemusic.player.PlaylistUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;

public class AddPlaylistUtils {
	
	//Retrieves a cursor that contiains the songs that need to be added to the new playlist.
	public static Cursor getPlaylistElementsCursor(Context context, 
										 		   DBAccessHelper musicLibraryDBHelper, 
										 		   SharedPreferences sharedPreferences,
										 		   String ARTIST,
										 		   String ALBUM,
										 		   String SONG,
										 		   String GENRE,
										 		   String ALBUM_ARTIST, 
										 		   String ADD_TYPE) {

		/*//Initialize the DB access and cursor object.
		Cursor cursor = null;
		
		if (ADD_TYPE.equals("ARTIST")) {
			String selection = null;
			String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    currentLibrary = currentLibrary.replace("'", "''");
		    ARTIST = ARTIST.replace("'", "''");
	    	
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'";
		        cursor = musicLibraryDBHelper.getAllSongsSearchable(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'"
		    			  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistSearchable(selection);
		        
		    } else {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" + " AND "
		    			  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistInLibrary(selection);
		        
		    }
		    
		    ARTIST = ARTIST.replace("''", "'");
		    
		} else if (ADD_TYPE.equals("ALBUM")) {
			String selection = null;
			String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    currentLibrary = currentLibrary.replace("'", "''");
			ARTIST = ARTIST.replace("'", "''");
			ALBUM = ALBUM.replace("'", "''");
			
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM  + "'" + " AND "
						  + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistAlbum(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" + " AND "
		    			  + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'"
		    			  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistSearchable(selection);
		        
		    } else {
		    	selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM  + "'" + " AND "
						  + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" + " AND " 
						  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistAlbumInLibrary(selection);
		        
		    }
		    
		    ARTIST = ARTIST.replace("''", "'");
		    ALBUM = ALBUM.replace("''", "'");
			
		} else if (ADD_TYPE.equals("SONG")) {
			String selection = null;
			SONG = SONG.replace("'", "''");
			ARTIST = ARTIST.replace("'", "''");
			ALBUM = ALBUM.replace("'", "''");
		    String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    
		    currentLibrary = currentLibrary.replace("'", "''");
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
		    	selection = " AND " + DBAccessHelper.SONG_TITLE + "=" + "'" + SONG + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'";
		        cursor = musicLibraryDBHelper.getAllSongsSearchable(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_TITLE + "=" + "'" + SONG + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'"
		    			  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistSearchable(selection);
		        
		    } else {
	    		selection = " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'"
	    				  + " AND " + DBAccessHelper.SONG_TITLE + "=" + "'" + SONG + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + ARTIST + "'" 
		    			  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'";
		        cursor = musicLibraryDBHelper.getAllSongsInLibrarySearchable(selection);
		        
		    }
		    
		    SONG = SONG.replace("''", "'");
			ARTIST = ARTIST.replace("''", "'");
			ALBUM = ALBUM.replace("''", "'");

		} else if (ADD_TYPE.equals("GENRE")) {
			String selection = null;
			GENRE = GENRE.replace("'", "''");
			String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    currentLibrary = currentLibrary.replace("'", "''");
			
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + GENRE + "'";
		        cursor = musicLibraryDBHelper.getAllSongsInGenre(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + GENRE + "'"
						  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		        cursor = musicLibraryDBHelper.getAllSongsByArtistSearchable(selection);
		        
		    } else {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + GENRE  + "'" + " AND " 
	    				  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
		        cursor = musicLibraryDBHelper.getAllSongsInGenreInLibrary(selection);
		        
		    }
		    
		    GENRE = GENRE.replace("''", "'");
		    
		} else if (ADD_TYPE.equals("ALBUM_ARTIST")) {
			String selection = null;
			ALBUM_ARTIST = ALBUM_ARTIST.replace("'", "''");
			
			String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    currentLibrary = currentLibrary.replace("'", "''");
			
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST + "'";
		        cursor = musicLibraryDBHelper.getAllSongsInAlbumArtist(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST + "'"
						  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		        cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistSearchable(selection);
		        
		    } else {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST  + "'" + " AND " 
	    				  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
		        cursor = musicLibraryDBHelper.getAllSongsInAlbumArtistInLibrary(selection);
		        
		    }
		    
		    ALBUM_ARTIST = ALBUM_ARTIST.replace("''", "'");
		} else if (ADD_TYPE.equals("ALBUM_BY_ALBUM_ARTIST")) {
			String selection = null;
			ALBUM_ARTIST = ALBUM_ARTIST.replace("'", "''");
			ALBUM = ALBUM.replace("'", "''");
			
			String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, context.getResources().getString(R.string.all_libraries));
		    currentLibrary = currentLibrary.replace("'", "''");
			
		    if (currentLibrary.equals(context.getResources().getString(R.string.all_libraries))) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST + "'"
	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'";
	    		cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
		        
		    } else if (currentLibrary.equals(context.getResources().getString(R.string.google_play_music_no_asterisk))) {
		    	selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST + "'"
		    			  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'"
						  + " AND " + DBAccessHelper.SONG_SOURCE + " = " + "'GOOGLE_PLAY_MUSIC'";
		    	cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
		        
		    } else {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + ALBUM_ARTIST  + "'"
	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + ALBUM + "'"
	    				  + " AND " + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
		        cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbumInLibrary(selection);
		        
		    }
		    
		    ALBUM_ARTIST = ALBUM_ARTIST.replace("''", "'");
		    ALBUM = ALBUM.replace("''", "'");
		}*/
		
		return null;
	}
	
	//Adds the specified song to Android's MediaStore.
	public static void addToMediaStorePlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] {"count(*)"};
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
        
    }
	
	public static void removeFromPlaylist(ContentResolver resolver, int audioId, long playlistId) {
	    String[] cols = new String[] {"count(*)"};
	    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
	    Cursor cur = resolver.query(uri, cols, null, null, null);
	    cur.moveToFirst();
	    final int base = cur.getInt(0);
	    cur.close();
	    ContentValues values = new ContentValues();
	
	    resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + audioId, null);
	    
	}
	
}
