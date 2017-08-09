package com.rexy.hook.record;

import android.support.v4.util.Pools;

/**
 * this class is used to record page switch option , we just interest in action onResume ,onPause , onDestroy ,onHiddenChanged .
 * just record the option type .
 */
class PageOption {
    /**
     * code for onResume
     */
    static final int OPTION_RESUME = 1;
    /**
     * code for onPause
     */
    static final int OPTION_PAUSE = 2;
    /**
     * code for fragment onHiddenChanged hide
     */
    static final int OPTION_HIDE = 3;
    /**
     * code for fragment onHiddenChanged show
     */
    static final int OPTION_SHOW = 4;
    /**
     * code for onDestroy
     */
    static final int OPTION_DESTROY = 5;


    private static Pools.Pool<PageOption> sPageOptionPool = new Pools.SimplePool(8);


    public static PageOption obtain(int option, PageOption next) {
        PageOption r = sPageOptionPool.acquire();
        if (r == null) {
            r = new PageOption();
        }
        return r.option(option, next);
    }

    int mOption;
    long mTime;
    PageOption mNext;

    private PageOption() {
    }

    private PageOption option(int option, PageOption next) {
        mOption = option;
        mTime = System.currentTimeMillis();
        mNext = next;
        return this;
    }

    public void recycle() {
        mNext = null;
        sPageOptionPool.release(this);
    }

    /**
     * get option name ,enum in {"resume","pause","hide","show","destroy"}
     */
    public String getOptionName() {
        if (mOption == OPTION_RESUME) {
            return "resume";
        }
        if (mOption == OPTION_PAUSE) {
            return "pause";
        }
        if (mOption == OPTION_HIDE) {
            return "hide";
        }
        if (mOption == OPTION_SHOW) {
            return "show";
        }
        if (mOption == OPTION_DESTROY) {
            return "destroy";
        }
        return "unknown";
    }
}
