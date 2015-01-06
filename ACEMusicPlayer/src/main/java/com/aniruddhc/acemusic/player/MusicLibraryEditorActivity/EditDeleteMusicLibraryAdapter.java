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
package com.aniruddhc.acemusic.player.MusicLibraryEditorActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class EditDeleteMusicLibraryAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private SharedPreferences sharedPreferences;
    private LibrariesListViewHolder holder = null;
	
    public EditDeleteMusicLibraryAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sliding_menu_list_layout, parent, false);
			holder = new LibrariesListViewHolder();
			holder.tagColor = (ImageView) convertView.findViewById(R.id.sliding_menu_libraries_icon);
			holder.title = (TextView) convertView.findViewById(R.id.sliding_menu_list_item);
            holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

			convertView.setTag(holder);
		} else {
		    holder = (LibrariesListViewHolder) convertView.getTag();
		}
		
		//Retrieve the library's parameters.
		String libraryName = c.getString(c.getColumnIndex(DBAccessHelper.LIBRARY_NAME));
		String libraryColorCode = c.getString(c.getColumnIndex(DBAccessHelper.LIBRARY_TAG));
		
		//Construct the library color tag drawable from the given color code string.
		int colorCodeDrawableID = mContext.getResources().getIdentifier(libraryColorCode, "drawable", mContext.getPackageName());
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.library_name, libraryName);
		convertView.setTag(R.string.library_color_code, libraryColorCode);
		
		//Set the library name.
		holder.title.setText(libraryName);

		holder.tagColor.setImageResource(colorCodeDrawableID);
        
		return convertView;
	}

	static class LibrariesListViewHolder {
	    public ImageView tagColor;
	    public TextView title;
	}
	
}
