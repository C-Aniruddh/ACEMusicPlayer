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
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class InnerNavigationDrawerFragment extends Fragment {

    private Common mApp;
	private ListView browsersListView;
	private ListView librariesListView;
	private TextView browsersHeaderText;
	private TextView librariesHeaderText;
	public static ImageView librariesColorTagImageView;
	private ImageView librariesIcon;
	
	private Cursor cursor;
	private DBAccessHelper userLibrariesDBHelper;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.navigation_drawer_layout, null);
		mApp = (Common) getActivity().getApplicationContext();
		
		browsersListView = (ListView) rootView.findViewById(R.id.browsers_list_view);
		librariesListView = (ListView) rootView.findViewById(R.id.libraries_list_view);
		browsersHeaderText = (TextView) rootView.findViewById(R.id.browsers_header_text);
		librariesHeaderText = (TextView) rootView.findViewById(R.id.libraries_header_text);
		librariesColorTagImageView = (ImageView) rootView.findViewById(R.id.library_color_tag);
		librariesIcon = (ImageView) rootView.findViewById(R.id.libraries_icon);
		librariesIcon.setImageResource(UIElementsHelper.getIcon(getActivity(), "libraries"));
		
		Drawable backgroundDrawable;
		if (mApp.getCurrentTheme()== Common.DARK_THEME) {
			backgroundDrawable = new ColorDrawable(0x191919);
		} else {
			backgroundDrawable = getResources().getDrawable(R.drawable.holo_white_selector);
		}
		
		int currentAPI = android.os.Build.VERSION.SDK_INT;
		if (currentAPI < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackgroundDrawable(backgroundDrawable);
		} else {
			rootView.setBackground(backgroundDrawable);
		}
		
		//Set the header text fonts/colors.
		browsersHeaderText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		librariesHeaderText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
		browsersHeaderText.setPaintFlags(browsersHeaderText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		librariesHeaderText.setPaintFlags(librariesHeaderText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		//Apply the Browser ListView's adapter.
		List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
		NavigationDrawerAdapter slidingMenuAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
		browsersListView.setAdapter(slidingMenuAdapter);
		browsersListView.setOnItemClickListener(browsersClickListener);
		setListViewHeightBasedOnChildren(browsersListView);
		
		/*//Apply the Libraries ListView's adapter.
		userLibrariesDBHelper = new DBAccessHelper(getActivity().getApplicationContext());
		cursor = userLibrariesDBHelper.getAllUniqueLibraries();
		NavigationDrawerLibrariesAdapter slidingMenuLibrariesAdapter = new NavigationDrawerLibrariesAdapter(getActivity(), cursor);
		librariesListView.setAdapter(slidingMenuLibrariesAdapter);
		setListViewHeightBasedOnChildren(librariesListView);*/
		librariesListView.setVisibility(View.GONE);
		librariesHeaderText.setVisibility(View.GONE);
		librariesIcon.setVisibility(View.GONE);

		return rootView;
	}
	
	private OnItemClickListener browsersClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			Intent intent = null;
			switch (position) {
			case 0:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "ARTISTS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "ALBUM_ARTISTS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 2:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "ALBUMS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "SONGS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 4:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "PLAYLISTS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 5:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "GENRES");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			case 6:
				intent = new Intent(getActivity(), MainActivity.class);
				intent.putExtra("TARGET_FRAGMENT", "FOLDERS");
				//intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			}
			
		}
		
	};
	
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	if (cursor!=null) {
    		cursor.close();
    		cursor = null;
    	}
		
    	if (userLibrariesDBHelper!=null) {
    		userLibrariesDBHelper.close();
    		userLibrariesDBHelper = null;
    	}
		
    }
	
}
