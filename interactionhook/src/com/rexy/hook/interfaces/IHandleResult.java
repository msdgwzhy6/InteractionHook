package com.rexy.hook.interfaces;

import android.view.View;

import java.util.Map;

/**
 * this interface is used to describe handle result .see {@link com.rexy.hook.handler.HandleResult}  ,it will be used by interface {@link IHandleListener}
 * after a {@link IHookHandler} produce a result.
 *
 * @author: rexy
 * @date: 2017-08-02 11:03
 */
public interface IHandleResult {
    /**
     * get target View for this handle result analyzed with
     */
    View getTargetView();

    /**
     * get timestamp for the result creation time
     * @return a millisecond timestamp from stand {@link System#currentTimeMillis()}
     */
    long getTimestamp();

    /**
     * get short description from all fields of this result
     *
     * @param sb null enable , description will write into this param or a new instance if null.
     */
    StringBuilder toShortString(StringBuilder sb);

    /**
     * dump all useful field set of the result into a given map
     * @param result null enable, all fields will be write into it or a new instance if null
     * @return return a map result that hold all useful field as a result .
     */
    Map<String, Object> dumpResult(Map<String, Object> result);
}
