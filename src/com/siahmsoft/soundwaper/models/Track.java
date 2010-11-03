package com.siahmsoft.soundwaper.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/*
* Copyright (C) 2010 Siahmsoft
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

/**
 * 
 * Represents a Track from Soundcloud. As this is an example app, the model is pretty basic
 *
 */
public class Track {
	
	private static final String TAG = Track.class.getSimpleName();
	
	private int id;
	private int userId;
	private String waveformUrl;
	private User user;
	private String url;
	private String title;
	
	private int commentsCount;
	private int playbackCount;
	private String favoritingsCount;
	
	
	public static Track fromJSON(String jsonTrack){
		Track track = null;
		
		try {
				JSONObject jsonObject = new JSONObject(jsonTrack);
				track = new Track();
				
				if (jsonObject.has("type")) {
                	jsonObject = jsonObject.getJSONObject("track");
        		}
				
				track.id = jsonObject.getInt("id");
				track.userId = jsonObject.getInt("user_id");
				track.waveformUrl = jsonObject.getString("waveform_url");
				track.url = jsonObject.getString("permalink_url"); 
				track.title = jsonObject.getString("title");
				track.favoritingsCount = jsonObject.getString("favoritings_count");
				track.commentsCount = jsonObject.getInt("comment_count");
				track.playbackCount = jsonObject.getInt("playback_count");
				
				/*"genre":"Chilled",
				"track_type":"remix",
				"isrc":"",
				"release_day":null,
				"release_year":null,
				"state":"finished",
				"favoritings_count":2,
				"download_count":0,
				"artwork_url":null,
				"downloadable":false,
				"id":3552246,
				"title":"Missy Higgins They weren't there Quano Cover.",
				"sharing":"public",
				"label_name":"",
				"video_url":null,
				"description":"Cover from Missy Higgings \"Track They Weren't there\", the original track had only vocal and piano , in this song i add instrumental and drums arrangements.",
				"streamable":false,
				"created_at":"2010/06/20 18:06:01 +0000",
				"permalink_url":"http://soundcloud.com/quano/missy-higgins-they-werent-there-quano-cover",
				"user_id":118865,
				"original_format":"mp3",
				"license":"cc-by-nc-nd",
				"commentable":true,
				"comment_count":1,
				"purchase_url":null,
				"playback_count":19,
				"label_id":null,
				"uri":"http://api.soundcloud.com/tracks/3552246",
				"key_signature":"",
				"bpm":null,
				"duration":244755,
				"permalink":"missy-higgins-they-werent-there-quano-cover",
				"tag_list":"",
				"release_month":null,
				"user":{"id":118865,"avatar_url":"http://a1.soundcloud.com/images/default_avatar_large.png?65d805","permalink_url":"http://soundcloud.com/quano","username":"quano","permalink":"quano","uri":"http://api.soundcloud.com/users/118865"},
				"waveform_url":"http://waveforms.soundcloud.com/xfF180zvz8sV_m.png",
				"release":""}*/
				
				if (jsonObject.has("user")) {
					jsonObject = jsonObject.getJSONObject("user");
					track.user = User.fromJSON(jsonObject.toString());
				}
				
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return track;
	}
	
	public static Track fromJSON(JSONObject jsonTrack){
		return fromJSON(jsonTrack.toString());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getWaveformUrl() {
		return waveformUrl;
	}

	public void setWaveformUrl(String waveformUrl) {
		this.waveformUrl = waveformUrl;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}

	public int getPlaybackCount() {
		return playbackCount;
	}

	public void setPlaybackCount(int playbackCount) {
		this.playbackCount = playbackCount;
	}

	public String getFavoritingsCount() {
		return favoritingsCount;
	}

	public void setUserFavoritingsCount(String userPlaybackCount) {
		this.favoritingsCount = userPlaybackCount;
	}
}