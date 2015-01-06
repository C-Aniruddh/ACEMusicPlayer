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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.R;

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

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

public class ID3sSongEditorDialog extends DialogFragment {
	
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
	
	private String SONG_URI;
	private String CALLING_FRAGMENT;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mContext = getActivity();
		parentActivity = getActivity();
		dialogFragment = this;
		
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
		
		//Get the song uri.
		SONG_URI = getArguments().getString("SONG");
		
		//Get the calling Fragment and retrieve the child view from it.
		CALLING_FRAGMENT = getArguments().getString("CALLING_FRAGMENT");
		
		if (SONG_URI!=null) {
			
			//Populate the ArrayLists with the song tags.
			try {
				getSongTags(SONG_URI);
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
			
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.edit_tags);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.save, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				boolean saveSucceeded = saveSongTags(SONG_URI);
					
				//Check if the write operations succeeded. If they didn't, display an error message.
				if (saveSucceeded==true) {
					Toast.makeText(getActivity().getApplicationContext(), R.string.song_tags_saved, Toast.LENGTH_SHORT).show();
					
/*					//Reinitialize the calling fragment.
					if (CALLING_FRAGMENT.equals("SONGS_FRAGMENT")) {


					} else if (CALLING_FRAGMENT.equals("ARTISTS_FLIPPED_SONGS_FRAGMENT")) {
						//ArtistsFlippedSongsFragment.getCursor();
						ArtistsFlippedSongsFragment.songsListViewAdapter.notifyDataSetChanged();
					} else if (CALLING_FRAGMENT.equals("ALBUMS_FLIPPED_FRAGMENT")) {
						AlbumsFlippedFragment.getCursor();
						AlbumsFlippedFragment.albumsFlippedListViewAdapter.notifyDataSetChanged();
					} else if (CALLING_FRAGMENT.equals("ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT")) {
						AlbumArtistsFlippedSongsFragment.getCursor();
						AlbumArtistsFlippedSongsFragment.songsListViewAdapter.notifyDataSetChanged();
					} else if (CALLING_FRAGMENT.equals("GENRES_FLIPPED_SONGS_FRAGMENT")) {
						GenresFlippedFragment.getCursor();
						GenresFlippedFragment.genresFlippedListViewAdapter.notifyDataSetChanged();
					}*/
					
				} else {
					Toast.makeText(parentActivity, R.string.error_occurred_tags, Toast.LENGTH_LONG).show();
				}
				
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
	
	//This method loops through all the songs and saves their tags into ArrayLists.
	public void getSongTags(String uri) throws CannotReadException, 
											   IOException, 
											   TagException, 
											   ReadOnlyFileException, 
											   InvalidAudioFrameException {
			
		File file = new File(uri);
		AudioFile audioFile = AudioFileIO.read(file);
		
		if (audioFile!=null && audioFile.getTag()!=null) {
			titleEditText.setText(audioFile.getTag().getFirst(FieldKey.TITLE));
			artistEditText.setText(audioFile.getTag().getFirst(FieldKey.ARTIST));
			albumEditText.setText(audioFile.getTag().getFirst(FieldKey.ALBUM));
			albumArtistEditText.setText(audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST));
			genreEditText.setText(audioFile.getTag().getFirst(FieldKey.GENRE));
			producerEditText.setText(audioFile.getTag().getFirst(FieldKey.PRODUCER));
			yearEditText.setText(audioFile.getTag().getFirst(FieldKey.YEAR));
			trackEditText.setText(audioFile.getTag().getFirst(FieldKey.TRACK));
			trackTotalEditText.setText(audioFile.getTag().getFirst(FieldKey.TRACK_TOTAL));
			commentsEditText.setText(audioFile.getTag().getFirst(FieldKey.COMMENT));
			
		}

	}
	
	//This method is called if the user touches the 'OK' button when they're editing an individual song's tags.
	public boolean saveSongTags(String uri) {
		
		File file = new File(uri);
		AudioFile audioFile = null;
		
		//Update the DB entries.
		DBAccessHelper dbHelper = new DBAccessHelper(mContext.getApplicationContext());
		
		//Escape any rogue apostrophes.
		if (SONG_URI.contains("'")) {
			SONG_URI = SONG_URI.replace("'", "''");
		}
		
		String whereClause = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + SONG_URI + "'";
		
		ContentValues values = new ContentValues();
		
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
		
		Tag tag = audioFile.getTag();
		
		if (tag!=null) {
			if (titleEdited==false) {
				//Don't do anything here. The user didn't change the title.
			} else {
				try {
					tag.setField(FieldKey.TITLE, titleEditText.getText().toString());
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
				
				String title = titleEditText.getText().toString();
				if (title.contains("'")) {
					title = title.replace("'", "''");
				}
				
				values.put(DBAccessHelper.SONG_TITLE, title);
				
			}
			
			if (albumEdited==false) {
				//Don't do anything here. The user didn't change the album.
			} else {
				try {
					tag.setField(FieldKey.ALBUM, albumEditText.getText().toString());
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
				
				String album = albumEditText.getText().toString();
				if (album.contains("'")) {
					album = album.replace("'", "''");
				}
				
				values.put(DBAccessHelper.SONG_ALBUM, album);
				
			}
			
			if (artistEdited==false) {
				//Don't do anything here. The user didn't change the artist.
			} else {
				try {
					tag.setField(FieldKey.ARTIST, artistEditText.getText().toString());
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
				
				String artist = artistEditText.getText().toString();
				if (artist.contains("'")) {
					artist = artist.replace("'", "''");
				}
				
				values.put(DBAccessHelper.SONG_ARTIST, artist);
				
			}
			
			if (albumArtistEdited==false) {
				//Don't do anything here. The user didn't change the album artist.
			} else {
				try {
					tag.setField(FieldKey.ALBUM_ARTIST, albumArtistEditText.getText().toString());
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
				
				String albumArtist = albumArtistEditText.getText().toString();
				if (albumArtist.contains("'")) {
					albumArtist = albumArtist.replace("'", "''");
				}
				values.put(DBAccessHelper.SONG_ALBUM_ARTIST, albumArtist);
				
			}
			
			if (genreEdited==false) {
				//Don't do anything here. The user didn't change the genre.
			} else {
				try {
					tag.setField(FieldKey.GENRE, genreEditText.getText().toString());
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
					tag.setField(FieldKey.PRODUCER, producerEditText.getText().toString());
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
					tag.setField(FieldKey.YEAR, yearEditText.getText().toString());
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
				
				String year = yearEditText.getText().toString();
				if (year.contains("'")) {
					year = year.replace("'", "''");
				}
				
				values.put(DBAccessHelper.SONG_YEAR, year);
				
			}
			
			if (trackEdited==false) {
				//Don't do anything here. The user didn't change the track number.
			} else {
				try {
					tag.setField(FieldKey.TRACK, trackEditText.getText().toString());
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
				
				String track = trackEditText.getText().toString();
				if (track.contains("'")) {
					track = track.replace("'", "''");
				}
				
				values.put(DBAccessHelper.SONG_TRACK_NUMBER, track);
				
			}
			
			try {
				tag.setField(FieldKey.TRACK_TOTAL, trackTotalEditText.getText().toString());
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
					tag.setField(FieldKey.COMMENT, commentsEditText.getText().toString());
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
			if (values.size()!=0) {
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
				
			}
			
		} else {
			Toast.makeText(mContext, R.string.unable_to_edit_song_tags, Toast.LENGTH_SHORT).show();
		}

		return true;
		
	}
	
}
