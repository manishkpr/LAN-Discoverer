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
