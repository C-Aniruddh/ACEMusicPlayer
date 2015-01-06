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

import org.apache.commons.io.FileUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.FoldersFragment.FilesFoldersFragment;

public class AsyncCopyMoveTask extends AsyncTask<String, Void, Boolean> {
	
    private Context mContext;
    private ProgressDialog pd;
    private FilesFoldersFragment mFragment;
    private boolean mShouldMove;
	
	private File mSourceFile;
	private File mDestinationFile;
    
    public AsyncCopyMoveTask(Context context,
                             File sourceFile,
                             File destinationFile,
                             FilesFoldersFragment fragment,
                             boolean shouldMove) {
    	
    	mContext = context;
    	mSourceFile = sourceFile;
        mFragment = fragment;
    	mDestinationFile = destinationFile;
        mShouldMove = shouldMove;
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mFragment.getActivity());
		pd.setCancelable(false);
		pd.setIndeterminate(false);

        if (mShouldMove) {
            pd.setTitle(R.string.move);
            pd.setMessage(mContext.getResources().getString(R.string.moving_file));
        } else {
            pd.setTitle(R.string.copy);
            pd.setMessage(mContext.getResources().getString(R.string.copying_file));
        }

		pd.setButton(DialogInterface.BUTTON_NEUTRAL, mContext.getResources()
															 .getString(R.string.run_in_background), 
															 new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				pd.dismiss();
				
			}
			
		});
		
		pd.show();
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {

        try {
            if (mSourceFile.getCanonicalPath()==mDestinationFile.getCanonicalPath()) {
                Toast.makeText(mContext, R.string.source_target_same, Toast.LENGTH_LONG).show();
                return false;
            }

        } catch (Exception e) {
            return false;
        }

    	if (mSourceFile.isDirectory()) {
    		
    		try {
                if (mShouldMove)
				    FileUtils.moveDirectoryToDirectory(mSourceFile, mDestinationFile, true);
                else
                    FileUtils.copyDirectoryToDirectory(mSourceFile, mDestinationFile);

			} catch (Exception e) {
				return false;
			}
    		
    	} else {
    		
    		try {
                if (mShouldMove)
    			    FileUtils.moveFileToDirectory(mSourceFile, mDestinationFile, true);
                else
                    FileUtils.copyFile(mSourceFile, mDestinationFile);

    		} catch (Exception e) {
    			return false;
    		}
    		
    	}

    	return true;
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    	pd.dismiss();
    	if (result==true) {
            if (mShouldMove)
        	    Toast.makeText(mContext, R.string.done_move, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, R.string.done_copy, Toast.LENGTH_SHORT).show();
    	} else {
            if (mShouldMove)
        	    Toast.makeText(mContext, R.string.file_could_not_be_written_new_location, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(mContext, R.string.file_could_not_be_written_new_location, Toast.LENGTH_LONG).show();
    	}

        try {
            mFragment.refreshListView();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error er) {
            er.printStackTrace();
        }
		
	}

}
