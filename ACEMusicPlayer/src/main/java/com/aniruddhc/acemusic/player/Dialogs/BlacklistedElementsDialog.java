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
package com.aniruddhc.acemusic.player.Dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Utils.Common;

/*******************************************************
 * Displays a list of blacklisted elements (which are 
 * specified by the user). All elements are checked by 
 * default, indicating that they are currently black-
 * listed. Unchecking an item's checkbox will remove 
 * that element from the blacklist.
 * 
 * @author Saravan Pantham
 *******************************************************/
public class BlacklistedElementsDialog extends DialogFragment { 
	
	//Temp array that holds the checkbox statuses in the ListView.
	private Common mApp;
	private static ArrayList<Boolean> checkboxStatuses = new ArrayList<Boolean>();
	private static String MANAGER_TYPE;
	private Cursor cursor;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mApp = (Common) getActivity().getApplicationContext();
		
		final BlacklistedElementsDialog dialog = this;
		MANAGER_TYPE = getArguments().getString("MANAGER_TYPE");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Get a cursor with a list of blacklisted elements.
        if (MANAGER_TYPE.equals("ARTISTS")) {
            builder.setTitle(R.string.blacklisted_artists);
        	cursor = mApp.getDBAccessHelper().getBlacklistedArtists();

        } else if (MANAGER_TYPE.equals("ALBUMS")) {
            builder.setTitle(R.string.blacklisted_albums);
        	cursor = mApp.getDBAccessHelper().getBlacklistedAlbums();
        	
        } else if (MANAGER_TYPE.equals("SONGS")) {
            builder.setTitle(R.string.blacklisted_songs);
        	cursor = mApp.getDBAccessHelper().getAllBlacklistedSongs();
        	
        } else if (MANAGER_TYPE.equals("PLAYLISTS")) {
            builder.setTitle(R.string.blacklisted_playlists);
        	/*DBAccessHelper playlistsDBHelper = new DBAccessHelper(getActivity());
        	cursor = playlistsDBHelper.getAllBlacklistedPlaylists();*/
        	
        } else {
        	Toast.makeText(getActivity(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        	return builder.create();
        }
        
        //Dismiss the dialog if there are no blacklisted elements.
        if (cursor.getCount()==0) {
        	Toast.makeText(getActivity(), R.string.no_blacklisted_items_found, Toast.LENGTH_LONG).show();
        	return builder.create();
        }

        //Loop through checkboxStatuses and insert "TRUE" at every position by default.
        for (int i=0; i < cursor.getCount(); i++) {
        	checkboxStatuses.add(true);
        }

        View rootView = this.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_blacklist_manager, null);
        TextView blacklistManagerInfoText = (TextView) rootView.findViewById(R.id.blacklist_manager_info_text);
        ListView blacklistManagerListView = (ListView) rootView.findViewById(R.id.blacklist_manager_list);
        
        blacklistManagerInfoText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        blacklistManagerInfoText.setPaintFlags(blacklistManagerInfoText.getPaintFlags()
        									   | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        blacklistManagerListView.setFastScrollEnabled(true);
        BlacklistedElementsAdapter adapter = new BlacklistedElementsAdapter(getActivity(), cursor);
        blacklistManagerListView.setAdapter(adapter);
        
        builder.setView(rootView);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialog.dismiss();
				
			}
        	
        });
        
        builder.setPositiveButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Loop through checkboxStatuses and unblacklist the elements that have been unchecked.
				for (int i=0; i < checkboxStatuses.size(); i++) {
					
					cursor.moveToPosition(i);
					if (checkboxStatuses.get(i)==true) {
						//The item is still blacklisted.
						continue;
					} else {
						//The item has been unblacklisted.
				        if (MANAGER_TYPE.equals("ARTISTS")) {
				        	mApp.getDBAccessHelper().setBlacklistForArtist(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)), false);
				        	
				        } else if (MANAGER_TYPE.equals("ALBUMS")) {
				        	mApp.getDBAccessHelper().setBlacklistForAlbum(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)), 
				        										cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)), 
				        										false);

				        } else if (MANAGER_TYPE.equals("SONGS")) {
				        	mApp.getDBAccessHelper().setBlacklistForSong(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)), false);

				        } else if (MANAGER_TYPE.equals("PLAYLISTS")) {
				        	/*DBAccessHelper playlistsDBHelper = new DBAccessHelper(getActivity());
				        	playlistsDBHelper.unBlacklistPlaylist(cursor.getString(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_BLACKLIST_STATUS)));*/

				        }
						
					}
					
				}
				
				dialog.dismiss();
				
			}
        	
        });
        
        return builder.create();
    }
	
	//Adapter subclass for the Blacklists ListView.
	static class BlacklistedElementsAdapter extends SimpleCursorAdapter {
		
		private Cursor mCursor = null;
		private String elementName = "";
		private String artistName = "";
		
	    public BlacklistedElementsAdapter(Context context, Cursor cursor) {
	        super(context, -1, null, new String[] {}, new int[] {}, 0);
	        mCursor = cursor;
	        
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
			mCursor.moveToPosition(position);
			if (MANAGER_TYPE.equals("ARTISTS")) {
				elementName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
			} else if (MANAGER_TYPE.equals("ALBUMS")) {
				elementName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ALBUM));
				artistName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
			} else if (MANAGER_TYPE.equals("SONGS")) {
				elementName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_TITLE));
				artistName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.SONG_ARTIST));
			} else if (MANAGER_TYPE.equals("PLAYLISTS")) {
				/*elementName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.PLAYLIST_NAME));
				artistName = mCursor.getString(mCursor.getColumnIndex(DBAccessHelper.NUMBER_OF_SONGS));*/
			}

			//Set element name.
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
	
	}
	 
	static class BlacklistManagerHolder {
	    public TextView blacklistedElementName;
	    public TextView blacklistedArtistName;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (this.isRemoving()) {
			cursor.close();
		}
		
	}

}
