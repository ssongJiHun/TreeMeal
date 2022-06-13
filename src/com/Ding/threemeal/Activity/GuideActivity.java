package com.Ding.threemeal.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.Ding.threemeal.R;
import com.Ding.threemeal.Adapter.CirclePageIndicator;

public class GuideActivity extends Activity implements OnClickListener {
	private ViewPager mPager;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor("#1057c2"));
		}

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(2);
		mPager.setAdapter(new PagerAdapterClass(getApplicationContext()));

		// 잡렉 로딩
		mPager.setCurrentItem(2, true);
		mPager.setCurrentItem(0);

		// 뷰페이퍼 좌표
		CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setCurrentItem(mPager.getCurrentItem());

		// 버튼
		final Button mNextButton = (Button) findViewById(R.id.NextButton);
		mNextButton.setOnClickListener(this);
		final Button mSkipButton = (Button) findViewById(R.id.SkipButton);
		mSkipButton.setOnClickListener(this);
		final Button mStartButton = (Button) findViewById(R.id.StartButton);
		mStartButton.setOnClickListener(this);

		mPager.setPageTransformer(false, new ViewPager.PageTransformer() {
			@Override
			public void transformPage(View page, float position) {
				float normalizedposition = Math.abs(1 - Math.abs(position));
				page.setAlpha(normalizedposition);
				page.setScaleX(normalizedposition / 2 + 0.5f);
				page.setScaleY(normalizedposition / 2 + 0.5f);

				// 마지막 뷰에 진입
				if (position == -2.0f) {
					mNextButton.setVisibility(View.GONE);
					mSkipButton.setVisibility(View.GONE);
					mStartButton.setVisibility(View.VISIBLE);
				} else if (position == -1.0f || position == 0.0f) { // 마지막 뷰가 아닐경우
					if (mStartButton.getVisibility() == View.GONE)
						return;

					mNextButton.setVisibility(View.VISIBLE);
					mSkipButton.setVisibility(View.VISIBLE);
					mStartButton.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.NextButton:
			// 맨마지막이 아닐때만
			if (mPager.getCurrentItem() != 2)
				mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
			break;
		case R.id.SkipButton:
			mPager.setCurrentItem(2, true);
			break;
		case R.id.StartButton:
			SharedPreferences mSharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putBoolean("guideComplete", true);
			editor.commit();
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void finish() {
		setResult(Activity.RESULT_OK, getIntent());

		super.finish();
	}

	private class PagerAdapterClass extends PagerAdapter {

		private LayoutInflater mInflater;

		public PagerAdapterClass(Context context) {
			super();
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(View pager, int position) {
			View mView = null;
			if (position == 0)
				mView = mInflater.inflate(R.layout.viewpager_one, null);
			else if (position == 1)
				mView = mInflater.inflate(R.layout.viewpager_two, null);
			else if (position == 2)
				mView = mInflater.inflate(R.layout.viewpager_three, null);

			((TextView) mView.findViewById(R.id.HelloText)).setTypeface(Typeface.createFromAsset(getAssets(), "NotoSansKR-Medium.otf"));
			((TextView) mView.findViewById(R.id.ContentText)).setTypeface(Typeface.createFromAsset(getAssets(), "NotoSansKR-Light.otf"));

			((ViewPager) pager).addView(mView, 0);

			return mView;
		}

		@Override
		public void destroyItem(View pager, int position, Object view) {
			((ViewPager) pager).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View pager, Object obj) {
			return pager == obj;
		}
	}

	@Override
	public void onBackPressed() {

	}
}
