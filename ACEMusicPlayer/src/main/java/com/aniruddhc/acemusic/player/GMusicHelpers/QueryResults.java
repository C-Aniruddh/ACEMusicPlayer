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

//not used in the Android app
public class QueryResults implements IJsonObject<QueryResults>, IJsonArray<WebClientSongsSchema>
{

	private ArrayList<WebClientSongsSchema> mArtists;
	private ArrayList<WebClientSongsSchema> mAlbums;
	private ArrayList<WebClientSongsSchema> mWebClientSongsSchemas;

	public ArrayList<WebClientSongsSchema> getArtists()
	{
		return mArtists;
	}

	public void setArtists(ArrayList<WebClientSongsSchema> artists)
	{
		mArtists = artists;
	}

	public ArrayList<WebClientSongsSchema> getAlbums()
	{
		return mAlbums;
	}

	public void setAlbums(ArrayList<WebClientSongsSchema> albums)
	{
		mAlbums = albums;
	}

	public ArrayList<WebClientSongsSchema> getWebClientSongsSchemas()
	{
		return mWebClientSongsSchemas;
	}

	public void setWebClientSongsSchemas(ArrayList<WebClientSongsSchema> songs)
	{
		mWebClientSongsSchemas = songs;
	}

	@Override
	public QueryResults fromJsonObject(JSONObject jsonObject)
	{
		if(jsonObject != null)
		{
			JSONArray jsonArray = jsonObject.optJSONArray("artists");
			mArtists = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);

			jsonArray = jsonObject.optJSONArray("albums");
			mAlbums = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);

			jsonArray = jsonObject.optJSONArray("songs");
			mWebClientSongsSchemas = (ArrayList<WebClientSongsSchema>) fromJsonArray(jsonArray);
		}

		// return this object to allow chaining
		return this;
	}

	@Override
	public ArrayList<WebClientSongsSchema> fromJsonArray(JSONArray jsonArray)
	{
		ArrayList<WebClientSongsSchema> songList = new ArrayList<WebClientSongsSchema>();
		if(jsonArray != null && jsonArray.length() > 0)
		{
			for(int i = 0; i < jsonArray.length(); i++)
			{
				try
				{
					WebClientSongsSchema song = new WebClientSongsSchema().fromJsonObject(jsonArray.getJSONObject(i));
					songList.add(song);
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		return songList;
	}
}
