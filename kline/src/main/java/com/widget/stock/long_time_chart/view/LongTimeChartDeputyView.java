package com.widget.stock.long_time_chart.view;

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
import com.widget.stock.long_time_chart.data.LongTimeDataChildList;
import com.widget.stock.long_time_chart.data.LongTimeDataGroupList;
import com.widget.stock.time_chart.view.TimeChartFrameLayout;


/**
 * Created by dingrui on 2016/10/11.
 * 多日分时副图
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LongTimeChartDeputyView extends ChartBaseView {

    private LongTimeChartFrameLayout mLongTimeChartFrameLayout;

    private LongTimeIndex mLongTimeIndex = LongTimeIndex.VOL;// 设置的指标
    private LongTimeIndex tagLti = LongTimeIndex.VOL;// 当前设置的指标

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
     */
    private LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList;// 数据
    private float width, height;// 宽、高
    private float scale;// 比例
    private int maxMinute;// 总共的时间
    private int howManyDays;// 多少日
    private float dayWidth;// 一日的长度
    private float dataSpacing;// 每条数据间隔距离
    private float dataWidth;// 每条数据宽度
    private float hig, low;// 最高、最低值

    /**
     * 指标
     */
    public enum LongTimeIndex {
        VOL("VOL");

        private String value;

        LongTimeIndex(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public LongTimeChartDeputyView(Context context) {
        super(context);
        init(context, null);
    }

    public LongTimeChartDeputyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LongTimeChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LongTimeChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LongTimeChartDeputyView);
            volUpColor = ta.getColor(R.styleable.LongTimeChartDeputyView_volUpColor, volUpColor);
            volLowColor = ta.getColor(R.styleable.LongTimeChartDeputyView_volLowColor, volLowColor);
            ta.recycle();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            setData(longTimeDataList);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() != (int) width || getHeight() != (int) height) {
            width = getWidth();
            height = getHeight();
            setData(longTimeDataList);
        }
    }

    /**
     * 绘制到Bitmap
     *
     * @param canvas
     */
    private void ondraw(Canvas canvas) {
        /**
         * 绘制指标
         */
        drawIndex(canvas);

        /**
         * 绘制左边刻度
         */
        drawText(canvas);
    }

    /**
     * 绘制指标
     *
     * @param canvas
     */
    private void drawIndex(Canvas canvas) {
        if (tagLti == LongTimeIndex.VOL) {
            drawVol(canvas);
        }
    }

    /**
     * 绘制成交量指标
     *
     * @param canvas
     */
    private void drawVol(Canvas canvas) {
        if (longTimeDataList == null || longTimeDataList.size() == 0) {
            return;
        }
        mPaint.setStrokeWidth(dataWidth);
        float preClose = (float) longTimeDataList.getPre();
        int color;
        float cjl, close, x;
        int count = longTimeDataList.size() - 1;
        float endPoint = width - mLongTimeChartFrameLayout.getStrokeWidth();
        float startX;
        int index = 0;
        for (int i = count; i >= count - howManyDays; i--) {
            if (i < 0 || index >= howManyDays) {
                break;
            }
            index++;
            startX = endPoint - index * dayWidth;
            LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> childList = longTimeDataList.get(i);
            if (childList == null || childList.size() == 0) {
                continue;
            }
            for (int j = 0;
                 j < childList.size() && j < maxMinute; j++) {
                cjl = (float) childList.get(j).getIndexVol().getVol();
                close = (float) childList.get(j).getClose();
                if (cjl <= 0) {
                    continue;
                }
                color = close >= preClose ? volUpColor : volLowColor;
                mPaint.setColor(color);
                preClose = close;
                x = startX + dataSpacing * j;
                canvas.drawLine(x, height, x, height
                                              - cjl * scale, mPaint);
            }

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
        float low = LongTimeChartDeputyView.this.low;
        float spacing = (hig - low) / 2;
        float y = height / 2;
        float strokeWidth2 = mLongTimeChartFrameLayout.getStrokeWidth() * 2.0f;

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
        setData(longTimeDataList);
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
        if (longTimeDataList != null) {
            longTimeDataList.clear();
        }
    }

    @Override
    public void build() {

    }

    /**
     * 获取最新设置的指标（可能还在计算中）
     *
     * @return 返回最新设置的指标
     */
    public LongTimeIndex getNewLongTimeIndex() {
        return mLongTimeIndex;
    }

    /**
     * 获取计算好的指标
     *
     * @return 返回计算好的指标
     */
    public LongTimeIndex getLongTimeIndex() {
        return tagLti;
    }

    /**
     * 设置指标
     *
     * @param mLongTimeIndex 多日分时指标
     */
    public void setLongTimeIndex(LongTimeIndex mLongTimeIndex) {
        if (this.mLongTimeIndex == mLongTimeIndex)
            return;
        this.mLongTimeIndex = mLongTimeIndex;
        if (mLongTimeChartFrameLayout != null) {
            mLongTimeChartFrameLayout.updateIndex();
        }
    }

    /**
     * 设置真实指标（仅限父布局调用）
     *
     * @param tagLti
     */
    void setTagLti(LongTimeIndex tagLti) {
        this.tagLti = tagLti;
    }

    /**
     * 设置根布局
     *
     * @param mLongTimeChartFrameLayout
     */
    public void setLongTimeChartFrameLayout(LongTimeChartFrameLayout mLongTimeChartFrameLayout) {
        this.mLongTimeChartFrameLayout = mLongTimeChartFrameLayout;
    }

    /**
     * 设置数据
     *
     * @param longTimeDataList
     */
    public void setData(LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList) {
        if(isDestroy){
            return;
        }
        this.longTimeDataList = longTimeDataList;
        if (getVisibility() == View.GONE || getVisibility() == View.INVISIBLE) {
            return;
        }
        if (mLongTimeChartFrameLayout != null) {
            howManyDays = mLongTimeChartFrameLayout.getHowManyDays();
            maxMinute = mLongTimeChartFrameLayout.getMaxMinute();
            dayWidth = mLongTimeChartFrameLayout.getDayWidth();
            dataSpacing = dayWidth
                          / (maxMinute - 1);
            dataWidth = (dayWidth) / maxMinute;
            if (dataWidth > lineWidth) {
                dataWidth = lineWidth;
            }
            if (longTimeDataList != null) {
                hig = (float) longTimeDataList.getIndexHig(tagLti.getValue());
                low = (float) longTimeDataList.getIndexLow(tagLti.getValue());
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
     * 清除数据
     */
    public void clear() {
        if (longTimeDataList != null) {
            longTimeDataList.clear();
        }
        setData(longTimeDataList);
    }
}
