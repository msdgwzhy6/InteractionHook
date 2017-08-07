package com.rexy.example.extend;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class ExpandCollapseAnimation extends Animation {
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 4;
    public static final int BOTTOM = 8;
    private View mAnimatedView;
    private int mEndHeight;
    private int mEndWidth;
    private int mGravity;
    private int mType;
    public static final int COLLAPSE = 1;
    public static final int EXPAND = 0;
    private ViewGroup.MarginLayoutParams mLayoutParams;

    /**
     * Initializes expand collapse animation, has two types, collapse (1) and
     * expand (0).
     *
     * @param view The view to animate
     * @param type The type of animation: 0 will expand from gone and 0 size to
     * visible and layout size defined in xml. 1 will collapse view
     * and set to gone
     */
    public ExpandCollapseAnimation(View view, int type, int gravity) {
        mGravity = gravity;
        if ((mGravity & (TOP | BOTTOM | LEFT | RIGHT)) == 0) {
            mGravity = BOTTOM;
        }
        mAnimatedView = view;
        mEndHeight = mAnimatedView.getMeasuredHeight();
        mEndWidth = mAnimatedView.getMeasuredWidth();
        mLayoutParams = ((ViewGroup.MarginLayoutParams) view.getLayoutParams());
        mType = type;
        if (mEndHeight <= 0 || mEndWidth <= 0) {
            int[] display = new int[]{0, 0};
            ViewUtils.measureView(mAnimatedView, 0, 0, display);
            mEndWidth = display[0];
            mEndHeight = display[1];
        }
        if (mType == EXPAND) {
            adjustMarge(-mEndHeight, -mEndWidth);
        } else {
            adjustMarge(0, 0);
        }
        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            int vertical = 0, horizonal = 0;
            if (mType == EXPAND) {
                if ((mGravity & (TOP | BOTTOM)) != 0) {
                    vertical = -mEndHeight + (int) (mEndHeight * interpolatedTime);
                }
                if ((mGravity & (LEFT | RIGHT)) != 0) {
                    horizonal = -mEndWidth + (int) (mEndWidth * interpolatedTime);
                }
            } else {
                if ((mGravity & (TOP | BOTTOM)) != 0) {
                    vertical = -(int) (mEndHeight * interpolatedTime);
                }
                if ((mGravity & (LEFT | RIGHT)) != 0) {
                    horizonal = -(int) (mEndWidth * interpolatedTime);
                }
            }
            adjustMarge(vertical, horizonal);
            mAnimatedView.requestLayout();
        } else {
            if (mType == EXPAND) {
                adjustMarge(0, 0);
                mAnimatedView.requestLayout();
            } else {
                adjustMarge(-mEndHeight, -mEndWidth);
                mAnimatedView.setVisibility(View.GONE);
                mAnimatedView.requestLayout();
            }
        }
    }

    private void adjustMarge(int vertical, int horizonal) {
        if ((mGravity & BOTTOM) != 0) {
            mLayoutParams.bottomMargin = vertical;
        } else if ((mGravity & TOP) != 0) {
            mLayoutParams.topMargin = vertical;
        }
        if ((mGravity & LEFT) != 0) {
            mLayoutParams.leftMargin = horizonal;
        } else if ((mGravity & RIGHT) != 0) {
            mLayoutParams.rightMargin = horizonal;
        }
    }

    public static void animateView(final View target, final int type, int duration, int gravity,
                                   AnimationListener l) {
        if (duration < 0) {
            duration = 500;
        }
        Animation anim = new ExpandCollapseAnimation(target, type, gravity);
        anim.setDuration(duration);
        if (l != null) {
            anim.setAnimationListener(l);
        }
        target.startAnimation(anim);
    }
}
