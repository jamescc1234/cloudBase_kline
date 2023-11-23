package com.widget.stock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import binance.stock.library.R;

import com.kedll.stock.library.base.BaseColorsConfigure;
import com.widget.stock.k_line.configure.DefaultColorsConfigure;
import com.widget.stock.utils.Utils;
import com.widget.stock.utils.Parse;


/**
 * Created by dingrui on 2016/10/11.
 * 图形分时、K线基类
 */

public abstract class ChartBaseFrameLayout extends FrameLayout {

    protected static final int MSG_OK = 0x1,// 子线程处理完毕
            MSG_RESTART_INDEX_THREAD = 0x2,// 重启计算指标的子线程
            MSG_LONG_PRESS = 0x11;// 长按事件消息

    /**
     * 手势识别距离
     */
    protected final int TOUCH_SPACING;

    protected Utils utils = Utils.getInstance();
    protected Parse parse = Parse.getInstance();

    protected float width, height;// 控件宽、高
    protected TimeLocation mTimeLocation = TimeLocation.BOTOOM;// 时间位置，默认底部

    protected Paint mPaint = new Paint();// 图形画笔
    protected TextPaint mTextPaint = new TextPaint();// 文字画笔

    private final int DEFAULT_TIME_TEXT_COLOR = 0xff333333;// 默认时间字体颜色
    protected int timeTextColor = DEFAULT_TIME_TEXT_COLOR;// 时间字体颜色
    protected int timeTextSize;// 时间字体大小
    protected boolean isSupportEvent = true;// 是否支持手势

    /**
     * 不可配置属性相关
     */
    protected final int strokeWidth,// 边线宽
            lineWidth;// 经纬线宽

    /**
     * 可配置属性相关
     */
    private final int DEFAULT_STROKE_COLOR = 0xffdad8d8,// 默认边线色
            DEFAULT_LINE_COLOR = 0xffe7e5e5;// 默认经纬线颜色

    protected int strokeColor = DEFAULT_STROKE_COLOR,// 边框线颜色
            lineColor = DEFAULT_LINE_COLOR;// 经纬线颜色
    /**
     * 十字线相关属性
     */
    private final int DEFAULT_CROSS_COLOR = 0xff047bfb,// 默认十字线颜色
            DEFAULT_DOT_COLOR = DEFAULT_CROSS_COLOR;// 默认十字线中心小圆点颜色

    protected int crossColor = DEFAULT_CROSS_COLOR,// 十字线颜色
            dotColor = DEFAULT_DOT_COLOR;// 十字线中心小圆点颜色
    protected boolean isDrawDot = true;// 是否绘制十字线中心圆点
    protected boolean isDrawMainLine = true;// 是否绘制主图横线
    protected int lineWidthCross;// 十字光标线宽

    private BaseColorsConfigure colorsConfigure = new DefaultColorsConfigure();

    public ChartBaseFrameLayout(Context context) {
        super(context);
        strokeWidth = utils.dp2px(context, 0.5f);
        lineWidth = strokeWidth;
        TOUCH_SPACING = ViewConfiguration.get(context).getScaledEdgeSlop();
        init(context, null);
    }

    public ChartBaseFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        strokeWidth = utils.dp2px(context, 0.5f);
        lineWidth = strokeWidth;
        TOUCH_SPACING = ViewConfiguration.get(context).getScaledEdgeSlop();
        init(context, attrs);
    }

    public ChartBaseFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = utils.dp2px(context, 0.5f);
        lineWidth = strokeWidth;
        TOUCH_SPACING = ViewConfiguration.get(context).getScaledEdgeSlop();
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChartBaseFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        strokeWidth = utils.dp2px(context, 0.5f);
        lineWidth = strokeWidth;
        TOUCH_SPACING = ViewConfiguration.get(context).getScaledEdgeSlop();
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        mPaint.setAntiAlias(true);
        mTextPaint.setAntiAlias(true);

        timeTextSize = utils.dp2px(context, 11);
        lineWidthCross = utils.dp2px(context, 0.5f);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChartBaseFrameLayout);
            strokeColor = ta.getColor(R.styleable.ChartBaseFrameLayout_strokeColor, strokeColor);
            lineColor = ta.getColor(R.styleable.ChartBaseFrameLayout_lineColor, lineColor);
            timeTextColor = ta.getColor(R.styleable.ChartBaseFrameLayout_timeTextColor, timeTextColor);
            timeTextSize = ta.getDimensionPixelSize(R.styleable.ChartBaseFrameLayout_timeTextSize, timeTextSize);
            int tl = ta.getInt(R.styleable.ChartBaseFrameLayout_timeLocation, 0x1);
            if (tl == 0x2) {
                mTimeLocation = TimeLocation.NONE;
            } else if (tl == 0x0) {
                mTimeLocation = TimeLocation.CENTER;
            } else {
                mTimeLocation = TimeLocation.BOTOOM;
            }
            /* 十字线相关 */
            crossColor = ta.getColor(R.styleable.ChartBaseFrameLayout_crossColor, crossColor);
            dotColor = ta.getColor(R.styleable.ChartBaseFrameLayout_dotColor, dotColor);
            isDrawDot = ta.getBoolean(R.styleable.ChartBaseFrameLayout_isDrawDot, isDrawDot);
            isDrawMainLine = ta.getBoolean(R.styleable.ChartBaseFrameLayout_isDrawMainLine, isDrawMainLine);
            lineWidthCross = ta.getDimensionPixelSize(R.styleable.ChartBaseFrameLayout_lineWidthCross, lineWidthCross);
            /* 是否支持手势 */
            isSupportEvent = ta.getBoolean(R.styleable.ChartBaseFrameLayout_isSupportEvent, isSupportEvent);
            int typeface = ta.getInteger(R.styleable.ChartBaseFrameLayout_android_typeface, -1);
            switch (typeface) {
                case 1:
                    mTextPaint.setTypeface(Typeface.SANS_SERIF);
                    break;
                case 2:
                    mTextPaint.setTypeface(Typeface.SERIF);
                    break;
                case 3:
                    mTextPaint.setTypeface(Typeface.MONOSPACE);
                    break;
                case -1:
                default:
                    mTextPaint.setTypeface(null);
                    break;
            }
            ta.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        left = 0;
        top = 0;
        right = 0;
        bottom = 0;
        super.setPadding(left, top, right, bottom);
    }

    /**
     * 设置时间绘制位置
     *
     * @param timeLocation
     */
    public void setTimeLocation(TimeLocation timeLocation) {
        if (timeLocation == null) {
            return;
        }
        this.mTimeLocation = timeLocation;
        postInvalidate();
    }

    /**
     * 获取边线宽度
     *
     * @return 获取边框宽度
     */
    public int getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onPause();
    }

    public boolean isSupportEvent() {
        return isSupportEvent;
    }

    public void setSupportEvent(boolean supportEvent) {
        isSupportEvent = supportEvent;
    }

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();

    public abstract void build();

    public void setColorsConfigure(BaseColorsConfigure colorsConfigure) {
        this.colorsConfigure = colorsConfigure;
        build();
    }

    public BaseColorsConfigure getColorsConfigure() {
        return this.colorsConfigure;
    }
}
