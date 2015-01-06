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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class AsyncGetAlbumArtTask extends AsyncTask<String, Void, Integer> {
    private Context mContext;
    private Common mApp;
    
    private String artist = "";
    private String album = "";
    private String urlArtist = "";
    private String urlAlbum = "";
    private String artworkURL;
    
    private File file;
    private Bitmap artworkBitmap;
    private Boolean URL_RETRIEVED = false;
    
	public static ArrayList<String> dataURIsList = new ArrayList<String>();
    
    public AsyncGetAlbumArtTask(Context context, View viewItem, int imageID) {
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	
    }
    
    @Override
    protected void onPreExecute() {
    	Toast.makeText(mContext, R.string.getting_album_art_toast, Toast.LENGTH_SHORT).show();
    	
    }
 
    @Override
    protected Integer doInBackground(String... params) {
    	/************************************************************************************************
    	 * RETRIEVE THE HTTP SEARCH RESPONSE FROM ITUNES SERVERS.
    	 ************************************************************************************************/
    	
    	//First, we'll make a HTTP request to iTunes' servers with the album and artist name.
    	if (params.length==2) {
        	artist = params[0];
        	album = params[1];

        	//Create duplicate strings that will be filtered out for the URL.
        	urlArtist = artist;
        	urlAlbum = album;
        	
        	//Remove any unacceptable characters.
			if (urlArtist.contains("#")) {
				urlArtist = urlArtist.replace("#", "");
			}
			
			if (urlArtist.contains("$")) {
				urlArtist = urlArtist.replace("$", "");
			}
			
			if (urlArtist.contains("@")) {
				urlArtist = urlArtist.replace("@", "");
			}
			
			if (urlAlbum.contains("#")) {
				urlAlbum = urlAlbum.replace("#", "");
			}
			
			if (urlAlbum.contains("$")) {
				urlAlbum = urlAlbum.replace("$", "");
			}
			
			if (urlAlbum.contains("@")) {
				urlAlbum = urlAlbum.replace("@", "");
			}
			
			//Replace any spaces in the artist and album fields with "%20".
			if (urlArtist.contains(" ")) {
				urlArtist = urlArtist.replace(" ", "%20");
			}
			
			if (urlAlbum.contains(" ")) {
				urlAlbum = urlAlbum.replace(" ", "%20");
			}
        	
    	}
    	
    	//Construct the url for the HTTP request.
    	URL uri = null;
		try {
			uri = new URL("http://itunes.apple.com/search?term=" + urlArtist + "+" + urlAlbum + "&entity=album");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return 1;
		}
    	
    	try {
	    	//Create a new HTTP connection.
	        HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
	        urlConnection.connect();
	
	        //Set the destination directory for the xml file.
	        File SDCardRoot = Environment.getExternalStorageDirectory();
	        file = new File(SDCardRoot, "albumArt.xml");
	
	        //Create the OuputStream that will be used to store the downloaded data into the file.
	        FileOutputStream fileOutput = new FileOutputStream(file);
	
	        //Create the InputStream that will read the data from the HTTP connection.
	        InputStream inputStream = urlConnection.getInputStream();
	        
	        //Total size of target file.
	        int totalSize = urlConnection.getContentLength();
	
	        //Temp variable that stores the number of downloaded bytes.
	        int downloadedSize = 0;
	
	        //Create a buffer to store the downloaded bytes.
	        byte[] buffer = new byte[1024];
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
    		e.printStackTrace();
    		return 1;
    	} catch (IOException e) {
    		// TODO Auto-generated method stub
    		e.printStackTrace();
    		return 1;
    	}
    	
    	
    	//Create a File object that points to the downloaded file.
    	File phpSource = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/albumArt.xml");
    	String phpAsString = null;
		try {
			phpAsString = FileUtils.readFileToString(phpSource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 1;
		}
		
		//Extract the albumArt parameter from the PHP response.
		artworkURL = StringUtils.substringBetween(phpAsString, "\"artworkUrl100\":\"", "\",");
		if (artworkURL==null) {
			
			//Check and see if a lower resolution image available.
			artworkURL = StringUtils.substringBetween(phpAsString, "\"artworkUrl60\":\"", "\",");
			
			if (artworkURL==null) {
				URL_RETRIEVED = false;
				return 1;
			} else {
				//Replace "100x100" with "600x600" to retrieve larger album art images.
				artworkURL = artworkURL.replace("100x100", "600x600");
				URL_RETRIEVED = true;
			}
	    	
		} else {
			//Replace "100x100" with "600x600" to retrieve larger album art images.
			artworkURL = artworkURL.replace("100x100", "600x600");
			URL_RETRIEVED = true;
		}
		
		//Loop through the songs table and retrieve the data paths of all the songs (used to embed the artwork).

		//Replace any rogue apostrophes.
		if (album.contains("'")) {
			album = album.replace("'", "''");
		}
		
		if (artist.contains("'")) {
			artist = artist.replace("'", "''");
		}
		
		String selection = DBAccessHelper.SONG_ALBUM + "=" + "'" + album + "'" + " AND "
						 + DBAccessHelper.SONG_ARTIST + "=" + "'" + artist + "'";
		
		String[] projection = { DBAccessHelper._ID, 
								DBAccessHelper.SONG_FILE_PATH };
		
		Cursor cursor = mApp.getDBAccessHelper().getWritableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
											 								 projection, 
					 														 selection, 
					 														 null, 
					 														 null, 
					 														 null, 
					 														 null);
		
		if (cursor.getCount()!=0) {
			cursor.moveToFirst();
			dataURIsList.add(cursor.getString(1));
			
			while(cursor.moveToNext()) {
				dataURIsList.add(cursor.getString(1));
			}
			
		}
		
		cursor.close();
		
		if (URL_RETRIEVED==true) {
    		artworkBitmap = mApp.getImageLoader().loadImageSync(artworkURL);

	    	File artworkFile = new File(Environment.getExternalStorageDirectory() + "/artwork.jpg");
    		
	    	//Display the album art on the grid/listview so that the user knows that the download is complete.
	    	publishProgress();
	    	
	    	//Save the artwork.
	    	try {
	    		FileOutputStream out = new FileOutputStream(artworkFile);
	    	    artworkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	    	    out.flush();
	    	    out.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		return 1;
	    	} finally {
		    	
	    		for (int i=0; i < dataURIsList.size(); i++) {
	    	       	
	    			if (dataURIsList.get(i)!=null) {
	    				
	    				File audioFile = new File(dataURIsList.get(i));
			    		AudioFile f = null;
			    		
						try {
							f = AudioFileIO.read(audioFile);
						} catch (CannotReadException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TagException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ReadOnlyFileException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidAudioFrameException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Tag tag = null;
						try {
							if (f!=null) {
								tag = f.getTag();
							} else {
								continue;
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						} 
			    		
			    		Artwork artwork = null;
						try {
							artwork = ArtworkFactory.createArtworkFromFile(artworkFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
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
								tag.setField(artwork);
							} catch (FieldDataInvalidException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
							
						}

			    		try {
							f.commit();
						} catch (CannotWriteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							setArtworkAsFile(artworkFile, dataURIsList.get(i));
							continue;
						} catch (Error e) {
							e.printStackTrace();
							setArtworkAsFile(artworkFile, dataURIsList.get(i));
							continue;
						}
			    		
			    		//Update the album art tag in Jams' database.
		    			ContentValues values = new ContentValues();
		    			String filePath = dataURIsList.get(i);
		    			filePath = filePath.replace("'", "''");
		    			String where = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + filePath + "'";
		    			values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, "byte://" + dataURIsList.get(i));
		    			
		    			mApp.getDBAccessHelper()
		    				.getWritableDatabase()
		    				.update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
			    		
	    			} else {
	    				continue;
	    			}
	    			
	    		}
	    		
	    		//Refresh the memory/disk cache for the ImageLoader instance.
	    		try {
		    		mApp.getImageLoader().clearMemoryCache();
		    		mApp.getImageLoader().clearDiscCache();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		
	    		//Delete the temporary files once the artwork has been embedded.
	    		artworkFile.delete();
	    		file.delete();
	    		
	    	}
	    	
    	}
		
    	return 0;
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
			
			mApp.getDBAccessHelper()
				.getWritableDatabase()
				.update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
    		
    	}

    }
    
    @Override
    protected void onProgressUpdate(Void... v) {
    	
    	if (URL_RETRIEVED==false) {
        	Toast.makeText(mContext, R.string.album_art_not_found, Toast.LENGTH_SHORT).show();
    	} else {
    		Toast.makeText(mContext, R.string.album_art_downloaded, Toast.LENGTH_SHORT).show();
    	}
    	
    }

    @Override
    protected void onPostExecute(Integer result) {
	    dataURIsList.clear();

	    if (result==0) {
	    	//Update the UI.
	    	mApp.broadcastUpdateUICommand(new String[] { }, new String[] { });
		    Toast.makeText(mContext, R.string.album_art_downloaded, Toast.LENGTH_SHORT).show();
	    } else {
	    	Toast.makeText(mContext, R.string.unable_to_get_album_art, Toast.LENGTH_SHORT).show();
	    }
	    
	}

}
