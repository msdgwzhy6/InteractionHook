package com.rexy.hook.interfaces;

import android.app.Activity;

import com.rexy.hook.InteractionHook;

/**
 *
 * a interface that should be realized by the handler {@link com.rexy.hook.handler.HookHandler} , every handler is managed by {@link InteractionHook} and dispatch to do some task .
 *
 * Created by rexy on 17/7/31.
 */

public interface IHookHandler {

    /**
     * hook handler tag ,used to distinguish the other handler
     */
    String getTag();

    /**
     * set handler disable or enable
     */
    void setHandlerEnable(boolean handlerEnable);

    /**
     * do some initialization here .
     *
     * @param activity     context of a current Activity.
     * @param caller the InteractionHook who has context and hook data and manage this handler .
     */
    void init(InteractionHook caller, Activity activity);

    /**
     * any time receive a inner error call this method to report to its caller
     * @param category  function group category
     */
    void reportError(Throwable error, String category);

    /**
     * report handle result to some one subscribe it
     * @param result handle result .
     * @return  return true to intercept the system handle .
     */
    boolean reportResult(IHandleResult result);

    /**
     * whether support to handle any task.
     *
     * @return true for available to dispatch further handle task.
     */
    boolean supportHandle();

    /**
     * handle any task here as a entry.
     *
     * @param caller the InteractionHook who has context and hook data .
     * @return true if handled
     */
    boolean handle(InteractionHook caller);

    /**
     * do some finalize here
     */
    void destroy();
}
