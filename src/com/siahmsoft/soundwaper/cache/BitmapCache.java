package com.siahmsoft.soundwaper.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.os.SystemClock;
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