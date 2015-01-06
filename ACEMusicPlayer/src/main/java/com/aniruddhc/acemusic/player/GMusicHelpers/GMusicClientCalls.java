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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

/*******************************************************************************
 * This class contains the MobileClient (preferred) and WebClient (deprecated) 
 * endpoints and method calls for Google Play Music. Retrieving the song stream 
 * URL and reordering songs in a playlist are the only operations that should 
 * be carried out using the WebClient protocol.
 * 
 * @author Saravan Pantham
 *******************************************************************************/
public class GMusicClientCalls {
	
	public static Context mContext;
	private static PersistentCookieStore mCookieStore;
	public static GMusicHttpClient mHttpClient;
	private static GMusicClientCalls mInstance;
	private static String mWebClientUserAgent;
	private static String mAuthToken;
	private static String mMobileClientUserAgent = "Android-Music/1301 (t03g JDQ39); gzip";
	public static JSONArray mPlaylistEntriesMutationsArray = new JSONArray();

	public static void createInstance(Context context) {
		getInstance(context);
	}

	public static GMusicClientCalls getInstance(Context context) {
		if(mInstance == null)
			mInstance = new GMusicClientCalls(context);
		
		return mInstance;
	}

	private GMusicClientCalls(Context context) {
		mHttpClient = new GMusicHttpClient();
		mContext = context;
		mCookieStore = new PersistentCookieStore(context.getApplicationContext());
		mHttpClient.setCookieStore(mCookieStore);
		mHttpClient.setUserAgent("");
	}
	
	/*************************************************
	 * Returns the raw HTTP client behind the custom 
	 * GMusicHTTPClient implementation.
	 * @return The raw HTTP client object.
	 *************************************************/
	public static final HttpClient getRawHttpClient() {
		return mHttpClient.getHttpClient();
	}

	/******************************************************************
	 * Sets the HTTP client's authorization header using the specified 
	 * authentication token. The header will be in the following form: 
	 * 
	 * "Authorization", "GoogleLogin auth=xxxxxxxxxxxxxx",
	 * 
	 * where "xxxxxxxxxxxxxxx" is the authentication token.
	 * 
	 * @param authToken The authentication token that will be used to 
	 * set the header.
	 ******************************************************************/
	public static final void setAuthorizationHeader(String authToken) {
		mAuthToken = authToken;
		mHttpClient.addHeader("Authorization", "GoogleLogin auth=" + mAuthToken);
	}
	
	/************************************************************************
	 * Resets the current HTTP client object by shutting it down and then 
	 * reinstantiating it. The old cookie store, user agent and authorization 
	 * header will be reused. This process will be done on a separate thread.
	 ************************************************************************/
	public static void resetHttpClient() {
		//Reset the HTTP Client on a separate thread.
		Thread thread = new Thread() {
			
			@Override
			public void run() {
				if (mHttpClient!=null) {
					mHttpClient.getHttpClient().getConnectionManager().shutdown();
					mHttpClient = new GMusicHttpClient();
					mHttpClient.setCookieStore(mCookieStore);
					mHttpClient.setUserAgent(mWebClientUserAgent);
					mHttpClient.addHeader("Authorization", "GoogleLogin auth=" + mAuthToken);
				}
				
			}
			
		};
		thread.start();
		
	}

	/*********************************************************
	 * Sets the user agent for webclient calls.
	 * 
	 * @param userAgent
	 *********************************************************/
	public static final void setWebClientUserAgent(String userAgent) {
		mWebClientUserAgent = userAgent;
		mHttpClient.setUserAgent(mWebClientUserAgent);
	}
	
	public static final GMusicHttpClient getHttpClient() {
		return mHttpClient;
	}
	
	/*************************************************
	 * Loops through the HTTP client's cookie store 
	 * and returns the value of the "xt" cookie.
	 * 
	 * @return Value of the "xt" cookie.
	 *************************************************/
	private static final String getXtCookieValue() {
		
		for(Cookie cookie : mCookieStore.getCookies()) {
			if(cookie.getName().equals("xt"))
				return cookie.getValue();
		}

		return null;
	}
	
	/*******************************************************************************
	 * Attempts to log the user into the "sj" (SkyJam) service using the provided
	 * authentication token. The authentication token is unique for each session 
	 * and user account. It can be obtained via the GoogleAuthUtil.getToken() 
	 * method. See AsyncGoogleMusicAuthenticationTask.java for the current
	 * implementation of this process. This method will return true if the login
	 * process succeeded. Returns false for any other type of failure.
	 * 
	 * @param context The context that will be used for the login process.
	 * @param authToken The authentication token that will be used to login. 
	 *******************************************************************************/
	public static final boolean login(Context context, String authToken) {
		
		if(!TextUtils.isEmpty(authToken))
		{
			JSONForm form = new JSONForm().close();
			GMusicClientCalls.setAuthorizationHeader(authToken);
			String response = mHttpClient.post(context, 
											   "https://play.google.com/music/listen?hl=en&u=0", 
											   new ByteArrayEntity(form.toString().getBytes()), 
											   form.getContentType());
			
			//Check if the required paramters are null.
			if (response!=null) {
				
				if (getXtCookieValue()!=null) {
					return true;
				} else {
					return false;
				}
				
			} else {
				return false;
			}

		} else {
			return false;
		}
		
	}

	/*****************************************************************
	 * Gets the URI of the song stream using the specified song Id. 
	 * The URI is dynamically generated by Google's servers and 
	 * expires after one minute of no usage. This method uses the 
	 * WebClient endpoint.
	 * 
	 * @param songId The id of the song that needs to be streamed.
	 * @return Returns the URI of the song stream.
	 * @throws JSONException
	 * @throws URISyntaxException
	 ******************************************************************/
	public static final URI getSongStream(String songId) 
							throws JSONException, URISyntaxException {

		RequestParams params = new RequestParams();
		params.put("u", "0");
		params.put("songid", songId);
		params.put("pt", "e");
		
		String response = mHttpClient.get("https://play.google.com/music/play", params);

		if (response!=null) {
			JSONObject jsonObject = new JSONObject(response);
			return new URI(jsonObject.optString("url", null));
		}
		
		return null;
	}

	/***************************************************************************************
	 * @deprecated The use of this method is highly discouraged as it sends/fetches large 
	 * amounts of data from Google's servers. All of this data is readily available via the 
	 * Google Play Music app's public ContentProvider. This method uses the WebClient 
	 * endpoint and is a helper method for getSongs().
	 * 
	 * @param context The context to use during the download process.
	 * @return
	 * @throws JSONException
	 ***************************************************************************************/
	public static final ArrayList<WebClientSongsSchema> getAllSongs(Context context) 
														throws JSONException {
		return getSongs(context, "");
	}

	/***************************************************************************************
	 * <p>
	 * Queries Google's servers for a list of all songs in the current Google account's 
	 * music library.
	 * </p>
	 * 
	 * @deprecated The use of this method is highly discouraged as it sends/fetches large 
	 * amounts of data from Google's servers. All of this data is readily available via the 
	 * Google Play Music app's public ContentProvider. This method uses the WebClient 
	 * endpoint.
	 * 
	 * @param context The context to use during the download process.
	 * @param continuationToken The token that will return the next set of songs (only 1000 
	 * songs are returned per request).
	 * @return
	 * @throws JSONException
	 ***************************************************************************************/
	public static final ArrayList<WebClientSongsSchema> getSongs(Context context, 
																 String continuationToken) 
																 throws JSONException {

		JSONForm form = new JSONForm();
		form.addField("json", "{\"continuationToken\":\"" + continuationToken + "\"}");
		form.close();

		String response = mHttpClient.post(context, 
										   "https://play.google.com/music/services/loadalltracks?u=0&xt=" + getXtCookieValue(), 
										   new ByteArrayEntity(form.toString().getBytes()), 
										   form.getContentType());
		
		JSONObject jsonObject = new JSONObject(response);
		WebClientPlaylistsSchema playlist = new WebClientPlaylistsSchema().fromJsonObject(jsonObject);

		ArrayList<WebClientSongsSchema> chunkedSongList = new ArrayList<WebClientSongsSchema>();
		chunkedSongList.addAll(playlist.getPlaylist());

		if(!TextUtils.isEmpty(playlist.getContinuationToken())) {
			chunkedSongList.addAll(getSongs(context, playlist.getContinuationToken()));
		}

		return chunkedSongList;
	}
	
	/****************************************************************************
	 * Creates a new, user generated playlist. This method only creates the 
	 * playlist; it does not add songs to the playlist.
	 * 
	 * @param context The context to use while creating the new playlist.
	 * @param playlistName The name of the new playlist.
	 * @return Returns the playlistId of the newly created playlist.
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 ****************************************************************************/
	public final static String createPlaylist(Context context, String playlistName) 
							   throws JSONException, IllegalArgumentException {
		
		JSONObject jsonParam = new JSONObject();
		JSONArray mutationsArray = new JSONArray();
		JSONObject createObject = new JSONObject();

		createObject.put("lastModifiedTimestamp", "0");
		createObject.put("name", playlistName);
		createObject.put("creationTimestamp", "-1");
		createObject.put("type", "USER_GENERATED");
		createObject.put("deleted", false);
		
		mutationsArray.put(new JSONObject().put("create", createObject));
		jsonParam.put("mutations", mutationsArray);

		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
										 "https://www.googleapis.com/sj/v1.1/playlistbatch?alt=json&hl=en_US", 
										 new ByteArrayEntity(jsonParam.toString().getBytes()), 
										 "application/json");
		
		mHttpClient.setUserAgent(mWebClientUserAgent);
		return new JSONObject(result).optJSONArray("mutate_response")
									 .getJSONObject(0).optString("id");
	}

	/*****************************************************************************
	 * Creates a JSONObject object that contains the delete command for the 
	 * specified playlist and adds it to the JSONArray that will pass the the 
	 * command on to Google's servers. 
	 * 
	 * @param context The context to use while deleting the playlist.
	 * @param playlistId The playlistId of the playlist to delete.
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 *****************************************************************************/
	public static final String deletePlaylist(Context context, String playlistId) 
							   throws JSONException, IllegalArgumentException {
		
		JSONObject jsonParam = new JSONObject();
		JSONArray mutationsArray = new JSONArray();
		
		mutationsArray.put(new JSONObject().put("delete", playlistId));
		jsonParam.put("mutations", mutationsArray);

		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
										 "https://www.googleapis.com/sj/v1.1/playlistbatch?alt=json&hl=en_US", 
										 new ByteArrayEntity(jsonParam.toString().getBytes()), 
										 "application/json");
		
		mHttpClient.setUserAgent(mWebClientUserAgent);
		return result;
	}
	
	/*****************************************************************************
	 * Creates a JSONObject object that contains the delete command for the 
	 * specified playlist entry.The object will be added to the JSONArray that 
	 * will be passed on to Google's servers. 
	 * 
	 * @param playlistId The playlistId of the playlist to delete.
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 *****************************************************************************/
	public static final void putDeletePlaylistEntryRequest(String playlistEntryId) 
							 throws JSONException, IllegalArgumentException {
		
		JSONObject deleteObject = new JSONObject();
		deleteObject.put("delete", playlistEntryId);
		mPlaylistEntriesMutationsArray.put(deleteObject);
	}
	
	/*****************************************************************************
	 * Adds the specified JSONObject to mPlaylistEntriesMutationsArray. The added 
	 * JSONObject will be placed under the "create" key. The JSONObject should 
	 * contain valid info about the new playlist entry (song) that will be created.
	 * 
	 * @param createObject The JSONObject that contains the new playlist entry's 
	 * info and will be placed under the "create" key. 
	 *****************************************************************************/
	public static final void putCreatePlaylistEntryRequest(JSONObject createObject)
							 throws JSONException {
		mPlaylistEntriesMutationsArray.put(new JSONObject().put("create", createObject));
	}
	
	/*****************************************************************************
	 * Adds the specified JSONObject to mPlaylistEntriesMutationsArray. The added 
	 * JSONObject will be placed under the "update" key. The JSONObject should 
	 * contain valid info about the playlist entry that is being updated.
	 * 
	 * @param updateObject The JSONObject that contains the updated playlist entry's 
	 * info and will be placed under the "update" key. 
	 *****************************************************************************/
	public static final void putUpdatePlaylistEntryRequest(JSONObject updateObject)
							 throws JSONException {
		mPlaylistEntriesMutationsArray.put(new JSONObject().put("update", updateObject));
	}
	
	/******************************************************************************************
	 * Executes a single/batch modification operation on a playlist's entry(ies). This method 
	 * is a general purpose method that simply hits the MobileClient endpoints using
	 * mPlaylistEntriesMutationsArray. Supported mutation operations include "create", 
	 * "delete", and "update". 
	 * 
	 * @param context The context to use while carrying out the modification operation.
	 * @param mutationsArray The JSONArray that contains the mutations command to be 
	 * carried out.
	 * @return The JSON response as a String.
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 ******************************************************************************************/
	public static final String modifyPlaylist(Context context) 
							   throws JSONException, IllegalArgumentException {
		
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("mutations", mPlaylistEntriesMutationsArray);
		
		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
										 "https://www.googleapis.com/sj/v1.1/plentriesbatch?alt=json&hl=en_US", 
										 new ByteArrayEntity(jsonParam.toString().getBytes()), 
										 "application/json");
		
		mHttpClient.setUserAgent(mWebClientUserAgent);
		
		//Clear out and reset the mutationsArray now that we're done using it.
		mPlaylistEntriesMutationsArray = null;
		mPlaylistEntriesMutationsArray = new JSONArray();
		
		return result;
	}    
	
	/*******************************************************************************************
	 * Returns the number of elements in mPlaylistEntriesMutationsArray. Used to check if a 
	 * POST request should be sent to Google's servers.
	 *******************************************************************************************/
	public static int getQueuedMutationsCount() {
		return mPlaylistEntriesMutationsArray.length();
	}
	
	/*******************************************************************************************
	 * Sends a POST request to Google's servers and retrieves a JSONArray with all user 
	 * playlists. The JSONArray contains the fields of the playlist such as "id", "name", 
	 * "type", etc. (for a list of all response fields, see MobileClientPlaylistsSchema.java).
	 * 
	 * @return A JSONArray object that contains all user playlists and their fields.
	 * @param context The context to use while retrieving user playlists.
	 *******************************************************************************************/
	public static final JSONArray getUserPlaylistsMobileClient(Context context)
								  throws JSONException, IllegalArgumentException {
		
		JSONObject jsonRequestParams = new JSONObject();
		JSONArray playlistsJSONArray = new JSONArray();
		
		jsonRequestParams.put("max-results", 250);
		jsonRequestParams.put("start-token", "0");
		
		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
				 						 "https://www.googleapis.com/sj/v1.1/playlistfeed?alt=json&hl=en_US&tier=basic", 
				 						 new ByteArrayEntity(jsonRequestParams.toString().getBytes()), 
				 						 "application/json");
		
		JSONObject resultJSONObject = new JSONObject(result);
		JSONObject dataJSONObject = new JSONObject();
		
		if (resultJSONObject!=null) {
			dataJSONObject = resultJSONObject.optJSONObject("data");
		}
		
		if (dataJSONObject!=null) {
			playlistsJSONArray = dataJSONObject.getJSONArray("items");
		}
		
		return playlistsJSONArray;
	}
	
	/******************************************************************************************
	 * Retrieves a JSONAray with all songs in <i><b>every</b></i> playlist. The JSONArray 
	 * contains the fields of the songs such as "id", "clientId", "trackId", etc. (for a list 
	 * of all fields, see MobileClientPlaylistEntriesSchema.java). 
	 * 
	 * @deprecated This method is fully functional. However, there are issues with retrieving 
	 * the correct playlist entryIds. Specifically, the entryIds do not seem to work with 
	 * reordering playlists via the MobileClient mutations protocol. 
	 * 
	 * @return A JSONArray object that contains all songs and their fields within every playlist. 
	 * @param context The context to use while retrieving songs from the playlist.
	 ******************************************************************************************/
	public static final JSONArray getPlaylistEntriesMobileClient(Context context) 
								  throws JSONException, IllegalArgumentException {
		
		JSONArray playlistEntriesJSONArray = new JSONArray();
		JSONObject jsonRequestParams = new JSONObject();
		
		jsonRequestParams.put("max-results", 10000);
		jsonRequestParams.put("start-token", "0");
		
		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
				 						 "https://www.googleapis.com/sj/v1.1/plentryfeed?alt=json&hl=en_US&tier=basic", 
				 						 new ByteArrayEntity(jsonRequestParams.toString().getBytes()), 
				 						 "application/json");
		
		JSONObject resultJSONObject = new JSONObject(result);
		JSONObject dataJSONObject = new JSONObject();
		
		if (resultJSONObject!=null) {
			dataJSONObject = resultJSONObject.optJSONObject("data");
		}
		
		if (dataJSONObject!=null) {
			playlistEntriesJSONArray = dataJSONObject.getJSONArray("items");
		}
		
		return playlistEntriesJSONArray;
	}
	
	/**************************************************************************************************
	 * Retrieves a JSONAray with all songs within the <b><i>specified</b></i> playlist. The JSONArray 
	 * contains the fields of the songs such as "id", "clientId", "trackId", etc. (for a list 
	 * of all fields, see WebClientSongsSchema.java). Uses the WebClient endpoint.
	 * 
	 * @return A JSONArray object that contains the songs and their fields within the specified playlist.
	 * @param context The context to use while retrieving songs from the playlist.
	 * @param playlistId The id of the playlist we need to fetch the songs from.
	 **************************************************************************************************/
	public static final JSONArray getPlaylistEntriesWebClient(Context context, String playlistId) 
							      throws JSONException, IllegalArgumentException {
		
		JSONObject jsonParam = new JSONObject();
		jsonParam.putOpt("id", playlistId);

		JSONForm form = new JSONForm();
		form.addField("json", jsonParam.toString());
		form.close();
		
		mHttpClient.setUserAgent(mMobileClientUserAgent);
		String result = mHttpClient.post(context, 
										 "https://play.google.com/music/services/loadplaylist?u=0&xt=" + getXtCookieValue(), 
				 						 new ByteArrayEntity(form.toString().getBytes()), 
				 						 form.getContentType());
		
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject(result);
		
		if (jsonObject!=null) {
			jsonArray = jsonObject.getJSONArray("playlist");
		}
		
		return jsonArray;
	}
	
	/**************************************************************************************
	 * Reorders the specified song (within the specified playlist) to a new position.
	 * 
	 * @param context The context to use during the reordering process.
	 * @param playlistId The id of the playlist which contains the song to be reordered.
	 * @param movedSongId The id of the song that is being reordered.
	 * @param movedEntryId The entryId of the song that is being reordered.
	 * @param afterEntryId The entryId of the song that is before the new position.
	 * @param beforeEntryId The entryId of the song that is after the new position.
	 * @return Returns the JSON response of the reorder task.
	 * @throws JSONException
	 **************************************************************************************/
	public static final String reorderPlaylistEntryWebClient(Context context,
														String playlistId, 
														ArrayList<String> movedSongId, 
														ArrayList<String> movedEntryId,
														String afterEntryId, 
														String beforeEntryId) throws JSONException {
		
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("playlistId", playlistId);
		jsonParam.put("movedSongIds", movedSongId);
		jsonParam.put("movedEntryIds", movedEntryId);
		jsonParam.put("afterEntryId", afterEntryId);
		jsonParam.put("beforeEntryId", beforeEntryId);
		
		String jsonParamString = jsonParam.toString();
		jsonParamString = jsonParamString.replace("\"[", "[\"");
		jsonParamString = jsonParamString.replace("]\"", "\"]");
		
		JSONForm form = new JSONForm();
		form.addField("json", jsonParamString);
		form.close();
		
		String result = mHttpClient.post(context, 
										 "https://play.google.com/music/services/changeplaylistorder?u=0&xt=" + getXtCookieValue(), 
										 new ByteArrayEntity(form.toString().getBytes()), 
										 form.getContentType());
		
		return result;
	}
	
}
