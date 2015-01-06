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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Services.AutoFetchAlbumArtService;
import com.aniruddhc.acemusic.player.Utils.Common;

/*********************************************************************
 * This class is different from AsyncGetAlbumArtTask. It includes an 
 * additional search functionality that scans the ENTIRE library,
 * checks for missing art, and then downloads them.
 *********************************************************************/
public class AsyncAutoGetAlbumArtTask extends AsyncTask<String, String, Void> {
	
    private Context mContext;
    private Common mApp;
    private Activity mActivity;
    private SharedPreferences sharedPreferences;
    private AsyncTask<String, String, Void> task;
    private String artworkURL;
    private Bitmap artworkBitmap;
    private byte[] buffer;
    private AudioFile audioFile;
    private File file;
    private DBAccessHelper dbHelper;
    
    private ProgressDialog pd;
    private int currentProgress = 0;
    private boolean DIALOG_VISIBLE = true;
    
	public static ArrayList<String> dataURIsList = new ArrayList<String>();
	public static ArrayList<String> artistsList = new ArrayList<String>();
	public static ArrayList<String> albumsList = new ArrayList<String>();
    
    public AsyncAutoGetAlbumArtTask(Context context, Activity activity) {
    	mContext = context;
        mApp = (Common) context.getApplicationContext();
    	mActivity = activity;
    	sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    	task = this;
    	dbHelper = new DBAccessHelper(mContext);
    }
    
    public void onPreExecute() {
    	super.onPreExecute();
    	
    	pd = new ProgressDialog(mActivity);
    	pd.setTitle(R.string.downloading_album_art);
    	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	pd.setCancelable(false);
    	pd.setCanceledOnTouchOutside(false);
    	pd.setMessage(mContext.getResources().getString(R.string.scanning_for_missing_art));
    	pd.setButton(DialogInterface.BUTTON_NEGATIVE, 
    				 mContext.getResources().getString(R.string.cancel),
    				 new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							task.cancel(true);
							
						}
    		
    	});
    	
    	pd.show();	
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {

    	//First, we'll go through all the songs in the music library DB and get their attributes.
    	dbHelper = new DBAccessHelper(mContext);
    	String selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
    	String[] projection = { DBAccessHelper._ID, 
    							DBAccessHelper.SONG_FILE_PATH,
    							DBAccessHelper.SONG_ALBUM,
    							DBAccessHelper.SONG_ARTIST, 
    							DBAccessHelper.SONG_TITLE };
    	
    	Cursor cursor = dbHelper.getWritableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
				    										 projection, 
				    										 selection, 
				    										 null, 
				    										 null, 
				    										 null, 
				    										 null);
    	
    	if (cursor.getCount()!=0) {
    		
    		cursor.moveToFirst();
    		dataURIsList.add(cursor.getString(1));
    		albumsList.add(cursor.getString(2));
    		artistsList.add(cursor.getString(3));
    		
    		while(cursor.moveToNext()) {
    			
    			dataURIsList.add(cursor.getString(1));
        		albumsList.add(cursor.getString(2));
        		artistsList.add(cursor.getString(3));
    		}
    		
    	} else {
    		//The user doesn't have any music so let's get outta here.
    		return null;
    	}
    	
    	pd.setMax(dataURIsList.size());
    	    	
    	//Now that we have the attributes of the songs, we'll go through them each and check for missing covers.
    	for (int i=0; i < dataURIsList.size(); i++) {
    		
    		try {
    			file = new File(dataURIsList.get(i));
    		} catch (Exception e) {
    			continue;
    		}

    		audioFile = null;
			try {
				audioFile = AudioFileIO.read(file);
			} catch (CannotReadException e2) {
				// TODO Auto-generated catch block
				continue;
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				continue;
			} catch (TagException e2) {
				// TODO Auto-generated catch block
				continue;
			} catch (ReadOnlyFileException e2) {
				// TODO Auto-generated catch block
				continue;
			} catch (InvalidAudioFrameException e2) {
				// TODO Auto-generated catch block
				continue;
			}
			
    		Tag tag = audioFile.getTag();
    		
	        //Set the destination directory for the xml file.
	        File SDCardRoot = Environment.getExternalStorageDirectory();
	        File xmlFile = new File(SDCardRoot,"albumArt.xml");
    		
	        if (tag!=null) {
	        	
    			String title = tag.getFirst(FieldKey.TITLE);
        		String checkingMessage = mContext.getResources().getString(R.string.checking_if)
        							   + " " 
        							   + title 
        							   + " " 
        							   + mContext.getResources().getString(R.string.has_album_art)
        							   + ".";
        		
        		currentProgress = currentProgress + 1;
	    		String[] checkingProgressParams = { checkingMessage, "" + currentProgress };
	    		publishProgress(checkingProgressParams);
	        	
	        	List<Artwork> artworkList = tag.getArtworkList();
	        	
	    		if (artworkList.size()==0) {

	    			//Since the file doesn't have any album artwork, we'll have to download it.
	    			//Get the artist and album name of the file we're working with.
	    			String artist = tag.getFirst(FieldKey.ARTIST);
	    			String album = tag.getFirst(FieldKey.ALBUM);
	    			
	    			//Update the progress dialog.
	    			String message = mContext.getResources().getString(R.string.downloading_artwork_for) + " " + title;
	    			String[] progressParams = { message, "" + currentProgress };
	    			publishProgress(progressParams);
	    			
	    			//Remove any unacceptable characters.
	    			if (artist.contains("#")) {
	    				artist = artist.replace("#", "");
	    			}
	    			
	    			if (artist.contains("$")) {
	    				artist = artist.replace("$", "");
	    			}
	    			
	    			if (artist.contains("@")) {
	    				artist = artist.replace("@", "");
	    			}
	    			
	    			if (album.contains("#")) {
	    				album = album.replace("#", "");
	    			}
	    			
	    			if (album.contains("$")) {
	    				album = album.replace("$", "");
	    			}
	    			
	    			if (album.contains("@")) {
	    				album = album.replace("@", "");
	    			}
	    			
	    			//Replace any spaces in the artist and album fields with "%20".
	    			if (artist.contains(" ")) {
	    				artist = artist.replace(" ", "%20");
	    			}
	    			
	    			if (album.contains(" ")) {
	    				album = album.replace(" ", "%20");
	    			}
	    			
	    	    	//Construct the url for the HTTP request.
	    	    	URL url = null;
	    			try {
	    				url = new URL("http://itunes.apple.com/search?term=" + artist + "+" + album + "&entity=album");
	    			} catch (MalformedURLException e1) {
	    				// TODO Auto-generated catch block
	    				continue;
	    			}
	    
	    			String xml = null;
	    	    	try {
	    	    		
	    		    	//Create a new HTTP connection.
	    		        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	    		
	    		        urlConnection.connect();
	    		        
	    		        //Check if albumArt.xml already exists and delete it.
	    		        if (xmlFile.exists()) {
	    		        	xmlFile.delete();
	    		        }
	    		        
	    		        //Create the OuputStream that will be used to store the downloaded data into the file.
	    		        FileOutputStream fileOutput = new FileOutputStream(xmlFile);
	    		
	    		        //Create the InputStream that will read the data from the HTTP connection.
	    		        InputStream inputStream = urlConnection.getInputStream();
	    		        
	    		        //Total size of target file.
	    		        int totalSize = urlConnection.getContentLength();
	    		
	    		        //Temp variable that stores the number of downloaded bytes.
	    		        int downloadedSize = 0;
	    		
	    		        //Create a buffer to store the downloaded bytes.
	    		        buffer = new byte[1024];
	    		        int bufferLength = 0;

	    		        //Now read through the buffer and write the contents to the file.
	    		        while((bufferLength = inputStream.read(buffer)) > 0 ) {
	    		            fileOutput.write(buffer, 0, bufferLength);
	    		            downloadedSize += bufferLength;
	    		
	    		        }
	    		        
	    		        //Close the File Output Stream.
	    		        fileOutput.close();

	    	    	} catch (MalformedURLException e) {
	    	    		//TODO Auto-generated method stub
	    	    		continue;
	    	    	} catch (IOException e) {
	    	    		// TODO Auto-generated method stub
	    	    		continue;
	    	    	}
	    	    	
	    	    	//Load the XML file into a String variable for local use.
	    	    	String xmlAsString = null;
	    			try {
	    				xmlAsString = FileUtils.readFileToString(xmlFile);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    			
	    			//Extract the albumArt parameter from the XML file.
	    			artworkURL = StringUtils.substringBetween(xmlAsString, "\"artworkUrl100\":\"", "\",");
	    	    	
	    			if (artworkURL==null) {
	    				
	    				//Check and see if a lower resolution image available.
	    				artworkURL = StringUtils.substringBetween(xmlAsString, "\"artworkUrl60\":\"", "\",");
	    				
	    				if (artworkURL==null) {
	    					//Can't do anything about that here.
	    				} else {
	    					//Replace "100x100" with "600x600" to retrieve larger album art images.
	    					artworkURL = artworkURL.replace("100x100", "600x600");
	    				}
	    		    	
	    			} else {
	    				//Replace "100x100" with "600x600" to retrieve larger album art images.
	    				artworkURL = artworkURL.replace("100x100", "600x600");
	    			}
	    			
	    			//If no URL has been found, there's no point in continuing.
	    			if (artworkURL!=null) {
	    				
	        			artworkBitmap = null;
	        			artworkBitmap = mApp.getImageLoader().loadImageSync(artworkURL);
	        			
	    	    		File artworkFile = new File(Environment.getExternalStorageDirectory() + "/artwork.jpg");
	        			
	        	    	//Save the artwork.
	        	    	try {
	        	    		
	        	    		FileOutputStream out = new FileOutputStream(artworkFile);
	     	    	       	artworkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	    					
	        	    	} catch (Exception e) {
	        	    		e.printStackTrace();
	        	    	} finally {
	        	    		
	        	    		Artwork artwork = null;
	    					try {
	    						artwork = ArtworkFactory.createArtworkFromFile(artworkFile);
	    					} catch (IOException e) {
	    						// TODO Auto-generated catch block
	    						setArtworkAsFile(artworkFile, dataURIsList.get(i));
								continue;
	    					} catch (ArrayIndexOutOfBoundsException e) {
	    						// TODO Auto-generated catch block
	    						setArtworkAsFile(artworkFile, dataURIsList.get(i));
								continue;
	    					} catch (Exception e) {
	    						e.printStackTrace();
	    						setArtworkAsFile(artworkFile, dataURIsList.get(i));
								continue;
	    					} catch (Error e) {
								e.printStackTrace();
								setArtworkAsFile(artworkFile, dataURIsList.get(i));
								continue;
							}
	    					
	    					if (artwork!=null) {
	    						
	        		    		try {
	        						//Remove the current artwork field and recreate it.
	        		    			tag.deleteArtworkField();
	        		    			tag.addField(artwork);
	        					} catch (Exception e) {
	        						// TODO Auto-generated catch block
	        						setArtworkAsFile(artworkFile, dataURIsList.get(i));
									continue;
	        					} catch (Error e) {
	    							e.printStackTrace();
	    							setArtworkAsFile(artworkFile, dataURIsList.get(i));
	    							continue;
	    						}
	        		    		
	        		    		try {
	        						audioFile.commit();
	        					} catch (CannotWriteException e) {
	        						// TODO Auto-generated catch block
	        						setArtworkAsFile(artworkFile, dataURIsList.get(i));
									continue;
	        					} catch (Error e) {
	    							e.printStackTrace();
	    							setArtworkAsFile(artworkFile, dataURIsList.get(i));
	    							continue;
	    						}
	    						
	    					}
	    					
	            	    	//Delete the temporary files that we stored during the fetching process.
	    		    		if (artworkFile.exists()) {
	    		    			artworkFile.delete();
	    		    		}
	    		    		
	    		    		if (xmlFile.exists()) {
	    		    			xmlFile.delete();
	    		    		}
	    	    	    	
	    		    		//Set the files to null to help clean up memory.
	    		    		artworkBitmap = null;
	    		    		audioFile = null;
	    		    		tag = null;
	    		    		xmlFile = null;
	    		    		artworkFile = null;
	    		    		
	    	    		}

	    			}

	    		}
	        	
	        }
    		
    	}
    	
    	audioFile = null;
    	file = null;
    	//System.gc();
    	
    	return null;
	    
    }
    
  //Saves the artwork as a JPEG file in the song's parent folder.
    public void setArtworkAsFile(File artworkFile, String songFilePath) {

    	File songFile = new File(songFilePath);
    	String songTitle = songFile.getName();
    	int lastDotSlash = songTitle.lastIndexOf(".");
    	String albumArtFileName = songTitle.substring(0, lastDotSlash);
    	
    	if (songFile.exists()) {
    		int lastSlashIndex = songFilePath.lastIndexOf("/");
    		String folderPath = songFilePath.substring(0, lastSlashIndex);
    		File destFile = new File(folderPath + "/" + albumArtFileName + ".jpg");
    		
    		try {
				FileUtils.copyFile(artworkFile, destFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
    		
    		//Update the album art tag in Jams' database.
			ContentValues values = new ContentValues();
			songFilePath = songFilePath.replace("'", "''");
			String where = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + songFilePath + "'";
			values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, folderPath + "/" + albumArtFileName + ".jpg");
			dbHelper.getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
    		
    	}

    }
    
    @Override
    public void onProgressUpdate(String... values) {
    	super.onProgressUpdate(values);
    	
    	if (DIALOG_VISIBLE==true) {
    		pd.setProgress(Integer.parseInt(values[1]));
    		pd.setMessage(values[0]);
    	}
    	
    	//Update the notification.
    	AutoFetchAlbumArtService.builder.setContentTitle(mContext.getResources().getString(R.string.downloading_missing_cover_art));
    	AutoFetchAlbumArtService.builder.setSmallIcon(R.drawable.notif_icon);
    	AutoFetchAlbumArtService.builder.setContentInfo(null);
    	AutoFetchAlbumArtService.builder.setContentText(null);
    	AutoFetchAlbumArtService.builder.setProgress(dataURIsList.size(), currentProgress, false);
    	AutoFetchAlbumArtService.notification = AutoFetchAlbumArtService.builder.build();

    	NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    	notifyManager.notify(AutoFetchAlbumArtService.NOTIFICATION_ID, AutoFetchAlbumArtService.notification);
    	
    }

    @Override
    protected void onPostExecute(Void arg0) {
		
    	Intent intent = new Intent(mContext, AutoFetchAlbumArtService.class);
    	mContext.stopService(intent);
    	
    	if (pd.isShowing() && DIALOG_VISIBLE==true) {
    		pd.dismiss();
    	}
    	
    	//Dismiss the notification.
    	AutoFetchAlbumArtService.builder.setTicker(mContext.getResources().getString(R.string.done_downloading_art));
    	AutoFetchAlbumArtService.builder.setContentTitle(mContext.getResources().getString(R.string.done_downloading_art));
    	AutoFetchAlbumArtService.builder.setSmallIcon(R.drawable.notif_icon);
    	AutoFetchAlbumArtService.builder.setContentInfo(null);
    	AutoFetchAlbumArtService.builder.setContentText(null);
    	AutoFetchAlbumArtService.builder.setProgress(0, 0, false);
    	AutoFetchAlbumArtService.notification = AutoFetchAlbumArtService.builder.build();
    	AutoFetchAlbumArtService.notification.flags = Notification.FLAG_AUTO_CANCEL;

    	NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    	notifyManager.notify(AutoFetchAlbumArtService.NOTIFICATION_ID, AutoFetchAlbumArtService.notification);
    	
    	Toast.makeText(mContext, R.string.done_downloading_art, Toast.LENGTH_LONG).show();
    	
    	//Rescan for album art.
		//Seting the "RESCAN_ALBUM_ART" flag to true will force MainActivity to rescan the folders.
		sharedPreferences.edit().putBoolean("RESCAN_ALBUM_ART", true).commit();
		
		//Restart the app.
		final Intent i = mActivity.getBaseContext()
				                  .getPackageManager()
				                  .getLaunchIntentForPackage(mActivity.getBaseContext().getPackageName());
		
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mActivity.startActivity(i);
		mActivity.finish();
    	
    }

}
