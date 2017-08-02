package com.rexy.hook.record;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * this is a helpful tool class to track a whole touch event and analyze the fling speed and measure whether it was dragging.
 *
 *
 * @author: rexy
 * @date: 2017-07-31 15:27
 */
public class TouchTracker {
    /**
     * a reflect field to find mFirstTouchTarget from ViewGroup
     */
    private static Field sTouchTargetField;

    /**
     * a reflect field to find child from TouchTarget
     */
    private static Field sTouchTargetChildField;


    float mTouchSlop;
    int mMaxFlingVelocity;
    float mLastX, mLastY;

    /**
     * whether to analyze touch drag state, if true it will have a calculation while touch move event.
     */
    boolean mHandleDragEnable = false;

    /**
     * current touch record to record touch down and touch up information
     */
    TouchRecord mTouchRecord;

    /**
     * the previous touch record to record touch down and touch up information
     */
    TouchRecord mLastTouchRecord;

    /**
     * help toll for calculate touch velocity ,only when use for dragging happened
     */
    private VelocityTracker mVelocityTracker;

    /**
     * the root view to find focus touch view from,
     * general a give a DecorView is a better way
     */
    ViewGroup mRootView;

    public TouchTracker(ViewGroup rootView) {
        mRootView = rootView;
        if (rootView != null) {
            ViewConfiguration vc = ViewConfiguration.get(rootView.getContext());
            mTouchSlop = vc.getScaledTouchSlop();
            mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        }
    }

    /**
     * set drag analytics enable ,true will have little compute for analyzing drag state
     */
    public void setHandleDragEnable(boolean handleDragEnable){
        mHandleDragEnable=handleDragEnable;
    }

    public TouchRecord getTouchRecord(){
        return mTouchRecord;
    }

    public TouchRecord getLastTouchRecord(){
        return mLastTouchRecord;
    }

    /**
     * get root view , in most case it will be a DecorView .
     */
    public ViewGroup getRootView(){
        return mRootView;
    }

    /**
     * @see #onTouch(MotionEvent, int)
     */
    public void onTouch(MotionEvent ev) {
        onTouch(ev, ev.getActionMasked());
    }

    /**
     * we just analyze touch record information don't intercept any touch event.
     *
     * @param ev original touch event.
     * @param action {@link MotionEvent#ACTION_DOWN},{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP},{@link MotionEvent#ACTION_CANCEL}
     */
    public void onTouch(MotionEvent ev, int action) {
        if (action == MotionEvent.ACTION_MOVE) {
            if (mHandleDragEnable) {
                if (!mTouchRecord.mDragged) {
                    int pointIndex =ev.getActionIndex();
                    if (pointIndex != -1 && mTouchRecord.mDownId == ev.getPointerId(pointIndex)) {
                        float x = ev.getX(pointIndex), y = ev.getY(pointIndex);
                        computeDragging(x, y);
                    }
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
            }
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                if (mLastTouchRecord != null) {
                    mLastTouchRecord.recycle();
                }
                mLastTouchRecord = mTouchRecord;//swap record to last holder.
                mTouchRecord = TouchRecord.start(ev);
                mLastX = mTouchRecord.mDownX;
                mLastY = mTouchRecord.mDownY;
            } else {
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mTouchRecord.stop(ev, action == MotionEvent.ACTION_CANCEL);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                        mTouchRecord.mVelocityX = mVelocityTracker.getXVelocity();
                        mTouchRecord.mVelocityY = mVelocityTracker.getYVelocity();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    mTouchRecord.mTarget = findTargetView();
                }
            }
        }
    }

    /**
     * ensure the reflect field is available
     *
     * @return true if both field is not null.
     */
    private boolean ensureTargetField() {
        if (sTouchTargetField == null) {
            try {
                Class viewClass = Class.forName("android.view.ViewGroup");
                if (viewClass != null) {
                    sTouchTargetField = viewClass.getDeclaredField("mFirstTouchTarget");
                    sTouchTargetField.setAccessible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (sTouchTargetField != null) {
                    sTouchTargetChildField = sTouchTargetField.getType().getDeclaredField("child");
                    sTouchTargetChildField.setAccessible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sTouchTargetField != null && sTouchTargetChildField != null;
    }

    /**
     * find the target view who is interest in the touch event. null if not find
     */
    private View findTargetView() {
        View nextTarget, target = null;
        if (ensureTargetField() && mRootView != null) {
            nextTarget = findTargetView(mRootView);
            do {
                target = nextTarget;
                nextTarget = null;
                if (target instanceof ViewGroup) {
                    nextTarget = findTargetView((ViewGroup) target);
                }
            } while (nextTarget != null);
        }
        return target;
    }

    /**
     * reflect to find the TouchTarget child view,null if not found .
     */
    private View findTargetView(ViewGroup parent) {
        try {
            Object target = sTouchTargetField.get(parent);
            if (target != null) {
                Object view = sTouchTargetChildField.get(target);
                if (view instanceof View) {
                    return (View) view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * try to calculate if it will drag possibly
     */
    private void computeDragging(float x, float y) {
        if (Math.abs(y - mLastY) > mTouchSlop) {
            mTouchRecord.mDragged = true;
            mVelocityTracker = VelocityTracker.obtain();
        } else if (Math.abs(x - mLastX) > mTouchSlop) {
            mTouchRecord.mDragged = true;
            mVelocityTracker = VelocityTracker.obtain();
        }
        mLastX = x;
        mLastY = y;
    }

    /**
     * recycle TouchRecord and do finalize if need .
     */
    public void destroy() {
        mRootView = null;
        if (mTouchRecord != null) {
            mTouchRecord.recycle();
            mTouchRecord = null;
        }
        if (mLastTouchRecord != null) {
            mLastTouchRecord.recycle();
            mLastTouchRecord = null;
        }
    }
}