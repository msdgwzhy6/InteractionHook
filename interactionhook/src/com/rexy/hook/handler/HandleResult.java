package com.rexy.hook.handler;

import android.content.res.Resources;
import android.view.View;

import com.rexy.hook.interfaces.IHandleResult;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * this is a base abstract class implements {@link IHandleResult} ,it's used by {@link com.rexy.hook.interfaces.IHandleListener} after some Handler create a result which should be packaged as a HandleResult
 * </p>
 *
 * <p>
 * Different Handler maybe has a different handle result , this result class must extends this base class .
 * </p>
 *
 * @author: rexy
 * @date: 2017-08-02 13:46
 */
public abstract class HandleResult implements IHandleResult {

    private View mTargetView;
    private long mCreateTime;


    /**
     * @see #HandleResult(View, long)
     */
    protected HandleResult(View target) {
        this(target, System.currentTimeMillis());
    }

    /**
     * @param target the target View that the handler is observing with .
     * @param createTime the timestamp when this result is created
     */
    protected HandleResult(View target, long createTime) {
        mTargetView = target;
        mCreateTime = createTime;
    }

    @Override
    public View getTargetView() {
        return mTargetView;
    }

    @Override
    public long getTimestamp() {
        return mCreateTime;
    }

    @Override
    public StringBuilder toShortString(StringBuilder sb) {
        sb = sb == null ? new StringBuilder(64) : sb;
        toShortStringImpl(sb);
        return sb;
    }

    @Override
    public Map<String, Object> dumpResult(Map<String, Object> result) {
        result = result == null ? new HashMap<String, Object>() : result;
        dumpResultImpl(result);
        return result;
    }

    /**
     * dump result as a short description into a given StringBuilder
     *
     * @param receiver a not null receiver to write result
     */
    protected abstract void toShortStringImpl(StringBuilder receiver);

    /**
     * dump result to a given map .
     *
     * @param receiver not null,use for receive any result . put(key,value);
     */
    protected abstract void dumpResultImpl(Map<String, Object> receiver);

    @Override
    public String toString() {
        return toShortString(null).toString();
    }

    /**
     * format View to a short description String like "EditText[editText1#id/7f0b0051]"
     */
    public String formatView(View view) {
        if (view == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(view.getClass().getSimpleName());
            int id = view.getId();
            Resources res = (id == View.NO_ID || view.getContext() == null) ? null : view.getContext().getResources();
            if (res != null) {
                String entryName = res.getResourceEntryName(id);
                sb.append('[');
                if (entryName != null) {
                    sb.append(entryName).append("#id/");
                }
                sb.append(Integer.toHexString(id));
                sb.append(']');
            }
            return sb.toString();
        }
    }

    /**
     * format timestamp {@link SimpleDateFormat}
     */
    public String formatTime(long time, String format) {
        return new SimpleDateFormat(format == null ? "yy-M-d HH:mm:ss.SSS" : format)
                .format(new java.util.Date(time));
    }
}
