package com.rexy.hook;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.rexy.hook.handler.HandlerFocus;
import com.rexy.hook.handler.HandlerGesture;
import com.rexy.hook.handler.HandlerInput;
import com.rexy.hook.handler.HandlerPreventFastClick;
import com.rexy.hook.handler.HandlerProxyClick;
import com.rexy.hook.interfaces.IHandleListener;
import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.hook.interfaces.IHookHandler;
import com.rexy.hook.record.PageTracker;
import com.rexy.hook.record.TouchRecord;
import com.rexy.hook.record.TouchTracker;

/**
 * <p>
 * this class manages a variety of handler {@link com.rexy.hook.handler.HookHandler},{@link IHookHandler} in order to monitor and collect global interaction event.
 * each handler focus on handling just on kind of interactions such as keyboard input,touch gesture,view click,focus change and so on.
 * </p>
 *
 * <p>
 * here is a simple example how to use to collect and monitor the use's interaction .
 * </p>
 * <p>
 * before entry process, we need register a global HandlerListener use {@link InteractionHook#setGlobalHandleListener;}
 * </p>
 * <p>
 * public class BaseActivity extends FragmentActivity {
 * private InteractionHook mInteractionHook;
 *
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * requestWindowFeature(Window.FEATURE_NO_TITLE);
 * super.onCreate(savedInstanceState);
 * mInteractionHook = new InteractionHook(this, true, "rexy_interaction");
 * }
 * @Override protected void onDestroy() {
 * super.onDestroy();
 * if (mInteractionHook != null) {
 * mInteractionHook.destroy();
 * }
 * }
 * @Override public boolean dispatchTouchEvent(MotionEvent ev) {
 * boolean handled = mInteractionHook == null ? false : mInteractionHook.onTouch(ev);
 * if (handled) {
 * final long now = SystemClock.uptimeMillis();
 * MotionEvent cancelEvent = MotionEvent.obtain(now, now,
 * MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), 0);
 * cancelEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
 * if (mInteractionHook != null) {
 * mInteractionHook.onTouch(cancelEvent);
 * }
 * super.dispatchTouchEvent(cancelEvent);
 * }
 * return handled || super.dispatchTouchEvent(ev);
 * }
 * }
 * </p>
 * @author: rexy
 * @date: 2017-07-31 14:11
 */
public class InteractionHook implements IHandleListener {
    private static IHandleListener sHandlerListener;

    private Activity mActivity = null;
    private TouchTracker mTouchTracker;
    private HandlerPreventFastClick mHandlerFastClick;
    private HandlerProxyClick mHandlerProxyClick;
    private HandlerGesture mHandlerGesture;
    private HandlerInput mHandlerInput;
    private HandlerFocus mHandlerFocus;

    /**
     * use for logger debug info
     */
    private String mLogTag;

    /**
     * @see #InteractionHook(Activity, boolean, String)
     */
    public InteractionHook(Activity activity, boolean mountHandler) {
        this(activity, mountHandler, null);
    }

    /**
     * create a hook instance for monitor user interaction
     * @param activity current context activity .
     * @param installHandler whether to install all the feature handler
     * @param logTag if not empty the log recorder is available .
     */
    public InteractionHook(Activity activity, boolean installHandler, String logTag) {
        mActivity = activity;
        ViewGroup rootView = null;
        if (mActivity.getWindow().getDecorView() instanceof ViewGroup) {
            rootView = (ViewGroup) mActivity.getWindow().getDecorView();
        }
        mTouchTracker = new TouchTracker(rootView);
        if (installHandler) {
            setHandleFastClickEnable(true);
            setHandleProxyClickEnable(true);
            setHandleFocusEnable(true);
            setHandleInputEnable(true);
            setHandleGestureEnable(true);
        }
        if (logTag != null) {
            setLogTag(logTag);
        }
    }

    /**
     * whether the log is allowed to output {@link #mLogTag}
     *
     * @return true is allowed to output a log
     */
    protected boolean isLogAccess() {
        return mLogTag != null;
    }

    /**
     * set log tag {@link #isLogAccess()}
     */
    public void setLogTag(String logTag) {
        mLogTag = logTag;
    }

    /**
     * print log information inner
     */
    protected void print(CharSequence category, CharSequence msg) {
        String tag = mLogTag + "#";
        if (category == null || msg == null) {
            msg = category == null ? msg : category;
        } else {
            tag = tag + category;
        }
        Log.d(tag, String.valueOf(msg));
    }

    /**
     * get current context activity
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * get the root view ,in most situation it will deservedly be DecorView
     */
    public ViewGroup getRootView() {
        return mTouchTracker.getRootView();
    }

    /**
     * get current TouchRecord information,which record a series of touch information from ACTION_DOWN to ACTION_UP.
     * @return return latest {@link TouchRecord}
     */
    public TouchRecord getTouchRecord() {
        return mTouchTracker.getTouchRecord();
    }

    /**
     * get the previous TouchRecord information
     */
    public TouchRecord getLastTouchRecord() {
        return mTouchTracker.getLastTouchRecord();
    }

    private <T extends IHookHandler> T dispatchDestroy(IHookHandler handler) {
        if (handler != null) {
            handler.destroy();
        }
        return (T) null;
    }

    private boolean dispatchHandle(IHookHandler handler) {
        return handler != null && handler.supportHandle() && handler.handle(this);
    }

    private boolean dispatchInit(IHookHandler handler) {
        if (handler != null) {
            handler.init(this, mActivity);
        }
        return handler != null && handler.supportHandle();
    }

    /**
     * set a handler who is interest in proxy and monitor any View click event , {@link HandlerProxyClick},{@link  android.view.View.OnClickListener}
     */
    public void setHandleProxyClick(HandlerProxyClick handler) {
        if (handler != mHandlerProxyClick) {
            dispatchDestroy(mHandlerProxyClick);
            mHandlerProxyClick = handler;
            dispatchInit(handler);
        }
    }

    /**
     * set proxy click handler enable or disable
     *
     * @param enable true will ensure the handler is set , it will set a default handler when it is null.
     */
    public void setHandleProxyClickEnable(boolean enable) {
        if (enable && mHandlerProxyClick == null) {
            setHandleProxyClick(new HandlerProxyClick("proxy-click"));
        }
        if (mHandlerProxyClick != null) {
            mHandlerProxyClick.setHandlerEnable(enable);
        }
    }

    /**
     * set a fast click handler to intercept any continuous twice click event , {@link HandlerPreventFastClick}.
     */
    public void setHandleFastClick(HandlerPreventFastClick handler) {
        if (handler != mHandlerFastClick) {
            dispatchDestroy(mHandlerFastClick);
            mHandlerFastClick = handler;
            dispatchInit(handler);
        }
    }

    /**
     * set fast click handler enable or disable
     *
     * @param enable true will ensure the handler is set , it will set a default handler when it is null.
     */
    public void setHandleFastClickEnable(boolean enable) {
        if (enable && mHandlerFastClick == null) {
            setHandleFastClick(new HandlerPreventFastClick("prevent-click"));
        }
        if (mHandlerFastClick != null) {
            mHandlerFastClick.setHandlerEnable(enable);
        }
    }

    /**
     * set fast click handler disable temporary just at next click event happened .
     */
    public void setIgnoreFastClickNextRound() {
        if (mHandlerFastClick == null) {
            setHandleFastClickEnable(true);
        }
        mHandlerFastClick.setIgnoreNextRound(true);
    }

    /**
     * set touch gesture handler to analyze any move gesture ,{@link HandlerGesture}
     */
    public void setHandleGesture(HandlerGesture handler) {
        if (handler != mHandlerGesture) {
            dispatchDestroy(mHandlerGesture);
            mHandlerGesture = handler;
            dispatchInit(handler);
            mTouchTracker.setHandleDragEnable(handler != null);
        }
    }

    /**
     * set gesture handler enable or disable
     *
     * @param enable true will ensure the handler is set , it will set a default handler when it is null.
     */
    public void setHandleGestureEnable(boolean enable) {
        if (enable && mHandlerGesture == null) {
            setHandleGesture(new HandlerGesture("gesture"));
        }
        if (mHandlerGesture != null) {
            mHandlerGesture.setHandlerEnable(enable);
            mTouchTracker.setHandleDragEnable(enable);
        }
    }

    /**
     * set input handler to handle any input by the user ,{@link HandlerInput},
     * the input event include keyboard code, device menu and so on .
     */
    public void setHandleInput(HandlerInput handler) {
        if (handler != mHandlerInput) {
            dispatchDestroy(mHandlerInput);
            mHandlerInput = handler;
            dispatchInit(handler);
        }
    }

    /**
     * set input handler enable or disable
     *
     * @param enable true will ensure the handler is set , it will set a default handler when it is null.
     */
    public void setHandleInputEnable(boolean enable) {
        if (enable && mHandlerInput == null) {
            setHandleInput(new HandlerInput("input"));
        }
        if (mHandlerInput != null) {
            mHandlerInput.setHandlerEnable(enable);
        }
    }

    /**
     * set input handler to handle any focus change event, {@link HandlerFocus},
     * the input event include keyboard code, device menu and so on .
     */
    public void setHandleFocus(HandlerFocus handler) {
        if (handler != mHandlerFocus) {
            dispatchDestroy(mHandlerFocus);
            mHandlerFocus = handler;
            dispatchInit(handler);
        }
    }

    /**
     * set input handler enable or disable
     *
     * @param enable true will ensure the handler is set , it will set a default handler when it is null.
     */
    public void setHandleFocusEnable(boolean enable) {
        if (enable && mHandlerFocus == null) {
            setHandleFocus(new HandlerFocus("focus"));
        }
        if (mHandlerFocus != null) {
            mHandlerFocus.setHandlerEnable(enable);
        }
    }

    /**
     * dispatch the top touch event,give a chance to any handler to handle ,called by the {@link Activity#dispatchTouchEvent(MotionEvent)}
     *
     * @param ev original touch event
     * @return true will intercept the touch sequence until the next down event happened.
     */
    public boolean onTouch(MotionEvent ev) {
        boolean intercept = false;
        int action = ev.getActionMasked();
        mTouchTracker.onTouch(ev, action);
        if (MotionEvent.ACTION_DOWN == action) {
            dispatchHandle(mHandlerProxyClick);
            intercept = dispatchHandle(mHandlerFastClick);
        } else {
            if ((MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action)) {
                dispatchHandle(mHandlerGesture);
            }
        }
        return intercept;
    }

    /**
     * do some recycle and destroy task,
     * called in the activity lifecycle method destroy,{@link Activity#onDestroy()}
     */
    public void destroy() {
        mHandlerProxyClick = dispatchDestroy(mHandlerProxyClick);
        mHandlerFastClick = dispatchDestroy(mHandlerFastClick);
        mHandlerGesture = dispatchDestroy(mHandlerGesture);
        mHandlerInput = dispatchDestroy(mHandlerInput);
        if (mTouchTracker != null) {
            mTouchTracker.destroy();
            mTouchTracker = null;
        }
        mActivity = null;
    }

    /**
     * called when any handler happened to catch a exception.
     *
     * @param handler  the handler who observed the error ,{@link IHookHandler},{@link IHookHandler}.
     * @param error    the original error caught by the handler
     * @param category a identify group option tag .
     * @return return true to intercept the original handle error .
     */
    @Override
    public boolean onReceiveHandleError(IHookHandler handler, Throwable error, String category) {
        boolean handled = sHandlerListener == null ? false : sHandlerListener.onReceiveHandleError(handler, error, category);
        if (!handled && isLogAccess()) {
            print("error", category + ">" + error);
        }
        return handled;
    }

    /**
     * called when a handler success handled something and created a result .
     * @param handler  handler who create this result ,{@link IHookHandler},{@link IHookHandler}.
     * @param result   handle result , a data struct see {@link com.rexy.hook.handler.HandleResult}
     * @return
     */
    @Override
    public boolean onReceiveHandleResult(IHookHandler handler, IHandleResult result) {
        boolean handled = sHandlerListener == null ? false : sHandlerListener.onReceiveHandleResult(handler, result);
        if (isLogAccess()) {
            print(handler.getTag(), result.toShortString(null));
        }
        return handled;
    }

    /**
     * set a global hook handle listener
     * @param l {@link IHandleListener}
     */
    public static void setGlobalHandleListener(IHandleListener l) {
        sHandlerListener = l;
    }


    private static PageTracker sPageTracker;

    private static PageTracker getPageTracker() {
        if (sPageTracker == null) {
            sPageTracker = new PageTracker();
        }
        sPageTracker.setHandleListener(sHandlerListener);
        return sPageTracker;
    }

    public static void onResume(Activity activity) {
        getPageTracker().onResume(activity, null, null);

    }

    public static void onPause(Activity activity) {
        getPageTracker().onPause(activity, null, null);
    }

    public static void onDestroy(Activity activity) {
        getPageTracker().onDestroy(activity, null, null);
    }

    public static void onResume(android.support.v4.app.Fragment fragment) {
        getPageTracker().onResume(fragment.getActivity(), fragment, fragment.getParentFragment());
    }

    public static void onPause(android.support.v4.app.Fragment fragment) {
        getPageTracker().onPause(fragment.getActivity(), fragment, fragment.getParentFragment());
    }

    public static void onHiddenChanged(android.support.v4.app.Fragment fragment, boolean hidden) {
        getPageTracker().onHiddenChanged(fragment.getActivity(), fragment, fragment.getParentFragment(), hidden);
    }

    public static void onDestroy(android.support.v4.app.Fragment fragment) {
        getPageTracker().onDestroy(fragment.getActivity(), fragment, fragment.getParentFragment());
    }

    public static void onResume(android.app.Fragment fragment) {
        getPageTracker().onResume(fragment.getActivity(), fragment, fragment.getParentFragment());
    }

    public static void onPause(android.app.Fragment fragment) {
        getPageTracker().onPause(fragment.getActivity(), fragment, fragment.getParentFragment());
    }

    public static void onHiddenChanged(android.app.Fragment fragment, boolean hidden) {
        getPageTracker().onHiddenChanged(fragment.getActivity(), fragment, fragment.getParentFragment(), hidden);
    }

    public static void onDestroy(android.app.Fragment fragment) {
        getPageTracker().onDestroy(fragment.getActivity(), fragment, fragment.getParentFragment());
    }
}
