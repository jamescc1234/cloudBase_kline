package com.widget.stock.k_line.view;

import static com.utils.ExtKt.makeVibrate;
import static com.widget.stock.k_line.configure.KlineConstants.KEY_KLINE_TOUCH_VIBRATION;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.drawing.DrawingStatus;
import com.drawing.DrawingType;
import com.kedll.stock.library.base.BaseThread;
import com.magnify.MagnifierAutoLayout;
import com.magnify.MagnifierBuilder;
import com.utils.DrawingStatusListener;
import com.utils.LocalStore;
import com.widget.StockGestureDetector;
import com.widget.stock.ChartBaseFrameLayout;
import com.widget.stock.ChartCrossView;
import com.widget.stock.ChartLinearLayout;
import com.widget.stock.OnChartDataObserver;
import com.widget.stock.TimeLocation;
import com.widget.stock.adapter.ChartAdapter;
import com.widget.stock.k_line.adapter.KLineChartAdapter;
import com.widget.stock.k_line.data.KLineData;
import com.widget.stock.k_line.data.KLineDataValid;
import com.widget.stock.k_line.data.Offset;
import com.widget.stock.k_line.data.TradeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import binance.stock.library.R;


/**
 * Created by dingrui on 2016/10/21.
 * K线根布局
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class KLineChartFrameLayout extends ChartBaseFrameLayout {
    private List<View> chartViewList = new ArrayList<>();// 主、副图集合
    private boolean isViewsAddAll = false;// 主、副图集合是否添加完毕
    private boolean isDestroy = false;// 视图是否结束
    public DrawingStatus drawingStatus = new DrawingStatus();

    private String symbol = "";// 交易对
    private String flagTime = "";// K线周期

    public static final int DRAW_DATE = 0,// 绘制日期
            DRAW_TIME = 1,// 绘制时间
            DRAW_DATE_TIME = 2;// 绘制时间和日期
    private int dateType = DRAW_DATE;
    private static final int LEAST_ELEMENT_TO_SHOW = 5;
    private int decPlace = 2;

    private boolean isZoomToLineEnabled = false;
    private boolean onlyShowFloatingView = false;

    public void setDecPlace(int decPlace) {
        this.decPlace = decPlace;
    }

    public int getDecPlace() {
        return decPlace;
    }

    public void setOnlyShowFloatingView(boolean onlyShowFloatingView) {
        this.onlyShowFloatingView = onlyShowFloatingView;
    }

    public boolean getOnlyShowFloatingView() {
        return onlyShowFloatingView;
    }

    public boolean getIsZoomToLineEnabled() {
        return isZoomToLineEnabled;
    }

    public void setIsZoomToLineStatus(boolean isZoomToLineEnabled) {
        this.isZoomToLineEnabled = isZoomToLineEnabled;
        notifyPointChange();
    }

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 十字光标View
     */
    private ChartCrossView mChartCrossView;

    private KLineChartAdapter mAdapter;// 适配器
    private BaseOnChartDataObserver baseOnChartDataObserver;// 适配器观察者
    private MagnifierAutoLayout magnifierAutoLayout;

    private KLineChartFrameLayoutHandler handler;
    private KLineChartFrameLayoutThread KLineChartFrameLayoutThread;
    private IndexThread mIndexThread;// 计算指标的线程

    private int w, h;// 控件宽、高

    public Rect mainViewOffsetViewBounds = new Rect();
    private final DrawingComponent drawingComponent = new DrawingComponent(this);

    /**
     * 数据相关
     */
    private ArrayList<KLineDataValid> kLineDataList;// K线数据
    private final int DEFAULT_MAX_KLINE_WIDTH = 35,// 默认最大一根K线宽度(像素单位)
            DEFAULT_MIN_KLINE_WIDTH = 7;// 默认最小一根K线宽度(像素单位)
    private int maxKLineWidth = DEFAULT_MAX_KLINE_WIDTH,// 最大一根K线宽度(像素单位)
            minKLineWidth = DEFAULT_MIN_KLINE_WIDTH;// 最小一根K线宽度(像素单位)
    private int zoomToLineWidth = DEFAULT_MIN_KLINE_WIDTH;// 缩放到的K线宽度(像素单位)
    private float initKLineWidth = (maxKLineWidth + minKLineWidth) / 2.0f;// 初始单根K线宽度
    private int currentCount = 0;// 显示当前K线根数，大于0时
    private float kLineWidth;// 当前K线宽度(像素单位)
    private float kLineSpacing;// 相邻K线间距(像素单位)
    private int maxKLineLength;// 界面总共需要显示的条数
    private int location;// 当前滚动位置
    private float fLocation;// 当前滚动位置
    private boolean isLoading = false;// 是否正在加载更多
    public Map<Long, Pair<TradeInfo, TradeInfo>> tradeInfoMap = new HashMap<>(); // 当前显示在K线的buySell标记

    private int OFFSET = 0;// 偏移多少条不显示

    public ArrayList<KLineDataValid> getKLineDataList() {
        return kLineDataList;
    }

    /**
     * 外部按钮调节K线大小相关
     */
    private final int DEFAUKT_MAX_ZOOM_STALL = 5;// 默认最大支持档位调节
    private int maxZoomStall = DEFAUKT_MAX_ZOOM_STALL;// 最大支持档位调节
    private float stallMaxKLineWidthScale;// 最大档与最大像素比

    /**
     * 手势相关
     */
    private final int MODE_CLICK = 0,// 点击
            MODE_LONG_PRESS = 1,// 长按事件
            MODE_TO_UP = 2,// 向上滚动
            MODE_TO_DOWN = 3,// 向下滚动
            MODE_TO_LEFT = 4,// 向左滚动
            MODE_TO_RIGHT = 5,// 向右滚动
            MODE_FLING = 6,// 抛掷事件
            MODE_POINTER = 7,// 双指事件
            MODE_POINTER_END = 8;// 双指事件结束
    private int mode;// 当前手势
    private int touchSpacing;// 判断手势范围
    private StockGestureDetector baseGestureDetector;// 手势识别
    private Scroller mScroller;// 滚动器
    private float downRawX, downRawY, touchRawX, touchRawY, touchX, touchY;// 当前触摸点
    private float drawX;// 十字光标的X点
    private float drawY;// 十字光标的Y点
    private float downPointerSpacing;// 两个手指按下时的间距
    private float pointerSpacing;// 两个手指实时间距

    /**
     * 横竖屏相关
     */
    private boolean isPortraitTouchEvent = true;// 是否支持竖屏滚动、缩放事件，默认支持
    public int orientation = Configuration.ORIENTATION_PORTRAIT;// 横竖屏属性

    /**
     * 接口相关
     */
    private OnZoomListener mOnZoomListener;// 放大缩小监听
    private List<OnKLineListener> mLList = new ArrayList<>();// 接口
    private OnIndexSwitchListener onIndexSwitchListener;// 指标切换
    private View floatingWindow;// 浮窗

    /**
     * 设置相关
     */
    private float lastVibrateX = -1F;
    private float lastVibrateY = -1F;

    private Position previousPointer; // 上一次双指触摸的位置

    private Boolean isTapToShowAbstractEnabled = false; // 是否开启点击显示摘要功能

    public void setIsTapToShowAbstractEnabled(Boolean isTapToShowAbstractEnabled) {
        this.isTapToShowAbstractEnabled = isTapToShowAbstractEnabled;
    }

    public KLineChartFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public void addDrawingStatusListener(DrawingStatusListener listener) {
        drawingStatus.addDrawingStatusListener(listener);
    }

    public KLineChartFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KLineChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KLineChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public DrawingComponent getDrawingComponent() {
        return drawingComponent;
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        touchSpacing = ViewConfiguration.get(context).getScaledTouchSlop();
        executorService = Executors.newFixedThreadPool(1);
        KLineChartFrameLayoutThread = new KLineChartFrameLayoutThread();
        handler = new KLineChartFrameLayoutHandler();
        baseOnChartDataObserver = new BaseOnChartDataObserver();
        baseGestureDetector = new StockGestureDetector(context, new StockSimpleOnGestureListener());
        baseGestureDetector.setIsLongpressEnabled(false);
        mScroller = new Scroller(context);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KLineChartFrameLayout);
            maxKLineWidth = ta.getDimensionPixelSize(R.styleable.KLineChartFrameLayout_maxKLineWidth, maxKLineWidth);
            minKLineWidth = ta.getDimensionPixelSize(R.styleable.KLineChartFrameLayout_minKLineWidth, minKLineWidth);
            initKLineWidth = ta.getDimensionPixelSize(R.styleable.KLineChartFrameLayout_initKLineWidth,
                    (int) initKLineWidth);
            zoomToLineWidth = ta.getDimensionPixelSize(R.styleable.KLineChartFrameLayout_zoomToLineWidth,
                    (int) zoomToLineWidth);
            kLineWidth = initKLineWidth;
            if (kLineWidth > maxKLineWidth) {
                kLineWidth = maxKLineWidth;
            } else if (kLineWidth < minKLineWidth) {
                kLineWidth = minKLineWidth;
            }
            dateType = ta.getInt(R.styleable.KLineChartFrameLayout_dateType, dateType);
            maxZoomStall = ta.getInteger(R.styleable.KLineChartFrameLayout_maxZoomStall, maxZoomStall);
            if (maxZoomStall < 2) {
                maxZoomStall = 2;
            } else if (maxZoomStall > 10) {
                maxZoomStall = 10;
            }
            stallMaxKLineWidthScale = (maxKLineWidth - minKLineWidth) / (maxZoomStall * 1.0f);
            isPortraitTouchEvent = ta.getBoolean(R.styleable.KLineChartFrameLayout_isPortraitTouchEvent, isPortraitTouchEvent);
            OFFSET = ta.getInt(R.styleable.KLineChartFrameLayout_offsetNum, OFFSET);
            ta.recycle();
        }
        kLineSpacing = getkLineSpacing(kLineWidth);
        mChartCrossView = new ChartCrossView(context);
        mChartCrossView.setTimeChartFrameLayout(this);
        mChartCrossView.setVisibility(View.GONE);
        mChartCrossView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mChartCrossView.setCrossColor(crossColor);
        mChartCrossView.setDotColor(dotColor);
        mChartCrossView.setDrawDot(isDrawDot);
        mChartCrossView.setDrawMainLine(isDrawMainLine);
        mChartCrossView.setLineWidth(lineWidthCross);
    }

    public int getZoomToLineWidth() {
        return zoomToLineWidth;
    }

    @Override
    public void onResume() {
        isDestroy = false;
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(1);
        }
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.registerObserver(baseOnChartDataObserver);
            if (kLineDataList == null || kLineDataList.size() == 0) {
                baseOnChartDataObserver.onChartData();
            }
        }
        for (int i = 0; i < chartViewList.size(); i++) {
            if (chartViewList.get(i) instanceof KLineChartMainView) {
                KLineChartMainView view = (KLineChartMainView) chartViewList.get(i);
                view.onResume();
            } else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                ((KLineChartDeputyView) chartViewList.get(i)).onResume();
            }
        }
    }


    @Override
    public void onPause() {
        isDestroy = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (BaseThread.isAlive(mIndexThread)) {
            mIndexThread.onStop();
        }
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.unRegisterObserver(baseOnChartDataObserver);
        }
        if (executorService != null && !executorService.isShutdown()) {
            try {
                executorService.shutdown();
                executorService = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < chartViewList.size(); i++) {
            if (chartViewList.get(i) instanceof KLineChartMainView) {
                ((KLineChartMainView) chartViewList.get(i)).onPause();
            } else if (chartViewList.get(i) instanceof KLineChartDeputyView) {
                ((KLineChartDeputyView) chartViewList.get(i)).onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        isDestroy = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (KLineChartFrameLayoutThread != null) {
            KLineChartFrameLayoutThread = null;
        }
        if (mIndexThread != null) {
            mIndexThread.onStop();
            mIndexThread = null;
        }
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.unRegisterObserver(baseOnChartDataObserver);
            baseOnChartDataObserver = null;
        }
        if (chartViewList != null) {
            chartViewList.clear();
        }
        if (mLList != null) {
            mLList.clear();
        }
        for (int i = 0; i < chartViewList.size(); i++) {
            if (chartViewList.get(i) instanceof KLineChartMainView) {
                ((KLineChartMainView) chartViewList.get(i)).onDestroy();
            } else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                ((KLineChartDeputyView) chartViewList.get(i)).onDestroy();
            }
        }
    }

    @Override
    public void build() {
        for (int i = 0; i < chartViewList.size(); i++) {
            View v = chartViewList.get(i);
            if (v instanceof KLineChartMainView) {
                ((KLineChartMainView) v).build();
            } else if (v instanceof KLineChartDeputyView) {
                ((KLineChartDeputyView) v).build();
            }
        }
    }

    public void notifyPointChange() {
        for (int i = 0; i < chartViewList.size(); i++) {
            View v = chartViewList.get(i);
            if (v instanceof KLineChartMainView) {
                ((KLineChartMainView) v).setData(kLineDataList);
            }
        }
    }

    // todo
    public Offset getRealPosition(Position position) {
        // 三种情况
        // 1. 最左边的情况
        // 2. 最右边的情况
        // 3. 中间的情况
        float topRate = 0.0f;
        float bottomRate = 0.0f;

        for (int index = 0; index < chartViewList.size(); index++) {
            View view = chartViewList.get(index);
            if (view instanceof KLineChartMainView) {
                KLineChartMainView kLineChartMainView = (KLineChartMainView) view;
                    topRate = kLineChartMainView.getTopRate();
                    bottomRate = kLineChartMainView.getBottomRate();
                    break;
            }
        }

        int startPosition = location;
        int endPosition = location + maxKLineLength - 1;

        double lowPrice = Integer.MAX_VALUE;
        double highPrice = 0;

        Log.d("getRealPosition", "startPosition = " + startPosition + " endPosition = " + endPosition);
        // 需要获取startTimestamp, endTimeStamp, lowPrice, highPrice
        for (int index = startPosition; index <= endPosition; index++) {
            if (index < 0) {
                continue;
            } else if (index >= kLineDataList.size()) {
                break;
            }

            KLineData kLineData = kLineDataList.get(index);
            if (kLineData.getLow() < lowPrice) {
                lowPrice = kLineData.getLow();
            }
            if (kLineData.getHig() > highPrice) {
                highPrice = kLineData.getHig();
            }
        }

        Log.d("getRealPosition", "lowPrice = " + lowPrice + " highPrice = " + highPrice);

        long timeStampGap = 0;
        if (kLineDataList.size() >= 2) {
            timeStampGap = kLineDataList.get(1).getTime() - kLineDataList.get(0).getTime();
        }
        Log.d("getRealPosition", "timeStampGap = " + timeStampGap);
        long startTimeStamp = 0;
        long endTimeStamp = 0;

        if (startPosition >= 0) {
            startTimeStamp = kLineDataList.get(startPosition).getTime();
        } else {
            startTimeStamp = kLineDataList.get(0).getTime() - timeStampGap * Math.abs(startPosition);
        }

        if (endPosition < kLineDataList.size()) {
            endTimeStamp = kLineDataList.get(endPosition).getTime();
        } else {
            endTimeStamp = kLineDataList.get(kLineDataList.size() - 1).getTime() + timeStampGap * Math.abs(kLineDataList.size() - endPosition);
        }

        Log.d("getRealPosition", "startTimeStamp = " + startTimeStamp + " endTimeStamp = " + endTimeStamp);
        Log.d("getRealPosition", "top of mainView is  = " + mainViewOffsetViewBounds.top +
                " bottom of mainView is " + mainViewOffsetViewBounds.bottom +
                " left of mainView is " + mainViewOffsetViewBounds.left +
                " right of mainView is " + mainViewOffsetViewBounds.right);
//        double scale = (mainViewOffsetViewBounds.bottom - mainViewOffsetViewBounds.top) / (highPrice - lowPrice);
//        float price = (mainViewOffsetViewBounds.bottom - mainViewOffsetViewBounds.top - position.getY()) / (float) scale + (float) lowPrice;
        float realHeight = (mainViewOffsetViewBounds.bottom - mainViewOffsetViewBounds.top) * (1 - topRate - bottomRate);
        Log.d("realheight", "realHeight = " + realHeight);
        float pricePerY = (float) (highPrice - lowPrice) / realHeight;
        float lowPositionY = (1 - bottomRate) * (mainViewOffsetViewBounds.bottom - mainViewOffsetViewBounds.top);
        float price = (lowPositionY - position.getY()) * pricePerY + (float) lowPrice;
        float timeStamp = startTimeStamp + timeStampGap * (position.getX() - mainViewOffsetViewBounds.left) / (kLineWidth + kLineSpacing);

        Log.d("getRealPosition", "timeStamp = " + timeStamp + " price = " + price);

        return new Offset((long)timeStamp,price);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.orientation = newConfig.orientation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (w != getWidth()
                || h != getHeight()) {
            w = getWidth();
            h = getHeight();
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            hideCrossView();

            // 只要此布局发生变化，重新初始化K线宽度、间距、最大显示条数、当前第一根K线的下标位置
            if (currentCount > 0) {
                float ksWidth = ((float) w - strokeWidth * 2f) / (float) currentCount;
                float kLineWidth = ksWidth / 4f * 3f;
                initKLineWidth = kLineWidth;
                if (initKLineWidth > maxKLineWidth) {
                    maxKLineWidth = (int) initKLineWidth + 1;
                } else if (initKLineWidth < minKLineWidth) {
                    minKLineWidth = (int) initKLineWidth;
                }
            }

            kLineWidth = initKLineWidth;
            kLineSpacing = getkLineSpacing(kLineWidth);
            maxKLineLength = getMaxLength(kLineWidth, kLineSpacing);
            if (kLineDataList == null || kLineDataList.size() <= 0) {
                location = 0;
            } else {
//                location = kLineDataList.size() - maxKLineLength;
                if (OFFSET > 0) {
                    if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                        location = kLineDataList.size();
                    } else if (location < OFFSET) {
                        location = OFFSET;
                    }
                } else {
//                    if (location < 0) {
//                        location = 0;
//                    }

                    if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                        location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                        fLocation = location * (kLineWidth + kLineSpacing);
                    }
                }
            }
            if (mOnZoomListener != null) {
                if (kLineWidth <= minKLineWidth) {
                    mOnZoomListener.onZoomListener(false, true);
                } else if (kLineWidth >= maxKLineWidth) {
                    mOnZoomListener.onZoomListener(true, false);
                } else {
                    mOnZoomListener.onZoomListener(false, false);
                }
            }
        }
        super.onDraw(canvas);
        if (chartViewList.size() == 0) {
            addKLineChartView(this);
            mChartCrossView.setChartList(chartViewList);
            if (mChartCrossView.getParent() != null &&
                    (mChartCrossView.getParent() instanceof KLineChartFrameLayout)) {
                removeView(mChartCrossView);
            }
            addView(mChartCrossView);
            isViewsAddAll = true;
        }

        /**
         * 绘制边框
         */
        drawStroke(canvas);

        /**
         * 绘制经线
         */
        drawLng(canvas);

        /**
         * 绘制纬线
         */
        drawLat(canvas);

        /**
         * 绘制时间
         */
        drawTime(canvas);
    }

    /**
     * 将K线主、副图添加到集合
     *
     * @param view
     */
    private void addKLineChartView(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof KLineChartMainView) {
            chartViewList.add(view);

//            view.getDrawingRect(mainViewOffsetViewBounds);
////            view.getHitRect(mainViewOffsetViewBounds);
//            offsetDescendantRectToMyCoords(view, mainViewOffsetViewBounds);
//            Log.d("JamesDebug", "top is " + mainViewOffsetViewBounds.top +
//                    ", left is " + mainViewOffsetViewBounds.left +
//                    ", bottom is " + mainViewOffsetViewBounds.bottom +
//                    ", right is " + mainViewOffsetViewBounds.right);
            ((KLineChartMainView) view).setKLineChartFrameLayout(this);
        } else if (view instanceof KLineChartDeputyView) {
            chartViewList.add(view);
            ((KLineChartDeputyView) view).setKLineChartFrameLayout(this);
        } else if (view instanceof KlineLinearLayout) {
            chartViewList.add(view);
        }
        else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                addKLineChartView(vg.getChildAt(i));
            }
        }
    }

    /**
     * 绘制边框
     */
    private void drawStroke(Canvas canvas) {
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(strokeColor);
        float sw2 = strokeWidth / 2.0f;
        int tts2 = (int) (timeTextSize * 2.0f);
        int mainViewTop = 0;
        boolean isSetBottomMargin = false;
        if (mTimeLocation == TimeLocation.NONE) {
            canvas.drawRect(sw2, sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2, mPaint);
        } else if (mTimeLocation == TimeLocation.BOTOOM) {
            if (chartViewList.size() > 0) {
                for (int i = chartViewList.size() - 1; i >= 0; i--) {
                    View kLineChartLast = chartViewList.get(i);
                    if (kLineChartLast.getParent() != null
                            && kLineChartLast.getParent() instanceof ChartLinearLayout) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) kLineChartLast.getLayoutParams();
                        if (kLineChartLast.getVisibility() == View.VISIBLE
                                && !isSetBottomMargin) {
                            isSetBottomMargin = true;
                            if (lp.bottomMargin != tts2) {
                                lp.bottomMargin = tts2;
                                kLineChartLast.setLayoutParams(lp);
                            }
                        } else {
                            if (lp.bottomMargin != 0) {
                                lp.bottomMargin = 0;
                                kLineChartLast.setLayoutParams(lp);
                            }
                        }
                    } else {
                        throw new IllegalStateException("K线主、副图父布局必须是ChartLinearLayout");
                    }
                }
                mainViewTop = chartViewList.get(0).getTop();
            }
            canvas.drawRect(sw2,
                    sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2 - tts2, mPaint);
        } else {
            if (chartViewList.size() > 0) {
                View kLineChartMain = chartViewList.get(0);
                if (kLineChartMain.getParent() != null
                        && kLineChartMain.getParent() instanceof ChartLinearLayout) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) kLineChartMain.getLayoutParams();
                    if (lp.bottomMargin != tts2) {
                        lp.bottomMargin = tts2;
                        kLineChartMain.setLayoutParams(lp);
                    }
                } else {
                    throw new IllegalStateException("K线主、副图父布局必须是ChartLinearLayout");
                }
                if (chartViewList.size() > 1) {
                    for (int i = 1; i < chartViewList.size(); i++) {
                        View kLineChartLast = chartViewList.get(i);
                        if (kLineChartLast.getParent() != null
                                && kLineChartLast.getParent() instanceof ChartLinearLayout) {
                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) kLineChartLast.getLayoutParams();
                            if (lp.bottomMargin != 0) {
                                lp.bottomMargin = 0;
                                kLineChartLast.setLayoutParams(lp);
                            }
                        } else {
                            throw new IllegalStateException("K线主、副图父布局必须是ChartLinearLayout");
                        }
                    }
                }
                mainViewTop = chartViewList.get(0).getTop();
            }
            canvas.drawRect(sw2, sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2, mPaint);
        }
    }

    /**
     * 绘制经线
     *
     * @param canvas
     */
    private void drawLng(Canvas canvas) {
        mTextPaint.setColor(timeTextColor);
        mTextPaint.setTextSize(timeTextSize);
        mPaint.setColor(lineColor);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        float sw2 = strokeWidth * 2.0f;
        float scale = (width - sw2) / 4.0f;
        float x;
        for (int i = 0; i <= 4; i++) {
            x = strokeWidth + scale * i;
            for (int j = 0; j < chartViewList.size(); j++) {
                View view = chartViewList.get(j);
                if (view.getVisibility() != View.VISIBLE) {
                    continue;
                }
                Rect mRect = new Rect();
                view.getHitRect(mRect);
                canvas.drawLine(x, mRect.top, x, mRect.bottom, mPaint);
            }
        }
    }

    /**
     * 绘制纬线
     *
     * @param canvas
     */
    private void drawLat(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);
        Rect mRect = new Rect();
        for (int i = 0; i < chartViewList.size(); i++) {
            View view = chartViewList.get(i);
            if (view.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (view.getParent() == null ||
                    !(view.getParent() instanceof ChartLinearLayout)) {
                continue;
            }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            view.getHitRect(mRect);
            float height = mRect.bottom - mRect.top;
            float y = mRect.top;
            if (chartViewList.get(i) instanceof KLineChartMainView) {
                canvas.drawLine(0, y, getWidth(), y, mPaint);
                float scale = height / 4.0f;
                for (int j = 0; j < 4; j++) {
                    y += scale;
                    if (mTimeLocation == TimeLocation.BOTOOM) {
                        if (y != (getHeight() - lp.bottomMargin)) {
                            canvas.drawLine(0, y, getWidth(), y, mPaint);
                        }
                    } else {
                        canvas.drawLine(0, y, getWidth(), y, mPaint);
                    }
                }
            } else {
                float scale = height / 2.0f;
                for (int j = 0; j < 2; j++) {
                    if (i == 0 && j == 0) {
                        y += scale;
                        continue;
                    }
                    canvas.drawLine(0, y, getWidth(), y, mPaint);
                    y += scale;
                    if (mTimeLocation == TimeLocation.BOTOOM) {
                        if (mRect.bottom != (getHeight() - lp.bottomMargin)
                                && j == 1) {
                            canvas.drawLine(0, mRect.bottom, getWidth(), mRect.bottom, mPaint);
                        }
                    } else {
                        if (mRect.bottom != getHeight()
                                && j == 1) {
                            canvas.drawLine(0, mRect.bottom, getWidth(), mRect.bottom, mPaint);
                        }
                    }
                }
            }
        }
    }

    /**
     * 绘制时间
     *
     * @param canvas
     */
    private void drawTime(Canvas canvas) {
        if (mTimeLocation == TimeLocation.NONE) {
            return;
        }
        if (kLineDataList != null
                && kLineDataList.size() > location) {
            int endLocation = location + maxKLineLength - 1;
            if (kLineDataList.size() <= endLocation || endLocation < 0) {
                endLocation = kLineDataList.size() - 1;
            }



            String startTime = utils.date2String("yyyy/MM/dd HH:mm", kLineDataList.get(0).getTime());
            int middleLocation = endLocation / 2;
            if (location > 0) {
                startTime = utils.date2String("yyyy/MM/dd HH:mm", kLineDataList.get(location).getTime());
                middleLocation = (location + endLocation) / 2;
            }
            String middleTime = utils.date2String("yyyy/MM/dd HH:mm", kLineDataList.get(
                    middleLocation).getTime());
            String endTime = utils.date2String("yyyy/MM/dd HH:mm", kLineDataList.get(
                    endLocation).getTime());
            if (dateType == DRAW_DATE) {
                if (!startTime.isEmpty()) {
                    startTime = startTime.substring(0, 10);
                }
                middleTime = middleTime.substring(0, 10);
                endTime = endTime.substring(0, 10);
            } else if (dateType == DRAW_TIME) {
                if (!startTime.isEmpty()) {
                    startTime = startTime.substring(11, 16);
                }
                middleTime = middleTime.substring(11, 16);
                endTime = endTime.substring(11, 16);
            }
            mTextPaint.setColor(timeTextColor);
            mTextPaint.setTextSize(timeTextSize);
            float sw2 = strokeWidth * 2.0f;
            float textY;
            if (mTimeLocation == TimeLocation.BOTOOM) {
                textY = height - timeTextSize / 2f;
            } else {
                textY = height / 2.0f - timeTextSize / 2f;
                if (chartViewList.size() > 0) {
                    View timeChart0 = chartViewList.get(0);
                    Rect rect = new Rect();
                    timeChart0.getHitRect(rect);
                    if (timeChart0 != null) {
                        textY = rect.bottom + timeTextSize / 2 + timeTextSize;
                    }
                }
            }
            canvas.drawText(startTime, sw2,
                    textY, mTextPaint);
            canvas.drawText(middleTime, width / 2.0f - mTextPaint.measureText(middleTime) / 2.0f,
                    textY, mTextPaint);
            canvas.drawText(endTime,
                    width - sw2 - mTextPaint.measureText(endTime),
                    textY, mTextPaint);
        }
    }

    /**
     * 获取相邻两条K线间距
     *
     * @param kLineWidth
     * @return
     */
    private float getkLineSpacing(float kLineWidth) {
//        float kLineWidth = ksWidth / 4f * 3f;
        float kLineSpacing = kLineWidth / 3f;
//        if (kLineSpacing < 3) {
//            kLineSpacing = 3;
//        }
        return kLineSpacing;
    }

    /**
     * 获取当前设置参数的情况下算出的总共绘制多少数据长度
     *
     * @param kLineWidth   一根K线宽度
     * @param kLineSpacing 相邻K线距离
     * @return
     */
    private int getMaxLength(float kLineWidth, float kLineSpacing) {
        float maxLength = 0;
        float viewWidth = getWidth() - strokeWidth * 2.0f;
        maxLength = viewWidth / (kLineWidth + kLineSpacing);
        float ml = maxLength - ((int) maxLength);
        int maxCount = ((int) maxLength);
        if ((kLineWidth + kLineSpacing) * ml >= kLineWidth) {
            maxCount += 1;
        }
        return maxCount;
    }

    /**
     * 设置单根K线当前的宽度
     *
     * @param kLineWidth
     */
    private void setKLineWidth(float kLineWidth) {
        this.kLineWidth = kLineWidth;
        kLineSpacing = getkLineSpacing(kLineWidth);
        maxKLineLength = getMaxLength(kLineWidth, kLineSpacing);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (kLineDataList != null) {
//                location = kLineDataList.size() - maxKLineLength;
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (kLineDataList != null) {
                if (kLineDataList.size() - location < maxKLineLength) {
//                    location = kLineDataList.size() - maxKLineLength;
                }
            }
        }
        if (OFFSET > 0) {
            if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                location = kLineDataList.size();
            } else if (location < OFFSET) {
                location = OFFSET;
            }
        } else {
            if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                fLocation = location * (kLineWidth + kLineSpacing);
            }
        }
        for (int i = 0; i < chartViewList.size(); i++) {
            if (chartViewList.get(i) instanceof KLineChartMainView)
                ((KLineChartMainView) chartViewList.get(i)).setData(kLineDataList);
            else if (chartViewList.get(i) instanceof KLineChartDeputyView) {
                ((KLineChartDeputyView) chartViewList.get(i)).setData(kLineDataList);
            }
        }
        postInvalidate();
    }

    private void vibrateWhenTouch(float x, float y) {
        boolean isVibrationOpen = LocalStore.getInstance().getBoolean(KEY_KLINE_TOUCH_VIBRATION, false);
        if (isVibrationOpen) {
            if (lastVibrateX == -1 || (x != lastVibrateX || y != lastVibrateY)) {
                makeVibrate(getContext(), 20L);
                lastVibrateX = x;
                lastVibrateY = y;
            }
        }
    }


    /**
     * 重新计算指标
     */
    public void updateIndex() {
        sendHandler(MSG_RESTART_INDEX_THREAD, null, null, null);
    }

    /**
     * 设置浮窗
     *
     * @param view
     */
    public void setFloatingWindow(View view) {
        if (view == null || this.floatingWindow == view) {
            return;
        } else {
            this.floatingWindow = view;
            removeView(floatingWindow);
            addView(floatingWindow);
            if (view instanceof OnKLineListener) {
                mLList.add((OnKLineListener) view);
            }
        }
    }

    /**
     * 添加接口
     *
     * @param l
     */
    public void addOnKLineListener(OnKLineListener l) {
        if (l != null && mLList.indexOf(l) < 0) {
            mLList.add(l);
        }
    }

    /**
     * 移除
     *
     * @param l
     */
    public void removeOnKLineListener(OnKLineListener l) {
        if (l != null && mLList.indexOf(l) > -1) {
            mLList.remove(l);
        }
    }

    /**
     * 清除所有回调
     */
    public void cleanOnKLineListener() {
        mLList.clear();
    }

    /**
     * 设置指标切换接口
     *
     * @param l
     */
    public void setOnIndexSwitchListener(OnIndexSwitchListener l) {
        this.onIndexSwitchListener = l;
    }

    /**
     * 设置放大缩小监听接口
     *
     * @param l
     */
    public void setOnZoomListener(OnZoomListener l) {
        this.mOnZoomListener = l;
    }

    /**
     * 设置是否绘制日期还是时间
     *
     * @param dateType
     */
    public void setDateType(int dateType) {
        this.dateType = dateType;
        postInvalidate();
    }

    /**
     * 是否绘制日期还是时间
     *
     * @return
     */
    public int getDateType() {
        return dateType;
    }

    /**
     * 设置当前K线显示根数
     *
     * @param currentCount
     */
    public void setCurrentCount(int currentCount) {
        if (currentCount > 0 && currentCount != this.currentCount) {
            this.currentCount = currentCount;
            if (w > 0 && h > 0) {
                float ksWidth = ((float) w - strokeWidth * 2f) / (float) currentCount;
                float kLineWidth = ksWidth / 4f * 3f;
                initKLineWidth = kLineWidth;
                if (initKLineWidth > maxKLineWidth) {
                    maxKLineWidth = (int) initKLineWidth + 1;
                } else if (initKLineWidth < minKLineWidth) {
                    minKLineWidth = (int) initKLineWidth;
                }
                setKLineWidth(kLineWidth);
            }
        }
    }

    /**
     * 设置K线宽度
     *
     * @param kLineWidth
     */
    public void setKLineWidth(int kLineWidth) {
        initKLineWidth = kLineWidth;
        if (initKLineWidth > maxKLineWidth) {
            maxKLineWidth = (int) initKLineWidth + 1;
        } else if (initKLineWidth < minKLineWidth) {
            minKLineWidth = (int) initKLineWidth;
        }
        setKLineWidth((float) kLineWidth);
    }

    /**
     * 获取当前K线根数
     *
     * @return
     */
    public int getCurrentCount() {
        return currentCount;
    }

    /**
     * 放大
     *
     * @return false不可以再放大了，true可以放大
     */
    public boolean zoomLarge() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int stall = parse.parseInt(String.format(Locale.ENGLISH,"%.0f",
                (kLineWidth - minKLineWidth) / stallMaxKLineWidthScale));
        if (stall >= maxZoomStall) {
            if (kLineWidth < maxKLineWidth) {
                setKLineWidth(maxKLineWidth);
            }
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(true, false);
            }
            return false;
        }
        if (stall + 1 >= maxZoomStall) {
            if (kLineWidth < maxKLineWidth) {
                setKLineWidth(maxKLineWidth);
            }
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(true, false);
            }
            return false;
        } else {
            setKLineWidth((int) ((stall + 1) * stallMaxKLineWidthScale) + minKLineWidth);
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(false, false);
            }
        }
        return true;
    }

    /**
     * 缩小
     *
     * @return false不可以再缩小了，true可以缩小
     */
    public boolean zoomSmall() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int stall = parse.parseInt(String.format(Locale.ENGLISH,"%.0f",
                (kLineWidth - minKLineWidth) / stallMaxKLineWidthScale));
        if (stall <= 0) {
            if (kLineWidth > minKLineWidth) {
                setKLineWidth(minKLineWidth);
            }
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(false, true);
            }
            return false;
        }
        if (stall - 1 <= 0) {
            if (kLineWidth > minKLineWidth) {
                setKLineWidth(minKLineWidth);
            }
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(false, true);
            }
            return false;
        } else {
            setKLineWidth(((stall - 1) * stallMaxKLineWidthScale) + minKLineWidth);
            if (mOnZoomListener != null) {
                mOnZoomListener.onZoomListener(false, false);
            }
        }
        return true;
    }

    /**
     * 设置适配器
     *
     * @param mAdapter
     */
    public void setAdapter(KLineChartAdapter mAdapter) {
        if (this.mAdapter != null && baseOnChartDataObserver != null) {
            this.mAdapter.unRegisterObserver(baseOnChartDataObserver);
        }
        this.mAdapter = mAdapter;
        if (this.mAdapter != null && baseOnChartDataObserver != null) {
            this.mAdapter.registerObserver(baseOnChartDataObserver);
        }
        if (baseOnChartDataObserver != null)
            baseOnChartDataObserver.onChartData();
    }

    public void setSymbolAndFlagTime(String symbol, String flagTime) {
        this.symbol = symbol;
        this.flagTime = flagTime;
        hideCrossView();
        drawingStatus.readDataFromCache(symbol, flagTime);
        notifyPointChange();
    }


    public String getSymbol() {
        return symbol;
    }

    public String getFlagTime() {
        return flagTime;
    }

    public void setMagnifierLayout(MagnifierAutoLayout magnifierLayout) {
        this.magnifierAutoLayout = magnifierLayout;
        magnifierAutoLayout.setMainChartFrameLayout(this);

        MagnifierBuilder magnifierBuilder = new MagnifierBuilder(getContext())
                .widthMagnifierRadius(utils.dp2px(getContext(), 50))
                .widthMagnifierScaleRate(1f)
                .widthMagnifierShouldAutoMoveMagnifier(true)
                .widthMagnifierStrokeWidth(utils.dp2px(getContext(), 1))
                .widthMagnifierLeftSpace(utils.dp2px(getContext(), 10))
                .widthMagnifierTopSpace(utils.dp2px(getContext(), 100));
        magnifierBuilder.setRealTopSpace(utils.dp2px(getContext(), 10));

        magnifierAutoLayout.setMagnifierBuilder(magnifierBuilder);
    }

    /**
     * 获取适配器
     *
     * @return
     */
    public ChartAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 设置滚动位置
     *
     * @param position
     */
    public void setPosition(int position) {
        if (getPosition() == position) {
            return;
        }
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int location = position;
        if (kLineDataList != null) {
//            if (position > kLineDataList.size() - maxKLineLength) {
//                location = position - kLineDataList.size() - maxKLineLength;
//            }
        } else {
            location = 0;
        }
        if (OFFSET > 0) {
            if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                location = kLineDataList.size();
            } else if (location < OFFSET) {
                location = OFFSET;
            }
        } else {
            if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                fLocation = location * (kLineWidth + kLineSpacing);
            }
        }
        this.location = location;
        for (int i = 0; i < chartViewList.size(); i++) {
            if (chartViewList.get(i) instanceof KLineChartMainView)
                ((KLineChartMainView) chartViewList.get(i)).setData(kLineDataList);
            else if (chartViewList.get(i) instanceof KLineChartDeputyView) {
                ((KLineChartDeputyView) chartViewList.get(i)).setData(kLineDataList);
            }
        }
    }

    /**
     * 设置是否偏移
     *
     * @param offset
     */
    public void setOffset(int offset) {
        this.OFFSET = offset;
    }

    /**
     * 获取单根K线宽度
     *
     * @return
     */
    public float getkLineWidth() {
        return kLineWidth;
    }

    /**
     * 获取相邻K线间距
     *
     * @return
     */
    public float getkLineSpacing() {
        return kLineSpacing;
    }

    /**
     * 获取界面显示最大K线根数
     *
     * @return
     */
    public int getMaxKLineLength() {
        return maxKLineLength;
    }

    /**
     * 获取当前界面显示的第一根K线的元素下标
     *
     * @return
     */
    public int getLocation() {
        return location;
    }

    /**
     * 获取当前界面显示的第一根K线的元素下标
     *
     * @return
     */
    public int getPosition() {
        return getLocation();
    }

    /**
     * 获取当前界面最后一根K线下标
     *
     * @return
     */
    public int getEndPosition() {
        int endLocation = location + maxKLineLength - 1;
        if (kLineDataList == null) {
            endLocation = -1;
        } else if (kLineDataList.size() <= endLocation) {
            endLocation = kLineDataList.size() - 1;
        }
        return endLocation;
    }

    /**
     * 观察者
     */
    class BaseOnChartDataObserver implements OnChartDataObserver {

        @Override
        public void onChartData() {
            if (!BaseThread.isAlive(KLineChartFrameLayoutThread)) {
                if (BaseThread.isAlive(mIndexThread)) {
                    mIndexThread.onStop();
                }
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.execute(KLineChartFrameLayoutThread);
                }
            }
        }
    }

    /**
     * 计算线程
     */
    class KLineChartFrameLayoutThread extends BaseThread {

        @Override
        public void running() {
            do {
                if (isDestroy) {
                    return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (BaseThread.isAlive(mIndexThread)) {
                    mIndexThread.onStop();
                }
                if (mAdapter == null || mAdapter.getCount() == 0) {
                    sendHandler(MSG_OK, null, null, null);
                    return;
                }
            } while (!isViewsAddAll);

            int count = mAdapter.getCount();
            ArrayList<KLineDataValid> kLineDataList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (isDestroy) {
                    return;
                }
                if (mAdapter == null) {
                    sendHandler(MSG_OK, null, null, null);
                    return;
                }
                KLineData item = mAdapter.getData(i);
                if (item == null) {
                    sendHandler(MSG_OK, null, null, null);
                    return;
                }
                String id = item.getId();
                long time = item.getTime();
                double hig = item.getHig();
                double open = item.getOpen();
                double low = item.getLow();
                double close = item.getClose();
                double vol = item.getVol();
                double cje = item.getCje();
                KLineDataValid newItem = new KLineDataValid(id, time, hig, open, low, close, vol, cje);
                newItem.setBuyAndSell(item.getBuyAndSell());
                kLineDataList.add(newItem);
            }
            if (BaseThread.isAlive(mIndexThread)) {
                mIndexThread.onStop();
            }
            mIndexThread = new IndexThread(kLineDataList);
            if (executorService != null && !executorService.isShutdown()) {
                try {
                    executorService.execute(mIndexThread);
                } catch (RejectedExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 计算指标的线程
     */
    class IndexThread extends BaseThread {

        private boolean isStop = false;
        private ArrayList<KLineDataValid> kLineDataList;
        private IndexCalculation mIndexCalculation = new IndexCalculation();

        public IndexThread(ArrayList<KLineDataValid> kLineDataList) {
            this.kLineDataList = kLineDataList;
        }

        @Override
        public void running() {
            try {
                if (kLineDataList == null || kLineDataList.size() == 0) {
                    sendHandler(MSG_OK, null, null, null);
                    return;
                }
                KLineChartMainView.KLineMainIndex mainIndex = null;
                List<KLineChartDeputyView.KLineDeputyIndex> deputyIndexs = new ArrayList<>();
                for (int i = 0; i < chartViewList.size(); i++) {
                    if (isStop) {
                        return;
                    }
                    if (chartViewList.get(i) instanceof KLineChartMainView) {
                        KLineChartMainView mainView = (KLineChartMainView) chartViewList.get(i);
                        mainIndex = mainView.getNewIndex();
                        if (mainIndex == KLineChartMainView.KLineMainIndex.NONE) {

                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.MA ||
                                mainIndex == KLineChartMainView.KLineMainIndex.GAME) {
                            kLineDataList = mIndexCalculation.calculationMainMa(kLineDataList, mainView.getNewParameter());
                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.EMA) {
                            kLineDataList = mIndexCalculation.calculationMainEMA(kLineDataList, mainView.getNewParameter());
                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.BOLL) {
                            kLineDataList = mIndexCalculation.calculationBOLL(kLineDataList, mainView.getNewParameter());
                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.RETURN_RATE) {
                            kLineDataList = mIndexCalculation.calculationReturnRate(kLineDataList);
                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.SAR) {
                            kLineDataList = mIndexCalculation.calculationSAR(kLineDataList, mainView.getNewParameter());
                        } else if (mainIndex == KLineChartMainView.KLineMainIndex.BOLL_POINT) {
                            kLineDataList = mIndexCalculation.calculationBOLL(kLineDataList, mainView.getNewParameter());
                        }
                        if (kLineDataList == null) {
                            sendHandler(MSG_OK, null, null, null);
                            return;
                        }
                    } else if(chartViewList.get(i) instanceof KLineChartDeputyView){
                        KLineChartDeputyView deputyView = (KLineChartDeputyView) chartViewList.get(i);
                        KLineChartDeputyView.KLineDeputyIndex deputyIndex = deputyView.getNewIndex();
                        if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.VOL) {
                            kLineDataList = mIndexCalculation.calculationVol(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.MACD) {
                            kLineDataList = mIndexCalculation.calculationMACD(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.KDJ) {
                            kLineDataList = mIndexCalculation.calculationKDJ(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.RSI) {
                            kLineDataList = mIndexCalculation.calculationRSI(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.BIAS) {
                            kLineDataList = mIndexCalculation.calculationBIAS(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.ARBR) {
                            kLineDataList = mIndexCalculation.calculationBRAR(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.CCI) {
                            kLineDataList = mIndexCalculation.calculationCCI(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.DMI) {
                            kLineDataList = mIndexCalculation.calculationDMI(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.CR) {
                            kLineDataList = mIndexCalculation.calculationCR(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.PSY) {
                            kLineDataList = mIndexCalculation.calculationPSY(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.DMA) {
                            kLineDataList = mIndexCalculation.calculationDMA(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.TRIX) {
                            kLineDataList = mIndexCalculation.calculationTRIX(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.KDJ_BS) {
                            kLineDataList = mIndexCalculation.calculationKDJ_BS(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.RSI_BS) {
                            kLineDataList = mIndexCalculation.calculationRSI_BS(kLineDataList, deputyView.getNewParameter());
                        } else if (deputyIndex == KLineChartDeputyView.KLineDeputyIndex.MACD_BS) {
                            kLineDataList = mIndexCalculation.calculationMACD_BS(kLineDataList, deputyView.getNewParameter());
                        }
                        deputyIndexs.add(deputyIndex);
                    }
                }
                if (isStop) {
                    return;
                }
                if (kLineDataList == null) {
                    sendHandler(MSG_OK, null, null, null);
                    return;
                }
                sendHandler(MSG_OK, kLineDataList, mainIndex, deputyIndexs);
            } catch (Exception e) {
                e.printStackTrace();
                sendHandler(MSG_OK, null, null, null);
            }
        }

        /**
         * 终止线程
         */
        public void onStop() {
            isStop = true;
            mIndexCalculation.onStop();
        }
    }

    /**
     * 发消息到Handler
     *
     * @param what
     * @param obj
     */
    private void sendHandler(int what, Object obj, KLineChartMainView.KLineMainIndex mainIndex, List<KLineChartDeputyView.KLineDeputyIndex> deputyIndexs) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            Bundle bundle = new Bundle();
            msg.obj = obj;
            bundle.putSerializable("mainIndex", mainIndex);
            bundle.putSerializable("deputyIndexs", (Serializable) deputyIndexs);
            msg.setData(bundle);
            if (handler != null)
                handler.sendMessage(msg);
        }
    }

    /**
     * 消息队列
     */
    class KLineChartFrameLayoutHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_OK:
                    ArrayList<KLineDataValid> list = (ArrayList<KLineDataValid>) msg.obj;

                    if (list == null || list.isEmpty()) {
                        return;
                    }

//                    if (BaseThread.isAlive(mIndexThread)) {
//                        mIndexThread.onStop();
//                        return;
//                    }
                    Bundle bundle = msg.getData();
                    KLineChartMainView.KLineMainIndex mainIndex = (KLineChartMainView.KLineMainIndex) bundle.getSerializable("mainIndex");
                    List<KLineChartDeputyView.KLineDeputyIndex> deputyIndexs = (List<KLineChartDeputyView.KLineDeputyIndex>) bundle.getSerializable("deputyIndexs");
                    int pos = 0;
                    if (mAdapter != null) {
                        pos = mAdapter.getLocation();
                    }
                    if (pos > -1) {
                        location = pos;
                        if (mAdapter != null) {
                            mAdapter.setLocation(-1);
                        }
                    }
                    if (kLineDataList == null || kLineDataList.size() <= 0) {
                        if (list == null || list.size() <= 0) {
                            location = 0;
                        } else {
                            location = list.size() - (maxKLineLength * 3 / 4);
                        }
                    } else {
                        if (list == null || list.size() <= 0) {
                            location = 0;
                        } else {
                            int s = list.size() - location;

                            if (s < LEAST_ELEMENT_TO_SHOW) {
                                location = list.size() - LEAST_ELEMENT_TO_SHOW;
                            }
                        }
                    }
                    if (OFFSET > 0) {
                        if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                            location = kLineDataList.size();
                        } else if (location < OFFSET) {
                            location = OFFSET;
                        }
                    } else {
                        if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                            location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                            fLocation = location * (kLineWidth + kLineSpacing);
                        }
                    }

                    kLineDataList = list;

                    if (kLineDataList == null || kLineDataList.size() <= 0) {
                        for (int i = 0; i < chartViewList.size(); i++) {
                            if (chartViewList.get(i) instanceof KLineChartMainView) {
                                KLineChartMainView mTimeChartMainView = (KLineChartMainView) chartViewList.get(i);
                                if (mainIndex != null) {
                                    mTimeChartMainView.setTagKLI(mainIndex);
                                }
                                mTimeChartMainView.clear();
                            } else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                                KLineChartDeputyView mTimeChartDeputyView = (KLineChartDeputyView) chartViewList.get(i);
                                if (deputyIndexs != null && deputyIndexs.size() > 0) {
                                    mTimeChartDeputyView.setTagKLI(deputyIndexs.get(0));
                                    deputyIndexs.remove(0);
                                }
                                mTimeChartDeputyView.clear();
                            }
                        }
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnKLineListener l :
                                    mLList) {
                                l.onCursorVisible(KLineChartFrameLayout.this, false);
                            }
                        }
                    } else {
                        for (int i = 0; i < chartViewList.size(); i++) {
                            if (chartViewList.get(i) instanceof KLineChartMainView) {
                                KLineChartMainView mTimeChartMainView = (KLineChartMainView) chartViewList.get(i);
                                if (mainIndex != null) {
                                    mTimeChartMainView.setTagKLI(mainIndex);
                                }
                                mTimeChartMainView.setData(kLineDataList);
                            } else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                                KLineChartDeputyView mTimeChartDeputyView = (KLineChartDeputyView) chartViewList.get(i);
                                if (deputyIndexs != null && deputyIndexs.size() > 0) {
                                    mTimeChartDeputyView.setTagKLI(deputyIndexs.get(0));
                                    deputyIndexs.remove(0);
                                }
                                mTimeChartDeputyView.setData(kLineDataList);
                            }
                        }
                        if (mode == MODE_LONG_PRESS) {
                            if (kLineDataList.size() > 0) {
                                calculationLocation(touchX, touchY);
                                mChartCrossView.setDrawLocation(drawX, drawY);
                            } else {
                                if (mChartCrossView.getVisibility() == View.VISIBLE) {
                                    mChartCrossView.setVisibility(View.GONE);
                                    for (OnKLineListener l :
                                            mLList) {
                                        l.onCursorVisible(KLineChartFrameLayout.this, false);
                                    }
                                }
                            }
                        } else {
                            if (kLineDataList.size() > 0) {
                                KLineDataValid kLineDataValid = kLineDataList.get(
                                        kLineDataList.size() - 1);
                                KLineDataValid oldKLineDataValid = (kLineDataList.size() - 2) < 0
                                        ? kLineDataValid
                                        : kLineDataList.get(
                                        kLineDataList.size() -
                                                2);
                                for (OnKLineListener l :
                                        mLList) {
                                    l.onKLineNewDataChange(KLineChartFrameLayout.this, kLineDataValid, oldKLineDataValid);
                                }

                                int endLocation = location + maxKLineLength - 1;
                                if (kLineDataList.size() <= endLocation || endLocation < 0) {
                                    endLocation = kLineDataList.size() - 1;
                                }
                                KLineDataValid kLineDataValid1 = kLineDataList.get(endLocation);
                                KLineDataValid oldKLineDataValid1 = (endLocation - 1) < 0
                                        ? kLineDataValid1
                                        : kLineDataList.get(
                                        endLocation -
                                                1);
                                for (OnKLineListener l :
                                        mLList) {
                                    l.onKLineEndDataChange(KLineChartFrameLayout.this, kLineDataValid1, oldKLineDataValid1);
                                }
                            }
                        }
                    }
                    calculationEndDataLocation();
                    if (mAdapter != null) {
                        if (kLineDataList != null) {
                            if (mAdapter.getCount() != kLineDataList.size()) {
                                if (baseOnChartDataObserver != null)
                                    baseOnChartDataObserver.onChartData();
                            }
                        } else {
                            if (mAdapter.getCount() != 0) {
                                if (baseOnChartDataObserver != null)
                                    baseOnChartDataObserver.onChartData();
                            }
                        }
                    }
                    postInvalidateDelayed(50);
                    break;

                case MSG_RESTART_INDEX_THREAD:
                    if (!BaseThread.isAlive(KLineChartFrameLayoutThread)) {
                        if (BaseThread.isAlive(mIndexThread)) {
                            mIndexThread.onStop();
                        }
                        mIndexThread = new IndexThread(kLineDataList);
                        if (executorService != null && !executorService.isShutdown()) {
                            executorService.execute(mIndexThread);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 获取两点间距
     *
     * @param event
     * @return
     */
    private float getPointerSpacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取两个手指中间点的Location位置
     *
     * @param event
     * @return
     */
    private int getPointerCenterDot(MotionEvent event) {
        int centerDot = 0;
        try {
            float minX = event.getX(0) > event.getX(1) ? event.getX(1) : event.getX(0);
            float centerX = Math.abs(event.getX(0) - event.getX(1)) / 2.0f;
            float x = minX + centerX;
            float startX = x < strokeWidth ? strokeWidth
                    : x > width - strokeWidth ? width - strokeWidth
                    : x;
            centerDot = (int) ((startX - strokeWidth) / (kLineSpacing + kLineWidth));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return centerDot;
    }

    private boolean isShowCrossView = false;

    public void setIsDrawingStatus(boolean isDrawing) {
//        this.isDrawing = isDrawing;
        drawingStatus.setDrawingMode(isDrawing);

        if (drawingStatus.getDrawingMode()) {
            hideCrossView();
        }

        notifyPointChange();

//        if (isDrawing) {
//            hideCrossView();
//        }
    }

    public void startDrawing(DrawingType type) {
        drawingStatus.startDrawing(type);
    }

    public void deleteCurrentSelectedDrawing() {
        drawingComponent.deleteSelected();
        drawingStatus.saveToCache(symbol, flagTime);
    }

    public void deleteAllDrawing() {
        drawingComponent.deleteAll();
        drawingStatus.saveToCache(symbol, flagTime);
    }

    public void lockLine()  {
        if (drawingStatus.getCurrentSelectedDrawingData() != null) {
            boolean isLocked = drawingStatus.getCurrentSelectedDrawingData().isLocked();
            drawingStatus.getCurrentSelectedDrawingData().setLocked(!isLocked);
            drawingStatus.getDefaultConfig().setLocked(!isLocked);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            Toast.makeText(getContext(), !isLocked ? "The current line is locked" : "The current line is unlocked", Toast.LENGTH_SHORT).show();
        } else if (drawingStatus.getCurrentDrawingData() != null) {
            boolean isLocked = drawingStatus.getCurrentDrawingData().isLocked();
            drawingStatus.getCurrentDrawingData().setLocked(!isLocked);
            drawingStatus.getDefaultConfig().setLocked(!isLocked);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            Toast.makeText(getContext(), !isLocked ? "The current line is locked" : "The current line is unlocked", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDrawingLineColor(int color) {
        if (drawingStatus.getCurrentSelectedDrawingData() != null) {
            drawingStatus.getCurrentSelectedDrawingData().setColor(color);
            drawingStatus.getDefaultConfig().setColor(color);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            notifyPointChange();
        } else if (drawingStatus.getCurrentDrawingData() != null) {
            drawingStatus.getCurrentDrawingData().setColor(color);
            drawingStatus.getDefaultConfig().setColor(color);
            drawingStatus.updateLocalDefaultConfig();
            notifyPointChange();
        }
    }

    public void setDrawingFillColor(int color) {
        if (drawingStatus.getCurrentSelectedDrawingData() != null) {
            drawingStatus.getCurrentSelectedDrawingData().setFillColor(color);
            drawingStatus.getDefaultConfig().setFillColor(color);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            notifyPointChange();
        } else if (drawingStatus.getCurrentDrawingData() != null) {
            drawingStatus.getCurrentDrawingData().setFillColor(color);
            drawingStatus.getDefaultConfig().setFillColor(color);
            drawingStatus.updateLocalDefaultConfig();
            notifyPointChange();
        }
    }

    public void setDrawingWidth(float width) {
        if (drawingStatus.getCurrentSelectedDrawingData() != null) {
            drawingStatus.getCurrentSelectedDrawingData().setWidth(width);
            drawingStatus.getDefaultConfig().setWidth(width);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            notifyPointChange();
        } else if (drawingStatus.getCurrentDrawingData() != null) {
            drawingStatus.getCurrentDrawingData().setWidth(width);
            drawingStatus.getDefaultConfig().setWidth(width);
            drawingStatus.updateLocalDefaultConfig();
            notifyPointChange();
        }
    }

    public void setDrawingLineStyle(float dashGap, float dashWidth) {
        if (drawingStatus.getCurrentSelectedDrawingData() != null) {
            drawingStatus.getCurrentSelectedDrawingData().setDashGap(dashGap);
            drawingStatus.getCurrentSelectedDrawingData().setDashWidth(dashWidth);
            drawingStatus.getDefaultConfig().setDashGap(dashGap);
            drawingStatus.getDefaultConfig().setDashWidth(dashWidth);
            drawingStatus.updateLocalDefaultConfig();
            drawingStatus.saveToCache(symbol, flagTime);
            notifyPointChange();
        } else if (drawingStatus.getCurrentDrawingData() != null) {
            drawingStatus.getCurrentDrawingData().setDashGap(dashGap);
            drawingStatus.getCurrentDrawingData().setDashWidth(dashWidth);
            drawingStatus.getDefaultConfig().setDashGap(dashGap);
            drawingStatus.getDefaultConfig().setDashWidth(dashWidth);
            drawingStatus.updateLocalDefaultConfig();
            notifyPointChange();
        }
    }

    public void toggleContinueDrawing() {
        boolean isContinueDrawing = drawingStatus.getContinueDrawing();
        drawingStatus.setContinueDrawing(!isContinueDrawing);
        Toast.makeText(getContext(), !isContinueDrawing ? "Enable continuous drawing" : "Disable continuous drawing", Toast.LENGTH_SHORT).show();
        notifyPointChange();
    }

    public void setContinueDrawingStatus(boolean isContinueDrawing) {
        drawingStatus.setContinueDrawing(isContinueDrawing);
        notifyPointChange();
    }

    public void toggleHideDrawing() {
        boolean isHideDrawing = drawingStatus.getHideAllDrawing();
        drawingStatus.setHideAllDrawing(!isHideDrawing);
        Toast.makeText(getContext(), !isHideDrawing ? "Hide drawing" : "Show drawing", Toast.LENGTH_SHORT).show();
        notifyPointChange();
    }

    public void hideDrawing(boolean hide) {
        drawingStatus.setHideAllDrawing(hide);
        notifyPointChange();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("onTouchEvent", "x: " + event.getX() + ", y: " + event.getY() );
        if (magnifierAutoLayout != null && drawingStatus.isDrawing()) {
            // need to separate drawingMode and isDrawing status
            drawingComponent.handleDrawingTouch(event);

            // 使用放大镜
            magnifierAutoLayout.setTouch(event, this, mainViewOffsetViewBounds);
            return true;
        }

        if (MotionEvent.ACTION_UP == event.getAction()) {
            Log.d("onTouchEvent 1", "magnifierAutoLayout is " + magnifierAutoLayout + "," +
                    "hasMovingPoint is " + drawingComponent.hasMovingPoint() + "," +
                    "drawingMode is " + drawingStatus.getDrawingMode() + "," +
                    "selectedDrawingData is " + drawingStatus.getCurrentSelectedDrawingData());
        }

        if (magnifierAutoLayout != null && drawingComponent.hasMovingPoint() && drawingStatus.getDrawingMode()) {

            if (MotionEvent.ACTION_UP == event.getAction()) {
                Log.d("onTouchEvent 2", "magnifierAutoLayout is " + magnifierAutoLayout + "," +
                        "hasMovingPoint is " + drawingComponent.hasMovingPoint() + "," +
                        "drawingMode is " + drawingStatus.getDrawingMode() + "," +
                        "selectedDrawingData is " + drawingStatus.getCurrentSelectedDrawingData());
            }
            if (drawingStatus.getCurrentSelectedDrawingData() != null && !drawingStatus.getCurrentSelectedDrawingData().isLocked()) {
                // 使用放大镜
                magnifierAutoLayout.setTouch(event, this, mainViewOffsetViewBounds);
            }
        }

        // 需要在后续的onTouchEvent中处理drawingMode，但不是isDrawing的情况
        if (!isSupportEvent) {
            if (onIndexSwitchListener != null) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("JamesDebug", "action down position is " + event.getX() + ", " + event.getY());
                        mode = MODE_CLICK;
                        isShowCrossView = hideCrossView();
                        return true;
                    case MotionEvent.ACTION_MOVE: {
                        float x = event.getX();
                        float y = event.getY();
                        if (mode == MODE_CLICK &&
                                (x > width || x < 0 || y > height || y < 0)) {
                            mode = -1;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        if (mode == MODE_CLICK) {
                            if (isShowCrossView) {
                                break;
                            }
                            boolean isIndexSwitch = false;
                            if (onIndexSwitchListener != null) {
                                int x = (int) event.getRawX();
                                int y = (int) event.getRawY();
                                for (int i = 0; i < chartViewList.size(); i++) {
                                    View view = chartViewList.get(i);
                                    if (view instanceof KLineChartDeputyView) {
                                        int[] location = new int[2];
                                        view.getLocationOnScreen(location);
                                        Rect rect = new Rect(location[0], location[1],
                                                location[0] + view.getWidth(),
                                                location[1] + view.getHeight());
                                        Rect leftRect = new Rect(rect.left, rect.top, (int) (
                                                rect.width() /
                                                        2f), rect.bottom);
                                        Rect rightRect = new Rect(leftRect.width(), rect.top, rect.width(), rect.bottom);
                                        if (leftRect.contains(x, y)) {
                                            onIndexSwitchListener.onKLinePreviousIndex(view);
                                            isIndexSwitch = true;
                                            return true;
                                        } else if (rightRect.contains(x, y)) {
                                            onIndexSwitchListener.onKLineNextIndex(view);
                                            isIndexSwitch = true;
                                            return true;
                                        }
                                    }
                                }
                            }
                            if (!isIndexSwitch) {
                                performClick();
                                return true;
                            }
                        }
                        break;
                }
            }
            return super.onTouchEvent(event);
        }


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                Log.d("JamesDebug", "action down position is " + event.getX() + ", " + event.getY());

                if (event.getY() < mainViewOffsetViewBounds.top || event.getY() > mainViewOffsetViewBounds.bottom) {
                    Log.d("JamesDebugNew", "out of main view");
                }
                isShowCrossView = hideCrossView();
                fLocation = location * (kLineWidth + kLineSpacing);
                mode = MODE_CLICK;

                // 画图模式
                if (drawingStatus.getDrawingMode()) {
                    drawingComponent.performPointerDown(new Position(event.getX(), event.getY(), 0));
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // 多点触摸一些起始性数据的记录
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (!isPortraitTouchEvent) {
                        break;
                    }
                }
                if (mode == MODE_CLICK && event.getPointerCount() >= 2) {
                    mode = MODE_POINTER;
                    downPointerSpacing = getPointerSpacing(event);
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (!isPortraitTouchEvent) {
                        break;
                    }
                }
                if (mode == MODE_POINTER) {
                    mode = MODE_POINTER_END;
                }
                return true;

            case MotionEvent.ACTION_MOVE:

                // 画图模式
                if (drawingStatus.getDrawingMode()) {
                    Position position = new Position(event.getX(), event.getY(), 0);
                    if (drawingComponent.isDragMode(position)) {
                        boolean result = drawingComponent.performDrag(event.getX() - touchX, event.getY() - touchY, position);

                        if (result) {
                            touchX = event.getX();
                            touchY = event.getY();
                            touchRawY = event.getRawY();
                            touchRawX = event.getRawX();
                            return true;
                        }
                    }
                }

                touchX = event.getX();
                touchY = event.getY();
                touchRawY = event.getRawY();
                touchRawX = event.getRawX();

                if (mode == MODE_LONG_PRESS) {
                    if (kLineDataList != null && kLineDataList.size() > 0) {
                        calculationLocation(touchX, touchY);
                        mChartCrossView.setDrawLocation(drawX, drawY);
                        vibrateWhenTouch(drawX, drawY);
                    } else {
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnKLineListener l :
                                    mLList) {
                                l.onCursorVisible(this, false);
                            }
                        }
                    }
                    return true;
                } else if (mode == MODE_POINTER) {
                    if (kLineDataList != null && kLineDataList.size() > 0) {
                        pointerSpacing = getPointerSpacing(event);
                        int locationCenter = getPointerCenterDot(event);

                        kLineWidth += (pointerSpacing - downPointerSpacing) / maxKLineLength;
                        if (kLineWidth < minKLineWidth) {
                            if (mOnZoomListener != null) {
                                mOnZoomListener.onZoomListener(false, true);
                            }
                            kLineWidth = minKLineWidth;
                        } else if (kLineWidth > maxKLineWidth) {
                            if (mOnZoomListener != null) {
                                mOnZoomListener.onZoomListener(true, false);
                            }
                            kLineWidth = maxKLineWidth;
                        } else {
                            if (mOnZoomListener != null) {
                                mOnZoomListener.onZoomListener(false, false);
                            }
                        }
                        kLineSpacing = getkLineSpacing(kLineWidth);
                        maxKLineLength = getMaxLength(kLineWidth, kLineSpacing);

                        int locationCenterEnd = getPointerCenterDot(event);

                        int offset = locationCenterEnd - locationCenter;

                        location -= offset;

                        if (location > kLineDataList.size() - LEAST_ELEMENT_TO_SHOW) {
                            location = kLineDataList.size() - LEAST_ELEMENT_TO_SHOW;
                            fLocation = location * (kLineWidth + kLineSpacing);
                        }

                        if (OFFSET > 0) {
                            if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                                location = kLineDataList.size();
                            } else if (location < OFFSET) {
                                location = OFFSET;
                            }
                            fLocation = location;
                        } else {
                            if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                                location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                                fLocation = location * (kLineWidth + kLineSpacing);
                            }
                        }

                        for (int i = 0; i < chartViewList.size(); i++) {
                            if (chartViewList.get(i) instanceof KLineChartMainView)
                                ((KLineChartMainView) chartViewList.get(i)).setData(kLineDataList);
                            else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                                ((KLineChartDeputyView) chartViewList.get(i)).setData(kLineDataList);
                            }
                        }
                        downPointerSpacing = pointerSpacing;

                        if (kLineDataList != null && kLineDataList.size() > 0) {
                            int endLocation = location + maxKLineLength - 1;
                            if (kLineDataList.size() <= endLocation || endLocation < 0) {
                                endLocation = kLineDataList.size() - 1;
                            }
                            KLineDataValid kLineDataValid = kLineDataList.get(endLocation);
                            KLineDataValid oldKLineDataValid = (endLocation - 1) < 0
                                    ? kLineDataValid
                                    : kLineDataList.get(
                                    endLocation -
                                            1);
                            for (OnKLineListener l :
                                    mLList) {
                                l.onKLineEndDataChange(this, kLineDataValid, oldKLineDataValid);
                            }
                        }
                        calculationEndDataLocation();
                        postInvalidate();
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Position currentPointer = new Position(event.getX(), event.getY(), event.getEventTime());
                if (!drawingStatus.getDrawingMode() && event.getActionMasked() == MotionEvent.ACTION_UP) {
                    // 处理双击事件
                    boolean hitDoubleTap = false;
                    if(previousPointer != null) {
                        double distance = Math.sqrt(Math.pow(currentPointer.getX() - previousPointer.getX(), 2) + Math.pow(currentPointer.getY() - previousPointer.getY(), 2));
                        long timeOffset = currentPointer.getTimeStamp() - previousPointer.getTimeStamp();

                        if (timeOffset < 500 && distance < 50) {
                            hitDoubleTap = true;
                            drawingComponent.performDoubleTap(currentPointer);
                        }
                    }

                    if (hitDoubleTap) {
                        previousPointer = null;
                    } else {
                        previousPointer = currentPointer;
                    }
                }

                if (drawingStatus.getDrawingMode() && event.getActionMasked() == MotionEvent.ACTION_UP) {
                    drawingComponent.performDragUp();

                    if (mode == MODE_CLICK) {
                        drawingComponent.performTap(currentPointer);
                    }
                } else if (isTapToShowAbstractEnabled && !drawingStatus.getDrawingMode() && event.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (mode == MODE_CLICK) {
                        if (kLineDataList != null && kLineDataList.size() > 0) {
                            calculationLocation(touchX, touchY);
                            mChartCrossView.setDrawLocation(drawX, drawY);
                            vibrateWhenTouch(drawX, drawY);
                        } else {
                            if (mChartCrossView.getVisibility() == View.VISIBLE) {
                                mChartCrossView.setVisibility(View.GONE);
                                for (OnKLineListener l :
                                        mLList) {
                                    l.onCursorVisible(this, false);
                                }
                            }
                        }
                    }
                }

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
//                if (mode == MODE_LONG_PRESS) {
//                    mode = MODE_CLICK;
//                    if (mChartCrossView.getVisibility() == View.VISIBLE) {
//                        mChartCrossView.setVisibility(View.GONE);
//                        for (OnKLineListener l : mLList) {
//                            l.onCursorVisible(this, false);
//                        }
//                    }
//                    if (kLineDataList != null && kLineDataList.size() > 0) {
//                        KLineDataValid kLineDataValid = kLineDataList.get(
//                                kLineDataList.size() - 1);
//                        KLineDataValid oldKLineDataValid = (kLineDataList.size() - 2) < 0
//                                                           ? kLineDataValid
//                                                           : kLineDataList.get(
//                                                                   kLineDataList.size() -
//                                                                   2);
//                        for (OnKLineListener l :
//                                mLList) {
//                            l.onKLineNewDataChange(this, kLineDataValid, oldKLineDataValid);
//                        }
//                    }
//                } else
                if (mode == MODE_CLICK) {
                    if (isShowCrossView) {
                        break;
                    }
                    boolean isIndexSwitch = false;
                    if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                        break;
                    }
                    if (onIndexSwitchListener != null) {
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        for (int i = 0; i < chartViewList.size(); i++) {
                            View view = chartViewList.get(i);
                            if (view instanceof KLineChartDeputyView) {
                                int[] location = new int[2];
                                view.getLocationOnScreen(location);
                                Rect rect = new Rect(location[0], location[1],
                                        location[0] + view.getWidth(),
                                        location[1] + view.getHeight());
                                Rect leftRect = new Rect(rect.left, rect.top, (int) (rect.width() /
                                        2f), rect.bottom);
                                Rect rightRect = new Rect(leftRect.width(), rect.top, rect.width(), rect.bottom);
                                if (leftRect.contains(x, y)) {
                                    onIndexSwitchListener.onKLinePreviousIndex(view);
                                    isIndexSwitch = true;
                                } else if (rightRect.contains(x, y)) {
                                    onIndexSwitchListener.onKLineNextIndex(view);
                                    isIndexSwitch = true;
                                }
                            }
                        }
                    }
                    if (!isIndexSwitch) {
                        performClick();
                    }
                    for (int i = 0; i < chartViewList.size(); i++) {
                        View view = chartViewList.get(i);
                        if (view instanceof KLineChartMainView) {
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            boolean clickCurClose = ((KLineChartMainView) view).handleClick(x, y);
                            if (clickCurClose) {
                                mAdapter.setLocation(mAdapter.getCount()-1);
                                mAdapter.notifyChangeData();
                            }
                        }
                    }
                }
                break;
        }
        if (baseGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 计算十字线位置
     *
     * @param touchX
     * @param touchY
     */
    private void calculationLocation(float touchX, float touchY) {
        float scale = 0;
        float mainChartHeight = 0;
        float dataSpacing = kLineSpacing + kLineWidth;
        if (chartViewList != null && chartViewList.size() > 0) {
            KLineChartMainView mainView = null;
            drawY = 0;
            if (chartViewList.get(0) instanceof KLineChartMainView) {
                mainView = (KLineChartMainView) chartViewList.get(0);
                scale = mainView.getScale();
                mainChartHeight = mainView.getHeight();
            }
            float x = touchX < strokeWidth ? strokeWidth
                    : touchX > width - strokeWidth ? width - strokeWidth
                    : touchX;
            int position = location + (int) ((x - strokeWidth) / dataSpacing);

            if (position >= maxKLineLength + location) {
                position = maxKLineLength + location - 1;
            }
            if (position >= kLineDataList.size()) {
                position = kLineDataList.size() - 1;
            } else if (position < 0) {
                position = 0;
            }

            drawX = (position - location) * dataSpacing + (strokeWidth + kLineWidth / 2.0f);

            if (mainView != null) {
                mChartCrossView.setDrawMainLine(isDrawMainLine);
                if (mainView.getKLineMainIndex() == KLineChartMainView.KLineMainIndex.RETURN_RATE) {
                    drawY = (float) (mainChartHeight -
                            (kLineDataList.get(position).getReturnRate().getRate() -
                                    mainView.getMinData()) *
                                    scale + mainView.getTop());
                } else {
                    drawY = touchY;
//                    drawY = (float) (mainChartHeight -
//                            (kLineDataList.get(position).getClose() -
//                                    mainView.getMinData()) *
//                                    scale + mainView.getTop());
                }
            } else {
                mChartCrossView.setDrawMainLine(false);
            }

            // 非绘制模式下才显示十字线
            if (!drawingStatus.getDrawingMode()) {
                if (mChartCrossView.getVisibility() == View.GONE) {
                    mChartCrossView.setVisibility(View.VISIBLE);
                    for (OnKLineListener l :
                            mLList) {
                        l.onCursorVisible(this, true);
                        l.onKLineListener(this, kLineDataList, position, drawX, drawY, tradeInfoMap);
                    }
                } else {
                    for (OnKLineListener l :
                            mLList) {
                        l.onKLineListener(this, kLineDataList, position, drawX, drawY,tradeInfoMap);
                    }
                }
            }
        }
    }

    /**
     * 计算最后一条数据的位置
     */
    private void calculationEndDataLocation() {
        boolean isCurrent = false;
        float x = -1, y = -1;
        if (chartViewList != null && chartViewList.size() > 0
                && kLineDataList != null && kLineDataList.size() > 0) {
            int position = kLineDataList.size() - 1;
            float dataSpacing = kLineSpacing + kLineWidth;
            x = (position - location) * dataSpacing + (strokeWidth + kLineWidth / 2.0f);
            if (x < width - strokeWidth) {
                isCurrent = true;
            }
            KLineDataValid data = kLineDataList.get(position);
            float scale = 0;
            float mainChartHeight = 0;
            KLineChartMainView mainView = null;
            if (chartViewList.get(0) instanceof KLineChartMainView) {
                mainView = (KLineChartMainView) chartViewList.get(0);
                scale = mainView.getScale();
                mainChartHeight = mainView.getHeight();
            }
            if (mainView != null) {
                if (mainView.getKLineMainIndex() == KLineChartMainView.KLineMainIndex.RETURN_RATE) {
                    y = (float) (mainChartHeight -
                            (data.getReturnRate().getRate() -
                                    mainView.getMinData()) *
                                    scale + mainView.getTop());
                } else {
                    y = (float) (mainChartHeight -
                            (data.getClose() -
                                    mainView.getMinData()) *
                                    scale + mainView.getTop());
                }
                if (y < mainView.getTop()) {
                    y = mainView.getTop();
                } else {
                    int Y = mainView.getTop() + mainView.getHeight();
                    if (y > Y) {
                        y = Y;
                    }
                }
            }
        }
        for (int i = 0; i < mLList.size(); i++) {
            mLList.get(i).onEndPoint(this, isCurrent, x, y);
        }
    }

    /**
     * 手势识别监听器
     */
    class StockSimpleOnGestureListener extends StockGestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            downRawX = e.getRawX();
            downRawY = e.getRawY();
            touchRawX = downRawX;
            touchRawY = downRawY;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mode == MODE_CLICK
                    && Math.abs(distanceX) > Math.abs(distanceY)) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (!isPortraitTouchEvent) {
                        return false;
                    }
                }
                if (distanceX > 0) {
                    mode = MODE_TO_LEFT;
                } else {
                    mode = MODE_TO_RIGHT;
                }
                distanceX = 0;
            }
            if (mode == MODE_CLICK
                    && Math.abs(distanceY) > Math.abs(distanceX)) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (!isPortraitTouchEvent) {
                        return false;
                    }
                }
                if (distanceY > 0) {
                    mode = MODE_TO_UP;
                } else {
                    mode = MODE_TO_DOWN;
                }
                distanceY = 0;
            }
            if (mode == MODE_TO_LEFT || mode == MODE_TO_RIGHT) {
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                if (kLineDataList != null && kLineDataList.size() > 0) {
                    fLocation += distanceX;
                    location = (int) (fLocation / (kLineWidth + kLineSpacing));

                    if (location > kLineDataList.size() - LEAST_ELEMENT_TO_SHOW) {
                        location = kLineDataList.size() - LEAST_ELEMENT_TO_SHOW;
                        fLocation = location * (kLineWidth + kLineSpacing);
                    }

                    if (OFFSET > 0) {
                        if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                            location = kLineDataList.size();
                        } else if (location < OFFSET) {
                            location = OFFSET;
                        }
                        fLocation = location;
                    } else {
                        if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                            location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                            fLocation = location * (kLineWidth + kLineSpacing);
                        }

                    }
                    for (int i = 0; i < chartViewList.size(); i++) {
                        if (chartViewList.get(i) instanceof KLineChartMainView)
                            ((KLineChartMainView) chartViewList.get(i)).setData(kLineDataList);
                        else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                            ((KLineChartDeputyView) chartViewList.get(i)).setData(kLineDataList);
                        }
                    }
                    if (kLineDataList != null && kLineDataList.size() > 0) {
                        int endLocation = location + maxKLineLength - 1;
                        if (kLineDataList.size() <= endLocation || endLocation < 0) {
                            endLocation = kLineDataList.size() - 1;
                        }
                        KLineDataValid kLineDataValid = kLineDataList.get(endLocation);
                        KLineDataValid oldKLineDataValid = (endLocation - 1) < 0
                                ? kLineDataValid
                                : kLineDataList.get(
                                endLocation -
                                        1);
                        for (OnKLineListener l :
                                mLList) {
                            l.onKLineEndDataChange(KLineChartFrameLayout.this, kLineDataValid, oldKLineDataValid);
                            if (((OFFSET > 0 && location == OFFSET)
                                    || location == 0) && mode == MODE_TO_RIGHT) {
                                if (!isLoading) {
                                    isLoading = true;
                                    l.onScrollLeft();
                                }
                            }
                        }
                    }
                    calculationEndDataLocation();
                    postInvalidate();
                }
            } else if (mode == MODE_TO_UP || mode == MODE_TO_DOWN) {
                return false;
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (mode == MODE_CLICK && (Math.abs(touchRawX - downRawX) < (touchSpacing / 2))
                    && (Math.abs(touchRawY - downRawY) < (touchSpacing / 2))) {
                mode = MODE_LONG_PRESS;
                touchX = e.getX();
                touchY = e.getY();
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                if (kLineDataList != null && kLineDataList.size() > 0) {
                    calculationLocation(touchX, touchY);
                    mChartCrossView.setDrawLocation(drawX, drawY);
                    lastVibrateX = -1;
                    lastVibrateY = -1;
                    vibrateWhenTouch(drawX, drawY);
                } else {
                    if (mChartCrossView.getVisibility() == View.VISIBLE) {
                        mChartCrossView.setVisibility(View.GONE);
                        for (OnKLineListener l :
                                mLList) {
                            l.onCursorVisible(KLineChartFrameLayout.this, false);
                        }
                    }
                }
            }
            super.onShowPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((mode == MODE_TO_LEFT || mode == MODE_TO_RIGHT)
                    && kLineDataList != null) {
                mode = MODE_FLING;
                mScroller.fling((int) fLocation, 0, (int) -velocityX, (int) -velocityY, Integer.MIN_VALUE,
                        Integer.MAX_VALUE, 0, 0);
                postInvalidate();
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                if (kLineDataList != null && kLineDataList.size() > 0) {
//                    fLocation = mScroller.getCurrX();
//                    location = (int) (fLocation / (kLineWidth + kLineSpacing));
//                    if (location > kLineDataList.size() - maxKLineLength) {
//                        location = kLineDataList.size() - maxKLineLength;
//                        fLocation = location * (kLineWidth + kLineSpacing);
//                        mScroller.abortAnimation();
//                    }

                    fLocation = mScroller.getCurrX();
                    location = (int) (fLocation / (kLineWidth + kLineSpacing));
                    if (location > kLineDataList.size() - LEAST_ELEMENT_TO_SHOW) {
                        location = kLineDataList.size() - LEAST_ELEMENT_TO_SHOW;
                        fLocation = location * (kLineWidth + kLineSpacing);
                        mScroller.abortAnimation();
                    }
                    if (OFFSET > 0) {
                        if (kLineDataList != null && kLineDataList.size() <= OFFSET) {
                            location = kLineDataList.size();
                            fLocation = location;
                            mScroller.abortAnimation();
                        } else if (location < OFFSET) {
                            location = OFFSET;
                            fLocation = location;
                            mScroller.abortAnimation();
                        }
                        fLocation = location;
                    } else {
                        if (location < LEAST_ELEMENT_TO_SHOW - maxKLineLength) {
                            location = LEAST_ELEMENT_TO_SHOW - maxKLineLength;
                            fLocation = location * (kLineWidth + kLineSpacing);
                            mScroller.abortAnimation();
                        }
                    }
                    for (int i = 0; i < chartViewList.size(); i++) {
                        if (chartViewList.get(i) instanceof KLineChartMainView)
                            ((KLineChartMainView) chartViewList.get(i)).setData(kLineDataList);
                        else if (chartViewList.get(i) instanceof KLineChartDeputyView){
                            ((KLineChartDeputyView) chartViewList.get(i)).setData(kLineDataList);
                        }
                    }
                    if (kLineDataList != null && kLineDataList.size() > 0) {
                        int endLocation = location + maxKLineLength - 1;
                        if (kLineDataList.size() <= endLocation || endLocation < 0) {
                            endLocation = kLineDataList.size() - 1;
                        }
                        KLineDataValid kLineDataValid = kLineDataList.get(endLocation);
                        KLineDataValid oldKLineDataValid = (endLocation - 1) < 0
                                ? kLineDataValid
                                : kLineDataList.get(
                                endLocation -
                                        1);
                        for (OnKLineListener l :
                                mLList) {
                            l.onKLineEndDataChange(this, kLineDataValid, oldKLineDataValid);
                        }
                    }
                    calculationEndDataLocation();
                    postInvalidate();
                }
            }
        }
    }

    /**
     * 隐藏十字光标
     */
    private boolean hideCrossView() {
        boolean isShowCrossView = false;
        if (mode == MODE_LONG_PRESS) {
            mode = MODE_CLICK;
        }
        if (mChartCrossView.getVisibility() == View.VISIBLE) {
            isShowCrossView = true;
            mChartCrossView.setVisibility(View.GONE);
            for (OnKLineListener l : mLList) {
                l.onCursorVisible(this, false);
            }
        }
        if (kLineDataList != null && kLineDataList.size() > 0) {
            KLineDataValid kLineDataValid = kLineDataList.get(
                    kLineDataList.size() - 1);
            KLineDataValid oldKLineDataValid = (kLineDataList.size() - 2) < 0
                    ? kLineDataValid
                    : kLineDataList.get(
                    kLineDataList.size() -
                            2);
            for (OnKLineListener l :
                    mLList) {
                l.onKLineNewDataChange(this, kLineDataValid, oldKLineDataValid);
            }

            int endLocation = location + maxKLineLength - 1;
            if (kLineDataList.size() <= endLocation || endLocation < 0) {
                endLocation = kLineDataList.size() - 1;
            }
            KLineDataValid kLineDataValid1 = kLineDataList.get(endLocation);
            KLineDataValid oldKLineDataValid1 = (endLocation - 1) < 0
                    ? kLineDataValid1
                    : kLineDataList.get(
                    endLocation -
                            1);
            for (OnKLineListener l :
                    mLList) {
                l.onKLineEndDataChange(this, kLineDataValid1, oldKLineDataValid1);
            }
        }
        return isShowCrossView;
    }

    /**
     * 加载更多完成
     */
    public void setLoadmoreOk() {
        isLoading = false;
    }

    /**
     * 是否正在加载更多
     *
     * @return
     */
    public boolean isLoading() {
        return isLoading;
    }

    public void setCrossColor(int crossColor) {
        if (crossColor != this.crossColor) {
            this.crossColor = crossColor;
            mChartCrossView.setCrossColor(crossColor);
        }
    }

    public void setDotColor(int dotColor) {
        if (this.dotColor != dotColor) {
            this.dotColor = dotColor;
            mChartCrossView.setDotColor(dotColor);
        }
    }

    public void setDrawDot(boolean isDrawDot) {
        if (this.isDrawDot != isDrawDot) {
            mChartCrossView.setDrawDot(isDrawDot);
        }
    }

    public boolean isDrawDot() {
        return this.isDrawDot;
    }

    /**
     * 接口
     */
    public interface OnKLineListener {

        /**
         * 十字光标是否显示中
         *
         * @param isCursorVisible true显示，false不显示
         */
        public void onCursorVisible(KLineChartFrameLayout mKLineChartFrameLayout, boolean isCursorVisible);

        /**
         * 传递数据
         *
         * @param mKLineChartFrameLayout
         * @param kLineList
         * @param position
         * @param cursorX
         */
        public void onKLineListener(KLineChartFrameLayout mKLineChartFrameLayout, ArrayList<KLineDataValid> kLineList, int position, float cursorX, float cursorY, Map<Long, Pair<TradeInfo, TradeInfo>> tradeMap);

        /**
         * 是否滚到最左边
         */
        public void onScrollLeft();

        /**
         * 当前视图最新数据回调
         *
         * @param mKLineChartFrameLayout
         * @param endKLineData           当前视图显示最后一条数据
         * @param preKLineData           上一周期数据
         */
        public void onKLineEndDataChange(KLineChartFrameLayout mKLineChartFrameLayout, KLineDataValid endKLineData, KLineDataValid preKLineData);

        /**
         * 最新数据回调
         *
         * @param mKLineChartFrameLayout
         * @param endKLineData           最新数据
         * @param preKLineData           上一周期数据
         */
        public void onKLineNewDataChange(KLineChartFrameLayout mKLineChartFrameLayout, KLineDataValid endKLineData, KLineDataValid preKLineData);

        /**
         * 最后一根K线位置，并不一定在当前视图
         *
         * @param mKLineChartFrameLayout
         * @param isCurrent              是否在当前视图中
         * @param x                      不存在当前视图中返回 -1
         * @param y                      不存在当前视图中返回 -1
         */
        public void onEndPoint(KLineChartFrameLayout mKLineChartFrameLayout, boolean isCurrent, float x, float y);
    }

    /**
     * 放大缩小
     */
    public interface OnZoomListener {
        /**
         * 放大缩小监听
         *
         * @param isZoomMax 是否已经放到最大
         * @param isZoomMin 是否已经缩到最小
         */
        public void onZoomListener(boolean isZoomMax, boolean isZoomMin);
    }

    /**
     * 副图指标切换
     */
    public interface OnIndexSwitchListener {
        /**
         * 切换上一个指标
         *
         * @param v 点击的视图
         */
        public void onKLinePreviousIndex(View v);

        /**
         * 切换下一个指标
         *
         * @param v 点击的视图
         */
        public void onKLineNextIndex(View v);
    }
}
