package com.soundcloud.adc.models;

import org.json.JSONException;
import org.json.JSONObject;

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