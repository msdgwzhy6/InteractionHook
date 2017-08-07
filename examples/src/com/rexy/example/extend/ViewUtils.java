package com.rexy.example.extend;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {
    public static <T extends View> T view(Activity aty, int id) {
        if (aty != null) {
            return (T) aty.findViewById(id);
        }
        return null;
    }

    public static <T extends View> T view(Fragment frag, int id) {
        if (frag != null && frag.getView() != null) {
            return (T) frag.getView().findViewById(id);
        }
        return null;
    }

    public static <T extends View> T view(View container, int id) {
        if (container != null) {
            return (T) container.findViewById(id);
        }
        return null;
    }

    /**
     * @param child
     * @param maxWidth unknown for 0.
     * @param maxHeight unknown for 0.
     * @param result accept the width and height ,clound not be null.
     * @return int[] allow to be null.
     * @throws
     */
    public static void measureView(View child, int maxWidth, int maxHeight, int[] result) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(-2, -2);
        }
        int heightSpec;// = ViewGroup.getChildMeasureSpec(0, 0, p.height);
        int widthSpec;
        if (p.width > 0) {// exactly size
            widthSpec = View.MeasureSpec.makeMeasureSpec(p.width, View.MeasureSpec.EXACTLY);
        } else if (p.width == -2 || maxWidth <= 0) {// wrapcontent
            widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        } else if (p.width == -1) {
            widthSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.EXACTLY);
        } else {// fillparent
            widthSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST);
        }
        if (p.height > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(p.height, View.MeasureSpec.EXACTLY);
        } else if (p.height == -2 || maxHeight <= 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        } else if (p.height == -1) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.EXACTLY);
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
        }
        child.measure(widthSpec, heightSpec);
        result[0] = child.getMeasuredWidth();
        result[1] = child.getMeasuredHeight();
    }
}
