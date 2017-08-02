package com.rexy.hook.interfaces;

import android.view.View;

/**
 * a interface to monitor click event since the click listener is adapted by {@link WrapClickListener} .
 *
 * @author: rexy
 * @date: 2017-07-31 15:16
 */
public interface IProxyClickListener {

    /**
     * called when catch a click event from a observed View .
     * @param wrap the adapter click listener which wrapped the original click listener.
     * @param v click target view
     * @return return ture will intercept the original listener to dispatch its event .
     */
    boolean onProxyClick(WrapClickListener wrap, View v);

    class WrapClickListener implements View.OnClickListener {
        IProxyClickListener mProxyListener;
        View.OnClickListener mBaseListener;

        public WrapClickListener(View.OnClickListener l, IProxyClickListener proxyListener) {
            mBaseListener = l;
            mProxyListener = proxyListener;
        }

        @Override
        public void onClick(View v) {
            boolean handled = mProxyListener == null ? false : mProxyListener.onProxyClick(WrapClickListener.this, v);
            if (!handled && mBaseListener != null) {
                mBaseListener.onClick(v);
            }
        }

        public View.OnClickListener getBaseListener() {
            return mBaseListener;
        }

        public void destroy() {
            mBaseListener = null;
            mProxyListener = null;
        }
    }
}
