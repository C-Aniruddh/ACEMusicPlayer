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
package com.aniruddhc.acemusic.player.BrowserSubListActivity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import java.util.HashMap;

/**
 * Generic ListView adapter for ListViewFragment.
 * 
 * @author Saravan Pantham
 */
public class BrowserSubListAdapter extends SimpleCursorAdapter implements Scrollable {

	private Context mContext;
	private Common mApp;
	private BrowserSubListActivity mActivity;
    public static ListViewHolder mHolder = null;
    private String mName = "";

    //HashMap for DB column names.
    private HashMap<Integer, String> mDBColumnsMap;
    public static final int TITLE_TEXT = 0;
    public static final int SOURCE = 1;
    public static final int FILE_PATH = 2;
    public static final int ARTWORK_PATH = 3;
    public static final int FIELD_1 = 4; //Empty fields for other
    public static final int FIELD_2 = 5;
    public static final int FIELD_3 = 6;
    public static final int FIELD_4 = 7;
    public static final int FIELD_5 = 8;

    public BrowserSubListAdapter(Context context, BrowserSubListActivity activity,
                                 HashMap<Integer, String> dbColumnsMap) {
    	
        super(context, -1, activity.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mActivity = activity;
        mApp = (Common) mContext.getApplicationContext();
        mDBColumnsMap = dbColumnsMap;
        
    }
    
    /**
     * Quick scroll indicator implementation.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String title = c.getString(c.getColumnIndex(mDBColumnsMap.get(TITLE_TEXT)));
    	if (title!=null && title.length() > 1)
    		return "  " + title.substring(0, 1) + "  ";
        else
            return "  N/A  ";
    }
    
    /**
     * Returns the current position of the top view in the list/grid.
     */
	@Override
	public int getScrollPosition(int childPosition, int groupPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	/**
	 * Returns the individual row/child in the list/grid.
	 */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_sub_browser_item, parent, false);
			
			mHolder = new ListViewHolder();
			mHolder.trackNumber = (TextView) convertView.findViewById(R.id.listViewTrackNumber);
			mHolder.titleText = (TextView) convertView.findViewById(R.id.listViewTitleText);
			mHolder.subText = (TextView) convertView.findViewById(R.id.listViewSubText);
			mHolder.rightSubText = (TextView) convertView.findViewById(R.id.listViewRightSubText);
            mHolder.overflowIcon = (ImageButton) convertView.findViewById(R.id.listViewOverflow);

			mHolder.titleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
            mHolder.subText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
            mHolder.rightSubText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
            mHolder.trackNumber.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
			
			mHolder.titleText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			mHolder.subText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			mHolder.rightSubText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
            mHolder.trackNumber.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

            mHolder.overflowIcon.setImageResource(UIElementsHelper.getIcon(mContext, "ic_action_overflow"));
			mHolder.overflowIcon.setOnClickListener(overflowClickListener);
			mHolder.overflowIcon.setFocusable(false);
			mHolder.overflowIcon.setFocusableInTouchMode(false);
			
			convertView.setTag(mHolder);
		} else {
		    mHolder = (ListViewHolder) convertView.getTag();
		}

		//Retrieve data from the cursor.
		String titleText = "";
		String source = "";
		String filePath = "";
		String artworkPath = "";
		String field1 = "";
		String field2 = "";
		String field3 = "";
		String field4 = "";
		String field5 = "";
		try {
			titleText = c.getString(c.getColumnIndex(mDBColumnsMap.get(TITLE_TEXT)));
			source = c.getString(c.getColumnIndex(mDBColumnsMap.get(SOURCE)));
			filePath = c.getString(c.getColumnIndex(mDBColumnsMap.get(FILE_PATH)));
			artworkPath = c.getString(c.getColumnIndex(mDBColumnsMap.get(ARTWORK_PATH)));
			field1 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_1)));
			field2 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_2)));
			field3 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_3)));
			field4 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_4)));
			field5 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_5)));
			
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
		
		//Set the tags for this grid item.
		convertView.setTag(R.string.title_text, titleText);
		convertView.setTag(R.string.song_source, source);
		convertView.setTag(R.string.song_file_path, filePath);
		convertView.setTag(R.string.album_art, artworkPath);
		convertView.setTag(R.string.field_1, field1);
		convertView.setTag(R.string.field_2, field2);
		convertView.setTag(R.string.field_3, field3);
		convertView.setTag(R.string.field_4, field4);
		convertView.setTag(R.string.field_5, field5);
		
		//Set the tags for this list item's overflow button.
		mHolder.overflowIcon.setTag(R.string.title_text, titleText);
		mHolder.overflowIcon.setTag(R.string.source, source);
		mHolder.overflowIcon.setTag(R.string.file_path, filePath);
		mHolder.overflowIcon.setTag(R.string.field_1, field1);
		mHolder.overflowIcon.setTag(R.string.field_2, field2);
		mHolder.overflowIcon.setTag(R.string.field_3, field3);
		mHolder.overflowIcon.setTag(R.string.field_4, field4);
		mHolder.overflowIcon.setTag(R.string.field_5, field5);
		
		//Set the title text in the ListView.
		mHolder.titleText.setText(titleText);
		mHolder.subText.setText(field2);
		mHolder.rightSubText.setText(field1);
        mHolder.trackNumber.setText(field3);
		
		return convertView;
	}

    
    /**
     * Click listener for overflow button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.artist_overflow_menu);
			//menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mName = (String) v.getTag(R.string.artist);
		    menu.show();
			
		}
    	
    };
    
    /**
     * Menu item click listener for the pop up menu.
     *//*
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			switch(item.getItemId()) {
        	case R.id.edit_artist_tags:
        		//Edit Artist Tags.
        		if (mApp.getSharedPreferences().getBoolean("SHOW_ARTIST_EDIT_CAUTION", true)==true) {
            		FragmentTransaction transaction = mListViewFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ARTIST");
            		bundle.putString("ARTIST", mName);
            		CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
    				FragmentTransaction ft = mListViewFragment.getFragmentManager().beginTransaction();
    				Bundle bundle = new Bundle();
    				bundle.putString("EDIT_TYPE", "ARTIST");
    				bundle.putString("ARTIST", mName);
    				ID3sArtistEditorDialog dialog = new ID3sArtistEditorDialog();
    				dialog.setArguments(bundle);
    				dialog.show(ft, "id3ArtistEditorDialog");
        		}
        		break;
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext,
        														   mListViewFragment,
        														   "ARTIST",
        														   mName, 
        														   null,
        														   null, 
        														   null, 
        														   null,
        														   null,
        														   null);
        		task.execute();
        		break;
        	case R.id.play_next:
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
																		   mListViewFragment,
																		   "ARTIST",
																		   mName, 
																		   null,
																		   null, 
																		   null, 
																		   null,
																		   null,
																		   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mListViewFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ARTIST");
				bundle.putString("ARTIST", mName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
				break;
        	case R.id.blacklist_artist:
        		//Blacklist Artist
        		mApp.getDBAccessHelper().setBlacklistForArtist(mName, true);
        		Toast.makeText(mContext, R.string.artist_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the ListView.
        		mListViewFragment.mHandler.post(mListViewFragment.queryRunnable);
        		mListViewFragment.getListViewAdapter().notifyDataSetChanged();

        		break;
        	
			}
			
			return false;
		}
    	
    };*/

    /**
     * Holder subclass for ListViewCardsAdapter.
     * 
     * @author Saravan Pantham
     */
	static class ListViewHolder {
	    public TextView trackNumber;
	    public TextView titleText;
	    public TextView subText;
	    public TextView rightSubText;
	    public ImageButton overflowIcon;

	}
	
}
