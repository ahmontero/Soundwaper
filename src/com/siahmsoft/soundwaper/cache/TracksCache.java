package com.siahmsoft.soundwaper.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

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

public final class TracksCache extends AbstractCache{
	private static final String TAG = TracksCache.class.getSimpleName();
	
	private static Map<String, Map<String, Track>> cache = new ConcurrentHashMap<String, Map<String, Track>>();
	private static Map<String, Track> tracksCache = new ConcurrentHashMap<String, Track>();
		
	
	public static boolean isInCache(String keyTrack) {
		return cache.containsKey(keyTrack);
	}
	
	public static Map<String, Track> get(String username) {
		return cache.get(username);
	}
	
	public static void putAll(String username, List<Track> tracks){
		for(Track track : tracks){
			Log.d(TAG, "Storing track from method 'putAll' with id ["+ track.getId() +"]" + " to cache region [" + username + "]");
			tracksCache.put(track.getUrl(), track);	
			cache.put(username, tracksCache);
		}	
		
		Log.d(TAG, "Cache region ["+ username +"]" + "contains [" + cache.get(username).keySet().size() + "] objects");
	}
	
	public static List<Track> getAll(String username){
		List<Track> tracks = new ArrayList<Track>();
		
		for(Track track : cache.get(username).values()){
			Log.d(TAG, "Obtaining track from method 'getAll' with id ["+ track.getId() +"]" + " from cache region [" + username + "]");
			tracks.add(track);
		}
		
		return tracks;
	}
	
	public static void put(String username, Pair<String, Track> toCache) {		
		if(isOutdated(SystemClock.elapsedRealtime())){
			Log.d(TAG, "Cleaning " + cache.get(username).keySet().size() + " tracks from cache region ["+ username +"]");
			tracksCache.clear();
			
			Log.d(TAG, "Cleaning " + cache.size() + " objects from cache region ["+ username +"]");
			cache.clear();
		}
		
		tracksCache.put(toCache.first, toCache.second);		
		cache.put(username,tracksCache);
	}
	
	public static void clearCache(){
		Log.d(TAG, "Received request to clean all data cached...");
		tracksCache.clear();
		cache.clear();		
		Log.d(TAG, "Done");		
	}
}