package com.fashare.nestedscrollwebview;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.webkit.WebView;

/**
 * Created by apple on 2017/12/11.
 */

public class NestedScrollWebview extends WebView implements NestedScrollingChild {
    private static final String TAG = "NestedScrollWebview";
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    public NestedScrollWebview(Context context) {
        this(context, null);
    }

    public NestedScrollWebview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    private int mTouchSlop;

    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;

    boolean mIsBeingDragged;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        initVelocityTrackerIfNotExists();

        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = ev.getActionMasked();

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        vtev.offsetLocation(0, mNestedYOffset);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
//                if ((mIsBeingDragged = !mScroller.isFinished())) {
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                }
//
//                /*
//                 * If being flinged and user touches, stop the fling. isFinished
//                 * will be false if being flinged.
//                 */
//                if (!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                }

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;

            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int deltaY = mLastMotionY - y;
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    vtev.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];

                    final int oldY = getScrollY();
//                    final int range = getScrollRange();
                    final int overscrollMode = getOverScrollMode();
//                    boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS
//                            || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                    // Calling overScrollByCompat will call onOverScrolled, which
                    // calls onScrollChanged if applicable.
//                    if (overScrollByCompat(0, deltaY, 0, getScrollY(), 0, range, 0,
//                            0, true) && !hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)) {
                        // Break our velocity if we hit a scroll barrier.
//                        mVelocityTracker.clear();
//                    }

                    final int scrolledDeltaY = getScrollY() - oldY;
                    final int unconsumedY = deltaY - scrolledDeltaY;
                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                    }
//                    else if (canOverscroll) {
//                        ensureGlows();
//                        final int pulledToY = oldY + deltaY;
//                        if (pulledToY < 0) {
//                            EdgeEffectCompat.onPull(mEdgeGlowTop, (float) deltaY / getHeight(),
//                                    ev.getX(activePointerIndex) / getWidth());
//                            if (!mEdgeGlowBottom.isFinished()) {
//                                mEdgeGlowBottom.onRelease();
//                            }
//                        } else if (pulledToY > range) {
//                            EdgeEffectCompat.onPull(mEdgeGlowBottom, (float) deltaY / getHeight(),
//                                    1.f - ev.getX(activePointerIndex)
//                                            / getWidth());
//                            if (!mEdgeGlowTop.isFinished()) {
//                                mEdgeGlowTop.onRelease();
//                            }
//                        }
//                        if (mEdgeGlowTop != null
//                                && (!mEdgeGlowTop.isFinished() || !mEdgeGlowBottom.isFinished())) {
//                            ViewCompat.postInvalidateOnAnimation(this);
//                        }
//                    }
                }
                break;
            case MotionEvent.ACTION_UP:
//                final VelocityTracker velocityTracker = mVelocityTracker;
//                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
//                int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
//                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
//                    flingWithNestedDispatch(-initialVelocity);
//                } else if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
//                        getScrollRange())) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
//                if (mIsBeingDragged && getChildCount() > 0) {
//                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
//                            getScrollRange())) {
//                        ViewCompat.postInvalidateOnAnimation(this);
//                    }
//                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

//        if (mVelocityTracker != null) {
//            mVelocityTracker.addMovement(vtev);
//        }
        vtev.recycle();
        return true;
    }

    private void endDrag() {
        mIsBeingDragged = false;

//        recycleVelocityTracker();
        stopNestedScroll();

//        if (mEdgeGlowTop != null) {
//            mEdgeGlowTop.onRelease();
//            mEdgeGlowBottom.onRelease();
//        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
//            if (mVelocityTracker != null) {
//                mVelocityTracker.clear();
//            }
        }
    }

    // NestedScrollingChild
    private NestedScrollingChildHelper mChildHelper;

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
