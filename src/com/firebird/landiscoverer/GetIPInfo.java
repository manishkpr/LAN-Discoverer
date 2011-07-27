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

import android.content.Context;

public class GetIPInfo {

	static public String getHardAddr(String ip) {
		if(ip == null)
			return null;
		BufferedReader br = null;
		String ret = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if(splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:.."))
						ret = mac;
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

	static public String getNicVendor(Context ctx, String mac) {
		if(mac == null)
			return null;
		Pattern p = Pattern.compile(mac.toUpperCase().subSequence(0, 7)+"   (.*)");
		Scanner s = new Scanner(ctx.getResources().openRawResource(R.raw.nic_vendor_db), "UTF-8");
		String match = null;
		while (match != null) {
			match = s.findWithinHorizon(p, 0);
			if (match != null)
				match = s.match().group(1);
		}
		return match;
	}
}
