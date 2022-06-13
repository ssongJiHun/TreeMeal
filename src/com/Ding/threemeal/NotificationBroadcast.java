package com.Ding.threemeal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import com.Ding.threemeal.Activity.MainActivity;
import com.Ding.threemeal.Utils.DINGUtil;

public class NotificationBroadcast extends BroadcastReceiver {
	private Context mContext;
	private int nMealsTime; // 불러왔을때 시간(아침, 점심, 저녁)
	private String mSchoolName = null; // 학교 이름

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		nMealsTime = intent.getExtras().getInt("mealTime");

		try {
			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
				DINGUtil.setAlarm(context, false);
			else
				new MyAsyncTask().execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			if (!DINGUtil.IsInternetConnection(mContext) || !DINGUtil.IsSchoolConnection(mContext))
				return null;

			// 저장되 있는 학교정보를 불러옵니다. 학교이름, 0. 지역주소, 1. 학교 번호 2. schulCrseScCode 3.schulKndScCode
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

			// 띄어진 알림창을 클릭하면 가게될 액티비티
			PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class),
					PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationManager mNotification = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

			String MenuValue = getMenu(result).replaceAll("(^\\<br />)|(\\<br />$)|(\\</td>)|(\\</div>)|([①-⑬])|([0-9])|\\=", "");
			MenuValue = MenuValue.replaceAll("(\\<br />)", "\n");			
			
			String NotiContent = (MenuValue.equals("")) ? "메뉴를 불러올 수 없습니다." : MenuValue;
			Spanned mContentTitle = Html.fromHtml("<font color=" + ((DINGUtil.isLollipop()) ? "#00caff" : "#ffffff")
					+ "><b>전국 학교급식 \"삼시세끼\"</b></font>");
			Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);

			Notification builder = new NotificationCompat.Builder(mContext)

					.setContentTitle("전국 학교급식 \"삼시세끼\"")
					.setContentText("오늘의 급식 메뉴는?")
					.setStyle(
							new NotificationCompat.BigTextStyle().setBigContentTitle(mContentTitle).bigText(NotiContent)
									.setSummaryText(getTimeSlot() + " - " + mSchoolName)).setTicker("삼시세끼 알림").setAutoCancel(true)
					.setSmallIcon(R.drawable.ic_launcher).setLargeIcon(largeIcon).setDefaults(Notification.DEFAULT_VIBRATE)
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(mPendingIntent)
					.setPriority(Notification.PRIORITY_MAX).setAutoCancel(true).build();

			mNotification.notify(1, builder);
		}

		// 선택되어있는 학교 정보를 가져 옵니다.
		String[] getSchoolValues() {
			String[] schoolValues = new String[4];
			try {
				SharedPreferences mSharedPreferences = mContext.getSharedPreferences("AppData", Context.MODE_PRIVATE);

				mSchoolName = mSharedPreferences.getString("schoolName", "");
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

		// 아침 점심 저녁을 가져옵니다.
		String getTimeSlot() {
			if (nMealsTime == 0)
				return "아침 메뉴 ";
			else if (nMealsTime == 1)
				return "점심 메뉴";
			else if (nMealsTime == 2)
				return "저녁 메뉴";

			return "찾을수 없습니다.";
		}

		// 스트링으로 구성된 메뉴를 아침점심저녁으로 짤라줍니다.
		String getMenu(String content) {
			try {
				// 전체 메뉴에서 인뎃스 0 : 날짜, / 1: 아참, 2: 점심 3:// 저녁
				String[] wholeMenu = content.split("\\.");
				if (nMealsTime == 0)
					return wholeMenu[1];
				else if (nMealsTime == 1)
					return wholeMenu[2];
				else if (nMealsTime == 2)
					return wholeMenu[3];
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "메뉴를 불러올 수 없습니다.";
		}
	}
}