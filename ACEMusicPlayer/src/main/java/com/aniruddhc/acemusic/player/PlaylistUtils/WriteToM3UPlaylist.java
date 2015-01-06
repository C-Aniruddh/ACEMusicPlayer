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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class WriteToM3UPlaylist {

	private String mPlaylistFolderPath;
	private String mPlaylistName;
	private String mPlaylistElementPath;
	private String mDurationInMs;
	private String mFullFilePath;
	
	public WriteToM3UPlaylist(String playlistFolderPath, 
							  String playlistName, 
							  String playlistElementPath,
							  String durationInMs) {
		
		mPlaylistFolderPath = playlistFolderPath;
		mPlaylistName = playlistName;
		mPlaylistElementPath = playlistElementPath;
		mDurationInMs = durationInMs;
	}
	
	//Saves the specified playlist to the specified file on the filesystem.
	//Returns true if the write operation succeeded. Returns false otherwise.
	public boolean writeToM3UFile() {
		
		BufferedWriter writer = null;
        try {
        	
        	//Check if the playlist target folder exists.
        	File folderPath = new File(mPlaylistFolderPath);
        	if (folderPath.isDirectory()==false) {
        		folderPath.mkdirs();
        	}
        	
            //Create/open the M3U file for writing.
        	mFullFilePath = mPlaylistFolderPath + "/" + mPlaylistName + ".m3u";
        	File file = new File(mFullFilePath);
            
        	//Create the opening M3U header only if the playlist doesn't already exist.
            writer = null;
        	if (file.exists()==false) {
        		writer = new BufferedWriter(new FileWriter(file, false));
        		writer.write("#EXTM3U");
        	} else {
        		writer = new BufferedWriter(new FileWriter(file, true));
        	}
        	
        	//Convert the duration units from millisecs to seconds.
        	long duration = Long.parseLong(mDurationInMs);
        	duration = duration/1000;
        	String durationInSecs = duration + "";
        	
        	//If the playlist element path and the duration are both null, just create an empty playlist.
        	if (durationInSecs!=null && mPlaylistElementPath!=null) {
                writer.newLine();
                writer.write("#EXTINF:" + durationInSecs + ", " + getSongTitle(mPlaylistElementPath));
                writer.newLine();
                writer.write("\"" + getRelativePath(mPlaylistFolderPath, mPlaylistElementPath) + "\"");
        	}
        	
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        	
            try {
                writer.close();
            } catch (Exception e) {
            	//Get rid of the writer object on the next garbage collection run.
            	writer = null;
            }
         
        }
        
        return true;
	}
	
	public String getSongTitle(String songFilePath) {
		
		String songTitle = "Title";
		
		//Try to get the song title from the ID3 tag.
		File songFile = new File(songFilePath);
		AudioFile audioFile = null;
		Tag tag = null;
		try {
			audioFile = AudioFileIO.read(songFile);
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
		
		tag = audioFile.getTag();
		songTitle = tag.getFirst(FieldKey.TITLE);
		
		//If the ID3 tag doesn't give us the info we need, just use the file name.
		if (songTitle.equals("Title") || songTitle.isEmpty()) {
			int indexOfLastSlash = songTitle.lastIndexOf("/")+1;
			int indexOfLastDot = songTitle.lastIndexOf(".");
			songTitle = songTitle.substring(indexOfLastSlash, indexOfLastDot);
		}
		
		return songTitle;
		
	}
	
	//This method will produce a file path for the songs, relative to the playlist location.
	public String getRelativePath(String playlistFilePath, String songFilePath) {
		
		String relativePath = RelativizePaths.convertToRelativePath(playlistFilePath, songFilePath);
		return relativePath;
		
	}
	
}
