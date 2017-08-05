package com.rexy.hook.record;

import android.support.v4.util.Pools;
import android.util.SparseIntArray;
import android.view.View;

/**
 * <p>
 * this class is used for recording EditText input information such as latest text, timestamp and change count of the char every time there is a text changed .
 * </p>
 *
 * <p>
 * each EditText will have a InputRecord to record its change .
 * </p>
 *
 * @author: rexy
 * @date: 2017-07-31 14:28
 */
public class InputRecord {
    /**
     * the target View that is ready for consuming these series of input event .
     */
    View mTarget = null;
    /**
     * the timestamp of the first char input change
     */
    long mReferTime = 0;

    /**
     * latest text of the EditText .
     */
    CharSequence mText;

    /**
     * record the deviation time refer to mReferTime as key ,
     * and added char or deleted char count as value ,
     */
    SparseIntArray mInputChange = new SparseIntArray(16);


    /**
     * point to the next InputRecord object.
     */
    public InputRecord mNext;

    private InputRecord() {
    }

    /**
     * get the EditText widget that consumed the input event .
     */
    public View getTargetView() {
        return mTarget;
    }

    /**
     * get the first timestamp that have a text change .
     */
    public long getReferTime() {
        return mReferTime;
    }

    /**
     *get the latest text of the EditText .
     */
    public CharSequence getText(){
        return mText;
    }

    /**
     * get all the text change record, timestamp and change count each array item .
     */
    public SparseIntArray getKeyRecord() {
        return mInputChange;
    }

    /**
     * save the input key in array
     *
     * @param time  timestamp of the change time .
     * @param added  text change count at this time ,added count will be position ,other will be a negative int value.
     * @param text current text of the EditText.
     */
    public void record(long time, int added,CharSequence text) {
        mText=text;
        mInputChange.put((int) (time - mReferTime), added);
    }

    /**
     * if the array support this timestamp as key , because key should less than max int value.
     *
     * @return true support to save
     */
    public boolean support(long time) {
        return (time - mReferTime) < Integer.MAX_VALUE;
    }

    /**
     * clear old value and reset to initialization and recycle self .
     */
    public InputRecord recycle() {
        InputRecord record = mNext;
        mNext = null;
        mTarget = null;
        mText=null;
        mReferTime = 0;
        mInputChange.clear();
        sTouchRecordPool.release(this);
        return record;
    }

    public static InputRecord obtain(View target, long referTime) {
        InputRecord ir = sTouchRecordPool.acquire();
        if (ir == null) {
            ir = new InputRecord();
        }
        ir.mTarget = target;
        ir.mReferTime = referTime;
        return ir;
    }

    private static Pools.Pool<InputRecord> sTouchRecordPool = new Pools.SimplePool(8);
}
