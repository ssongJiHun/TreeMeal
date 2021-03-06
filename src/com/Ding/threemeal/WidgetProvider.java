package com.Ding.threemeal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.Ding.threemeal.Utils.DINGUtil;

public class WidgetProvider extends AppWidgetProvider {
	private Context mContext;
	private RemoteViews mRemoteViews;

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		// Default Recevier
		if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {

		} else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			updateAppWidget(context, manager, manager.getAppWidgetIds(new ComponentName(context, getClass())));
			
		} else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {

		} else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {

		}else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		//super.onUpdate(context, appWidgetManager, appWidgetIds);

		updateAppWidget(context, appWidgetManager, appWidgetIds);
	}

	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		mContext = context;
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		try {
			MyAsyncTask mMyAsyncTask = new MyAsyncTask();
			mMyAsyncTask.mRemoteViews = updateViews;
			mMyAsyncTask.mAppWidgetManager = appWidgetManager;
			mMyAsyncTask.mAppWidgetIds = appWidgetIds;
			mMyAsyncTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, String> {
		public RemoteViews mRemoteViews;
		public AppWidgetManager mAppWidgetManager;
		public int[] mAppWidgetIds;

		@Override
		protected String doInBackground(Void... params) {
			if (!DINGUtil.IsInternetConnection(mContext))
				return null;

			// ????????? ?????? ??????????????? ???????????????. , 1. ????????????, 2. ?????? ??????
			String[] schoolValues = getSchoolValues();

			// ??????????????? ????????? ?????????????????? ???????????????.
			for (int i = 0; i < schoolValues.length; i++) {
				if (schoolValues[i].equals(""))
					return null;
			}

			Calendar mCalendar = Calendar.getInstance();

			String url = "http://food.dingsoft.kr/server/carteServer.php?schoolLocation=" + schoolValues[0] + "&schoolCode=" + schoolValues[1]
					+ "&schulCrseScCode=" + schoolValues[2] + "&schulKndScCode=" + schoolValues[3] + "&year=" + mCalendar.get(Calendar.YEAR)
					+ "&month=" + ((mCalendar.get(Calendar.MONTH) + 1) < 10 ? "0" : "") + (mCalendar.get(Calendar.MONTH) + 1) + "&day="
					+ (mCalendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + mCalendar.get(Calendar.DAY_OF_MONTH);

			return getInternetData(url);
		}

		protected void onPostExecute(String result) {
			if (result == null || result.equals(""))
				return;

			Calendar mCalendar = Calendar.getInstance();
			String MenuValue = getMenu(mCalendar, result).replaceAll("(^\\<br />)|(\\<br />$)|(\\</td>)|(\\</div>)|([???-???])|([0-9])|\\=", "");
			MenuValue = MenuValue.replaceAll("(\\<br />)", ", ");

			mRemoteViews.setTextViewText(R.id.MealSlot, getSlot(mCalendar));
			mRemoteViews.setTextViewText(R.id.MealMenu, MenuValue);
			for (int i = 0; i < mAppWidgetIds.length; i++)
				mAppWidgetManager.updateAppWidget(mAppWidgetIds[i], mRemoteViews);
			
		}

		// ?????????????????? ?????? ????????? ?????? ?????????.
		String[] getSchoolValues() {
			String[] schoolValues = new String[4];
			try {
				SharedPreferences mSharedPreferences = mContext.getSharedPreferences("AppData", Context.MODE_PRIVATE);

				schoolValues[0] = mSharedPreferences.getString("schoolLocation", "");
				schoolValues[1] = mSharedPreferences.getString("schoolNumber", "");
				schoolValues[2] = mSharedPreferences.getString("schulCrseScCode", "");
				schoolValues[3] = mSharedPreferences.getString("schulKndScCode", "");

			} catch (Exception e) {
				e.printStackTrace();
			}
			return schoolValues;
		}

		// ????????? ?????? ???????????? ????????? ???????????????.
		String getInternetData(String url) {
			String result = "";
			try {
				// ?????? url ??????
				URL mUrl = new URL(url);
				// ????????? ?????? ??????
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				// ??????????????????.
				if (conn != null) {

					// ??????????????? ????????? ????????????.
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
						String line = null;

						while ((line = br.readLine()) != null)
							result += line;
						br.close();
					}
					conn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		String getSlot(Calendar calendar) {
			if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 1 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 10)
				return "??????";
			else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 11 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 15)
				return "??????";
			else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 16 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 24)
				return "??????";
			return "??????";
		}

		// ??????????????? ????????? ????????? ???????????????????????? ???????????????.
		String getMenu(Calendar calendar, String content) {
			try {
				// 01??? ~ 08??? ?????? , 09??? ~ 13??? ?????? ~ 14??? ~ 24??? ??????
				String[] wholeMenu = content.split("\\.");
				if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 1 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 10)
					return wholeMenu[1];
				else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 11 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 15)
					return wholeMenu[2];
				else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 16 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 24)
					return wholeMenu[3];
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "????????? ????????? ??? ????????????.";
		}
	}
}
