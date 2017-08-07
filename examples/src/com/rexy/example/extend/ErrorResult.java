package com.rexy.example.extend;

import android.view.View;

import com.rexy.hook.handler.HandleResult;

import java.util.Map;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-08-07 14:03
 */
public class ErrorResult extends HandleResult {
    private Throwable mError;

    protected ErrorResult(View target, String tag, Throwable error) {
        super(target, tag);
        mError = error;
    }

    public Throwable getError() {
        return mError;
    }

    @Override
    protected void toShortStringImpl(StringBuilder receiver) {
        receiver.append(formatView(getTargetView())).append("{");
        receiver.append("time=").append(formatTime(getTimestamp(), null)).append(',');
        receiver.append("error=").append(new ErrorReportFormater(3,6,1).getDescription(getError())).append(',');
        receiver.setCharAt(receiver.length() - 1, '}');
    }

    @Override
    protected void dumpResultImpl(Map<String, Object> receiver) {
        if (getTargetView() != null) {
            receiver.put("view", getTargetView());
        }
        receiver.put("time", getTimestamp());
        receiver.put("error", getError());
    }
}
