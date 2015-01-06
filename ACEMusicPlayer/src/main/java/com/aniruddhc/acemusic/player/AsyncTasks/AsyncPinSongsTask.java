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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.GMusicHelpers.GMusicClientCalls;
import com.aniruddhc.acemusic.player.Services.PinGMusicSongsService;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncPinSongsTask extends AsyncTask<String, Integer, Boolean> {
	
    private Context mContext;
    private Common mApp;
    private String mSaveLocation;
    private String mFileName;
    private int fileSize;
    private int currentDownloadedSize = 0;
    
	private ArrayList<String> songIdsList;
	private ArrayList<String> songTitlesList;
	private int i;
	
	//File size/unit dividers
	private static final long kiloBytes = 1024;
	private static final long megaBytes = kiloBytes * kiloBytes;
	private static final long gigaBytes = megaBytes * kiloBytes;
	private static final long teraBytes = gigaBytes * kiloBytes;
    
    public AsyncPinSongsTask(Context context) {
    	
    	//Context.
    	mContext = context;
    	songIdsList = new ArrayList<String>();
    	songTitlesList = new ArrayList<String>();
    	
    	//If the cache storage directory for local copies doesn't exist, go ahead and create it.
    	File cacheFolder = null;
		try {
			cacheFolder = new File(mContext.getCacheDir().getCanonicalPath() + "/music");
	    	if (!cacheFolder.exists()) {
	    		cacheFolder.mkdirs();
	    	}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(mContext, R.string.download_failed_to_initialize, Toast.LENGTH_LONG).show();
			return;
		}
    	
    	mApp = (Common) mContext.getApplicationContext();
    	try {
			mSaveLocation = cacheFolder.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	//The initial notification will display a "Starting download" message + indeterminate progress bar.
    	PinGMusicSongsService.mBuilder.setContentTitle(mContext.getResources().getString(R.string.starting_download));
    	PinGMusicSongsService.mBuilder.setTicker(mContext.getResources().getString(R.string.starting_download));
    	PinGMusicSongsService.mBuilder.setSmallIcon(R.drawable.pin_light);
    	PinGMusicSongsService.mBuilder.setProgress(0, 0, true);
    	
    	PinGMusicSongsService.mNotifyManager.notify(PinGMusicSongsService.notificationID, 
    											    PinGMusicSongsService.mBuilder.build());
    }
 
	@Override
    protected Boolean doInBackground(String... params) {
		
		//Iterate through the cursor and download/cache the requested songs.
		boolean getAllPinnedTracks = false;
		if (mApp.getPinnedSongsCursor()==null) {
			//The user asked to get all pinned songs from the official GMusic app.
			getAllPinnedTracks = true;
			
			//Check to make sure that the official GMusic app exists.
			PackageManager pm = mContext.getPackageManager();
	    	boolean installed = false;
	    	try {
				pm.getPackageInfo("com.google.android.music", PackageManager.GET_ACTIVITIES);
				installed = true;
			} catch (NameNotFoundException e1) {
				//The app isn't installed.
				installed = false;
			}
	    	
	    	if (installed==false) {
	    		//The app isn't installed. Notify the user.
	    		publishProgress(new Integer[] {2});
	    		return false;
	    	}
	    	
	    	//Query GMusic's content mApp for pinned songs.
	    	Uri googlePlayMusicContentProviderUri = Uri.parse("content://com.google.android.music.MusicContent/audio");
	    	String[] projection = { "title", "TrackType AS track_type", "LocalCopyPath AS local_copy_path",
	    							"SourceType AS source_type", "SourceId" };
	    	
	    	/* source_type values:
	    	 * 0: Local file (not used).
	    	 * 1: Unknown.
	    	 * 2: Personal, free GMusic library (used).
	    	 * 3: All Access (not used).
	    	 */
	    	String selection = "source_type=2 AND track_type=0 AND local_copy_path<>''";
	    	mApp.setPinnedSongsCursor(mContext.getContentResolver().query(googlePlayMusicContentProviderUri, 
	    																  projection, 
	    																  selection, 
	    																  null, 
	    																  null));

		}
		
		/* Load the cursor data into a temp ArrayList. If the app is closed, the cursor 
		 * will also be closed, so we need to preserve it.
		 */
		mApp.getPinnedSongsCursor().moveToPosition(-1);
		while (mApp.getPinnedSongsCursor().moveToNext()) {
			//Download the song only if it's from GMusic.
			if (getAllPinnedTracks || mApp.getPinnedSongsCursor().getString(
									  mApp.getPinnedSongsCursor().getColumnIndex(
									  DBAccessHelper.SONG_SOURCE)).equals(DBAccessHelper.GMUSIC)) {
				
		    	//Retrieve the song ID of current song and set it as the file name.
		    	String songID = "";
		    	String songTitle = "";
		    	if (mApp.getPinnedSongsCursor().getColumnIndex("SourceId")!=-1) {
		    		songID = mApp.getPinnedSongsCursor().getString(
		    				 mApp.getPinnedSongsCursor().getColumnIndex(
		    						 "SourceId"));
		    		
		    		songTitle = mApp.getPinnedSongsCursor().getString(
		    				 	mApp.getPinnedSongsCursor().getColumnIndex(
		    						 "title"));
		    	} else {
		        	songID = mApp.getPinnedSongsCursor().getString(
		       			 	 mApp.getPinnedSongsCursor().getColumnIndex(
		   		 					DBAccessHelper.SONG_ID));
		        	
		        	songTitle = mApp.getPinnedSongsCursor().getString(
		    				 	mApp.getPinnedSongsCursor().getColumnIndex(
		    						 DBAccessHelper.SONG_TITLE));
		    	}
		    	
				songIdsList.add(songID);
				songTitlesList.add(songTitle);
			}
			
		}

		//Clear out Common's cursor.
		if (mApp.getPinnedSongsCursor()!=null) {
			mApp.getPinnedSongsCursor().close();
			mApp.setPinnedSongsCursor(null);
		}
		
		//Iterate through the songs and download them.
		for (i=0; i < songIdsList.size(); i++) {
			downloadSong(songIdsList.get(i));
		}
		
    	return true;
    }
    
    public void downloadSong(String songID) {
    	
    	//Update the notification.
    	publishProgress(new Integer[] {1});

    	//Check if the file already exists. If so, skip it.
    	File tempFile = new File(mSaveLocation + "/" + songID + ".mp3");
    	
    	if (tempFile.exists()) {
    		tempFile = null;
    		return;
    	}
    	
    	tempFile = null;
    	mFileName = songID + ".mp3";
    	
		//Get the url for the song file using the songID.
		URL url = null;
		try {
			url = GMusicClientCalls.getSongStream(songID).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			//Download the file to the specified location.
			//Create a new connection to the server.
		    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		
		    //Set the request method of the connection.
		    urlConnection.setRequestMethod("GET");
		    urlConnection.setDoOutput(true);
		
		    //Aaaand connect!
		    urlConnection.connect();
		
		    //Set the destination path of the file.
		    File SDCardRoot = new File(mSaveLocation);

		    //Create the destination file.
		    File file = new File(SDCardRoot, mFileName);
		
		    //We'll use this to write the downloaded data into the file we created
		    FileOutputStream fileOutput = new FileOutputStream(file);
		
		    //Aaand we'll use this to read the data from the server.
		    InputStream inputStream = urlConnection.getInputStream();
		
		    //Total size of the file.
		    fileSize = urlConnection.getContentLength();
		    
		    //Specifies how much of the total size has been downloaded.
		    currentDownloadedSize = 0;
		
		    //Buffers galore!
		    byte[] buffer = new byte[1024];
		    int bufferLength = 0; //Used to store a temporary size of the buffer.
		    int updateValue = 0;
		    
		    //Read through the input buffer and write the destination file, piece by piece.
		    while ((bufferLength = inputStream.read(buffer)) > 0) {
	            fileOutput.write(buffer, 0, bufferLength);
	            currentDownloadedSize += bufferLength;
	            updateValue = updateValue + 1;
	            
	            //Update the notification for every 100 iterations.
	            if (updateValue==100) {
	            	publishProgress(new Integer[] {0});
	            	updateValue = 0;
	            }
	        
		    }
		    
		    try {
		    	//Close the output stream.
		    	fileOutput.close();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		    
		//And we're done!
		} catch (MalformedURLException e) {
			return;
		} catch (IOException e) {
			return;
		} catch (Exception e) {
			return;
		}
		
		//Insert the file path of the local copy into the DB.
		ContentValues values = new ContentValues();
		String selection = DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";

		String localCopyPath = null;
		try {
			localCopyPath = mContext.getCacheDir().getCanonicalPath() + "/music/" + mFileName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		values.put(DBAccessHelper.LOCAL_COPY_PATH, localCopyPath);
		mApp.getDBAccessHelper()
			.getWritableDatabase()
			.update(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
					values, 
					selection, 
					null);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    	int updateCode = values[0];
    	switch(updateCode) {
    	case 0:
    		String currentProgressText = getFormattedFileSize((long) currentDownloadedSize)
    								   + " of " 
    								   + getFormattedFileSize((long) fileSize);
    		
    		PinGMusicSongsService.mBuilder.setTicker(null);
    		PinGMusicSongsService.mBuilder.setProgress(fileSize, currentDownloadedSize, false);
    		PinGMusicSongsService.mBuilder.setContentText(currentProgressText);
    		PinGMusicSongsService.mNotifyManager.notify(PinGMusicSongsService.notificationID, PinGMusicSongsService.mBuilder.build());
    		break;
    	case 1:
    		if (i < songTitlesList.size()) {

			 	//Create the notification that displays the download progress of the song.
			 	String title = mContext.getResources().getString(R.string.downloading_no_dot) + " " + songTitlesList.get(i);
			
			 	PinGMusicSongsService.mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			 	PinGMusicSongsService.mBuilder = new NotificationCompat.Builder(mContext);
			 	PinGMusicSongsService.mBuilder.setContentTitle(title);
			 	PinGMusicSongsService.mBuilder.setContentText(mContext.getResources().getString(R.string.starting_download));
			 	PinGMusicSongsService.mBuilder.setSmallIcon(R.drawable.pin_light);
			 	PinGMusicSongsService.mBuilder.setProgress(0, 0, true);
			 	PinGMusicSongsService.mNotifyManager.notify(PinGMusicSongsService.notificationID, PinGMusicSongsService.mBuilder.build());
    		}
    		break;
    	case 2:
    		Toast.makeText(mContext, R.string.gmusic_app_not_installed_pin, Toast.LENGTH_SHORT).show();
    		break;
    	}

    }
    
    public static String getFormattedFileSize(final long value) {
    	
    	final long[] dividers = new long[] { teraBytes, gigaBytes, megaBytes, kiloBytes, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "bytes" };
        
        if(value < 1) {
        	return "";
        }
        
        String result = null;
        for(int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if(value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
            
        }
        
        return result;
    }
    
    private static String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);	
		PinGMusicSongsService.mBuilder.setProgress(0, 0, false);
		PinGMusicSongsService.mBuilder.setContentText("");
		PinGMusicSongsService.mBuilder.setContentTitle(mContext.getResources().getString(R.string.done_pinning_songs));
		PinGMusicSongsService.mNotifyManager.notify(PinGMusicSongsService.notificationID, PinGMusicSongsService.mBuilder.build());
		
		//Notify the user.
		if (mApp.isFetchingPinnedSongs()==true) {
			Toast.makeText(mContext, R.string.done_pinning_songs_from_gmusic, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, R.string.done_pinning_songs, Toast.LENGTH_LONG).show();
		}
		
		songTitlesList.clear();
		songTitlesList = null;
		songIdsList.clear();
		songIdsList = null;

		mApp.setIsFetchingPinnedSongs(false);
		
		//Stop the service.
		mContext.stopService(new Intent(mContext, PinGMusicSongsService.class));
		
	}

}
