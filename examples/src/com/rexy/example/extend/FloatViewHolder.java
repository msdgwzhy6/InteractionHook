package com.rexy.example.extend;

import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * @author: rexy
 * @date: 2017-08-07 10:21
 */
public abstract class FloatViewHolder {

    private View mRootView;
    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private boolean mViewAdded;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float lastMotionX;
        float lastMotionY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastMotionX = event.getRawX();
                    lastMotionY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX();
                    float y = event.getRawY();
                    updateViewBy((int) (x - lastMotionX), (int) (y - lastMotionY));
                    lastMotionX = x;
                    lastMotionY = y;
                    break;
            }
            return false;
        }
    };

    public FloatViewHolder(View rootView, WindowManager windowManager) {
        mRootView = rootView;
        mWindowManager = windowManager;
    }

    public void show() {
        show(-1, -2, 0, mRootView.getResources().getDisplayMetrics().heightPixels-mRootView.getMeasuredHeight());
    }

    public void hide() {
        detachView();
    }

    public void destroy() {
        if (wmParams != null) {
            detachView();
        }
        mRootView = null;
        mWindowManager = null;
        mTouchListener = null;
    }

    public boolean isDestroy() {
        return mRootView == null;
    }

    public View getRootView() {
        return mRootView;
    }

    protected abstract View getTouchDragView();

    protected void show(int width, int height, int positionX, int positionY) {
        if (!mViewAdded) {
            attachView(width, height);
            View touchDragView = getTouchDragView();
            if (touchDragView != null) {
                touchDragView.setClickable(true);
                touchDragView.setOnTouchListener(mTouchListener);
            }
            if (positionX != 0 || positionY != 0) {
                updateViewTo(positionX, positionY);
            }
        }
    }

    protected void updateViewBy(int movedX, int movedY) {
        updateViewTo(wmParams.x + movedX, wmParams.y + movedY);
    }

    protected void updateViewTo(int x, int y) {
        if (mRootView != null && mWindowManager != null && wmParams != null) {
            wmParams.x = x;
            wmParams.y = y;
            mWindowManager.updateViewLayout(mRootView, wmParams);
        }
    }

    protected void updateViewWidth(int x, int y) {
        if (mRootView != null && mWindowManager != null && wmParams != null) {
            boolean changed = false;
            if (x != 0 && wmParams.width != x) {
                changed = true;
                wmParams.width = x;
            }
            if (y != 0 && wmParams.height != y) {
                changed = true;
                wmParams.height = y;
            }
            if (changed) {
                mWindowManager.updateViewLayout(mRootView, wmParams);
            }
        }
    }


    private void attachView(int width, int height) {
        if (!mViewAdded && mRootView != null && mWindowManager != null) {
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < 24 ?
                    WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            wmParams.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
            wmParams.width = width;
            wmParams.height = height;
            mWindowManager.addView(mRootView, wmParams);
            mViewAdded = true;
        }
    }

    private void detachView() {
        if (mViewAdded && mRootView != null && mWindowManager != null) {
            mWindowManager.removeView(mRootView);
            View touchDragView = getTouchDragView();
            if (touchDragView != null) {
                touchDragView.setOnTouchListener(null);
            }
            mViewAdded = false;
        }
        wmParams = null;
    }
}
