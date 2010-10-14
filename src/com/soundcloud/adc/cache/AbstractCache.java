package com.soundcloud.adc.cache;

public abstract class AbstractCache {
	
	//30 minutes
	private static final long CACHE_EXPIRATION_TIME = 1800000;
	
	private static long mCreationDate;
		
	protected static boolean isOutdated(long timeMilis){
		return timeMilis - mCreationDate > CACHE_EXPIRATION_TIME;
	}
}