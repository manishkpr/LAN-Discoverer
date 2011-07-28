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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;

public class GetIPInfo {

	private Context mContext;
	private Resources res;
	private WifiManager wifi;
	private String mIpAddr, mMacAddr;

	public GetIPInfo(Context ctx) {
		mContext = ctx;
		res = mContext.getResources();
		wifi = (WifiManager)mContext.getSystemService(Activity.WIFI_SERVICE);
		mIpAddr = MainActivity.intToIp(MainActivity.littleToBigEndian(wifi.getDhcpInfo().ipAddress));
		mMacAddr = wifi.getConnectionInfo().getMacAddress();
	}

	public String getHardAddr(String ip) {
		if(ip == null)
			return null;
		if(ip.equals(mIpAddr))
			return mMacAddr;
		BufferedReader br = null;
		String ret = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if(splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:..")) {
						ret = mac;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public String getNicVendor(String mac) {
		if(mac == null)
			return null;
		String[] smac = mac.toUpperCase().split(":");
		Pattern p = Pattern.compile(smac[0]+"-"+smac[1]+"-"+smac[2]+" (.*)");
		Scanner scanner = new Scanner(res.openRawResource(R.raw.nic_vendor_db), "UTF-8");
		String match = "";
		while (match != null) {
			match = scanner.findWithinHorizon(p, 0);
			if (match != null) {
				match = scanner.match().group(1);
				break;
			}
		}
		return match;
	}
}