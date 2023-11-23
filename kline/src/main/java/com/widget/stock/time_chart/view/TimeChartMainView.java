package com.widget.stock.time_chart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.widget.stock.ChartBaseView;
import com.widget.stock.time_chart.data.TimeDataList;

import binance.stock.library.R;


/**
 * Created by dingrui on 16/9/22.
 * 分时主图
 */

public class TimeChartMainView extends ChartBaseView {

    private TimeChartFrameLayout mTimeChartFrameLayout;

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
    private boolean isDrawShader = false;// 是否绘制分时线的渐变效果

    private LinearGradient mLinearGradient;// 线性渐变

    /**
     * 数据相关
     */
    private TimeDataList<TimeChartFrameLayout.TimeDataValid> timeList;
    private float width, height;// 控件宽高
    private float scale;// 比例
    private int maxMinute;// 总共的时间
    private float dataSpacing;// 每条数据间隔距离
    private BitmapDrawable mBitmapDrawable;// 生成的图片
    private float minY;// 最小Y点

    public TimeChartMainView(Context context) {
        super(context);
        init(context, null);
    }

    public TimeChartMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimeChartMainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TimeChartMainView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeChartMainView);
            upColor = ta.getColor(R.styleable.TimeChartMainView_textUpColor, upColor);
            flatColor = ta.getColor(R.styleable.TimeChartMainView_textFlatColor, flatColor);
            lowColor = ta.getColor(R.styleable.TimeChartMainView_textLowColor, lowColor);
            timeLineColor = ta.getColor(R.styleable.TimeChartMainView_timeLineColor, timeLineColor);
            averageLineColor = ta.getColor(R.styleable.TimeChartMainView_averageLineColor, averageLineColor);
            isDrawShader = ta.getBoolean(R.styleable.TimeChartMainView_isDrawShader, isDrawShader);
            ta.recycle();
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
         * 绘制分时线
         */
        drawTimeLine(canvas);
        /**
         * 绘制均线
         */
        drawAverage(canvas);
        /**
         * 绘制两边刻度
         */
        drawText(canvas);
    }

    /**
     * 绘制分时线
     *
     * @param canvas
     */
    private void drawTimeLine(Canvas canvas) {
        if (timeList == null || timeList.size() == 0) {
            return;
        }
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(timeLineColor);
        if (timeList.size() == 1) {
            mPaint.setStyle(Paint.Style.FILL);
            float startWhiteX = mTimeChartFrameLayout.getStrokeWidth();
            float startWhiteY = (float) (height -
                    (timeList.get(0).getClose() - timeList.getLow()) *
                            scale);
            canvas.drawCircle(startWhiteX, startWhiteY, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            float endWhiteX = 0;
            float endWhiteY;
            float minY = height;
            for (int i = 0; i < timeList.size() && i < maxMinute; i++) {
                endWhiteY = (float) (height -
                        (timeList.get(i).getClose() - timeList.getLow()) *
                                scale);
                minY = minY < endWhiteY ? minY : endWhiteY;
                endWhiteX = dataSpacing * i + mTimeChartFrameLayout.getStrokeWidth();
                if (mPath.isEmpty())
                    mPath.moveTo(endWhiteX, endWhiteY);
                else
                    mPath.lineTo(endWhiteX, endWhiteY);
            }
            canvas.drawPath(mPath, mPaint);

            if (isDrawShader) {
                if (minY != this.minY || mLinearGradient == null) {
                    mLinearGradient = new LinearGradient(0, minY, 0, height, new int[]{
                            timeLineColor,
                            timeLineColor -
                                    0xbf000000,},
                            null, Shader.TileMode.CLAMP);
                }
                this.minY = minY;
                mPaint.setStyle(Paint.Style.FILL);
                mPath.lineTo(endWhiteX, height);
                mPath.lineTo(mTimeChartFrameLayout.getStrokeWidth(), height);
                mPath.close();
                mPaint.setShader(mLinearGradient);
                canvas.drawPath(mPath, mPaint);
                mPaint.setShader(null);
            }
        }
    }

    /**
     * 绘制均线
     *
     * @param canvas
     */
    private void drawAverage(Canvas canvas) {
        if (timeList == null || timeList.size() == 0) {
            return;
        }
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(averageLineColor);
        if (timeList.size() == 1) {
            mPaint.setStyle(Paint.Style.FILL);
            float startWhiteX = mTimeChartFrameLayout.getStrokeWidth();
            float startWhiteY = (float) (height -
                    (timeList.get(0).getAverage() - timeList.getLow()) *
                            scale);
            canvas.drawCircle(startWhiteX, startWhiteY, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            float endWhiteX;
            float endWhiteY;
            for (int i = 0; i < timeList.size() && i < maxMinute; i++) {
                endWhiteY = (float) (height -
                        (timeList.get(i).getAverage() - timeList.getLow()) *
                                scale);
                endWhiteX = dataSpacing * i + mTimeChartFrameLayout.getStrokeWidth();
                if (mPath.isEmpty())
                    mPath.moveTo(endWhiteX, endWhiteY);
                else
                    mPath.lineTo(endWhiteX, endWhiteY);
            }
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 绘制两边刻度文字
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
        float strokeWidth2 = mTimeChartFrameLayout.getStrokeWidth() * 2.0f;
        if (timeList != null) {
            pre = timeList.getPre();
            hig = timeList.getHig();
            low = timeList.getLow();
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
     * 设置根布局
     *
     * @param mTimeChartFrameLayout
     */
    public void setTimeChartFrameLayout(TimeChartFrameLayout mTimeChartFrameLayout) {
        this.mTimeChartFrameLayout = mTimeChartFrameLayout;
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
        if (mTimeChartFrameLayout != null) {
            maxMinute = mTimeChartFrameLayout.getMaxMinute();
            dataSpacing =
                    (width - mTimeChartFrameLayout.getStrokeWidth() * 2.0f) / (maxMinute - 1);
            if (timeList != null) {
                scale = (float) (height / (timeList.getHig() - timeList.getLow()));
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
     * 设置是否绘制分时线渐变色
     *
     * @param isDrawShader
     */
    public void setDrawShader(boolean isDrawShader) {
        this.isDrawShader = isDrawShader;
    }

    /**
     * 是否绘制分时线渐变色
     *
     * @return
     */
    public boolean isDrawShader() {
        return isDrawShader;
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

}
