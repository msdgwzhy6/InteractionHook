package com.rexy.hook.record;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.rexy.hook.handler.HandleResult;
import com.rexy.hook.interfaces.IHandleListener;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * record every activity and fragment of its lifecycle,track and analyze the page change .
 *
 * @author: rexy
 * @date: 2017-08-09 15:52
 */
public class PageTracker {
    /**
     * current activity that is resumed.
     */
    Activity mCurrentActivity;
    /**
     * current fragment that is visible
     */
    Object mCurrentFragment;
    /**
     * Activity record include Fragment record inner.
     */
    PageActivityRecord mRecords;
    /**
     * listener for a PageResult after analyze the record change .
     */
    IHandleListener mListener;

    public static void testPrint(CharSequence msg) {
        Log.d("rexy_page", msg.toString());
    }

    public void setHandleListener(IHandleListener listener) {
        mListener = listener;
    }

    public void onResume(Activity activity, Object fragment, Object parentFragment) {
        record(activity, fragment, parentFragment, PageOption.OPTION_RESUME);
        if (fragment == null) {
            setCurrentActivity(activity);
        } else {
            setCurrentFragment(fragment);
        }
    }

    public void onPause(Activity activity, Object fragment, Object parentFragment) {
        record(activity, fragment, parentFragment, PageOption.OPTION_PAUSE);
    }

    public void onHiddenChanged(Activity activity, Object fragment, Object parentFragment, boolean hidden) {
        record(activity, fragment, parentFragment, hidden ? PageOption.OPTION_HIDE : PageOption.OPTION_SHOW);
        if (!hidden) {
            setCurrentFragment(fragment);
        }
    }

    public void onDestroy(Activity activity, Object fragment, Object parentFragment) {
        record(activity, fragment, parentFragment, PageOption.OPTION_DESTROY);
    }

    private PageActivityRecord findActivityRecord(Activity activity) {
        PageActivityRecord peek = mRecords;
        while (peek != null) {
            if (peek.mActivityRef != null && peek.mActivityRef.get() == activity) {
                return peek;
            } else {
                peek = peek.mNext;
            }
        }
        return null;
    }

    /**
     * record option for current activity or fragment .
     *
     * @param activity       current activity not null
     * @param fragment       current operated fragment maybe null if there is no fragment interaction,
     * @param parentFragment parent fragment of current operated fragment if it exists
     * @param optionCode     operate code
     */
    private void record(Activity activity, Object fragment, Object parentFragment, int optionCode) {
        PageActivityRecord activityRecord = findActivityRecord(activity);
        if (fragment == null) {
            if (activityRecord == null) {
                activityRecord = new PageActivityRecord(activity, mRecords);
                mRecords = activityRecord;
            }
            activityRecord.record(optionCode);
        } else {
            if (activityRecord != null) {
                activityRecord.record(fragment, parentFragment, optionCode);
            }
        }
    }

    private void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    private void setCurrentFragment(Object fragment) {
        mCurrentFragment = fragment;
    }

    public static class PageFragmentRecord {
        WeakReference mFragment;
        PageOption mOptions;
        PageFragmentRecord mNext;
        PageFragmentRecord mChild;

        PageFragmentRecord(Object fragment, PageFragmentRecord next) {
            mFragment = new WeakReference(fragment);
            mNext = next;
        }

        void record(int optionCode) {
            mOptions = PageOption.obtain(optionCode, mOptions);
        }
    }

    public static class PageActivityRecord {
        WeakReference<Activity> mActivityRef;
        PageOption mOptions;
        PageFragmentRecord mFragmentRecords;
        PageActivityRecord mNext;

        PageActivityRecord(Activity activity, PageActivityRecord next) {
            mActivityRef = new WeakReference(activity);
            mNext = next;
        }

        PageFragmentRecord findFragmentRecordNoRecursive(Object fragment, PageFragmentRecord fromRecord) {
            PageFragmentRecord recorder = fromRecord;
            while (recorder != null) {
                if (recorder.mFragment != null && recorder.mFragment.get() == fragment) {
                    return recorder;
                } else {
                    recorder = recorder.mNext;
                }
            }
            return null;
        }

        PageFragmentRecord findFragmentRecordRecursive(Object fragment, PageFragmentRecord fromRecord) {
            PageFragmentRecord find = null;
            if (fromRecord != null) {
                if (fromRecord.mFragment != null && fromRecord.mFragment.get() == fragment) {
                    find = fromRecord;
                } else {
                    if (fromRecord.mNext != null) {
                        find = findFragmentRecordRecursive(fragment, fromRecord.mNext);
                    }
                    if (fromRecord.mChild != null && find == null) {
                        find = findFragmentRecordRecursive(fragment, fromRecord.mChild);
                    }
                }
            }
            return find;
        }

        void record(int optionCode) {
            mOptions = PageOption.obtain(optionCode, mOptions);
        }

        void record(Object fragment, Object parentFragment, int optionCode) {
            PageFragmentRecord fragmentRecord = null;
            if (mFragmentRecords == null) {
                fragmentRecord = new PageFragmentRecord(fragment, mFragmentRecords);
                mFragmentRecords = fragmentRecord;
            } else {
                if (parentFragment == null) {
                    fragmentRecord = findFragmentRecordNoRecursive(fragment, mFragmentRecords);
                    if (fragmentRecord == null) {
                        fragmentRecord = new PageFragmentRecord(fragment, mFragmentRecords);
                        mFragmentRecords = fragmentRecord;
                    }
                } else {
                    PageFragmentRecord parentRecord = findFragmentRecordRecursive(parentFragment, mFragmentRecords);
                    if (parentRecord != null) {
                        fragmentRecord = findFragmentRecordNoRecursive(fragment, parentRecord);
                        if (fragmentRecord == null) {
                            fragmentRecord = new PageFragmentRecord(fragment, parentRecord.mChild);
                            parentRecord.mChild = fragmentRecord;
                        }
                    }
                }
            }
            if (fragmentRecord != null) {
                fragmentRecord.record(optionCode);
            }
        }
    }

    public static class ResultPage extends HandleResult {

        protected ResultPage(View target, String tag) {
            super(target, tag);
        }

        @Override
        protected void toShortStringImpl(StringBuilder receiver) {

        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {

        }
    }
}
