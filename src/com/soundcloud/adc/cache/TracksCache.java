package com.soundcloud.adc.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.soundcloud.adc.models.Track;

/**
 * 
 * Class that caches Track objects
 *
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