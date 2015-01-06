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

public class MobileClientPlaylistEntriesSchema implements IJsonObject<MobileClientPlaylistEntriesSchema>, IJsonArray<WebClientSongsSchema> {
	
	private String mKind;
	private String mId;
	private String mClientId;
	private String mPlaylistId;
	private String mTrackId;
	private String mCreationTimestamp;
	private String mLastModifiedTimestamp;
	private boolean mDeleted;
	private String mSource;

	public String getKind() {
		return mKind;
	}

	public void setKind(String kind) {
		this.mKind = kind;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String id) {
		this.mId = id;
	}

	public String getClientId() {
		return mClientId;
	}
	
	public void setClientId(String clientId) {
		this.mClientId = clientId;
	}
	
	public String getPlaylistId() {
		return mPlaylistId;
	}

	public void setPlaylistId(String playlistId) {
		this.mPlaylistId = playlistId;
	}
	
	public String getTrackId() {
		return mTrackId;
	}
	
	public void setTrackId(String trackId) {
		this.mTrackId = trackId;
	}

	public String getCreationTimestamp() {
		return mCreationTimestamp;
	}

	public void setCreationTimestamp(String creationTimestamp) {
		this.mCreationTimestamp = creationTimestamp;
	}

	public String getLastModifiedTimestamp() {
		return mLastModifiedTimestamp;
	}

	public void setLastModifiedTimestamp(String lastModifiedTimestamp) {
		this.mLastModifiedTimestamp = lastModifiedTimestamp;
	}

	public boolean isDeleted() {
		return mDeleted;
	}

	public void setDeleted(boolean deleted) {
		this.mDeleted = deleted;
	}
	
	public String getSource() {
		return mSource;
	}
	
	public void setSource(String source) {
		this.mSource = source;
	}

	@Override
	public MobileClientPlaylistEntriesSchema fromJsonObject(JSONObject jsonObject) {
		if(jsonObject != null) {
			mKind = jsonObject.optString("kind", null);
			mPlaylistId = jsonObject.optString("playlistId", null);
			mCreationTimestamp = jsonObject.optString("creationTimestamp");
			mLastModifiedTimestamp = jsonObject.optString("lastModifiedTimestamp", null);
			mDeleted = jsonObject.optBoolean("deleted");
			mClientId = jsonObject.optString("clientId");
			mTrackId = jsonObject.optString("trackId");
			mId = jsonObject.optString("id");
			mSource = jsonObject.optString("source");
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
