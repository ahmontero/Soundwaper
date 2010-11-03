package com.siahmsoft.soundwaper.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import com.siahmsoft.soundwaper.cache.BitmapCache;
import com.siahmsoft.soundwaper.cache.TracksCache;
import com.siahmsoft.soundwaper.models.Track;

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
 * Manage all the requests to Soundcloud
 *
 */
public class SoundcloudApi {
	
	private static final String TAG = SoundcloudApi.class.getSimpleName();
	
	public static final String API_ENDPOINT_URL = "http://api.soundcloud.com/tracks.json";

    public static final String API_KEY = "J4D4ODBNeuHeXTQlbwUp3A";

    private final NetManager netMgr = new NetManager();
    
    private final Random roulette = new Random();
    
    public Pair<Bitmap, Track> retrieveImage(String username){
    	List<Track> tracks = null;
    	
    	if(TracksCache.isInCache(username)){
    		Log.d(TAG, "Bingo! The user " + username + " has cached data");
    		Map<String, Track> cachedTracks = TracksCache.get(username);
    		
    		if(cachedTracks != null && !cachedTracks.isEmpty()){
    			tracks = TracksCache.getAll(username);
    		}else{
    			TracksCache.clearCache();
    			tracks = getUserTracks(username);
    		}
    	}else{
    		TracksCache.clearCache();
    		tracks = getUserTracks(username);
    	}
  		
        return getRandomBitmap(tracks);
	}
    
    private List<Track> getUserTracks(String username){
    	URL url = null;
    	List<Track> tracks;
    	
		StringBuilder builder = new StringBuilder(SoundcloudApi.API_ENDPOINT_URL);
        builder.append('?').append("consumer_key=").append(SoundcloudApi.API_KEY).append('&').append("q=").append(username);
                
        try {            
        	url = new URL(builder.toString());
            Log.d(TAG, "Requesting static image from Soundcloud=[" + url + "]");

        } catch (MalformedURLException error) {
            error.printStackTrace();
        }
        
    	tracks = retrieveTracks(url);
    	
    	if(tracks == null){
    		return new ArrayList<Track>();
    	}
    	
    	TracksCache.putAll(username, tracks);
    	
    	return tracks;
    }
    
    private List<Track> retrieveTracks(URL url) {
		List<Track> tracks = null;
		
		 String responseAsString = null;
	        int retries = 0;
	        do {
	            if (retries > 0) {
	                Log.e(TAG, "Couldn't retrieve Tracks. Retrying: " + retries);
	            }
	            try {
	                HttpResponse response = netMgr.getHTTPResponse(url);
	                InputStream input = response.getEntity().getContent();
	                responseAsString = convertStreamToString(input);	                
	                tracks = convertJsonToTracks(responseAsString);
	            }catch (IllegalArgumentException e) {
	                Log.e(TAG, "Could not retrieve tracks with null URL", e);
	            }catch (IOException e) {
	                Log.e(TAG, "Could not retrieve tracks from resulting httpResponse", e);
	            }
	            retries++;
	        } while (responseAsString == null && retries < 3);

	        return tracks;
	}
    
    private List<Track> convertJsonToTracks(String responseAsString) {
		List<Track> tracks = new ArrayList<Track>();
		
		try {
			JSONArray tracksArray = new JSONArray(responseAsString);
						
			for(int i = 0; i < tracksArray.length(); i++){
				JSONObject track = (JSONObject) tracksArray.get(i);	
				Log.d(TAG, "Parsing JSON response: [" + track.toString() + "]");
				tracks.add(Track.fromJSON(track));
			}
			
		} catch (JSONException e) {
			Log.e(TAG, "Could not obtains tracks from response", e);
		}
		
		return tracks;
	}
    
    private Pair<Bitmap, Track> getRandomBitmap(List<Track> tracks) {
    	
    	Track track = null;
    	Bitmap bitmap = null;
    	int randomIndex = 0;
		
    	if(tracks.size() > 0){
			randomIndex = roulette.nextInt(tracks.size());
			
			track = tracks.get(randomIndex);
			
			if(track != null){
				try {
					bitmap = retrieveBitmap(new URL(track.getWaveformUrl()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}	

		return new Pair<Bitmap, Track>(bitmap, track);
	}

    public Bitmap retrieveBitmap(URL photoUrl) {
        Bitmap bitmap = null;

        if(BitmapCache.isInCache(photoUrl.toString())){
        	bitmap = BitmapCache.get(photoUrl.toString());
        }else{
        	int retries = 0;
            do {
                if (retries > 0) {
                    Log.e(TAG, "Couldn't retrieve Image. Retrying for " + retries + " time");
                }
                try {
                    HttpResponse response = netMgr.getHTTPResponse(photoUrl);
                    Object input = response.getEntity().getContent();                    
                    InputStream inputStream = (InputStream) input;
                	bitmap = BitmapFactory.decodeStream(inputStream);
                	BitmapCache.put(photoUrl.toString(), bitmap);
                }catch (IllegalArgumentException e) {
                    Log.e(TAG, "Could not retireve bitmap wth null URL", e);
                }catch (IOException e) {
                    Log.e(TAG, "Could not retrieve bitmap from resulting httpResponse", e);
                }
                retries++;
            } while (bitmap == null && retries < 3); 
        }       

        return bitmap;
    }

	public String convertStreamToString(InputStream is) throws IOException {
		 if (is != null) {
			 Writer writer = new StringWriter();
		 
		     char[] buffer = new char[1024];
		     try {
		    	 Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		         int n;
		         while ((n = reader.read(buffer)) != -1) {
		        	 writer.write(buffer, 0, n);
		         }
		     } finally {
		    	 is.close();
		     }
		     return writer.toString();
		 } else {       
			 return "";
		 }
	}
}