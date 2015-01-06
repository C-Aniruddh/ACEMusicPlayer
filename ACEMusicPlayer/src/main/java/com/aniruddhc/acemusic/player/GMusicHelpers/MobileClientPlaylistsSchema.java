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

public class MobileClientPlaylistsSchema implements IJsonObject<MobileClientPlaylistsSchema>, IJsonArray<WebClientSongsSchema> {
	
	private String mKind;
	private String mPlaylistId;
	private String mCreationTimestamp;
	private String mLastModifiedTimestamp;
	private String mRecentTimestamp;
	private boolean mDeleted;
	private String mName;
	private String mType;
	private String mShareToken;
	private String mOwnerName;
	private String mOwnerProfilePhotoUrl;
	private boolean mAccessControlled;
	private ArrayList<WebClientSongsSchema> mPlaylist;

	public String getKind() {
		return mKind;
	}

	public void setKind(String kind) {
		this.mKind = kind;
	}

	public String getPlaylistId() {
		return mPlaylistId;
	}

	public void setPlaylistId(String playlistId) {
		this.mPlaylistId = playlistId;
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

	public String getRecentTimestamp() {
		return mRecentTimestamp;
	}

	public void setRecentTimestamp(String recentTimestamp) {
		this.mRecentTimestamp = recentTimestamp;
	}

	public boolean isDeleted() {
		return mDeleted;
	}

	public void setDeleted(boolean deleted) {
		this.mDeleted = deleted;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public String getShareToken() {
		return mShareToken;
	}

	public void setShareToken(String shareToken) {
		this.mShareToken = shareToken;
	}

	public String getOwnerName() {
		return mOwnerName;
	}

	public void setOwnerName(String ownerName) {
		this.mOwnerName = ownerName;
	}

	public String getOwnerProfilePhotoUrl() {
		return mOwnerProfilePhotoUrl;
	}

	public void setOwnerProfilePhotoUrl(String ownerProfilePhotoUrl) {
		this.mOwnerProfilePhotoUrl = ownerProfilePhotoUrl;
	}

	public boolean ismAccessControlled() {
		return mAccessControlled;
	}

	public void setAccessControlled(boolean accessControlled) {
		this.mAccessControlled = accessControlled;
	}

	public ArrayList<WebClientSongsSchema> getPlaylist() {
		return mPlaylist;
	}

	public void setPlaylist(ArrayList<WebClientSongsSchema> playlist) {
		this.mPlaylist = playlist;
	}

	@Override
	public MobileClientPlaylistsSchema fromJsonObject(JSONObject jsonObject) {
		if(jsonObject != null) {
			mKind = jsonObject.optString("kind", null);
			mPlaylistId = jsonObject.optString("id", null);
			mCreationTimestamp = jsonObject.optString("creationTimestamp");
			mLastModifiedTimestamp = jsonObject.optString("lastModifiedTimestamp", null);
			mRecentTimestamp = jsonObject.optString("recentTimestamp");
			mDeleted = jsonObject.optBoolean("deleted");
			mName = jsonObject.optString("name");
			mType = jsonObject.optString("type");
			mShareToken = jsonObject.optString("shareToken");
			mOwnerName = jsonObject.optString("ownerName");
			mOwnerProfilePhotoUrl = jsonObject.optString("ownerProfilePhotoUrl");
			mAccessControlled = jsonObject.optBoolean("accessControlled");
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
