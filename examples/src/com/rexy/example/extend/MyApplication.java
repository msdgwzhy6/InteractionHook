package com.rexy.example.extend;

import android.app.Application;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.interfaces.IHandleListener;
import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.hook.interfaces.IHookHandler;

/**
 * Created by rexy on 17/8/2.
 */

public class MyApplication extends Application implements IHandleListener {
    @Override
    public void onCreate() {
        super.onCreate();
        InteractionHook.setGlobalHandleListener(this);
    }

    @Override
    public boolean onReceiveHandleError(IHookHandler handler, Throwable error, String category) {
        return false;
    }

    @Override
    public boolean onReceiveHandleResult(IHookHandler handler, IHandleResult result) {
        return false;
    }
}
