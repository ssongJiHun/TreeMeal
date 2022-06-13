package com.Ding.threemeal.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.Ding.threemeal.R;
import com.Ding.threemeal.Fragment.BreakfastFragment;
import com.Ding.threemeal.Fragment.DinnerFragment;
import com.Ding.threemeal.Fragment.LunchFragment;
import com.Ding.threemeal.Utils.DINGUtil;
import com.Ding.threemeal.Utils.SwitchButton;

public class AlarmSettingActivity extends FragmentActivity {
	private BreakfastFragment mBreakfastFragment;
	private LunchFragment mLunchFragment;
	private DinnerFragment mDinnerFragment;
	private SwitchButton mSwitchButton;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarmsetting);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor("#1a367f"));
		}

		getActionBar().setHomeAsUpIndicator(R.drawable.actionbar_home_up); // 뒤로가기 버튼 이미지 변경
		getActionBar().setTitle("삼시세끼");
		getActionBar().setDisplayShowHomeEnabled(false); // 아이콘 모양 안보이기
		getActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 보이기

		FragmentManager fragmentManager = getSupportFragmentManager();

		mBreakfastFragment = (BreakfastFragment) fragmentManager.findFragmentById(R.id.fragment_breakfast);
		mLunchFragment = (LunchFragment) fragmentManager.findFragmentById(R.id.fragment_lunch);
		mDinnerFragment = (DinnerFragment) fragmentManager.findFragmentById(R.id.fragment_dinner);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.alarm_actionbar, menu);
		SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);

		mSwitchButton = (SwitchButton) menu.findItem(R.id.switchActionBar).getActionView().findViewById(R.id.sb_md);
		mSwitchButton.setChecked(mSharedPreferences.getBoolean("USE_ALARM", false));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			dialogSaveData();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {
		dialogSaveData();
	}

	private void dialogSaveData() {
		SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
		final SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean("USE_ALARM", mSwitchButton.isChecked());
		editor.commit();

		AlertDialog.Builder builder = new AlertDialog.Builder(AlarmSettingActivity.this);
		builder.setTitle("현재 정보를 저장") // 제목 설정
				.setMessage("현재 정보를 저장하시겠습니까?") // 메세지 설정
				.setCancelable(false) // 뒤로 버튼 클릭시 취소 가능 설정
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					// 확인 버튼 클릭시 설정
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mSwitchButton.isChecked()) {
							int[] breakfastArray = mBreakfastFragment.getTimeValues();
							int[] luchArray = mLunchFragment.getTimeValues();
							int[] dinnerArray = mDinnerFragment.getTimeValues();

							editor.putInt("BREAKFAST_HOUR", breakfastArray[0]);
							editor.putInt("BREAKFAST_MINUTE", breakfastArray[1]);
							editor.putInt("LUNCH_HOUR", luchArray[0]);
							editor.putInt("LUNCH_MINUTE", luchArray[1]);
							editor.putInt("DINNER_HOUR", dinnerArray[0]);
							editor.putInt("DINNER_MINUTE", dinnerArray[1]);
							editor.commit();

							DINGUtil.setAlarm(AlarmSettingActivity.this, true);
						} else {
							DINGUtil.cancelAlarm(AlarmSettingActivity.this);
						}
						finish();
					}
				}).setNegativeButton("취소", new DialogInterface.OnClickListener() {
					// 취소 버튼 클릭시 설정
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				});

		AlertDialog dialog = builder.create(); // 알림창 객체 생성
		dialog.show(); // 알림창 띄우기
	}
}
