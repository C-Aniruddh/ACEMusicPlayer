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
package com.aniruddhc.acemusic.player.NowPlayingActivity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class NowPlayingQueueListAdapter extends ArrayAdapter<Integer> {

	private Context mContext;
	private Common mApp;
	private ArrayList<Integer> mPlaybackIndecesList;
   
    public NowPlayingQueueListAdapter(Context context, ArrayList<Integer> playbackIndecesList) {
    	
    	super(context, -1, playbackIndecesList);
    	
    	mContext = context;
    	mApp = (Common) mContext;
    	mPlaybackIndecesList = playbackIndecesList;
    	
    }
    
    public View getView(final int position, View convertView, ViewGroup parent){
    	
    	NowPlayingQueueListViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.now_playing_queue_listview_layout, parent, false);
			holder = new NowPlayingQueueListViewHolder();
			holder.songTitleText = (TextView) convertView.findViewById(R.id.playlists_flipped_song);
			holder.artistText = (TextView) convertView.findViewById(R.id.playlists_flipped_artist);
			holder.removeSong = (ImageView) convertView.findViewById(R.id.remove_song_from_queue);
			
			holder.songTitleText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.songTitleText.setPaintFlags(holder.songTitleText.getPaintFlags()
											 | Paint.ANTI_ALIAS_FLAG
											 | Paint.SUBPIXEL_TEXT_FLAG);		
			
			holder.artistText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			holder.artistText.setPaintFlags(holder.artistText.getPaintFlags()
											 | Paint.ANTI_ALIAS_FLAG
											 | Paint.SUBPIXEL_TEXT_FLAG);
			
			convertView.setTag(holder);
		} else {
		    holder = (NowPlayingQueueListViewHolder) convertView.getTag();
		}
		
		//Move the local cursor to the correct position.
		mApp.getService().getCursor().moveToPosition(mPlaybackIndecesList.get(position));
		
		//Get the song's parameters.
		String songTitle;
		String songFilePath;
		String songArtist;
		try {
			songTitle = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE));
			songFilePath = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
			songArtist = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ARTIST));
		} catch (Exception e) {
			/* If an exception is raised, the user is probably playing from the folders and the cursor hasn't been completely built yet.
			 * Just use temporary placeholders for now and the ListView will automatically refresh itself once the cursor is fully built. */
			songTitle = "Loading...";
			songFilePath = "";
			songArtist = "";
		}
		
		//Set the view tags.
		convertView.setTag(R.string.title, songTitle);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.artist, songArtist);
		
		holder.songTitleText.setText(songTitle);
		holder.artistText.setText(songArtist);
		
		//Apply the card layout's background based on the color theme.
		if (position==mApp.getService().getCurrentSongIndex()) {
			int[] colors = UIElementsHelper.getQuickScrollColors(mContext);
			convertView.setBackgroundColor(colors[0]);
			holder.songTitleText.setTextColor(colors[2]);
			holder.artistText.setTextColor(colors[2]);
			holder.removeSong.setImageResource(R.drawable.cross_light);
			
		} else if (mApp.getCurrentTheme()==Common.LIGHT_THEME) {
			convertView.setBackgroundColor(0xFFFFFFFF);
			holder.songTitleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.artistText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
			holder.removeSong.setImageResource(R.drawable.cross);
			
		} else if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			convertView.setBackgroundColor(0xFF191919);
			holder.songTitleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.artistText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
			holder.removeSong.setImageResource(R.drawable.cross_light);
			
		}
		
		return convertView;

	}
    
	class NowPlayingQueueListViewHolder {
	    public TextView songTitleText;
	    public TextView artistText;
	    public ImageView removeSong;
	}
	
}
