package com.Ding.threemeal.Utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.Ding.threemeal.NotificationBroadcast;

public class DINGUtil {
	private static String IMTENT_ACTION = "com.Ding.threemeal.NotificationBroadcast";

	public static void setAlarm(Context mContext, boolean bVisibleMsg) {
		
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences("AppData", Context.MODE_PRIVATE);
		// 사용자가 알람을 취소했을때
		if(!mSharedPreferences.getBoolean("USE_ALARM", false))
			return;
		
		// 알람 설정
		AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

		// 캘린더 초기화
		Calendar mCalendarAlarm = Calendar.getInstance();

		// 저장된 정보
		
		int DINNER_HOUR = mSharedPreferences.getInt("DINNER_HOUR", 0);
		int DINNER_MINUTE = mSharedPreferences.getInt("DINNER_MINUTE", 0);

		int LUNCH_HOUR = mSharedPreferences.getInt("LUNCH_HOUR", 0);
		int LUNCH_MINUTE = mSharedPreferences.getInt("LUNCH_MINUTE", 0);

		int BREAKFAST_HOUR = mSharedPreferences.getInt("BREAKFAST_HOUR", 0);
		int BREAKFAST_MINUTE = mSharedPreferences.getInt("BREAKFAST_MINUTE", 0);

		// 아침 식사 메뉴 설정
		Intent breakfastIntent = new Intent(IMTENT_ACTION);
		breakfastIntent.putExtra("mealTime", 0);
		PendingIntent mPendingIntent0 = PendingIntent.getBroadcast(mContext, 0, breakfastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 아침 캘린더 설정
		// 날(일) 설정
		if (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) > BREAKFAST_HOUR
				|| (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) == BREAKFAST_HOUR && mCalendarAlarm.get(Calendar.HOUR_OF_DAY) >= BREAKFAST_MINUTE))
			mCalendarAlarm.add(Calendar.DAY_OF_MONTH, +1);

		// 시간과 분 설정
		mCalendarAlarm.set(Calendar.HOUR_OF_DAY, BREAKFAST_HOUR);
		mCalendarAlarm.set(Calendar.MINUTE, BREAKFAST_MINUTE);

		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendarAlarm.getTimeInMillis(), 24 * 60 * 60 * 1000, mPendingIntent0);

		// 캘린더 초기화
		mCalendarAlarm = Calendar.getInstance();

		// 점식 식사 메뉴 설정
		Intent lunchIntent = new Intent(IMTENT_ACTION);
		lunchIntent.putExtra("mealTime", 1);
		PendingIntent mPendingIntent1 = PendingIntent.getBroadcast(mContext, 1, lunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 점심 캘린더 설정
		// mCalendarAlarm.add(Calendar.DAY_OF_MONTH, (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) >= 11) ? +1 : +0);
		// 날(일) 설정
		if (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) > LUNCH_HOUR
				|| (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) == LUNCH_HOUR && mCalendarAlarm.get(Calendar.HOUR_OF_DAY) >= LUNCH_MINUTE))
			mCalendarAlarm.add(Calendar.DAY_OF_MONTH, +1);

		// 시간과 분 설정
		mCalendarAlarm.set(Calendar.HOUR_OF_DAY, LUNCH_HOUR);
		mCalendarAlarm.set(Calendar.MINUTE, LUNCH_MINUTE);

		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendarAlarm.getTimeInMillis(), 24 * 60 * 60 * 1000, mPendingIntent1);

		// 캘린더 초기화
		mCalendarAlarm = Calendar.getInstance();

		// 저녁 식사 메뉴 설정
		Intent dinnerIntent = new Intent(IMTENT_ACTION);
		dinnerIntent.putExtra("mealTime", 2);
		PendingIntent mPendingIntent2 = PendingIntent.getBroadcast(mContext, 2, dinnerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 저녁 캘린더 설정
		// 날(일) 설정
		if (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) > DINNER_HOUR
				|| (mCalendarAlarm.get(Calendar.HOUR_OF_DAY) == DINNER_HOUR && mCalendarAlarm.get(Calendar.HOUR_OF_DAY) >= DINNER_MINUTE))
			mCalendarAlarm.add(Calendar.DAY_OF_MONTH, +1);

		// 시간과 분 설정
		mCalendarAlarm.set(Calendar.HOUR_OF_DAY, DINNER_HOUR);
		mCalendarAlarm.set(Calendar.MINUTE, DINNER_MINUTE);

		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendarAlarm.getTimeInMillis(), 24 * 60 * 60 * 1000, mPendingIntent2);

		if (bVisibleMsg)
			Toast.makeText(
					mContext,
					"삼시 세끼 알람이 설정되었습니다. \n" + "아침 " + String.valueOf(BREAKFAST_HOUR) + ":"
							+ String.format(Locale.getDefault(), "%02d", BREAKFAST_MINUTE) + " 점심 " + String.valueOf(LUNCH_HOUR) + ":"
							+ String.format(Locale.getDefault(), "%02d", LUNCH_MINUTE) + " 저녁 " + String.valueOf(DINNER_HOUR) + ":"
							+ String.format(Locale.getDefault(), "%02d", DINNER_MINUTE) + "\n메뉴를 알려드립니다.", Toast.LENGTH_LONG).show();
	}

	public static boolean getRunAlarm(Context mContext) {
		// 아침 점심 저녁 실행중이아니면 마이너스
		for (int i = 0; i < 3; i++) {
			if (PendingIntent.getBroadcast(mContext, i, new Intent(IMTENT_ACTION), PendingIntent.FLAG_NO_CREATE) == null)
				return false;
		}
		return true;
	}

	public static void cancelAlarm(Context mContext) {
		AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

		for (int i = 0; i < 3; i++) {
			PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, i, new Intent(IMTENT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
			mAlarmManager.cancel(mPendingIntent);
		}
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int pxToDp(Context context, int px) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}

	public static boolean isLollipop() {
		return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	// 인터넷 접속을 확인합니다.
	public static boolean IsInternetConnection(Context context) {
		try {
			ConnectivityManager con_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (con_manager.getActiveNetworkInfo() != null && con_manager.getActiveNetworkInfo().isAvailable()
					&& con_manager.getActiveNetworkInfo().isConnected())
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// 학교 설정 확인
	public static boolean IsSchoolConnection(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE);

		if (!mSharedPreferences.getString("schoolName", "").equals(""))
			return true;

		return false;
	}


	public static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
		final int count = numberPicker.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = numberPicker.getChildAt(i);
			if (child instanceof EditText) {
				try {
					Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
					selectorWheelPaintField.setAccessible(true);
					((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
					((EditText) child).setTextColor(color);
					numberPicker.invalidate();
				} catch (NoSuchFieldException e) {
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				}
			}
		}
	}
}
