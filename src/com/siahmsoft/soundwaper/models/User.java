package com.siahmsoft.soundwaper.models;

import org.json.JSONException;
import org.json.JSONObject;

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
 * Represents an User from Soundcloud. As this is an example app, the model is pretty basic
 * 
 */
public class User {
	private static final String TAG = User.class.getSimpleName();

	private int id;
	private String permalink;
	private String username;

	public static User fromJSON(String jsonUser) {
		User user = null;

		try {
				JSONObject jsonObject = new JSONObject(jsonUser);
				user = new User();
	
				if (jsonObject.has("type")) {
					jsonObject = jsonObject.getJSONObject("user");
				}
	
				user.id = jsonObject.getInt("id");
				user.permalink = jsonObject.getString("permalink");
				user.username = jsonObject.getString("username");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user;
	}

	public static User fromJSON(JSONObject jsonTrack) {
		return fromJSON(jsonTrack.toString());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}