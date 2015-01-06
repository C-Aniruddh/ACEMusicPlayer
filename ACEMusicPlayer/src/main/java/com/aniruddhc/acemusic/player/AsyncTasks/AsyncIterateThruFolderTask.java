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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.aniruddhc.acemusic.player.R;

/***********************************************************************************
 * This task goes through the specified parent folder and retrieves the canonical 
 * paths of all the subfolders within that parent folder. If the input parameter is 
 * "ADD", the task adds the paths to an ArrayList. If the input parameter is "REMOVE", 
 * the task removes the paths from the specified ArrayList.
 * 
 * @author Saravan Pantham
 ***********************************************************************************/
public class AsyncIterateThruFolderTask extends AsyncTask<String, String, Void> {
    private Context mContext;
    private ProgressDialog pd;
	private String mParentPath;
	private String mOperation;
	private String mFolderName;
	private ArrayList<String> mTempSelectedFolderPaths = new ArrayList<String>();
	private ArrayList<String> mTempSelectedFolderTimestamps = new ArrayList<String>();
	private boolean mFirstRun;
    
    public AsyncIterateThruFolderTask(Context context, 
    								  boolean firstRun, 
    								  ArrayList<String> tempSelectedFolderPaths, 
    								  ArrayList<String> tempSelectedFolderTimestamps,
    								  String parentPath, 
    								  String operation) {
    	mContext = context;
    	mFirstRun = firstRun;
    	mOperation = operation;
    	mParentPath = parentPath;
    	mTempSelectedFolderPaths = tempSelectedFolderPaths;
    	mTempSelectedFolderTimestamps = tempSelectedFolderTimestamps;
    	
    	//Get the name of the folder that's being iterated through.
    	int index = mParentPath.lastIndexOf("/");
    	mFolderName = mParentPath.substring((index+1), (mParentPath.length()));
    	
    }
    
    protected void onPreExecute() {
    	//Create a String template for the dialog's message body.
    	String message = mContext.getResources().getString(R.string.scanning_subfolders_message) + " " + mFolderName;
    	
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		pd.setIndeterminate(false);
		pd.setTitle(R.string.scanning_subfolders);
		pd.setMessage(message);
		pd.show();
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {
    	iterateThruFolder(mParentPath, mOperation);
		return null;
		
    }
    
    /* This method goes through a folder recursively and gets all the paths 
     * of the directories within that folder. The method accepts two forms
     * for the "operation" argument:
     * "ADD": Adds the specified folder paths to tempSelectedFolderPaths.
     * "REMOVE": Removes the specified folder paths from tempSelectedFolderPaths. 
     */
    public void iterateThruFolder(String path, String operation) {

    	File root = null;
    	File[] list = null;
    	try {
            root = new File(path);
            list = root.listFiles();
    	} catch (Exception e) {
    		e.printStackTrace();
    	} catch (StackOverflowError e2) {
    		//The recursive function call is too damn long. Just quit at this point.
    		return;
    	}
    	
        if (list==null) {
        	return;
        }

        for (File f : list) {
        	
            if (f.isDirectory()) {
            	
            	//Update the progress dialog message.
            	try {
					publishProgress(new String[] { f.getCanonicalPath() });
				} catch (Exception e) {
					//Don't do anything.
				}
            	
            	if (operation.equals("ADD")) {
                    try {
						iterateThruFolder(f.getCanonicalPath(), "ADD");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    
                    try {
                    	String canonicalPath = f.getCanonicalPath();
						if (!mTempSelectedFolderPaths.contains(canonicalPath)) {
							mTempSelectedFolderPaths.add(canonicalPath);
							mTempSelectedFolderTimestamps.add("" + f.lastModified());
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
            	} else if (operation.equals("REMOVE")) {
            		
                    try {
						iterateThruFolder(f.getCanonicalPath(), "REMOVE");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    
                    try {
                    	String canonicalPath = f.getCanonicalPath();
						if (mTempSelectedFolderPaths.contains(canonicalPath)) {
							mTempSelectedFolderPaths.remove(canonicalPath);
							mTempSelectedFolderTimestamps.remove("" + f.lastModified());
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
            	}

            }
            
        }
        
    }
    
    @Override
    public void onProgressUpdate(String... values) {
    	super.onProgressUpdate(values);
    	String currentFolderName = values[0];
    	
    	//Create a String template for the dialog's message body.
    	String message = mContext.getResources().getString(R.string.scanning_subfolders_message) 
    				   + " " 
    				   + mFolderName
    				   + "\n"
    				   + "\n"
    				   + currentFolderName;
    	
    	pd.setMessage(message);
    	
    }

    @Override
	protected void onPostExecute(Void result) {
    	pd.dismiss();
    	
/*    	if (mFirstRun==true) {
    		//Assign the newly populated ArrayLists to the calling fragment/activity.
        	MusicFoldersSelectionFragment.tempSelectedFolderPaths = mTempSelectedFolderPaths;
        	MusicFoldersSelectionFragment.tempSelectedFolderTimestamps = mTempSelectedFolderTimestamps;
    	} else {
    		//Assign the newly populated ArrayLists to the calling fragment/activity.
        	MusicFoldersSelectionDialog.tempSelectedFolderPaths = mTempSelectedFolderPaths;
        	MusicFoldersSelectionDialog.tempSelectedFolderTimestamps = mTempSelectedFolderTimestamps;
    	}*/
		
	}

}
