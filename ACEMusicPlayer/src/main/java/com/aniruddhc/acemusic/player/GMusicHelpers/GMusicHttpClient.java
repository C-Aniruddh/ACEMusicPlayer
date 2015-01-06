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

import org.apache.http.HttpEntity;

import android.content.Context;

import com.loopj.android.http.SyncHttpClient;

public class GMusicHttpClient extends SyncHttpClient
{

	public GMusicHttpClient()
	{
		super();
	}

	public String post(Context context, String url, HttpEntity entity, String contentType)
	{
		post(context, url, entity, contentType, responseHandler);
		return result;
	}

	@Override
	public String onRequestFailed(Throwable error, String content)
	{
		return null;
	}
}
