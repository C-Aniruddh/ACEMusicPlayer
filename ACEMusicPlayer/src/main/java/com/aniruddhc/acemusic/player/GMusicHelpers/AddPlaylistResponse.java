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

// not used in the Android app
public class AddPlaylistResponse implements IJsonObject<AddPlaylistResponse>
{
	private String mId;
	private String mTitle;
	private boolean mSuccess;

	public final String getId()
	{
		return mId;
	}

	public final void setId(String id)
	{
		mId = id;
	}

	public final String getTitle()
	{
		return mTitle;
	}

	public final void setTitle(String title)
	{
		mTitle = title;
	}

	public final boolean isSuccess()
	{
		return mSuccess;
	}

	public final void setSuccess(boolean success)
	{
		mSuccess = success;
	}

	@Override
	public AddPlaylistResponse fromJsonObject(JSONObject jsonObject)
	{
		if(jsonObject != null)
		{
			mId = jsonObject.optString("id", null);
			mTitle = jsonObject.optString("title", null);
			mSuccess = jsonObject.optBoolean("success");
		}

		// return this object to allow chaining
		return this;
	}
}
