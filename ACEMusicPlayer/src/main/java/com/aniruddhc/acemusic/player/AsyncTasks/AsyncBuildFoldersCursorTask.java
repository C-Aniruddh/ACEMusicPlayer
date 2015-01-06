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
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;

import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

/*************************************************************
 * Builds an initial MatrixCursor with 5 entries/songs. This 
 * initial cursor is then returned to the service for playback. 
 * This AsyncTask then continues to add rows to the MatrixCursor 
 * and periodically updates the service with the new cursor.
 * This will allow NowPlayingActivity to load as quick as possible 
 * and build the MatrixCursor in the background.
 * 
 * @author Saravan Pantham
 *************************************************************/
public class AsyncBuildFoldersCursorTask extends AsyncTask<String, Integer, Boolean> {

	private Context mContext;
	private Common mApp;
	private ArrayList<String> mSongFilePathsList = new ArrayList<String>();
	
	public AsyncBuildFoldersCursorTask(Context context, ArrayList<String> songFilePathsList) {
		mContext = context;
		mApp = (Common) mContext;
		mSongFilePathsList = songFilePathsList;
	}
	
	@Override
	public void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		
		//We'll create a matrix cursor that includes all the audio files within the specified folder.
		String[] foldersCursorColumns = { DBAccessHelper.SONG_ARTIST,  
										  DBAccessHelper.SONG_ALBUM, 
										  DBAccessHelper.SONG_TITLE, 
										  DBAccessHelper.SONG_FILE_PATH,
										  DBAccessHelper.SONG_DURATION, 
										  DBAccessHelper.SONG_GENRE, 
										  DBAccessHelper.SONG_SOURCE, 
										  DBAccessHelper.SONG_ALBUM_ART_PATH, 
										  DBAccessHelper.SONG_ID, 
										  DBAccessHelper.LOCAL_COPY_PATH };
					    		
		MatrixCursor foldersCursor = new MatrixCursor(foldersCursorColumns);
		String artist = "";
		String album = "";
		String title = "";
		String filePath = "";
		String duration = "";
		String genre = "";
		String songSource = "LOCAL_FILE";
		String songAlbumArtPath = "";
		String songId = "";
		
		for (int i=0; i < mSongFilePathsList.size(); i++) {
			
			if (mSongFilePathsList.size() <= 5 && i==5) {
				mApp.getService().setCursor((Cursor) foldersCursor);
			}
			
			/*//Extract metadata from the file, (if it exists).
			try {
				mmdr.setDataSource(mSongFilePathsList.get(i));
			} catch (Exception e) {
				//Skip the audio file if we can't read it.
				continue;
			}
			
			artist = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			if (artist==null || artist.isEmpty()) {
				artist = "Unknown Artist";
			}
			
			album = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			if (album==null || album.isEmpty()) {
				album = "Unknown Album";
			}
			
			title = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			if (title==null || title.isEmpty()) {
				title = mSongFilePathsList.get(i);
			}
			
			filePath = mSongFilePathsList.get(i);
			duration = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (duration==null || duration.isEmpty()) {
				duration = "0";
			}
			
			genre = mmdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
			if (genre==null || genre.isEmpty()) {
				genre = "Unknown Genre";
			}*/
			
			try {
				File file = new File(mSongFilePathsList.get(i));
    			AudioFile audioFile = AudioFileIO.read(file);
    			Tag tag = audioFile.getTag();
    			filePath = mSongFilePathsList.get(i);
    			
    			artist = tag.getFirst(FieldKey.ARTIST);
    			if (artist==null || artist.equals(" ") || artist.isEmpty()) {
    				artist = "Unknown Artist";
    			}
    			
    			album = tag.getFirst(FieldKey.ALBUM);
    			if (album==null || album.equals(" ") || album.isEmpty()) {
    				album = "Unknown Album";
    			}
    			
    			title = tag.getFirst(FieldKey.ARTIST);
    			if (title==null || title.equals(" ") || title.isEmpty()) {
    				title = filePath;
    			}
    			
    			duration = "" + audioFile.getAudioHeader().getTrackLength();
    			if (duration==null || duration.equals(" ") || duration.isEmpty()) {
    				duration = "0";
    			}
    			
    			genre = tag.getFirst(FieldKey.GENRE);
    			if (genre==null || genre.equals(" ") || genre.isEmpty()) {
    				genre = "Unknown Genre";
    			}
    			
    			foldersCursor.addRow(new Object[] { artist,
    												album,
    												title,
    												filePath,
    												duration,
    												genre,
    												songSource,
    												songAlbumArtPath,
    												songId, 
    												"" });
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
		
		//cursor = (Cursor) foldersCursor;
		mApp.getService().setCursor((Cursor) foldersCursor);
		
		return null;
	}
	
	@Override
	public void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		
	}
	
	@Override
	public void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
	}

}
