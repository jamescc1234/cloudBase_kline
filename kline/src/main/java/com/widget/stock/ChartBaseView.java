package com.widget.stock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import binance.stock.library.R;

import com.widget.stock.utils.Parse;
import com.widget.stock.utils.Utils;


/**
 * Created by dingrui on 2016/10/17.
 * 绘图基类
 */

public abstract class ChartBaseView extends View {

    /**
     * 必备品
     */
    protected Utils utils = Utils.getInstance();
    protected StockUtils stockUtils = StockUtils.getInstance();
    protected Parse parse = Parse.getInstance();
    protected Paint mPaint = new Paint();
    protected TextPaint mTextPaint = new TextPaint();
    protected Path mPath = new Path();
    protected Bitmap mBitmap;
    protected BitmapDrawable mBitmapDrawable;
    protected Canvas mCanvas;

    /**
     * 数据相关
     */
    public static final int ALL = 0,
            BEGINNING = 1,
            MIDDLE = 2,
            END = 4;
    protected int scaleRule = ALL;
    protected boolean isDraw = false,// 是否正在绘制
            isDestroy = false;// 是否结束
    protected float topRate = 0.05f,// 绘图区域距离顶部比例
            bottomRate = topRate;// 绘图区域距离底部比例

    /**
     * 不可配置属性
     */
    protected final int lineWidth;// 默认线宽度
    /**
     * 字体属性
     */
    protected final int DEFAULT_TEXT_COLOR = 0xff333333;// 默认字体颜色
    protected int textColor = DEFAULT_TEXT_COLOR;// 字体颜色
    protected int textSize;// 字体大小
    protected boolean isShowText = true;// 是否显示刻度

    protected int decPlace = 2;// 小数位

    public ChartBaseView(Context context) {
        super(context);
        lineWidth = utils.dp2px(context, 0.5f);
        init(context, null);
    }

    public ChartBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        lineWidth = utils.dp2px(context, 0.5f);
        init(context, attrs);
    }

    public ChartBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lineWidth = utils.dp2px(context, 0.5f);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChartBaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        lineWidth = utils.dp2px(context, 0.5f);
        init(context, attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        mPaint.setAntiAlias(true);
        mTextPaint.setAntiAlias(true);
        textSize = utils.dp2px(context, 11.0f);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChartBaseView);
            textSize = ta.getDimensionPixelSize(R.styleable.ChartBaseView_android_textSize, textSize);
            textColor = ta.getColor(R.styleable.ChartBaseView_android_textColor, textColor);
            decPlace = ta.getInt(R.styleable.ChartBaseView_decPlace, decPlace);
            scaleRule = ta.getInt(R.styleable.ChartBaseView_scaleRule, ALL);
            topRate = ta.getFloat(R.styleable.ChartBaseView_topRate, 0.05f);
            if (topRate < 0.05f) {
                topRate = 0.05f;
            } else if (topRate > 0.3f) {
                topRate = 0.3f;
            }
            bottomRate = ta.getFloat(R.styleable.ChartBaseView_bottomRate, 0.05f);
            if (bottomRate < 0.05f) {
                bottomRate = 0.05f;
            } else if (bottomRate > 0.3f) {
                bottomRate = 0.3f;
            }
            isShowText = ta.getBoolean(R.styleable.ChartBaseView_isShowText, isShowText);
            int typeface = ta.getInteger(R.styleable.ChartBaseView_android_typeface, -1);
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

    /**
     * 判断子线程是否活动中
     *
     * @param thread
     * @return true：活动中；false：未活动
     */
    protected boolean isAlive(Thread thread) {
        if (thread == null) {
            return false;
        } else {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getDecPlace() {
        return decPlace;
    }

    /**
     * 设置小数位数
     *
     * @param decPlace
     */
    public ChartBaseView setDecPlace(int decPlace) {
        if(this.decPlace != decPlace){
            this.decPlace = decPlace;
        }
        return this;
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

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onDestroy();

    public abstract void build();
}
