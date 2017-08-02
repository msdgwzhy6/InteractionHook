package com.rexy.example;

import android.os.Bundle;
import android.view.View;

import com.rexy.example.extend.BaseActivity;
import com.rexy.example.extend.ViewUtils;
import com.rexy.interactionhook.example.R;

/**
 * Created by rexy on 17/4/11.
 */
public class ActivityMain extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.view(this,R.id.button1).setOnClickListener(this);
        ViewUtils.view(this,R.id.button2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
    }
}
