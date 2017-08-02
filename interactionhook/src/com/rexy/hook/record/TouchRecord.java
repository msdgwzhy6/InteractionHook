package com.rexy.hook.record;

import android.support.v4.util.Pools;
import android.view.MotionEvent;
import android.view.View;

/**
 * <p>
 * this class used for recording a series of touch event from ACTION_DOWN to ACTION_UP or ACTION_CANCEL .
 * </p>
 *
 * <p>
 * it records timestamp, x and y position , touch id of ACTION_DOWN and ACTION_UP|ACTION_CANCEL . and it will analyze whether it is dragging ,at last it would calculate the fling speed
 * </p>
 *
 * @author: rexy
 * @date: 2017-07-31 14:28
 */
public class TouchRecord {
    /**
     * target View who consumed these series of touch event .
     */
    View mTarget = null;

    /**
     * whether the user touch move considered as scroll action.
     */
    boolean mDragged = false;

    /**
     * whether the touch event stop by cancel
     */
    boolean mCancel = false;

    /**
     * touch down unique id
     */
    int mDownId = -1;
    /**
     * touch down timestamp
     */
    long mDownTime;
    /**
     * touch down window position x
     */
    float mDownX;
    /**
     * touch down window position y
     */
    float mDownY;
    /**
     * velocity x only when drag happened ever
     */
    float mVelocityX;




    /**
     * touch up unique id
     */
    int mUpId;
    /**
     * touch up timestamp
     */
    long mUpTime;
    /**
     * touch up window position x
     */
    float mUpX;
    /**
     * touch up window position y
     */
    float mUpY;
    /**
     * velocity y only when drag happened ever
     */
    float mVelocityY;

    /**
     * create a new instance of a copy  .
     */
    public TouchRecord copy() {
        TouchRecord copy = start(null);
        copy.mDownId=mDownId;
        copy.mDownX=mDownX;
        copy.mDownY=mDownY;
        copy.mDownTime=mDownTime;

        copy.mUpId=mUpId;
        copy.mUpX=mUpX;
        copy.mUpY=mUpY;
        copy.mUpTime=mUpTime;

        copy.mVelocityX=mVelocityX;
        copy.mVelocityY=mVelocityY;

        copy.mCancel=mCancel;
        copy.mDragged=mDragged;
        copy.mTarget=mTarget;
        return copy;
    }

    /**
     * get touch down x position
     */
    public float getDownX() {
        return mDownX;
    }

    /**
     * get touch down y position
     */
    public float getDownY() {
        return mDownY;
    }

    /**
     * get touch down timestamp
     */
    public long getDownTime() {
        return mDownTime;
    }

    /**
     * get touch up or cancel x position
     */
    public float getUpX() {
        return mUpX;
    }

    /**
     * get touch up or cancel y position
     */
    public float getUpY() {
        return mUpY;
    }

    /**
     * get touch up or cancel timestamp
     */
    public long getUpTime() {
        return mUpTime;
    }

    /**
     * get touch up  fling x velocity
     */
    public float getVelocityX(){
        return mVelocityX;
    }

    /**
     * get touch up  fling y velocity
     */
    public float getVelocityY(){
        return mVelocityY;
    }

    public View getTargetView() {
        return mTarget;
    }

    /**
     * stop touch monitor with the last MotionEvent .
     * @param e ACTION_UP OR ACTION_CANCEL touch event .
     * @param cancel true indicates it is a ACTION_CANCEL event .
     */
    public void stop(MotionEvent e, boolean cancel) {
        int pointIndex =e.getActionIndex();
        if (pointIndex != -1) {
            mUpId = e.getPointerId(pointIndex);
            mUpTime = System.currentTimeMillis();
            mUpX = e.getX(pointIndex);
            mUpY = e.getY(pointIndex);
        }
        mCancel = cancel;
    }

    /**
     * whether it was a canceled touch event .
     */
    public boolean isCanceled(){
        return mCancel;
    }

    /**
     * whether it could be a dragged gesture
     */
    public boolean isDraggedPossible() {
        return mDragged;
    }

    /**
     * whether it could be a click action
     */
    public boolean isClickPossible(float slop) {
        if (mCancel || mDownId == -1 || mUpId == -1 || mDownTime == 0 || mUpTime == 0) {
            return false;
        } else {
            return Math.abs(mDownX - mUpX) < slop && Math.abs(mDownY - mUpY) < slop;
        }
    }

    public void recycle() {
        mTarget = null;
        mCancel = false;
        mDragged = false;
        mDownId = mUpId = 0;
        mDownTime = mUpTime = 0;
        mDownX = mUpX = 0;
        mDownY = mUpY = 0;
        mVelocityX=mVelocityY=0;
        sTouchRecordPool.release(this);
    }

    /**
     * start monitor with a ACTION_DOWN event .
     * @param e touch down event .
     * @return a new TouchRecord object .
     */
    public static TouchRecord start(MotionEvent e) {
        TouchRecord tr = sTouchRecordPool.acquire();
        if (tr == null) {
            tr = new TouchRecord();
        }
        int pointIndex = e == null ? -1 : e.getActionIndex();
        if (pointIndex != -1) {
            tr.mDownId = e.getPointerId(pointIndex);
            tr.mDownTime = System.currentTimeMillis();
            tr.mDownX = e.getX(pointIndex);
            tr.mDownY = e.getY(pointIndex);
        }
        return tr;
    }

    private static Pools.Pool<TouchRecord> sTouchRecordPool = new Pools.SimplePool(8);
}
