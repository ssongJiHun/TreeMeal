package com.Ding.threemeal.Activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.Ding.threemeal.R;
import com.Ding.threemeal.FAB.FloatingActionButton;
import com.Ding.threemeal.FAB.FloatingActionsMenu;
import com.Ding.threemeal.FAB.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener;
import com.Ding.threemeal.Utils.DINGUtil;
import com.Ding.threemeal.Utils.GestureWebView;
import com.Ding.threemeal.Utils.SwitchButton;
import com.Ding.threemeal.Utils.GestureWebView.OnScrollChangedCallback;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {
	private FloatingActionsMenu mFloatingMenu; // 플로팅 메뉴
	private GestureWebView mWebView; // 웹뷰
	private View mBlurView; // 플로팅 버튼 누를시 띄어질 화면 블러처리
	private boolean bFABAnimate = false; // 플로팅 애니메이션 작동중
	private boolean bWebViewTouch = false; // 웨뷰 터치
	private Calendar mCalendar = Calendar.getInstance();
	private CookieManager mCookieManager = CookieManager.getInstance();

	private static final String mSiteAddress = "http://food.dingsoft.kr/";
	private static final int SWIPE_MIN_DISTANCE = 120; // 스와이프 최소 거리
	private static final int SWIPE_THRESHOLD_VELOCITY = 500; // 스와이프 속도
	private static final int SWIPE_MAX_OFF_PATH = 250; // 스와이프 대각선 최대 거리
	private long backKeyPressedTime = 0; // 뒤로가기 버튼 클릭
	private float fSwipeOffsetX = 0;
	private float fSwipeOffsetY = 0;
	private long fSwipeVelocityX = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Intent breakfastIntent = new Intent("com.Ding.threemeal.NotificationBroadcast");
		// breakfastIntent.putExtra("mealTime", 2);
		//
		// sendBroadcast(breakfastIntent);

		// startActivity(new Intent(MainActivity.this, AlarmSettingActivity.class));
		

		Intent widgetIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		sendBroadcast(widgetIntent);

		// 롤리팝 전용 스텟바
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor("#00b3ed"));
		}

		// 가이드 완료 확인
		SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);

		String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			if (DINGUtil.IsInternetConnection(getApplicationContext()))
				new MyAsyncTask().execute(versionName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 버전 업데이트되면 데이터 무조껀 초기화
		if (mSharedPreferences.getBoolean("VERSION_INIT_" + versionName, false)) {
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.clear();
			editor.putBoolean("VERSION_INIT_" + versionName, true);
			editor.commit();
		}

		// 다시 로딍
		mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);

		if (!mSharedPreferences.getBoolean("guideComplete", false)) {
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putInt("BREAKFAST_HOUR", 6);
			editor.putInt("BREAKFAST_MINUTE", 0);
			editor.putInt("LUNCH_HOUR", 11);
			editor.putInt("LUNCH_MINUTE", 0);
			editor.putInt("DINNER_HOUR", 17);
			editor.putInt("DINNER_MINUTE", 0);
			editor.putBoolean("USE_ALARM", true);
			editor.commit();

			startActivityForResult(new Intent(MainActivity.this, GuideActivity.class), 1);
		}

		// 플로팅버튼 누를시 뜨는 블러뷰
		mBlurView = (View) findViewById(R.id.blurView);

		// 플로팅버튼 메뉴
		mFloatingMenu = (FloatingActionsMenu) findViewById(R.id.floatingMenu);
		mFloatingMenu.setOnFloatingActionsMenuUpdateListener(onFloatingMenu);
		mFloatingMenu.setSoundEffectsEnabled(true);

		// 쿠키 설정 되있을때만 알람설정 모바일 앱 접속 쿠키
		mCookieManager.setCookie(mSiteAddress, "androidDevice=true");
		// 학교 설정이 되있는지 확인
		if (DINGUtil.IsSchoolConnection(MainActivity.this)) {
			// 오늘 날짜 쿠키 셋팅
			updateCookieDate();

			if (!DINGUtil.getRunAlarm(MainActivity.this))
				DINGUtil.setAlarm(MainActivity.this, true);

			// 학교설정이 안되있고, 가이드는 완료했을때
		} else if (mSharedPreferences.getBoolean("guideComplete", false)) {
			showSchoolSearchActivity();
		}

		// 웹브라우져 관련 설정
		mWebView = (GestureWebView) findViewById(R.id.webView1);
		mWebView.setBackgroundColor(Color.parseColor("#146df3"));
		mWebView.setOnTouchListener(onSwipeGesture);
		mWebView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});

		// 스크롤 작동시 플루팅버튼 애니메이션
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mWebView.setOnScrollChangedCallback(onWebViewScroll);

	//	mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);// 웹뷰가 html 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정되도록 한다.
		mWebView.getSettings().setUseWideViewPort(true); // 웹뷰 크기에 맞게 메ㅌ태그에 정의된 속석으로 로드

		if (checkInternetConnection())
			mWebView.loadUrl(mSiteAddress);
		else
			Toast.makeText(MainActivity.this, "인터넷 연결이 원활하지 않습니다.", Toast.LENGTH_LONG).show();

		// 웹뷰에서 링크클릭
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				CookieSyncManager.getInstance().sync();
			}
		});

		mWebView.setWebChromeClient(new WebChromeClient());

		// 플루팅버튼 설정
		FloatingActionButton Action_Search = (FloatingActionButton) findViewById(R.id.Action_school);
		Action_Search.setOnClickListener(this);
		Action_Search.setSoundEffectsEnabled(true);

		// 플로팅 버튼 새로고침
		FloatingActionButton Action_Refresh = (FloatingActionButton) findViewById(R.id.Action_refresh);
		Action_Refresh.setOnClickListener(this);
		Action_Refresh.setSoundEffectsEnabled(true);

		FloatingActionButton Action_Alarm = (FloatingActionButton) findViewById(R.id.Action_alarm);
		Action_Alarm.setOnClickListener(this);
		Action_Alarm.setSoundEffectsEnabled(true);

		// 플로팅버튼 초기화
		FloatingActionButton Action_Retore = (FloatingActionButton) findViewById(R.id.Action_restore);
		Action_Retore.setOnClickListener(this);
		Action_Retore.setSoundEffectsEnabled(true);

	}

	// 액티비티 전환시 받아오는 함수
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == Activity.RESULT_OK) {

			// requestCode == 1 가이드 액티비티에서 넘어올시
			if (requestCode == 1) {
				// 검색 액티비티로 이동
				showSchoolSearchActivity();

				// requestCode == 2 학교검색 액티비티
			} else if (requestCode == 2) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !mFloatingMenu.isExpanded())
					mFloatingMenu.animate().translationY(0).setDuration(100).setListener(null);

				// 학교를 변경햇을시
				if (intent.getBooleanExtra("bSchoolChange", false) == true) {
					// 위젯 업데이트 재설정
					Intent widgetIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					sendBroadcast(widgetIntent);
					// 알람 업데이트 재설정
					DINGUtil.setAlarm(MainActivity.this, true);
					mWebView.reload();
				}
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		CookieSyncManager.createInstance(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}

	@Override
	public void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	boolean checkInternetConnection() {

		ConnectivityManager con_manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (con_manager.getActiveNetworkInfo() != null && con_manager.getActiveNetworkInfo().isAvailable()
				&& con_manager.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	// 설정된 쿠키데이터를 mCalendar의 날짜로 업데이트 합니다.
	void updateCookieDate() {
		mCookieManager.setCookie(mSiteAddress, "year=" + mCalendar.get(Calendar.YEAR));
		mCookieManager
				.setCookie(mSiteAddress, "month=" + ((mCalendar.get(Calendar.MONTH) + 1) < 10 ? "0" : "") + (mCalendar.get(Calendar.MONTH) + 1));
		mCookieManager
				.setCookie(mSiteAddress, "day=" + (mCalendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + mCalendar.get(Calendar.DAY_OF_MONTH));
	}

	void showSchoolSearchActivity() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !mFloatingMenu.isExpanded()) {
			mFloatingMenu.animate().translationY(500).setDuration(100).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					startActivityForResult(new Intent(MainActivity.this, SchoolSearchActivity.class), 2);
					// 첫번째 매개변수는 생성할 액티비티에 적용 두번째는 현재 액티비티 적용
					overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
				}
			});
		} else {
			startActivityForResult(new Intent(MainActivity.this, SchoolSearchActivity.class), 2);
			overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
		}
	}

	private OnScrollChangedCallback onWebViewScroll = new OnScrollChangedCallback() {
		@Override
		public void onScroll(int CurrentY, int OffsetY) {
			// 터치중일때는 스크롤 호출안함
			if (!bWebViewTouch)
				return;

			// 아래로 스크롤
			if (OffsetY < CurrentY && !bFABAnimate) {
				mFloatingMenu.animate().translationY(500).setDuration(100).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						bFABAnimate = true;
					}
				});
			} else if (OffsetY > CurrentY && bFABAnimate) { // 위로 스크롤
				mFloatingMenu.animate().translationY(0).setDuration(100).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
						bFABAnimate = false;
					}
				});
			}
		}
	};

	private OnTouchListener onSwipeGesture = new OnTouchListener() {
		@SuppressLint("NewApi")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// 메뉴를 눌렀을때
			if (mFloatingMenu.isExpanded())
				return true;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				bWebViewTouch = true;
				fSwipeOffsetX = event.getX();
				fSwipeOffsetY = event.getY();
				fSwipeVelocityX = event.getEventTime();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				bWebViewTouch = false;
				// , 학교설정을 안했을때 세로 거리
				if (Math.abs(fSwipeOffsetY - event.getY()) > SWIPE_MAX_OFF_PATH)
					break;
				long currentTime = event.getEventTime();
				float difference = Math.abs(fSwipeOffsetX - event.getX());
				long lVelocity = currentTime - fSwipeVelocityX;

				// 왼쪽 스와이프
				if ((fSwipeOffsetX < event.getX()) && (lVelocity < SWIPE_THRESHOLD_VELOCITY) && (difference > SWIPE_MIN_DISTANCE)) {
					// 날짜 설정
					mCalendar.add(Calendar.DAY_OF_MONTH, -1);
					// 쿠키설정
					updateCookieDate();
					mWebView.reload();
				} else if ((fSwipeOffsetX > event.getX()) && (lVelocity < SWIPE_THRESHOLD_VELOCITY) && (difference > SWIPE_MIN_DISTANCE)) {
					// 날짜 설정
					mCalendar.add(Calendar.DAY_OF_MONTH, +1);
					// 쿠키설정
					updateCookieDate();
					mWebView.reload();
				}
				break;
			default:
				break;
			}
			return false;
		}
	};

	private OnFloatingActionsMenuUpdateListener onFloatingMenu = new OnFloatingActionsMenuUpdateListener() {
		@Override
		public void onMenuExpanded() {
			mBlurView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onMenuCollapsed() {
			mBlurView.setVisibility(View.GONE);
		}
	};

	@Override
	public void onClick(View v) {
		mFloatingMenu.collapse();
		switch (v.getId()) {
		// 학교 검색
		case R.id.Action_school:
			showSchoolSearchActivity();
			DINGUtil.cancelAlarm(MainActivity.this);
			break;
		// 새로고침
		case R.id.Action_refresh:
			mWebView.reload();
			break;
		case R.id.Action_alarm:
			startActivity(new Intent(MainActivity.this, AlarmSettingActivity.class));
			break;
		// 초기화
		case R.id.Action_restore:
			// 쿠키 삭제
			mCookieManager.removeAllCookie();
			DINGUtil.cancelAlarm(MainActivity.this);
			
			SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.clear();
			editor.commit();
			
			Toast.makeText(MainActivity.this, "초기화 되었습니다.", Toast.LENGTH_LONG).show();

			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mFloatingMenu.isExpanded())
			mFloatingMenu.collapse();
		else {
			if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
				backKeyPressedTime = System.currentTimeMillis();
				Toast.makeText(MainActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
				return;
			} else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
				finish();
			}
		}
	}

	public class MyAsyncTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			String marketVersion = getMarketVersionFast(MainActivity.this.getPackageName());
			if (marketVersion == null)
				return false;

			// 동일한 버전을때
			if (marketVersion.equals(params[0]))
				return true;

			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("새로운버전") // 제목 설정
						.setMessage("새로운 버전이 나왔습니다. 업그레이드를 하시겠습니까?") // 메세지 설정
						.setCancelable(false) // 뒤로 버튼 클릭시 취소 가능 설정
						.setPositiveButton("확인", new DialogInterface.OnClickListener() {
							// 확인 버튼 클릭시 설정
							public void onClick(DialogInterface dialog, int whichButton) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(Uri.parse("market://details?id=" + MainActivity.this.getPackageName()));
								startActivity(intent);
								finish();
							}
						}).setNegativeButton("취소", new DialogInterface.OnClickListener() {
							// 취소 버튼 클릭시 설정
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();
							}
						});

				AlertDialog dialog = builder.create(); // 알림창 객체 생성
				dialog.show(); // 알림창 띄우기
			}
		}

		public String getMarketVersionFast(String packageName) {
			String mData = "", mVer = null;

			try {
				URL mUrl = new URL("https://play.google.com/store/apps/details?id=" + packageName);
				HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();

				if (mConnection == null)
					return null;

				mConnection.setConnectTimeout(5000);
				mConnection.setUseCaches(false);
				mConnection.setDoOutput(true);

				if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader mReader = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));

					while (true) {
						String line = mReader.readLine();
						if (line == null)
							break;
						mData += line;
					}

					mReader.close();
				}

				mConnection.disconnect();

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

			String startToken = "softwareVersion\">";
			String endToken = "<";
			int index = mData.indexOf(startToken);

			if (index == -1) {
				mVer = null;

			} else {
				mVer = mData.substring(index + startToken.length(), index + startToken.length() + 100);
				mVer = mVer.substring(0, mVer.indexOf(endToken)).trim();
			}

			return mVer;
		}
	}
}
