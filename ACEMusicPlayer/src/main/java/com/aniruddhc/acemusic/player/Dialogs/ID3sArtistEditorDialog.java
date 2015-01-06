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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;

public class ID3sArtistEditorDialog extends DialogFragment {

	private Context mContext;
	private static Activity parentActivity;
	private DialogFragment dialogFragment;
	private View rootView;
	
	private TextView titleText;
	private TextView artistText;
	private TextView albumText;
	private TextView albumArtistText;
	private TextView genreText;
	private TextView producerText;
	private TextView yearText;
	private TextView trackText;
	private TextView ofText;
	private TextView commentsText;

	private EditText titleEditText;
	private EditText artistEditText;
	private EditText albumEditText;
	private EditText albumArtistEditText;
	private EditText genreEditText;
	private EditText producerEditText;
	private EditText yearEditText;
	private EditText trackEditText;
	private EditText trackTotalEditText;
	private EditText commentsEditText;
	
	private CheckBox titleCheckbox;
	private CheckBox artistCheckbox;
	private CheckBox albumCheckbox;
	private CheckBox albumArtistCheckbox;
	private CheckBox genreCheckbox;
	private CheckBox producerCheckbox;
	private CheckBox yearCheckbox;
	private CheckBox trackCheckbox;
	private CheckBox commentCheckbox;
	
	private boolean titleEdited = false;
	private boolean artistEdited = false;
	private boolean albumEdited = false;
	private boolean albumArtistEdited = false;
	private boolean genreEdited = false;
	private boolean producerEdited = false;
	private boolean yearEdited = false;
	private boolean trackEdited = false;
	private boolean commentEdited = false;
	
	private String ARTIST;
	
	private ArrayList<String> titlesList = new ArrayList<String>();
	private ArrayList<String> artistsList = new ArrayList<String>();
	private ArrayList<String> albumsList = new ArrayList<String>();
	private ArrayList<String> albumArtistsList = new ArrayList<String>();
	private ArrayList<String> genresList = new ArrayList<String>();
	private ArrayList<String> producersList = new ArrayList<String>();
	private ArrayList<String> yearsList = new ArrayList<String>();
	private ArrayList<String> trackNumbersList = new ArrayList<String>();
	private ArrayList<String> totalTracksList = new ArrayList<String>();
	private ArrayList<String> commentsList = new ArrayList<String>();
	private ArrayList<String> songURIsList = new ArrayList<String>();
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mContext = getActivity();
		parentActivity = getActivity();
		dialogFragment = this;
		
		//Get the artist name.
		ARTIST = getArguments().getString("ARTIST");
		
		rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.fragment_edit_id3_artist_album_dialog, null);
		
		titleText = (TextView) rootView.findViewById(R.id.edit_title_text);
		artistText = (TextView) rootView.findViewById(R.id.edit_artist_text);
		albumText = (TextView) rootView.findViewById(R.id.edit_album_text);
		albumArtistText = (TextView) rootView.findViewById(R.id.edit_album_artist_text);
		genreText = (TextView) rootView.findViewById(R.id.edit_genre_text);
		producerText = (TextView) rootView.findViewById(R.id.edit_producer_text);
		yearText = (TextView) rootView.findViewById(R.id.edit_year_text);
		trackText = (TextView) rootView.findViewById(R.id.edit_track_text);
		ofText = (TextView) rootView.findViewById(R.id.text_of);
		commentsText = (TextView) rootView.findViewById(R.id.edit_comment_text);
		
		titleEditText = (EditText) rootView.findViewById(R.id.edit_title_field);
		artistEditText = (EditText) rootView.findViewById(R.id.edit_artist_field);
		albumEditText = (EditText) rootView.findViewById(R.id.edit_album_field);
		albumArtistEditText = (EditText) rootView.findViewById(R.id.edit_album_artist_field);
		genreEditText = (EditText) rootView.findViewById(R.id.edit_genre_field);
		producerEditText = (EditText) rootView.findViewById(R.id.edit_producer_field);
		yearEditText = (EditText) rootView.findViewById(R.id.edit_year_field);
		trackEditText = (EditText) rootView.findViewById(R.id.edit_track_field);
		trackTotalEditText = (EditText) rootView.findViewById(R.id.edit_track_total_field);
		commentsEditText = (EditText) rootView.findViewById(R.id.edit_comment_field);
		
		titleCheckbox = (CheckBox) rootView.findViewById(R.id.title_checkbox);
		artistCheckbox = (CheckBox) rootView.findViewById(R.id.artist_checkbox);
		albumCheckbox = (CheckBox) rootView.findViewById(R.id.album_checkbox);
		albumArtistCheckbox = (CheckBox) rootView.findViewById(R.id.album_artist_checkbox);
		genreCheckbox = (CheckBox) rootView.findViewById(R.id.genre_checkbox);
		producerCheckbox = (CheckBox) rootView.findViewById(R.id.producer_checkbox);
		yearCheckbox = (CheckBox) rootView.findViewById(R.id.year_checkbox);
		trackCheckbox = (CheckBox) rootView.findViewById(R.id.track_checkbox);
		commentCheckbox = (CheckBox) rootView.findViewById(R.id.comment_checkbox);
		
		titleText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		artistText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		albumText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		albumArtistText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		genreText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		producerText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		yearText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		trackText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		ofText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		commentsText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		
		titleText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		artistText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		albumText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		albumArtistText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		genreText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		producerText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		yearText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		trackText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		ofText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		commentsText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		titleEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		artistEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		albumEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		albumArtistEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		genreEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		producerEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		yearEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		trackEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		trackTotalEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		commentsEditText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
		
		titleEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		artistEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		albumEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		albumArtistEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		genreEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		producerEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		yearEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		trackEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		trackTotalEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		commentsEditText.setPaintFlags(titleText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		
		//Keep all the fields locked by default.
		titleCheckbox.setChecked(false);
		artistCheckbox.setChecked(false);
		albumCheckbox.setChecked(false);
		albumArtistCheckbox.setChecked(false);
		genreCheckbox.setChecked(false);
		producerCheckbox.setChecked(false);
		yearCheckbox.setChecked(false);
		trackCheckbox.setChecked(false);
		commentCheckbox.setChecked(false);
		
		//Disable all EditTexts by default.
		titleEditText.setEnabled(false);
		artistEditText.setEnabled(false);
		albumEditText.setEnabled(false);
		albumArtistEditText.setEnabled(false);
		genreEditText.setEnabled(false);
		producerEditText.setEnabled(false);
		yearEditText.setEnabled(false);
		trackEditText.setEnabled(false);
		commentsEditText.setEnabled(false);
		
		//Register click registers on each checkbox.
		titleCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {

				if (checked==true) {
					titleEdited = true;
					titleEditText.setEnabled(true);
				} else {
					titleEdited = false;
					titleEditText.setEnabled(false);
				}
				
			}
			
		});
		
		artistCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					artistEdited = true;
					artistEditText.setEnabled(true);;
				} else {
					artistEdited = false;
					artistEditText.setEnabled(false);
				}
				
			}
			
		});
		
		albumArtistCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					albumEdited = true;
					albumEditText.setEnabled(true);;
				} else {
					albumEdited = false;
					albumEditText.setEnabled(false);
				}
				
			}
			
		});
		
		albumCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					albumArtistEdited = true;
					albumArtistEditText.setEnabled(true);;
				} else {
					albumArtistEdited = false;
					albumArtistEditText.setEnabled(false);
				}
				
			}
			
		});
		
		genreCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					genreEdited = true;
					genreEditText.setEnabled(true);;
				} else {
					genreEdited = false;
					genreEditText.setEnabled(false);
				}
				
			}
			
		});
		
		producerCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					producerEdited = true;
					producerEditText.setEnabled(true);;
				} else {
					producerEdited = false;
					producerEditText.setEnabled(false);
				}
				
			}
			
		});
		
		yearCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					yearEdited = true;
					yearEditText.setEnabled(true);;
				} else {
					yearEdited = false;
					yearEditText.setEnabled(false);
				}
				
			}
			
		});
		
		trackCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					trackEdited = true;
					trackEditText.setEnabled(true);;
				} else {
					trackEdited = false;
					trackEditText.setEnabled(false);
				}
				
			}
			
		});
		
		commentCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton checkbox, boolean checked) {
				
				if (checked==true) {
					commentEdited = true;
					commentsEditText.setEnabled(true);;
				} else {
					commentEdited = false;
					commentsEditText.setEnabled(false);
				}
				
			}
			
		});

		if (ARTIST!=null) {
			songURIsList = getAllSongsByArtist(ARTIST);
			
			//Populate the ArrayLists with the song tags.
			try {
				getSongTags(songURIsList);
			} catch (CannotReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadOnlyFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAudioFrameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Now check if any of the ArrayLists contain the same exact elements.
			boolean titlesSame = checkIfAllElementsEqual(titlesList);
			boolean artistsSame = checkIfAllElementsEqual(artistsList);
			boolean albumsSame = checkIfAllElementsEqual(albumsList);
			boolean albumArtistsSame = checkIfAllElementsEqual(albumArtistsList);
			boolean genresSame = checkIfAllElementsEqual(genresList);
			boolean producersSame = checkIfAllElementsEqual(producersList);
			boolean yearsSame = checkIfAllElementsEqual(yearsList);
			boolean tracksSame = checkIfAllElementsEqual(trackNumbersList);
			boolean totalTracksSame = checkIfAllElementsEqual(totalTracksList);
			boolean commentsSame = checkIfAllElementsEqual(commentsList);
			
			//Populate the EditTexts.
			setEditorFields(titlesSame, titlesList, titleEditText);
			setEditorFields(artistsSame, artistsList, artistEditText);
			setEditorFields(albumsSame, albumsList, albumEditText);
			setEditorFields(albumArtistsSame, albumArtistsList, albumArtistEditText);
			setEditorFields(genresSame, genresList, genreEditText);
			setEditorFields(producersSame, producersList, producerEditText);
			setEditorFields(yearsSame, yearsList, yearEditText);
			setEditorFields(tracksSame, trackNumbersList, trackEditText);
			setEditorFields(totalTracksSame, totalTracksList, trackTotalEditText);
			setEditorFields(commentsSame, commentsList, commentsEditText);
			
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.edit_tags);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.save, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				AsyncSaveArtistTagsTask asyncSaveArtistTagsTask = new AsyncSaveArtistTagsTask(getActivity(), getActivity());
				asyncSaveArtistTagsTask.execute();
        	
			}
	        
        });
        
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
        	
        });

        return builder.create();
			
	}
	
	public static ArrayList<String> getAllSongsInAlbum(String albumName, String artistName) {
		ArrayList<String> songURIsList = new ArrayList<String>();
		
		DBAccessHelper dbHelper = new DBAccessHelper(parentActivity);
		
		//Escape any rogue apostrophes.
		if (albumName.contains("'")) {
			albumName = albumName.replace("'", "''");
		}
		
		if (artistName.contains("'")) {
			artistName = artistName.replace("'", "''");
		}
		
		String selection = DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName + "'" + " AND "
						 + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'" + " AND "
						 + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
		
		String[] projection = { DBAccessHelper._ID, DBAccessHelper.SONG_FILE_PATH };
		
		Cursor cursor = dbHelper.getWritableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
															 projection,
															 selection, 
															 null, 
															 null, 
															 null, 
															 null);
		
		cursor.moveToFirst();
		
		if (cursor.getCount()!=0) {
			
			songURIsList.add(cursor.getString(1));
			
			while (cursor.moveToNext()) {
				songURIsList.add(cursor.getString(1));
				
			}
			
		}
		
		cursor.close();
		
		return songURIsList;
		
	}
	
	public static ArrayList<String> getAllSongsByArtist(String artistName) {
		ArrayList<String> songURIsList = new ArrayList<String>();
		
		DBAccessHelper dbHelper = new DBAccessHelper(parentActivity);
		
		//Escape any rogue apostrophes.
		if (artistName.contains("'")) {
			artistName = artistName.replace("'", "''");
		}
		
		String selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'"
						 + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
		
		String[] projection = { DBAccessHelper._ID, DBAccessHelper.SONG_FILE_PATH };
		
		Cursor cursor = dbHelper.getWritableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
															 projection,
															 selection, 
															 null, 
															 null, 
															 null, 
															 null);
		
		cursor.moveToFirst();
		
		if (cursor.getCount()!=0) {
			
			songURIsList.add(cursor.getString(1));
			
			while (cursor.moveToNext()) {
				songURIsList.add(cursor.getString(1));
				
			}
			
		}
		
		cursor.close();
		
		return songURIsList;
		
	}
	
	//This method loops through all the songs and saves their tags into ArrayLists.
	public void getSongTags(ArrayList<String> dataURIsList) throws CannotReadException, 
																   IOException, 
																   TagException, 
																   ReadOnlyFileException, 
																   InvalidAudioFrameException {
		
		for (int i=0; i < dataURIsList.size(); i++) {
			
			try {
				File file = new File(dataURIsList.get(i));
				AudioFile audioFile = AudioFileIO.read(file);
				
				titlesList.add(audioFile.getTag().getFirst(FieldKey.TITLE));
				artistsList.add(audioFile.getTag().getFirst(FieldKey.ARTIST));
				albumsList.add(audioFile.getTag().getFirst(FieldKey.ALBUM));
				albumArtistsList.add(audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST));
				genresList.add(audioFile.getTag().getFirst(FieldKey.GENRE));
				producersList.add(audioFile.getTag().getFirst(FieldKey.PRODUCER));
				yearsList.add(audioFile.getTag().getFirst(FieldKey.YEAR));
				trackNumbersList.add(audioFile.getTag().getFirst(FieldKey.TRACK));
				totalTracksList.add(audioFile.getTag().getFirst(FieldKey.TRACK_TOTAL));
				commentsList.add(audioFile.getTag().getFirst(FieldKey.COMMENT));
				
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
		
	}
	
	//This method goes through the inputted ArrayList and checks if all its elements are the same.
	public static boolean checkIfAllElementsEqual(ArrayList<String> list) {
		
		if (list.size()!=0) {
			String firstElement = list.get(0);
			
			for (int i=0; i < list.size(); i++) {
				if (!firstElement.equals(list.get(i))) {
					return false;
				}
				
			}
			
			return true;
		}
		
		return false;
		
	}
	
	//This method sets the specified EditText values based on the boolean parameter.
	public static void setEditorFields(boolean allElementsSame, ArrayList<String> list, EditText editText) {
		
		if (allElementsSame==true) {
			editText.setText(list.get(0));
		} else {
			editText.setText(R.string.varies_by_song);
		}
		
	}
	
	class AsyncSaveArtistTagsTask extends AsyncTask<String, String, String> {

		private Context mContext;
		private Activity mActivity;
		private ProgressDialog pd;
		private int i = 0;
		
		private String songTitle;
		private String songArtist;
		private String songAlbum;
		private String songAlbumArtist;
		private String songComposer;
		private String songProducer;
		private String songTrackNumber;
		private String songTrackTotals;
		private String songComments;
		private String songYear;
		
		public AsyncSaveArtistTagsTask(Context context, Activity activity) {
			mContext = context;
			mActivity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pd = new ProgressDialog(mActivity);
			pd.setTitle(R.string.saving_artist_info);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(false);
			pd.setMax(songURIsList.size());
			pd.show();
			
			//Retrieve the strings from the EditText fields.
			if (titleEdited) {
				songTitle = titleEditText.getText().toString();	
				songTitle = songTitle.replace("'", "''");
			} else {
				songTitle = null;
			}
			
			if (artistEdited) {
				songArtist = artistEditText.getText().toString();
				songArtist = songArtist.replace("'", "''");
			} else {
				songArtist = null;
			}
			
			if (albumEdited) {
				songAlbum = albumEditText.getText().toString();
				songAlbum = songAlbum.replace("'", "''");
			} else {
				songAlbum = null;
			}
			
			if (albumArtistEdited) {
				songAlbumArtist = albumArtistEditText.getText().toString();
				songAlbumArtist = songAlbumArtist.replace("'", "''");
			} else {
				songAlbumArtist = null;
			}
			
			if (genreEdited) {
				songComposer = genreEditText.getText().toString();
				songComposer = songComposer.replace("'", "''");
			} else {
				songComposer = null;
			}
			
			if (producerEdited) {
				songProducer = producerEditText.getText().toString();
				songProducer = songProducer.replace("'", "''");
			} else {
				songProducer = null;
			}
			
			if (trackEdited) {
				songTrackNumber = trackEditText.getText().toString();
				songTrackNumber = songTrackNumber.replace("'", "''");
				songTrackTotals = trackTotalEditText.getText().toString();
				songTrackTotals = songTrackTotals.replace("'", "''");
			} else {
				songTrackNumber = null;
				songTrackTotals = null;
			}
			
			if (commentEdited) {
				songComments = commentsEditText.getText().toString();
				songComments = songComments.replace("'", "''");
			} else {
				songComments = null;
			}
			
			if (yearEdited) {
				songYear = yearEditText.getText().toString();
				songYear = songYear.replace("'", "''");
			} else {
				songYear = null;
			}
			
		}
		
		@Override
		protected String doInBackground(String... arg0) {

			//Create DB instances.
			DBAccessHelper dbHelper = new DBAccessHelper(mContext.getApplicationContext());
			
			for (i=0; i < songURIsList.size(); i++) {
				publishProgress(new String[] {});
				File file = null;
				try {
					file = new File(songURIsList.get(i));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				AudioFile audioFile = null;
				
				try {
					audioFile = AudioFileIO.read(file);
				} catch (CannotReadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TagException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Escape any rogue apostrophes.
				String uri = songURIsList.get(i);
				
				if (uri!=null) {
					if (uri.contains("'")) {
						uri = uri.replace("'", "''");
					}
					
				} else {
					continue;
				}

				Tag tag = audioFile.getTag();
				
				if (tag!=null) {
					String whereClause = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + uri + "'";
					ContentValues values = new ContentValues();
					
					if (titleEdited==false) {
						//Don't do anything here. The user didn't change the title.
					} else {
						try {
							tag.setField(FieldKey.TITLE, songTitle);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_TITLE, songTitle);
						
					}
					
					if (albumEdited==false) {
						//Don't do anything here. The user didn't change the album.
					} else {
						try {
							tag.setField(FieldKey.ALBUM, songAlbum);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
						
					}
					
					if (artistEdited==false) {
						//Don't do anything here. The user didn't change the artist.
					} else {
						try {
							tag.setField(FieldKey.ARTIST, songArtist);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_ARTIST, songArtist);
						
					}
					
					if (albumArtistEdited==false) {
						//Don't do anything here. The user didn't change the album artist.
					} else {
						try {
							tag.setField(FieldKey.ALBUM_ARTIST, songAlbumArtist);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
						
					}
					
					if (genreEdited==false) {
						//Don't do anything here. The user didn't change the genre.
					} else {
						try {
							tag.setField(FieldKey.GENRE, songComposer);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					if (producerEdited==false) {
						//Don't do anything here. The user didn't change the producer.
					} else {
						try {
							tag.setField(FieldKey.PRODUCER, songProducer);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					if (yearEdited==false) {
						//Don't do anything here. The user didn't change the year.
					} else {
						try {
							tag.setField(FieldKey.YEAR, songYear);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_YEAR, songYear);
						
					}
					
					if (trackEdited==false) {
						//Don't do anything here. The user didn't change the track number.
					} else {
						try {
							tag.setField(FieldKey.TRACK, songTrackNumber);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
						
					}
					
					try {
						tag.setField(FieldKey.TRACK_TOTAL, songTrackTotals);
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FieldDataInvalidException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchElementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (commentEdited==false) {
						//Don't do anything here. The user didn't change the comments.
					} else {
						try {
							tag.setField(FieldKey.COMMENT, songComments);
						} catch (KeyNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					try {
						audioFile.commit();
					} catch (CannotWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Write the values to the DB.
					try {
						dbHelper.getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
															  values, 
															  whereClause, 
															  null);

						dbHelper.close();
						dbHelper = null;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					Toast.makeText(mContext, R.string.unable_to_edit_artist_tags, Toast.LENGTH_SHORT).show();
				}
				
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			pd.setProgress(i);
			String message = mContext.getResources().getString(R.string.saving_song_info_for) + " " + titlesList.get(i) + ".";
			pd.setMessage(message);
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pd.dismiss();
			
			try {

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Delete all the contents of the ArrayLists.
			clearArrayLists();
			dialogFragment.dismiss();
			Toast.makeText(parentActivity, R.string.changes_saved, Toast.LENGTH_SHORT).show();
			
		}
		
	}


	public void clearArrayLists() {
		if (titlesList!=null) {
			titlesList.clear();
			titlesList = null;
		}
		
		if (artistsList!=null) {
			artistsList.clear();
			artistsList = null;
		}
		
		if (albumsList!=null) {
			albumsList.clear();
			albumsList = null;
		}
		
		if (albumArtistsList!=null) {
			albumArtistsList.clear();
			albumArtistsList = null;
		}
		
		if (genresList!=null) {
			genresList.clear();
			genresList = null;
		}
		
		if (producersList!=null) {
			producersList.clear();
			producersList = null;
		}
		
		if (yearsList!=null) {
			yearsList.clear();
			yearsList = null;
		}
		
		if (trackNumbersList!=null) {
			trackNumbersList.clear();
			trackNumbersList = null;
		}
		
		if (totalTracksList!=null) {
			totalTracksList.clear();
			totalTracksList = null;
		}
		
		if (commentsList!=null) {
			commentsList.clear();
			commentsList = null;
		}
		
		if (songURIsList!=null) {
			songURIsList.clear();
			songURIsList = null;
		}
		
		/*if (songSourcesList!=null) {
			songSourcesList.clear();
			songSourcesList = null;
		}
		
		if (songIdsList!=null) {
			songIdsList.clear();
			songIdsList = null;
		}*/
		
	}
	
}
