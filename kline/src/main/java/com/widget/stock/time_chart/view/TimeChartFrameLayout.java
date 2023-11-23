package com.widget.stock.time_chart.view;

import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.kedll.stock.library.base.BaseThread;
import com.widget.stock.ChartBaseFrameLayout;
import com.widget.stock.ChartBaseView;
import com.widget.stock.ChartCrossView;
import com.widget.stock.ChartLinearLayout;
import com.widget.stock.OnChartDataObserver;
import com.widget.stock.TimeLocation;
import com.widget.stock.adapter.ChartAdapter;
import com.widget.stock.time_chart.adapter.TimeChartAdapter;
import com.widget.stock.time_chart.data.TimeData;
import com.widget.stock.time_chart.data.TimeDataList;
import com.widget.stock.time_chart.data.TimeSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dingrui on 16/9/23.
 * 分时父布局,此Layout只绘制边线、纬线、经线
 */

public class TimeChartFrameLayout extends ChartBaseFrameLayout {

    private TimeChartAdapter mAdapter;
    private BaseOnChartDataObserver baseOnChartDataObserver;
    private BaseHandler handler;
    private BaseTimeChartPrameLayoutThread baseTimeChartPrameLayoutThread;
    private IndexThread mIndexThread;
    private boolean isDestroy = false;// 视图是否结束

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 分时主图、副图集合
     */
    private List<View> timeChartList = new ArrayList<>();// 主、副图集合
    private boolean isViewsAddAll = false;// 主、副图是否添加完毕

    /**
     * 十字光标View
     */
    private ChartCrossView mChartCrossView;

    private TimeConfigure mTimeConfigure = new TimeConfigure();// 时间表配置

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
    private float drawX;// 十字线绘制X点
    private float drawY;// 十字线绘制Y点

    /**
     * 数据相关
     */
    private TimeDataList<TimeDataValid> timeList;// 计算后的分时数据
    private float mainChartHeight;// 分时主图高度
    private float dataSpacing;// 每条数据间隔距离
    private float scale;// 主图比例

    /**
     * 接口
     */
    private List<OnTimeListener> mLList = new ArrayList<>();
    private View floatingWindow;

    public TimeChartFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public TimeChartFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimeChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TimeChartFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        executorService = Executors.newFixedThreadPool(1);
        baseTimeChartPrameLayoutThread = new BaseTimeChartPrameLayoutThread();
        baseOnChartDataObserver = new BaseOnChartDataObserver();
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
    }

    /**
     * 将分时主图和副图添加到集合
     *
     * @param view
     */
    private void addTimeChartView(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof TimeChartMainView
                || view instanceof TimeChartDeputyView) {
            if (!timeChartList.contains(view)) {
                timeChartList.add(view);
            }
            if (view instanceof TimeChartDeputyView) {
                ((TimeChartDeputyView) view).setTimeChartFrameLayout(this);
            } else {
                ((TimeChartMainView) view).setTimeChartFrameLayout(this);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                addTimeChartView(vg.getChildAt(i));
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
        if (view instanceof OnTimeListener) {
            this.floatingWindow = view;
            removeView(floatingWindow);
            addView(floatingWindow);
            mLList.add((OnTimeListener) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                seekFloatingWindow(vg.getChildAt(i));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (timeChartList.size() == 0) {
            addTimeChartView(this);
            mChartCrossView.setChartList(timeChartList);
            if (mChartCrossView.getParent() != null &&
                    (mChartCrossView.getParent() instanceof TimeChartFrameLayout)) {
                removeView(mChartCrossView);
            }
            addView(mChartCrossView);
            if (floatingWindow == null)
                seekFloatingWindow(this);
            isViewsAddAll = true;
        }

        /**
         * 绘制边框
         */
        drawStroke(canvas);

        /**
         * 绘制经线和时间
         */
        drawLngAndTime(canvas);

        /**
         * 绘制纬线
         */
        drawLat(canvas);
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
                    if (timeList != null && timeList.size() > 0) {
                        calculationLocation(touchX, touchY);
                        mChartCrossView.setDrawLocation(drawX, drawY);
                    } else {
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnTimeListener l :
                                    mLList) {
                                l.onCursorVisible(false);
                            }
                        }
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (mode == MODE_LONG_PRESS) {
                    mode = MODE_CLICK;
                    if (mChartCrossView.getVisibility() == View.VISIBLE) {
                        mChartCrossView.setVisibility(View.GONE);
                        for (OnTimeListener l :
                                mLList) {
                            l.onCursorVisible(false);
                        }
                    }
                    if (timeList != null && timeList.size() > 0) {
                        TimeDataValid timeDataValid = timeList.get(timeList.size() - 1);
                        TimeDataValid oldTimeDataValid = timeList.size() - 2 < 0
                                ? timeDataValid : timeList.get(
                                timeList.size() - 2);
                        for (OnTimeListener l :
                                mLList) {
                            l.onTimeEndDataChange(timeDataValid, oldTimeDataValid, timeList.getPre());
                        }
                    }
                } else {
                    if (handler != null)
                        handler.removeMessages(MSG_LONG_PRESS);
                    if (mode == MODE_CLICK) {
                        performClick();
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
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
        for (int i = 0; i < timeChartList.size(); i++) {
            View view = timeChartList.get(i);
            if (view instanceof TimeChartMainView
                    || view instanceof TimeChartDeputyView) {
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
        for (int i = 0; i < timeChartList.size(); i++) {
            View view = timeChartList.get(i);
            if (view instanceof TimeChartMainView
                    || view instanceof TimeChartDeputyView) {
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
        if (baseTimeChartPrameLayoutThread != null) {
            baseTimeChartPrameLayoutThread = null;
        }
        if (mIndexThread != null) {
            mIndexThread.onStop();
            mIndexThread = null;
        }
        if (timeList != null) {
            timeList.clear();
        }
        if (mAdapter != null && baseOnChartDataObserver != null) {
            mAdapter.unRegisterObserver(baseOnChartDataObserver);
            baseOnChartDataObserver = null;
        }
        if (timeChartList != null) {
            timeChartList.clear();
        }
        if (mTimeConfigure != null) {
            mTimeConfigure.clear();
        }
        if (mLList != null)
            mLList.clear();
        for (int i = 0; i < timeChartList.size(); i++) {
            View view = timeChartList.get(i);
            if (view instanceof TimeChartMainView
                    || view instanceof TimeChartDeputyView) {
                ((ChartBaseView) view).onDestroy();
            }
        }
    }

    @Override
    public void build() {

    }

    /**
     * 计算十字线位置
     *
     * @param touchX
     * @param touchY
     */
    private void calculationLocation(float touchX, float touchY) {
        if (timeChartList != null && timeChartList.size() > 0) {
            TimeChartMainView mainView = (TimeChartMainView) timeChartList.get(0);
            dataSpacing = mainView.getDataSpacing();
            scale = mainView.getScale();
            mainChartHeight = mainView.getHeight();
            float x = touchX < strokeWidth ? strokeWidth
                    : touchX > width - strokeWidth ? width - strokeWidth
                    : touchX;
            int position = parse.parseInt(String.format(Locale.ENGLISH, "%.0f", (x - strokeWidth) / dataSpacing));

            if (position >= mTimeConfigure.getMaxMinute()) {
                position = mTimeConfigure.getMaxMinute() - 1;
            }
            if (position >= timeList.size()) {
                position = timeList.size() - 1;
            } else if (position < 0) {
                position = 0;
            }

            drawX = position * dataSpacing + strokeWidth;
            drawY = (float) (mainChartHeight -
                    (timeList.get(position).getClose() -
                            timeList.getLow()) *
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
                for (OnTimeListener l :
                        mLList) {
                    l.onCursorVisible(true);
                    l.onTimeListener(timeList, position, drawX, drawY);
                }
            } else {
                for (OnTimeListener l :
                        mLList) {
                    l.onTimeListener(timeList, position, drawX, drawY);
                }
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
        boolean isSetBottomMargin = false;
        int mainViewTop = 0;
        if (mTimeLocation == TimeLocation.BOTOOM) {
            if (timeChartList.size() > 0) {
                for (int i = timeChartList.size() - 1; i >= 0; i--) {
                    View timeChartLast = timeChartList.get(i);
                    if (timeChartLast.getParent() != null
                            && timeChartLast.getParent() instanceof ChartLinearLayout) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) timeChartLast.getLayoutParams();
                        if (timeChartLast.getVisibility() == View.VISIBLE
                                && !isSetBottomMargin) {
                            isSetBottomMargin = true;
                            if (lp.bottomMargin != tts2) {
                                lp.bottomMargin = tts2;
                                timeChartLast.setLayoutParams(lp);
                            }
                        } else {
                            if (lp.bottomMargin != 0) {
                                lp.bottomMargin = 0;
                                timeChartLast.setLayoutParams(lp);
                            }
                        }
                    } else {
                        throw new IllegalStateException("分时主、副图父布局必须是ChartLinearLayout");
                    }
                }
                mainViewTop = timeChartList.get(0).getTop();
            }
            canvas.drawRect(sw2,
                    sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2 - tts2, mPaint);
        } else {
            if (timeChartList.size() > 0) {
                View timeChartMain = timeChartList.get(0);
                if (timeChartMain.getParent() != null
                        && timeChartMain.getParent() instanceof ChartLinearLayout) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) timeChartMain.getLayoutParams();
                    if (lp.bottomMargin != tts2) {
                        lp.bottomMargin = tts2;
                        timeChartMain.setLayoutParams(lp);
                    }
                } else {
                    throw new IllegalStateException("分时主、副图父布局必须是ChartLinearLayout");
                }
                if (timeChartList.size() > 1) {
                    for (int i = 1; i < timeChartList.size(); i++) {
                        View kLineChartLast = timeChartList.get(i);
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
                mainViewTop = timeChartList.get(0).getTop();
            }
            canvas.drawRect(sw2, sw2 + mainViewTop, getWidth() - sw2, getHeight() - sw2, mPaint);
        }
    }

    /**
     * 绘制经线和时间
     *
     * @param canvas
     */
    private void drawLngAndTime(Canvas canvas) {
        mTextPaint.setColor(timeTextColor);
        mTextPaint.setTextSize(timeTextSize);
        mPaint.setColor(lineColor);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        float sw2 = strokeWidth * 2.0f;
        float scale = (width - sw2) / (mTimeConfigure.getMaxMinute() - 1);
        float textY;
        if (mTimeLocation == TimeLocation.BOTOOM) {
            textY = height - timeTextSize;
        } else {
            textY = height / 2.0f - timeTextSize;
            if (timeChartList.size() > 0) {
                View timeChart0 = timeChartList.get(0);
                if (timeChart0 != null) {
                    textY = timeChart0.getHeight() + timeChart0.getTop() + timeTextSize;
                }
            }
        }
        for (int i = 0; i < mTimeConfigure.size(); i++) {
            if (i == 0) {
                canvas.drawText(mTimeConfigure.getTime(i), sw2,
                        textY, mTextPaint);
            } else if (i == mTimeConfigure.size() - 1) {
                canvas.drawText(mTimeConfigure.getTime(i),
                        width - sw2 - mTextPaint.measureText(mTimeConfigure.getTime(i)),
                        textY, mTextPaint);
            } else {
                float x = strokeWidth + (mTimeConfigure.getMinute(i) - 1) * scale;
                canvas.drawText(mTimeConfigure.getTime(i),
                        x
                                - mTextPaint.measureText(mTimeConfigure.getTime(i)) / 2.0f,
                        textY, mTextPaint);
                for (int j = 0; j < timeChartList.size(); j++) {
                    View view = timeChartList.get(j);
                    if (view.getVisibility() != View.VISIBLE) {
                        continue;
                    }
                    Rect mRect = new Rect();
                    view.getHitRect(mRect);
                    canvas.drawLine(x, mRect.top, x, mRect.bottom, mPaint);
                }
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
        for (int i = 0; i < timeChartList.size(); i++) {
            View view = timeChartList.get(i);
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
            if (timeChartList.get(i) instanceof TimeChartMainView) {
                float scale = height / 4.0f;
                for (int j = 0; j < 4; j++) {
                    y += scale;
                    canvas.drawLine(0, y, getWidth(), y, mPaint);
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
            mTimeConfigure.clear();
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
                maxMinute += 1;
                if (i == 0) {
                    mTimeConfigure.add(startTime, maxMinute);
                }
                mTimeConfigure.add(endTime, maxMinute);
            }
            mTimeConfigure.setMaxMinute(maxMinute);
            postInvalidate();
        }
    }

    /**
     * 获取最大交易时间
     *
     * @return 返回最大交易时间
     */
    public int getMaxMinute() {
        return mTimeConfigure.getMaxMinute();
    }

    /**
     * 时间表配置
     */
    class TimeConfigure {
        private int maxMinute;// 总共时间,单位分钟
        private List<Time> timeList = new ArrayList<>();// 需要绘制的时间集合

        public int getMaxMinute() {
            return maxMinute;
        }

        public void setMaxMinute(int maxMinute) {
            this.maxMinute = maxMinute;
        }

        /**
         * 清除需要绘制的时间集合
         */
        public void clear() {
            timeList.clear();
        }

        public int size() {
            return timeList.size();
        }

        public void add(String time, int minute) {
            timeList.add(new Time(time, minute));
        }

        public String getTime(int i) {
            return timeList.get(i).getTime();
        }

        public int getMinute(int i) {
            return timeList.get(i).getMinute();
        }

        class Time {
            public Time(String time, int minute) {
                this.time = time;
                this.minute = minute;
            }

            private String time;// 需要绘制的时间
            private int minute;// 需要绘制的时间占据总共的比例

            public String getTime() {
                return time;
            }

            public int getMinute() {
                return minute;
            }
        }
    }

    /**
     * 设置浮窗
     *
     * @param view
     */
    public void setFloatingWindow(View view) {
        this.floatingWindow = view;
    }

    /**
     * 设置接口
     *
     * @param l
     */
    public void addOnTimeListener(OnTimeListener l) {
        mLList.add(l);
    }

    public void removeOnTimeListener(OnTimeListener l) {
        mLList.remove(l);
    }

    /**
     * 设置适配器
     *
     * @param mAdapter
     */
    public void setAdapter(TimeChartAdapter mAdapter) {
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
     * 计算线程
     */
    class BaseTimeChartPrameLayoutThread extends BaseThread {
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
                if (mAdapter == null || mAdapter.getTimeDataList() == null) {
                    sendHandler(MSG_OK, null);
                    return;
                }
            } while (!isViewsAddAll);

            int count = mAdapter.getCount();
            TimeDataList<TimeData> adapterList = mAdapter.getTimeDataList();
            TimeDataList<TimeDataValid> list = new TimeDataList<>();
            list.setDaPan(adapterList.isDaPan());
            list.setHig(adapterList.getHig());
            list.setLow(adapterList.getLow());
            list.setOpen(adapterList.getOpen());
            list.setPreStatement(adapterList.getPreStatement());
            list.setPreClose(adapterList.getPreClose());
            double pre = list.getPre();
            double hig = list.getHig();
            double low = list.getLow();

            if (list.isDaPan()) {
                double closes = 0;
                for (int i = 0; i < count; i++) {
                    if (isDestroy) {
                        return;
                    }
                    if (mAdapter == null) {
                        sendHandler(MSG_OK, null);
                        return;
                    }
                    TimeData item = mAdapter.getData(i);
                    if (item != null) {
                        closes += item.getClose();
                        long timeStamp = item.getTimeStamp();
                        double average = 0;
                        if (i == 0) {
                            average = closes;
                        } else {
                            average = closes / (i + 1);
                        }
                        if (Double.isInfinite(average) || Double.isNaN(average)) {
                            if (i == 0) {
                                average = item.getClose();
                            } else {
                                average = list.get(i - 1).getAverage();
                            }
                        }
                        double vol = item.getVol();
                        double cje = item.getCje();
                        double close = item.getClose();
                        double riseAndFall = close - pre;
                        double were = riseAndFall / pre * 100;
                        list.add(new TimeDataValid(timeStamp, close, vol, cje, average, were, riseAndFall));
                    } else {
                        sendHandler(MSG_OK, null);
                        return;
                    }
                }
            } else {
                for (int i = 0; i < count; i++) {
                    if (isDestroy) {
                        return;
                    }
                    if (mAdapter == null) {
                        sendHandler(MSG_OK, null);
                        return;
                    }
                    TimeData item = mAdapter.getData(i);
                    if (item != null) {
                        long timeStamp = item.getTimeStamp();
                        double average = item.getCje() / item.getVol();
                        if (Double.isInfinite(average) || Double.isNaN(average)) {
                            if (i == 0) {
                                average = item.getClose();
                            } else {
                                average = list.get(i - 1).getAverage();
                            }
                        }
                        double vol = item.getVol();
                        double cje = item.getCje();
                        double close = item.getClose();
                        double riseAndFall = close - pre;
                        double were = riseAndFall / pre * 100;
                        list.add(new TimeDataValid(timeStamp, close, vol, cje, average, were, riseAndFall));
                    } else {
                        sendHandler(MSG_OK, null);
                        return;
                    }
                }
            }
            if (hig <= 0.0f || low <= 0.0f) {
                double maxHig = 0;
                double minLow = 0;
                if (list.size() > 0) {
                    minLow = list.get(0).getClose();
                }
                for (int i = 0; i < list.size(); i++) {
                    if (isDestroy) {
                        return;
                    }
                    double close = list.get(i).getClose();
                    double average = list.get(i).getAverage();
                    double max = close > average ? close : average;
                    double min = close < average ? close : average;
                    maxHig = maxHig > max ? maxHig : max;
                    minLow = minLow < min ? minLow : min;
                }
                list.setHig(maxHig * 1.02f);
                list.setLow(minLow * 0.98f);
            }
            hig = list.getHig();
            low = list.getLow();
            pre = list.getPre();
            if (hig >= pre && low <= pre) {
                if (hig - pre >= pre - low) {
                    list.setLow(pre - (hig - pre));
                } else {
                    list.setHig(pre + (pre - low));
                }
            } else if (hig >= pre && low >= pre) {
                list.setLow(pre - (hig - pre));
            } else {
                list.setHig(pre + (pre - low));
            }
            if (BaseThread.isAlive(mIndexThread)) {
                mIndexThread.onStop();
            }
            mIndexThread = new IndexThread(list);
            if (executorService != null && !executorService.isShutdown()) {
                executorService.execute(mIndexThread);
            }
        }

    }

    /**
     * 计算指标的线程
     */
    class IndexThread extends BaseThread {

        private TimeDataList<TimeDataValid> list;
        private boolean isStop = false;

        public IndexThread(TimeDataList<TimeDataValid> list) {
            this.list = list;
        }

        public void onStop() {
            this.isStop = true;
        }

        @Override
        public void running() {
            try {
                if (list == null || list.size() <= 0) {
                    sendHandler(MSG_OK, null);
                    return;
                }
                for (int i = 0; i < timeChartList.size(); i++) {
                    if (isStop) {
                        return;
                    }
                    if (timeChartList.get(i) instanceof TimeChartDeputyView) {
                        TimeChartDeputyView tcdView = (TimeChartDeputyView) timeChartList.get(i);
                        TimeChartDeputyView.TimeIndex mTimeIndex = tcdView.getNewTimeIndex();
                        double vol, cje;
                        for (int j = 0; j < list.size(); j++) {
                            if (isStop) {
                                return;
                            }
                            vol = (j == 0 ? list.get(j).getVol()
                                    : list.get(j).getVol() - list.get(j - 1).getVol()) /
                                    100.0f;
                            cje = j == 0 ? list.get(j).getCje()
                                    : list.get(j).getCje() - list.get(j - 1).getCje();
                            list.get(j).setIndexVol(new TimeDataValid.Vol(vol, cje));
                        }
                        if (mTimeIndex == TimeChartDeputyView.TimeIndex.VOL) {
                            double indexHig = 0;
                            for (int j = 0; j < list.size(); j++) {
                                if (isStop) {
                                    return;
                                }
                                TimeDataValid.Vol Vol = list.get(j).getIndexVol();
                                vol = Vol.getVol();
//                            cje = Vol.getCje();
                                indexHig = indexHig > vol ? indexHig : vol;
                            }
                            list.setIndexHigLow(mTimeIndex.getValue(), indexHig, 0);
                        }
                        /**
                         * 新增自定义指标算法
                         */
                        tcdView.setTagTi(mTimeIndex);
                    }
                }
                sendHandler(MSG_OK, list);
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
                    timeList = (TimeDataList<TimeDataValid>) msg.obj;
                    if (timeList == null) {
                        for (int i = 0; i < timeChartList.size(); i++) {
                            if (timeChartList.get(i) instanceof TimeChartMainView) {
                                TimeChartMainView mTimeChartMainView = (TimeChartMainView) timeChartList.get(i);
                                mTimeChartMainView.clear();
                            } else {
                                TimeChartDeputyView mTimeChartDeputyView = (TimeChartDeputyView) timeChartList.get(i);
                                mTimeChartDeputyView.clear();
                            }
                        }
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnTimeListener l :
                                    mLList) {
                                l.onCursorVisible(false);
                            }
                        }
                    } else {
                        for (int i = 0; i < timeChartList.size(); i++) {
                            if (timeChartList.get(i) instanceof TimeChartMainView)
                                ((TimeChartMainView) timeChartList.get(i)).setData(timeList);
                            else {
                                ((TimeChartDeputyView) timeChartList.get(i)).setData(timeList);
                            }
                        }
                        if (mode == MODE_LONG_PRESS) {
                            if (timeList.size() > 0) {
                                calculationLocation(touchX, touchY);
                                mChartCrossView.setDrawLocation(drawX, drawY);
                            } else {
                                if (mChartCrossView.getVisibility() == View.VISIBLE) {
                                    mChartCrossView.setVisibility(View.GONE);
                                    for (OnTimeListener l :
                                            mLList) {
                                        l.onCursorVisible(false);
                                    }
                                }
                            }
                        } else {
                            if (timeList.size() > 0) {
                                TimeDataValid timeDataValid = timeList.get(timeList.size() - 1);
                                TimeDataValid oldTimeDataValid = timeList.size() - 2 < 0
                                        ? timeDataValid : timeList.get(
                                        timeList.size() - 2);
                                for (OnTimeListener l :
                                        mLList) {
                                    l.onTimeEndDataChange(timeDataValid, oldTimeDataValid, timeList.getPre());
                                }
                            }
                        }
                    }
                    if (mAdapter != null) {
                        if (timeList != null) {
                            if (mAdapter.getCount() != timeList.size()) {
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
                    break;

                case MSG_RESTART_INDEX_THREAD:
                    if (!BaseThread.isAlive(baseTimeChartPrameLayoutThread)) {
                        if (BaseThread.isAlive(mIndexThread)) {
                            mIndexThread.onStop();
                        }
                        mIndexThread = new IndexThread(timeList);
                        if (executorService != null && !executorService.isShutdown()) {
                            executorService.execute(mIndexThread);
                        }
                    }
                    break;

                case MSG_LONG_PRESS:
                    mode = MODE_LONG_PRESS;
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                    if (timeList != null && timeList.size() > 0) {
                        calculationLocation(touchX, touchY);
                        mChartCrossView.setDrawLocation(drawX, drawY);
                    } else {
                        if (mChartCrossView.getVisibility() == View.VISIBLE) {
                            mChartCrossView.setVisibility(View.GONE);
                            for (OnTimeListener l :
                                    mLList) {
                                l.onCursorVisible(false);
                            }
                        }
                    }
                    break;
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
     * 观察者
     */
    class BaseOnChartDataObserver implements OnChartDataObserver {

        @Override
        public void onChartData() {
            if (!BaseThread.isAlive(baseTimeChartPrameLayoutThread)) {
                if (BaseThread.isAlive(mIndexThread)) {
                    mIndexThread.onStop();
                }
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.execute(baseTimeChartPrameLayoutThread);
                }
            }
        }
    }

    /**
     * 计算后有效分时数据
     */
    public static class TimeDataValid extends TimeData {

        private static final long serialVersionUID = -4973642697819772300L;
        /**
         * 均价
         */
        private double average;// 均价
        /**
         * 涨幅
         */
        private double were;// 涨幅
        /**
         * 涨跌额
         */
        private double riseAndFall;// 涨跌额
        /**
         * 成交量指标
         */
        private Vol indexVol;


        /**
         * 构造器
         *
         * @param timeStamp   时间戳
         * @param close       收盘价
         * @param vol         成交量（递增）
         * @param cje         成交额
         * @param average     均价
         * @param were        涨幅
         * @param riseAndFall 涨跌额
         */
        public TimeDataValid(long timeStamp, double close, double vol, double cje,
                             double average, double were, double riseAndFall) {
            super(timeStamp, close, vol, cje);
            this.average = average;
            this.were = were;
            this.riseAndFall = riseAndFall;
        }

        public double getAverage() {
            return average;
        }

        public double getWere() {
            return were;
        }

        public double getRiseAndFall() {
            return riseAndFall;
        }

        public void setIndexVol(Vol indexVol) {
            this.indexVol = indexVol;
        }

        public Vol getIndexVol() {
            return indexVol;
        }

        /**
         * 成交量指标
         */
        public static class Vol {
            private double vol, // 成交量
                    cje;// 成交额

            public Vol(double vol, double cje) {
                this.vol = vol;
                this.cje = cje;
            }

            public double getVol() {
                return vol;
            }

            public double getCje() {
                return cje;
            }

        }

    }

    /**
     * 接口
     */
    public interface OnTimeListener {

        /**
         * 十字光标是否显示中
         *
         * @param isCursorVisible true显示，false不显示
         */
        public void onCursorVisible(boolean isCursorVisible);

        /**
         * 传递数据
         *
         * @param timeList 分时数据
         * @param position 触摸位置
         * @param cursorX  触摸X点
         */
        public void onTimeListener(TimeDataList<TimeDataValid> timeList, int position, float cursorX, float cursorY);

        /**
         * 最新数据回调
         *
         * @param endTimeData 最新数据
         * @param preTimeData 上一周期数据
         * @param pre         昨结或昨收
         */
        public void onTimeEndDataChange(TimeDataValid endTimeData, TimeDataValid preTimeData, double pre);
    }
}
