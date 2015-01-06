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
package com.aniruddhc.acemusic.player.BrowserSubGridActivity;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

import java.util.HashMap;

/**
 * Generic GridView adapter for BrowserSubGridActivity.
 * 
 * @author Saravan Pantham
 */
public class BrowserSubGridAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private Common mApp;
	private BrowserSubGridActivity mActivity;
    public static GridViewHolder mHolder = null;
    private String mName = "";
    private int mWidth;
    private int mHeight;
    
    //HashMap for DB column names.
    private HashMap<Integer, String> mDBColumnsMap;
    public static final int TITLE_TEXT = 0;
    public static final int SOURCE = 1;
    public static final int FILE_PATH = 2;
    public static final int ARTWORK_PATH = 3;
    public static final int FIELD_1 = 4; //Empty fields for other parameters.
    public static final int FIELD_2 = 5;
    public static final int FIELD_3 = 6;
    public static final int FIELD_4 = 7;
    public static final int FIELD_5 = 8;
    
    public BrowserSubGridAdapter(Context context, BrowserSubGridActivity activity,
                                 HashMap<Integer, String> dbColumnsMap) {
    	
        super(context, -1, activity.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mActivity = activity;
        mApp = (Common) mContext.getApplicationContext();
        mDBColumnsMap = dbColumnsMap;

        //Calculate the height and width of each item image.
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        if (mApp.isTabletInPortrait()) {
            //3 column layout.
            mWidth = (metrics.widthPixels)/3;
            mHeight = mWidth + (mWidth/4);
        } else if (mApp.isPhoneInLandscape() || mApp.isTabletInLandscape()) {
            //4 column layout.
            mWidth = (metrics.widthPixels)/4;
            mHeight = mWidth + (mWidth/5);
        } else {
            //2 column layout.
            mWidth = (metrics.widthPixels)/2;
            mHeight = mWidth + (mWidth/3);
        }

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

		if (convertView==null) {
			mHolder = new GridViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_view_item, parent, false);

            mHolder.background = (RelativeLayout) convertView.findViewById(R.id.gridViewItemLayout);
            mHolder.textLayout = (RelativeLayout) convertView.findViewById(R.id.gridViewTextLayout);
			mHolder.gridViewArt = (ImageView) convertView.findViewById(R.id.gridViewImage);
			mHolder.titleText = (TextView) convertView.findViewById(R.id.gridViewTitleText);
            mHolder.subText = (TextView) convertView.findViewById(R.id.gridViewSubText);

			mHolder.overflowButton = (ImageButton) convertView.findViewById(R.id.gridViewOverflowButton);
            mHolder.overflowButton.setImageResource(UIElementsHelper.getIcon(mContext, "ic_action_overflow"));
			mHolder.overflowButton.setOnClickListener(overflowClickListener);
			mHolder.overflowButton.setFocusable(false);
			mHolder.overflowButton.setFocusableInTouchMode(false);

			mHolder.titleText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
            mHolder.subText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			
	        mHolder.gridViewArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mHolder.gridViewArt.setImageResource(UIElementsHelper.getEmptyColorPatch(mContext));
            //mHolder.textLayout.setBackgroundColor(UIElementsHelper.getGridViewBackground(mContext));
            //mHolder.overflowButton.setBackgroundColor(UIElementsHelper.getGridViewBackground(mContext));
            mHolder.titleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
            mHolder.subText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));

            //Apply the ImageView's dimensions.
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.gridViewArt.getLayoutParams();
            params.width = mWidth;
            params.height = mWidth;
            mHolder.gridViewArt.setLayoutParams(params);

            //Apply the card's background.
            mHolder.background.setBackgroundResource(UIElementsHelper.getGridViewCardBackground(mContext));
			
			convertView.setTag(mHolder);
		} else {
		    mHolder = (GridViewHolder) convertView.getTag();
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
		
		/*//Set the tags for this grid item's overflow button.
		mHolder.overflowButton.setTag(R.string.title_text, titleText);
		mHolder.overflowButton.setTag(R.string.source, source);
		mHolder.overflowButton.setTag(R.string.file_path, filePath);
		mHolder.overflowButton.setTag(R.string.field_1, field1);
		mHolder.overflowButton.setTag(R.string.field_2, field2);
		mHolder.overflowButton.setTag(R.string.field_3, field3);
		mHolder.overflowButton.setTag(R.string.field_4, field4);
		mHolder.overflowButton.setTag(R.string.field_5, field5);*/
		
		//Set the title text in the GridView.
		mHolder.titleText.setText(titleText);
        mHolder.subText.setText(field1);

		//Load the album art.
        mApp.getPicasso().load(artworkPath)
                         .placeholder(UIElementsHelper.getEmptyColorPatch(mContext))
                         .into(mHolder.gridViewArt);

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
    
   /* *//**
     * Menu item click listener for the pop up menu.
     *//*
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			switch(item.getItemId()) {
        	case R.id.edit_artist_tags:
        		//Edit Artist Tags.
        		if (mApp.getSharedPreferences().getBoolean("SHOW_ARTIST_EDIT_CAUTION", true)==true) {
            		FragmentTransaction transaction = mGridViewFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ARTIST");
            		bundle.putString("ARTIST", mName);
            		CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
    				FragmentTransaction ft = mGridViewFragment.getFragmentManager().beginTransaction();
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
        														   mGridViewFragment,
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
																		   mGridViewFragment,
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
        		FragmentTransaction ft = mGridViewFragment.getFragmentManager().beginTransaction();
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
        		
        		//Update the GridView.
        		mGridViewFragment.mHandler.post(mGridViewFragment.queryRunnable);
        		mGridViewFragment.getGridViewAdapter().notifyDataSetChanged();

        		break;
        	
			}
			
			return false;
		}
    	
    };*/

    /**
     * Holder subclass for GridViewAdapter.
     * 
     * @author Saravan Pantham
     */
	public static class GridViewHolder {
	    public ImageView gridViewArt;
	    public TextView titleText;
        public TextView subText;
	    public RelativeLayout background;
	    public RelativeLayout textLayout;
        public ImageButton overflowButton;

	}
	
}
