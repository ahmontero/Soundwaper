package com.siahmsoft.soundwaper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.siahmsoft.soundwaper.models.Track;
import com.siahmsoft.soundwaper.net.SoundcloudApi;

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

public class SoundcloudLiveWallpaper extends WallpaperService {
	private static final String TAG = SoundcloudLiveWallpaper.class.getSimpleName();

	@Override
	public Engine onCreateEngine() {
		return new SoundcloudEngine();
	}

	class SoundcloudEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

		private SoundcloudApi mSoundcloudApi;
		
		private SharedPreferences mPrefs;
		
		private float mXOffset;
        private boolean mVisible;
        
        private Bitmap mWaveform;
        private Track mTrack;
		
		private final Handler mHandler = new Handler();

		private final Runnable fetchTrack = new Runnable() {
			public void run() {
				
				mHandler.removeCallbacks(fetchTrack);
				
				fetchTrack();
				drawImage();
		            
		        if (mVisible) {
		        	mHandler.postDelayed(fetchTrack, mTimer);
		        }
			}
		};

		private Integer mTimer;

		private boolean mShowTrackInfo;
		private boolean mClickToChangeTrack;

		private float mCenterX;
		private float mCenterY;
		
		SoundcloudEngine(){
			//mCurrentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
			mSoundcloudApi = new SoundcloudApi();

			// register the listener to detect preference changes
			mPrefs = SoundcloudLiveWallpaper.this.getSharedPreferences(Constants.Prefs.NAME, MODE_PRIVATE);
			mPrefs.registerOnSharedPreferenceChangeListener(this);

			// initialize the starting preferences
			onSharedPreferenceChanged(mPrefs, null);
			setTouchEventsEnabled(true);

			if (!mPrefs.contains(Constants.Prefs.USER_KEY)) {
				Editor edit = mPrefs.edit();
				edit.putString(Constants.Prefs.USER_KEY, Constants.Prefs.DEFAULT_USER);
				edit.commit();
			}

			updateSettings();
		}
		
		private void fetchTrack(){

			if (isConnectedToInternet()) {
				Log.d(TAG, "Device is connected to Internet...");
				
				String user = mPrefs.getString(Constants.Prefs.USER_KEY, Constants.Prefs.DEFAULT_USER);
				
				Log.d(TAG, "Fetching track...");
				Pair<Bitmap, Track> soundcloudResult = mSoundcloudApi.retrieveImage(user);
				
				mWaveform = soundcloudResult.first == null?BitmapFactory.decodeResource(getResources(), R.drawable.icon):soundcloudResult.first;
				mTrack = soundcloudResult.second;

				if (mWaveform == null || mTrack == null) {
					Log.e(TAG, "I'm not sure what went wrong but waveform could not be retrieved");
					throw new IllegalStateException("Whoops! We had problems retrieving the waveform. Please try again.");
				}
			}else{
				Log.d(TAG, "Device is NOT connected to Internet");
				notifyNoInternet();
				//We will try to fetch tracks later
				mHandler.postDelayed(fetchTrack, mTimer);
			}
		}
		
		private boolean isConnectedToInternet(){
			ConnectivityManager mConnectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			return mConnectivityManager != null && mConnectivityManager.getActiveNetworkInfo() != null && mConnectivityManager.getActiveNetworkInfo().isConnected();
		}
		
		protected void drawImage() {
			if (isVisible()) {
				Log.d(TAG, "Drawing image...");

				final SurfaceHolder holder = getSurfaceHolder();

	            Canvas c = null;
	            try {
	                c = holder.lockCanvas();
	                if (c != null) {
	                	draw(c);    
	                }
	            } finally {
	                if (c != null) holder.unlockCanvasAndPost(c);
	            }
			}
		}
		
		private void draw(Canvas c){
			
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.parseColor("#FF8C4A"));
            paint.setTextAlign(Paint.Align.CENTER);
            
			c.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon), mCenterX/2, mCenterY/2, paint);
			
            if(mWaveform != null){
            	c.drawColor(Color.parseColor("#FF8C4A"));
            	
            	paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.GRAY);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(15);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            	final Rect frame = getSurfaceHolder().getSurfaceFrame();
            	
            	/*Matrix matrix = new Matrix();
                matrix.postScale(metrics.widthPixels/mWaveform.getWidth(), metrics.heightPixels/mWaveform.getHeight());
                Bitmap b = Bitmap.createBitmap(mWaveform, 0, 0, mWaveform.getWidth(), mWaveform.getHeight(), matrix, true);*/
                
                c.drawBitmap(mWaveform, null, frame, paint);
               
                if(mShowTrackInfo){
                	
                	int y = 125;
                    y -= paint.ascent() / 2;
                    
                    int left = (int) (mXOffset + (mWaveform.getWidth() - frame.width()) / 2);
        			Rect window = new Rect((int) (frame.right + mXOffset), frame.top, left + frame.right, frame.bottom);
                    
                	String title = mTrack.getUser().getUsername() + " - " + mTrack.getTitle();
                    String playbacks = "Playbacks: " + mTrack.getPlaybackCount();
                    String favorites = "Favorites: " + mTrack.getFavoritingsCount();
                    String comments = "Comments: " + mTrack.getCommentsCount();
                    
                	c.drawText(title, window.left + 25, 40, paint);
                    
                    c.drawText(playbacks, window.left + mXOffset + 50, y, paint);
                    
                    c.drawText(favorites, window.left + mXOffset + paint.measureText(playbacks) + 50, y, paint);
                    
                    c.drawText(comments, window.left + mXOffset + paint.measureText(playbacks) + paint.measureText(favorites) + 60, y, paint);
                }
            }else{
            	mHandler.postDelayed(fetchTrack, mTimer);
            }
              
		}
		
		private DisplayMetrics getScreenSize() {
			DisplayMetrics metrics = new DisplayMetrics();
			Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			display.getMetrics(metrics);
			
			return metrics;
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);	
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(fetchTrack);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mHandler.removeCallbacks(fetchTrack);
		}
		
		@Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mCenterX = width/2.0f;
            mCenterY = height/2.0f;
            drawImage();
		}
		 
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			mXOffset = xPixelOffset;
			drawImage();
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
            if (visible) {
            	//mHandler.post(fetchTrack);
            	drawImage();
            } else {
                mHandler.removeCallbacks(fetchTrack);
            }
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
			Intent intent = null;
			Log.i(TAG, "An action going on" + action);
			if (action.equals(WallpaperManager.COMMAND_TAP)) {

				updateSettings();
				
				if (mClickToChangeTrack) {
					mHandler.post(fetchTrack);
				} else {
					try {
						//Show track url
						if(mTrack != null){
							Log.i(TAG, "Browsing to track=[" + mTrack.getUrl() + "]");
							intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mTrack.getUrl()));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					} catch (NullPointerException e) {
						Log.e(TAG, "Soundcloud track URL was null", e);
					}
				}
			}

			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
		}

		private void notifyNoInternet() {
			NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			Notification notif = new Notification(R.drawable.icon, getText(R.string.network_not_available), System.currentTimeMillis());
			notif.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name), getText(R.string.network_not_available), contentIntent);
			nm.notify(R.string.app_name, notif);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String keyChanged) {
			Log.i(TAG, "Shared Preferences changed: " + keyChanged);
			updateSettings();
			//When the user changes the preferences,it is necessary to refresh the waveform
			mHandler.post(fetchTrack);
		}
		
		private void updateSettings(){
			mTimer = Integer.valueOf(mPrefs.getString(Constants.Prefs.TIMER_KEY, "5000"));
			mShowTrackInfo = mPrefs.getBoolean(Constants.Prefs.SHOW_TRACK_INFO_KEY, true);
			mClickToChangeTrack = mPrefs.getBoolean(Constants.Prefs.CLICK_TO_CHANGE_KEY, true);
		}
	}
} 