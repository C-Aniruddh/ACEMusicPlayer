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
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.FoldersFragment.FileExtensionFilter;
import com.aniruddhc.acemusic.player.NowPlayingActivity.NowPlayingActivity;

public class AsyncPlayFolderRecursiveTask extends AsyncTask<String, Void, Void> {
    private Activity mContext;
    private String mFolderName;
    private ProgressDialog pd;
	boolean dialogVisible = true;
	
	private ArrayList<String> audioFilePathsInFolder = new ArrayList<String>();
	private ArrayList<String> subdirectoriesList = new ArrayList<String>();
	private ArrayList<Object> metadata = new ArrayList<Object>();
    
    public AsyncPlayFolderRecursiveTask(Activity context, String folderName) {
    	mContext = context;
    	mFolderName = folderName;
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		pd.setIndeterminate(false);
		pd.setTitle(R.string.play_folder_recursive);
		pd.setButton(DialogInterface.BUTTON_NEUTRAL, mContext.getResources()
															 .getString(R.string.cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				pd.dismiss();
				
			}
			
		});
		
		pd.show();
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {
    	String folderPath = params[0];
    	getAudioFilePathsInFolder(folderPath);
		
		//Get the list of subdirectories and iterate through them for audio files.
		iterateThruFolder(folderPath);
		
		for (int i=0; i < subdirectoriesList.size(); i++) {
			getAudioFilePathsInFolder(subdirectoriesList.get(i));
		}
		
		//Extract the metadata from the first audio file (if any).
		if (audioFilePathsInFolder!=null && audioFilePathsInFolder.size() > 0) {
			metadata = extractFileMetadata(audioFilePathsInFolder.get(0));
		}
    	
    	return null;
	    
    }
    
    //Stores an ArrayList of all the audio files' paths within the specified folder.
    public void getAudioFilePathsInFolder(String folderPath) {
    	
    	//We'll use a filter to retrieve a list of all files with a matching extension.
    	File file = new File(folderPath);
    	FileExtensionFilter AUDIO_FILES_FILTER = new FileExtensionFilter(new String[] {".mp3", ".3gp", ".mp4",
    																				   ".m4a", ".aac", ".ts", 
    																				   ".flac", ".mid", ".xmf", 
    																				   ".mxmf", ".midi", ".rtttl", 
    																				   ".rtx", ".ota", ".imy", ".ogg", 
    																				   ".mkv", ".wav" });
    	
    	File[] filesInFolder = file.listFiles(AUDIO_FILES_FILTER);
    	
    	//Loop through the list of files and add their file paths to the corresponding ArrayList.
    	for (int i=0; i < filesInFolder.length; i++) {
    		
    		try {
				audioFilePathsInFolder.add(filesInFolder[i].getCanonicalPath());
			} catch (IOException e) {
				//Skip any corrupt audilo files.
				continue;
			}
    		
    	}
    	
    }
    
    /* This method goes through a folder recursively and saves all its
     * subdirectories to an ArrayList (subdirectoriesList). */
    public void iterateThruFolder(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list==null) {
        	return;
        }

        for (File f : list) {
        	
        	mFolderName = f.getName();
        	publishProgress();
        	
            if (f.isDirectory()) {
                iterateThruFolder(f.getAbsolutePath());
                
                if (!subdirectoriesList.contains(f.getPath())) {
                	subdirectoriesList.add(f.getPath());
                }
                    
            }
            
        }
        
    }
    
    @Override
    protected void onProgressUpdate(Void... v) {

		//Update the progress on the progress dialog.
		pd.setMessage(mContext.getResources().getString(R.string.scanning_for_files) + " " + mFolderName);
    	 
    }
    
    //Extracts specific ID3 metadata from an audio file and returns them in an ArrayList.
    public static ArrayList<Object> extractFileMetadata(String filePath) {
    	ArrayList<Object> metadata = new ArrayList<Object>();
    	
    	MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    	mediaMetadataRetriever.setDataSource(filePath);
    	
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    	metadata.add(mediaMetadataRetriever.getEmbeddedPicture());
    	
    	return metadata;
    	
    }
    
    //Call the player activity once we've accumulated the first song's path.
    
    

    @Override
    protected void onPostExecute(Void arg0) {
    	
    	/* Now that we have a list of audio files within the folder, pass them
    	 * on to NowPlayingActivity (which will assemble the files into a cursor for the service. */
		
    	//Check if the list is empty. If it is, show a Toast message to the user.
    	if (audioFilePathsInFolder.size() > 0) {
    		
    		//Check if the audio file has a title. If not, use the file name.
    		String title = "";
    		if (metadata.get(0)==null) {
    			title = audioFilePathsInFolder.get(0);
    		} else {
    			title = (String) metadata.get(0);
    		}
    		
    		Intent intent = new Intent(mContext, NowPlayingActivity.class);
    		intent.putExtra("DURATION", (String) metadata.get(3));
    		intent.putExtra("SONG_NAME", title);
    		intent.putExtra("NUMBER_SONGS", 1);
    		
    		if (metadata.get(1)==null) {
    			intent.putExtra("ARTIST", "Unknown Artist");
    		} else {
    			intent.putExtra("ARTIST", (String) metadata.get(1));
    		}
    		
    		if (metadata.get(2)==null) {
    			intent.putExtra("ALBUM", "Unknown Album");
    		} else {
    			intent.putExtra("ALBUM", (String) metadata.get(2));
    		}
    		
    		if (metadata.get(3)==null) {
    			intent.putExtra("SELECTED_SONG_DURATION", 0);
    		} else {
    			intent.putExtra("SELECTED_SONG_DURATION", (String) metadata.get(3));
    		}
    		
    		intent.putExtra("DATA_URI", audioFilePathsInFolder.get(0));
    		
    		if (metadata.get(4)==null) {
    			intent.putExtra("EMBEDDED_ART", (byte[]) null);
    		} else {
    			intent.putExtra("EMBEDDED_ART", (byte[]) metadata.get(4));
    		}

    		intent.putExtra("NEW_PLAYLIST", true);
    		intent.putExtra("CALLED_FROM_FOOTER", false);
    		intent.putExtra("CALLED_FROM_FOLDERS", true);
    		intent.putExtra("CALLING_FRAGMENT", "FOLDERS_FRAGMENT");
    		
    		//We're dealing with the first audio file in the list, so just use zero for SONG_SELECTED_INDEX.
    		intent.putExtra("SONG_SELECTED_INDEX", 0);
    		
    		//Pass on the list of file paths to NowPlayingActivity (which will assemble them into a cursor).
    		intent.putStringArrayListExtra("FOLDER_AUDIO_FILE_PATHS", audioFilePathsInFolder);
    		
    		pd.dismiss();
    		
    		Log.e("DEBUG", ">>>>>>>>>>>>TIME TO START THE ACTIVITY");
    		mContext.startActivity(intent);
    		mContext.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    		
    	} else {
    		pd.dismiss();
    		Toast.makeText(mContext, R.string.no_audio_files_found, Toast.LENGTH_LONG).show();
    	}
    	
    	mContext = null;
		
	}

}
