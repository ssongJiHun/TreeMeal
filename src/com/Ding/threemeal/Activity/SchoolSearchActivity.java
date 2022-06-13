package com.Ding.threemeal.Activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Ding.threemeal.R;
import com.Ding.threemeal.Adapter.SchoolSearchAdapter;
import com.Ding.threemeal.Adapter.SchoolSearchEntry;
import com.Ding.threemeal.Utils.DINGUtil;

public class SchoolSearchActivity extends Activity {

	// ListView 뷰 데이터 관련
	private ArrayList<SchoolSearchEntry> mSearchArray = new ArrayList<SchoolSearchEntry>(); // 학교 리스트 배열
	// private ArrayList<SchoolSearchEntry> mCopyArray = new ArrayList<SchoolSearchEntry>(); // 8개 이상 배열
	private SchoolSearchAdapter mSchoolSearchAdapter; // 학교 검색 어탭터

	private LinearLayout mSchoolLayout; // 학교 상태 전체 레이아웃
	private ImageView mSchoolIcon; // 학교 아이콘
	private TextView mSchoolTitle; // 학교 아이콘밑에있는 제목
	private TextView mSchoolContent; // 학교찾으라는 설정
	// private ArrayList[] mRestultArray = new ArrayList[17]; // 병령 검색하기 때문에 순서정렬

	// 에이터 박스
	private EditText mEditText;
	// private String mEditContent;
	private ImageButton deleteButton;
	private boolean bDeleteAnumate = false;

	// ListView 프로그래스바 로딩 창
	private LinearLayout mProgressLayout; // listview footer 에 속해있는 프로그래스바
	private LinearLayout mTextLayout; // listview fppter 에 속해있는 데이터 출처 그룹

	// 검색 핸들러 700 딜레이 관련
	private Runnable mDelayRunnable; // 검색어 딜레이
	private Handler mDelayHandler = new Handler(); // 검색 Runnable 작동시켜주는 핸들러
	private boolean bDelayRunnable = false; // 검색 기능 사용중인지 체크
	private static final String mSiteAddress = "http://food.dingsoft.kr/";

	// 파씽 쓰레드 관련
	private int searchCount = 0; // 몇지역의 교육청을 검색했나?
	private boolean bSearchThread = false; // 쓰레드 작동중
	// private boolean[] mThreadFinish = new boolean[17]; // 로딩순서검사 위해서
	private String[] districtString = { "sen.go.kr", "pen.go.kr", "dge.go.kr", "ice.go.kr", "gen.go.kr", "dje.go.kr", "use.go.kr", "sje.go.kr",
			"goe.go.kr", "kwe.go.kr", "cbe.go.kr", "cne.go.kr", "jbe.go.kr", "jne.go.kr", "gbe.kr", "gne.go.kr", "jje.go.kr" };

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor("#1057c2"));
		}

		// 리스트 관련 총 설정
		ListView mSearchListView = (ListView) findViewById(R.id.listView1);

		// 리스너 어탭터 설정과 스크롤 리스너
		mSchoolSearchAdapter = new SchoolSearchAdapter(SchoolSearchActivity.this, R.layout.row_search, mSearchArray);
		mSearchListView.setAdapter(mSchoolSearchAdapter);
		mSearchListView.setOnItemClickListener(onListItemListener);

		// 에디터 박스 관련 총 설정
		mEditText = (EditText) findViewById(R.id.editText1);
		mEditText.addTextChangedListener(onTextWatcher);
		mEditText.setSoundEffectsEnabled(true);

		// ListView 프로그래스바 로딩 창
		mProgressLayout = (LinearLayout) findViewById(R.id.progressLayout);
		mTextLayout = (LinearLayout) findViewById(R.id.textLayout);

		// 검색 초기화
		deleteButton = (ImageButton) findViewById(R.id.imageButton2);
		deleteButton.setOnClickListener(onDeleteButtonListener);
		deleteButton.setSoundEffectsEnabled(true);

		// 학교 검색 상태
		mSchoolLayout = (LinearLayout) findViewById(R.id.schoolLayout);
		mSchoolIcon = (ImageView) findViewById(R.id.schoolIcon);
		mSchoolTitle = (TextView) findViewById(R.id.schoolTitle);
		mSchoolContent = (TextView) findViewById(R.id.schoolContent);

		// 검색 딜레이 핸들러 설정
		mDelayRunnable = new Runnable() {
			@Override
			public void run() {
				// 검색 딜레이 취소
				bDelayRunnable = false;
				setSearchSchool();
			}
		};
	}

	@Override
	public void finish() {
		setResult(Activity.RESULT_OK, getIntent());
		super.finish();
		overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
	}

	// state true . 학교를 검색해주세요, false 학교를 찾을수 없습니다.
	void setSearchState(boolean visible, boolean state) {
		if (!visible) {
			mSchoolLayout.setVisibility(View.GONE);
			return;
		}

		if (state) {
			mSchoolIcon.setImageResource(R.drawable.school);
			mSchoolTitle.setText("학교를 검색해주세요.");
			mSchoolContent.setText("어떤 학교의 식단표를 알고 싶으신가요?");
		} else {
			mSchoolIcon.setImageResource(R.drawable.school__);
			mSchoolTitle.setText("학교를 찾을 수 없습니다..");
			mSchoolContent.setText("연결된 네트워크의 문제 또는 일시적인 현상일 수 있습니다.");
		}
		mSchoolLayout.setVisibility(View.VISIBLE);
	}

	// 학교 검색을 시작한다. 한번에 17개의 구역의 학교를 불러옵니다.
	@SuppressLint("NewApi")
	void setSearchSchool() {
		String mEditContent = mEditText.getText().toString().replaceAll("\\s", "");
		// 이미 쓰레드가 돌아가고 있을 경우 초기화
		if (bSearchThread) {
			mSearchArray.clear();
			// mCopyArray.clear();
			mSchoolSearchAdapter.notifyDataSetChanged();
		}

		// // 초기화
		// for(int i = 0; i < 17; i++){
		// if(mRestultArray[i] != null){
		// mRestultArray[i].clear();
		// mRestultArray[i] = null;
		// }
		// }

		bSearchThread = true;

		searchCount = 0;
		if (mEditContent.equals("학교")) {
			setProgressVisible(1);
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			for (int i = 0; i < 17; i++)
				new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mEditContent, String.valueOf(i));
		} else {
			for (int i = 0; i < 17; i++)
				new MyAsyncTask().execute(mEditContent, String.valueOf(i));
		}

	}

	// 0. 프로그래스바만 보임, 1. 텍스트만 보임, 3. 둘다 안보임
	void setProgressVisible(int nState) {
		if (nState == 0) {
			mProgressLayout.setVisibility(View.VISIBLE);
			mTextLayout.setVisibility(View.GONE);
		} else if (nState == 1) {
			mProgressLayout.setVisibility(View.GONE);
			mTextLayout.setVisibility(View.VISIBLE);
		} else if (nState == 2) {
			mProgressLayout.setVisibility(View.GONE);
			mTextLayout.setVisibility(View.GONE);
		}
	}

	// 리스트박스 아이템 클릭
	private OnItemClickListener onListItemListener = new OnItemClickListener() {
		@Override
		// year년도 month월 day일
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Calendar mCalendar = Calendar.getInstance();
			CookieManager cookieManager = CookieManager.getInstance();
			// 캐쉬에 추가
			cookieManager.setCookie(mSiteAddress, "schoolLocationValue=" + mSearchArray.get(position).getDistrictAdres()); // jbc.co.kr 예를들어
			cookieManager.setCookie(mSiteAddress, "schoolNumber=" + mSearchArray.get(position).getOrgCode());
			cookieManager.setCookie(mSiteAddress, "schulCrseScCode=" + mSearchArray.get(position).getCrseScCode());
			cookieManager.setCookie(mSiteAddress, "schulKndScCode=" + mSearchArray.get(position).getKndScCode());
			cookieManager.setCookie(mSiteAddress, "schoolName=" + mSearchArray.get(position).getOrgName());
			cookieManager.setCookie(mSiteAddress, "year=" + mCalendar.get(Calendar.YEAR));
			cookieManager.setCookie(mSiteAddress, "month=" + ((mCalendar.get(Calendar.MONTH) + 1) < 10 ? "0" : "")
					+ (mCalendar.get(Calendar.MONTH) + 1));
			cookieManager.setCookie(mSiteAddress,
					"day=" + (mCalendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + mCalendar.get(Calendar.DAY_OF_MONTH));
			cookieManager.setCookie(mSiteAddress, "androidDevice=true");

			// 데이테어 추가
			SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString("schoolLocation", mSearchArray.get(position).getDistrictAdres());
			editor.putString("schoolNumber", mSearchArray.get(position).getOrgCode());
			editor.putString("schoolName", mSearchArray.get(position).getOrgName());
			editor.putString("schulKndScCode", mSearchArray.get(position).getKndScCode());
			editor.putString("schulCrseScCode", mSearchArray.get(position).getCrseScCode());
			editor.commit();

			getIntent().putExtra("bSchoolChange", true);
			finish();
		}
	};

	// 에디터 박스 변경 리스너
	private TextWatcher onTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// 검색 0.7초안에 키보드를 기면 검색 안함
			if (bDelayRunnable)
				mDelayHandler.removeCallbacks(mDelayRunnable);

			// 검색창에 먼가를 치고있을때
			if (!s.toString().equals("")) {
				bDelayRunnable = true;
				mDelayHandler.postDelayed(mDelayRunnable, 1000);

				setSearchState(false, false);

				if (mProgressLayout.getVisibility() == View.GONE) // 검색 했을시 프로그래스바가 떠야됨
					setProgressVisible(0);
			} else {// 검색창 초기화 했을때
				bDelayRunnable = false;
				setProgressVisible(2);
				setSearchState(true, true);
			}

			// 글자누를때마다 초기화 연속적 렉 방지 또는 첫글자 삭제
			if (mSearchArray.size() != 0) {
				mSearchArray.clear();
				// mCopyArray.clear();
				mSchoolSearchAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	// 에디터 박스 초기화 버튼
	private OnClickListener onDeleteButtonListener = new OnClickListener() {
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !bDeleteAnumate) {
				bDeleteAnumate = true;
				deleteButton.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						bDeleteAnumate = false;
					}
				});
			}
			mEditText.setText("");
		}
	};

	@Override
	public void onBackPressed() {
		if (DINGUtil.IsSchoolConnection(SchoolSearchActivity.this))
			finish();
		else
			Toast.makeText(SchoolSearchActivity.this, "학교를 설정해주세요", Toast.LENGTH_LONG).show();
	}

	// 비동기 Json 파싱 설정 베열중 0 = 검색이름, 1 = 인덱스
	public class MyAsyncTask extends AsyncTask<String, Void, ArrayList<SchoolSearchEntry>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ArrayList<SchoolSearchEntry> doInBackground(String... params) {
			int searchIndex = Integer.valueOf(params[1]);

			// 검색창의 검색어를 변경하면 검색중지
			if (!params[0].equals(mEditText.getText().toString().replaceAll("\\s", "")))
				cancel(true);

			try {
				String url = "http://hes." + districtString[searchIndex] + "/spr_ccm_cm01_100.do?kraOrgNm=" + URLEncoder.encode(params[0], "UTF-8");
				return getJsonArray(searchIndex, getJsonFromWeb(url));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(ArrayList<SchoolSearchEntry> result) {
			// 검색수 증가 단순히 몇개 로딩했는지
			searchCount++;
			int nVisibleSize = 5; // 보여지는 갯수

			// 배열을 받아서 추가해준다
			for (int j = 0; j < result.size(); j++) {
				if (mSearchArray.size() < nVisibleSize)
					mSearchArray.add((SchoolSearchEntry) result.get(j));
				else
					break;
				// else
				// mCopyArray.add((SchoolSearchEntry) mRestultArray[result].get(j));
			}
			mSchoolSearchAdapter.notifyDataSetChanged();
			
			// 전체적으로 17개의 학교를 다 불러왔을때
			if (searchCount == 17) {
				bSearchThread = false;
				setProgressVisible(1);

				if (mSearchArray.size() == 0) {
					setSearchState(true, false);
				}
			}
		}

		ArrayList<SchoolSearchEntry> getJsonArray(int searchIndex, String searchContent) {
			ArrayList<SchoolSearchEntry> array = new ArrayList<SchoolSearchEntry>();
			try {
				JSONObject json = new JSONObject(searchContent);
				JSONObject resultSVO = json.getJSONObject("resultSVO");
				JSONArray orgDVOList = resultSVO.getJSONArray("orgDVOList");

				for (int i = 0; i < orgDVOList.length(); i++) {
					JSONObject obj = orgDVOList.getJSONObject(i);
					array.add(new SchoolSearchEntry(districtString[searchIndex], obj.getString("kraOrgNm"),
							obj.getString("zipAdres").equals(" ") ? "주소를 찾을수 없습니다." : obj.getString("zipAdres"), obj.getString("orgCode"), obj
									.getString("schulKndScCode"), obj.getString("schulCrseScCode")));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return array;
		}

		String getJsonFromWeb(String sUrl) {
			String result = "";
			try {
				// 연결 url 설정
				URL url = new URL(sUrl);
				// 커넥션 객체 생성
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
	}
}
