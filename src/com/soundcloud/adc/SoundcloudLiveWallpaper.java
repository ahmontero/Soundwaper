package com.soundcloud.adc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.soundcloud.adc.models.Track;
import com.soundcloud.adc.net.SoundcloudApi;

/**
 * 
 * Soundcloud Wallpaper 
 *
 */
public class SoundcloudLiveWallpaper extends WallpaperService {
	private static final String TAG = SoundcloudLiveWallpaper.class.getSimpleName();
	
	@Override
	public Engine onCreateEngine() {
		return new SoundcloudEngine();
	}

	class SoundcloudEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private boolean mScroll;
		private boolean mStretch;
		private boolean mClickToChange;
		private boolean mImageReady;
		
		private int mXAxisPixelOffset;				
		private int mDesiredMinimumWidth;
		private int mScreenHeight;
		private int mScreenWidth;
		private int mTransition;
		private int mCurrentAlpha;
		private long mTimer;
		private long mStartTime;
		private float mXOffset;
		private float mXOffsetStep;
		
		private Bitmap mCurrentBitmap;
		private Paint mDefaultPaint;
		private Paint mImagePaint;
		private Rect mSurfaceFrame;		
		private SharedPreferences mPrefs;		
		private SoundcloudApi mSoundcloudApi;
		private Track mCurrentTrack;
		
		private final Handler mHandler = new Handler();
		
		private final Runnable mSlideShow = new Runnable() {
			public void run() {
				(new Thread(){
					public void run(){
						if (SystemClock.elapsedRealtime() - mStartTime > mTimer) {
							showNewImage();
						}else {
							mHandler.postDelayed(mSlideShow, mTimer - (SystemClock.elapsedRealtime() - mStartTime));
						}
					}
				}).start();
				
			}
		};
		
		private final Runnable mSlideShowFade = new Runnable() {
			public void run() {
				(new Thread(){
					public void run(){
						showNewImageWithFadeTransition(mCurrentBitmap, mCurrentAlpha);
					}
				}).start();
			}
		 };
		 
		 @Override
		 public void onCreate(SurfaceHolder surfaceHolder) {
			 super.onCreate(surfaceHolder);
			 initializeValues();
			 createPainters();
			 getScreenSize();	
		 }
		
		private void initializeValues(){
			mCurrentAlpha = 0;	
			mImageReady = false;
			
			mCurrentBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.icon);
			
			mSurfaceFrame = getSurfaceHolder().getSurfaceFrame();	
			
			mSoundcloudApi = new SoundcloudApi();
						
			// register the listener to detect preference changes
			mPrefs = getSharedPreferences(Constants.Prefs.NAME, MODE_PRIVATE);
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
			
			mDesiredMinimumWidth = getDesiredMinimumWidth();
			
			mStartTime = SystemClock.elapsedRealtime();
		}
		
		private void updateSettings(){
			mTimer = Integer.valueOf(mPrefs.getString(Constants.Prefs.TIMER_KEY, "5000"));
			mScroll = mPrefs.getBoolean(Constants.Prefs.SCROLLING_KEY, true);
			mStretch = mPrefs.getBoolean(Constants.Prefs.STRETCHING_KEY, false);
			mClickToChange = mPrefs.getBoolean(Constants.Prefs.CLICK_TO_CHANGE_KEY, false);			
			mTransition = Integer.valueOf(mPrefs.getString(Constants.Prefs.TRANSITION_KEY, "0"));
		}
		
		private void createPainters(){
			mImagePaint = new Paint();
			mImagePaint.setAlpha(255);

			if (mTransition != Constants.Prefs.FADE_TRANSITION) {
				mImagePaint.setAlpha(255);
			}
			
			mDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mDefaultPaint.setTextAlign(Paint.Align.CENTER);
			mDefaultPaint.setColor(Color.GRAY);
			mDefaultPaint.setTextSize(24);	
		}
		
		private void getScreenSize() {
			DisplayMetrics metrics = new DisplayMetrics();
			Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			display.getMetrics(metrics);

			switch(getResources().getConfiguration().orientation){
				case Configuration.ORIENTATION_LANDSCAPE:
					this.mScreenWidth = metrics.heightPixels;
					this.mScreenHeight = metrics.widthPixels;
					break;
					
				case Configuration.ORIENTATION_PORTRAIT:
					this.mScreenHeight = metrics.heightPixels;
					this.mScreenWidth = metrics.widthPixels;
					break;
					
				default:
					this.mScreenHeight = metrics.heightPixels;
					this.mScreenWidth = metrics.widthPixels;
					break;
			}				
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mSlideShowFade);
			mHandler.removeCallbacks(mSlideShow);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {	
			super.onSurfaceDestroyed(holder);
			mHandler.removeCallbacks(mSlideShowFade);
			mHandler.removeCallbacks(mSlideShow);		
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			if (this.mXAxisPixelOffset != xPixelOffset * -1 || this.mXOffset != xOffset || this.mXOffsetStep != xOffsetStep) {
				this.mXAxisPixelOffset = xPixelOffset * -1;
				this.mXOffset = xOffset;
				this.mXOffsetStep = xOffsetStep;
				drawBitmap(mCurrentBitmap);
			}
		}		

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible){
				mHandler.post(mSlideShow);
			} else {
				mHandler.removeCallbacks(mSlideShowFade);
				mHandler.removeCallbacks(mSlideShow);
			}
		}

		/**
		 * This happens on launcher rotation
		 */
		@Override
		public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
			super.onDesiredSizeChanged(desiredWidth, desiredHeight);
			Log.v(TAG, "onDesiredSizeChanged");
			getScreenSize();
			this.mDesiredMinimumWidth = desiredWidth;

			drawBitmap(mCurrentBitmap);
		}
		

		/**
		 * Draw the bitmap to the surface canvas
		 * 
		 * @param bitmap the bitmap to draw
		 */
		private void drawBitmap(Bitmap bitmap) {
			if (bitmap == null) {
				Log.d(TAG, "Waveform bitmap is null! There is nothing to draw on canvas!");
				return;
			}
			
			final SurfaceHolder holder = getSurfaceHolder();
			int virtualWidth = mDesiredMinimumWidth;

			if (mSurfaceFrame == null) {
				Log.d(TAG, "surfaceFrame == null!");
			}		
			
			Rect window;
			Rect dstWindow = mSurfaceFrame;
			if (!mScroll) {
				// virtual width becomes screen width
				virtualWidth = mScreenWidth;
				mXAxisPixelOffset = 0;
			}

			// virtualWidth must be greater than 0
			if (virtualWidth == 0) {
				Log.d(TAG, "Can not draw bitmap because 'virtualWidth == 0'");
				return;
			}
			
			int virtualHeight = mScreenHeight;
					                                                         
			Log.d(TAG, "Waveform on screen dimensions: [Width:" + virtualWidth + "], [Height:" + virtualHeight + "]");

			if (bitmap == mCurrentBitmap && !mImageReady) {

				// scale the bitmap to fit the screen as well as possible
				if (mStretch) {
					Log.d(TAG, "Waveform dimensions: [Width:" + bitmap.getWidth() + "], [Height:" + bitmap.getHeight() + "]");
					float scale = 0;
					if (virtualHeight - bitmap.getHeight() < virtualWidth - bitmap.getWidth()) {
						// vertically
						scale = (float) virtualHeight / bitmap.getHeight();
						Log.d(TAG, "Vertical scale: " + scale);
					} else {
						// horizontally
						scale = (float) virtualWidth / bitmap.getWidth();
						Log.d(TAG, "Horizontal scale: " + scale);
					}

					bitmap = BitmapHelper.scale(bitmap, scale);
				}

				mImageReady = true;
				mCurrentBitmap = bitmap;
			}

			int vertMargin = (bitmap.getHeight() - virtualHeight) / 2;

			if (bitmap.getWidth() >= virtualWidth && bitmap.getHeight() >= virtualHeight) {
				int pictureHorizOffset = mXAxisPixelOffset + (bitmap.getWidth() - virtualWidth) / 2;
				window = new Rect(pictureHorizOffset, vertMargin, pictureHorizOffset + mSurfaceFrame.right, bitmap.getHeight() - vertMargin);
			} else {
				int pictureHorizOffset = mXAxisPixelOffset + (bitmap.getWidth() - virtualWidth) / 2;

				window = null;
				dstWindow = new Rect(mSurfaceFrame);
				dstWindow.top = -vertMargin;
				dstWindow.bottom = bitmap.getHeight() - vertMargin;
				dstWindow.left = -pictureHorizOffset;
				dstWindow.right = -pictureHorizOffset + bitmap.getWidth();
			}

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null && bitmap != null) {
					c.drawColor(Color.parseColor("#FF8C4A")); //#FF8C4A FF9933
					c.drawBitmap(bitmap, window, dstWindow, mImagePaint);
			
					//It would be great if the text scrolls along the waveform.	
					//It is necessary a bit of googling
					if(mCurrentTrack != null && mCurrentTrack.getUser() != null){
						c.drawText((mCurrentTrack.getUser()).getUsername(), mScreenWidth/2, (mScreenHeight/2) - 50, mDefaultPaint);
						c.drawText(mCurrentTrack.getTitle(), mScreenWidth/2, (mScreenHeight/2), mDefaultPaint);
					}
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}		
		
		@Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            Intent intent = null;
            Log.i(TAG, "An action going on" + action);
            if (action.equals(WallpaperManager.COMMAND_TAP)) {

                boolean tappingOpt = mPrefs.getBoolean(Constants.Prefs.CLICK_TO_CHANGE_KEY, mClickToChange);

                if (tappingOpt) {
                    showNewImage();
                } else {
                    try {
                    	//Show track url
                    	if(mCurrentTrack != null){
                    		Log.i(TAG, "Browsing to track=[" + mCurrentTrack.getUrl() + "]");
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentTrack.getUrl()));
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

		/**
		 * Called whenever you want a new image. This will also post the message for when the next frame should be drawn
		 */
		protected void showNewImage() {
			Log.d(TAG, "Refreshing image...");
			
			Pair<Bitmap, Track> soundcloudResult = requestImageAndTrack();
			
			//cancelAnyNotifications();

			mCurrentBitmap = soundcloudResult.first;

			if (mCurrentBitmap == null) {
				Log.e(TAG, "I'm not sure what went wrong but waveform could not be retrieved");
				throw new IllegalStateException("Whoops! We had problems retrieving the waveform. Please try again.");
			}

			mCurrentTrack = soundcloudResult.second;

			//mImageReady to false because it is necessary to resize the image
			mImageReady = false;

			switch (mTransition) {
				case Constants.Prefs.FADE_TRANSITION:
					showNewImageWithFadeTransition(mCurrentBitmap, 0);
					break;
				default:
					drawBitmap(mCurrentBitmap);
					break;
			}

			mHandler.removeCallbacks(mSlideShow);

			if (isVisible()) {
				mHandler.postDelayed(mSlideShow, mTimer);
			}
		}
		
		private Pair<Bitmap, Track> requestImageAndTrack() {
			Pair<Bitmap, Track> soundcloudResult = null;		
			ConnectivityManager mConnectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			
			//If we have Internet connection
			if (mConnectivityManager.getActiveNetworkInfo().isConnected()) {
				String user = mPrefs.getString(Constants.Prefs.USER_KEY, Constants.Prefs.DEFAULT_USER);
				soundcloudResult = mSoundcloudApi.retrieveImage(user);
			}else{
				notifyNoInternet();
			}
			
			return soundcloudResult;
		}
		
		private void notifyNoInternet() {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            Notification notif = new Notification(R.drawable.icon, getText(R.string.network_not_available), System.currentTimeMillis());
            notif.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name), getText(R.string.network_not_available), contentIntent);
            nm.notify(R.string.app_name, notif);
        }

		/**
		 * Execute a fade transition. 
		 */
		private void showNewImageWithFadeTransition(Bitmap bitmap, int alpha) {
			mCurrentAlpha = alpha;
			mCurrentAlpha += 255 / 25;
			if (mCurrentAlpha > 255) {
				mCurrentAlpha = 255;
			}
			// Log.v(TAG, "alpha " + currentAlpha);
			mImagePaint.setAlpha(mCurrentAlpha);
			drawBitmap(bitmap);

			//remove old callback and draw new image
			mHandler.removeCallbacks(mSlideShowFade);
			
			if (isVisible() && mCurrentAlpha < 255) {
				mHandler.post(mSlideShowFade);
			}
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String keyChanged) {			
			if (keyChanged != null) {
                Log.i(TAG, "Shared Preferences changed: " + keyChanged);
                updateSettings();
                //When the user changes the preferences,it is necessary to refresh the waveform
                mHandler.post(mSlideShow);
            }
		}
	}
} 