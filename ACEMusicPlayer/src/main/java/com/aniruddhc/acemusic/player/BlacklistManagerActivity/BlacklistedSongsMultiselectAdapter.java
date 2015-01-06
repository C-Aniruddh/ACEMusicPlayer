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
package com.aniruddhc.acemusic.player.BlacklistManagerActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

public class BlacklistedSongsMultiselectAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private Common mApp;
	
    public BlacklistedSongsMultiselectAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        final Cursor c = (Cursor) getItem(position);
	    SongsListViewHolder holder = null;

		if (convertView == null) {
			
			convertView = LayoutInflater.from(mContext).inflate(R.layout.music_library_editor_songs_layout, parent, false);
			holder = new SongsListViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.songThumbnailMusicLibraryEditor);
			holder.title = (TextView) convertView.findViewById(R.id.songNameMusicLibraryEditor);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.songCheckboxMusicLibraryEditor);
			holder.subText = (TextView) convertView.findViewById(R.id.artistNameSongListView);

			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}
		
		final View finalConvertView = convertView;
		final String songId = c.getString(c.getColumnIndex(DBAccessHelper._ID));
		final String songTitle = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
		final String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String songAlbumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String songArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songBlacklistStatus = c.getString(c.getColumnIndex(DBAccessHelper.BLACKLIST_STATUS));
		
		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		
		holder.subText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.subText.setPaintFlags(holder.subText.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		
		//Set the songID as the view's tag.
		convertView.setTag(R.string.song_id, songId);
		convertView.setTag(R.string.song_file_path, songFilePath);
		
		//Set the song title.
		holder.title.setText(songTitle);
		holder.subText.setText(songArtist);
        mApp.getImageLoader().displayImage(songAlbumArtPath, holder.image, BlacklistManagerActivity.displayImageOptions);

        //Check if the song's DB ID exists in the HashSet and set the appropriate checkbox status.
        if (BlacklistManagerActivity.songIdBlacklistStatusPair.get(songId).equals("TRUE")) {
        	convertView.setBackgroundColor(0xCCFF4444);
        	holder.checkBox.setChecked(true);
        } else {
        	convertView.setBackgroundColor(0x00000000);
        	holder.checkBox.setChecked(false);
        }
        
        holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {
				
				if (isChecked==true) {
					
					//Only receive inputs by the user and ignore any system-made changes to the checkbox state.
					if (checkbox.isPressed()) {
						finalConvertView.setBackgroundColor(0xCCFF4444);
						BlacklistManagerActivity.songIdBlacklistStatusPair.put(songId, true);
					}
					
				} else if (isChecked==false) {
					
					//Only receive inputs by the user and ignore any system-made changes to the checkbox state.
					if (checkbox.isPressed()) {
						finalConvertView.setBackgroundColor(0x000000);
						BlacklistManagerActivity.songIdBlacklistStatusPair.put(songId, false);
					}

				}

			}
			
        });
 
		return convertView;
	}
    
	static class SongsListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public TextView subText;
	    public CheckBox checkBox;
	}
	
}
