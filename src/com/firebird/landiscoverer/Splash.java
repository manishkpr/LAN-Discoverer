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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;

public class Splash extends Activity {

	WifiManager wifi;
	Activity activity;
	AlertDialog failedDialog, askDialog;
	BroadcastReceiver broadcastReceiver;
	IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Fill screen...
		setContentView(R.layout.splash_screen);

		//Define elements...
		wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		activity = this;

		//Failed dialog...
		Builder fDialog = new AlertDialog.Builder(activity);
		fDialog.setTitle(R.string.splash_failed_dialog_error);
		fDialog.setMessage(R.string.splash_failed_dialog_msg);
		fDialog.setNeutralButton(R.string.splash_failed_dialog_ok_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Exit...
				activity.finish();
			}
		});
		failedDialog = fDialog.create();

		//Ask dialog...
		Builder aDialog = new AlertDialog.Builder(activity);
		aDialog.setTitle(R.string.splash_ask_dialog_title);
		aDialog.setMessage(R.string.splash_ask_dialog_msg);
		aDialog.setPositiveButton(R.string.splash_ask_dialog_yes_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(!wifi.setWifiEnabled(true))
					failedDialog.show();
			}
		});
		aDialog.setNegativeButton(R.string.splash_ask_dialog_no_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Exit...
				activity.finish();
			}
		});
		askDialog = aDialog.create();

		//Intent filters...
		intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				//Code to execute when WIFI_STATE_CHANGED_ACTION event occurs
				switch(i.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)) {
				case WifiManager.WIFI_STATE_UNKNOWN:
					failedDialog.show();
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					startActivity(new Intent(activity, MainActivity.class));
					activity.finish();
				case WifiManager.WIFI_STATE_DISABLED:
					askDialog.show();
					break;
				}
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();

		//Check wifi state...
		if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
			askDialog.show();

		//Register receivers...
		registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	public void onPause(){
		super.onPause();

		//Unregister receivers...
		unregisterReceiver(broadcastReceiver);

		//Dismiss dialogs...
		askDialog.dismiss();
		failedDialog.dismiss();
	}
}
