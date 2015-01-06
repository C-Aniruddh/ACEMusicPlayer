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
package com.aniruddhc.acemusic.player.MusicFoldersSelectionFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class MusicFoldersSelectionFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	private boolean mWelcomeSetup = false;

    private RelativeLayout mUpLayout;
    private ImageView mUpIcon;
    private TextView mUpText;
    private TextView mCurrentFolderText;

	private ListView mFoldersListView;
	private Cursor mCursor;
	
	private String mRootDir;
	private String mCurrentDir;
	
	private List<String> mFileFolderNamesList; 
	private List<String> mFileFolderPathsList;
	private List<String> mFileFolderSizesList;
	private HashMap<String, Boolean> mMusicFolders;
	
	private static boolean CALLED_FROM_WELCOME = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity().getApplicationContext();
		mApp = (Common) mContext;
		View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_folders_selection, null);
		mMusicFolders = new HashMap<String, Boolean>();

        mFoldersListView = (ListView) rootView.findViewById(R.id.folders_list_view);
        mFoldersListView.setFastScrollEnabled(true);
        mWelcomeSetup = getArguments().getBoolean("com.aniruddhc.acemusic.player.WELCOME");

        mUpLayout = (RelativeLayout) rootView.findViewById(R.id.folders_up_layout);
        mUpIcon = (ImageView) rootView.findViewById(R.id.folders_up_icon);
        mUpText = (TextView) rootView.findViewById(R.id.folders_up_text);
        mCurrentFolderText = (TextView) rootView.findViewById(R.id.folders_current_directory_text);

        mUpText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mCurrentFolderText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

        mUpLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    getDir(new File(mCurrentDir).getParentFile().getCanonicalPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        if (mWelcomeSetup) {
            mFoldersListView.setDivider(getResources().getDrawable(R.drawable.icon_list_divider_light));
            mUpIcon.setImageResource(R.drawable.up);
        } else {
            mUpIcon.setImageResource(UIElementsHelper.getIcon(mContext, "up"));

            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                mUpIcon.setImageResource(R.drawable.icon_list_divider_light);
            } else {
                mUpIcon.setImageResource(R.drawable.icon_list_divider);
            }

        }

		mFoldersListView.setDividerHeight(1);
		mRootDir = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
		mCurrentDir = mRootDir;

        //Get a mCursor with a list of all the current folder paths (will be empty if this is the first run).
		mCursor = mApp.getDBAccessHelper().getAllMusicFolderPaths();
        
		//Get a list of all the paths that are currently stored in the DB.
		for (int i=0; i < mCursor.getCount(); i++) {
			mCursor.moveToPosition(i);
			
			//Filter out any double slashes.
			String path = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.FOLDER_PATH));
			if (path.contains("//")) {
				path.replace("//", "/");
			}

			mMusicFolders.put(path, true);
		}
		
		//Close the cursor.
        if (mCursor!=null)
		    mCursor.close();
		
		//Get the folder hierarchy of the selected folder.
        getDir(mRootDir);
        
        mFoldersListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				String newPath = mFileFolderPathsList.get(index);
				getDir(newPath);
				
			}
        	
        });
        
        return rootView;
    }

    /**
     * Sets the current directory's text.
     */
    private void setCurrentDirText() {
        mCurrentFolderText.setText(mCurrentDir);
    }
	
	/**
	 * Retrieves the folder hierarchy for the specified folder 
	 * (this method is NOT recursive and doesn't go into the parent 
	 * folder's subfolders. 
	 */
    private void getDir(String dirPath) {

		mFileFolderNamesList = new ArrayList<String>();
		mFileFolderPathsList = new ArrayList<String>();
		mFileFolderSizesList = new ArrayList<String>();
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		Arrays.sort(files);
		 
		if (files!=null) {
			
			for(int i=0; i < files.length; i++) {
				
				File file = files[i];
			 
				if(!file.isHidden() && file.canRead()) {
					
					if (file.isDirectory()) {
						
						/*
						 * Starting with Android 4.2, /storage/emulated/legacy/... 
						 * is a symlink that points to the actual directory where 
						 * the user's files are stored. We need to detect the 
						 * actual directory's file path here.
						 */
						String filePath;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) 
							filePath = getRealFilePath(file.getAbsolutePath());
						else
							filePath = file.getAbsolutePath();
							
						mFileFolderPathsList.add(filePath);
						mFileFolderNamesList.add(file.getName());
						
						File[] listOfFiles = file.listFiles();
						
						if (listOfFiles!=null) {
							if (listOfFiles.length==1) {
								mFileFolderSizesList.add("" + listOfFiles.length + " item");
							} else {
								mFileFolderSizesList.add("" + listOfFiles.length + " items");
							}
							
						}
						
					}
					
				} 
			
			}
			
		}
		
		boolean dirChecked = false;
		if (getMusicFoldersHashMap().get(dirPath)!=null)
			dirChecked = getMusicFoldersHashMap().get(dirPath);
		
		MultiselectListViewAdapter mFoldersListViewAdapter = new MultiselectListViewAdapter(getActivity(), 
																							this, 
																							mWelcomeSetup,
																							dirChecked);
		
		mFoldersListView.setAdapter(mFoldersListViewAdapter);
		mFoldersListViewAdapter.notifyDataSetChanged();

        mCurrentDir = dirPath;
        setCurrentDirText();
		
    }
    
    /**
     * Resolves the /storage/emulated/legacy paths to 
     * their true folder path representations. Required 
     * for Nexuses and other devices with no SD card.
     */
    @SuppressLint("SdCardPath") 
    private String getRealFilePath(String filePath) {
    	
    	if (filePath.equals("/storage/emulated/0") || 
    		filePath.equals("/storage/emulated/0/") ||
    		filePath.equals("/storage/emulated/legacy") ||
    		filePath.equals("/storage/emulated/legacy/") ||
    		filePath.equals("/storage/sdcard0") || 
    		filePath.equals("/storage/sdcard0/") ||
    		filePath.equals("/sdcard") || 
    		filePath.equals("/sdcard/") || 
    		filePath.equals("/mnt/sdcard") || 
    		filePath.equals("/mnt/sdcard/")) {
    		
    		return Environment.getExternalStorageDirectory().toString();
    	}

    	return filePath;
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (CALLED_FROM_WELCOME==false) {
			getActivity().finish();
		}
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (CALLED_FROM_WELCOME==false) {
			getActivity().finish();
		}
		
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (isRemoving()) {
			mCursor.close();
			mCursor = null;
		}
		
	}
	
	/*
	 * Getter methods.
	 */
	public HashMap<String, Boolean> getMusicFoldersHashMap() {
		return mMusicFolders;
	}
	
	public ArrayList<String> getMusicFolderPaths() {
		return new ArrayList<String>(mMusicFolders.keySet());
	}
	
	public List<String> getFileFolderNamesList() {
		return mFileFolderNamesList;
	}
	
	public List<String> getFileFolderSizesList() {
		return mFileFolderSizesList;
	}
	
	public List<String> getFileFolderPathsList() {
		return mFileFolderPathsList;
	}
	
}

