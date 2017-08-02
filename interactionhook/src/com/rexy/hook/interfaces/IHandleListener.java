package com.rexy.hook.interfaces;

/**
 * this interface used as callback when the {@link IHookHandler} handle some task to report handle result or an error occurred
 *
 * @author: rexy
 * @date: 2017-08-02 11:03
 */
public interface IHandleListener {
    /**
     * just report the error to the subscriber
     * @param handler the handler who originally receive the error
     * @param error a caught exception while handle some task or do some initialization
     * @param category a identify group option tag .
     * @return true for handled by the caller,otherwise do something by the handler itself.
     */
    boolean onReceiveHandleError(IHookHandler handler, Throwable error, String category);

    /**
     * called when the handler create a result and immediately informs the subscriber
     * @param handler  handler who create this result .
     * @param result   handle result , a data struct see {@link com.rexy.hook.handler.HandleResult}
     * @return  return true to intercept the system handle, in most case require to return false
     */
    boolean onReceiveHandleResult(IHookHandler handler, IHandleResult result);
}
