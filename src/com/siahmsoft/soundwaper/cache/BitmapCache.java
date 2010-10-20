package com.siahmsoft.soundwaper.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

/**
 * 
 * Class that caches Bitmap objects.
 * 
 */
public class BitmapCache extends AbstractCache{
	private static final String TAG = BitmapCache.class.getSimpleName();
	
	private static Map<String, Bitmap> cache = new ConcurrentHashMap<String, Bitmap>();
	

	public static boolean isInCache(String cacheKey) {
		return cache.containsKey(cacheKey);
	}
	
	public static Bitmap get(String cacheKey) {
		return cache.get(cacheKey);
	}
	
	public static void put(String cacheKey, Bitmap bitmap) {
		if(isOutdated(SystemClock.elapsedRealtime())){
			Log.d(TAG, "Cleaning" + cache.size() + " tracks from cache region ["+ cacheKey +"]");
			cache.clear();
		}
		cache.put(cacheKey, bitmap); 
	}	
}