package com.widget.stock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;


import androidx.annotation.RequiresApi;

import com.widget.stock.k_line.view.KlineLinearLayout;
import com.widget.stock.utils.Utils;

import java.util.List;

/**
 * Created by dingrui on 16/9/29.
 * 十字光标的View
 */

public class ChartCrossView extends View {

    private Utils utils = Utils.getInstance();
    private List<View> chartList;
    private ChartBaseFrameLayout mChartBaseFrameLayout;// 主控根布局

    /**
     * 图形画笔
     */
    private Paint mPaint = new Paint();

    /**
     * 不可配置属性
     */
    private int lineWidth;// 十字线宽
    private int dotRadius;// 十字线中心圆点半径
    private int bigDotRadius;// 十字线中心大圆点半径

    /**
     * 可配置属性相关
     */
    private final int BIG_GAP = 0x77000000;// 大圆、与小圆的颜色差距
    private int crossColor,// 十字线颜色
            dotColor,// 十字线中心小圆点颜色
            dotBigColor;// 十字线中心大圆点颜色

    private boolean isDrawDot = true;// 是否绘制十字线中心圆点

    private boolean isDrawMainLine = true;// 是否绘制主图横线

    /**
     * 数据相关
     */
    private float drawX, drawY;// x、y点
    private float strokeWidth;// 边线宽
    //    private TimeDataList<TimeChartFrameLayout.TimeDataValid> timeList;
    private float width, height;// 控件宽高


    public ChartCrossView(Context context) {
        super(context);
        init(context);
    }

    public ChartCrossView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChartCrossView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChartCrossView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(getParent() instanceof ChartBaseFrameLayout)) {
            throw new IllegalStateException("父布局必须是ChartBaseFrameLayout");
        }
        height = getHeight();
        width = getWidth();

        /**
         * 绘制十字线
         */
        drawCross(canvas);

    }

    /**
     * 绘制十字线
     *
     * @param canvas
     */
    private void drawCross(Canvas canvas) {
        mPaint.setColor(crossColor);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect();
        mChartBaseFrameLayout.getHitRect(rect);


        // 绘制竖线
//        canvas.drawLine(drawX, rect.top, drawX, rect.bottom, mPaint);
        KlineLinearLayout layout = null;
        for (int i = 0; i < chartList.size(); i++) {
            View view = chartList.get(i);

            if (view instanceof KlineLinearLayout) {
                layout = (KlineLinearLayout) view;
            }

            if (view.getVisibility() != View.VISIBLE) {
                continue;
            }
            Rect mRect = new Rect();
            view.getHitRect(mRect);
            canvas.drawLine(drawX, mRect.top, drawX, mRect.bottom, mPaint);
        }

        if (isDrawMainLine) {
            // 绘制横线
            if (layout != null) {
                Rect layoutRect = new Rect();
                layout.getHitRect(layoutRect);

                if (drawY < layoutRect.top || drawY > layoutRect.bottom) {
                    canvas.drawLine(0, drawY, width, drawY, mPaint);
                }
            } else {
                canvas.drawLine(0, drawY, width, drawY, mPaint);
            }
        }

        if (isDrawDot) {
            // 绘制中心点
            mPaint.setColor(dotBigColor);
            canvas.drawCircle(drawX, drawY, bigDotRadius, mPaint);
            mPaint.setColor(dotColor);
            canvas.drawCircle(drawX, drawY, dotRadius, mPaint);
        }
    }

    /**
     * 设置根布局
     *
     * @param mChartBaseFrameLayout
     */
    public void setTimeChartFrameLayout(ChartBaseFrameLayout mChartBaseFrameLayout) {
        this.mChartBaseFrameLayout = mChartBaseFrameLayout;
        if (this.mChartBaseFrameLayout != null)
            strokeWidth = this.mChartBaseFrameLayout.getStrokeWidth();
    }

    /**
     * 设置主图、副图集合
     *
     * @param chartList
     */
    public void setChartList(List<View> chartList) {
        this.chartList = chartList;
    }

    /**
     * 设置十字线颜色
     *
     * @param crossColor
     */
    public void setCrossColor(int crossColor) {
        this.crossColor = crossColor;
        postInvalidate();
    }

    /**
     * 设置小圆点颜色
     *
     * @param dotColor
     */
    public void setDotColor(int dotColor) {
        this.dotColor = dotColor;
        this.dotBigColor = dotColor - BIG_GAP;
        postInvalidate();
    }

    /**
     * 设置是否绘制十字线中心圆点
     *
     * @param isDrawDot
     */
    public void setDrawDot(boolean isDrawDot) {
        this.isDrawDot = isDrawDot;
    }

    /**
     * 设置当前绘制点
     *
     * @param drawX
     * @param drawY
     */
    public void setDrawLocation(float drawX, float drawY) {
        this.drawX = drawX;
        this.drawY = drawY;
        postInvalidate();
    }

    /**
     * 是否绘制主图横线
     *
     * @return
     */
    public boolean isDrawMainLine() {
        return isDrawMainLine;
    }

    /**
     * 设置是否绘制主图横线
     *
     * @param drawMainLine
     */
    public void setDrawMainLine(boolean drawMainLine) {
        isDrawMainLine = drawMainLine;
    }

    public void setLineWidth(int lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;
            int dotR = utils.dp2px(getContext(), 3f);
            dotRadius = (int) (lineWidth * 1.5f);
            if (dotRadius < dotR) {
                dotRadius = dotR;
            }
            bigDotRadius = dotRadius * 2 - dotRadius / 2;
        }
    }
}
