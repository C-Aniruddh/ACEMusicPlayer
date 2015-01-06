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
package com.aniruddhc.acemusic.player.GMusicHelpers;

import org.json.JSONObject;

public class WebClientSongsSchema implements IJsonObject<WebClientSongsSchema> {
	private int mTotalTracks;
	private boolean mSubjectToCuration;
	private String mName;
	private int mTotalDiscs;
	private String mTitleNorm;
	private String mAlbumNorm;
	private int mTrack;
	private String mAlbumArtUrl;
	private String mUrl;
	private long mCreationDate;
	private String mAlbumArtistNorm;
	private String mArtistNorm;
	private long mLastPlayed;
	private String mMatchedId;
	private int mType;
	private int mDisc;
	private String mGenre;
	private int mBeatsPerMinute;
	private String mAlbum;
	private String mId;
	private String mComposer;
	private String mTitle;
	private String mAlbumArtist;
	private int mYear;
	private String mArtist;
	private long mDurationMillis;
	private boolean mIsDeleted;
	private int mPlayCount;
	private String mRating;
	private String mComment;
	private String mPlaylistEntryId;

	public String getAlbum() {
		return mAlbum;
	}

	public String getAlbumArtist() {
		return mAlbumArtist;
	}

	public String getAlbumArtistNorm() {
		return mAlbumArtistNorm;
	}

	public String getAlbumArtUrl() {
		return mAlbumArtUrl;
	}

	public String getAlbumNorm() {
		return mAlbumNorm;
	}

	public String getArtist() {
		return mArtist;
	}

	public String getArtistNorm() {
		return mArtistNorm;
	}

	public int getBeatsPerMinute() {
		return mBeatsPerMinute;
	}

	public String getComment() {
		return mComment;
	}

	public String getComposer() {
		return mComposer;
	}

	public float getCreationDate() {
		return mCreationDate;
	}

	public int getDisc() {
		return mDisc;
	}

	public long getDurationMillis() {
		return mDurationMillis;
	}

	public String getGenre() {
		return mGenre;
	}

	public String getId() {
		return mId;
	}

	public double getLastPlayed() {
		return mLastPlayed;
	}

	public String getMatchedId() {
		return mMatchedId;
	}

	public String getName() {
		return mName;
	}

	public int getPlayCount() {
		return mPlayCount;
	}

	public String getRating() {
		return mRating;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getTitleNorm() {
		return mTitleNorm;
	}

	public int getTotalDiscs() {
		return mTotalDiscs;
	}

	public int getTotalTracks() {
		return mTotalTracks;
	}

	public int getTrack() {
		return mTrack;
	}

	public int getType() {
		return mType;
	}

	public String getUrl() {
		return mUrl;
	}

	public int getYear() {
		return mYear;
	}
	
	public String getPlaylistEntryId() {
		return mPlaylistEntryId;
	}

	public boolean isDeleted() {
		return mIsDeleted;
	}

	public boolean isSubjectToCuration() {
		return mSubjectToCuration;
	}

	public void setAlbum(String album) {
		mAlbum = album;
	}

	public void setAlbumArtist(String albumArtist) {
		mAlbumArtist = albumArtist;
	}

	public void setAlbumArtistNorm(String albumArtistNorm) {
		mAlbumArtistNorm = albumArtistNorm;
	}

	public void setAlbumArtUrl(String albumArtUrl) {
		mAlbumArtUrl = albumArtUrl;
	}

	public void setAlbumNorm(String albumNorm) {
		mAlbumNorm = albumNorm;
	}

	public void setArtist(String artist) {
		mArtist = artist;
	}

	public void setArtistNorm(String artistNorm) {
		mArtistNorm = artistNorm;
	}

	public void setBeatsPerMinute(int beatsPerMinute) {
		mBeatsPerMinute = beatsPerMinute;
	}

	public void setComment(String comment) {
		mComment = comment;
	}

	public void setComposer(String composer) {
		mComposer = composer;
	}

	public void setCreationDate(long creationDate) {
		mCreationDate = creationDate;
	}

	public void setDeleted(boolean isDeleted) {
		mIsDeleted = isDeleted;
	}

	public void setDisc(int disc) {
		mDisc = disc;
	}

	public void setDurationMillis(long durationMillis) {
		mDurationMillis = durationMillis;
	}

	public void setGenre(String genre) {
		mGenre = genre;
	}

	public void setId(String id) {
		mId = id;
	}

	public void setLastPlayed(long lastPlayed) {
		mLastPlayed = lastPlayed;
	}

	public void setMatchedId(String matchedId) {
		mMatchedId = matchedId;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setPlaycount(int playcount) {
		mPlayCount = playcount;
	}

	public void setRating(String rating) {
		mRating = rating;
	}

	public void setSubjectToCuration(boolean subjectToCuration) {
		mSubjectToCuration = subjectToCuration;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setTitleNorm(String titleNorm) {
		mTitleNorm = titleNorm;
	}

	public void setTotalDiscs(int totalDiscs) {
		mTotalDiscs = totalDiscs;
	}

	public void setTotalTracks(int totalTracks) {
		mTotalTracks = totalTracks;
	}

	public void setTrack(int track) {
		mTrack = track;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public void setYear(int year) {
		mYear = year;
	}
	
	public void setEntryId(String playlistEntryId) {
		mPlaylistEntryId = playlistEntryId;
	}

	@Override
	public WebClientSongsSchema fromJsonObject(JSONObject jsonObject) {
		
		if(jsonObject != null) {
			mTotalTracks = jsonObject.optInt("totalTracks");
			mSubjectToCuration = jsonObject.optBoolean("subjectToCuration");
			mName = jsonObject.optString("name", null);
			mTotalDiscs = jsonObject.optInt("totalDiscs");
			mTitleNorm = jsonObject.optString("titleNorm", null);
			mAlbumNorm = jsonObject.optString("albumNorm", null);
			mTrack = jsonObject.optInt("track");
			mAlbumArtUrl = jsonObject.optString("albumArtUrl", null);
			mUrl = jsonObject.optString("url", null);
			mCreationDate = jsonObject.optLong("creationDate");
			mAlbumArtistNorm = jsonObject.optString("albumArtistNorm", null);
			mArtistNorm = jsonObject.optString("artistNorm", null);
			mLastPlayed = jsonObject.optLong("lastPlayed");
			mMatchedId = jsonObject.optString("matchedId", null);
			mType = jsonObject.optInt("type");
			mDisc = jsonObject.optInt("disc");
			mGenre = jsonObject.optString("genre", null);
			mBeatsPerMinute = jsonObject.optInt("beatsPerMinute");
			mAlbum = jsonObject.optString("album", null);
			mId = jsonObject.optString("id", null);
			mComposer = jsonObject.optString("composer", null);
			mTitle = jsonObject.optString("title", null);
			mAlbumArtist = jsonObject.optString("albumArtist", null);
			mYear = jsonObject.optInt("year");
			mArtist = jsonObject.optString("artist", null);
			mDurationMillis = jsonObject.optLong("durationMillis");
			mIsDeleted = jsonObject.optBoolean("deleted");
			mPlayCount = jsonObject.optInt("playCount");
			mRating = jsonObject.optString("rating", null);
			mComment = jsonObject.optString("comment", null);
			mPlaylistEntryId = jsonObject.optString("playlistEntryId");
		}

		//This method returns itself to support chaining.
		return this;
	}
	
}
