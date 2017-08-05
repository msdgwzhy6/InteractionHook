package com.rexy.hook.handler;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.rexy.hook.record.InputRecord;
import com.rexy.hook.InteractionHook;

import java.util.Map;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-08-01 10:49
 */
public class HandlerInput extends HookHandler {

    private ViewGroup mRootView;
    /**
     * current focus edit text widget
     */
    private EditText mEditText;
    /**
     * current focus view maybe the same with {@link #mEditText}
     */
    private InputRecord mRecordHeader;
    private InputRecord mRecordPointer;

    private static int sTimeMaxInterval = 1000 * 5;

    /**
     * global focus change listener to observe the current focus View.
     */
    ViewTreeObserver.OnGlobalFocusChangeListener mFocusListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            onFocusViewChanged(newFocus, oldFocus);
        }
    };

    /**
     * TextWatcher to observe the text change of the current focus EditText
     */
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (before == 0 || count == 0) {
                if (before > 1 && (s == null || s.length() == 0)) {
                    return;
                }
                if (count > 1 && s != null && count == s.length()) {
                    return;
                }
                int added = before == 0 ? count : -before;
                long time = System.currentTimeMillis();
                if (mRecordHeader == null && mRecordPointer == null) {
                    mRecordPointer = mRecordHeader = InputRecord.obtain(mEditText, time);
                } else {
                    if (mEditText != mRecordPointer.getTargetView() || !mRecordPointer.support(time)) {
                        mRecordPointer.mNext = InputRecord.obtain(mEditText, time);
                        mRecordPointer = mRecordPointer.mNext;
                    }
                }
                mRecordPointer.record(time, added,s);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public HandlerInput(String tag) {
        super(tag);
    }

    @Override
    public void init(InteractionHook caller, Activity activity) {
        super.init(caller, activity);
        mRootView = caller.getRootView();
        mRootView.getViewTreeObserver().addOnGlobalFocusChangeListener(mFocusListener);
        onFocusViewChanged(mRootView.findFocus(), null);
    }

    /**
     * call when the focus view changed in this window
     *
     * @param focusView new focus View
     * @param oldFocusView old focus View may be null
     */
    private void onFocusViewChanged(View focusView, View oldFocusView) {
        EditText prevEdit = mEditText;
        if (focusView instanceof EditText) {
            EditText edit = (EditText) focusView;
            if (edit != mEditText) {
                inputListenerFor(mEditText, false);
                inputListenerFor(edit, true);
                mEditText = edit;
            }
        }
        if (mRecordHeader != null) {
            if ((oldFocusView instanceof EditText) || (focusView == null && oldFocusView == mRootView)) {
                InputRecord header = mRecordHeader;
                mRecordHeader = mRecordPointer = null;
                handleInputRecordAfterUnfocused(prevEdit, header);
            }
        }
    }

    /**
     * add or remove TextWatcher with a given EditText .
     *
     * @param install true to add TextWatch ,false to remove
     */
    private void inputListenerFor(EditText edit, boolean install) {
        if (edit != null) {
            if (install) {
                edit.addTextChangedListener(mTextWatcher);
            } else {
                edit.removeTextChangedListener(mTextWatcher);
            }
        }
    }

    @Override
    public boolean handle(InteractionHook caller) {
        return false;
    }


    private void handleInputRecordAfterUnfocused(EditText edit, InputRecord header) {
        while (header != null && header.getTargetView() != edit) {
            header = header.recycle();
        }
        InputRecord pointer = header == null ? null : header.mNext, previous = header;
        while (pointer != null) {
            if (pointer.getTargetView() == edit) {
                previous = pointer;
                pointer = pointer.mNext;
            } else {
                previous.mNext = pointer.recycle();
                pointer = previous.mNext;
            }
        }

        if (header != null) {
            ResultInput result = new ResultInput(edit,header.getReferTime());
            long lastTime = header.getReferTime();
            while (header != null) {
                lastTime = analyzeInputResult(lastTime, header.getReferTime(), header.getKeyRecord(), result);
                result.mText=header.getText();
                header = header.recycle();
            }
            reportResult(result);
        }
    }

    private long analyzeInputResult(long lastTime, long refer, SparseIntArray array, ResultInput result) {
        int size = array.size();
        for (int i = 0; i < size; i++) {
            int key = array.keyAt(i);
            int count = array.get(key);
            int optCount;
            if (count > 0) {
                optCount = count;
                result.mTypeInputCount += optCount;
            } else {
                optCount = -count;
                result.mTypeDeleteCount += optCount;
            }
            long currentTime = refer + key;
            if (currentTime - lastTime < sTimeMaxInterval) {
                result.mValidTimeCount += optCount;
                result.mValidTimeCost += (currentTime - lastTime);
            }
            lastTime = currentTime;
        }
        return lastTime;
    }

    @Override
    public void destroy() {
        super.destroy();
        inputListenerFor(mEditText, false);
        mRootView.getViewTreeObserver().removeOnGlobalFocusChangeListener(mFocusListener);
        mEditText = null;
        mRootView = null;
        mTextWatcher = null;
        mFocusListener = null;
        mRecordPointer = mRecordHeader;
        while (mRecordPointer != null) {
            mRecordHeader = mRecordPointer.recycle();
            mRecordPointer = mRecordHeader;
        }
    }

    /**
     * input analytics result after a EditText lost its focus.
     */
    public static class ResultInput extends HandleResult {
        /**
         * timestamp of the first time when user give a input value
         */
        private long mStartTime;

        /**
         * the latest text of the EditText
         */
        private CharSequence mText;

        /**
         * input add char count
         */
        private int mTypeInputCount;

        /**
         * input delete char count
         */
        private int mTypeDeleteCount;

        /**
         * input total time cost considerate as valid
         */
        private long mValidTimeCost;

        /**
         * input time that considerate as valid in a max interaction time 5 second
         * mValidTimeCount/mValidTimeCost will be the average input speed .
         */
        private int mValidTimeCount;


        private ResultInput(View target,long startTime) {
            super(target);
            mStartTime=startTime;
        }

        /**
         * get the latest text of the target EditText
         * @return
         */
        public CharSequence getText(){
            return mText;
        }

        public long getStartTime() {
            return mStartTime;
        }

        public long getEndTime() {
            return getTimestamp();
        }

        public int getInputCount() {
            return mTypeInputCount + mTypeDeleteCount;
        }

        public int getDeleteCount() {
            return mTypeDeleteCount;
        }

        /**
         * @param unit every second for 1000,every minute for 1000*60;
         * @return input speed in measure unit
         */
        public float getInputSpeed(int unit) {
            return (float) (mValidTimeCount * unit) / (mValidTimeCost + 1);
        }

        @Override
        protected void toShortStringImpl(StringBuilder receiver) {
            receiver.append(formatView(getTargetView())).append("{");
            receiver.append("time=").append(formatTime(getTimestamp(), null)).append(',');
            receiver.append("startTime=").append(formatTime(getStartTime(), null)).append(',');
            receiver.append("text=").append(getText()).append(',');
            receiver.append("input=").append(getInputCount()).append(',');
            receiver.append("delete=").append(getDeleteCount()).append(',');
            receiver.append("speed=").append((int)getInputSpeed(60 * 1000)).append(',');
            receiver.setCharAt(receiver.length() - 1, '}');
        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {
            receiver.put("view", getTargetView());
            receiver.put("time", getTimestamp());
            receiver.put("startTime", getStartTime());
            receiver.put("text",getText());
            receiver.put("inputCount", getInputCount());
            receiver.put("deleteCount", getDeleteCount());
            receiver.put("speed", getInputSpeed(60 * 1000));
        }
    }
}
