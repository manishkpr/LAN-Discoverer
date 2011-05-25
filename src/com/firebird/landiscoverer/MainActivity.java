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
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
	private ArrayList<String> hostsList;
	private ListViewAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Set view...
		setContentView(R.layout.main);

		//Define elements...
		context = this;
		scanTask = new ScanTask();
		wifi = (WifiManager)context.getSystemService(WIFI_SERVICE);
		startStopButton = (Button) findViewById(R.id.main_start_stop_button);
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
		hostsList = new ArrayList<String>();
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

	class ScanTask extends AsyncTask<Void, String[], Void> {

		private static final int maxThreads = 10;	//TODO PREFERENCES
		private ExecutorService mExecutor;
		private DhcpInfo info;
		private int numberOfIps;
		private long basicIp;

		private class CheckReachable implements Runnable {

			private String mTarget;

			public CheckReachable(String target) {
				this.mTarget = target;
			}

			public void run() {
				try {
					if(InetAddress.getByName(mTarget).isReachable(/* TODO Preferences.pingTimeout*1000 */2000))
						publishProgress(new String[]{ "true", mTarget });
					else
						publishProgress(new String[]{ "false" });
				} catch (UnknownHostException e) {
				} catch (IOException e) {}
			}
		}

		@Override
		public void onPreExecute() {
			startStopButton.setText(R.string.main_stop_button);
			progressBar.setProgress(0);
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
				hostsList.add(params[0][1]);
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
			(long)((( ip     &0xFF)&0xFF)<<24) +
			(long)((((ip>> 8)&0xFF)&0xFF)<<16) +
			(long)((((ip>>16)&0xFF)&0xFF)<<8) +
			(long)((( ip>>24)&0xFF)&0xFF);
	}

	public String intToIp(long ip){
		return
			((ip>>24)&0xFF)+"."+
			((ip>>16)&0xFF)+"."+
			((ip>> 8)&0xFF)+"."+
			( ip     &0xFF);
	}
}
