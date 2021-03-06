package com.Ding.threemeal.Fragment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;

import com.Ding.threemeal.R;
import com.Ding.threemeal.Utils.DINGUtil;
import com.Ding.threemeal.Utils.NumberPicker;
import com.Ding.threemeal.Utils.NumberPicker.OnNumberTouchListener;

public class LunchFragment extends Fragment implements OnTouchListener {
	private LinearLayout mDatePickerLayout;
	private Context mContext;
	private boolean mDateMarginVisible = false;
	private NumberPicker HourPicker;
	private NumberPicker MinutePicker;
	
	public LunchFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAttach(Activity activity) {
		mContext = activity.getApplicationContext();
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.alarm_lunch_layout, container, false);
		mView.setOnTouchListener(this);

		mDatePickerLayout = (LinearLayout) mView.findViewById(R.id.timeLayout);
		setMarginVisible(false);
		
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences("AppData", Context.MODE_PRIVATE);
		
		HourPicker = (NumberPicker) mView.findViewById(R.id.HourPicker);
		HourPicker.setMaxValue(24);
		HourPicker.setMinValue(0);
		HourPicker.setFocusable(true);
		HourPicker.setFocusableInTouchMode(true);
		HourPicker.setValue(mSharedPreferences.getInt("LUNCH_HOUR", 0));
		HourPicker.setOnNumberTouchListener(mOnNumberTouchListener);
		DINGUtil.setNumberPickerTextColor(HourPicker, Color.parseColor("#fdc98c"));

		MinutePicker = (NumberPicker) mView.findViewById(R.id.MinutePicker);
		MinutePicker.setMaxValue(59);
		MinutePicker.setMinValue(0);
		MinutePicker.setFocusable(true);
		MinutePicker.setFocusableInTouchMode(true);
		MinutePicker.setValue(mSharedPreferences.getInt("LUNCH_MINUTE", 0));
		MinutePicker.setOnNumberTouchListener(mOnNumberTouchListener);
		DINGUtil.setNumberPickerTextColor(MinutePicker, Color.parseColor("#fdc98c"));
		
		return mView;
	}
	
	public int[] getTimeValues() {
		int[] array = {HourPicker.getValue(), MinutePicker.getValue()};
		return array;
	}

	// ???????????? ????????????
	private void setMarginVisible(boolean visible) {
		ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mDatePickerLayout.getLayoutParams();
		// ??????
		if (!visible)
			params.setMargins(0, DINGUtil.dpToPx(mContext, -32), 0, DINGUtil.dpToPx(mContext, -32));
		else
			params.setMargins(0, 0, 0, 0);
		mDatePickerLayout.setLayoutParams(params);
	}

	private void startMarginAnimate() {
		final ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mDatePickerLayout.getLayoutParams();
		ValueAnimator mValueAnimator = null;

		// ??????
		if (!mDateMarginVisible)
			mValueAnimator = ValueAnimator.ofInt(0, DINGUtil.dpToPx(mContext, -32));
		else
			// ??????
			mValueAnimator = ValueAnimator.ofInt(params.bottomMargin, 0);

		mValueAnimator.setDuration(300);
		mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				params.setMargins(0, value, 0, value);
				mDatePickerLayout.setLayoutParams(params);
			}
		});
		mValueAnimator.start();
	}

	// ????????? ???????????? ???????????? ??????
	private OnNumberTouchListener mOnNumberTouchListener = new OnNumberTouchListener() {
		@Override
		public void onNumberTouch() {
			if (mDateMarginVisible)
				return;

			mDateMarginVisible = true;
			startMarginAnimate();
		}
	};

	// ?????? ??????????????? ?????? ????????? ??????
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (!mDateMarginVisible)
				return false;

			mDateMarginVisible = false;
			startMarginAnimate();
		}
		return false;
	}
}