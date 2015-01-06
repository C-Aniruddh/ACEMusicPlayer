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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebClientPlaylistsSchema implements IJsonObject<WebClientPlaylistsSchema>, IJsonArray<WebClientSongsSchema> {

	private String mTitle;
	private String mPlaylistId;
	private long mRequestTime;
	private String mContinuationToken;
	private boolean mDifferentialUpdate;
	private ArrayList<WebClientSongsSchema> mPlaylist;
	private boolean mContinuation;

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getPlaylistId() {
		return mPlaylistId;
	}

	public void setPlaylistId(String playlistId) {
		mPlaylistId = playlistId;
	}

	public long getRequestTime() {
		return mRequestTime;
	}

	public void setRequestTime(long requestTime) {
		mRequestTime = requestTime;
	}

	public String getContinuationToken() {
		return mContinuationToken;
	}

	public void setContinuationToken(String continuationToken) {
		mContinuationToken = continuationToken;
	}

	public boolean isDifferentialUpdate() {
		return mDifferentialUpdate;
	}

	public void setDifferentialUpdate(boolean differentialUpdate) {
		mDifferentialUpdate = differentialUpdate;
	}

	public ArrayList<WebClientSongsSchema> getPlaylist() {
		return mPlaylist;
	}

	public void setPlaylist(ArrayList<WebClientSongsSchema> playlist) {
		mPlaylist = playlist;
	}

	public boolean isContinuation() {
		return mContinuation;
	}

	public void setContinuation(boolean continuation) {
		mContinuation = continuation;
	}

	@Override
	public WebClientPlaylistsSchema fromJsonObject(JSONObject jsonObject) {
		if(jsonObject != null) {
			mTitle = jsonObject.optString("title", null);
			mPlaylistId = jsonObject.optString("playlistId", null);
			mRequestTime = jsonObject.optLong("requestTime");
			mContinuationToken = jsonObject.optString("continuationToken", null);
			mDifferentialUpdate = jsonObject.optBoolean("differentialUpdate");
			mContinuation = jsonObject.optBoolean("continuation");

			JSONArray songsArray = jsonObject.optJSONArray("playlist");
			mPlaylist = fromJsonArray(songsArray);
		}

		//This method returns itself to support chaining.
		return this;
	}

	@Override
	public ArrayList<WebClientSongsSchema> fromJsonArray(JSONArray jsonArray) {
		
		ArrayList<WebClientSongsSchema> songList = new ArrayList<WebClientSongsSchema>();
		if(jsonArray != null && jsonArray.length() > 0) {
			for(int i = 0; i < jsonArray.length(); i++) {
				try {
					WebClientSongsSchema song = new WebClientSongsSchema().fromJsonObject(jsonArray.getJSONObject(i));
					songList.add(song);
					
				} catch(JSONException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		return songList;
	}
	
}
