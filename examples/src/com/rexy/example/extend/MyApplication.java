package com.rexy.example.extend;

import android.app.Application;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.interfaces.IHandleListener;
import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.hook.interfaces.IHookHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rexy on 17/8/2.
 */

public class MyApplication extends Application implements IHandleListener {

    List<IHandleListener> mListener=new ArrayList();

    @Override
    public void onCreate() {
        super.onCreate();
        InteractionHook.setGlobalHandleListener(this);
    }

    @Override
    public boolean onReceiveHandleError(IHookHandler handler, Throwable error, String category) {
        boolean result=false;
        for(IHandleListener l :mListener){
            result=l.onReceiveHandleError(handler,error,category)||result;
        }
        return result;
    }

    @Override
    public boolean onReceiveHandleResult(IHookHandler handler, IHandleResult result) {
        boolean intercept=false;
        for(IHandleListener l :mListener){
            intercept=l.onReceiveHandleResult(handler,result)||intercept;
        }
        return intercept;
    }

    public void registerHandleListener(IHandleListener l){
        if(!mListener.contains(l)){
            mListener.add(l);
        }
    }

    public void unregisterHandleListener(IHandleListener l){
        mListener.remove(l);
    }
}
