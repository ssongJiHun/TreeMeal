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

			// 저장되 있는 학교정보를 불러옵니다. , 1. 지역주소, 2. 학교 번호
			String[] schoolValues = getSchoolValues();

			// 정상적으로 학교가 설정되있는지 확인합니다.
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
			String MenuValue = getMenu(mCalendar, result).replaceAll("(^\\<br />)|(\\<br />$)|(\\</td>)|(\\</div>)|([①-⑬])|([0-9])|\\=", "");
			MenuValue = MenuValue.replaceAll("(\\<br />)", ", ");

			mRemoteViews.setTextViewText(R.id.MealSlot, getSlot(mCalendar));
			mRemoteViews.setTextViewText(R.id.MealMenu, MenuValue);
			for (int i = 0; i < mAppWidgetIds.length; i++)
				mAppWidgetManager.updateAppWidget(mAppWidgetIds[i], mRemoteViews);
			
		}

		// 선택되어있는 학교 정보를 가져 옵니다.
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

		// 주소를 통해 스트링의 문자를 가져옵니다.
		String getInternetData(String url) {
			String result = "";
			try {
				// 연결 url 설정
				URL mUrl = new URL(url);
				// 커넥션 객체 생성
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				// 연결되었으면.
				if (conn != null) {

					// 연결되었음 코드가 리턴되면.
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
				return "아침";
			else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 11 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 15)
				return "점심";
			else if ((calendar.get(Calendar.HOUR_OF_DAY)) >= 16 && (calendar.get(Calendar.HOUR_OF_DAY)) <= 24)
				return "저녁";
			return "오류";
		}

		// 스트링으로 구성된 메뉴를 아침점심저녁으로 짤라줍니다.
		String getMenu(Calendar calendar, String content) {
			try {
				// 01시 ~ 08시 아침 , 09시 ~ 13시 점심 ~ 14시 ~ 24시 저녁
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
			return "메뉴를 불러올 수 없습니다.";
		}
	}
}
