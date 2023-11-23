package com.widget.stock.time_chart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import binance.stock.library.R;
import com.widget.stock.ChartBaseView;
import com.widget.stock.time_chart.data.TimeDataList;


/**
 * Created by dingrui on 16/9/23.
 * 分时副图
 */

public class TimeChartDeputyView extends ChartBaseView {

    private TimeIndex mTimeIndex = TimeIndex.VOL;
    private TimeIndex tagTi = TimeIndex.VOL;
    private TimeChartFrameLayout mTimeChartFrameLayout;


    /**
     * 可配置属性
     */
    /**
     * 图形颜色属性
     */
    private final int DEFAULT_VOL_UP_COLOR = 0xffeb4646,// 默认成交量涨的颜色
            DEFAULT_VOL_LOW_COLOR = 0xff1bd253;// 默认成交量跌的颜色

    private int volUpColor = DEFAULT_VOL_UP_COLOR,// 成交量涨的颜色
            volLowColor = DEFAULT_VOL_LOW_COLOR;// 成交量跌的颜色

    /**
     * 数据相关
     * @param context
     */
    /**
     * 数据相关
     */
    private TimeDataList<TimeChartFrameLayout.TimeDataValid> timeList;
    private float width, height;// 控件宽高
    private float scale;// 比例
    private int maxMinute;// 总共的时间
    private float dataSpacing;// 每条数据间隔距离
    private float dataWidth;// 每条数据的线宽
    private float hig, low;// 最高、最低值

    public TimeChartDeputyView(Context context) {
        super(context);
        init(context, null);
    }

    public TimeChartDeputyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimeChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TimeChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeChartDeputyView);
            volUpColor = ta.getColor(R.styleable.TimeChartDeputyView_volUpColor, volUpColor);
            volLowColor = ta.getColor(R.styleable.TimeChartDeputyView_volLowColor, volLowColor);
            ta.recycle();
        }
    }

    /**
     * 指标
     */
    public enum TimeIndex {

        VOL("VOL");

        private String value;

        TimeIndex(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            setData(this.timeList);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTimeChartFrameLayout == null)
            return;
        if (getWidth() != (int) width || getHeight() != (int) height) {
            width = getWidth();
            height = getHeight();
            setData(timeList);
        }
    }

    /**
     * 绘制
     *
     * @param canvas
     */
    private void ondraw(Canvas canvas) {
        /**
         * 绘制指标
         */
        drawIndex(canvas);
        /**
         * 绘制刻度
         */
        drawText(canvas);
    }

    /**
     * 绘制指标
     *
     * @param canvas
     */
    private void drawIndex(Canvas canvas) {
        if (tagTi == TimeIndex.VOL) {
            drawVol(canvas);
        }
    }

    /**
     * 绘制成交量指标
     *
     * @param canvas
     */
    private void drawVol(Canvas canvas) {
        if (timeList == null)
            return;
        mPaint.setStrokeWidth(dataWidth);
        float preClose = (float) timeList.getPre();
        int color;
        float cjl, close, x;
        for (int i = 0; i < timeList.size() && i < maxMinute; i++) {
            cjl = (float) timeList.get(i).getIndexVol().getVol();
            close = (float) timeList.get(i).getClose();
            if (cjl <= 0) {
                continue;
            }
            color = close >= preClose ? volUpColor : volLowColor;
            mPaint.setColor(color);
            preClose = close;
            x = dataSpacing * i + mTimeChartFrameLayout.getStrokeWidth();
            canvas.drawLine(x, height, x, height
                                          - cjl * scale, mPaint);
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        float low = TimeChartDeputyView.this.low;
        float spacing = (hig - low) / 2;
        float y = height / 2;
        float strokeWidth2 = mTimeChartFrameLayout.getStrokeWidth() * 2.0f;

        for (int i = 0; i < 3; i++) {
            if (i == 2) {
                canvas.drawText(
                        stockUtils.parse2CNStringVol(hig, 2), strokeWidth2, textSize, mTextPaint);
            } else if (i == 0) {
                canvas.drawText(stockUtils.parse2CNStringVol(low, 2),
                        strokeWidth2,
                        height - 2, mTextPaint);
            } else {
                canvas.drawText(
                        stockUtils.parse2CNStringVol(low, 2,
                                false), strokeWidth2, height - y * i + textSize / 2
                        , mTextPaint);
            }
            low += spacing;
        }
    }

    @Override
    public void onResume() {
        isDestroy = false;
        setData(timeList);
    }

    @Override
    public void onPause() {
        isDestroy = true;
        if (isDraw) {
            onPause();
            return;
        }
        if (mBitmap != null) {
            mBitmapDrawable = null;
            mCanvas = null;
            mBitmap.recycle();
            mBitmap = null;
            setBackgroundDrawable(mBitmapDrawable);
        }
    }

    @Override
    public void onDestroy() {
        isDestroy = true;
        if (timeList != null) {
            timeList.clear();
        }
    }

    @Override
    public void build() {

    }

    /**
     * 设置数据
     *
     * @param timeList
     */
    public void setData(TimeDataList<TimeChartFrameLayout.TimeDataValid> timeList) {
        if (isDestroy) {
            return;
        }
        this.timeList = timeList;
        if (getVisibility() == View.GONE || getVisibility() == View.INVISIBLE) {
            return;
        }
        if (mTimeChartFrameLayout != null) {
            maxMinute = mTimeChartFrameLayout.getMaxMinute();
            dataSpacing =
                    (width - mTimeChartFrameLayout.getStrokeWidth() * 2.0f) / (maxMinute - 1);
            dataWidth = (width - mTimeChartFrameLayout.getStrokeWidth() * 2.0f) / maxMinute;
            if (dataWidth > lineWidth) {
                dataWidth = lineWidth;
            }
            if (timeList != null) {
                hig = (float) timeList.getIndexHig(tagTi.getValue());
                low = (float) timeList.getIndexLow(tagTi.getValue());
                scale = height / (hig - low);
            } else {
                scale = 0.0f;
            }
            if (getWidth() > 0 && getHeight() > 0) {
                if (mBitmap != null) {
                    mBitmapDrawable = null;
                    mCanvas = null;
                    mBitmap.recycle();
                    mBitmap = null;
                }
                isDraw = true;
                mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                setBackgroundDrawable(mBitmapDrawable = new BitmapDrawable(mBitmap));
                mCanvas = new Canvas(mBitmap);
                ondraw(mCanvas);
                isDraw = false;
            }
        }
    }

    /**
     * 清除图层
     */
    public void clear() {
        if (this.timeList != null) {
            this.timeList.clear();
        }
        setData(this.timeList);
    }

    /**
     * 获取最新设置的指标（可能还在计算中）
     *
     * @return 返回最新设置的指标
     */
    public TimeIndex getNewTimeIndex() {
        return mTimeIndex;
    }

    /**
     * 设置当前的指标
     *
     * @param mTimeIndex 分时指标
     */
    public void setTimeIndex(TimeIndex mTimeIndex) {
        if (this.mTimeIndex == mTimeIndex) {
            return;
        }
        this.mTimeIndex = mTimeIndex;
        if (mTimeChartFrameLayout != null)
            mTimeChartFrameLayout.updateIndex();
    }

    /**
     * 获取计算好的指标
     *
     * @return 返回计算好的指标
     */
    public TimeIndex getTimeIndex() {
        return tagTi;
    }

    /**
     * 设置计算好的指标（仅限父布局调用）
     *
     * @param tagTi
     */
    void setTagTi(TimeIndex tagTi) {
        this.tagTi = tagTi;
    }

    /**
     * 设置根布局
     *
     * @param mTimeChartFrameLayout
     */
    public void setTimeChartFrameLayout(TimeChartFrameLayout mTimeChartFrameLayout) {
        this.mTimeChartFrameLayout = mTimeChartFrameLayout;
    }

}
