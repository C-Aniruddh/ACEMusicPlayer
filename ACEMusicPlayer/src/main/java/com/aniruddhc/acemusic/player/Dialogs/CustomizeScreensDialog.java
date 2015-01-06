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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

@SuppressLint("DefaultLocale")
public class CustomizeScreensDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;
	private TextView customizeScreensText;
	private DragSortListView listView;
	public static CustomizeScreensListAdapter adapter;
	private ArrayList<String> screenTitlesList = new ArrayList<String>();
	private SharedPreferences sharedPreferences;
	
	String page1;
	String page2;
	String page3;
	String page4;
	String page5;
	String page6;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("customizeScreensDialog");
		
		sharedPreferences = parentActivity.getSharedPreferences("com.aniruddhc.acemusic.player", Context.MODE_PRIVATE);
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.customize_screens_layout, null);
		
		customizeScreensText = (TextView) rootView.findViewById(R.id.customize_screens_text);
		customizeScreensText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		customizeScreensText.setPaintFlags(customizeScreensText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		//Populate the arraylists with the settings saved in SharedPreferences.
		page1 = sharedPreferences.getString("PAGE_1", getResources().getString(R.string.artists_caps));
		page2 = sharedPreferences.getString("PAGE_2", getResources().getString(R.string.albums_caps));
		page3 = sharedPreferences.getString("PAGE_3", getResources().getString(R.string.songs_caps));
		page4 = sharedPreferences.getString("PAGE_4", getResources().getString(R.string.playlists_caps));
		page5 = sharedPreferences.getString("PAGE_5", getResources().getString(R.string.genres_caps));
		page6 = sharedPreferences.getString("PAGE_6", getResources().getString(R.string.folders_caps));
		
		if (!page1.equals("null") || !page1.equals(null)) {
			screenTitlesList.add(page1);
		}
		
		if (!page2.equals("null") || !page2.equals(null)) {
			screenTitlesList.add(page2);
		}
		
		if (!page3.equals("null") || !page3.equals(null)) {
			screenTitlesList.add(page3);
		}
		
		if (!page4.equals("null") || !page4.equals(null)) {
			screenTitlesList.add(page4);
		}
		
		if (!page5.equals("null") || !page5.equals(null)) {
			screenTitlesList.add(page5);
		}
		
		if (!page6.equals("null") || !page6.equals(null)) {
			screenTitlesList.add(page6);
		}

		listView = (DragSortListView) rootView.findViewById(R.id.customize_screens_listview);
		adapter = new CustomizeScreensListAdapter(parentActivity, screenTitlesList);
		listView.setAdapter(adapter);
		listView.setDropListener(onDrop);
		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(listView);
		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
		listView.setFloatViewManager(simpleFloatViewManager);
			
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.customize_screens);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.done, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				
				//adapter.getItem(i) will get us the order for the screens.
				sharedPreferences.edit().putString("PAGE_1", adapter.getItem(0).toString().toUpperCase()).commit();
				sharedPreferences.edit().putString("PAGE_2", adapter.getItem(1).toString().toUpperCase()).commit();
				sharedPreferences.edit().putString("PAGE_3", adapter.getItem(2).toString().toUpperCase()).commit();
				sharedPreferences.edit().putString("PAGE_4", adapter.getItem(3).toString().toUpperCase()).commit();
				sharedPreferences.edit().putString("PAGE_5", adapter.getItem(4).toString().toUpperCase()).commit();
				sharedPreferences.edit().putString("PAGE_6", adapter.getItem(5).toString().toUpperCase()).commit();
				
				Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
				
				//Restart the app.
				Intent i = parentActivity.getBaseContext()
										 .getPackageManager()
										 .getLaunchIntentForPackage(parentActivity.getBaseContext()
												 								  .getPackageName());
				
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				dialogFragment.dismiss();
				getActivity().finish();
				startActivity(i);
				
			}
        	
        });

        return builder.create();
    }
	
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
    	
        @Override
        public void drop(int from, int to) {
            if (from!=to) {
                String item = adapter.getItem(from);
                adapter.remove(item);
                adapter.insert(item, to);
                
            }
            
        }
        
    };
    
    public static class CustomizeScreensListAdapter extends ArrayAdapter<String> {

    	private Context mContext;
    	private ArrayList<String> mScreensList;
    	
    	private TextView screenTitle;
       
        public CustomizeScreensListAdapter(Context context, ArrayList<String> screensList) {
        	
        	super(context, R.id.customize_screens_title, screensList);
        	
        	mContext = context;
        	mScreensList = screensList;

        }
        
        public View getView(final int position, View convertView, ViewGroup parent){

    		View v = convertView;

    		if (v == null) {
    			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = inflater.inflate(R.layout.customize_screens_listview_layout, null);
    		}
    		
    		screenTitle = (TextView) v.findViewById(R.id.customize_screens_title);

    		screenTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
    		screenTitle.setPaintFlags(screenTitle.getPaintFlags()
    										 | Paint.ANTI_ALIAS_FLAG
    										 | Paint.SUBPIXEL_TEXT_FLAG);		
    		
    		screenTitle.setText(mScreensList.get(position));
    		
    		return v;

    	}
       
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().finish();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().finish();
		
	}
	
}
