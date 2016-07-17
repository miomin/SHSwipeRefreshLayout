package com.scu.miomin.shswiperefresh.core;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scu.miomin.shswiperefresh.drawable.SHCircleProgressBar;
import com.scu.miomin.shswiperefresh.util.DipUtils;


/**
 * Created by 莫绪旻 on 16/6/24.
 */
public class SHGuidanceView extends LinearLayout {

    private static final int DEFAULT_CIRCLE_SIZE = 36;
    private SHCircleProgressBar circleProgressBar;
    private TextView tvLoad;

    public SHGuidanceView(Context context) {
        super(context);
        setupViews();
    }

    public SHGuidanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews();
    }

    public SHGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupViews();
    }

    private void setupViews() {
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER);

        circleProgressBar = new SHCircleProgressBar(getContext());
        LayoutParams lp = new LayoutParams((int) DipUtils.dipToPx(getContext(), DEFAULT_CIRCLE_SIZE),
                (int) DipUtils.dipToPx(getContext(), DEFAULT_CIRCLE_SIZE));
        lp.rightMargin = (int) DipUtils.dipToPx(getContext(), 10);
        addView(circleProgressBar, lp);
        tvLoad = new TextView(getContext());
        addView(tvLoad);
    }

    public void setGuidanceView(final View view) {
        if (view == null)
            return;
        removeAllViews();
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(view,lp);
    }

    public void setGuidanceView(@LayoutRes int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(layoutResID, null);
        if (view == null)
            return;
        removeAllViews();
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(view,lp);
    }

    public void setText(String loadtText) {
        if (tvLoad != null)
            tvLoad.setText(loadtText);
    }

    public void setTextColor(int color) {
        if (tvLoad != null)
            tvLoad.setTextColor(color);
    }

    public void setProgressBgColor(int color) {
        if (circleProgressBar != null)
            circleProgressBar.setBackgroundColor(color);
    }

    public void setProgressColor(int color) {
        if (circleProgressBar != null)
            circleProgressBar.setColorSchemeColors(color);
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (circleProgressBar != null)
            circleProgressBar.start();
    }

    /**
     * 设置动画起始位置
     */
    public void setStartEndTrim(float startAngle, float endAngle) {
        if (circleProgressBar != null)
            circleProgressBar.setStartEndTrim(startAngle, endAngle);
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (circleProgressBar != null)
            circleProgressBar.stop();
    }

    public void setProgressRotation(float rotation) {
        if (circleProgressBar != null)
            circleProgressBar.setProgressRotation(rotation);
    }
}
