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

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

/*********************************************************************************
 * Custom CursorLoader class that adds support for SQLite databases.
 * The default CursorLoader that's supplied through the Android
 * compatibility library only supports ContentProviders. 
 * 
 * Since this app maintains it's own music library/database and doesn't depend 
 * on Android's MediaStore ContentProvider, we need a way to channel 
 * queries directly to a raw SQLite database. This class simply replaces 
 * every instance of a ContentProvider query with queries that connect 
 * directly to an SQLite database.
 * 
 * @author Saravan Pantham
 *********************************************************************************/
public class SQLiteDBCursorLoader extends AsyncTaskLoader<Cursor> {
    final ForceLoadContentObserver mObserver;

    String mTableName;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
    String mLimit = null;

    Cursor mCursor;
    boolean mDistinctPlaylists = false;
    boolean mDistinctGenres = false;
    String mDistinctField = "";

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
    	
    /*	//Retrieve the appropriate cursor based on @param mDistinctField;
    	Cursor cursor = null;
    	if (mDistinctField.equals("PLAYLISTS")) {
    		DBAccessHelper playlistsDBHelper = new DBAccessHelper(getContext());
    		cursor = playlistsDBHelper.getAllUniquePlaylists(mSelection);
    		
    	} else if (mDistinctField.equals("GENRES")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllUniqueGenres(mSelection);

    	} else if (mDistinctField.equals("ARTISTS")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllUniqueArtists(mSelection);
    		
    	} else if (mDistinctField.equals("ALBUMS")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllUniqueAlbums(mSelection);
    		
    	} else if (mDistinctField.equals("SONGS")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllSongsSearchable(mSelection);
    		
    	} else if (mDistinctField.equals("ARTISTS_FLIPPED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllUniqueAlbumsByArtist(mSelection);
    		
    	} else if (mDistinctField.equals("ARTISTS_FLIPPED_SONGS")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllSongsByArtistAlbum(mSelection);
    		
    	} else if (mDistinctField.equals("ALBUMS_FLIPPED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllSongsByArtistAlbum(mSelection);
    		
    	} else if (mDistinctField.equals("TOP_25_PLAYED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getTop25PlayedTracks(mSelection);
    		
    	} else if (mDistinctField.equals("RECENTLY_ADDED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getRecentlyAddedSongs(mSelection);
    		
    	} else if (mDistinctField.equals("TOP_RATED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getTopRatedSongs(mSelection);
    		
    	} else if (mDistinctField.equals("RECENTLY_PLAYED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getRecentlyPlayedSongs(mSelection);
    		
    	} else if (mDistinctField.equals("GENRES_FLIPPED")) {
    		DBAccessHelper dBHelper = new DBAccessHelper(getContext());
    		cursor = dBHelper.getAllSongsInGenre(mSelection);
    		
    	}
        
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            registerContentObserver(cursor, mObserver);
        }*/
        
        return null;
    }

    /**
     * Registers an observer to get notifications from the content mApp
     * when the cursor needs to be refreshed.
     */
    void registerContentObserver(Cursor cursor, ContentObserver observer) {
        cursor.registerContentObserver(mObserver);
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public SQLiteDBCursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public SQLiteDBCursorLoader(Context context, String tableName, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        super(context);
        
        mObserver = new ForceLoadContentObserver();
        mTableName = tableName;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }
    
    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public SQLiteDBCursorLoader(Context context, String tableName, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, String limit) {
        super(context);
        
        mObserver = new ForceLoadContentObserver();
        mTableName = tableName;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
        mLimit = limit;
    }
    
    /**
     * Creates a fully-specified, DISTINCT CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public SQLiteDBCursorLoader(Context context, boolean distinctPlaylists) {
        super(context);
        
        mDistinctPlaylists = distinctPlaylists;
        mObserver = new ForceLoadContentObserver();

    }
    
    /**
     * Creates a fully-specified, DISTINCT CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     * 
     * boolean dummy is a dummy variable, used to differentiate constructors.
     */
    public SQLiteDBCursorLoader(Context context, boolean distinctGenres, boolean dummy) {
        super(context);
        
        mDistinctGenres = distinctGenres;
        mObserver = new ForceLoadContentObserver();

    }
    
    /**
     * Creates a fully-specified, DISTINCT CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     * 
     * boolean dummy is a dummy variable, used to differentiate constructors.
     */
    public SQLiteDBCursorLoader(Context context, String distinctField, String selection) {
        super(context);
        
        mDistinctField = distinctField;
        mSelection = selection;
        mObserver = new ForceLoadContentObserver();

    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        
        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    public String getTableName() {
        return mTableName;
    }

    public void setUri(String tableName) {
        mTableName = tableName;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix); writer.print("mUri="); writer.println(mTableName);
        writer.print(prefix); writer.print("mProjection=");
                writer.println(Arrays.toString(mProjection));
        writer.print(prefix); writer.print("mSelection="); writer.println(mSelection);
        writer.print(prefix); writer.print("mSelectionArgs=");
                writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix); writer.print("mSortOrder="); writer.println(mSortOrder);
        writer.print(prefix); writer.print("mCursor="); writer.println(mCursor);
        writer.print(prefix); writer.print("mContentChanged="); //writer.println(mContentChanged);
    }
}
