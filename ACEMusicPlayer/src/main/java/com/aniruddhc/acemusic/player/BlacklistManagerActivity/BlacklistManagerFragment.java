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

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;

/*******************************************************
 * Displays a list of blacklisted elements (which are 
 * specified by the user). Removing an element from the 
 * blacklist is as easy as flinging it away in either 
 * direction.
 * 
 * @author Saravan Pantham
 *******************************************************/
public class BlacklistManagerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	
	//Temp array that holds the checkbox statuses in the ListView.
	private static ArrayList<Boolean> checkboxStatuses = new ArrayList<Boolean>();
	private static String MANAGER_TYPE;
	private Cursor cursor;
	private BlacklistedElementsAdapter adapter;
	
	//Temp ArrayLists for the cursor data.
	private ArrayList<String> elementNameList = new ArrayList<String>();
	private ArrayList<String> artistNameList = new ArrayList<String>();
	private ArrayList<String> filePathList = new ArrayList<String>();
	private ArrayList<String> songIdsList = new ArrayList<String>();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mApp = (Common) getActivity().getApplicationContext();
        View rootView = (ViewGroup) inflater.inflate(R.layout.fragment_blacklist_manager, container, false);
        mContext = getActivity().getApplicationContext();
        
        ImageView blacklistImage = (ImageView) rootView.findViewById(R.id.blacklist_image);
        blacklistImage.setImageResource(UIElementsHelper.getIcon(mContext, "manage_blacklists"));
    	
        MANAGER_TYPE = getArguments().getString("MANAGER_TYPE");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Get a cursor with a list of blacklisted elements.
        if (MANAGER_TYPE.equals("ARTISTS")) {
            builder.setTitle(R.string.blacklisted_artists);
        	cursor = mApp.getDBAccessHelper().getBlacklistedArtists();
        	
            //Finish the activity if there are no blacklisted elements.
            if (cursor.getCount()==0) {
            	Toast.makeText(getActivity(), R.string.no_blacklisted_items_found, Toast.LENGTH_LONG).show();
            	getActivity().finish();
            } else {
                //Load the cursor data into temporary ArrayLists.
            	for (int i=0; i < cursor.getCount(); i++) {
            		cursor.moveToPosition(i);
            		elementNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
            	}
            	
            }

        } else if (MANAGER_TYPE.equals("ALBUMS")) {
            builder.setTitle(R.string.blacklisted_albums);
        	cursor = mApp.getDBAccessHelper().getBlacklistedAlbums();
        	
        	//Finish the activity if there are no blacklisted elements.
            if (cursor.getCount()==0) {
            	Toast.makeText(getActivity(), R.string.no_blacklisted_items_found, Toast.LENGTH_LONG).show();
            	getActivity().finish();
            } else {
                //Load the cursor data into temporary ArrayLists.
            	for (int i=0; i < cursor.getCount(); i++) {
            		cursor.moveToPosition(i);
            		elementNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
            		artistNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
            	}
            	
            }
        	
        } else if (MANAGER_TYPE.equals("SONGS")) {
            builder.setTitle(R.string.blacklisted_songs);
        	cursor = mApp.getDBAccessHelper().getAllBlacklistedSongs();
        	
        	//Finish the activity if there are no blacklisted elements.
            if (cursor.getCount()==0) {
            	Toast.makeText(getActivity(), R.string.no_blacklisted_items_found, Toast.LENGTH_LONG).show();
            	getActivity().finish();
            } else {
                //Load the cursor data into temporary ArrayLists.
            	for (int i=0; i < cursor.getCount(); i++) {
            		cursor.moveToPosition(i);
            		elementNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
            		artistNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
            		filePathList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
            		songIdsList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ID)));
            	}
            	
            }
        	
        } else if (MANAGER_TYPE.equals("PLAYLISTS")) {
            /*builder.setTitle(R.string.blacklisted_playlists);
        	DBAccessHelper playlistsDBHelper = new DBAccessHelper(getActivity());
        	cursor = playlistsDBHelper.getAllBlacklistedPlaylists();
        	
        	//Finish the activity if there are no blacklisted elements.
            if (cursor.getCount()==0) {
            	Toast.makeText(getActivity(), R.string.no_blacklisted_items_found, Toast.LENGTH_LONG).show();
            	getActivity().finish();
            } else {
                //Load the cursor data into temporary ArrayLists.
            	for (int i=0; i < cursor.getCount(); i++) {
            		cursor.moveToPosition(i);
            		elementNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_NAME)));
            		artistNameList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.NUMBER_OF_SONGS)));
            		filePathList.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_FILE_PATH)));
            	}
            	
            }*/
        	
        } else {
        	Toast.makeText(getActivity(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        	getActivity().finish();
        }
        
        
        TextView blacklistManagerInfoText = (TextView) rootView.findViewById(R.id.blacklist_manager_info_text);
        DragSortListView blacklistManagerListView = (DragSortListView) rootView.findViewById(R.id.blacklist_manager_list);
        blacklistManagerListView.setRemoveListener(onRemove);
        
        blacklistManagerInfoText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        blacklistManagerInfoText.setPaintFlags(blacklistManagerInfoText.getPaintFlags()
        									   | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        blacklistManagerListView.setFastScrollEnabled(true);
        adapter = new BlacklistedElementsAdapter(getActivity(), elementNameList, artistNameList, MANAGER_TYPE);
        blacklistManagerListView.setAdapter(adapter);
        
        return rootView;
        
    }
    
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
    	
        @Override
        public void remove(int which) {
        	
        	//Before we remove the item, retrieve its parameters so we can form a where clause.
        	String element = elementNameList.get(which);
        	
        	String artist = "";
        	if (artistNameList.size()!=0) {
            	artist = artistNameList.get(which);
        	}
        	
        	String filePath = "";
        	if (filePathList.size()!=0) {
            	filePath = filePathList.get(which);
        	}
        	
        	//Escape any rogue apostrophes.
        	if (element.contains("'")) {
        		element = element.replace("'", "''");
        	}
        	
        	if (artist.contains("'")) {
        		artist = artist.replace("'", "''");
        	}
        	
        	if (filePath.contains("'")) {
        		filePath = filePath.replace("'", "''");
        	}
        	
            adapter.remove(adapter.getItem(which));
            
            //Remove the elements in the actual ArrayLists.
            artistNameList.remove(which);
            
            //Unblacklist the element based on the MANAGER_TYPE.
            String where = "";
            if (MANAGER_TYPE.equals("ARTISTS")) {
            	where = DBAccessHelper.SONG_ARTIST + "=" + "'" + element + "'";
            	mApp.getDBAccessHelper().setBlacklistForArtist(element, false);
            	
            } else if (MANAGER_TYPE.equals("ALBUMS")) {
            	where = DBAccessHelper.SONG_ARTIST + "=" + "'" + element + "'";
            	mApp.getDBAccessHelper().setBlacklistForAlbum(element, artist, false);
            	
            } else if (MANAGER_TYPE.equals("SONGS")) {
            	where = DBAccessHelper.SONG_ARTIST + "=" + "'" + element + "'";
            	mApp.getDBAccessHelper().setBlacklistForSong(songIdsList.get(which), false);
            	
            } else if (MANAGER_TYPE.equals("PLAYLISTS")) {
/*            	DBAccessHelper playlistsDBHelper = new DBAccessHelper(mContext);
            	where = DBAccessHelper.SONG_ARTIST + "=" + "'" + element + "'";
            	playlistsDBHelper.unBlacklistPlaylist(filePath);
            	playlistsDBHelper.close();*/
            	
            }
            
            Toast.makeText(mContext, R.string.item_removed_from_blacklist, Toast.LENGTH_LONG).show();
            
        }
        
    };
    
}
