package com.rexy.example.extend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;


/**
 * TODO:功能说明
 *
 * @author: rexy
 */
public class FadeTextButton extends TextView {
    private static final String TAG = FadeTextButton.class.getSimpleName();
    private static final int FLAG_TOUCH = 1;
    private static final int FLAG_ANIM_NOW = 2;
    private static final int FLAG_FLADE_FLOAT = 4;
    private static final int FLAG_FADE_ENABLE = 8;
    private static final int FLAG_WILL_STOP = 16;
    private static final int FLAG_LAST_FADE = 32;
    private static final int FLAG_FADE_EXCEPT_STATELIST = 64;

    private float mPressAlphaTo = 0.75f;
    private float mCurrentAlpha = 1;
    private int mFadeDuration = 250;
    private int mTouchSlop = 0;
    private Drawable mFloatDrawable = null;

    int mFlag = FLAG_FLADE_FLOAT | FLAG_FADE_ENABLE;

    public FadeTextButton(Context context) {
        super(context);
        initInner(context);
    }

    public FadeTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInner(context);
    }

    public FadeTextButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initInner(context);
    }

    private void initInner(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setFadeFloat(boolean fadeFloat) {
        if (fadeFloat) {
            mFlag |= FLAG_FLADE_FLOAT;
        } else {
            mFlag |= (~FLAG_FLADE_FLOAT);
        }
    }

    public void setPressAlphaTo(float alpha) {
        mPressAlphaTo = Math.min(Math.max(alpha, 0), 1);
    }

    public void setPressFadeAble(boolean fadeAble, boolean exceptSelectDrawable) {
        if (fadeAble) {
            mFlag |= FLAG_FADE_ENABLE;
        } else {
            mFlag |= (~FLAG_FADE_ENABLE);
        }
        if (exceptSelectDrawable) {
            mFlag |= FLAG_FADE_EXCEPT_STATELIST;
        } else {
            mFlag |= (~FLAG_FADE_EXCEPT_STATELIST);
        }
    }

    public void setFadeDuration(int duration) {
        mFadeDuration = duration;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int flag1 = FLAG_FADE_ENABLE | FLAG_FLADE_FLOAT;
        int flag2 = FLAG_TOUCH | FLAG_ANIM_NOW | FLAG_WILL_STOP;
        if (flag1 == (flag1 & mFlag) && 0 != (flag2 & mFlag)) {
            Drawable fadeDrawable = getFadeDrawable();
            fadeDrawable.setBounds(0, 0, getWidth(), getHeight());
            fadeDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (FLAG_FADE_ENABLE == (FLAG_FADE_ENABLE & mFlag) && !(FLAG_FADE_EXCEPT_STATELIST == (mFlag & FLAG_FADE_EXCEPT_STATELIST) && (getBackground() instanceof StateListDrawable))) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mFlag |= FLAG_TOUCH;
                startFadeAnim();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mFlag |= (~FLAG_TOUCH);
                stopFadeAnim();
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (FLAG_TOUCH == (FLAG_TOUCH & mFlag)) {
                    if (!pointInView(event.getX(), event.getY(), mTouchSlop)) {
                        mFlag |= (~FLAG_TOUCH);
                        stopFadeAnim();
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean pointInView(float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((getRight() - getLeft()) + slop) &&
                localY < ((getBottom() - getTop()) + slop);
    }

    private void startFadeAnim() {
        Animation anim = new FadeAnimation(1f, mPressAlphaTo);
        anim.setDuration(mFadeDuration);
        startAnimation(anim);
    }

    private void stopFadeAnim() {
        mFlag |= FLAG_WILL_STOP;
        if (FLAG_ANIM_NOW == (FLAG_ANIM_NOW & mFlag)) {
            clearAnimation();
        }
        Animation anim = new FadeAnimation(mCurrentAlpha, 1f);
        anim.setDuration((int) (mFadeDuration * 0.8f));
        startAnimation(anim);
    }


    private Drawable getFadeDrawable() {
        if (mFloatDrawable == null) {
            mFloatDrawable = new ColorDrawable(0xff000000);
        }
        return mFloatDrawable;
    }


    private class FadeAnimation extends Animation {
        private float mFromAlpha;
        private float mToAlpha;

        public FadeAnimation(float fromAlpha, float toAlpha) {
            mFromAlpha = fromAlpha;
            mToAlpha = toAlpha;
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mFlag |= FLAG_ANIM_NOW;
                    if (FLAG_TOUCH == (FLAG_TOUCH & mFlag)) {
                        mFlag |= FLAG_LAST_FADE;
                    } else {
                        mFlag |= (~FLAG_LAST_FADE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mFlag |= (~FLAG_ANIM_NOW);
                    if (FLAG_WILL_STOP == (FLAG_WILL_STOP & mFlag) && 0 == (FLAG_LAST_FADE & mFlag)) {
                        mFlag |= (~FLAG_WILL_STOP);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mCurrentAlpha = mFromAlpha + ((mToAlpha - mFromAlpha) * interpolatedTime);
            if (FLAG_FLADE_FLOAT == (FLAG_FLADE_FLOAT & mFlag)) {
                Drawable drawable = getFadeDrawable();
                drawable.setAlpha((int) (255 * (1 - mCurrentAlpha)));
                invalidate();
            } else {
                mCurrentAlpha = mCurrentAlpha * mCurrentAlpha;
                setAlpha(mCurrentAlpha);
            }
        }
    }
}
