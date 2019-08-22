package com.sombersoft.slacklog;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceWithSummary extends ListPreference{

	public ListPreferenceWithSummary(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		super.setValue(value);
		setSummary(value);
	}

	@Override
	public void setSummary(CharSequence summary) {
		// TODO Auto-generated method stub
		super.setSummary(getEntry());
	}
}
