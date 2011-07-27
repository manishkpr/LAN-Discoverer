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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private ProgressBar progressBar;
	private Button startStopButton;
	private ScanTask scanTask;
	private WifiManager wifi;
	private Context context;
	private SharedPreferences prefs;
	private ArrayList<String[]> hostsList;
	private ListViewAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Set view...
		setContentView(R.layout.main);

		//Define elements...
		context = this;
		scanTask = new ScanTask();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		wifi = (WifiManager)context.getSystemService(WIFI_SERVICE);
		startStopButton = (Button) findViewById(R.id.main_start_stop_button);
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
		hostsList = new ArrayList<String[]>();
		adapter = new ListViewAdapter(context, R.id.main_list_view, hostsList);
		((ListView) findViewById(R.id.main_list_view)).setAdapter(adapter);

		//Set on click listeners...
		startStopButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(scanTask.getStatus() == AsyncTask.Status.RUNNING)
			scanTask.cancel(true);
		else if(scanTask.getStatus() == AsyncTask.Status.PENDING)
		{
			if(!wifi.isWifiEnabled()){
				Toast.makeText(this, R.string.main_enable_wifi, Toast.LENGTH_LONG).show();
				return;
			}
			scanTask.execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_menu_preferences:
			startActivity(new Intent(context, Preferences.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class ScanTask extends AsyncTask<Void, String[], Void> {

		private ExecutorService mExecutor;
		private DhcpInfo info;
		private int numberOfIps, pingTimeout, maxThreads;
		private long basicIp;

		private class CheckReachable implements Runnable {

			private String mTarget;

			public CheckReachable(String target) {
				this.mTarget = target;
			}

			public void run() {
				try {
					InetAddress ia = InetAddress.getByName(mTarget);
					if(ia.isReachable(pingTimeout*1000)) {
						String mac = GetIPInfo.getHardAddr(mTarget);
						publishProgress(new String[]{ "true", ia.getCanonicalHostName(), mac, GetIPInfo.getNicVendor(context, mac) });
					} else
						publishProgress(new String[]{ "false" });
				} catch (UnknownHostException e) {
				} catch (IOException e) {}
			}
		}

		@Override
		public void onPreExecute() {
			startStopButton.setText(R.string.main_stop_button);
			progressBar.setProgress(0);
			pingTimeout = Integer.parseInt(prefs.getString(getResources().getString(R.string.prefs_keys_ping_timeout), getResources().getString(R.string.prefs_defs_ping_timeout)));
			maxThreads = Integer.parseInt(prefs.getString(getResources().getString(R.string.prefs_keys_max_threads), getResources().getString(R.string.prefs_defs_max_threads)));
			info = wifi.getDhcpInfo();
			basicIp = littleToBigEndian((long)(info.ipAddress & info.netmask));
			numberOfIps = (int) (littleToBigEndian((long)((info.ipAddress & info.netmask) | ~info.netmask)) - littleToBigEndian((long) (info.ipAddress & info.netmask)));
			progressBar.setMax(numberOfIps);
			return;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			mExecutor = Executors.newFixedThreadPool(maxThreads);
			int position = 0;
			for(int x = 0; x <= numberOfIps; x++)
				if(!isCancelled()) {
					if(position >= maxThreads) {
						mExecutor.shutdown();
						try {
							mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
						} catch (InterruptedException e) {}
						mExecutor = Executors.newFixedThreadPool(maxThreads);
						position = 0;
					}
					mExecutor.execute(new CheckReachable(intToIp(basicIp+x)));
					position++;
				}
			mExecutor.shutdown();
			try {
				mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {}
			return null;
		}

		@Override
		protected void onProgressUpdate(String[]... params) {
			progressBar.incrementProgressBy(1);
			if(Boolean.parseBoolean(params[0][0])) {
				hostsList.add(new String[]{ params[0][1], params[0][2]});
				adapter.notifyDataSetChanged();
			}
			return;
		}

		@Override
		public void onPostExecute(Void voids) {
			startStopButton.setText(R.string.main_start_button);
			progressBar.setProgress(progressBar.getMax());
			scanTask = new ScanTask();
			return;
		}

		@Override
		public void onCancelled() {
			startStopButton.setText(R.string.main_start_button);
			progressBar.setProgress(progressBar.getMax());
			scanTask = new ScanTask();
			return;
		}
	}

	public long littleToBigEndian(long ip){
		return
			(long)(( ip     &0xFF)<<24) +
			(long)(((ip>> 8)&0xFF)<<16) +
			(long)(((ip>>16)&0xFF)<<8) +
			(long)(( ip>>24)&0xFF);
	}

	public String intToIp(long ip){
		return
			((ip>>24)&0xFF)+"."+
			((ip>>16)&0xFF)+"."+
			((ip>> 8)&0xFF)+"."+
			( ip     &0xFF);
	}
}
