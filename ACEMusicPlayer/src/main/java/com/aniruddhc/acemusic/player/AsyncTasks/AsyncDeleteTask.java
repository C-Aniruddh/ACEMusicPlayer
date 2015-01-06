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

public class AsyncDeleteTask extends AsyncTask<String, Void, Boolean> {
    private Context mContext;
    private ProgressDialog pd;
    private FilesFoldersFragment mFragment;
	
	private File mSourceFile;
	private int mSourceType;
    
    public AsyncDeleteTask(Context context,
                           FilesFoldersFragment fragment,
    					   File source,
    					   int sourceType) {
    	
    	mContext = context;
        mFragment = fragment;
    	mSourceFile = source;
    	mSourceType = sourceType;
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		pd.setIndeterminate(false);
		pd.setTitle(R.string.delete);
		pd.setMessage(mContext.getResources().getString(R.string.deleting_files));
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
    	
    	if (mSourceType==FilesFoldersFragment.FOLDER) {
    		
    		try {
				FileUtils.deleteDirectory(mSourceFile);
			} catch (Exception e) {
				return false;
			}
    		
    	} else {
    		try {
    			boolean status = mSourceFile.delete();
    			if (status==true) {
    				return true;
    			} else {
    				return false;
    			}
    			
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
            if (mSourceType==FilesFoldersFragment.FOLDER)
        	    Toast.makeText(mContext, R.string.folder_deleted, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, R.string.file_deleted, Toast.LENGTH_SHORT).show();

    	} else {
            if (mSourceType==FilesFoldersFragment.FOLDER)
        	    Toast.makeText(mContext, R.string.folder_could_not_be_deleted, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(mContext, R.string.file_could_not_be_deleted, Toast.LENGTH_LONG).show();

    	}

        try {
            mFragment.refreshListView();
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

}
