/*
 * This file is part of LAN Discoverer for Android.
 *
 * LAN Discoverer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LAN Discoverer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LAN Discoverer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.firebird.landiscoverer;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {

	Context context;

	OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
	    	if(!newValue.toString().equals("")  &&  newValue.toString().matches("\\d*")) {
		        return true;
		    } else {
		        Toast.makeText(context, getResources().getString(R.string.error_not_numerical_value), Toast.LENGTH_SHORT).show();
		        return false;
		    }
	    }
	};

	OnPreferenceClickListener clearSettingsListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
				Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
				editor.putString(getResources().getString(R.string.prefs_keys_ping_timeout), getResources().getString(R.string.prefs_defs_ping_timeout));
				editor.putString(getResources().getString(R.string.prefs_keys_max_threads), getResources().getString(R.string.prefs_defs_max_threads));
				editor.commit();
				return true;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Set view...
		addPreferencesFromResource(R.xml.preference);

		//Define elements...
		context = this;
		PreferenceScreen preferenceScreen = getPreferenceScreen();

		//Set listeners...
		preferenceScreen.findPreference(getResources().getString(R.string.prefs_keys_ping_timeout)).setOnPreferenceChangeListener(numberCheckListener);
		preferenceScreen.findPreference(getResources().getString(R.string.prefs_keys_max_threads)).setOnPreferenceChangeListener(numberCheckListener);
		preferenceScreen.findPreference(getResources().getString(R.string.prefs_keys_clear)).setOnPreferenceClickListener(clearSettingsListener);
	}
}
