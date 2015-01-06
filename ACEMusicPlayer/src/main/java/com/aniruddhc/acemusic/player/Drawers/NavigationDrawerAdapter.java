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
package com.aniruddhc.acemusic.player.Drawers;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class NavigationDrawerAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private SharedPreferences sharedPreferences;
	private ArrayList<String> mTitlesList;
   
    public NavigationDrawerAdapter(Context context, ArrayList<String> titlesList) {
    	super(context, R.layout.sliding_menu_browsers_layout, titlesList);
    	mContext = context;
    	mTitlesList = titlesList;
    	sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	
    	SongsListViewHolder holder = null;
		if (convertView == null) {	
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sliding_menu_browsers_layout, parent, false);
			holder = new SongsListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.nav_drawer_item_title);
			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}

		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
		holder.title.setText(mTitlesList.get(position));
		holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
		
		//Highlight the current browser.
		int[] colors = UIElementsHelper.getQuickScrollColors(mContext);
		if (MainActivity.mCurrentFragmentId==Common.ARTISTS_FRAGMENT && 
			mTitlesList.get(position).equals(mContext.getResources().getString(R.string.artists))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.ALBUM_ARTISTS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.album_artists))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.ALBUMS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.albums))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.SONGS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.songs))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.PLAYLISTS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.playlists))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.GENRES_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.genres))) {
			holder.title.setTextColor(colors[0]);
		} else if (MainActivity.mCurrentFragmentId==Common.FOLDERS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.folders))) {
			holder.title.setTextColor(colors[0]);
		}
		
		return convertView;

	}
    
	static class SongsListViewHolder {
	    public TextView title;
	}
   
}
