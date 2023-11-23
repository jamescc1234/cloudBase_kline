package com.widget.stock.long_time_chart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import binance.stock.library.R;
import com.widget.stock.ChartBaseView;
import com.widget.stock.long_time_chart.data.LongTimeDataChildList;
import com.widget.stock.long_time_chart.data.LongTimeDataGroupList;
import com.widget.stock.time_chart.view.TimeChartFrameLayout;


/**
 * Created by dingrui on 2016/10/11.
 * 多日分时主图
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LongTimeChartMainView extends ChartBaseView {

    private LongTimeChartFrameLayout mLongTimeChartFrameLayout;

    /**
     * 可配置属性
     */
    /**
     * 文字颜色属性
     */
    private final int DEFAULT_UP_COLOR = 0xffe60012,// 默认涨的颜色
            DEFAULT_LOW_COLOR = 0xff009944;// 默认跌的颜色

    private int upColor = DEFAULT_UP_COLOR,// 涨的颜色
            flatColor,// 不涨不跌的颜色
            lowColor = DEFAULT_LOW_COLOR;// 跌的颜色

    /**
     * 图形颜色属性
     */
    private final int DEFAULT_TIME_LINE_COLOR = 0xff368bff,// 默认分时线颜色
            DEFAULT_AVERAGE_LINE_COLOR = 0xfffde01b;// 默认均线颜色

    private int timeLineColor = DEFAULT_TIME_LINE_COLOR,// 分时线颜色
            averageLineColor = DEFAULT_AVERAGE_LINE_COLOR;// 均线颜色

    /**
     * 数据相关
     */
    private LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList;// 数据
    private float width, height;// 宽、高
    private float scale;// 数据与图形比例
    private int maxMinute;// 总共的时间
    private int howManyDays;// 多少日
    private float dayWidth;// 一日的长度
    private float dataSpacing;// 每条数据间隔距离

    public LongTimeChartMainView(Context context) {
        super(context);
        init(context, null);
    }

    public LongTimeChartMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LongTimeChartMainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LongTimeChartMainView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        flatColor = textColor;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LongTimeChartMainView);
            upColor = ta.getColor(R.styleable.LongTimeChartMainView_textUpColor, upColor);
            lowColor = ta.getColor(R.styleable.LongTimeChartMainView_textLowColor, lowColor);
            flatColor = ta.getColor(R.styleable.LongTimeChartMainView_textFlatColor, flatColor);
            timeLineColor = ta.getColor(R.styleable.LongTimeChartMainView_timeLineColor, timeLineColor);
            averageLineColor = ta.getColor(R.styleable.LongTimeChartMainView_averageLineColor, averageLineColor);
            ta.recycle();
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
         * 绘制分时线
         */
        drawTimeLine(canvas);

        /**
         * 绘制均线
         */
        drawAverage(canvas);

        /**
         * 绘制刻度
         */
        drawText(canvas);
    }

    /**
     * 绘制分时线
     *
     * @param canvas
     */
    private void drawTimeLine(Canvas canvas) {
        if (longTimeDataList == null || longTimeDataList.size() == 0) {
            return;
        }
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(timeLineColor);
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
            if (childList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                float startWhiteX = startX;
                float startWhiteY = (float) (height -
                                             (childList.get(0).getClose() -
                                              longTimeDataList.getLow()) * scale);
                canvas.drawCircle(startWhiteX, startWhiteY, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                float endWhiteY, endWhiteX;
                for (int j = 0; j < childList.size() && j < maxMinute; j++) {
                    endWhiteY = (float) (height -
                                         (childList.get(j).getClose() -
                                          longTimeDataList.getLow()) *
                                         scale);
                    endWhiteX = startX + dataSpacing * j;
                    if (mPath.isEmpty())
                        mPath.moveTo(endWhiteX, endWhiteY);
                    else
                        mPath.lineTo(endWhiteX, endWhiteY);
                }
                canvas.drawPath(mPath, mPaint);
            }
        }
    }

    /**
     * 绘制均线
     *
     * @param canvas
     */
    private void drawAverage(Canvas canvas) {
        if (longTimeDataList == null || longTimeDataList.size() == 0) {
            return;
        }
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(averageLineColor);
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
            if (childList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                float startWhiteX = startX;
                float startWhiteY = (float) (height -
                                             (childList.get(0).getAverage() -
                                              longTimeDataList.getLow()) * scale);
                canvas.drawCircle(startWhiteX, startWhiteY, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                float endWhiteX, endWhiteY;
                for (int j = 0; j < childList.size() && j < maxMinute; j++) {
                    endWhiteY = (float) (height -
                                         (childList.get(j).getAverage() -
                                          longTimeDataList.getLow()) *
                                         scale);
                    endWhiteX = startX + dataSpacing * j;
                    if (mPath.isEmpty())
                        mPath.moveTo(endWhiteX, endWhiteY);
                    else
                        mPath.lineTo(endWhiteX, endWhiteY);
                }
                canvas.drawPath(mPath, mPaint);
            }
        }
    }

    /**
     * 绘制两边刻度
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        mTextPaint.setTextSize(textSize);
        float y = height / 4;
        double pre = 0;
        double hig = 0;
        double low = 0;
        double were = 0;
        float spacing = 0;
        float strokeWidth2 = mLongTimeChartFrameLayout.getStrokeWidth() * 2.0f;
        if (longTimeDataList != null) {
            pre = longTimeDataList.getPre();
            hig = longTimeDataList.getHig();
            low = longTimeDataList.getLow();
            were = (hig - pre) / pre * 100;
            spacing = (float) ((hig - low) / 4.0f);
        }
        String zdf;
        for (int i = 0; i < 5; i++) {
            zdf = stockUtils.parse2String(were) + "%";
            if (i == 0 || i == 1) {
                mTextPaint.setColor(upColor);
            } else if (i == 2) {
                mTextPaint.setColor(flatColor);
            } else {
                mTextPaint.setColor(lowColor);
            }
            float rightX = width - mTextPaint.measureText(zdf) - strokeWidth2;
            if (i == 0) {
                canvas.drawText(
                        stockUtils.parse2String(hig, decPlace),
                        strokeWidth2, textSize, mTextPaint);
                // 绘制右边刻度
                canvas.drawText(zdf,
                        rightX,
                        textSize, mTextPaint);
            } else if (i == 4) {
                canvas.drawText(
                        stockUtils.parse2String(low, decPlace),
                        strokeWidth2, y * i - 2, mTextPaint);
                // 绘制右边刻度
                canvas.drawText(zdf,
                        rightX,
                        y * i - 2, mTextPaint);
            } else {
                canvas.drawText(
                        stockUtils.parse2String(hig, decPlace),
                        strokeWidth2,
                        y * i + textSize / 2 - 2, mTextPaint);
                // 绘制右边刻度
                canvas.drawText(zdf,
                        rightX,
                        y * i + textSize / 2 - 2, mTextPaint);
            }
            hig -= spacing;
            were = ((hig - pre) / pre) * 100;
        }
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
        if (isDestroy) {
            return;
        }
        this.longTimeDataList = longTimeDataList;
        if (mLongTimeChartFrameLayout != null) {
            howManyDays = mLongTimeChartFrameLayout.getHowManyDays();
            maxMinute = mLongTimeChartFrameLayout.getMaxMinute();
            dayWidth = mLongTimeChartFrameLayout.getDayWidth();
            dataSpacing = dayWidth
                          / (maxMinute - 1);
            if (longTimeDataList != null) {
                scale = (float) (height /
                                 (longTimeDataList.getHig() - longTimeDataList.getLow()));
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
     * 获取每条数据间隔距离
     *
     * @return 返回数据间隔
     */
    public float getDataSpacing() {
        return dataSpacing;
    }

    /**
     * 获取数据与图形比例
     *
     * @return 返回数据与图形比例
     */
    public float getScale() {
        return scale;
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
