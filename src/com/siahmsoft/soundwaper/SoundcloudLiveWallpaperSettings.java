package com.siahmsoft.soundwaper;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/*
 * 
 * Settings for wallpaper that is inflated from 'soundcloud_settings.xml'
 *
 */
public class SoundcloudLiveWallpaperSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// to get rid of the transparent background
		getListView().setBackgroundColor(Color.WHITE);
		getListView().setCacheColorHint(Color.WHITE);

		getPreferenceManager().setSharedPreferencesName(Constants.Prefs.NAME);
		addPreferencesFromResource(R.xml.soundcloud_settings);		

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}
}
