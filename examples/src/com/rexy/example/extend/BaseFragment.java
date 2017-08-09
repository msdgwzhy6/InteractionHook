package com.rexy.example.extend;

import android.support.v4.app.Fragment;

import com.rexy.hook.InteractionHook;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-08-09 14:34
 */
public class BaseFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
        InteractionHook.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        InteractionHook.onPause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        InteractionHook.onDestroy(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        InteractionHook.onHiddenChanged(this, hidden);
    }
}
