package com.scu.miomin.shswiperefresh.core;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.scu.miomin.shswiperefresh.R;


/**
 * Created by miomin on 16/6/24.
 */
public class SHSwipeRefreshLayout extends FrameLayout implements NestedScrollingParent {

    private NestedScrollingParentHelper parentHelper;
    private SHSOnRefreshListener onRefreshListener;

    public static final int NOT_OVER_TRIGGER_POINT = 1;
    public static final int OVER_TRIGGER_POINT = 2;
    public static final int START = 3;

    /**
     * On refresh Callback, call on start refresh
     */
    public interface SHSOnRefreshListener {

        void onRefresh();

        void onLoading();

        void onRefreshPulStateChange(float percent, int state);

        void onLoadmorePullStateChange(float percent, int state);
    }

    static class WXRefreshAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private SHGuidanceView headerView = new SHGuidanceView(getContext());
    private SHGuidanceView footerView = new SHGuidanceView(getContext());
    private View mTargetView;

    private static final int GUIDANCE_VIEW_HEIGHT = 80;
    private static final int ACTION_PULL_REFRESH = 0;
    private static final int ACTION_LOADMORE = 1;

    // Enable PullRefresh and Loadmore
    private boolean mPullRefreshEnable = true;
    private boolean mPullLoadEnable = true;

    // Is Refreshing
    volatile private boolean mRefreshing = false;

    // RefreshView Height
    private float guidanceViewHeight = 0;

    // RefreshView Over Flow Height
    private float guidanceViewFlowHeight = 0;

    // Drag Action
    private int mCurrentAction = -1;
    private boolean isConfirm = false;

    // RefreshView Attrs
    private int mGuidanceViewBgColor;
    private int mGuidanceViewTextColor;
    private int mProgressBgColor;
    private int mProgressColor;
    private String mRefreshDefaulText = "";
    private String mLoadDefaulText = "";

    public SHSwipeRefreshLayout(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public SHSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public SHSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SHSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {

        if (getChildCount() > 1) {
            throw new RuntimeException("WXSwipeLayout should not have more than one child");
        }

        parentHelper = new NestedScrollingParentHelper(this);

        guidanceViewHeight = dipToPx(context, GUIDANCE_VIEW_HEIGHT);
        guidanceViewFlowHeight = guidanceViewHeight * (float)1.5;

        if (isInEditMode() && attrs == null) {
            return;
        }

        int resId;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SHSwipeRefreshLayout);
        Resources resources = context.getResources();

        //Indicator背景颜色
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_guidance_view_bg_color, -1);
        if (resId == -1) {
            mGuidanceViewBgColor = ta.getColor(R.styleable.SHSwipeRefreshLayout_guidance_view_bg_color,
                    Color.WHITE);
        } else {
            mGuidanceViewBgColor = resources.getColor(resId);
        }

        //加载文字颜色
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_guidance_text_color, -1);
        if (resId == -1) {
            mGuidanceViewTextColor = ta.getColor(R.styleable.SHSwipeRefreshLayout_guidance_text_color,
                    Color.BLACK);
        } else {
            mGuidanceViewTextColor = resources.getColor(resId);
        }

        //进度条背景颜色
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_progress_bg_color, -1);
        if (resId == -1) {
            mProgressBgColor = ta.getColor(R.styleable.SHSwipeRefreshLayout_progress_bg_color,
                    Color.WHITE);
        } else {
            mProgressBgColor = resources.getColor(resId);
        }

        //进度条颜色
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_progress_bar_color, -1);
        if (resId == -1) {
            mProgressColor = ta.getColor(R.styleable.SHSwipeRefreshLayout_progress_bar_color,
                    Color.RED);
        } else {
            mProgressColor = resources.getColor(resId);
        }

        //下拉刷新文字描述
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_refresh_text, -1);
        if (resId == -1) {
            mRefreshDefaulText = ta.getString(R.styleable.SHSwipeRefreshLayout_refresh_text);
        } else {
            mRefreshDefaulText = resources.getString(resId);
        }

        //上拉加载文字描述
        resId = ta.getResourceId(R.styleable.SHSwipeRefreshLayout_load_text, -1);
        if (resId == -1) {
            mLoadDefaulText = ta.getString(R.styleable.SHSwipeRefreshLayout_load_text);
        } else {
            mLoadDefaulText = resources.getString(resId);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTargetView = getChildAt(0);
        setGuidanceView();
    }

    /**
     * Init refresh view or loading view
     */
    private void setGuidanceView() {
        // SetUp HeaderView
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        headerView.setStartEndTrim(0, 0.75f);
        headerView.setText(mRefreshDefaulText);
        headerView.setTextColor(mGuidanceViewTextColor);
        headerView.setBackgroundColor(mGuidanceViewBgColor);
        headerView.setProgressBgColor(mProgressBgColor);
        headerView.setProgressColor(mProgressColor);
        addView(headerView, lp);

        // SetUp FooterView
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        lp.gravity = Gravity.BOTTOM;
        footerView.setStartEndTrim(0.5f, 1.25f);
        footerView.setText(mLoadDefaulText);
        footerView.setTextColor(mGuidanceViewTextColor);
        footerView.setBackgroundColor(mGuidanceViewBgColor);
        footerView.setProgressBgColor(mProgressBgColor);
        footerView.setProgressColor(mProgressColor);
        addView(footerView, lp);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ((!mPullRefreshEnable && !mPullLoadEnable)) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    /*********************************** NestedScrollParent *************************************/

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
    }

    /**
     * Callback on TouchEvent.ACTION_CANCLE or TouchEvent.ACTION_UP
     * handler : refresh or loading
     * @param child : child view of SwipeLayout,RecyclerView or Scroller
     */
    @Override
    public void onStopNestedScroll(View child) {
        parentHelper.onStopNestedScroll(child);
        handlerAction();
    }

    /**
     * With child view to processing move events
     * @param target the child view
     * @param dx move x
     * @param dy move y
     * @param consumed parent consumed move distance
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        if ((!mPullRefreshEnable && !mPullLoadEnable)) {
            return;
        }

        // Prevent Layout shake
        if (Math.abs(dy) > 200) {
            return;
        }

        if (!isConfirm) {
            if (dy < 0 && !canChildScrollUp()) {
                mCurrentAction = ACTION_PULL_REFRESH;
                isConfirm = true;
            } else if (dy > 0 && !canChildScrollDown()) {
                mCurrentAction = ACTION_LOADMORE;
                isConfirm = true;
            }
        }

        if (moveGuidanceView(-dy)) {
            consumed[1] += dy;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public int getNestedScrollAxes() {
        return parentHelper.getNestedScrollAxes();
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    /**
     * Adjust the refresh or loading view according to the size of the gesture
     *
     * @param distanceY move distance of Y
     */
    private boolean moveGuidanceView(float distanceY) {

        if (mRefreshing) {
            return false;
        }

        if (!canChildScrollUp() && mPullRefreshEnable && mCurrentAction == ACTION_PULL_REFRESH) {
            // Pull Refresh
            LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
            lp.height += distanceY;
            if (lp.height < 0) {
                lp.height = 0;
            }
            if (lp.height > guidanceViewFlowHeight) {
                lp.height = (int) guidanceViewFlowHeight;
            }

            if (onRefreshListener != null) {
                if (lp.height >= guidanceViewHeight) {
                    onRefreshListener.onRefreshPulStateChange(lp.height / guidanceViewHeight, OVER_TRIGGER_POINT);
                } else {
                    onRefreshListener.onRefreshPulStateChange(lp.height / guidanceViewHeight, NOT_OVER_TRIGGER_POINT);
                }
            }

            if (lp.height == 0) {
                isConfirm = false;
                mCurrentAction = -1;
            }
            headerView.setLayoutParams(lp);
            headerView.setProgressRotation(lp.height / guidanceViewFlowHeight);
            moveTargetView(lp.height);
            return true;
        } else if (!canChildScrollDown() && mPullLoadEnable && mCurrentAction == ACTION_LOADMORE) {
            // Load more
            LayoutParams lp = (LayoutParams) footerView.getLayoutParams();
            lp.height -= distanceY;
            if (lp.height < 0) {
                lp.height = 0;
            }
            if (lp.height > guidanceViewFlowHeight) {
                lp.height = (int) guidanceViewFlowHeight;
            }

            if (onRefreshListener != null) {
                if (lp.height >= guidanceViewHeight) {
                    onRefreshListener.onLoadmorePullStateChange(lp.height / guidanceViewHeight, OVER_TRIGGER_POINT);
                } else {
                    onRefreshListener.onLoadmorePullStateChange(lp.height / guidanceViewHeight, NOT_OVER_TRIGGER_POINT);
                }
            }

            if (lp.height == 0) {
                isConfirm = false;
                mCurrentAction = -1;
            }
            footerView.setLayoutParams(lp);
            footerView.setProgressRotation(lp.height / guidanceViewFlowHeight);
            moveTargetView(-lp.height);
            return true;
        }
        return false;
    }

    /**
     * Adjust contentView(Scroller or List) at refresh or loading time
     * @param h Height of refresh view or loading view
     */
    private void moveTargetView(float h) {
        mTargetView.setTranslationY(h);
    }

    /**
     * Decide on the action refresh or loadmore
     */
    private void handlerAction() {

        if (isRefreshing()) {
            return;
        }
        isConfirm = false;

        LayoutParams lp;
        if (mPullRefreshEnable && mCurrentAction == ACTION_PULL_REFRESH) {
            lp = (LayoutParams) headerView.getLayoutParams();
            if (lp.height >= guidanceViewHeight) {
                startRefresh(lp.height);
                if (onRefreshListener != null)
                    onRefreshListener.onRefreshPulStateChange(1,START);
            } else if (lp.height > 0) {
                resetHeaderView(lp.height);
            } else {
                resetRefreshState();
            }
        }

        if (mPullLoadEnable && mCurrentAction == ACTION_LOADMORE) {
            lp = (LayoutParams) footerView.getLayoutParams();
            if (lp.height >= guidanceViewHeight) {
                startLoadmore(lp.height);
                if (onRefreshListener != null)
                    onRefreshListener.onLoadmorePullStateChange(1,START);
            } else if (lp.height > 0) {
                resetFootView(lp.height);
            } else {
                resetLoadmoreState();
            }
        }
    }

    /**
     * Start Refresh
     * @param headerViewHeight
     */
    private void startRefresh(int headerViewHeight) {
        mRefreshing = true;
        ValueAnimator animator = ValueAnimator.ofFloat(headerViewHeight, guidanceViewHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
                lp.height = (int) ((Float) animation.getAnimatedValue()).floatValue();
                headerView.setLayoutParams(lp);
                moveTargetView(lp.height);
            }
        });
        animator.addListener(new WXRefreshAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                headerView.startAnimation();
                //TODO updateLoadText
                if (onRefreshListener != null) {
                    onRefreshListener.onRefresh();
                }
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    /**
     * Reset refresh state
     * @param headerViewHeight
     */
    private void resetHeaderView(int headerViewHeight) {
        headerView.stopAnimation();
        headerView.setStartEndTrim(0, 0.75f);
        ValueAnimator animator = ValueAnimator.ofFloat(headerViewHeight, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
                lp.height = (int) ((Float) animation.getAnimatedValue()).floatValue();
                headerView.setLayoutParams(lp);
                moveTargetView(lp.height);
            }
        });
        animator.addListener(new WXRefreshAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetRefreshState();

            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void resetRefreshState() {
        mRefreshing = false;
        isConfirm = false;
        mCurrentAction = -1;
        //TODO updateLoadText
    }

    /**
     * Start loadmore
     * @param headerViewHeight
     */
    private void startLoadmore(int headerViewHeight) {
        mRefreshing = true;
        ValueAnimator animator = ValueAnimator.ofFloat(headerViewHeight, guidanceViewHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams lp = (LayoutParams) footerView.getLayoutParams();
                lp.height = (int) ((Float) animation.getAnimatedValue()).floatValue();
                footerView.setLayoutParams(lp);
                moveTargetView(-lp.height);
            }
        });
        animator.addListener(new WXRefreshAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                footerView.startAnimation();
                //TODO updateLoadText
                if (onRefreshListener != null) {
                    onRefreshListener.onLoading();
                }
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    /**
     * Reset loadmore state
     * @param headerViewHeight
     */
    private void resetFootView(int headerViewHeight) {
        footerView.stopAnimation();
        footerView.setStartEndTrim(0.5f, 1.25f);
        ValueAnimator animator = ValueAnimator.ofFloat(headerViewHeight, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams lp = (LayoutParams) footerView.getLayoutParams();
                lp.height = (int) ((Float) animation.getAnimatedValue()).floatValue();
                footerView.setLayoutParams(lp);
                moveTargetView(-lp.height);
            }
        });
        animator.addListener(new WXRefreshAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetLoadmoreState();

            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void resetLoadmoreState() {
        mRefreshing = false;
        isConfirm = false;
        mCurrentAction = -1;
        //TODO updateLoadText
    }

    /**
     * Whether child view can scroll up
     * @return
     */
    public boolean canChildScrollUp() {
        if (mTargetView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTargetView, -1) || mTargetView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, -1);
        }
    }

    /**
     * Whether child view can scroll down
     * @return
     */
    public boolean canChildScrollDown() {
        if (mTargetView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1)
                            .getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1
                            && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(mTargetView, 1) || mTargetView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, 1);
        }
    }

    public float dipToPx(Context context, float value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

    public void setOnRefreshListener(SHSOnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    /**
     * Callback on refresh finish
     */
    public void finishRefresh() {
        if (mCurrentAction == ACTION_PULL_REFRESH) {
            resetHeaderView(headerView == null ? 0 : headerView.getMeasuredHeight());
        }
    }

    /**
     * Callback on loadmore finish
     */
    public void finishLoadmore() {
        if (mCurrentAction == ACTION_LOADMORE) {
            resetFootView(footerView == null ? 0 : footerView.getMeasuredHeight());
        }
    }

    public boolean isLoadmoreEnable() {
        return mPullLoadEnable;
    }

    public void setLoadmoreEnable(boolean mPullLoadEnable) {
        this.mPullLoadEnable = mPullLoadEnable;
    }

    public boolean isRefreshEnable() {
        return mPullRefreshEnable;
    }

    public void setRefreshEnable(boolean mPullRefreshEnable) {
        this.mPullRefreshEnable = mPullRefreshEnable;
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setHeaderView(@LayoutRes int layoutResID) {
        headerView.setGuidanceView(layoutResID);
    }

    public void setHeaderView(View view) {
        headerView.setGuidanceView(view);
    }

    public void setFooterView(@LayoutRes int layoutResID) {
        footerView.setGuidanceView(layoutResID);
    }

    public void setFooterView(View view) {
        footerView.setGuidanceView(view);
    }

    public void setRefreshViewText(String text) {
        headerView.setText(text);
    }

    public void setLoaderViewText(String text) {
        footerView.setText(text);
    }
}
