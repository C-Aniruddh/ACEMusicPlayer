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
package com.aniruddhc.acemusic.player.FoldersFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.AsyncTasks.AsyncCopyMoveTask;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncDeleteTask;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * FilesFoldersFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class FilesFoldersFragment extends Fragment {

	//Context.
	private Context mContext;
	private FilesFoldersFragment mFilesFoldersFragment;
	private Common mApp;
	
	//UI Elements.
	private ListView listView;
	
	//Folder parameter ArrayLists.
	private String rootDir;
	public static String currentDir;
	private List<String> fileFolderNameList = null; 
	private List<String> fileFolderPathList = null;
	private List<String> fileFolderSizeList = null;
	private List<Integer> fileFolderTypeList = null;
	
	//File size/unit dividers
	private final long kiloBytes = 1024;
	private final long megaBytes = kiloBytes * kiloBytes;
	private final long gigaBytes = megaBytes * kiloBytes;
	private final long teraBytes = gigaBytes * kiloBytes;
	
	//List of subdirectories within a directory (Used by "Play Folder Recursively").
	private ArrayList<String> subdirectoriesList = new ArrayList<String>();
	
	//Flag that determines whether hidden files are displayed or not.
	private boolean SHOW_HIDDEN_FILES = false;

    //Temp file for copy/move operations.
    public File copyMoveSourceFile;

    //Flag that indicates if copyMoveSourceFile should be moved or copied.
    public boolean shouldMoveCopiedFile = false;
    public boolean mIsPasteShown = false;

    //HashMap to store the each folder's previous scroll/position state.
    private HashMap<String, Parcelable> mFolderStateMap;

	//Handler.
	private Handler mHandler = new Handler();

    public static final int FOLDER = 0;
    public static final int FILE = 1;
    public static final int AUDIO_FILE = 3;
    public static final int PICTURE_FILE = 4;
    public static final int VIDEO_FILE = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
        mContext = getActivity().getApplicationContext();
        mFilesFoldersFragment = this;
        mApp = (Common) mContext;
        mFolderStateMap = new HashMap<String, Parcelable>();
        
        //Set the hidden files flag.
        SHOW_HIDDEN_FILES = mApp.getSharedPreferences().getBoolean("SHOW_HIDDEN_FILES", false);

        listView = (ListView) rootView.findViewById(R.id.folders_list_view);
        listView.setFastScrollEnabled(true);
        listView.setVisibility(View.INVISIBLE);
        
		//Set the background color based on the theme.
        rootView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

        //Apply the ListView params.
        //Apply the ListViews' dividers.
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            listView.setDivider(mContext.getResources().getDrawable(R.drawable.icon_list_divider));
        } else {
            listView.setDivider(mContext.getResources().getDrawable(R.drawable.icon_list_divider_light));
        }

        listView.setDividerHeight(1);

		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            if (rootView!=null) {
            	rootView.setPadding(0, topPadding, 0, 0);
            }
            
            listView.setClipToPadding(false);
            listView.setPadding(0, 0, 0, navigationBarHeight);
        }

        rootDir = mApp.getSharedPreferences().getString("DEFAULT_FOLDER", Environment.getExternalStorageDirectory().getPath());
        currentDir = rootDir;
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                slideUpListView();
            }

        }, 250);
        return rootView;
    }

    /**
     * Slides in the ListView.
     */
    private void slideUpListView() {

        getDir(rootDir, null);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 2.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f);

        animation.setDuration(600);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
               listView.setVisibility(View.VISIBLE);

            }

        });

        listView.startAnimation(animation);
    }
    
    /**
     * Retrieves the folder hierarchy for the specified folder.
     *
     * @param dirPath The path of the new folder.
     * @param restoreState The state of the ListView that should be restored. Pass
     *                     null if the ListView's position should not be restored.
     */
    private void getDir(String dirPath, Parcelable restoreState) {
    	
        ((MainActivity) getActivity()).showFolderFragmentActionItems(dirPath,
                                                                     getActivity().getMenuInflater(),
                                                                     ((MainActivity) getActivity()).getMenu(),
                                                                     mIsPasteShown);

		fileFolderNameList = new ArrayList<String>();
		fileFolderPathList = new ArrayList<String>();
		fileFolderSizeList = new ArrayList<String>();
		fileFolderTypeList = new ArrayList<Integer>();
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		 
		if (files!=null) {
			
			//Sort the files by name.
			Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
			
			for(int i=0; i < files.length; i++) {
				
				File file = files[i];
				if(file.isHidden()==SHOW_HIDDEN_FILES && file.canRead()) {
					
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

                        fileFolderPathList.add(filePath);
						fileFolderNameList.add(file.getName());
						File[] listOfFiles = file.listFiles();
						
						if (listOfFiles!=null) {
							fileFolderTypeList.add(FOLDER);
							if (listOfFiles.length==1) {
								fileFolderSizeList.add("" + listOfFiles.length + " item");
							} else {
								fileFolderSizeList.add("" + listOfFiles.length + " items");
							}
							
						} else {
							fileFolderTypeList.add(FOLDER);
							fileFolderSizeList.add("Unknown items");
						}
						
					} else {
						
						try {
							String path = file.getCanonicalPath();
							fileFolderPathList.add(path);
						} catch (IOException e) {
							continue;
						}
						
						fileFolderNameList.add(file.getName());
						String fileName = "";
						try {
							fileName = file.getCanonicalPath();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//Add the file element to fileFolderTypeList based on the file type.
						if (getFileExtension(fileName).equalsIgnoreCase("mp3") ||
							getFileExtension(fileName).equalsIgnoreCase("3gp") ||
							getFileExtension(fileName).equalsIgnoreCase("mp4") ||
							getFileExtension(fileName).equalsIgnoreCase("m4a") ||
							getFileExtension(fileName).equalsIgnoreCase("aac") ||
							getFileExtension(fileName).equalsIgnoreCase("ts") ||
							getFileExtension(fileName).equalsIgnoreCase("flac") ||
							getFileExtension(fileName).equalsIgnoreCase("mid") ||
							getFileExtension(fileName).equalsIgnoreCase("xmf") ||
							getFileExtension(fileName).equalsIgnoreCase("mxmf") ||
							getFileExtension(fileName).equalsIgnoreCase("midi") ||
							getFileExtension(fileName).equalsIgnoreCase("rtttl") ||
							getFileExtension(fileName).equalsIgnoreCase("rtx") ||
							getFileExtension(fileName).equalsIgnoreCase("ota") ||
							getFileExtension(fileName).equalsIgnoreCase("imy") ||
							getFileExtension(fileName).equalsIgnoreCase("ogg") ||
							getFileExtension(fileName).equalsIgnoreCase("mkv") ||
							getFileExtension(fileName).equalsIgnoreCase("wav")) {
							
							//The file is an audio file.
							fileFolderTypeList.add(AUDIO_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else if (getFileExtension(fileName).equalsIgnoreCase("jpg") ||
								   getFileExtension(fileName).equalsIgnoreCase("gif") ||
								   getFileExtension(fileName).equalsIgnoreCase("png") ||
								   getFileExtension(fileName).equalsIgnoreCase("bmp") ||
								   getFileExtension(fileName).equalsIgnoreCase("webp")) {
							
							//The file is a picture file.
							fileFolderTypeList.add(PICTURE_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else if (getFileExtension(fileName).equalsIgnoreCase("3gp") ||
								   getFileExtension(fileName).equalsIgnoreCase("mp4") ||
								   getFileExtension(fileName).equalsIgnoreCase("3gp") ||
								   getFileExtension(fileName).equalsIgnoreCase("ts") ||
								   getFileExtension(fileName).equalsIgnoreCase("webm") ||
								   getFileExtension(fileName).equalsIgnoreCase("mkv")) {
							
							//The file is a video file.
							fileFolderTypeList.add(VIDEO_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else {
							
							//We don't have an icon for this file type so give it the generic file flag.
							fileFolderTypeList.add(FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						}

					}
					
				} 
			
			}
			
		}
		
		FoldersListViewAdapter foldersListViewAdapter = new FoldersListViewAdapter(getActivity(),
                                                                                   this,
																				   fileFolderNameList,
																				   fileFolderTypeList, 
																				   fileFolderSizeList, 
																				   fileFolderPathList);
		
		listView.setAdapter(foldersListViewAdapter);
		foldersListViewAdapter.notifyDataSetChanged();

        //Restore the ListView's previous state.
        if (restoreState!=null) {
            listView.onRestoreInstanceState(restoreState);
        } else if (mFolderStateMap.containsKey(dirPath)) {
            listView.onRestoreInstanceState(mFolderStateMap.get(dirPath));
        }

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {

                //Store the current folder's state in the HashMap.
                if (mFolderStateMap.size()==3) {
                    mFolderStateMap.clear();
                }

                mFolderStateMap.put(currentDir, listView.onSaveInstanceState());

                String newPath = fileFolderPathList.get(index);
                if ((Integer) view.getTag(R.string.folder_list_item_type)==FOLDER)
                    currentDir = newPath;

                //Check if the selected item is a folder or a file.
                if (fileFolderTypeList.get(index)==FOLDER) {
                    getDir(newPath, null);
                } else {
                    int fileIndex = 0;
                    for (int i=0; i < index; i++) {
                        if (fileFolderTypeList.get(i)==AUDIO_FILE)
                            fileIndex++;
                    }

                    play(fileFolderTypeList.get(index), fileIndex, currentDir);
                }

            }

        });
		
    }

    /**
     * Refreshes the ListView with the current dataset.
     */
    public void refreshListView() {
        //Update the ListView.
        getDir(currentDir, listView.onSaveInstanceState());
    }

    /**
     * Resolves the /storage/emulated/legacy paths to
     * their true folder path representations. Required
     * for Nexii and other devices with no SD card.
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

    /**
     * Calculates the parent dir of the current dir and calls getDir().
     * Returns true if the parent dir is the rootDir
     */
    public boolean getParentDir() {

        if (currentDir.equals("/"))
            return true;

        //Get the current folder's parent folder.
        File currentFolder = new File(currentDir);
        String parentFolder = "";
        try {
            parentFolder = currentFolder.getParentFile().getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FilesFoldersFragment.currentDir = parentFolder;
        getDir(parentFolder, null);
        return false;

    }

    /**
     * Takes in a file size value and formats it.
     */
    public String getFormattedFileSize(final long value) {
    	
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

    public String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }
    
    public String getFileExtension(String fileName) {
        String fileNameArray[] = fileName.split("\\.");
        String extension = fileNameArray[fileNameArray.length-1];

        return extension;
        
    }
    
    /**
     * This method goes through a folder recursively and saves all its
     * subdirectories to an ArrayList (subdirectoriesList). 
     */
    public void iterateThruFolder(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list==null) {
        	return;
        }

        for (File f : list) {
        	
            if (f.isDirectory()) {
                iterateThruFolder(f.getAbsolutePath());
                
                if (!subdirectoriesList.contains(f.getPath())) {
                	subdirectoriesList.add(f.getPath());
                }
                    
            }
            
        }
        
    }
    
    /**
     * Plays the specified file/folder.
     *
     * @param itemType Specifies whether the input path is a file path
     *                 or a folder path.
     * @param index The index of the first song to play. Pass 0 if itemType
     *              is FOLDER.
     * @param folderPath The path of the folder that should be played.
     */
    public void play(int itemType, int index, String folderPath) {
        //Build the query's selection clause.
        String querySelection = MediaStore.Audio.Media.DATA + " LIKE "
                + "'" + folderPath.replace("'", "''") + "/%'";

        //Exclude all subfolders from this playback sequence if we're playing a file.
        if (itemType==AUDIO_FILE) {
            for (int i = 0; i < fileFolderPathList.size(); i++) {
                if (fileFolderTypeList.get(i) == FOLDER)
                    querySelection += " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE "
                            + "'" + fileFolderPathList.get(i).replace("'", "''") + "/%'";

            }

            mApp.getPlaybackKickstarter().initPlayback(mContext,
                                                       querySelection,
                                                       Common.PLAY_ALL_IN_FOLDER,
                                                       index,
                                                       true, false);

        } else if (itemType==FOLDER) {
            mApp.getPlaybackKickstarter().initPlayback(mContext,
                                                       querySelection,
                                                       Common.PLAY_ALL_IN_FOLDER,
                                                       index,
                                                       true, false);
        } else {
            Toast.makeText(mContext, R.string.cant_play_this_file, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Displays a "Rename" dialog and renames the specified file/folder.
     *
     * @param path The path of the folder/file that needs to be renamed.
     */
    public void rename(String path) {

        final File renameFile = new File(path);
        final AlertDialog renameAlertDialog = new AlertDialog.Builder(getActivity()).create();
        final EditText fileEditText = new EditText(getActivity());

        fileEditText.setHint(R.string.file_name);
        fileEditText.setSingleLine(true);
        fileEditText.setText(renameFile.getName());

        renameAlertDialog.setView(fileEditText);
        renameAlertDialog.setTitle(R.string.rename);
        renameAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                                    mContext.getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            renameAlertDialog.dismiss();
                                        }

                                    });

        renameAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                                    mContext.getResources().getString(R.string.rename),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //Check if the new file name is empty.
                                            if (fileEditText.getText().toString().isEmpty()) {
                                                Toast.makeText(getActivity(), R.string.enter_a_name_for_folder, Toast.LENGTH_LONG).show();
                                            } else {

                                                File newNameFile = null;
                                                try {
                                                    newNameFile = new File(renameFile.getParentFile().getCanonicalPath() + "/" + fileEditText.getText().toString());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getActivity(), R.string.folder_could_not_be_renamed, Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                                try {
                                                    if (renameFile.isDirectory())
                                                        FileUtils.moveDirectory(renameFile, newNameFile);
                                                    else
                                                        FileUtils.moveFile(renameFile, newNameFile);

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getActivity(), R.string.folder_could_not_be_renamed, Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                                Toast.makeText(getActivity(), R.string.folder_renamed, Toast.LENGTH_SHORT).show();
                                                renameAlertDialog.dismiss();
                                                refreshListView();

                                            }

                                        }

                                    });

        renameAlertDialog.show();

    }

    /**
     * Stores the specified file/folder's path in a temp variable and displays
     * the "Paste" option in the ActionBar.
     *
     * @param path The path of the file/folder to copy/move.
     * @param shouldMove Pass true if the file/folder should be moved instead of copied.
     */
    public void copyMove(String path, boolean shouldMove) {
        shouldMoveCopiedFile = shouldMove;
        copyMoveSourceFile = new File(path);
        if (!copyMoveSourceFile.exists()) {
            Toast.makeText(mContext, R.string.cant_copy_this_file_folder, Toast.LENGTH_SHORT).show();
            return;
        }

        //Show the paste option in the ActionBar.
        mIsPasteShown =  true;
        ((MainActivity) getActivity()).showFolderFragmentActionItems(currentDir,
                                                                     getActivity().getMenuInflater(),
                                                                     ((MainActivity) getActivity()).getMenu(),
                                                                     true);

    }

    /**
     * Pastes the specified file into the current directory.
     *
     * @param file The file to paste into the current directory.
     */
    public void pasteIntoCurrentDir(File file) {
        mIsPasteShown =  false;
        AsyncCopyMoveTask task = new AsyncCopyMoveTask(mContext, file, new File(currentDir + "/" + file.getName()),
                                                       this, shouldMoveCopiedFile);
        task.execute();
    }

    /**
     * Deletes the specified file.
     *
     * @param file The file to delete.
     */
    public void deleteFile(File file) {
        int fileType;
        if (file.isDirectory())
            fileType = FOLDER;
        else
            fileType = FILE;

        AsyncDeleteTask task = new AsyncDeleteTask(getActivity(), this, file, fileType);
        task.execute();
    }

    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	mContext = null;
    	listView = null;

    }
    
    /*
     * Getter methods. 
     */
    public String getCurrentDir() {
    	return currentDir;
    }
     
    /*
     * Setter methods.
     */
    public void setCurrentDir(String currentDir) {
    	this.currentDir = currentDir;
    }
    
}
