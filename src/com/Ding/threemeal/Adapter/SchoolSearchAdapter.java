package com.Ding.threemeal.Adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.Ding.threemeal.R;

public class SchoolSearchAdapter extends ArrayAdapter<SchoolSearchEntry> {
	private LayoutInflater mLayoutInflater;
	private ArrayList<SchoolSearchEntry> mSearchArray;
	private int mResource;

	public SchoolSearchAdapter(Context context, int resource, ArrayList<SchoolSearchEntry> arrays) {
		super(context, resource, arrays);

		mResource = resource;
		mSearchArray = arrays;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder mViewHolder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(mResource, parent, false);			
			
			mViewHolder = new ViewHolder();
			mViewHolder.schoolName = (TextView) convertView.findViewById(R.id.textSchoolName);
			mViewHolder.schoolAdress = (TextView) convertView.findViewById(R.id.textSchoolAddress);

			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		SchoolSearchEntry mEntry = mSearchArray.get(position);

		mViewHolder.schoolName.setText(mEntry.getOrgName());
		mViewHolder.schoolAdress.setText(mEntry.getZipAdres());

		return convertView;
	}

	public class ViewHolder {
		TextView schoolName;
		TextView schoolAdress;
		
	}
}
