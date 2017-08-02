package com.rexy.hook.handler;

import android.app.Activity;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewConfiguration;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.record.TouchRecord;

import java.util.Map;

/**
 * this class is used to prevent a fast double click at a same view . in most case it would be exactly correct.
 * the interval of twice click will be calculated dynamically and apply every MAX_TRACK_INTERVAL_COUNT click.
 *
 * @author: rexy
 * @date: 2017-03-08 16:56
 */
public class HandlerPreventFastClick extends HookHandler {

    private static int MIN_CLICK_INTERVAL = 120;
    private static int MAX_CLICK_INTERVAL = 500;
    private static int STEP_CLICK_INTERVAL = 20;
    private static int MAX_TRACK_INTERVAL_COUNT = 10;
    private static SparseIntArray sIntervalTracker = new SparseIntArray();

    private int[] mLocation = new int[2];

    /**
     * click interval between twice continuous click event, less than it we considerate as a fast click and should be prevented.
     */
    private int mClickInterval = (MIN_CLICK_INTERVAL + MAX_CLICK_INTERVAL) >> 1;
    /**
     * a min distance considerate as not moved ever before.
     */
    private float mTouchSlop;
    /**
     * a min distance we considerate it click cross two view .
     */
    private float mClickDistance;

    /**
     * don't prevent fast click just next touch round.
     */
    private boolean mIgnoreNextRound = false;

    /**
     * dynamic adjust interval of twice continuous click .
     */
    private boolean mDynamicAdjustInterval = true;

    public HandlerPreventFastClick(String tag) {
        super(tag);
    }

    @Override
    public void init(InteractionHook caller, Activity activity) {
        super.init(caller, activity);
        mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        mClickDistance = activity.getResources().getDisplayMetrics().widthPixels - mTouchSlop;
    }

    /**
     * intercept touch if needed .
     *
     * @param caller the InteractionHook who has context and hook data .
     * @return true to intercept the touch event
     */
    @Override
    public boolean handle(InteractionHook caller) {
        TouchRecord cur = caller.getTouchRecord(), pre = caller.getLastTouchRecord();
        boolean intercept = false;
        if (mIgnoreNextRound) {
            mIgnoreNextRound = false;
        } else {
            if (cur != null && pre != null && pre.isClickPossible(mTouchSlop)) {
                int interval = (int) (cur.getDownTime() - pre.getUpTime());
                intercept = interval > 0 && interval < mClickInterval;
                if (intercept && mClickDistance > 0 && interval > MIN_CLICK_INTERVAL) {
                    float dx = cur.getDownX() - pre.getUpX(), dy = cur.getDownY() - pre.getUpY();
                    intercept = Math.sqrt(dx * dx + dy * dy) < mClickDistance;
                }
                if (intercept) {
                    View targetView = pre.getTargetView();
                    if (intercept = clickViewAt(cur.getDownX(), cur.getDownY(), targetView)) {
                        reportResult(new ResultPreventFastClick(targetView, cur, mClickInterval));
                    }
                }
                if (mDynamicAdjustInterval && interval < MAX_CLICK_INTERVAL) {
                    adjustIntervalDynamic(interval);
                }
            }
        }
        return intercept;
    }

    /**
     * judge the given window local position is in the View bounds
     * @param windowX click position x in window
     * @param windowY click position y in window
     * @param clickView last target view who handled the touch event.
     * @return true if this touch down over the last click View.
     */
    private boolean clickViewAt(float windowX, float windowY, View clickView) {
        if (clickView != null) {
            clickView.getLocationInWindow(mLocation);
            float inWindowLeft = mLocation[0];
            float inWindowRight = inWindowLeft + clickView.getWidth();
            float inWindowTop = mLocation[1];
            float inWindowBottom = inWindowTop + clickView.getHeight();
            if (windowX >= inWindowLeft && windowX <= inWindowRight && windowY >= inWindowTop && windowY <= inWindowBottom) {
                return true;
            }
        }
        return false;
    }

    /**
     * set ignore the next round to prevent fast click,just do nothing when accept a serious of touch event.
     *
     * @param ignoreNextRound true to ignore the next round .
     */
    public void setIgnoreNextRound(boolean ignoreNextRound) {
        mIgnoreNextRound = ignoreNextRound;
    }

    /**
     * dynamic adjust the interval of twice continuous
     *
     * @param duration last twice click time gap.
     */
    void adjustIntervalDynamic(int duration) {
        if (duration > 0) {
            int scale = Math.max(10, STEP_CLICK_INTERVAL);
            int key = Math.max(1, duration / scale);
            sIntervalTracker.put(key, sIntervalTracker.get(key, 0) + 1);
            int trackCount = sIntervalTracker.get(MAX_CLICK_INTERVAL, 0) + 1;
            sIntervalTracker.put(MAX_CLICK_INTERVAL, trackCount);
            if (trackCount >= MAX_TRACK_INTERVAL_COUNT) {
                int size = sIntervalTracker.size();
                float avg = 0, avg2 = 0, total = trackCount;
                for (int i = 0; i < size; i++) {
                    key = sIntervalTracker.keyAt(i);
                    if (key != MAX_CLICK_INTERVAL) {
                        avg += (key * sIntervalTracker.get(key) / total);
                        avg2 += (key * key * sIntervalTracker.get(key) / total);
                    }
                }
                if (avg != 0) {
                    int qx = (int) (Math.sqrt(avg2 - avg * avg) * scale);
                    int ex = Math.max(Math.round(avg * scale) + scale / 2, MIN_CLICK_INTERVAL);
                    float minPercent = Math.min(1, qx / (float) ex);
                    int finalInterval = (int) (MIN_CLICK_INTERVAL * minPercent + ex * (1 - minPercent));
                    if (finalInterval != mClickInterval) {
                        mClickInterval = finalInterval;
                    }
                }
                sIntervalTracker.clear();
            }
        }
    }

    public static class ResultPreventFastClick extends HandleResult {
        private int mClickX;
        private int mClickY;
        private int mClickAverageInterval;

        private ResultPreventFastClick(View target, TouchRecord down, int clickInterval) {
            super(target, down.getDownTime());
            mClickX = (int) down.getDownX();
            mClickY = (int) down.getDownY();
            mClickAverageInterval = clickInterval;
        }

        /**
         * get maybe click x in window
         */
        public int getClickX() {
            return mClickX;
        }

        /**
         * get maybe click y in window
         */
        public int getClickY() {
            return mClickY;
        }

        /**
         * get average click interval
         */
        public int getClickAverageInterval() {
            return mClickAverageInterval;
        }

        @Override
        protected void toShortStringImpl(StringBuilder receiver) {
            receiver.append(formatView(getTargetView())).append("{");
            receiver.append("time=").append(formatTime(getTimestamp(),null)).append(',');
            receiver.append("clickX=").append(getClickX()).append(',');
            receiver.append("clickY=").append(getClickY()).append(',');
            receiver.append("clickInterval=").append(getClickAverageInterval()).append(',');
            receiver.setCharAt(receiver.length()-1,'}');
        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {
            receiver.put("view",getTargetView());
            receiver.put("time",getTimestamp());
            receiver.put("clickInterval",getClickAverageInterval());
            receiver.put("clickX",getClickX());
            receiver.put("clickY",getClickY());
        }
    }
}