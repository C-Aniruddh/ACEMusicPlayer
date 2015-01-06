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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.MainActivity.MainActivity;
import com.aniruddhc.acemusic.player.SettingsActivity.SettingsActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class NavigationDrawerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;

    private RelativeLayout mLibraryPickerLayout;
    private TextView mLibraryPickerHeaderText;
    private Spinner mLibraryPickerSpinner;
	private ListView browsersListView;
	
	private Cursor cursor;
    private int mCurrentLibraryPosition;
	private NavigationDrawerLibrariesAdapter mLibrariesAdapter;
	private NavigationDrawerAdapter mBrowsersAdapter;
	private Handler mHandler;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity();
		mApp = (Common) mContext.getApplicationContext();
		mHandler = new Handler();

		View rootView = inflater.inflate(R.layout.navigation_drawer_layout, null);
		rootView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

		browsersListView = (ListView) rootView.findViewById(R.id.browsers_list_view);
        mLibraryPickerLayout = (RelativeLayout) rootView.findViewById(R.id.library_picker_layout);
        mLibraryPickerSpinner = (Spinner) rootView.findViewById(R.id.library_picker_spinner);
        mLibraryPickerHeaderText = (TextView) rootView.findViewById(R.id.library_picker_header_text);
        mLibraryPickerHeaderText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
		
		//Apply the Browser ListView's adapter.
		List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
		mBrowsersAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
		browsersListView.setAdapter(mBrowsersAdapter);
		browsersListView.setOnItemClickListener(browsersClickListener);
		setListViewHeightBasedOnChildren(browsersListView);

		//Apply the Libraries ListView's adapter.
        cursor = mApp.getDBAccessHelper().getAllUniqueLibraries();
        mLibrariesAdapter = new NavigationDrawerLibrariesAdapter(getActivity(), cursor);
        mLibraryPickerSpinner.setAdapter(mLibrariesAdapter);
        mLibraryPickerSpinner.setSelection(mApp.getCurrentLibraryIndex());
        mLibraryPickerSpinner.setOnItemSelectedListener(librariesItemSelectedListener);

        browsersListView.setDividerHeight(0);

        //KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Common.getNavigationBarHeight(mContext);
            if (browsersListView!=null) {
                browsersListView.setPadding(0, 0, 0, navBarHeight);
                browsersListView.setClipToPadding(false);
            }

        }

		return rootView;
	}

    private AdapterView.OnItemSelectedListener librariesItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (mApp.getCurrentLibraryIndex()==position)
                return;

            mApp.getSharedPreferences().edit().putString(Common.CURRENT_LIBRARY,
                                                         (String) view.getTag(R.string.library_name)).commit();

            mApp.getSharedPreferences().edit().putInt(Common.CURRENT_LIBRARY_POSITION, position).commit();

            //Update the fragment.
            ((MainActivity) getActivity()).loadFragment(null);

            //Reset the ActionBar after 500ms.
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    getActivity().invalidateOptionsMenu();

                }

            }, 500);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

	private OnItemClickListener browsersClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			switch (position) {
			case 0:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ARTISTS_FRAGMENT);
				break;
			case 1:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ALBUM_ARTISTS_FRAGMENT);
				break;
			case 2:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ALBUMS_FRAGMENT);
				break;
			case 3:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.SONGS_FRAGMENT);
				break;
			case 4:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.PLAYLISTS_FRAGMENT);
				break;
			case 5:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.GENRES_FRAGMENT);
				break;
			case 6:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.FOLDERS_FRAGMENT);
				break;
            case 7:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
			}
			
			//Update the adapter to reflect the new fragment.
			List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
			mBrowsersAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
			browsersListView.setAdapter(mBrowsersAdapter);
			
			//Update the fragment.
			((MainActivity) getActivity()).loadFragment(null);
			
			//Reset the ActionBar after 500ms.
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					getActivity().invalidateOptionsMenu();
					
				}
				
			}, 500);

		}
		
	};
	
	/**
	 * Clips ListViews to fit within the drawer's boundaries.
	 */
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
		
    }
	
}
