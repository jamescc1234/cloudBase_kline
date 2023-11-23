package com.widget.stock.long_time_chart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;

import com.kedll.stock.library.base.BaseThread;
import com.widget.stock.ChartBaseFrameLayout;
import com.widget.stock.ChartBaseView;
import com.widget.stock.ChartCrossView;
import com.widget.stock.ChartLinearLayout;
import com.widget.stock.OnChartDataObserver;
import com.widget.stock.TimeLocation;
import com.widget.stock.adapter.ChartAdapter;
import com.widget.stock.long_time_chart.adapter.LongTimeChartAdapter;
import com.widget.stock.long_time_chart.data.LongTimeDataChildList;
import com.widget.stock.long_time_chart.data.LongTimeDataGroupList;
import com.widget.stock.time_chart.data.TimeData;
import com.widget.stock.time_chart.data.TimeSlot;
import com.widget.stock.time_chart.view.TimeChartFrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import binance.stock.library.R;


/**
 * Created by dingrui on 2016/10/9.
 * 多日分时父布局,此Layout只绘制边线、纬线、经线
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LongTimeChartFrameLayout extends ChartBaseFrameLayout {

    private ChartCrossView mChartCrossView;// 十字线的View
    private boolean isDestroy = false;// 视图是否结束

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 手势相关
     */
    private final int MODE_CLICK = 0,// 点击
            MODE_LONG_PRESS = 1,// 长按事件
            MODE_TO_UP = 2,// 向上滚动
            MODE_TO_DOWN = 3,// 向下滚动
            MODE_TO_LEFT = 4,// 向左滚动
            MODE_TO_RIGHT = 5;// 向右滚动
    private int mode;// 当前手势
    private float downX, downY;// 按下是的X、Y点
    private float lastX, lastY;// 纪录上一个X、Y点
    private float touchX, touchY;// 当前触摸的X、Y点
    private float drawX,// 十字线绘制X点
            drawY;// 十字线绘制Y点

    private List<View> longTimeChartList = new ArrayList<>();// 主图、副图集合
    private boolean isViewsAddAll = false;// 主、副图是否添加完毕
    private String[] dates;// 时间数组
    private TimeConfigure mTimeConfigure = new TimeConfigure();// 一日分时时间配置
    private float dayWidth;// 每日占用宽度
    private BaseOnChartDataObserver baseOnChartDataObserver = new BaseOnChartDataObserver();// 观察者
    private LongTimeChartAdapter mAdapter;
    private LongTimeChartFrameLayoutThread longTimeChartFrameLayoutThread;
    private IndexThread mIndexThread;
    private BaseHandler handler;

    private List<OnLongTimeListener> mLList = new ArrayList<>();// 接口
    private View floatingWindow;// 详情浮窗

    /**
     * 数据相关
     */
    private LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList;// 计算后的数据集合

    @IntDef({
            TWO_DAYS,
            THREE_DAYS,
            FOUR_DAYS,
            FIVE_DAYS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface HowManyDays {
    }

    public static final int TWO_DAYS = 2,// 2日
            THREE_DAYS = 3,// 3日
            FOUR_DAYS = 4,// 4日
            FIVE_DAYS = 5;// 5日
    private final int[] howManyDayes = {TWO_DAYS, THREE_DAYS, FOUR_DAYS, FIVE_DAYS};
    private int howManyDays = TWO_DAYS;// 多少日

    public LongTimeChartFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public LongTimeChartFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LongTimeChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LongTimeChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        executorService = Executors.newFixedThreadPool(1);
        longTimeChartFrameLayoutThread = new LongTimeChartFrameLayoutThread();
        handler = new BaseHandler();
        List<TimeSlot> timeSlotList = new ArrayList<>();
        timeSlotList.add(new TimeSlot("09:30", "11:30"));
        timeSlotList.add(new TimeSlot("13:00", "15:00"));
        setTimeSlotList(timeSlotList);

        mChartCrossView = new ChartCrossView(context);
        mChartCrossView.setTimeChartFrameLayout(this);
        mChartCrossView.setVisibility(View.GONE);
        mChartCrossView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mChartCrossView.setCrossColor(crossColor);
        mChartCrossView.setDotColor(dotColor);
        mChartCrossView.setDrawDot(isDrawDot);
        mChartCrossView.setDrawMainLine(isDrawMainLine);
        mChartCrossView.setLineWidth(lineWidthCross);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LongTimeChartFrameLayout);
            howManyDays = ta.getInt(R.styleable.LongTimeChartFrameLayout_howManyDays, howManyDays);

            ta.recycle();
        }
        for (int i = 0; i < howManyDayes.length; i++) {
            if (howManyDayes[i] == howManyDays) {
                setHowManyDays(howManyDayes[i]);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        isDestroy = false;
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.registerObserver(baseOnChartDataObserver);
        }
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(1);
        }
        for (int i = 0; i < longTimeChartList.size(); i++) {
            View view = longTimeChartList.get(i);
            if (view instanceof LongTimeChartMainView
                    || view instanceof LongTimeChartDeputyView) {
                ((ChartBaseView) view).onResume();
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
                executorService.shutdownNow();
                executorService = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < longTimeChartList.size(); i++) {
            View view = longTimeChartList.get(i);
            if (view instanceof LongTimeChartMainView
                    || view instanceof LongTimeChartDeputyView) {
                ((ChartBaseView) view).onPause();
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
        if (longTimeChartFrameLayoutThread != null) {
            longTimeChartFrameLayoutThread = null;
        }
        if (mIndexThread != null) {
            mIndexThread.onStop();
            mIndexThread = null;
        }
        if (longTimeDataList != null) {
            longTimeDataList.clear();
        }
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.unRegisterObserver(baseOnChartDataObserver);
            baseOnChartDataObserver = null;
        }
        if (longTimeChartList != null) {
            longTimeChartList.clear();
        }
        if (mTimeConfigure != null) {
            mTimeConfigure = null;
        }
        if (mLList != null) {
            mLList.clear();
        }
        for (int i = 0; i < longTimeChartList.size(); i++) {
            View view = longTimeChartList.get(i);
            if (view instanceof LongTimeChartMainView
                    || view instanceof LongTimeChartDeputyView) {
                ((ChartBaseView) view).onDestroy();
            }
        }
    }

    @Override
    public void build() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (longTimeChartList.size() == 0) {
            addLongTimeChart(this);
            mChartCrossView.setChartList(longTimeChartList);
            if (mChartCrossView.getParent() != null &&
                    (mChartCrossView.getParent() instanceof com.widget.stock.long_time_chart.view.LongTimeChartFrameLayout)) {
                removeView(mChartCrossView);
            }
            addView(mChartCrossView);
            if (floatingWindow == null)
                seekFloatingWindow(this);
            isViewsAddAll = true;
        }
        float sw2 = strokeWidth * 2.0f;
        dayWidth = (width - sw2) / howManyDays;

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
         * 绘制日期
         */
        drawDate(canvas);
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
        boolean isSetBottomMargin = false;
        int mainViewTop = 0;
        if (mTimeLocation == TimeLocation.BOTOOM) {
            if (longTimeChartList.size() > 0) {
                for (int i = longTimeChartList.size() - 1; i >= 0; i--) {
                    View longTimeChartLast = longTimeChartList.get(i);
                    if (longTimeChartLast.getParent() != null
                            && longTimeChartLast.getParent() instanceof ChartLinearLayout) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) longTimeChartLast.getLayoutParams();
                        if (longTimeChartLast.getVisibility() == View.VISIBLE
                                && !isSetBottomMargin) {
                            isSetBottomMargin = true;
                            if (lp.bottomMargin != tts2) {
                                lp.bottomMargin = tts2;
                                longTimeChartLast.setLayoutParams(lp);
                            }
                        } else {
                            if (lp.bottomMargin != 0) {
                                lp.bottomMargin = 0;
                                longTimeChartLast.setLayoutParams(lp);
                            }
                        }
                    } else {
                        throw new IllegalStateException("多日分时主、副图父布局必须是ChartLinearLayout");
                    }
                }
                mainViewTop = longTimeChartList.get(0).getTop();
            }
            canvas.drawRect(sw2,
                    sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2 - tts2, mPaint);
        } else {
            if (longTimeChartList.size() > 0) {
                View longTimeChartMain = longTimeChartList.get(0);
                if (longTimeChartMain.getParent() != null
                        && longTimeChartMain.getParent() instanceof ChartLinearLayout) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) longTimeChartMain.getLayoutParams();
                    if (lp.bottomMargin != tts2) {
                        lp.bottomMargin = tts2;
                        longTimeChartMain.setLayoutParams(lp);
                    }
                } else {
                    throw new IllegalStateException("多日分时主、副图父布局必须是ChartLinearLayout");
                }
                if (longTimeChartList.size() > 1) {
                    for (int i = 1; i < longTimeChartList.size(); i++) {
                        View kLineChartLast = longTimeChartList.get(i);
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
                mainViewTop = longTimeChartList.get(0).getTop();
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
        mPaint.setColor(lineColor);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 1; i < howManyDays; i++) {
            float drawX = strokeWidth + dayWidth * i;
            for (int j = 0; j < longTimeChartList.size(); j++) {
                View view = longTimeChartList.get(j);
                if (view.getVisibility() != View.VISIBLE) {
                    continue;
                }
                Rect mRect = new Rect();
                view.getHitRect(mRect);
                canvas.drawLine(drawX, mRect.top, drawX, mRect.bottom, mPaint);
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
        float scale;
        for (int i = 0; i < longTimeChartList.size(); i++) {
            View view = longTimeChartList.get(i);
            if (view.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (view.getParent() == null ||
                    !(view.getParent() instanceof ChartLinearLayout)) {
                continue;
            }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            Rect mRect = new Rect();
            view.getHitRect(mRect);
            float height = mRect.bottom - mRect.top;
            float y = mRect.top;
            if (longTimeChartList.get(i) instanceof LongTimeChartMainView) {
                scale = height / 4.0f;
                for (int j = 0; j < 4; j++) {
                    y += scale;
                    canvas.drawLine(0, y, getWidth(), y, mPaint);
                }
            } else {
                scale = height / 2.0f;
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
    private void drawDate(Canvas canvas) {
        String[] dates = this.dates;
        int howManyDays = this.howManyDays;
        if (dates != null) {
            mTextPaint.setColor(timeTextColor);
            mTextPaint.setTextSize(timeTextSize);
            float y, x;
            if (mTimeLocation == TimeLocation.BOTOOM) {
                y = height - timeTextSize;
            } else {
                y = height / 2.0f - timeTextSize;
                if (longTimeChartList.size() > 0) {
                    View timeChart0 = longTimeChartList.get(0);
                    if (timeChart0 != null) {
                        y = timeChart0.getHeight() + timeChart0.getTop() + timeTextSize;
                    }
                }
            }
            int count = dates.length - 1;
            int j = 0;
            float endPoint = width - strokeWidth;
            for (int i = count; i >= count - howManyDays; i--) {
                if (i < 0 || j >= howManyDays) {
                    break;
                }
                x = endPoint -
                        j * dayWidth - dayWidth / 2.0f -
                        mTextPaint.measureText(dates[i]) /
                                2.0f;
                canvas.drawText(dates[i], x, y, mTextPaint);
                j++;
            }
        }
    }

    /**
     * 添加主、副图
     *
     * @param view
     */
    private void addLongTimeChart(View view) {
        if (view == null)
            return;
        if (view instanceof LongTimeChartMainView
                || view instanceof LongTimeChartDeputyView) {
            longTimeChartList.add(view);
            if (view instanceof LongTimeChartMainView) {
                ((LongTimeChartMainView) view).setLongTimeChartFrameLayout(this);
            } else if (view instanceof LongTimeChartDeputyView) {
                ((LongTimeChartDeputyView) view).setLongTimeChartFrameLayout(this);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                addLongTimeChart(vg.getChildAt(i));
            }
        }
    }

    /**
     * 寻找浮窗
     *
     * @param view
     */
    private void seekFloatingWindow(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof TimeChartFrameLayout.OnTimeListener) {
            this.floatingWindow = view;
            removeView(floatingWindow);
            addView(floatingWindow);
            mLList.add((OnLongTimeListener) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                seekFloatingWindow(vg.getChildAt(i));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSupportEvent) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode = MODE_CLICK;
                touchX = lastX = downX = event.getX();
                touchY = lastY = downY = event.getY();
                if (handler != null) {
                    handler.sendEmptyMessageAtTime(MSG_LONG_PRESS, event.getDownTime() + 300);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                touchY = event.getY();
                if (mode == MODE_CLICK
                        && (Math.abs(touchY - downY) > TOUCH_SPACING)
                        && (Math.abs(touchX - downX) <= TOUCH_SPACING)) {
                    if (handler != null)
                        handler.removeMessages(MSG_LONG_PRESS);
                    if (touchY - downY > 0) {
                        mode = MODE_TO_DOWN;
                    } else {
                        mode = MODE_TO_UP;
                    }
                    lastX = downX = event.getX();
                    lastY = downY = event.getY();
                    return true;
                } else if (mode == MODE_CLICK
                        && (Math.abs(touchY - downY) <= TOUCH_SPACING)
                        && (Math.abs(touchX - downX) > TOUCH_SPACING)) {
                    if (handler != null)
                        handler.removeMessages(MSG_LONG_PRESS);
                    if (touchX - downX > 0) {
                        mode = MODE_TO_RIGHT;
                    } else {
                        mode = MODE_TO_LEFT;
                    }
                    lastX = downX = event.getX();
                    lastY = downY = event.getY();
                    return true;
                }
                if (mode == MODE_LONG_PRESS) {
                    if (longTimeDataList != null && longTimeDataList.size() > 0) {
                        calculationLocation(touchX, touchY);
                        mChartCrossView.setDrawLocation(drawX, drawY);
                    } else {
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnLongTimeListener l :
                                    mLList) {
                                l.onLongCursorVisible(false);
                            }
                        }
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mode == MODE_LONG_PRESS) {
                    mode = MODE_CLICK;
                    if (mChartCrossView.getVisibility() == View.VISIBLE) {
                        mChartCrossView.setVisibility(View.GONE);
                        for (OnLongTimeListener l :
                                mLList) {
                            l.onLongCursorVisible(false);
                        }
                    }
                } else {
                    if (handler != null)
                        handler.removeMessages(MSG_LONG_PRESS);
                    if (mode == MODE_CLICK) {
                        performClick();
                    }
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
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
        float dataSpacing = 0.0f;
        float scale = 0.0f;
        float mainChartHeight = 0.0f;
        if (longTimeChartList != null && longTimeChartList.size() > 0) {
            LongTimeChartMainView mainView = (LongTimeChartMainView) longTimeChartList.get(0);
            dataSpacing = mainView.getDataSpacing();
            scale = mainView.getScale();
            mainChartHeight = mainView.getHeight();

            int groupPosition, childPosition;
            float x = touchX < strokeWidth ? strokeWidth
                    : touchX > width - strokeWidth ? width - strokeWidth
                    : touchX;
            float dayPos = (x - strokeWidth) / dayWidth;
            groupPosition = (int) dayPos;
            if (groupPosition < 0) {
                groupPosition = 0;
            } else if (groupPosition >= howManyDays) {
                groupPosition = howManyDays - 1;
            }
            groupPosition = groupPosition - (howManyDays - longTimeDataList.size());

            if (groupPosition < 0) {
                groupPosition = 0;
            }

            LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> longTimeChildList = null;
            do {
                if (groupPosition <= -1)
                    break;
                longTimeChildList = longTimeDataList.get(groupPosition);
                if (longTimeChildList != null && longTimeChildList.size() > 0) {
                    break;
                } else {
                    groupPosition--;
                }
            } while (groupPosition >= -1);
            if (longTimeChildList == null || groupPosition <= -1) {
                if (mChartCrossView.getVisibility() == View.VISIBLE) {
                    mChartCrossView.setVisibility(View.GONE);
                    for (OnLongTimeListener l :
                            mLList) {
                        l.onLongCursorVisible(false);
                    }
                }
                return;
            }

            float startX =
                    (groupPosition + (howManyDays - longTimeDataList.size())) * dayWidth +
                            strokeWidth;
            childPosition = parse.parseInt(String.format(Locale.ENGLISH, "%.0f", (x - startX) / dataSpacing));
            if (childPosition >= mTimeConfigure.getMaxMinute()) {
                childPosition = mTimeConfigure.getMaxMinute() - 1;
            }
            if (childPosition >= longTimeChildList.size()) {
                childPosition = longTimeChildList.size() - 1;
            } else if (childPosition < 0) {
                childPosition = 0;
            }

            drawX = startX + dataSpacing * childPosition;
            drawY = (float) (mainChartHeight -
                    (longTimeChildList.get(childPosition).getClose() -
                            longTimeDataList.getLow()) *
                            scale + mainView.getTop());
            if (floatingWindow != null) {
                if (floatingWindow.getParent() == this) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) floatingWindow.getLayoutParams();
                    if (drawX > getWidth() / 2.0f) {
                        if (lp.leftMargin != 0) {
                            lp.leftMargin = 0;
                            floatingWindow.setLayoutParams(lp);
                        }
                    } else {
                        int leftMargin = (int) (width - floatingWindow.getWidth());
                        if (lp.leftMargin != leftMargin) {
                            lp.leftMargin = leftMargin;
                            floatingWindow.setLayoutParams(lp);
                        }
                    }
                }
            }
            if (mChartCrossView.getVisibility() == View.GONE) {
                mChartCrossView.setVisibility(View.VISIBLE);
                for (OnLongTimeListener l :
                        mLList) {
                    l.onLongCursorVisible(true);
                    l.onLongTimeListener(longTimeDataList, groupPosition, childPosition, drawX, drawY);
                }
            } else {
                for (OnLongTimeListener l :
                        mLList) {
                    l.onLongTimeListener(longTimeDataList, groupPosition, childPosition, drawX, drawY);
                }
            }
        }
    }

    /**
     * 计算线程
     */
    class LongTimeChartFrameLayoutThread extends BaseThread {

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
                if (mAdapter == null || mAdapter.getLongTimeDataList() == null) {
                    sendHandler(MSG_OK, null);
                    return;
                }
            } while (!isViewsAddAll);

            int groupCount = mAdapter.getGroupCount();
            LongTimeDataGroupList<LongTimeDataChildList<TimeData>> adapterGroupList = mAdapter.getLongTimeDataList();
            LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList = new LongTimeDataGroupList<>();
            longTimeDataList.setDaPan(adapterGroupList.isDaPan());
            longTimeDataList.setOpen(adapterGroupList.getOpen());
            longTimeDataList.setPreClose(adapterGroupList.getPreClose());
            longTimeDataList.setPreStatement(adapterGroupList.getPreStatement());
            double pre = longTimeDataList.getPre();
            boolean isDaPan = longTimeDataList.isDaPan();
            for (int i = 0; i < groupCount; i++) {
                if (isDestroy) {
                    return;
                }
                if (mAdapter == null) {
                    sendHandler(MSG_OK, null);
                    return;
                }
                LongTimeDataChildList<TimeData> adapterChildList = mAdapter.getGroupData(i);
                if (adapterChildList == null) {
                    sendHandler(MSG_OK, null);
                    return;
                }
                int childCount = mAdapter.getChildCount(i);
                LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> childList = new LongTimeDataChildList<>();
                childList.setTimeStamp(adapterChildList.getTimeStamp());
                if (isDaPan) {
                    double closes = 0;
                    for (int j = 0; j < childCount; j++) {
                        if (isDestroy) {
                            return;
                        }
                        if (mAdapter == null) {
                            sendHandler(MSG_OK, null);
                            return;
                        }
                        TimeData childItem = mAdapter.getChildData(i, j);
                        if (childItem == null) {
                            sendHandler(MSG_OK, null);
                            return;
                        }
                        closes += childItem.getClose();
                        long timeStamp = childItem.getTimeStamp();
                        double average = 0;
                        if (j == 0) {
                            average = closes;
                        } else {
                            average = closes / (j + 1);
                        }
                        if (Double.isInfinite(average) || Double.isNaN(average)) {
                            if (i == 0) {
                                average = childItem.getClose();
                            } else {
                                average = childList.get(j - 1).getAverage();
                            }
                        }
                        double vol = childItem.getVol();
                        double cje = childItem.getCje();
                        double close = childItem.getClose();
                        double riseAndFall = close - pre;
                        double were = riseAndFall / pre * 100;
                        childList.add(new TimeChartFrameLayout.TimeDataValid(timeStamp, close, vol, cje, average, were, riseAndFall));
                    }
                    longTimeDataList.add(childList);
                } else {
                    for (int j = 0; j < childCount; j++) {
                        if (isDestroy) {
                            return;
                        }
                        if (mAdapter == null) {
                            sendHandler(MSG_OK, null);
                            return;
                        }
                        TimeData item = mAdapter.getChildData(i, j);
                        if (item == null) {
                            sendHandler(MSG_OK, null);
                            return;
                        }

                        long timeStamp = item.getTimeStamp();
                        double average = item.getCje() / item.getVol();
                        if (Double.isInfinite(average) || Double.isNaN(average)) {
                            if (j == 0) {
                                average = item.getClose();
                            } else {
                                average = childList.get(j - 1).getAverage();
                            }
                        }
                        double vol = item.getVol();
                        double cje = item.getCje();
                        double close = item.getClose();
                        double riseAndFall = close - pre;
                        double were = riseAndFall / pre * 100;
                        childList.add(new TimeChartFrameLayout.TimeDataValid(timeStamp, close, vol, cje, average, were, riseAndFall));
                    }
                    longTimeDataList.add(childList);
                }
            }

            double maxHig = 0, minLow = 0;
            if (longTimeDataList.size() > 0) {
                LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> childList = longTimeDataList.get(0);
                if (childList.size() > 0) {
                    minLow = childList.get(0).getClose();
                }
            }
            for (int i = 0; i < longTimeDataList.size(); i++) {
                if (isDestroy) {
                    return;
                }
                LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> childList = longTimeDataList.get(i);
                for (int j = 0; j < childList.size(); j++) {
                    if (isDestroy) {
                        return;
                    }
                    double close = childList.get(j).getClose();
                    double average = childList.get(j).getAverage();
                    double max = close > average ? close : average;
                    double min = close < average ? close : average;
                    maxHig = maxHig > max ? maxHig : max;
                    minLow = minLow < min ? minLow : min;
                }
            }
            longTimeDataList.setHig(maxHig * 1.02f);
            longTimeDataList.setLow(minLow * 0.98f);

            double hig = longTimeDataList.getHig();
            double low = longTimeDataList.getLow();
            pre = longTimeDataList.getPre();
            if (hig >= pre && low <= pre) {
                if (hig - pre >= pre - low) {
                    longTimeDataList.setLow(pre - (hig - pre));
                } else {
                    longTimeDataList.setHig(pre + (pre - low));
                }
            } else if (hig >= pre && low >= pre) {
                longTimeDataList.setLow(pre - (hig - pre));
            } else {
                longTimeDataList.setHig(pre + (pre - low));
            }

            if (BaseThread.isAlive(mIndexThread)) {
                mIndexThread.onStop();
            }
            mIndexThread = new IndexThread(longTimeDataList);
            if (executorService != null && !executorService.isShutdown()) {
                executorService.execute(mIndexThread);
            }
        }
    }

    /**
     * 计算指标的线程
     */
    class IndexThread extends BaseThread {

        private LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList;
        private boolean isStop = false;

        public IndexThread(LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList) {
            this.longTimeDataList = longTimeDataList;
        }

        /**
         * 终止线程
         */
        public void onStop() {
            this.isStop = true;
        }

        @Override
        public void running() {
            try {
                if (longTimeDataList == null || longTimeDataList.size() <= 0) {
                    sendHandler(MSG_OK, null);
                    return;
                }
                for (int i = 0; i < longTimeChartList.size(); i++) {
                    if (isStop) {
                        return;
                    }
                    if (longTimeChartList.get(i) instanceof LongTimeChartDeputyView) {
                        LongTimeChartDeputyView ltcdView = (LongTimeChartDeputyView) longTimeChartList.get(i);
                        LongTimeChartDeputyView.LongTimeIndex mLongTimeIndex = ltcdView.getNewLongTimeIndex();

                        if (mLongTimeIndex == LongTimeChartDeputyView.LongTimeIndex.VOL) {
                            double indexHig = 0;
                            for (int j = 0; j < longTimeDataList.size(); j++) {
                                LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid> childList = longTimeDataList.get(j);
                                if (isStop) {
                                    return;
                                }
                                for (int k = 0; k < childList.size(); k++) {
                                    if (isStop) {
                                        return;
                                    }
                                    double vol = k == 0 ? childList.get(k).getVol()
                                            : childList.get(k).getVol() -
                                            childList.get(k - 1).getVol();
                                    double cje = k == 0 ? childList.get(k).getCje()
                                            : childList.get(k).getCje() -
                                            childList.get(k - 1).getCje();
                                    childList.get(k).setIndexVol(new TimeChartFrameLayout.TimeDataValid.Vol(vol, cje));
                                    indexHig = indexHig > vol ? indexHig : vol;

                                }

                            }
                            longTimeDataList.setIndexHigLow(mLongTimeIndex.getValue(), indexHig, 0);
                        }
                        /**
                         * 新增自定义指标算法
                         */
                        ltcdView.setTagLti(mLongTimeIndex);
                    }
                    if (isStop) {
                        return;
                    }
                }
                sendHandler(MSG_OK, longTimeDataList);
            } catch (Exception e) {
                e.printStackTrace();
                sendHandler(MSG_OK, null);
            }
        }
    }

    /**
     * 发消息到Handler
     *
     * @param what
     * @param obj
     */
    private void sendHandler(int what, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            if (handler != null)
                handler.sendMessage(msg);
        }
    }

    /**
     * 消息队列
     */
    class BaseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_OK:
                    longTimeDataList = (LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>>) msg.obj;
                    if (msg.obj == null) {
                        for (int i = 0; i < longTimeChartList.size(); i++) {
                            if (longTimeChartList.get(i) instanceof LongTimeChartMainView) {
                                LongTimeChartMainView mLongTimeChartMainView = (LongTimeChartMainView) longTimeChartList.get(i);
                                mLongTimeChartMainView.clear();
                            } else {
                                LongTimeChartDeputyView mLongTimeChartDeputyView = (LongTimeChartDeputyView) longTimeChartList.get(i);
                                mLongTimeChartDeputyView.clear();
                            }
                        }
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnLongTimeListener l :
                                    mLList) {
                                l.onLongCursorVisible(false);
                            }
                        }
                    } else {
                        for (int i = 0; i < longTimeChartList.size(); i++) {
                            if (longTimeChartList.get(i) instanceof LongTimeChartMainView)
                                ((LongTimeChartMainView) longTimeChartList.get(i)).setData(longTimeDataList);
                            else {
                                ((LongTimeChartDeputyView) longTimeChartList.get(i)).setData(longTimeDataList);
                            }
                        }
                        if (mode == MODE_LONG_PRESS) {
                            if (longTimeDataList.size() > 0) {
                                int childSize = 0;
                                for (int i = 0; i < longTimeDataList.size(); i++) {
                                    childSize = childSize < mAdapter.getChildCount(i)
                                            ? mAdapter.getChildCount(i) : childSize;
                                }
                                calculationLocation(touchX, touchY);
                                mChartCrossView.setDrawLocation(drawX, drawY);
                            } else {
                                if (mChartCrossView.getVisibility() == View.VISIBLE) {
                                    mChartCrossView.setVisibility(View.GONE);
                                    for (OnLongTimeListener l :
                                            mLList) {
                                        l.onLongCursorVisible(false);
                                    }
                                }
                            }
                        }
                    }
                    if (mAdapter != null) {
                        if (longTimeDataList != null) {
                            int groupCount = mAdapter.getGroupCount();
                            if (groupCount != longTimeDataList.size()) {
                                if (baseOnChartDataObserver != null)
                                    baseOnChartDataObserver.onChartData();
                            } else {
                                for (int i = 0; i < groupCount; i++) {
                                    if (longTimeDataList.get(i).size() !=
                                            mAdapter.getChildCount(i)) {
                                        if (baseOnChartDataObserver != null)
                                            baseOnChartDataObserver.onChartData();
                                        return;
                                    }
                                }
                            }
                        } else {
                            if (mAdapter.getGroupCount() != 0) {
                                if (baseOnChartDataObserver != null)
                                    baseOnChartDataObserver.onChartData();
                            }
                        }
                    }
                    if (longTimeDataList != null && longTimeDataList.size() > 0) {
                        dates = new String[longTimeDataList.size()];
                        for (int i = 0; i < dates.length; i++) {
                            String date = utils.date2String("yyyy/MM/dd HH:mm:ss", longTimeDataList.get(i).getTimeStamp());
                            dates[i] = date.substring(0, 10);
                        }
                    } else {
                        dates = null;
                    }
                    postInvalidate();
                    break;

                case MSG_RESTART_INDEX_THREAD:
                    if (!BaseThread.isAlive(longTimeChartFrameLayoutThread)) {
                        if (BaseThread.isAlive(mIndexThread)) {
                            mIndexThread.onStop();
                        }
                        mIndexThread = new IndexThread(longTimeDataList);
                        if (executorService != null && !executorService.isShutdown()) {
                            executorService.execute(mIndexThread);
                        }
                    }
                    break;

                case MSG_LONG_PRESS:
                    mode = MODE_LONG_PRESS;
                    ViewParent parent = getParent();
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(true);
                    if (longTimeDataList != null && longTimeDataList.size() > 0) {
                        calculationLocation(touchX, touchY);
                        mChartCrossView.setDrawLocation(drawX, drawY);
                    } else {
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnLongTimeListener l :
                                    mLList) {
                                l.onLongCursorVisible(false);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 时间配置
     */
    class TimeConfigure {
        private int maxMinute;// 总共时间,单位分钟

        public int getMaxMinute() {
            return maxMinute;
        }

        public void setMaxMinute(int maxMinute) {
            this.maxMinute = maxMinute;
        }
    }

    /**
     * 观察者
     */
    class BaseOnChartDataObserver implements OnChartDataObserver {

        @Override
        public void onChartData() {
            if (!BaseThread.isAlive(longTimeChartFrameLayoutThread)) {
                if (BaseThread.isAlive(mIndexThread)) {
                    mIndexThread.onStop();
                }
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.execute(longTimeChartFrameLayoutThread);
                }
            }
        }

    }

    /**
     * 更新指标
     */
    public void updateIndex() {
        sendHandler(MSG_RESTART_INDEX_THREAD, null);
    }

    /**
     * 设置多少日
     *
     * @param howManyDays
     */
    public void setHowManyDays(@HowManyDays int howManyDays) {
        if (this.howManyDays == howManyDays) {
            return;
        }
        this.howManyDays = howManyDays;
        postInvalidate();
        float sw2 = strokeWidth * 2.0f;
        dayWidth = (width - sw2) / howManyDays;
        for (int i = 0; i < longTimeChartList.size(); i++) {
            if (longTimeChartList.get(i) instanceof LongTimeChartMainView)
                ((LongTimeChartMainView) longTimeChartList.get(i)).setData(longTimeDataList);
            else {
                ((LongTimeChartDeputyView) longTimeChartList.get(i)).setData(longTimeDataList);
            }
        }
    }

    /**
     * 获取当前多少日
     *
     * @return 返回当前多少日
     */
    public int getHowManyDays() {
        return this.howManyDays;
    }

    /**
     * 获取一日占据宽度
     *
     * @return 返回一日需要占据的像素宽度
     */
    public float getDayWidth() {
        return dayWidth;
    }

    /**
     * 获取一日最大交易时间
     *
     * @return 返回一日最大交易时间
     */
    public int getMaxMinute() {
        return mTimeConfigure.getMaxMinute();
    }

    /**
     * 设置时间段
     *
     * @param timeSlotList
     */
    public void setTimeSlotList(List<TimeSlot> timeSlotList) {
        if (timeSlotList == null) {
            throw new NullPointerException("时间段集合不得为Null");
        } else if (timeSlotList.size() == 0) {
            throw new IllegalStateException("时间段集合长度不得为0");
        } else {
            int maxMinute = 0;
            for (int i = 0; i < timeSlotList.size(); i++) {
                String startTime = timeSlotList.get(i).getStartTime();
                String endTime = timeSlotList.get(i).getEndTime();
                String[] startTimes = startTime.split(":");
                String[] endTimes = endTime.split(":");
                int startH = parse.parseInt(startTimes[0]);
                int startM = parse.parseInt(startTimes[1]);
                int endH = parse.parseInt(endTimes[0]);
                int endM = parse.parseInt(endTimes[1]);
                if (utils.isMax(startTime, endTime, ":")) {
                    maxMinute += (((endH - startH) * 60) + (endM - startM));
                } else if (utils.isMin(startTime, endTime, ":")) {
                    maxMinute += (((endH + 24 - startH) * 60) + (endM - startM));
                } else {
                    maxMinute += 24 * 60;
                }
            }
            maxMinute += 1;
            mTimeConfigure.setMaxMinute(maxMinute);
            postInvalidate();
        }
    }

    /**
     * 设置适配器
     *
     * @param mAdapter
     */
    public void setAdapter(LongTimeChartAdapter mAdapter) {
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.unRegisterObserver(baseOnChartDataObserver);
            mAdapter.registerObserver(baseOnChartDataObserver);
        }
        this.mAdapter = mAdapter;
        if (baseOnChartDataObserver != null)
            baseOnChartDataObserver.onChartData();
    }

    /**
     * 获取适配器
     *
     * @return 返回适配器
     */
    public ChartAdapter getAdapter() {
        return this.mAdapter;
    }

    /**
     * 设置浮窗
     *
     * @param view 浮窗
     */
    public void setFloatingWindow(View view) {
        this.floatingWindow = view;
    }

    /**
     * 添加接口
     *
     * @param l
     */
    public void addOnLongTimeListener(OnLongTimeListener l) {
        mLList.add(l);
    }

    /**
     * 移除接口
     *
     * @param l
     */
    public void removeOnLongTimeListener(OnLongTimeListener l) {
        mLList.remove(l);
    }

    /**
     * 接口
     */
    public interface OnLongTimeListener {
        public void onLongCursorVisible(boolean isCursorVisible);

        public void onLongTimeListener(LongTimeDataGroupList<LongTimeDataChildList<TimeChartFrameLayout.TimeDataValid>> longTimeDataList, int groupPosition, int childPosition, float cursorX, float cursorY);
    }
}
