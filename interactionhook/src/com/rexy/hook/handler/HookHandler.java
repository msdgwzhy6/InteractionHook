package com.rexy.hook.handler;

import android.app.Activity;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.interfaces.IHandleListener;
import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.hook.interfaces.IHookHandler;


/**
 * <p>
 * this is a base abstract class implements {@link IHookHandler} ,do any hook task should create a Handler class that must extends this base class.
 *
 * @author: rexy
 * @date: 2017-08-02 13:46
 */
abstract class HookHandler implements IHookHandler {
    /**
     * whether this handler is enable
     */
    private boolean mHandlerEnable = true;

    /**
     * hook handler tag ,used to distinguish the other handler
     */
    private String mTag;

    /**
     * a callback use to subscribe for handle result and any error happened while deal with task
     */
    private IHandleListener mHandleListener;

    public HookHandler(String tag) {
        mTag = tag;
    }

    @Override
    public void setHandlerEnable(boolean handlerEnable) {
        mHandlerEnable = handlerEnable;
    }

    @Override
    public boolean supportHandle() {
        return mHandlerEnable;
    }

    @Override
    public String getTag() {
        return mTag;
    }

    @Override
    public void init(InteractionHook caller, Activity activity) {
        mHandleListener = caller;
    }

    @Override
    public void reportError(Throwable error, String category) {
        if (mHandleListener == null || !mHandleListener.onReceiveHandleError(this, error, category)) {
            error.printStackTrace();
        }
    }

    @Override
    public boolean reportResult(IHandleResult result) {
        if (mHandleListener != null && result != null) {
            return mHandleListener.onReceiveHandleResult(this, result);
        }
        return false;
    }

    @Override
    public void destroy() {
        mHandleListener = null;
    }
}
