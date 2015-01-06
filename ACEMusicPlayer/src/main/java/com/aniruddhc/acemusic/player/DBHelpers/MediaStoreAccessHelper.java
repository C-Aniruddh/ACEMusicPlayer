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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Helper class that contains methods that access 
 * Android's MediaStore. For methods that access 
 * Jams' private database, see DBAccessHelper.
 * 
 * @author Saravan Pantham
 */
public class MediaStoreAccessHelper {

	/* Hidden album artist field. See: http://stackoverflow.com/questions/20710542/
	 * why-doesnt-mediastore-audio-albums-external-content-uri-provide-an-accurate-al
	 */
	public static final String ALBUM_ARTIST = "album_artist";

	/**
	 * Queries MediaStore and returns a cursor with songs limited 
	 * by the selection parameter.
	 */
	public static Cursor getAllSongsWithSelection(Context context, 
												  String selection, 
												  String[] projection, 
												  String sortOrder) {
		
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		
		return contentResolver.query(uri, projection, selection, null, sortOrder);
		
	}
	
	/**
	 * Queries MediaStore and returns a cursor with all songs.
	 */
	public static Cursor getAllSongs(Context context, 
									 String[] projection, 
									 String sortOrder) {
		
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
		
		return contentResolver.query(uri, null, selection, null, sortOrder);
		
	}
	
	/**
	 * Queries MediaStore and returns a cursor with all unique artists, 
	 * their ids, and their number of albums.
	 */
	public static Cursor getAllUniqueArtists(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		String[] projection = { MediaStore.Audio.Artists._ID, 
							    MediaStore.Audio.Artists.ARTIST, 
							    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS };
		
		return contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
									 projection, 
									 null, 
									 null, 
									 MediaStore.Audio.Artists.ARTIST + " ASC");
		
	}
	
	/**
	 * Queries MediaStore and returns a cursor with all unique albums, 
	 * their ids, and their number of songs.
	 */
	public static Cursor getAllUniqueAlbums(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		String[] projection = { MediaStore.Audio.Albums._ID, 
							    MediaStore.Audio.Albums.ALBUM, 
							    MediaStore.Audio.Albums.NUMBER_OF_SONGS };
		
		return contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
									 projection, 
									 null, 
									 null, 
									 MediaStore.Audio.Albums.ALBUM + " ASC");
		
	}
	
	/**
	 * Queries MediaStore and returns a cursor with all unique genres 
	 * and their ids.
	 */
	public static Cursor getAllUniqueGenres(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		String[] projection = { MediaStore.Audio.Genres._ID, 
							    MediaStore.Audio.Genres.NAME };
		
		return contentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, 
									 projection, 
									 null, 
									 null, 
									 MediaStore.Audio.Genres.NAME + " ASC");
		
	}

    /**
     * Queries MediaStore and returns a cursor with all unique playlists.
     */
    public static Cursor getAllUniquePlaylists(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { MediaStore.Audio.Playlists._ID,
                                MediaStore.Audio.Playlists.NAME };

        return contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                     projection,
                                     null,
                                     null,
                                     MediaStore.Audio.Playlists.NAME + " ASC");

    }
	
}
