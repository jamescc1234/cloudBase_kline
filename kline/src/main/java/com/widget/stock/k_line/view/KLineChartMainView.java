package com.widget.stock.k_line.view;

import static com.widget.stock.k_line.configure.KlineConstants.KEY_KLINE_ORDER_HISTORY;
import static com.widget.stock.k_line.configure.KlineConstants.KEY_KLINE_ZOOM_TO_LINE;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import androidx.core.content.ContextCompat;
import com.contants.KlineConfig;
import com.drawing.DrawingData;
import com.drawing.DrawingPoint;
import com.utils.LocalStore;
import com.widget.stock.ChartBaseView;
import com.widget.stock.StockUtils;
import com.widget.stock.k_line.KLineSetUtils;
import com.widget.stock.k_line.data.KLineDataValid;
import com.widget.stock.k_line.data.KLineSetModelConfig;
import com.widget.stock.k_line.data.Offset;
import com.widget.stock.k_line.data.TradeInfo;
import com.widget.stock.utils.Parse;
import com.widget.stock.utils.SaveObject;
import com.widget.stock.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import binance.stock.library.R;


/**
 * Created by dingrui on 2016/10/24.
 * K线主图
 */
public class KLineChartMainView extends RelativeLayout {

    private KLineChartFrameLayout mKLineChartFrameLayout;// 根布局
    private KLineMainIndex mKLineMainIndex = KLineMainIndex.MA,// 预设指标
            tagKLI = mKLineMainIndex;// 计算好的指标

    /**
     * 数据相关
     */
    private ArrayList<KLineDataValid> kLineDataList;// K线数据
    private float width, height;// 控件宽高
    private float kLineWidth;// 当前K线宽度(像素单位)
    private int zoomToLineWidth;
    private float kLineSpacing;// (像素单位)
    private int maxKLineLength;// 界面总共需要显示的条数
    private int location;// 当前滚动位置
    private float kLineXCenter;// 当前K线宽度的中间值
    private double preMaxData = Double.NaN;// 预设当前最大数据
    private double preMinData = Double.NaN;// 预设当前最小数据
    private double maxData = 0;// 当前最大数据
    private double minData = 0;// 当前最小数据
    private double curCloseData = 0;// 当前close数据
    private float scale;// 比例

    private boolean isDraw = false,// 是否正在绘制
            isDestroy = false;// 是否结束
    private float topRate = 0.05f,// 绘图区域距离顶部比例
            bottomRate = topRate;// 绘图区域距离底部比例

    private int highPosition,// K线最高点
            lowPosition;// K线最低点

    private double symbolExrate = 1;// 币种汇率

    private int scaleRule = ChartBaseView.ALL;// 刻度绘制规则
    private List<TradeInfo> tradeInfoList = new ArrayList<>(); // 成交历史数据
    private long earliestTradeTime = -1; // 最早一笔成交时间
    public Map<Long, Pair<TradeInfo, TradeInfo>> tradeInfoMap = new HashMap<>(); // 当前显示在K线的buySell标记

    /**
     * 必备品
     */
    private Utils utils = Utils.getInstance();
    private StockUtils stockUtils = StockUtils.getInstance();
    private Parse parse = Parse.getInstance();

    /**
     * 绘制相关
     */
    private Paint mPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();
    private Path mPath = new Path();
    private Bitmap mBitmap;
    private BitmapDrawable mBitmapDrawable;
    private Canvas mCanvas;
    private LinearGradient mLinearGradient;// 线性渐变
    private Path path1, path2;
    private Bitmap buyIcon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_kline_trade_buy);
    private Bitmap sellIcon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_kline_trade_sell);
    private Paint buySellPaint = new Paint();

    private float minY;// 最小Y点

    private int lineWidth;// 线宽度
    private RectF curCloseRect; // 当前close矩形框
    /**
     * 字体属性
     */
    private final int DEFAULT_TEXT_COLOR = 0xff333333;// 默认字体颜色
    private int textColor = DEFAULT_TEXT_COLOR;// 字体颜色
    private int textSize;// 字体大小
    private boolean isShowText = true,// 是否显示刻度
            isShowHighAndLowData = true;// 是否显示最高最低数据
    private int decPlace = 2;// 保留小数位数

    /**
     * 图形属性
     */
    private final int DEFAULT_ANODE_COLOR = 0xffeb4646,// 阳线默认颜色
            DEFAULT_CATHODE_COLOR = 0xff1bd253,// 阴线默认颜色
            DEFAULT_FLAT_COLOR = 0xfffddd00;// 默认不涨不跌的颜色

    private int anodeColor = DEFAULT_ANODE_COLOR,// 阳线颜色
            cathodeColor = DEFAULT_CATHODE_COLOR,// 阴线颜色
            flatColor = DEFAULT_FLAT_COLOR,  // 不涨不跌的颜色
            timeLineColor = DEFAULT_FLAT_COLOR; // 分时线颜色
    private int shaderColor = flatColor;// 渐变色
    private boolean isAnodeStroke = false,// 阳线是否边框柱形图，否则实心柱形图
            isCathodeStroke = false;// 阴线是否边框柱形图，否则实心柱形图

    /**
     * 指标颜色相关
     */
    private int[] indexColors;// 当前指标颜色
    private Map<String, int[]> indexParameter,// 指标参数
            tagIndexParameter;// 计算之后指标参数
    private final String PARAMETER_FILEPATH = ".KLINE",// K线参数缓存目录
            PARAMETER_FILE = ".mainChart",// K线主图参数缓存文件名
            VERSION = "1.0.3";// 缓存版本（修改指标必须更改版本）
    private File paramenterFile;// K线主图参数文件路径

    private String gameBuyText, gameSellText;
    private int gameTextSize;

    private DashPathEffect mDashPathEffect;
    private DrawListener drawListener;

    public float getTopRate() {
        return topRate;
    }

    public float getBottomRate() {
        return bottomRate;
    }

    /**
     * 自定义指标
     */
    public KLineSetModelConfig configMA = KLineSetUtils.INSTANCE.getConfig("MA",false);
    public KLineSetModelConfig configEMA = KLineSetUtils.INSTANCE.getConfig("EMA",false);
    public KLineSetModelConfig configBOLL = KLineSetUtils.INSTANCE.getConfig("BOLL",false);

    public void setCathodeColor(int cathodeColor) {
        this.cathodeColor = cathodeColor;
    }

    public void setAnodeColor(int anodeColor) {
        this.anodeColor = anodeColor;
    }
    /**
     * 主图指标
     */
    public enum KLineMainIndex implements Serializable {
        NONE("NONE"),// 不绘制任何指标
        TIME("TIME"),// 分时线
        MA("MA", 7, 30, 20, 30, 60, 120, 1, 1, 0, 0, 0, 0),// 前面6个指标参数，后面6个指标开关
        BOLL("BOLL", 20, 2),
        RETURN_RATE("收益率"),
        SAR("SAR", 4),
        GAME("GAME", 5, 10, 20, 30, 60, 120, 1, 1, 0, 0, 0, 0),// 前面6个指标参数，后面6个指标开关
        BOLL_POINT("BOLL", 20, 2),// 布林支撑、压力位
        EMA("EMA", 7, 25, 99, 30, 60, 120, 1, 1, 1, 0, 0, 0);// 前面6个指标参数，后面6个指标开关

        public static KLineMainIndex[] MAIN_INDEX_S = {MA, BOLL, TIME, RETURN_RATE, SAR,
                GAME, BOLL_POINT, EMA};

        private String value;
        private int[] parameter;// 指标参数

        KLineMainIndex(String value, int... parameter) {
            this.value = value;
            this.parameter = parameter;
        }

        public String getValue() {
            return value;
        }

        public void setParameter(int[] parameter) {
            this.parameter = parameter;
        }

        public int[] getParameter() {
            return parameter;
        }
    }

    public KLineChartMainView(Context context) {
        super(context);
        init(context, null);
    }

    public KLineChartMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KLineChartMainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        if (indexParameter == null) {
            indexParameter = new HashMap<>();
            for (int i = 0; i < KLineMainIndex.MAIN_INDEX_S.length; i++) {
                String name = KLineMainIndex.MAIN_INDEX_S[i].getValue();
                if (name.equals("MA")) {
                    indexParameter.put("MA", convertArray(KLineSetUtils.INSTANCE.getParams(configMA)));
                } else if (name.equals("EMA")) {
                    indexParameter.put("EMA", convertArray(KLineSetUtils.INSTANCE.getParams(configEMA)));
                }else if (name.equals("BOLL")) {
                    indexParameter.put("BOLL", convertArray(KLineSetUtils.INSTANCE.getParams(configBOLL)));
                }else {
                    indexParameter.put(KLineMainIndex.MAIN_INDEX_S[i].getValue(),
                            KLineMainIndex.MAIN_INDEX_S[i].getParameter());
                }
            }
        }
        tagIndexParameter = indexParameter;
        setWillNotDraw(false);
        mPaint.setAntiAlias(true);
        mTextPaint.setAntiAlias(true);
        textSize = utils.dp2px(context, 11f);
        gameTextSize = utils.dp2px(context, 10f);
        lineWidth = utils.dp2px(context, 0.5f);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KLineChartMainView);
            lineWidth = ta.getDimensionPixelSize(R.styleable.KLineChartMainView_lineWidth, lineWidth);
            isAnodeStroke = ta.getBoolean(R.styleable.KLineChartMainView_anodeStroke, isAnodeStroke);
            isCathodeStroke = ta.getBoolean(R.styleable.KLineChartMainView_cathodeStroke, isCathodeStroke);
            anodeColor = ta.getColor(R.styleable.KLineChartMainView_anodeColor, anodeColor);
            cathodeColor = ta.getColor(R.styleable.KLineChartMainView_cathodeColor, cathodeColor);
            flatColor = ta.getColor(R.styleable.KLineChartMainView_flatColor, flatColor);
            timeLineColor = ta.getColor(R.styleable.KLineChartMainView_timeColor, timeLineColor);
            shaderColor = ta.getColor(R.styleable.KLineChartMainView_shaderColor, shaderColor);
            textColor = ta.getColor(R.styleable.KLineChartMainView_android_textColor, textColor);
            textSize = ta.getDimensionPixelSize(R.styleable.KLineChartMainView_android_textSize, textSize);
            decPlace = ta.getInt(R.styleable.KLineChartMainView_decPlace, decPlace);
            int index = ta.getInt(R.styleable.KLineChartMainView_indexMain, 0);
            KLineMainIndex kLineMainIndex;
            if (index < 0) {
                kLineMainIndex = KLineMainIndex.NONE;
            } else {
                kLineMainIndex = KLineMainIndex.MA;
            }
            setKLineMainIndex(kLineMainIndex);
            scaleRule = ta.getInt(R.styleable.KLineChartMainView_scaleRule, ChartBaseView.ALL);
            topRate = ta.getFloat(R.styleable.KLineChartMainView_topRate, 0.05f);
            if (topRate < 0.05f) {
                topRate = 0.05f;
            } else if (topRate > 0.3f) {
                topRate = 0.3f;
            }
            bottomRate = ta.getFloat(R.styleable.KLineChartMainView_bottomRate, 0.05f);
            if (bottomRate < 0.05f) {
                bottomRate = 0.05f;
            } else if (bottomRate > 0.3f) {
                bottomRate = 0.3f;
            }

            isShowText = ta.getBoolean(R.styleable.KLineChartMainView_isShowText, isShowText);
            isShowHighAndLowData = ta.getBoolean(R.styleable.KLineChartMainView_isShowHighAndLowData, isShowHighAndLowData);

            gameBuyText = ta.getString(R.styleable.KLineChartMainView_gameBuyText);
            gameSellText = ta.getString(R.styleable.KLineChartMainView_gameSellText);
            int typeface = ta.getInteger(R.styleable.KLineChartMainView_android_typeface, -1);
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
        mDashPathEffect = new DashPathEffect(new float[]{lineWidth * 5f,
                lineWidth * 2f}, 0);

        if (gameBuyText == null) {
            gameBuyText = "Buy";
        }
        if (gameSellText == null) {
            gameSellText = "Sell";
        }
        setKLineMainIndex(mKLineMainIndex);
    }

    public static int[] convertArray(Integer[] array) {
        int[] result = new int[array.length];
        for(int i =0;i<array.length;i++) {
            result[i] = array[i];
        }
        return result;
    }

    public void onResume() {
        isDestroy = false;
        setData(kLineDataList);
    }

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

    public void onDestroy() {
        isDestroy = true;
        if (kLineDataList != null) {
            kLineDataList.clear();
        }
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((int) width != getWidth() ||
                (int) height != getHeight()) {
            width = getWidth();
            height = getHeight();

            getDrawingRect(mKLineChartFrameLayout.mainViewOffsetViewBounds);
            Log.d("KlineChartMainView", "onDraw: top is " + mKLineChartFrameLayout.getTop() +
                    ", bottom is " + mKLineChartFrameLayout.getBottom() +
                    ", left is " + mKLineChartFrameLayout.getLeft() +
                    ", right is " + mKLineChartFrameLayout.getRight());

            if (kLineDataList != null && !kLineDataList.isEmpty()) {
                setData(kLineDataList);
            }
//            setData(this.kLineDataList);
        }
    }

    public double getTimeRange() {
        if (kLineDataList != null && !kLineDataList.isEmpty()) {
            if (kLineDataList.size() > 2) {
                double unit = kLineWidth + kLineSpacing;
                double unitTime = kLineDataList.get(1).getTime() - kLineDataList.get(0).getTime();
                return width / unit * unitTime;
            }
        }

        return 0;
    }

    private double getTimeUnit() {
        if (kLineDataList != null && !kLineDataList.isEmpty()) {
            if (kLineDataList.size() > 2) {
                double unitTime = kLineDataList.get(1).getTime() - kLineDataList.get(0).getTime();
                return unitTime;
            }
        }

        return 0;
    }

    /**
     * 绘制
     *
     * @param canvas
     */
    private void ondraw(Canvas canvas) {
        boolean isDrawKline = false;
        if (tagKLI == KLineMainIndex.TIME) {
            drawTime(canvas);

            if (kLineDataList != null && !kLineDataList.isEmpty()) {
                mKLineChartFrameLayout.getDrawingComponent().setKlineDataList(kLineDataList);
                mKLineChartFrameLayout.getDrawingComponent().setUnit(kLineWidth + kLineSpacing);
                mKLineChartFrameLayout.getDrawingComponent().drawCustomPath(canvas, kLineDataList, mKLineChartFrameLayout.getDrawingComponent().getTimeRange(), mKLineChartFrameLayout.getDrawingComponent().getTimeUnit(),  maxData, minData);
            }
        } else if (tagKLI == KLineMainIndex.RETURN_RATE) {
            mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStrokeCap(Paint.Cap.BUTT);
            drawReturnRate(canvas);
        } else {
            mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStrokeCap(Paint.Cap.BUTT);
            boolean zoomToLine = LocalStore.getInstance().getBoolean(KEY_KLINE_ZOOM_TO_LINE, false);
            /**
             * 绘制蜡烛线
             */
            if (zoomToLine) {
                if (kLineWidth > zoomToLineWidth) {
                    isDrawKline = true;
                    drawKLine(canvas);
                } else {
                    drawTime(canvas);
                }
            } else {
                isDrawKline = true;
                drawKLine(canvas);
            }

            /**
             * 绘制指标
             */
            drawIndex(canvas);
            /**
             * 绘制画图
             */
//            drawCustomPath(canvas);

            if (kLineDataList != null && !kLineDataList.isEmpty()) {
                mKLineChartFrameLayout.getDrawingComponent().setKlineDataList(kLineDataList);
                mKLineChartFrameLayout.getDrawingComponent().setUnit(kLineWidth + kLineSpacing);
                mKLineChartFrameLayout.getDrawingComponent().drawCustomPath(canvas, kLineDataList, mKLineChartFrameLayout.getDrawingComponent().getTimeRange(), mKLineChartFrameLayout.getDrawingComponent().getTimeUnit(),  maxData, minData);
            }

//            Rect rect = new Rect();
//            getGlobalVisibleRect(rect);
//            mKLineChartFrameLayout.offsetDescendantRectToMyCoords(this, rect);
//            Log.d("getRealPosition", "onDraw: " + width + " " + height);
//            Log.d("getRealPosition", "onDraw: " + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
            /**
             * 绘制点
             */
//            drawPoint(canvas);
        }
        /**
         * 绘制刻度
         */

        if (!isDrawKline) {
            drawText(canvas);
        }
    }

    /**
     * 绘制分时线
     *
     * @param canvas
     */
    private void drawTime(Canvas canvas) {
        Log.d("drawTime", "klineWidth is " + kLineWidth);
        if (kLineDataList == null || kLineDataList.size() == 0)
            return;
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
//        mPaint.setColor(flatColor);
        mPaint.setColor(timeLineColor);
        mPaint.setStrokeWidth(lineWidth);

        //连接的外边缘以圆弧的方式相交
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        //线条结束处绘制一个半圆
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        float Y, startX = 0, xCenter = 0;
        int count = kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        float hX = 0, hY = 0;
        float lX = 0, lY = 0;
        String strH = "", strL = "";
        if (kLineDataList.size() == 1) {
            mPaint.setStyle(Paint.Style.FILL);
            KLineDataValid entity = kLineDataList.get(0);
            Y = (float) (height - (entity.getClose() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (0 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
            if (isShowHighAndLowData) {
                hY = Y - textSize / 2f;
                String h = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2String(entity.getClose() * symbolExrate, false, decPlace)
                        : stockUtils.parse2String(entity.getClose() * symbolExrate, decPlace, false);
                strH = h;
                float w = mTextPaint.measureText(strH);
                hX = xCenter - w / 2f;
                if (hX < mKLineChartFrameLayout.getStrokeWidth()) {
                    hX = mKLineChartFrameLayout.getStrokeWidth();
                } else if (hX + w > width - mKLineChartFrameLayout.getStrokeWidth()) {
                    hX = width - mKLineChartFrameLayout.getStrokeWidth() - w;
                }
                lY = Y + textSize + textSize / 2f;
                String l = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2String(entity.getClose() * symbolExrate, false, decPlace)
                        : stockUtils.parse2String(entity.getClose() * symbolExrate, decPlace, false);
                strL = l;
                w = mTextPaint.measureText(strL);
                lX = xCenter - w / 2f;
                if (lX < mKLineChartFrameLayout.getStrokeWidth()) {
                    lX = mKLineChartFrameLayout.getStrokeWidth();
                } else if (lX + w > width - mKLineChartFrameLayout.getStrokeWidth()) {
                    lX = width - mKLineChartFrameLayout.getStrokeWidth() - w;
                }
            }
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            float minY = 0;
            float w;
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }

                if (index >= kLineDataList.size()) {
                    break;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getClose() - minData) * scale);
                minY = Y < minY ? Y : minY;
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
                if (isShowHighAndLowData) {
                    if (index == highPosition) {
                        hY = Y - textSize / 2f;
                        String h = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2USString(entity.getClose() * symbolExrate, false, decPlace)
                                : stockUtils.parse2String(entity.getClose() * symbolExrate, decPlace, false);
                        strH = h;
                        w = mTextPaint.measureText(strH);
                        hX = xCenter - w / 2f;
                        if (hX < mKLineChartFrameLayout.getStrokeWidth()) {
                            hX = mKLineChartFrameLayout.getStrokeWidth();
                        } else if (hX + w > width - mKLineChartFrameLayout.getStrokeWidth()) {
                            hX = width - mKLineChartFrameLayout.getStrokeWidth() - w;
                        }
                    }
                    if (index == lowPosition) {
                        lY = Y + textSize + textSize / 2f;
                        String l = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2USString(entity.getClose() * symbolExrate, false, decPlace)
                                : stockUtils.parse2String(entity.getClose() * symbolExrate, decPlace, false);
                        strL = l;
                        w = mTextPaint.measureText(strL);
                        lX = xCenter - w / 2f;
                        if (lX < mKLineChartFrameLayout.getStrokeWidth()) {
                            lX = mKLineChartFrameLayout.getStrokeWidth();
                        } else if (lX + w > width - mKLineChartFrameLayout.getStrokeWidth()) {
                            lX = width - mKLineChartFrameLayout.getStrokeWidth() - w;
                        }
                    }
                }
            }
            canvas.drawPath(mPath, mPaint);
            int shaderColor = 0;
            if (minY != this.minY || mLinearGradient == null || shaderColor != this.shaderColor) {
                shaderColor = this.shaderColor;
                mLinearGradient = new LinearGradient(0, minY, 0, height, new int[]{
                        shaderColor,
                        0,},
                        null, Shader.TileMode.CLAMP);
            }
            this.minY = minY;
            mPaint.setStyle(Paint.Style.FILL);
            mPath.lineTo(xCenter, height);
            mPath.lineTo(mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth / 2f), height);
            mPath.close();
            mPaint.setShader(mLinearGradient);
            canvas.drawPath(mPath, mPaint);
            mPaint.setShader(null);

            if (isShowHighAndLowData) {
                if (!TextUtils.isEmpty(strH)) {
                    canvas.drawText(strH, hX, hY, mTextPaint);
                }
                if (!TextUtils.isEmpty(strL)) {
                    canvas.drawText(strL, lX, lY, mTextPaint);
                }
            }
        }
    }

    /**
     * 绘制收益率指标
     *
     * @param canvas
     */
    private void drawReturnRate(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        mPaint.setColor(flatColor);
        mPaint.setStrokeWidth(lineWidth);
        float Y, startX = 0, xCenter = 0;
        int count = kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;
        if (kLineDataList.size() == 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(0).getReturnRate().getRate() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (0 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            float minY = 0;
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }
                if (index >= kLineDataList.size()) {
                    break;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getReturnRate().getRate() - minData) * scale);
                minY = Y < minY ? Y : minY;
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
            if (minY != this.minY || mLinearGradient == null) {
                mLinearGradient = new LinearGradient(0, minY, 0, height, new int[]{
                        flatColor,
                        flatColor -
                                0xbf000000,},
                        null, Shader.TileMode.CLAMP);
            }
            this.minY = minY;
            mPaint.setStyle(Paint.Style.FILL);
            mPath.lineTo(xCenter, height);
            mPath.lineTo(mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth / 2f), height);
            mPath.close();
            mPaint.setShader(mLinearGradient);
            canvas.drawPath(mPath, mPaint);
            mPaint.setShader(null);
        }
    }

    /**
     * 绘制蜡烛线
     *
     * @param canvas
     */
    private void drawKLine(Canvas canvas) {
        Log.d("testDebug", "come to drawKLine");
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        mPaint.setStrokeWidth(lineWidth);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        float startX;
        float endX;
        float xCenter;
        float openY, closeY, higY, lowY;
//        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                : maxKLineLength;
        int count = maxKLineLength;
        int index;

        float hX = 0, hY = 0;
        float lX = 0, lY = 0;
        float curCloseX = 0, curCloseY = 0;
        String strH = null, strL = null;

        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index < 0) {
                continue;
            }

            if (index >= kLineDataList.size()) {
                break;
            }
            KLineDataValid entity = kLineDataList.get(index);
            KLineDataValid oldEntity = kLineDataList.get((index - 1) < 0 ? 0 : (index - 1));

            // 柱形图
            openY = (float) (height
                    - (entity.getOpen() - minData) * scale);
            closeY = height
                    - (float) (entity.getClose() - minData) * scale;
            // 中线
            higY = height
                    - (float) (entity.getHig() - minData) * scale;
            lowY = height
                    - (float) (entity.getLow() - minData) * scale;

            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing)
                    * i;
            endX = startX + kLineWidth;
            if (i == count - 1) {
                curCloseX = endX;
            }

            xCenter = startX + kLineXCenter;

            if (isShowHighAndLowData) {
                if (index == highPosition) {
                    hY = higY + textSize / 2f;
                    String h = KlineConfig.SHOW_US_PRICE ?
                            stockUtils.parse2USString(entity.getHig() * symbolExrate, false, decPlace)
                            : stockUtils.parse2String(entity.getHig() * symbolExrate, decPlace, false);
                    if (xCenter < getWidth() / 2f) {
                        strH = "← " + h;
                        hX = xCenter;
                    } else {
                        strH = h + " →";
                        float w = mTextPaint.measureText(strH);
                        hX = xCenter - w;
                    }
                }
                if (index == lowPosition) {
                    lY = lowY + textSize / 2f;
                    String l = KlineConfig.SHOW_US_PRICE ?
                            stockUtils.parse2USString(entity.getLow() * symbolExrate, false, decPlace)
                            : stockUtils.parse2String(entity.getLow() * symbolExrate, decPlace, false);
                    if (xCenter < getWidth() / 2f) {
                        strL = "← " + l;
                        lX = xCenter;
                    } else {
                        strL = l + " →";
                        float w = mTextPaint.measureText(strL);
                        lX = xCenter - w;
                    }
                }
            }

            if (entity.getClose() < entity.getOpen()) {
                mPaint.setStyle(isCathodeStroke ? Paint.Style.STROKE : Paint.Style.FILL);
                mPaint.setColor(cathodeColor);
                if (closeY - openY > lineWidth) {
                    canvas.drawRect(startX, openY, endX, closeY, mPaint);
                } else {
                    canvas.drawLine(startX, closeY, endX, closeY, mPaint);
                }
                canvas.drawLine(xCenter, higY,
                        xCenter, closeY, mPaint);
                canvas.drawLine(xCenter, lowY,
                        xCenter, openY, mPaint);
            } else if (entity.getClose() > entity.getOpen()) {
                mPaint.setStyle(isAnodeStroke ? Paint.Style.STROKE : Paint.Style.FILL);
                mPaint.setColor(anodeColor);
                if (openY - closeY > lineWidth) {
                    canvas.drawRect(startX, closeY, endX, openY, mPaint);
                } else {
                    canvas.drawLine(startX, openY, endX, openY, mPaint);
                }
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawLine(xCenter, higY,
                        xCenter, closeY, mPaint);
                canvas.drawLine(xCenter, lowY,
                        xCenter, openY, mPaint);
            } else {
                if (entity.getClose() < oldEntity.getClose()) {
                    mPaint.setStyle(isCathodeStroke ? Paint.Style.STROKE : Paint.Style.FILL);
                    mPaint.setColor(cathodeColor);
                    if (closeY - openY > lineWidth) {
                        canvas.drawRect(startX, openY, endX, closeY, mPaint);
                    } else {
                        canvas.drawLine(startX, closeY, endX, closeY, mPaint);
                    }
                    canvas.drawLine(xCenter, higY,
                            xCenter, closeY, mPaint);
                    canvas.drawLine(xCenter, lowY,
                            xCenter, openY, mPaint);
                } else if (entity.getClose() > oldEntity.getClose()) {
                    mPaint.setStyle(isAnodeStroke ? Paint.Style.STROKE : Paint.Style.FILL);
                    mPaint.setColor(anodeColor);
                    if (openY - closeY > lineWidth) {
                        canvas.drawRect(startX, closeY, endX, openY, mPaint);
                    } else {
                        canvas.drawLine(startX, openY, endX, openY, mPaint);
                    }
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawLine(xCenter, higY,
                            xCenter, closeY, mPaint);
                    canvas.drawLine(xCenter, lowY,
                            xCenter, openY, mPaint);
                } else {
                    mPaint.setColor(flatColor);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawLine(startX, openY, endX, openY, mPaint);
                    canvas.drawLine(xCenter, higY,
                            xCenter, lowY, mPaint);
                }
            }

            // buy和sell标记
            Boolean orderHostory = LocalStore.getInstance().getBoolean(KEY_KLINE_ORDER_HISTORY, false);
            if (mKLineChartFrameLayout.orientation == Configuration.ORIENTATION_PORTRAIT && orderHostory) {
                Pair<TradeInfo, TradeInfo> pair = tradeInfoMap.get(entity.getTime());
                if (pair != null) {
                    TradeInfo buyInfo = pair.first;
                    TradeInfo sellInfo = pair.second;
                    if (buyInfo != null) {
                        canvas.drawBitmap(buyIcon, xCenter-utils.dp2px(getContext(), 5f),lowY+utils.dp2px(getContext(), 2f), buySellPaint);
                    }
                    if (sellInfo != null) {
                        canvas.drawBitmap(sellIcon, xCenter-utils.dp2px(getContext(), 5f),higY-utils.dp2px(getContext(), 16f), buySellPaint);
                    }
                }
            }
        }

        // 绘制刻度，要在价格线的前面
        drawText(canvas);

        // 在非画图模式下，绘制当前价格虚线
        if (!mKLineChartFrameLayout.drawingStatus.getDrawingMode()) {
            // 当前close价格虚线
            KLineDataValid lastEntity = kLineDataList.get(kLineDataList.size()-1);
            curCloseY = height - (float) (lastEntity.getClose() - minData) * scale;
            if (curCloseY <= 0) {
                curCloseY = 36;
            }
            if (curCloseY >= height) {
                curCloseY = height - 20;
            }
            // K线最右侧的位置
            float rightScreenPostion = 0;
            // 带箭头的当前价格右间距补偿
            float curPriceRectOffset = 0;
            if (mKLineChartFrameLayout.orientation == Configuration.ORIENTATION_PORTRAIT) {
                rightScreenPostion = mKLineChartFrameLayout.getWidth() - kLineWidth;
                curPriceRectOffset = getContext().getResources().getDimensionPixelSize(R.dimen.x138);
            } else {
                rightScreenPostion = mKLineChartFrameLayout.getWidth() - kLineWidth - getContext().getResources().getDimensionPixelSize(R.dimen.x203);
                curPriceRectOffset = getContext().getResources().getDimensionPixelSize(R.dimen.x341);
            }
            if (curCloseX < rightScreenPostion) {
                curCloseRect = null;
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new DashPathEffect(new float[] {12, 4}, 0));
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.Color_TextLink));
                canvas.drawLine(curCloseX, curCloseY, mKLineChartFrameLayout.getWidth(), curCloseY, mPaint);
                String closeStr = String.format(Locale.ENGLISH, "%." + decPlace + "f", lastEntity.getClose());
                float w = mTextPaint.measureText(closeStr);
                float h = textSize;
                mPaint.setPathEffect(null);
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.Cus_Color_Yellow));
                mPaint.setStyle(Paint.Style.FILL);
                float rectPadding = 6;
                RectF rect = new RectF((int)(mKLineChartFrameLayout.getWidth() - w - rectPadding*2), (int)(curCloseY - h/2 - rectPadding), (int)(mKLineChartFrameLayout.getWidth()), (int)(curCloseY + h/2 + rectPadding));
                canvas.drawRoundRect(rect, 10, 10, mPaint);
                mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.Color_PrimaryYellow));
                canvas.drawText(closeStr,mKLineChartFrameLayout.getWidth() - w - rectPadding, curCloseY + h/3, mTextPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new DashPathEffect(new float[] {12, 4}, 0));
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.Color_TextLink));
                canvas.drawLine(0, curCloseY, mKLineChartFrameLayout.getWidth(), curCloseY, mPaint);
//                String closeStr = String.valueOf(lastEntity.getClose()).concat(" >");
//                String closeStr = lastEntity.getClose() + " >";
                String closeStr = String.format(Locale.ENGLISH, "%." + decPlace + "f", lastEntity.getClose()).concat(" >");
                float w = mTextPaint.measureText(closeStr);
                float h = textSize;
                mPaint.setPathEffect(null);
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.Cus_Color_Yellow));
                mPaint.setStyle(Paint.Style.FILL);
                float rectPadding = 6;
                curCloseRect = new RectF((int)(mKLineChartFrameLayout.getWidth() - w - curPriceRectOffset - rectPadding), (int)(curCloseY - h/2 - rectPadding),(int)(mKLineChartFrameLayout.getWidth() - curPriceRectOffset + rectPadding), (int)(curCloseY + h/2 + rectPadding));
                canvas.drawRoundRect(curCloseRect, 10, 10,mPaint);
                mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.Color_PrimaryYellow));
                canvas.drawText(closeStr,mKLineChartFrameLayout.getWidth() - w - curPriceRectOffset, curCloseY + h/3, mTextPaint);
            }
            mPaint.setPathEffect(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mTextPaint.setColor(textColor);
        }

        if (isShowHighAndLowData) {
            if (!TextUtils.isEmpty(strH)) {
                canvas.drawText(strH, hX, hY, mTextPaint);
            }
            if (!TextUtils.isEmpty(strL)) {
                canvas.drawText(strL, lX, lY, mTextPaint);
            }
        }
    }

    private void drawPoint(Canvas canvas) {
//        Offset position = DrawingComponent.Companion.getRealPosition();
//        if (position == null || kLineDataList == null || kLineDataList.size() == 0) {
//            return;
//        }
//
//        double startTimeStamp = 0;
//        if (location >= 0) {
//            startTimeStamp = kLineDataList.get(location).getTime();
//        } else {
//            startTimeStamp = kLineDataList.get(0).getTime() + location * getTimeUnit();
//        }
//        double endTimeStamp = startTimeStamp + getTimeRange();
//        boolean isPoint1InRange = position.getTimeStamp() >= startTimeStamp && position.getTimeStamp() <= endTimeStamp
//                && position.getPrice() >= minData && position.getPrice() <= maxData;
//
//        double point1X = (position.getTimeStamp() - startTimeStamp) / getTimeRange() * width;
//        double point1Y = (maxData - position.getPrice()) / (maxData - minData) * height;
//
//        mPaint.setStrokeWidth(lineWidth);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(getResources().getColor(R.color.blue));
//        canvas.drawCircle((float)point1X, (float) point1Y, 10, mPaint);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(getResources().getColor(R.color.point_color));
//        canvas.drawCircle((float)point1X, (float) point1Y, 11, mPaint);
    }

    private void drawCustomPath(Canvas canvas) {
        DrawingData currentData = mKLineChartFrameLayout.drawingStatus.getCurrentDrawingData();
        if (currentData != null && !currentData.getPoints().isEmpty()) {
            return;
        }
    }


    /**
     * 绘制指标
     *
     * @param canvas
     */
    private void drawIndex(Canvas canvas) {
        if (tagKLI == KLineMainIndex.NONE) {

        } else if (tagKLI == KLineMainIndex.MA || tagKLI == KLineMainIndex.GAME) {
            drawMA(canvas);
            if (tagKLI == KLineMainIndex.GAME) {
                drawGame(canvas);
            }
        } else if (tagKLI == KLineMainIndex.EMA) {
            drawEMA(canvas);
        } else if (tagKLI == KLineMainIndex.BOLL) {
            drawBOLL(canvas);
        } else if (tagKLI == KLineMainIndex.SAR) {
            drawSAR(canvas);
        } else if (tagKLI == KLineMainIndex.BOLL_POINT) {
            drawBOLL_POINT(canvas);
        }
    }

    /**
     * 绘制MA指标
     *
     * @param canvas
     */
    private void drawMA(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configMA))[0]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configMA))[0]);
        float startX;
        float xCenter;
        float Y;
        int count = kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        int p0 = tagIndexParameter.get(tagKLI.getValue())[0];
        int p1 = tagIndexParameter.get(tagKLI.getValue())[1];
        int p2 = tagIndexParameter.get(tagKLI.getValue())[2];

        if (kLineDataList.size() == p0) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(p0 - 1).getMainMA().getmA1() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (p0 - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }
                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p0 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainMA().getmA1() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configMA))[1]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configMA))[1]);
        if (kLineDataList.size() == p1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(p1 - 1).getMainMA().getmA2() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (p1 - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }

                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p1 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainMA().getmA2() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configMA))[2]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configMA))[2]);
        if (kLineDataList.size() == p2) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(p2 - 1).getMainMA().getmA3() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (p2 - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }

                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p2 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainMA().getmA3() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 绘制EMA指标
     *
     * @param canvas
     */
    private void drawEMA(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configEMA))[0]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configEMA))[0]);
        float startX;
        float xCenter;
        float Y;
        int count = kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        int s0 = tagIndexParameter.get(tagKLI.getValue())[6],
                s1 = tagIndexParameter.get(tagKLI.getValue())[7],
                s2 = tagIndexParameter.get(tagKLI.getValue())[8],
                s3 = tagIndexParameter.get(tagKLI.getValue())[9],
                s4 = tagIndexParameter.get(tagKLI.getValue())[10],
                s5 = tagIndexParameter.get(tagKLI.getValue())[11];

        if (s0 > 0) {
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA1() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }

                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA1() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }

        if (s1 > 0) {
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configEMA))[1]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configEMA))[1]);
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA2() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }

                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA2() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }

        if (s2 > 0) {
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configEMA))[2]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configEMA))[2]);
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA3() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }

                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA3() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }

        if (s3 > 0) {
            mPaint.setColor(indexColors[3]);
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA4() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }

                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA4() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }

        if (s4 > 0) {
            mPaint.setColor(indexColors[4]);
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA5() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }

                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA5() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }

        if (s5 > 0) {
            mPaint.setColor(indexColors[5]);
            if (kLineDataList.size() == 1) {
                mPaint.setStyle(Paint.Style.FILL);
                Y = (float) (height - (kLineDataList.get(0).getMainEMA().geteMA6() -
                        minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() +
                        (kLineWidth + kLineSpacing) * (0 - location);
                xCenter = startX + kLineXCenter;
                canvas.drawCircle(xCenter, Y, 3, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPath.reset();
                for (int i = 0; i < count; i++) {
                    index = location + i;
                    if (index < 0) {
                        continue;
                    }
                    if (index >= kLineDataList.size()) {
                        break;
                    }
                    KLineDataValid entity = kLineDataList.get(index);
                    Y = (float) (height - (entity.getMainEMA().geteMA6() - minData) * scale);
                    startX = mKLineChartFrameLayout.getStrokeWidth() +
                            (kLineWidth + kLineSpacing) * i;
                    xCenter = startX + kLineXCenter;
                    if (mPath.isEmpty()) {
                        mPath.moveTo(xCenter, Y);
                    } else {
                        mPath.lineTo(xCenter, Y);
                    }
                }
                canvas.drawPath(mPath, mPaint);
            }
        }
    }

    /**
     * 绘制BOLL
     *
     * @param canvas
     */
    private int drawBOLL(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return -1;
        mPaint.setStrokeWidth(lineWidth);
        int p0 = tagIndexParameter.get(tagKLI.getValue())[0];
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index = -1;
        if (kLineDataList.size() == p0) {
            index = p0 - 1 - location;

            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * index;
            xCenter = startX + kLineXCenter;

            KLineDataValid entity = kLineDataList.get(p0 - 1);
            Y = (float) (height -
                    (entity.getMainBOLL().getmID() - minData) * scale);
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[0]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[0]);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            Y = (float) (height -
                    (entity.getMainBOLL().getUpper() - minData) * scale);
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[1]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[1]);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            Y = (float) (height -
                    (entity.getMainBOLL().getLower() - minData) * scale);
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[2]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[2]);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[0]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[0]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }
                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p0 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainBOLL().getmID() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[1]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[1]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }
                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p0 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainBOLL().getUpper() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configBOLL))[2]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configBOLL))[2]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0) {
                    continue;
                }
                if (index >= kLineDataList.size()) {
                    break;
                }
                if (index < p0 - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height - (entity.getMainBOLL().getLower() - minData) * scale);
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }
        return index;
    }

    /**
     * 绘制SAR
     *
     * @param canvas
     */
    private void drawSAR(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getSARColors();
        mPaint.setStrokeWidth(lineWidth * 2f);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index = -1;
        mPaint.setColor(indexColors[0]);
        mPaint.setStyle(Paint.Style.STROKE);
        mPath.reset();
        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index < 0) {
                continue;
            }
            if (index >= kLineDataList.size()) {
                break;
            }
            KLineDataValid entity = kLineDataList.get(index);
            Y = (float) (height - (entity.getSar().getSar() - minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, kLineWidth / 2f, mPaint);
        }
    }

    /**
     * 绘制游戏
     *
     * @param canvas
     */
    private void drawGame(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getGameColors();
        mPaint.setStrokeWidth(lineWidth);
        mTextPaint.setTextSize(gameTextSize);
        float startX;
        float xCenter;
        float higY;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;
        if (path1 == null) {
            path1 = new Path();
        }
        if (path2 == null) {
            path2 = new Path();
        }

        RectF rectF = new RectF();
        float textWidth;
        float padding = (float) gameTextSize / 3f;
        float arrowH = 8f, arrowW = 8f;
        String text;
        int strokeColor, backgroundColor;

        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index < 0) {
                continue;
            }
            if (index >= kLineDataList.size()) {
                break;
            }
            KLineDataValid entity = kLineDataList.get(index);
            byte buyAndSell = entity.getBuyAndSell();
            if (buyAndSell == 0b0) {
                continue;
            }
            higY = height
                    - (float) (entity.getHig() - minData) * scale;
            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing)
                    * i;
            xCenter = startX + kLineXCenter;

            if (buyAndSell > 0) {
                strokeColor = indexColors[2];
                backgroundColor = indexColors[4];
                mTextPaint.setColor(indexColors[0]);
                textWidth = mTextPaint.measureText(gameBuyText);
                text = gameBuyText;
            } else {
                strokeColor = indexColors[3];
                backgroundColor = indexColors[5];
                mTextPaint.setColor(indexColors[1]);
                textWidth = mTextPaint.measureText(gameSellText);
                text = gameSellText;
            }
            rectF.left = xCenter - textWidth / 2f - padding;
            rectF.top = higY - (float) gameTextSize - padding * 2f - arrowH;
            rectF.right = rectF.left + textWidth + padding * 2f;
            rectF.bottom = rectF.top + gameTextSize + padding * 2f;
            if (rectF.top < 0) {
                float by = Math.abs(0 - rectF.top);
                rectF.top = 0;
                rectF.bottom = rectF.bottom + by;
            }
            path1.addRoundRect(rectF, 7, 7, Path.Direction.CCW);
            if (path2.isEmpty()) {
                path2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
            }
            path2.lineTo(xCenter, rectF.bottom + arrowH);
            path2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
            path2.close();
            path1.op(path2, Path.Op.UNION);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(backgroundColor);
            canvas.drawPath(path1, mPaint);
            path1.reset();
            path2.reset();

            path1.addRoundRect(rectF, 7, 7, Path.Direction.CCW);
            if (path2.isEmpty()) {
                path2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
            }
            path2.lineTo(xCenter, rectF.bottom + arrowH);
            path2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
            path2.close();
            path1.op(path2, Path.Op.UNION);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(strokeColor);
            canvas.drawPath(path1, mPaint);
            path1.reset();
            path2.reset();
            canvas.drawText(text,
                    xCenter - textWidth / 2f,
                    rectF.top + padding + gameTextSize - 3, mTextPaint);
        }
    }

    /**
     * 绘制布林支撑、压力位
     *
     * @param canvas
     */
    private void drawBOLL_POINT(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0) {
            return;
        }
        int index = drawBOLL(canvas);
        int p1 = tagIndexParameter.get(tagKLI.getValue())[1];
        if (index > -1 && index < kLineDataList.size() && index > p1 - 2) {
            KLineDataValid lastData = kLineDataList.get(index);
            float upY, downY;
            double upData, downData;
            mPaint.setPathEffect(mDashPathEffect);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(lineWidth);

            if (lastData.getMainBOLL().getUpper() > lastData.getHig()
                    && lastData.getMainBOLL().getmID() < lastData.getLow()) {
                mPaint.setColor(anodeColor);
                upData = lastData.getMainBOLL().getUpper();
                upY = (float) (height - (upData - minData) * scale);
                canvas.drawLine(0, upY, width, upY, mPaint);
                mPaint.setColor(cathodeColor);
                downData = lastData.getMainBOLL().getmID();
                downY = (float) (height - (downData - minData) * scale);
                canvas.drawLine(0, downY, width, downY, mPaint);

            } else if (lastData.getMainBOLL().getLower() < lastData.getLow()
                    && lastData.getMainBOLL().getmID() > lastData.getHig()) {
                mPaint.setColor(anodeColor);
                upData = lastData.getMainBOLL().getmID();
                upY = (float) (height - (upData - minData) * scale);
                canvas.drawLine(0, upY, width, upY, mPaint);
                mPaint.setColor(cathodeColor);
                downData = lastData.getMainBOLL().getLower();
                downY = (float) (height - (downData - minData) * scale);
                canvas.drawLine(0, downY, width, downY, mPaint);

            } else {
                mPaint.setColor(anodeColor);
                upData = lastData.getMainBOLL().getUpper();
                upY = (float) (height - (upData - minData) * scale);
                canvas.drawLine(0, upY, width, upY, mPaint);
                mPaint.setColor(cathodeColor);
                downData = lastData.getMainBOLL().getLower();
                downY = (float) (height - (downData - minData) * scale);
                canvas.drawLine(0, downY, width, downY, mPaint);

            }
            mPaint.setPathEffect(null);
            mPaint.setStyle(Paint.Style.FILL);
            String upText = "压力位 " + String.format(Locale.ENGLISH, "%." + decPlace + "f", upData);
            String downText = "支撑位 " + String.format(Locale.ENGLISH, "%." + decPlace + "f", downData);

            mTextPaint.setTextSize(textSize);
            mTextPaint.setColor(0xffffffff);
            mPaint.setColor(anodeColor);
            float w = mTextPaint.measureText(upText) + 4;
            canvas.drawRect(0, upY - textSize - 4, w, upY, mPaint);
            canvas.drawText(upText, 2, upY - 4, mTextPaint);

            mPaint.setColor(cathodeColor);
            w = mTextPaint.measureText(downText) + 4;
            float bottom = downY + textSize + 4;
            canvas.drawRect(0, downY, w, bottom, mPaint);
            canvas.drawText(downText, 2, bottom - 4, mTextPaint);
        }
    }

    private double hig;
    private double low;
    private float y;
    private float spacing;
    private float strokeWidth2;
    private final String BFH = "%";

    /**
     * 绘制左/右边刻度
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        if (!isShowText) {
            return;
        }
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        y = height / 4.0f;
        if (Double.isNaN(preMaxData)
                || Double.isNaN(preMinData)) {
            hig = maxData;
            low = minData;
        } else {
            hig = preMaxData;
            low = preMinData;
        }
        spacing = (float) ((hig - low) / 4.0f);
        strokeWidth2 = mKLineChartFrameLayout.getStrokeWidth() * 2.0f;
        if (tagKLI == KLineMainIndex.RETURN_RATE) {
            String str;
            String[] strs;
            for (int i = 0; i < 5; i++) {
                str = stockUtils.parse2USString(hig * symbolExrate, false, decPlace);
                if (str.contains(".")) {
                    strs = str.split("\\.");
                    String l = "";
                    for (int j = 0; j < decPlace; j++) {
                        l += "0";
                    }
                    if (l.equals(strs[1])) {
                        str = strs[0];
                    }
                }
                if (i == 0) {
                    canvas.drawText(
                            str + BFH,
                            strokeWidth2, textSize, mTextPaint);
                } else if (i == 4) {
                    str = stockUtils.parse2String(low * symbolExrate, decPlace, false);
                    if (str.contains(".")) {
                        strs = str.split("\\.");
                        String l = "";
                        for (int j = 0; j < decPlace; j++) {
                            l += "0";
                        }
                        if (l.equals(strs[1])) {
                            str = strs[0];
                        }
                    }
                    canvas.drawText(
                            str + BFH,
                            strokeWidth2, y * i - 2, mTextPaint);
                } else {
                    canvas.drawText(
                            str + BFH,
                            strokeWidth2,
                            y * i + textSize / 2 - 2, mTextPaint);
                }
                hig -= spacing;
            }
        } else {
            for (int i = 0; i < 5; i++) {
                if (i == 0) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.BEGINNING
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2USString(hig * symbolExrate, false, decPlace)
                                : stockUtils.parse2String(hig * symbolExrate, decPlace, false);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(text,
                                getWidth() - width - strokeWidth2, textSize, mTextPaint);
                    }
                } else if (i == 4) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.END
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2USString(low * symbolExrate, false, decPlace)
                                : stockUtils.parse2String(low * symbolExrate, decPlace, false);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2, y * i - 2, mTextPaint);
                    }
                } else {
                    if (scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)) {
                        if (i == 1 || i == 3) {
                            hig -= spacing;
                            continue;
                        }
                    } else if (scaleRule == ChartBaseView.BEGINNING
                            || scaleRule == ChartBaseView.END
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        hig -= spacing;
                        continue;
                    }
                    String text = KlineConfig.SHOW_US_PRICE ? stockUtils.parse2USString(hig * symbolExrate, false, decPlace)
                            : stockUtils.parse2String(hig * symbolExrate, decPlace, false);
                    float width = mTextPaint.measureText(text);
                    canvas.drawText(
                            text,
                            getWidth() - width - strokeWidth2,
                            y * i + textSize / 2 - 2, mTextPaint);
                }
                hig -= spacing;
            }
        }
    }

    /**
     * 设置币种汇率
     *
     * @param symbolExrate
     * @return
     */
    public KLineChartMainView setSymbolExrate(double symbolExrate) {
        if (this.symbolExrate != symbolExrate && symbolExrate > 0) {
            this.symbolExrate = symbolExrate;
        }
        return this;
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public void setData(ArrayList<KLineDataValid> list) {
        if (isDestroy) {
            return;
        }
        kLineDataList = list;
        if (mKLineChartFrameLayout != null) {
            kLineWidth = mKLineChartFrameLayout.getkLineWidth();
            zoomToLineWidth = mKLineChartFrameLayout.getZoomToLineWidth();
            kLineSpacing = mKLineChartFrameLayout.getkLineSpacing();
            maxKLineLength = mKLineChartFrameLayout.getMaxKLineLength();
            location = mKLineChartFrameLayout.getLocation();
            kLineXCenter = kLineWidth / 2.0f;
            if (kLineDataList != null && kLineDataList.size() > location) {
//                int count = kLineDataList.size() < maxKLineLength ? kLineDataList.size()
//                        : maxKLineLength;
                int count = maxKLineLength;
                double maxData = 0;
                double minData = 0;
                double maxHigh = 0;
                double minLow = Double.MAX_VALUE;
                int position;
                if (tagKLI == KLineMainIndex.NONE) {
                    maxData = kLineDataList.get(0).getHig();
                    minData = kLineDataList.get(0).getLow();

                    if (location > 0) {
                        maxData = kLineDataList.get(location).getHig();
                        minData = kLineDataList.get(location).getLow();
                    }

                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (maxHigh < item.getHig()) {
                            maxHigh = item.getHig();
                            maxData = maxHigh;
                            highPosition = position;
                        }
                        if (minLow > item.getLow()) {
                            minLow = item.getLow();
                            minData = minLow;
                            lowPosition = position;
                        }
                    }
                } else if (tagKLI == KLineMainIndex.MA || tagKLI == KLineMainIndex.GAME) {
                    int p0 = tagIndexParameter.get(tagKLI.getValue())[0],
                            p1 = tagIndexParameter.get(tagKLI.getValue())[1],
                            p2 = tagIndexParameter.get(tagKLI.getValue())[2],
                            p3 = tagIndexParameter.get(tagKLI.getValue())[3],
                            p4 = tagIndexParameter.get(tagKLI.getValue())[4],
                            p5 = tagIndexParameter.get(tagKLI.getValue())[5];
                    int s0 = tagIndexParameter.get(tagKLI.getValue())[6],
                            s1 = tagIndexParameter.get(tagKLI.getValue())[7],
                            s2 = tagIndexParameter.get(tagKLI.getValue())[8],
                            s3 = tagIndexParameter.get(tagKLI.getValue())[9],
                            s4 = tagIndexParameter.get(tagKLI.getValue())[10],
                            s5 = tagIndexParameter.get(tagKLI.getValue())[11];

                    if (location >= 0) {
                        maxData = kLineDataList.get(location).getHig();
                        minData = kLineDataList.get(location).getLow();
                    } else {
                        maxData = kLineDataList.get(0).getHig();
                        minData = kLineDataList.get(0).getLow();
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }

                        if (position < 0) {
                            continue;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (maxHigh < item.getHig()) {
                            maxHigh = item.getHig();
                            highPosition = position;
                        }
                        if (minLow > item.getLow()) {
                            minLow = item.getLow();
                            lowPosition = position;
                        }

                        if (maxHigh > maxData) {
                            maxData = maxHigh;
                        }
                        if (minLow < minData) {
                            minData = minLow;
                        }
                        if (s0 > 0)
                            if (position >= p0 - 1) {
                                maxData = maxData > item.getMainMA().getmA1() ? maxData
                                        : item.getMainMA().getmA1();
                                minData = minData < item.getMainMA().getmA1() ? minData
                                        : item.getMainMA().getmA1();
                            }
                        if (s1 > 0)
                            if (position >= p1 - 1) {
                                maxData = maxData > item.getMainMA().getmA2() ? maxData
                                        : item.getMainMA().getmA2();
                                minData = minData < item.getMainMA().getmA2() ? minData
                                        : item.getMainMA().getmA2();
                            }
                        if (s2 > 0)
                            if (position >= p2 - 1) {
                                maxData = maxData > item.getMainMA().getmA3() ? maxData
                                        : item.getMainMA().getmA3();
                                minData = minData < item.getMainMA().getmA3() ? minData
                                        : item.getMainMA().getmA3();
                            }
                        if (s3 > 0)
                            if (position >= p3 - 1) {
                                maxData = maxData > item.getMainMA().getmA4() ? maxData
                                        : item.getMainMA().getmA4();
                                minData = minData < item.getMainMA().getmA4() ? minData
                                        : item.getMainMA().getmA4();
                            }
                        if (s4 > 0)
                            if (position >= p4 - 1) {
                                maxData = maxData > item.getMainMA().getmA5() ? maxData
                                        : item.getMainMA().getmA5();
                                minData = minData < item.getMainMA().getmA5() ? minData
                                        : item.getMainMA().getmA5();
                            }
                        if (s5 > 0)
                            if (position >= p5 - 1) {
                                maxData = maxData > item.getMainMA().getmA6() ? maxData
                                        : item.getMainMA().getmA6();
                                minData = minData < item.getMainMA().getmA6() ? minData
                                        : item.getMainMA().getmA6();
                            }
                    }
                } else if (tagKLI == KLineMainIndex.EMA) {
                    int s0 = tagIndexParameter.get(tagKLI.getValue())[6],
                            s1 = tagIndexParameter.get(tagKLI.getValue())[7],
                            s2 = tagIndexParameter.get(tagKLI.getValue())[8],
                            s3 = tagIndexParameter.get(tagKLI.getValue())[9],
                            s4 = tagIndexParameter.get(tagKLI.getValue())[10],
                            s5 = tagIndexParameter.get(tagKLI.getValue())[11];
                    maxData = kLineDataList.get(0).getHig();
                    minData = kLineDataList.get(0).getLow();

                    if (location > 0) {
                        maxData = kLineDataList.get(location).getHig();
                        minData = kLineDataList.get(location).getLow();
                    }

                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (maxHigh < item.getHig()) {
                            maxHigh = item.getHig();
                            highPosition = position;
                        }
                        if (minLow > item.getLow()) {
                            minLow = item.getLow();
                            lowPosition = position;
                        }

                        if (maxHigh > maxData) {
                            maxData = maxHigh;
                        }
                        if (minLow < minData) {
                            minData = minLow;
                        }
                        if (s0 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA1() ? maxData
                                    : item.getMainEMA().geteMA1();
                            minData = minData < item.getMainEMA().geteMA1() ? minData
                                    : item.getMainEMA().geteMA1();
                        }
                        if (s1 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA2() ? maxData
                                    : item.getMainEMA().geteMA2();
                            minData = minData < item.getMainEMA().geteMA2() ? minData
                                    : item.getMainEMA().geteMA2();
                        }
                        if (s2 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA3() ? maxData
                                    : item.getMainEMA().geteMA3();
                            minData = minData < item.getMainEMA().geteMA3() ? minData
                                    : item.getMainEMA().geteMA3();
                        }
                        if (s3 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA4() ? maxData
                                    : item.getMainEMA().geteMA4();
                            minData = minData < item.getMainEMA().geteMA4() ? minData
                                    : item.getMainEMA().geteMA4();
                        }
                        if (s4 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA5() ? maxData
                                    : item.getMainEMA().geteMA5();
                            minData = minData < item.getMainEMA().geteMA5() ? minData
                                    : item.getMainEMA().geteMA5();
                        }
                        if (s5 > 0) {
                            maxData = maxData > item.getMainEMA().geteMA6() ? maxData
                                    : item.getMainEMA().geteMA6();
                            minData = minData < item.getMainEMA().geteMA6() ? minData
                                    : item.getMainEMA().geteMA6();
                        }
                    }
                } else if (tagKLI == KLineMainIndex.BOLL ||
                        tagKLI == KLineMainIndex.BOLL_POINT) {
                    int p0 = tagIndexParameter.get(tagKLI.getValue())[0];

                    maxData = kLineDataList.get(0).getHig();
                    minData = kLineDataList.get(0).getLow();

                    if (location > 0) {
                        maxData = kLineDataList.get(location).getHig();
                        minData = kLineDataList.get(location).getLow();
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (maxHigh < item.getHig()) {
                            maxHigh = item.getHig();
                            highPosition = position;
                        }
                        if (minLow > item.getLow()) {
                            minLow = item.getLow();
                            lowPosition = position;
                        }

                        if (maxHigh > maxData) {
                            maxData = maxHigh;
                        }
                        if (minLow < minData) {
                            minData = minLow;
                        }
                        if (position < p0 - 1) {
                            continue;
                        }
                        maxData = maxData > item.getMainBOLL().getUpper() ? maxData
                                : item.getMainBOLL().getUpper();
                        minData = minData < item.getMainBOLL().getLower() ? minData
                                : item.getMainBOLL().getLower();
                    }
                } else if (tagKLI == KLineMainIndex.TIME) {
                    maxData = kLineDataList.get(0).getClose();
                    minData = kLineDataList.get(0).getClose();

                    if (location > 0) {
                        maxData = kLineDataList.get(location).getClose();
                        minData = kLineDataList.get(location).getClose();
                    }

                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);
                        if (maxHigh < item.getClose()) {
                            maxHigh = item.getClose();
                            maxData = maxHigh;
                            highPosition = position;
                        }
                        if (minLow > item.getClose()) {
                            minLow = item.getClose();
                            minData = minLow;
                            lowPosition = position;
                        }
                    }
                } else if (tagKLI == KLineMainIndex.RETURN_RATE) {
                    maxData = kLineDataList.get(location).getReturnRate().getRate();
                    minData = maxData;
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);
                        maxData = maxData > item.getReturnRate().getRate() ? maxData
                                : item.getReturnRate().getRate();
                        minData = minData < item.getReturnRate().getRate() ? minData
                                : item.getReturnRate().getRate();
                    }
                } else if (tagKLI == KLineMainIndex.SAR) {
                    maxData = kLineDataList.get(location).getHig();
                    minData = kLineDataList.get(location).getLow();
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (maxHigh < item.getHig()) {
                            maxHigh = item.getHig();
                            highPosition = position;
                        }
                        if (minLow > item.getLow()) {
                            minLow = item.getLow();
                            lowPosition = position;
                        }

                        if (maxHigh > maxData) {
                            maxData = maxHigh;
                        }
                        if (minLow < minData) {
                            minData = minLow;
                        }

                        maxData = maxData > item.getSar().getSar() ? maxData
                                : item.getSar().getSar();
                        minData = minData < item.getSar().getSar() ? minData
                                : item.getSar().getSar();
                    }
                }
                if (tagKLI == KLineMainIndex.RETURN_RATE) {
                    if (maxData <= 0.01f && minData >= -0.01f) {
                        this.maxData = 0.02f;
                        this.minData = -0.02f;
                    } else {
                        int num = 4;
                        double positiveNum = 0;
                        double negativeNum = 0;
                        double diff = 0;
                        if (minData >= 0) {
                            diff = maxData / num;
                            positiveNum = num;
                        } else if (maxData <= 0) {
                            diff = Math.abs(minData / num);
                            negativeNum = num;
                        } else {
                            double positiveMin = Math.min(Math.abs(minData), Math.abs(maxData));
                            double positiveMax = Math.max(Math.abs(minData), Math.abs(maxData));
                            if (positiveMax / (num - 1) >= positiveMin) {
                                diff = positiveMax / (num - 1);
                                positiveNum = Math.abs(maxData) >= Math.abs(minData) ? num - 1 : 1;
                                negativeNum = num - positiveNum;
                            } else {
                                double sum = Math.abs(minData) + Math.abs(maxData);
                                double avg = sum / num;
                                double remainder = Math.min(
                                        Math.abs(minData) % avg, Math.abs(maxData) % avg);
                                diff = avg + remainder;
                                positiveNum = Math.abs(maxData) % avg >= Math.abs(minData) % avg
                                        ? Math.ceil(Math.abs(maxData) / avg) :
                                        Math.floor(Math.abs(maxData) / avg);
                                negativeNum = num - positiveNum;
                            }
                        }
                        double index = Math.floor(Math.log10(diff));
                        double powVal = index >= 0 ? Math.pow(10, index) : Math.pow(10, -index);
                        double newDiff = index >= 0 ? +(Math.ceil(diff / powVal) * powVal)
                                : +(Math.ceil(diff * powVal) / powVal);
                        this.minData = -newDiff * negativeNum;
                        this.maxData = newDiff * positiveNum;
                    }
                } else if (tagKLI == KLineMainIndex.TIME) {
                    double diff = maxData - minData;
                    this.maxData = Math.max(maxData + diff * topRate, maxData);
//                    if (isShowText) {
//                        this.minData = Math.max(minData - diff * bottomRate, 0);
//                    } else {
                    this.minData = minData - diff * bottomRate;
//                    }
                    if (this.maxData == this.minData) {
                        this.maxData = this.minData + 1f;
                        this.minData = this.minData - 1f;
                        if (isShowText) {
                            this.minData = this.minData < 0 ? 0 : this.minData;
                        }
                    }
                } else {
                    double diff = maxData - minData;
                    this.maxData = Math.max(maxData + diff * topRate, maxData);
                    this.minData = minData - diff * bottomRate;
                    if (this.maxData == this.minData) {
                        this.maxData = this.minData + 1f;
                        this.minData = this.minData - 1f;
                    }
                }
                scale = (float) (height / (this.maxData - this.minData));
                if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                    scale = 0;
                }
                if (count > 0 && count <= kLineDataList.size()) {
                    curCloseData = kLineDataList.get(count-1).getClose();
                }
            } else {
                this.maxData = 4;
                this.minData = 0;
                scale = 0;
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
                // 计算历史成交数据
                tradeInfoMap.clear();
                mKLineChartFrameLayout.tradeInfoMap.clear();
                Boolean orderHostory = LocalStore.getInstance().getBoolean(KEY_KLINE_ORDER_HISTORY, false);
                if (mKLineChartFrameLayout.orientation == Configuration.ORIENTATION_PORTRAIT && orderHostory) {
                    if (kLineDataList != null && !kLineDataList.isEmpty()) {
                        for(int i=0;i<kLineDataList.size();i++){
                            Pair<TradeInfo, TradeInfo> pair = getTwoOrder(i);
                            if (pair.first != null || pair.second != null) {
                                tradeInfoMap.put(kLineDataList.get(i).getTime(), pair);
                            }
                        }
                        mKLineChartFrameLayout.tradeInfoMap = tradeInfoMap;
                    }
                }
                ondraw(mCanvas);
                isDraw = false;
            }
            if (drawListener != null && kLineDataList != null && !kLineDataList.isEmpty()) {
                drawListener.drawKLineFinish();
            }
        }

    }

    /**
     * 设置根布局
     *
     * @param mKLineChartFrameLayout
     */
    public void setKLineChartFrameLayout(KLineChartFrameLayout mKLineChartFrameLayout) {
        this.mKLineChartFrameLayout = mKLineChartFrameLayout;
    }

    /**
     * 设置指标参数
     *
     * @param mKLineMainIndex
     * @param parameter
     * @return true成功，false失败
     */
    public boolean setParameter(KLineMainIndex mKLineMainIndex, int... parameter) {
        switch (mKLineMainIndex.getValue()) {
            case "MA": {
                configMA = KLineSetUtils.INSTANCE.getConfig("MA",false);
            }
            case "EMA": {
                configEMA = KLineSetUtils.INSTANCE.getConfig("EMA",false);
            }
            case "BOLL": {
                configBOLL = KLineSetUtils.INSTANCE.getConfig("BOLL",false);
            }
        }
        if (mKLineMainIndex == null || mKLineMainIndex.getParameter() == null ||
                parameter == null) {
            return false;
        }
        if (mKLineMainIndex.getParameter().length != parameter.length) {
            return false;
        }
        indexParameter.put(mKLineMainIndex.getValue(), parameter);
        if (mKLineChartFrameLayout != null && mKLineMainIndex == this.mKLineMainIndex) {
            mKLineChartFrameLayout.updateIndex();
        }
        return true;
    }

    /**
     * 获取当前指标参数
     *
     * @return
     */
    public int[] getParameter() {
        int[] pm = tagIndexParameter.get(tagKLI.getValue());
        if (pm == null || pm.length != tagKLI.getParameter().length) {
            return tagKLI.getParameter();
        } else {
            return pm;
        }
    }

    /**
     * 获取最新指标参数（可能正在计算中或者还未进行计算）
     *
     * @return
     */
    public int[] getNewParameter() {
        int[] pm = indexParameter.get(mKLineMainIndex.getValue());
        if (pm == null || pm.length != mKLineMainIndex.getParameter().length) {
            return mKLineMainIndex.getParameter();
        } else {
            return pm;
        }
    }

    /**
     * 获取最新指定指标参数（可能正在计算中或者还未进行计算）
     *
     * @return
     */
    public int[] getNewParameter(KLineMainIndex mKLineMainIndex) {
        int[] pm = indexParameter.get(mKLineMainIndex.getValue());
        if (pm == null || pm.length != mKLineMainIndex.getParameter().length) {
            return mKLineMainIndex.getParameter();
        } else {
            return pm;
        }
    }

    /**
     * 设置指标
     *
     * @param mKLineMainIndex
     */
    public void setKLineMainIndex(KLineMainIndex mKLineMainIndex) {
        setKLineMainIndex(mKLineMainIndex, true);
    }

    /**
     * 设置指标
     *
     * @param mKLineMainIndex
     * @param isNotifyRefresh 是否通知刷新指标
     */
    public void setKLineMainIndex(KLineMainIndex mKLineMainIndex, boolean isNotifyRefresh) {
        if (mKLineMainIndex == null || mKLineMainIndex == this.mKLineMainIndex) {
            return;
        }
        this.mKLineMainIndex = mKLineMainIndex;
        if (indexParameter.get(mKLineMainIndex.getValue()) == null
                || (mKLineMainIndex.getParameter() != null &&
                indexParameter.get(mKLineMainIndex.getValue()).length !=
                        mKLineMainIndex.getParameter().length)
                || (indexParameter.get(mKLineMainIndex.getValue()) != null
                && mKLineMainIndex.getParameter() == null)) {
            indexParameter.put(mKLineMainIndex.getValue(), mKLineMainIndex.getParameter());
            tagIndexParameter = indexParameter;
        }
        if (isNotifyRefresh) {
            if (mKLineChartFrameLayout != null) {
                mKLineChartFrameLayout.updateIndex();
            }
        }
    }

    /**
     * 获取计算好的指标
     *
     * @return
     */
    public KLineMainIndex getKLineMainIndex() {
        return tagKLI;
    }

    /**
     * 获取当前颜色数组
     *
     * @return
     */
    public int[] getIndexColors() {
        return indexColors;
    }

    /**
     * 计算好的指标
     *
     * @param tagKLI
     */
    void setTagKLI(KLineMainIndex tagKLI) {
        this.tagKLI = tagKLI;
        tagIndexParameter = indexParameter;
    }

    /**
     * 获取最新设置的指标（可能正在计算中或者还未进行计算）
     *
     * @return
     */
    public KLineMainIndex getNewIndex() {
        return mKLineMainIndex;
    }

    public int getDecPlace() {
        return decPlace;
    }

    /**
     * 设置保留小数位数
     *
     * @param decPlace
     */
    public KLineChartMainView setDecPlace(int decPlace) {
        if (this.decPlace != decPlace) {
            this.decPlace = decPlace;
        }
        return this;
    }

    /**
     * 设置不涨不跌的颜色
     *
     * @param flatColor
     * @return
     */
    public KLineChartMainView setFlatColor(int flatColor) {
        if (this.flatColor != flatColor) {
            this.flatColor = flatColor;
        }
        return this;
    }

    public int getFlatColor() {
        return flatColor;
    }

    /**
     * 设置渐变色
     *
     * @param shaderColor
     * @return
     */
    public KLineChartMainView setShaderColor(int shaderColor) {
        if (this.shaderColor != shaderColor) {
            this.shaderColor = shaderColor;
        }
        return this;
    }

    /**
     * 设置是否显示最高最低
     *
     * @param isShowHighAndLowData
     * @return
     */
    public KLineChartMainView setShowHighAndLowData(boolean isShowHighAndLowData) {
        if (this.isShowHighAndLowData != isShowHighAndLowData) {
            this.isShowHighAndLowData = isShowHighAndLowData;
        }
        return this;
    }

    /**
     * 设置是否显示刻度
     *
     * @param isShowText
     * @return
     */
    public KLineChartMainView setShowText(boolean isShowText) {
        this.isShowText = isShowText;
        return this;
    }

    public void build() {
        setData(this.kLineDataList);
    }

    /**
     * 清除数据
     */
    public void clear() {
        if (kLineDataList != null) {
            kLineDataList.clear();
            setData(kLineDataList);
        }
    }

    /**
     * 获取数据与图形比例
     *
     * @return
     */
    public float getScale() {
        return scale;
    }

    /**
     * 获取最小数据
     *
     * @return
     */
    public double getMinData() {
        return minData;
    }

    /**
     * 设置顶部比例
     *
     * @param topRate
     */
    public void setTopRate(float topRate) {
        if (this.topRate != topRate) {
            this.topRate = topRate;
            if (this.topRate < 0.05f) {
                this.topRate = 0.05f;
            } else if (this.topRate > 0.3f) {
                this.topRate = 0.3f;
            }
        }
    }

    /**
     * 设置底部比例
     *
     * @param bottomRate
     */
    public void setBottomRate(float bottomRate) {
        if (this.bottomRate != bottomRate) {
            this.bottomRate = bottomRate;
            if (this.bottomRate < 0.05f) {
                this.bottomRate = 0.05f;
            } else if (this.bottomRate > 0.3f) {
                this.bottomRate = 0.3f;
            }
        }
    }

    /**
     * 获取预设值
     *
     * @return
     */
    public double getPreMaxData() {
        return preMaxData;
    }

    /**
     * 设置预设值
     *
     * @param preMaxData
     */
    public void setPreMaxData(double preMaxData) {
        this.preMaxData = preMaxData;
    }

    /**
     * 获取预设值
     *
     * @return
     */
    public double getPreMinData() {
        return preMinData;
    }

    /**
     * 设置预设值
     *
     * @param preMinData
     */
    public void setPreMinData(double preMinData) {
        this.preMinData = preMinData;
    }

    public void setDrawListener(DrawListener drawListener) {
        this.drawListener = drawListener;
    }

    public interface DrawListener {
        void drawKLineFinish();
    }

    public boolean handleClick(int rawX, int rawY) {
        int[] location = new int[2];
        getLocationOnScreen(location);
        int x = rawX - location[0];
        int y = rawY - location[1];
        return curCloseRect!=null && curCloseRect.contains(x, y);
    }

    public void setTradeList(List<TradeInfo> tradeInfoList) {
        this.tradeInfoList = tradeInfoList;
        if (!tradeInfoList.isEmpty()) {
            earliestTradeTime = tradeInfoList.get(0).getTime();
        }
        for(int i=0;i<tradeInfoList.size();i++) {
            TradeInfo item = tradeInfoList.get(i);
            if (item.getTime() < earliestTradeTime) {
                earliestTradeTime = item.getTime();
            }
        }
    }

    private Pair<TradeInfo,TradeInfo> getTwoOrder(int index) {
        if (tradeInfoList.isEmpty() || kLineDataList.isEmpty()) {
            return new Pair(null, null);
        }
        TradeInfo buyOrder = null;
        TradeInfo sellOrder = null;
        long klineStartTime = kLineDataList.get(index).getTime();
        long klineEndTime = Long.MAX_VALUE;
        if (index+1 < kLineDataList.size()) {
            klineEndTime = kLineDataList.get(index+1).getTime();
        }
        if (klineEndTime < earliestTradeTime) {
            return new Pair(null, null);
        }
        for(int i=0;i<tradeInfoList.size();i++) {
            TradeInfo item = tradeInfoList.get(i);
            if (item.getTime() > klineStartTime && item.getTime() < klineEndTime) {
                if (item.isBuyOrder() && buyOrder == null) {
                    buyOrder = item;
                }
                if (!item.isBuyOrder() && sellOrder == null) {
                    sellOrder = item;
                }
            }
        }
        return new Pair(buyOrder, sellOrder);
    }
}
