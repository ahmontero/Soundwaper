package com.siahmsoft.soundwaper.models;

import org.json.JSONException;
import org.json.JSONObject;

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
				
				if (jsonObject.has("user")) {
					jsonObject = jsonObject.getJSONObject("user");
					track.user = User.fromJSON(jsonObject.toString());
				}
				
		} catch (JSONException e) {
			e.printStackTrace();
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
}