package com.rexy.hook.handler;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.interfaces.IProxyClickListener;
import com.rexy.hook.record.TouchRecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * this class use to hook click event of any view .
 * every time the user make a touch down event,it will find and install a proxy click listener to all clickable view.
 *
 * @author: rexy
 * @date: 2017-03-08 16:56
 */
public class HandlerProxyClick extends HookHandler {
    /**
     * a reflect method to get getListenerInfo from a View.
     */
    private static Method sHookMethod;

    /**
     * a reflect field to get mOnClickListener object from ListenerInfo object.
     */
    private static Field sHookFiled;

    /**
     * touch down position x
     */
    private float mDownX;
    /**
     * touch down position y
     */
    private float mDownY;

    /**
     * touch down timestamp
     */
    private long mDownTime;

    /**
     * a proxy click listener that can be rewritten the base onClickListener.do you business here .
     * return true to intercept the original clickListener.
     */
    IProxyClickListener mInnerClickProxy = new IProxyClickListener() {
        @Override
        public boolean onProxyClick(WrapClickListener wrap, View v) {
            return reportResult(new ResultProxyClick(v, getTag(), mDownX, mDownY,mDownTime));
        }
    };

    private int mPrivateTagKey = System.identityHashCode(this);

    public HandlerProxyClick(String tag) {
        super(tag);
        mPrivateTagKey=mPrivateTagKey|((0xFFFF)<<24);
    }

    /**
     * install proxy click listener in a recursive function
     *
     * @param view                  root view .
     * @param recycledContainerDeep view hierarchy level
     */
    private void hookViews(View view, int recycledContainerDeep) {
        if (view.getVisibility() == View.VISIBLE) {
            boolean forceHook = recycledContainerDeep == 1;
            if (view instanceof ViewGroup) {
                boolean existAncestorRecycle = recycledContainerDeep > 0;
                ViewGroup p = (ViewGroup) view;
                if (!(p instanceof AbsListView || p instanceof RecyclerView) || existAncestorRecycle) {
                    hookClickListener(view, recycledContainerDeep, forceHook);
                    if (existAncestorRecycle) {
                        recycledContainerDeep++;
                    }
                } else {
                    recycledContainerDeep = 1;
                }
                int childCount = p.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = p.getChildAt(i);
                    hookViews(child, recycledContainerDeep);
                }
            } else {
                hookClickListener(view, recycledContainerDeep, forceHook);
            }
        }
    }

    /**
     * hook original click listener of a single view.
     *
     * @param forceHook force hook  View if it is direct child of some AbsListView 。
     */
    private void hookClickListener(View view, int recycledContainerDeep, boolean forceHook) {
        boolean needHook = forceHook;
        if (!needHook) {
            needHook = view.isClickable();
            if (needHook && recycledContainerDeep == 0) {
                needHook = view.getTag(mPrivateTagKey) == null;
            }
        }
        if (needHook) {
            try {
                Object getListenerInfo = sHookMethod.invoke(view);
                View.OnClickListener baseClickListener = getListenerInfo == null ? null : (View.OnClickListener) sHookFiled.get(getListenerInfo);//获取已设置过的监听器
                if ((baseClickListener != null && !(baseClickListener instanceof IProxyClickListener.WrapClickListener))) {
                    sHookFiled.set(getListenerInfo, new IProxyClickListener.WrapClickListener(baseClickListener, mInnerClickProxy));
                    view.setTag(mPrivateTagKey, recycledContainerDeep);
                }
            } catch (Exception e) {
                reportError(e,"hook");
            }
        }
    }

    /**
     * ensure all the hook method and field is available.
     *
     * @param caller   the InteractionHook who has context and hook data .
     * @param activity context of a current Activity.
     */
    @Override
    public void init(InteractionHook caller, Activity activity) {
        super.init(caller, activity);
        if (sHookMethod == null) {
            try {
                Class viewClass = Class.forName("android.view.View");
                if (viewClass != null) {
                    sHookMethod = viewClass.getDeclaredMethod("getListenerInfo");
                    if (sHookMethod != null) {
                        sHookMethod.setAccessible(true);
                    }
                }
            } catch (Exception e) {
                reportError(e, "init");
            }
        }
        if (sHookFiled == null) {
            try {
                Class listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
                if (listenerInfoClass != null) {
                    sHookFiled = listenerInfoClass.getDeclaredField("mOnClickListener");
                    if (sHookFiled != null) {
                        sHookFiled.setAccessible(true);
                    }
                }
            } catch (Exception e) {
                reportError(e, "init");
            }
        }
    }

    /**
     * if init success and everything is ready to handle any task .
     */
    @Override
    public boolean supportHandle() {
        return super.supportHandle() && sHookMethod != null && sHookFiled != null;
    }

    @Override
    public boolean handle(InteractionHook caller) {
        View rootView = caller.getRootView();
        if (rootView != null) {
            TouchRecord down=caller.getTouchRecord();
            mDownX=down.getDownX();
            mDownY=down.getDownY();
            mDownTime = down.getDownTime();
            hookViews(rootView, 0);
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        mInnerClickProxy = null;
        super.destroy();
    }

    public static class ResultProxyClick extends HandleResult {
        private int mClickX;
        private int mClickY;
        private long mDownTime;

        private ResultProxyClick(View target, String tag, float clickX, float clickY, long downTime) {
            super(target, tag);
            mClickX = (int) clickX;
            mClickY = (int) clickY;
            mDownTime = downTime;
        }

        /**
         * get maybe click x in window
         */
        public int getClickX() {
            return mClickX;
        }

        /**
         * get maybe click y in window
         */
        public int getClickY() {
            return mClickY;
        }

        /**
         * get click touch down timestamp;
         */
        public long getDownTime() {
            return mDownTime;
        }

        /**
         * get click touch up timestamp;
         */
        public long getUpTime() {
            return getTimestamp();
        }

        @Override
        protected void toShortStringImpl(StringBuilder receiver) {
            receiver.append(formatView(getTargetView())).append("{");
            receiver.append("clickX=").append(getClickX()).append(',');
            receiver.append("clickY=").append(getClickY()).append(',');
            receiver.append("downTime=").append(formatTime(getDownTime(),null)).append(',');
            receiver.append("time=").append(formatTime(getTimestamp(),null)).append(',');
            receiver.setCharAt(receiver.length()-1,'}');
        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {
            receiver.put("view",getTargetView());
            receiver.put("time",getTimestamp());
            receiver.put("downTime", getTimestamp());
            receiver.put("clickX",getClickX());
            receiver.put("clickY",getClickY());
        }
    }
}