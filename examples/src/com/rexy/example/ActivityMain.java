package com.rexy.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.rexy.example.extend.BaseActivity;
import com.rexy.example.extend.MyApplication;
import com.rexy.example.extend.ViewUtils;
import com.rexy.hook.interfaces.IHandleListener;
import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.hook.interfaces.IHookHandler;
import com.rexy.interactionhook.example.R;

/**
 * Created by rexy on 17/4/11.
 */
public class ActivityMain extends BaseActivity implements View.OnClickListener, IHandleListener {
    TextView mTextIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.view(this,R.id.button1).setOnClickListener(this);
        ViewUtils.view(this,R.id.button2).setOnClickListener(this);
        mTextIndicator=ViewUtils.installIndicator(this);
        ((MyApplication) getApplication()).registerHandleListener(this);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MyApplication) getApplication()).unregisterHandleListener(this);
    }

    @Override
    public boolean onReceiveHandleError(IHookHandler handler, Throwable error, String category) {
        mTextIndicator.append(String.format("error:%s",error.getLocalizedMessage()));
        mTextIndicator.append("\n\n");
        return false;
    }

    @Override
    public boolean onReceiveHandleResult(IHookHandler handler, IHandleResult result) {
        mTextIndicator.append(String.format("%s:%s",handler.getTag(),result.toShortString(null)));
        mTextIndicator.append("\n\n");
        return false;
    }
}
