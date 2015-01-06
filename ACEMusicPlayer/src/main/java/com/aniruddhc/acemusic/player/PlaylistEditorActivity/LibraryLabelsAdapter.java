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
package com.aniruddhc.acemusic.player.PlaylistEditorActivity;

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
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class LibraryLabelsAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private ArrayList<String> mColorsList;
   
    public LibraryLabelsAdapter(Context context, ArrayList<String> colorsList) {
    	super(context, R.id.playlists_flipped_song, colorsList);
    	mContext = context;
    	mColorsList = colorsList;

    }
    
    public View getView(final int position, View convertView, ViewGroup parent){
    	
    	SongsListViewHolder holder = null;
		if (convertView == null) {	
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sliding_menu_list_layout, parent, false);
			holder = new SongsListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.sliding_menu_list_item);
			holder.image = (ImageView) convertView.findViewById(R.id.sliding_menu_libraries_icon);
			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}

		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.title.setPaintFlags(holder.title.getPaintFlags()
										 | Paint.ANTI_ALIAS_FLAG
										 | Paint.SUBPIXEL_TEXT_FLAG);		
		
		holder.title.setText(mColorsList.get(position));

		//Set the icon.
		switch(position) {
		case 0:
			holder.image.setImageResource(R.drawable.circle_blue_dark);
			break;
		case 1:
			holder.image.setImageResource(R.drawable.circle_blue_light);
			break;
		case 2:
			holder.image.setImageResource(R.drawable.circle_green_dark);
			break;
		case 3:
			holder.image.setImageResource(R.drawable.circle_green_light);
			break;
		case 4:
			holder.image.setImageResource(R.drawable.circle_purple_dark);
			break;
		case 5:
			holder.image.setImageResource(R.drawable.circle_purple_light);
			break;
		case 6:
			holder.image.setImageResource(R.drawable.circle_red_dark);
			break;
		case 7:
			holder.image.setImageResource(R.drawable.circle_red_light);
			break;
		case 8:
			holder.image.setImageResource(R.drawable.circle_yellow_dark);
			break;
		case 9:
			holder.image.setImageResource(R.drawable.circle_yellow_light);
			break;
		}
		
		return convertView;

	}
    
	static class SongsListViewHolder {
	    public TextView title;
	    public ImageView image;
	}
   
}
