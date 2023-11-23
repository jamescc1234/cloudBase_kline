package com.widget.stock;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

/**
 * Created by dingrui on 16/9/26.
 * 分时图、K线图父布局
 */

public class ChartLinearLayout extends LinearLayout {
    public ChartLinearLayout(Context context) {
        super(context);
        init();
    }

    public ChartLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChartLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setPadding(0, 0, 0, 0);
        setBackgroundColor(Color.TRANSPARENT);
        setDividerDrawable(null);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(VERTICAL);
    }
}
