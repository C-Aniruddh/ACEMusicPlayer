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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

//Adapter subclass for the Blacklists ListView.
public class BlacklistedElementsAdapter extends ArrayAdapter<String> {
	
	private Context mContext;
	private String elementName = "";
	private String artistName = "";
	private String MANAGER_TYPE = "";
	
	private ArrayList<String> mElementsList = new ArrayList<String>();
	private ArrayList<String> mArtistsList = new ArrayList<String>();
	
    public BlacklistedElementsAdapter(Context context, 
    								  ArrayList<String> elementsList, 
    								  ArrayList<String> artistsList,
    								  String MANAGER_TYPE) {
    	super(context, R.id.customize_screens_title, elementsList);
    	
    	mContext = context;
    	mElementsList = elementsList;
    	mArtistsList = artistsList;
        this.MANAGER_TYPE = MANAGER_TYPE;
        
    }

    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
	    BlacklistManagerHolder holder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.blacklist_manager_list_layout, parent, false);

			holder = new BlacklistManagerHolder();
			holder.blacklistedElementName = (TextView) convertView.findViewById(R.id.blacklist_manager_element_name);
			holder.blacklistedArtistName = (TextView) convertView.findViewById(R.id.blacklist_manager_artist);

			convertView.setTag(holder);
		} else {
		    holder = (BlacklistManagerHolder) convertView.getTag();
		}
		
		//Retrieve the UI element values based on the manager type.
		if (MANAGER_TYPE.equals("ARTISTS")) {
			elementName = mElementsList.get(position);
		} else {
			elementName = mElementsList.get(position);
			artistName = mArtistsList.get(position);
		}

		//Set the element name.
		holder.blacklistedElementName.setText(elementName);

		holder.blacklistedElementName.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.blacklistedElementName.setPaintFlags(holder.blacklistedElementName.getPaintFlags() | 
												    Paint.SUBPIXEL_TEXT_FLAG | 
												    Paint.ANTI_ALIAS_FLAG);
		
		//Hide the artist textview if we're not dealing with blacklisted albums, songs, or playlists.
        if (MANAGER_TYPE.equals("ARTIST")) {
        	holder.blacklistedArtistName.setVisibility(View.GONE);
        } else {
        	
			holder.blacklistedArtistName.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.blacklistedArtistName.setPaintFlags(holder.blacklistedArtistName.getPaintFlags() | 
													    Paint.SUBPIXEL_TEXT_FLAG | 
													    Paint.ANTI_ALIAS_FLAG);
			
			holder.blacklistedArtistName.setText(artistName);
			
        }
		
		return convertView;
	}
    
    static class BlacklistManagerHolder {
        public TextView blacklistedElementName;
        public TextView blacklistedArtistName;
        
    }

}
