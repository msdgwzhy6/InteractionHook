package com.rexy.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.rexy.example.extend.BaseActivity;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-06-05 14:54
 */
public class ActivityCommon extends BaseActivity {
    public static String KEY_FRAGMENT_NAME;

    public static void launch(Context context, Class<? extends Fragment> fragment) {
        Intent t = new Intent(context, ActivityCommon.class);
        t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        t.putExtra(KEY_FRAGMENT_NAME, fragment.getName());
        context.startActivity(t);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fragmentName = getIntent().getStringExtra(KEY_FRAGMENT_NAME);
        if (!TextUtils.isEmpty(fragmentName)) {
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content,Fragment.instantiate(this,fragmentName,new Bundle()),"root");
            ft.commitAllowingStateLoss();
        }
    }
}
