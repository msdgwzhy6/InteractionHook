package com.rexy.example.extend;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rexy.widgets.layout.PageScrollView;

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

    public static TextView installIndicator(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        PageScrollView scrollView=new PageScrollView(activity);
        scrollView.setOrientation(PageScrollView.VERTICAL);
        TextView textView=new TextView(activity);
        textView.setBackgroundColor(0x22000000);
        textView.setTextColor(0x77ff0000);
        scrollView.addView(textView,-1,-2);
        scrollView.setMaxHeight(activity.getResources().getDisplayMetrics().heightPixels/3);
        decorView.addView(scrollView);
        return textView;
    }

}
