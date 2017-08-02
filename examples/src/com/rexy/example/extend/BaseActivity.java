package com.rexy.example.extend;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Window;

import com.rexy.hook.InteractionHook;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-06-05 14:45
 */
public class BaseActivity extends FragmentActivity {
    private InteractionHook mInteractionHook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mInteractionHook = new InteractionHook(this, true, "rexy_interaction");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInteractionHook != null) {
            mInteractionHook.destroy();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = mInteractionHook == null ? false : mInteractionHook.onTouch(ev);
        if (handled) {
            final long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), 0);
            cancelEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            if (mInteractionHook != null) {
                mInteractionHook.onTouch(cancelEvent);
            }
            super.dispatchTouchEvent(cancelEvent);
        }
        return handled || super.dispatchTouchEvent(ev);
    }
}
