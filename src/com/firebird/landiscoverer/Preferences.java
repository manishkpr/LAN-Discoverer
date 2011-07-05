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

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Set view...
		addPreferencesFromResource(R.xml.preference);
	}
	
	
	/*Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
	        //Check that the string is an integer.
	        return numberCheck(newValue);
	    }
	};
	private boolean numberCheck(Object newValue) {
	    if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
	        return true;
	    }
	    else {
	        Toast.makeText(ActivityUserPreferences.this, newValue+" "+getResources().getString(R.string.is_an_invalid_number), Toast.LENGTH_SHORT).show();
	        return false;
	    }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //get XML preferences
	    addPreferencesFromResource(R.xml.user_preferences);
	    //get a handle on preferences that require validation
	    delayPreference = getPreferenceScreen().findPreference("pref_delay");
	    //Validate numbers only
	    delayPreference.setOnPreferenceChangeListener(numberCheckListener);
	}*/

}
