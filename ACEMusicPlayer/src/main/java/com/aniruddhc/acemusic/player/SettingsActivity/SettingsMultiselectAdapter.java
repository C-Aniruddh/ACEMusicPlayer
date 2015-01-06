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
package com.aniruddhc.acemusic.player.SettingsActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import java.util.Set;

public class SettingsMultiselectAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private Common mApp;
	private SettingsMusicFoldersDialog mFragment;
	private boolean mDirChecked;
	private boolean mWelcomeSetup;
   
    public SettingsMultiselectAdapter(Context context,
                                      SettingsMusicFoldersDialog fragment,
                                      boolean welcomeSetup,
                                      boolean dirChecked) {
    	
    	super(context, -1, fragment.getFileFolderNamesList());
    	
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	mFragment = fragment;
    	mDirChecked = dirChecked; //Indicates if this entire dir is a music folder.
    	mWelcomeSetup = welcomeSetup;
    	
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
		FoldersMultiselectHolder holder = null;

		if (convertView==null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_view_layout_multiselect, parent, false);

			holder = new FoldersMultiselectHolder();
			holder.fileFolderNameText = (TextView) convertView.findViewById(R.id.file_folder_title_multiselect);
			holder.fileFoldersCheckbox = (CheckBox) convertView.findViewById(R.id.music_folder_select_checkbox);
			holder.fileFoldersImage = (ImageView) convertView.findViewById(R.id.file_folder_icon);
			holder.fileFolderSizeText = (TextView) convertView.findViewById(R.id.file_folder_size_multiselect);

            holder.fileFolderNameText.setTextColor(Color.parseColor("#2F2F2F"));
            holder.fileFolderSizeText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));

			holder.fileFolderNameText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			holder.fileFolderSizeText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			
			holder.fileFoldersImage.setImageResource(R.drawable.icon_folderblue);
			convertView.setTag(holder);
			
		} else {
		    holder = (FoldersMultiselectHolder) convertView.getTag();
		}
		
		try {
			holder.fileFolderNameText.setText(mFragment.getFileFolderNamesList().get(position));
			holder.fileFolderSizeText.setText(mFragment.getFileFolderSizesList().get(position));
			
			//Set the corresponding path of the checkbox as it's tag.
			holder.fileFoldersCheckbox.setTag(mFragment.getFileFolderPathsList().get(position));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Set the checkbox status.
		String folderPath = mFragment.getFileFolderPathsList().get(position);
		if (mDirChecked) {
			holder.fileFoldersCheckbox.setChecked(true);
			if (mFragment.getMusicFoldersHashMap().get(folderPath)!=null && 
				mFragment.getMusicFoldersHashMap().get(folderPath)==false) {
				holder.fileFoldersCheckbox.setChecked(false);
			}
			
		} else {
			holder.fileFoldersCheckbox.setChecked(false);
			if (mFragment.getMusicFoldersHashMap().get(folderPath)!=null && 
				mFragment.getMusicFoldersHashMap().get(folderPath)==true) {
				holder.fileFoldersCheckbox.setChecked(true);
			}
			
		}
		
		holder.fileFoldersCheckbox.setOnCheckedChangeListener(checkChangeListener);
		return convertView;
	}
    
    /**
	 * Checkbox status listener.
     */
    private OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
			
			//Only respond to user presses.
			if (checkBox.isPressed()) {
				String filePath = (String) checkBox.getTag();
                if (isChecked)
				    mFragment.getMusicFoldersHashMap().put(filePath, true);
                else
                    if (mFragment.getMusicFoldersHashMap().containsKey(filePath))
                        removeKeyAndSubFolders(filePath);
                    else
                        mFragment.getMusicFoldersHashMap().put(filePath, false);
				
			}
			
		}
    	
    };

    /**
     * Loops through the HashMap and removes the specified key and
     * all other keys that start with the specified key.
     */
    private void removeKeyAndSubFolders(String key) {
        //Get a list of all file paths (keys).
        Set<String> keySet = mFragment.getMusicFoldersHashMap().keySet();
        String[] keyArray = new String[keySet.size()];
        keySet.toArray(keyArray);

        if (keyArray==null || keyArray.length==0)
            return;

        for (int i=0; i < keyArray.length; i++)
            if (keyArray[i].startsWith(key))
                mFragment.getMusicFoldersHashMap().remove(keyArray[i]);

    }

    static class FoldersMultiselectHolder {
		public TextView fileFolderNameText;
		public TextView fileFolderSizeText;
		public CheckBox fileFoldersCheckbox;
		public ImageView fileFoldersImage;

    }
   
}
