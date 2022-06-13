package com.Ding.threemeal.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class GestureWebView extends WebView{
	 private OnScrollChangedCallback mOnScrollChangedCallback;
	 
	public GestureWebView(Context context) {
		super(context);
	}
	public GestureWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public GestureWebView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(t, oldt);
	}
    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback)
    {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public static interface OnScrollChangedCallback
    {
        public void onScroll(int CurrentY, int OffsetY);
    }

}
