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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<String> {

	private ArrayList<String> mItems;
	private Context mContext;

	public ListViewAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
		super(context, textViewResourceId, items);
		mItems = items;
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null)
			v = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_item, null);
		String item = mItems.get(position);
		if (item != null) { //TODO IMAGES ON RESULTS!
			((TextView) v.findViewById(R.id.listview_item_ipaddr)).setText(item);
		}
		return v;
	}
}
