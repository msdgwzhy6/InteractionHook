package com.rexy.example.extend;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-06-05 14:42
 */
public class BaseFragment extends Fragment {
    private int mVisibleStatus = -1;

    public BaseFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVisibleStatus == -1) {
            mVisibleStatus = 1;
            fragmentVisibleChanged(true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVisibleStatus == 1) {
            mVisibleStatus = -1;
            fragmentVisibleChanged(false, true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mVisibleStatus = hidden ? 0 : 1;
        fragmentVisibleChanged(mVisibleStatus == 1, false);
    }


    protected void fragmentVisibleChanged(boolean visible, boolean fromLifecycle) {
        onFragmentVisibleChanged(visible, fromLifecycle);
    }

    protected void onFragmentVisibleChanged(boolean visible, boolean fromLifecycle) {
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle b = getArguments();
        if (b != null) {
            outState.putAll(b);
        }
    }
}
