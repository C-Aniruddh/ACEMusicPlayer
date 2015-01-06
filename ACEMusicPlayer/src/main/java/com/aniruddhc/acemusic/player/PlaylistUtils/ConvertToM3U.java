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

import java.io.File;

/*******************************************************
 * This helper class utilizes the Lizzy library to 
 * convert an M3U playlist file to another file format. 
 * 
 * @author Saravan Pantham
 *******************************************************/
public class ConvertToM3U {

	//Playlist parameters
	private File mPlaylistFile;
	private String mOutputFilePath;
	
	//Success/Error codes.
	public static String SUCCESS = "Playlist converted.";
	public static String INPUT_FILE_IO_EXCEPTION = "The playlist file could not be read.";
	public static String INPUT_FILE_INVALID = "The playlist file is invalid or corrupt.";
	public static String PLAYLIST_COULD_NOT_BE_CONVERTED = "The playlist could not be converted.";
	public static String PLAYLIST_COULD_NOT_BE_SAVED = "The converted playlist could not be saved.";
	
	public ConvertToM3U(File playlistFile, String outputFilePath) {
		mPlaylistFile = playlistFile;
		mOutputFilePath = outputFilePath;
	}
	
	/* Runs the actual conversion process. Returns "SUCCESS" if the 
	 * operation succeeded. Returns an error code otherwise. */
	public String convertPlaylistFile() {
		
		return SUCCESS;
	}
	
}
