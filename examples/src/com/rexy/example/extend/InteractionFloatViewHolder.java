package com.rexy.example.extend;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.rexy.hook.interfaces.IHandleResult;
import com.rexy.interactionhook.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-08-07 09:50
 */
public class InteractionFloatViewHolder extends FloatViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static String sEmpty = "";
    private static InteractionFloatViewHolder sHolder;

    public static InteractionFloatViewHolder getInstance(Activity activity) {
        if (sHolder == null || sHolder.isDestroy()) {
            synchronized (InteractionFloatViewHolder.class) {
                if (sHolder == null || sHolder.isDestroy()) {
                    sHolder = new InteractionFloatViewHolder(LayoutInflater.from(activity).inflate(R.layout.float_view_interaction, null, false)
                            , (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE));
                }
            }
        }
        return sHolder;
    }


    TextView mTextTitle;
    TextView mTextMessage;
    View mIcon;
    View mLayoutOption;
    boolean mExpanded;
    List<IHandleResult> mResults = new ArrayList(16);

    private List<String> mAccepts = new ArrayList(8);

    public InteractionFloatViewHolder(View rootView, WindowManager windowManager) {
        super(rootView, windowManager);
        mTextTitle = ViewUtils.view(rootView, R.id.title);
        mTextMessage = ViewUtils.view(rootView, R.id.message);
        mLayoutOption = ViewUtils.view(rootView, R.id.expanded_menu);
        mIcon = ViewUtils.view(rootView, R.id.icon);
        mLayoutOption.setVisibility(View.GONE);
        mIcon.setOnClickListener(this);
        ViewUtils.view(rootView, R.id.clean).setOnClickListener(this);
        ViewUtils.view(rootView, R.id.close).setOnClickListener(this);
        initToggleButton(rootView, R.id.toogleInput);
        initToggleButton(rootView, R.id.toogleProxyClick);
        initToggleButton(rootView, R.id.tooglePreventClick);
        initToggleButton(rootView, R.id.toogleGesture);
        initToggleButton(rootView, R.id.toogleFocus);
        initToggleButton(rootView, R.id.toogleError);
    }

    private void initToggleButton(View rootView, int rid) {
        ToggleButton button = ViewUtils.view(rootView, rid);
        onCheckedChanged(button, button.isChecked());
        button.setOnCheckedChangeListener(this);
    }

    @Override
    protected View getTouchDragView() {
        return getRootView() == null ? null : getRootView().findViewById(R.id.titleHeader);
    }

    protected void startAnimWhenStateChanged(boolean toReversed, int duration) {
        int from = toReversed ? 0 : 180;
        int to = toReversed ? 180 : 360;
        RotateAnimation animation = new RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        mIcon.clearAnimation();
        mIcon.startAnimation(animation);
    }

    public void toggleExpand() {
        int duration = 200;
        int gravity = Gravity.BOTTOM;
        int expandType = mExpanded ? ExpandCollapseAnimation.COLLAPSE : ExpandCollapseAnimation.EXPAND;
        ExpandCollapseAnimation.animateView(mLayoutOption, expandType, duration, gravity, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mExpanded = !mExpanded;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimWhenStateChanged(!mExpanded, duration);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.icon) {
            toggleExpand();
        }
        if (id == R.id.close) {
            destroy();
        }
        if (id == R.id.clean) {
            mTextMessage.setText(sEmpty);
            mResults.clear();
        }
    }

    private void updateLoggerAccept(ToggleButton button, boolean isChecked) {
        if (button.getTag() instanceof String) {
            String tag = (String) button.getTag();
            boolean contained = mAccepts.contains(tag);
            if (isChecked) {
                if (!contained) {
                    mAccepts.add(tag);
                }
            } else {
                if (contained) {
                    mAccepts.remove(tag);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String r : mAccepts) {
            sb.append(r).append(",");
        }
        if (sb.length() == 0) {
            sb.append("All Type");
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        mTextTitle.setText(sb);
    }

    private IHandleResult acceptLogger(IHandleResult result) {
        if (mAccepts.isEmpty()) {
            return result;
        }
        String tag = result.getTag();
        for (String r : mAccepts) {
            if (TextUtils.equals(tag, r)) {
                return result;
            }
        }
        return null;
    }

    private void appendLogger(IHandleResult result) {
        mTextMessage.append(result.getTag());
        mTextMessage.append(":");
        mTextMessage.append(result.toShortString(null));
        mTextMessage.append("\n\n");
    }


    public void recordResult(IHandleResult result) {
        mResults.add(result);
        if (acceptLogger(result) != null) {
            appendLogger(result);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView instanceof ToggleButton) {
            updateLoggerAccept((ToggleButton) buttonView, isChecked);
        }
        if (mResults.size() > 0) {
            mTextMessage.setText(sEmpty);
            for (IHandleResult r : mResults) {
                if (acceptLogger(r) != null) {
                    appendLogger(r);
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mAccepts.clear();
        mResults.clear();
    }
}
