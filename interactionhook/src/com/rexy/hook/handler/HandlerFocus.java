package com.rexy.hook.handler;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.rexy.hook.InteractionHook;

import java.util.Map;

/**
 * this handler is used for observing global focus change . when there is a focus change it will create a {@link ResultFocus} and report it to its subscriber .
 *
 * @author: rexy
 * @date: 2017-08-01 10:49
 */
public class HandlerFocus extends HookHandler {
    ViewGroup mRootView;
    /**
     * the close focus EditText
     */
    EditText mFocusEdit;
    /**
     * current focus view maybe the same with {@link #mFocusEdit}
     */
    View mFocusView;

    /**
     * global focus change listener to observe the current focus View.
     */
    ViewTreeObserver.OnGlobalFocusChangeListener mFocusListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            onFocusViewChanged(newFocus, oldFocus);
        }
    };

    public HandlerFocus(String tag) {
        super(tag);
    }

    @Override
    public void init(InteractionHook caller, Activity activity) {
        super.init(caller, activity);
        mRootView = caller.getRootView();
        mRootView.getViewTreeObserver().addOnGlobalFocusChangeListener(mFocusListener);
        View focusView = mRootView.findFocus();
        if (focusView != null) {
            onFocusViewChanged(focusView, null);
        }
    }

    /**
     * call when the focus view changed in this window
     *
     * @param focusView new focus View may be null
     * @param oldFocusView old focus View may be null
     */
    private void onFocusViewChanged(View focusView, View oldFocusView) {
        if (focusView instanceof EditText) {
            EditText edit = (EditText) focusView;
            if (edit != mFocusEdit) {
                mFocusEdit = edit;
            }
        }
        if (mFocusView != focusView) {
            mFocusView = focusView;
            reportResult(new ResultFocus(focusView == null ? oldFocusView : focusView, getTag(), focusView, oldFocusView));
        }
    }


    @Override
    public boolean handle(InteractionHook caller) {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        mRootView.getViewTreeObserver().removeOnGlobalFocusChangeListener(mFocusListener);
        mFocusEdit = null;
        mFocusView = null;
        mRootView = null;
        mFocusListener = null;
    }

    /**
     * this is a class to record result of focus change ,refer to {@link ViewTreeObserver#addOnGlobalFocusChangeListener(ViewTreeObserver.OnGlobalFocusChangeListener)}.
     */
    public static class ResultFocus extends HandleResult {

        private View mOldFocusView;
        private View mFocusView;

        private ResultFocus(View target, String tag, View focusView, View oldFocusView) {
            super(target, tag);
            mFocusView = focusView;
            mOldFocusView = oldFocusView;
        }

        /**
         * get current focus View maybe null if there is no view has focus
         */
        public View getFocusView() {
            return mFocusView;
        }

        /**
         * get the previous old focus View .
         */
        public View getOldFocusView() {
            return mOldFocusView;
        }

        @Override
        protected void toShortStringImpl(StringBuilder receiver) {
            View target = getTargetView(), view;
            if (target == null) {
                target = getOldFocusView();
            }
            receiver.append(formatView(target)).append("{");
            view = getFocusView();
            if (view == target) {
                receiver.append("focusView=").append("this,");
            } else {
                receiver.append("focusView=").append(formatView(view)).append(',');
            }
            view = getOldFocusView();
            if (view == target) {
                receiver.append("oldFocusView=").append("this,");
            } else {
                receiver.append("oldFocusView=").append(formatView(view)).append(',');
            }
            receiver.append("time=").append(formatTime(getTimestamp(),null)).append(',');
            receiver.setCharAt(receiver.length()-1,'}');
        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {
            receiver.put("view",getTargetView());
            receiver.put("time",getTimestamp());
            receiver.put("focusView",getFocusView());
            receiver.put("oldFocusView",getOldFocusView());
        }
    }
}
