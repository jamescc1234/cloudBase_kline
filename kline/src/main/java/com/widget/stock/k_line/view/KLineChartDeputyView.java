package com.widget.stock.k_line.view;

import static com.widget.stock.k_line.view.KLineChartMainView.convertArray;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.contants.KlineConfig;
import com.widget.stock.ChartBaseView;
import com.widget.stock.k_line.KLineSetUtils;
import com.widget.stock.k_line.data.KLineDataValid;
import com.widget.stock.k_line.data.KLineSetModelConfig;
import com.widget.stock.utils.SaveObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import binance.stock.library.R;

/**
 * Created by dingrui on 2016/10/24.
 * K线副图
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class KLineChartDeputyView extends ChartBaseView {

    private KLineChartFrameLayout mKLineChartFrameLayout;// 根布局
    private KLineDeputyIndex mKLineDeputyIndex = KLineDeputyIndex.VOL,// 预设指标
            tagKLI = mKLineDeputyIndex;// 计算好的指标

    /**
     * 数据相关
     */
    private ArrayList<KLineDataValid> kLineDataList;// K线数据
    private float width, height;// 控件宽高
    private float kLineWidth;// 当前K线宽度(像素单位)
    private float kLineSpacing;// (像素单位)
    private int maxKLineLength;// 界面总共需要显示的条数
    private int location;// 当前滚动位置
    private float kLineXCenter;// 当前K线宽度的中间值
    private double maxData = 0;// 当前最大数据
    private double minData = 0;// 当前最小数据
    private float scale;// 比例

    private double symbolExrate = 1;// 币种汇率

    private static final String BUY = "B",
            SELL = "S";

    /**
     * 绘制相关
     */
    private Path mPath1;
    private Path mPath2;

    /**
     * 指标颜色相关
     */
    private int[] indexColors;// 当前指标颜色


    private Map<String, int[]> indexParameter,// 指标参数
            tagIndexParameter;// 计算之后指标参数
    private final String PARAMETER_FILEPATH = ".KLINE",// K线参数缓存目录
            PARAMETER_FILE = ".deputyChart",// K线副图参数缓存文件名
            VERSION = "1.0.2";// 缓存版本（修改指标必须更改版本）
    private File paramenterFile;// K线副图参数文件路径

    /**
     * 自定义指标
     */
    public KLineSetModelConfig configMACD = KLineSetUtils.INSTANCE.getConfig("MACD",false);
    public KLineSetModelConfig configKDJ = KLineSetUtils.INSTANCE.getConfig("KDJ",false);
    public KLineSetModelConfig configRSI = KLineSetUtils.INSTANCE.getConfig("RSI",false);

    /**
     * 副图指标
     */
    public enum KLineDeputyIndex implements Serializable {
        VOM("VOM"),
        VOL("VOL", 5, 10, 20),
        MACD("MACD", 12, 26, 9),
        KDJ("KDJ", 9, 3, 3),
        RSI("RSI", 6, 12, 24),
        BIAS("BIAS", 6, 12, 24),
        ARBR("ARBR", 26),
        CCI("CCI", 14),
        DMI("DMI", 14, 6),
        CR("CR", 26, 5, 10, 20),
        PSY("PSY", 12),
        DMA("DMA", 10, 50, 10),
        TRIX("TRIX", 12, 20),
        KDJ_BS("KDJ", 9, 3, 3),
        RSI_BS("RSI", 6, 12, 24),
        MACD_BS("MACD", 12, 26, 9);

        public final static KLineDeputyIndex[] DEPUTY_INDEX_S = {VOL, MACD, KDJ, RSI, BIAS, ARBR,
                CCI,
                DMI, CR, PSY, DMA, TRIX, VOM,
                KDJ_BS, RSI_BS, MACD_BS};

        private String value;

        private int[] parameter;

        KLineDeputyIndex(String value, int... parameter) {
            this.value = value;
            this.parameter = parameter;
        }

        public String getValue() {
            return value;
        }

        public int[] getParameter() {
            return parameter;
        }
    }

    public KLineChartDeputyView(Context context) {
        super(context);
        init(context, null);
    }

    public KLineChartDeputyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KLineChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KLineChartDeputyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        if (indexParameter == null) {
            indexParameter = new HashMap<>();
            for (int i = 0; i < KLineDeputyIndex.DEPUTY_INDEX_S.length; i++) {
                String name = KLineDeputyIndex.DEPUTY_INDEX_S[i].getValue();
                if (name.equals("MACD")) {
                    indexParameter.put("MACD", convertArray(KLineSetUtils.INSTANCE.getParams(configMACD)));
                } else if (name.equals("KDJ")) {
                    indexParameter.put("KDJ", convertArray(KLineSetUtils.INSTANCE.getParams(configKDJ)));
                }else if (name.equals("RSI")) {
                    indexParameter.put("RSI", convertArray(KLineSetUtils.INSTANCE.getParams(configRSI)));
                }else {
                    indexParameter.put(KLineDeputyIndex.DEPUTY_INDEX_S[i].getValue(),
                            KLineDeputyIndex.DEPUTY_INDEX_S[i].getParameter());
                }
            }
        }
        tagIndexParameter = indexParameter;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KLineChartDeputyView);
            int index = ta.getInt(R.styleable.KLineChartDeputyView_indexDeputy, 0);
            KLineDeputyIndex kLineDeputyIndex = KLineDeputyIndex.DEPUTY_INDEX_S[index];
            setKLineDeputyIndex(kLineDeputyIndex);
            ta.recycle();
        }
        setKLineDeputyIndex(mKLineDeputyIndex);
    }

    @Override
    public void onResume() {
        isDestroy = false;
        setData(kLineDataList);
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
        if (kLineDataList != null) {
            kLineDataList.clear();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            setData(this.kLineDataList);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((int) width != getWidth() ||
                (int) height != getHeight()) {
            width = getWidth();
            height = getHeight();
            setData(kLineDataList);
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
        if (tagKLI == KLineDeputyIndex.VOL) {
            drawVOL(canvas);
        } else if (tagKLI == KLineDeputyIndex.MACD) {
            drawMACD(canvas);
        } else if (tagKLI == KLineDeputyIndex.KDJ) {
            drawKDJ(canvas);
        } else if (tagKLI == KLineDeputyIndex.RSI) {
            drawRSI(canvas);
        } else if (tagKLI == KLineDeputyIndex.BIAS) {
            drawBIAS(canvas);
        } else if (tagKLI == KLineDeputyIndex.ARBR) {
            drawARBR(canvas);
        } else if (tagKLI == KLineDeputyIndex.CCI) {
            drawCCI(canvas);
        } else if (tagKLI == KLineDeputyIndex.DMI) {
            drawDMI(canvas);
        } else if (tagKLI == KLineDeputyIndex.CR) {
            drawCR(canvas);
        } else if (tagKLI == KLineDeputyIndex.PSY) {
            drawPSY(canvas);
        } else if (tagKLI == KLineDeputyIndex.DMA) {
            drawDMA(canvas);
        } else if (tagKLI == KLineDeputyIndex.TRIX) {
            drawTRIX(canvas);
        } else if (tagKLI == KLineDeputyIndex.VOM) {
            drawVOM(canvas);
        } else if (tagKLI == KLineDeputyIndex.KDJ_BS) {
            drawKDJ_BS(canvas);
        } else if (tagKLI == KLineDeputyIndex.RSI_BS) {
            drawRSI_BS(canvas);
        } else if (tagKLI == KLineDeputyIndex.MACD_BS) {
            drawMACD_BS(canvas);
        }
    }

    /**
     * 绘制VOM指标
     *
     * @param canvas
     */
    private void drawVOM(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getVOMColors();
        mPaint.setStrokeWidth(kLineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        float startX;
        float xCenter;
        float Y;
        double close, refClose;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;
        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index < 0 || index >= kLineDataList.size()) {
                break;
            }

            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing)
                    * i;
            xCenter = startX + kLineXCenter;

            KLineDataValid entity = kLineDataList.get(index);
            close = entity.getClose();
            Y = (float) (height
                    - (entity.getCje() - minData) * scale);

            if (index > 0) {
                KLineDataValid refEntity = kLineDataList.get(index - 1);
                refClose = refEntity.getClose();
            } else {
                refClose = entity.getClose();
            }
            if (close < refClose) {
                mPaint.setColor(indexColors[1]);
                canvas.drawLine(xCenter, Y, xCenter, height, mPaint);
            } else {
                mPaint.setColor(indexColors[0]);
                canvas.drawLine(xCenter, Y, xCenter, height, mPaint);
            }
        }

    }

    /**
     * 绘制VOL指标
     *
     * @param canvas
     */
    private void drawVOL(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getVOLColors();
        mPaint.setStrokeWidth(kLineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        float startX;
        float xCenter;
        float Y;
        double close, refClose;
//        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                : maxKLineLength;
        int count = maxKLineLength;
        int index;
        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index >= kLineDataList.size()) {
                break;
            } else if (index < 0) {
                continue;
            }

            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing)
                    * i;
            xCenter = startX + kLineXCenter;

            KLineDataValid entity = kLineDataList.get(index);
            close = entity.getClose();
            Y = (float) (height
                    - (entity.getVol() - minData) * scale);

            if (index > 0) {
                KLineDataValid refEntity = kLineDataList.get(index - 1);
                refClose = refEntity.getClose();
            } else {
                refClose = entity.getOpen();
            }
            if (close < refClose) {
                mPaint.setColor(indexColors[1]);
                canvas.drawLine(xCenter, Y, xCenter, height, mPaint);
            } else {
                mPaint.setColor(indexColors[0]);
                canvas.drawLine(xCenter, Y, xCenter, height, mPaint);
            }
        }

        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(indexColors[2]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getVolMA().getmA1() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getVolMA().getmA1() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[3]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[1] - 1).getVolMA().getmA2() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getVolMA().getmA2() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[4]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[2]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[2] - 1).getVolMA().getmA3() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[2] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getVolMA().getmA3() - minData) * scale);
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
     * 绘制MACD指标
     *
     * @param canvas
     */
    private void drawMACD(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getMACDColors();
        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configMACD))[0]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configMACD))[0]);
        float startX;
        float xCenter;
        float Y;
//        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                : maxKLineLength;
        int count = maxKLineLength;
        int index;
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[1] - 1).getMACD().getDiff() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if ( index >= kLineDataList.size()) {
                    break;
                } else if (index < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getMACD().getDiff() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configMACD))[1]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configMACD))[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1] +
                tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[1] +
                    tagIndexParameter.get(tagKLI.getValue())[2] -
                    2).getMACD().getDea() - minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (tagIndexParameter.get(tagKLI.getValue())[1] +
                            tagIndexParameter.get(tagKLI.getValue())[2] -
                            2 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index >= kLineDataList.size()) {
                    break;
                } else if (index < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] +
                        tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getMACD().getDea() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setStyle(Paint.Style.STROKE);
        float zero = (float) (height - (0 - minData) * scale);
        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index >= kLineDataList.size()) {
                break;
            } else if (index < 0) {
                continue;
            }
            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
            xCenter = startX + kLineXCenter;
            if (index < tagIndexParameter.get(tagKLI.getValue())[1] +
                    tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                continue;
            }
            KLineDataValid entity = kLineDataList.get(index);
            Y = (float) (height
                    - (entity.getMACD().getMacd() - minData) * scale);
            if (Y < zero) {
                mPaint.setColor(indexColors[2]);
            } else {
                mPaint.setColor(indexColors[3]);
            }
            canvas.drawLine(xCenter, Y, xCenter, zero, mPaint);
        }
    }

    /**
     * 绘制MACD买卖指标
     *
     * @param canvas
     */
    private void drawMACD_BS(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getMACDBSColors();
        mPaint.setStrokeWidth(lineWidth);
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(textSize);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;
        float r = kLineWidth / 2f;
        if (r < lineWidth + 1) {
            r = lineWidth + 1;
        }
        RectF rectF = new RectF();
        float textWidth;
        float padding = (float) textSize / 6f;
        float arrowH = 8f, arrowW = 8f;

        if (mPath1 == null) {
            mPath1 = new Path();
        }
        if (mPath2 == null) {
            mPath2 = new Path();
        }

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[1] - 1).getMACD_BS().getDiff() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getMACD_BS().getDiff() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1] +
                tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[1] +
                    tagIndexParameter.get(tagKLI.getValue())[2] -
                    2).getMACD_BS().getDea() - minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (tagIndexParameter.get(tagKLI.getValue())[1] +
                            tagIndexParameter.get(tagKLI.getValue())[2] -
                            2 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] +
                        tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getMACD_BS().getDea() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
                if (entity.getMACD_BS().isDrawBuy()) {
                    mPaint.setColor(indexColors[4]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(BUY);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(BUY,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);

                } else if (entity.getMACD_BS().isDrawSell()) {
                    mPaint.setColor(indexColors[5]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(SELL);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(SELL,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);
                }
            }
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(indexColors[3]);
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setStyle(Paint.Style.STROKE);
        float zero = (float) (height - (0 - minData) * scale);
        for (int i = 0; i < count; i++) {
            index = location + i;
            if (index < 0 || index >= kLineDataList.size()) {
                break;
            }
            startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
            xCenter = startX + kLineXCenter;
            if (index < tagIndexParameter.get(tagKLI.getValue())[1] +
                    tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                continue;
            }
            KLineDataValid entity = kLineDataList.get(index);
            Y = (float) (height
                    - (entity.getMACD_BS().getMacd() - minData) * scale);
            if (Y < zero) {
                mPaint.setColor(indexColors[2]);
            } else {
                mPaint.setColor(indexColors[3]);
            }
            canvas.drawLine(xCenter, Y, xCenter, zero, mPaint);
        }
    }

    /**
     * 绘制KDJ指标
     *
     * @param canvas
     */
    private void drawKDJ(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getKDJColors();
        float startX;
        float xCenter;
        float Y;
//        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                : maxKLineLength;
        int count = maxKLineLength;
        int index;

        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[0]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[0]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ().getK() - minData) *
                    scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[1]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[1]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ().getD() - minData) *
                    scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[2]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[2]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ().getJ() - minData) *
                    scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[0]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[0]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if ( index >= kLineDataList.size()) {
                    break;
                } else if (index  < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ().getK() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[1]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[1]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ().getD() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configKDJ))[2]);
            mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configKDJ))[2]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index >= kLineDataList.size()) {
                    break;
                } else if (index  < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ().getJ() - minData) * scale);
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
     * 绘制KDJ买卖指标
     *
     * @param canvas
     */
    private void drawKDJ_BS(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getKDJBSColors();
        mPaint.setStrokeWidth(lineWidth);
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(textSize);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        float r = kLineWidth / 2f;
        if (r < lineWidth + 1) {
            r = lineWidth + 1;
        }

        RectF rectF = new RectF();
        float textWidth;
        float padding = (float) textSize / 6f;
        float arrowH = 8f, arrowW = 8f;

        if (mPath1 == null) {
            mPath1 = new Path();
        }
        if (mPath2 == null) {
            mPath2 = new Path();
        }

        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(indexColors[0]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ_BS().getK() - minData) *
                    scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            mPaint.setColor(indexColors[1]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ_BS().getD() - minData) *
                    scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            mPaint.setColor(indexColors[2]);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getKDJ_BS().getJ() - minData) *
                    scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            mPaint.setColor(indexColors[0]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ_BS().getK() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(indexColors[1]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ_BS().getD() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getKDJ_BS().getJ() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }

                if (entity.getKDJ_BS().isDrawBuy()) {
                    mPaint.setColor(indexColors[3]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(BUY);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(BUY,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);
                } else if (entity.getKDJ_BS().isDrawSell()) {
                    mPaint.setColor(indexColors[4]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(SELL);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(SELL,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);
                }
            }
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(indexColors[2]);
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 绘制RSI指标
     *
     * @param canvas
     */
    private void drawRSI(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getRSIColors();
        float startX;
        float xCenter;
        float Y;
//        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                : maxKLineLength;
        int count = maxKLineLength;
        int index;

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configRSI))[0]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configRSI))[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getRSI().getRsi1() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index >= kLineDataList.size()) {
                    break;
                } else if (index < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI().getRsi1() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configRSI))[1]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configRSI))[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[1]).getRSI().getRsi2() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index >= kLineDataList.size()) {
                    break;
                } else if (index < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI().getRsi2() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(convertArray(KLineSetUtils.INSTANCE.getLineColors(configRSI))[2]);
        mPaint.setStrokeWidth(convertArray(KLineSetUtils.INSTANCE.getLineWidth(configRSI))[2]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[2] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[2]).getRSI().getRsi3() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[2] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if ( index >= kLineDataList.size()) {
                    break;
                } else if (index < 0) {
                    continue;
                }

                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[2]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI().getRsi3() - minData) * scale);
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
     * 绘制RSI买卖指标
     *
     * @param canvas
     */
    private void drawRSI_BS(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getRSIBSColors();
        mPaint.setStrokeWidth(lineWidth);
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(textSize);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        float r = kLineWidth / 2f;
        if (r < lineWidth + 1) {
            r = lineWidth + 1;
        }

        RectF rectF = new RectF();
        float textWidth;
        float padding = (float) textSize / 6f;
        float arrowH = 8f, arrowW = 8f;

        if (mPath1 == null) {
            mPath1 = new Path();
        }
        if (mPath2 == null) {
            mPath2 = new Path();
        }

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getRSI_BS().getRsi1() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPath.reset();
            mPaint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI_BS().getRsi1() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
                if (entity.getRSI_BS().isDrawBuy()) {
                    mPaint.setColor(indexColors[3]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(BUY);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(BUY,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);
                } else if (entity.getRSI_BS().isDrawSell()) {
                    mPaint.setColor(indexColors[4]);
                    canvas.drawCircle(xCenter, Y, r, mPaint);

                    textWidth = mTextPaint.measureText(SELL);
                    rectF.left = xCenter - textWidth / 2f - padding;
                    rectF.top = Y + padding + r;
                    rectF.right = rectF.left + textWidth + padding * 2f;
                    rectF.bottom = rectF.top + textSize + padding * 2f;
                    if (rectF.bottom > height) {
                        rectF.top = Y - (float) textSize - padding * 2f - arrowH - r;
                        rectF.bottom = rectF.top + textSize + padding * 2f;

                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.bottom);
                        }
                        mPath2.lineTo(xCenter, rectF.bottom + arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.bottom);
                        mPath2.close();
                    } else {
                        if (mPath2.isEmpty()) {
                            mPath2.moveTo(xCenter - arrowW / 2f, rectF.top);
                        }
                        mPath2.lineTo(xCenter, rectF.top - arrowH);
                        mPath2.lineTo(xCenter + arrowW / 2f, rectF.top);
                        mPath2.close();
                    }

                    mPath1.addRoundRect(rectF, 5, 5, Path.Direction.CCW);
                    mPath1.op(mPath2, Path.Op.UNION);

                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(mPath1, mPaint);
                    mPath1.reset();
                    mPath2.reset();

                    canvas.drawText(SELL,
                            xCenter - textWidth / 2f,
                            rectF.top + padding + textSize - 3, mTextPaint);
                }
            }
            mPaint.setColor(indexColors[0]);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[1]).getRSI_BS().getRsi2() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI_BS().getRsi2() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[2]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[2] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[2]).getRSI_BS().getRsi3() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[2] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[2]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getRSI_BS().getRsi3() - minData) * scale);
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
     * 绘制BIAS指标
     *
     * @param canvas
     */
    private void drawBIAS(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getBIASColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getBIAS().getBias1() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getBIAS().getBias1() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[1] - 1).getBIAS().getBias2() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getBIAS().getBias2() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[2]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[2]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[2] - 1).getBIAS().getBias3() -
                    minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[2] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getBIAS().getBias3() - minData) * scale);
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
     * 绘制ARBR指标
     *
     * @param canvas
     */
    private void drawARBR(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getARBRColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getBRAR().getAr() - minData) *
                    scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getBRAR().getAr() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getBRAR().getBr() -
                            minData) * scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getBRAR().getBr() - minData) * scale);
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
     * 绘制CCI指标
     *
     * @param canvas
     */
    private void drawCCI(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getCCIColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0]) {
            mPaint.setStyle(Paint.Style.FILL);
            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[0] - 1).getCCI().getCci() - minData) *
                    scale);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - 1 - location);
            xCenter = startX + kLineXCenter;
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getCCI().getCci() - minData) * scale);
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
     * 绘制DMI指标
     *
     * @param canvas
     */
    private void drawDMI(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getDMIColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - location);
            xCenter = startX + kLineXCenter;

            mPaint.setColor(indexColors[0]);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getDMI().getPdi() -
                            minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

            mPaint.setColor(indexColors[1]);
            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getDMI().getMdi() -
                            minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            mPaint.setColor(indexColors[0]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMI().getPdi() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);

            mPath.reset();
            mPaint.setColor(indexColors[1]);
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMI().getMdi() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[2]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] +
                tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (tagIndexParameter.get(tagKLI.getValue())[0] +
                            tagIndexParameter.get(tagKLI.getValue())[1] -
                            1 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0] +
                    tagIndexParameter.get(tagKLI.getValue())[1] -
                    1).getDMI().getAdx() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] +
                        tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMI().getAdx() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[3]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] +
                tagIndexParameter.get(tagKLI.getValue())[1] * 2 - 2) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (tagIndexParameter.get(tagKLI.getValue())[0] +
                            tagIndexParameter.get(tagKLI.getValue())[1] *
                                    2 - 2 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0] +
                    tagIndexParameter.get(tagKLI.getValue())[1] *
                            2 - 2).getDMI().getAdxr() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0] +
                        tagIndexParameter.get(tagKLI.getValue())[1] * 2 - 2) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMI().getAdxr() - minData) * scale);
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
     * 绘制CR指标
     *
     * @param canvas
     */
    private void drawCR(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getCRColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        int p0 = tagIndexParameter.get(tagKLI.getValue())[0],
                pm0 = p0 - 1,
                p1 = tagIndexParameter.get(tagKLI.getValue())[1],
                pm1 = p1 - 1,
                p2 = tagIndexParameter.get(tagKLI.getValue())[2],
                pm2 = p2 - 1,
                p3 = tagIndexParameter.get(tagKLI.getValue())[3];
        int ref0 = (int) (p1 / 2.5f + 1);
        int r0 = pm0 + p1 + ref0;

        int ref1 = (int) (p2 / 2.5f + 1);
        int r1 = pm0 + p2 + ref1;

        int ref2 = (int) (p3 / 2.5f + 1);
        int r2 = pm0 + p3 + ref2;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == p0 + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (p0 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(p0).getCR().getCr() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < p0) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getCR().getCr() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == r0 + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (r0 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(r0).getCR().getMa1() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < r0) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getCR().getMa1() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[2]);
        if (kLineDataList.size() == r1 + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (r1 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(r1).getCR().getMa2() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < r1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getCR().getMa2() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[3]);
        if (kLineDataList.size() == r2 + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (r2 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(r2).getCR().getMa3() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < r2) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getCR().getMa3() - minData) * scale);
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
     * 绘制PSY指标
     *
     * @param canvas
     */
    private void drawPSY(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getPSYColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[0] + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[0] - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height -
                    (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[0]).getPSY().getPsy() -
                            minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[0]) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getPSY().getPsy() - minData) * scale);
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
     * 绘制DMA指标
     *
     * @param canvas
     */
    private void drawDMA(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getDMAColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) *
                            (tagIndexParameter.get(tagKLI.getValue())[1] - 1 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(
                    tagIndexParameter.get(tagKLI.getValue())[1] - 1).getDMA().getDif() - minData) *
                    scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMA().getDif() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == tagIndexParameter.get(tagKLI.getValue())[1]
                + tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (tagIndexParameter.get(tagKLI.getValue())[1]
                            + tagIndexParameter.get(tagKLI.getValue())[2] -
                            2 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height - (kLineDataList.get(tagIndexParameter.get(tagKLI.getValue())[1]
                    + tagIndexParameter.get(tagKLI.getValue())[2] -
                    2).getDMA().getAma() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < tagIndexParameter.get(tagKLI.getValue())[1]
                        + tagIndexParameter.get(tagKLI.getValue())[2] -
                        2) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getDMA().getAma() - minData) * scale);
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
     * 绘制TRIX指标
     *
     * @param canvas
     */
    private void drawTRIX(Canvas canvas) {
        if (kLineDataList == null || kLineDataList.size() <= 0)
            return;
        indexColors = mKLineChartFrameLayout.getColorsConfigure().getTRIXColors();
        mPaint.setStrokeWidth(lineWidth);
        float startX;
        float xCenter;
        float Y;
        int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
                : maxKLineLength;
        int index;

        int p0 = tagIndexParameter.get(tagKLI.getValue())[0],
                p1 = tagIndexParameter.get(tagKLI.getValue())[1];
        int pm0 = p0 - 1;
        int pm1 = pm0 * 2;
        int pm2 = pm0 * 3;

        mPaint.setColor(indexColors[0]);
        if (kLineDataList.size() == pm2 + 2) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (pm2 + 1 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height -
                    (kLineDataList.get(pm2 + 1).getTRIX().getTrix() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < pm2 + 1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getTRIX().getTrix() - minData) * scale);
                if (mPath.isEmpty()) {
                    mPath.moveTo(xCenter, Y);
                } else {
                    mPath.lineTo(xCenter, Y);
                }
            }
            canvas.drawPath(mPath, mPaint);
        }

        mPaint.setColor(indexColors[1]);
        if (kLineDataList.size() == pm2 + p1 + 1) {
            mPaint.setStyle(Paint.Style.FILL);
            startX = mKLineChartFrameLayout.getStrokeWidth() +
                    (kLineWidth + kLineSpacing) * (pm2 + p1 - location);
            xCenter = startX + kLineXCenter;

            Y = (float) (height -
                    (kLineDataList.get(pm2 + p1).getTRIX().getMaTrix() - minData) * scale);
            canvas.drawCircle(xCenter, Y, 3, mPaint);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            mPath.reset();
            for (int i = 0; i < count; i++) {
                index = location + i;
                if (index < 0 || index >= kLineDataList.size()) {
                    break;
                }
                startX = mKLineChartFrameLayout.getStrokeWidth() + (kLineWidth + kLineSpacing) * i;
                xCenter = startX + kLineXCenter;
                if (index < pm2 + p1) {
                    continue;
                }
                KLineDataValid entity = kLineDataList.get(index);
                Y = (float) (height
                        - (entity.getTRIX().getMaTrix() - minData) * scale);
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
     * 绘制刻度
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        if (!isShowText) {
            return;
        }
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        float y = height / 2.0f;
        double hig = maxData;
        double low = minData;
        float spacing = (float) ((hig - low) / 2.0f);
        float strokeWidth2 = mKLineChartFrameLayout.getStrokeWidth() * 2.0f;
        if (tagKLI == KLineDeputyIndex.VOL) {
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.BEGINNING
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = stockUtils.parse2CNStringVol(hig, 2);
                        float width = mTextPaint.measureText(text);

                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2, textSize, mTextPaint);
                    }
                } else if (i == 2) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.END
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {

                        String text = stockUtils.parse2CNStringVol(low, 2);
                        float width = mTextPaint.measureText(text);

                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2, y * i - 2, mTextPaint);
                    }
                } else {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.MIDDLE
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)) {
                        String text = stockUtils.parse2CNStringVol(hig, 2);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2,
                                y * i + textSize / 2 - 2, mTextPaint);
                    }
                }
                hig -= spacing;
            }
        } else if (tagKLI == KLineDeputyIndex.VOM) {
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.BEGINNING
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = stockUtils.parse2CNStringCJE(hig * symbolExrate, false);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2, textSize, mTextPaint);
                    }
                } else if (i == 2) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.END
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = stockUtils.parse2CNStringCJE(low * symbolExrate, false);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2, y * i - 2, mTextPaint);
                    }
                } else {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.MIDDLE
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)) {
                        String text = stockUtils.parse2CNStringCJE(hig * symbolExrate, false);
                        float width = mTextPaint.measureText(text);
                        canvas.drawText(
                                text,
                                getWidth() - width - strokeWidth2,
                                y * i + textSize / 2 - 2, mTextPaint);
                    }
                }
                hig -= spacing;
            }
        } else {
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.BEGINNING
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = KlineConfig.SHOW_US_PRICE
                                ? stockUtils.parse2USString(hig * symbolExrate, false, decPlace)
                                : stockUtils.parse2String(hig * symbolExrate, decPlace, false);
                        float width = mTextPaint.measureText(text);

                        canvas.drawText(text,
                                getWidth() - width - strokeWidth2, textSize, mTextPaint);
                    }
                } else if (i == 2) {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.END
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.END)) {
                        String text = KlineConfig.SHOW_US_PRICE
                                ? stockUtils.parse2USString(low * symbolExrate, false, decPlace) :
                                stockUtils.parse2String(low * symbolExrate, decPlace, false);
                        float width = mTextPaint.measureText(text);

                        canvas.drawText(text,
                                getWidth() - width - strokeWidth2, y * i - 2, mTextPaint);
                    }
                } else {
                    if (scaleRule == ChartBaseView.ALL
                            || scaleRule == ChartBaseView.MIDDLE
                            || scaleRule == (ChartBaseView.END | ChartBaseView.MIDDLE)
                            || scaleRule ==
                            (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE | ChartBaseView.END)
                            || scaleRule == (ChartBaseView.BEGINNING | ChartBaseView.MIDDLE)) {
                        String text = KlineConfig.SHOW_US_PRICE
                                ? stockUtils.parse2USString(hig * symbolExrate, false, decPlace) :
                                stockUtils.parse2String(hig * symbolExrate, decPlace, false);
                        float width = mTextPaint.measureText(text);

                        canvas.drawText(text,
                                getWidth() - width - strokeWidth2,
                                y * i + textSize / 2 - 2, mTextPaint);
                    }
                }
                hig -= spacing;
            }
        }
    }

    @Override
    public void build() {
        setData(this.kLineDataList);
    }

    /**
     * 设置是否显示刻度
     *
     * @param isShowText
     * @return
     */
    public KLineChartDeputyView setShowText(boolean isShowText) {
        this.isShowText = isShowText;
        return this;
    }

    /**
     * 设置数据
     *
     * @param kLineDataList
     */
    public void setData(ArrayList<KLineDataValid> kLineDataList) {
        if (isDestroy) {
            return;
        }
        this.kLineDataList = kLineDataList;
        if (getVisibility() == View.GONE || getVisibility() == View.INVISIBLE) {
            return;
        }
        if (mKLineChartFrameLayout != null) {
            kLineWidth = mKLineChartFrameLayout.getkLineWidth();
            kLineSpacing = mKLineChartFrameLayout.getkLineSpacing();
            maxKLineLength = mKLineChartFrameLayout.getMaxKLineLength();
            location = mKLineChartFrameLayout.getLocation();
            kLineXCenter = kLineWidth / 2.0f;
            if (this.kLineDataList != null && this.kLineDataList.size() > location) {
//                int count = this.kLineDataList.size() < maxKLineLength ? this.kLineDataList.size()
//                        : maxKLineLength;
                int count = maxKLineLength;
                double maxData = 0;
                double minData = Double.MAX_VALUE;
                int position;
                if (tagKLI == KLineDeputyIndex.VOL) {
                    if (location >= 0) {
                        maxData = kLineDataList.get(location).getVol();
                        minData = 0;
                    } else {
                        maxData = kLineDataList.get(0).getVol();
                        minData = 0;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);
                        maxData = maxData > item.getVol() ? maxData : item.getVol();

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            maxData = maxData > item.getVolMA().getmA1() ? maxData
                                    : item.getVolMA().getmA1();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            maxData = maxData > item.getVolMA().getmA2() ? maxData
                                    : item.getVolMA().getmA2();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
                            maxData = maxData > item.getVolMA().getmA3() ? maxData
                                    : item.getVolMA().getmA3();
                        }
                    }
                } else if (tagKLI == KLineDeputyIndex.MACD) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                        maxData = kLineDataList.get(location).getMACD().getDiff();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            maxData = maxData > item.getMACD().getDiff() ? maxData
                                    : item.getMACD().getDiff();
                            minData = minData < item.getMACD().getDiff() ? minData
                                    : item.getMACD().getDiff();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1]
                                + tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                            maxData = maxData > item.getMACD().getDea() ? maxData
                                    : item.getMACD().getDea();
                            minData = minData < item.getMACD().getDea() ? minData
                                    : item.getMACD().getDea();

                            maxData = maxData > item.getMACD().getMacd() ? maxData
                                    : item.getMACD().getMacd();
                            minData = minData < item.getMACD().getMacd() ? minData
                                    : item.getMACD().getMacd();
                        }
                    }
                    if (maxData == 0 && minData > maxData) {
                        maxData = 1;
                        minData = -1;
                    }
                    double upSpacing = Math.abs(maxData - 0.0f),
                            downSpacing = Math.abs(minData - 0.0f);
                    if (upSpacing > downSpacing) {
                        minData = 0.0f - upSpacing;
                    } else {
                        maxData = downSpacing + 0.0f;
                    }
                } else if (tagKLI == KLineDeputyIndex.MACD_BS) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                        maxData = kLineDataList.get(location).getMACD_BS().getDiff();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            maxData = maxData > item.getMACD_BS().getDiff() ? maxData
                                    : item.getMACD_BS().getDiff();
                            minData = minData < item.getMACD_BS().getDiff() ? minData
                                    : item.getMACD_BS().getDiff();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1]
                                + tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                            maxData = maxData > item.getMACD_BS().getDea() ? maxData
                                    : item.getMACD_BS().getDea();
                            minData = minData < item.getMACD_BS().getDea() ? minData
                                    : item.getMACD_BS().getDea();

                            maxData = maxData > item.getMACD_BS().getMacd() ? maxData
                                    : item.getMACD_BS().getMacd();
                            minData = minData < item.getMACD_BS().getMacd() ? minData
                                    : item.getMACD_BS().getMacd();
                        }
                    }
                    if (maxData == 0 && minData > maxData) {
                        maxData = 1;
                        minData = -1;
                    }
                    double upSpacing = Math.abs(maxData - 0.0f),
                            downSpacing = Math.abs(minData - 0.0f);
                    if (upSpacing > downSpacing) {
                        minData = 0.0f - upSpacing;
                    } else {
                        maxData = downSpacing + 0.0f;
                    }
                } else if (tagKLI == KLineDeputyIndex.KDJ) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                        maxData = kLineDataList.get(location).getKDJ().getK();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getKDJ().getK() ? maxData
                                : item.getKDJ().getK();
                        minData = minData < item.getKDJ().getK() ? minData
                                : item.getKDJ().getK();

                        maxData = maxData > item.getKDJ().getD() ? maxData
                                : item.getKDJ().getD();
                        minData = minData < item.getKDJ().getD() ? minData
                                : item.getKDJ().getD();

                        maxData = maxData > item.getKDJ().getJ() ? maxData
                                : item.getKDJ().getJ();
                        minData = minData < item.getKDJ().getJ() ? minData
                                : item.getKDJ().getJ();
                    }
                } else if (tagKLI == KLineDeputyIndex.KDJ_BS) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                        maxData = kLineDataList.get(location).getKDJ_BS().getK();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getKDJ_BS().getK() ? maxData
                                : item.getKDJ_BS().getK();
                        minData = minData < item.getKDJ_BS().getK() ? minData
                                : item.getKDJ_BS().getK();

                        maxData = maxData > item.getKDJ_BS().getD() ? maxData
                                : item.getKDJ_BS().getD();
                        minData = minData < item.getKDJ_BS().getD() ? minData
                                : item.getKDJ_BS().getD();

                        maxData = maxData > item.getKDJ_BS().getJ() ? maxData
                                : item.getKDJ_BS().getJ();
                        minData = minData < item.getKDJ_BS().getJ() ? minData
                                : item.getKDJ_BS().getJ();
                    }
                } else if (tagKLI == KLineDeputyIndex.RSI) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                        maxData = kLineDataList.get(location).getRSI().getRsi1();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        } else if (position < 0) {
                            continue;
                        }

                        KLineDataValid item = kLineDataList.get(position);

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                            maxData = maxData > item.getRSI().getRsi1() ? maxData
                                    : item.getRSI().getRsi1();
                            minData = minData < item.getRSI().getRsi1() ? minData
                                    : item.getRSI().getRsi1();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1]) {
                            maxData = maxData > item.getRSI().getRsi2() ? maxData
                                    : item.getRSI().getRsi2();
                            minData = minData < item.getRSI().getRsi2() ? minData
                                    : item.getRSI().getRsi2();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[2]) {
                            maxData = maxData > item.getRSI().getRsi3() ? maxData
                                    : item.getRSI().getRsi3();
                            minData = minData < item.getRSI().getRsi3() ? minData
                                    : item.getRSI().getRsi3();
                        }
                    }
                } else if (tagKLI == KLineDeputyIndex.RSI_BS) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                        maxData = kLineDataList.get(location).getRSI_BS().getRsi1();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                            maxData = maxData > item.getRSI_BS().getRsi1() ? maxData
                                    : item.getRSI_BS().getRsi1();
                            minData = minData < item.getRSI_BS().getRsi1() ? minData
                                    : item.getRSI_BS().getRsi1();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1]) {
                            maxData = maxData > item.getRSI_BS().getRsi2() ? maxData
                                    : item.getRSI_BS().getRsi2();
                            minData = minData < item.getRSI_BS().getRsi2() ? minData
                                    : item.getRSI_BS().getRsi2();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[2]) {
                            maxData = maxData > item.getRSI_BS().getRsi3() ? maxData
                                    : item.getRSI_BS().getRsi3();
                            minData = minData < item.getRSI_BS().getRsi3() ? minData
                                    : item.getRSI_BS().getRsi3();
                        }
                    }
                } else if (tagKLI == KLineDeputyIndex.BIAS) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                        maxData = kLineDataList.get(location).getBIAS().getBias1();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            maxData = maxData > item.getBIAS().getBias1() ? maxData
                                    : item.getBIAS().getBias1();
                            minData = minData < item.getBIAS().getBias1() ? minData
                                    : item.getBIAS().getBias1();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            maxData = maxData > item.getBIAS().getBias2() ? maxData
                                    : item.getBIAS().getBias2();
                            minData = minData < item.getBIAS().getBias2() ? minData
                                    : item.getBIAS().getBias2();
                        }

                        if (position >= tagIndexParameter.get(tagKLI.getValue())[2] - 1) {
                            maxData = maxData > item.getBIAS().getBias3() ? maxData
                                    : item.getBIAS().getBias3();
                            minData = minData < item.getBIAS().getBias3() ? minData
                                    : item.getBIAS().getBias3();
                        }
                    }
                } else if (tagKLI == KLineDeputyIndex.ARBR) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                        maxData = kLineDataList.get(location).getBRAR().getAr();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getBRAR().getAr() ? maxData
                                : item.getBRAR().getAr();
                        minData = minData < item.getBRAR().getAr() ? minData
                                : item.getBRAR().getAr();

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0]) {
                            continue;
                        }
                        maxData = maxData > item.getBRAR().getBr() ? maxData
                                : item.getBRAR().getBr();
                        minData = minData < item.getBRAR().getBr() ? minData
                                : item.getBRAR().getBr();
                    }
                } else if (tagKLI == KLineDeputyIndex.CCI) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                        maxData = kLineDataList.get(location).getCCI().getCci();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getCCI().getCci() ? maxData
                                : item.getCCI().getCci();
                        minData = minData < item.getCCI().getCci() ? minData
                                : item.getCCI().getCci();
                    }
                } else if (tagKLI == KLineDeputyIndex.DMI) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                        maxData = kLineDataList.get(location).getDMI().getPdi();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0]) {
                            continue;
                        }
                        maxData = maxData > item.getDMI().getPdi() ? maxData
                                : item.getDMI().getPdi();
                        minData = minData < item.getDMI().getPdi() ? minData
                                : item.getDMI().getPdi();
                        maxData = maxData > item.getDMI().getMdi() ? maxData
                                : item.getDMI().getMdi();
                        minData = minData < item.getDMI().getMdi() ? minData
                                : item.getDMI().getMdi();

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] +
                                tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getDMI().getAdx() ? maxData
                                : item.getDMI().getAdx();
                        minData = minData < item.getDMI().getAdx() ? minData
                                : item.getDMI().getAdx();

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0] +
                                tagIndexParameter.get(tagKLI.getValue())[1] * 2 - 2) {
                            continue;
                        }
                        maxData = maxData > item.getDMI().getAdxr() ? maxData
                                : item.getDMI().getAdxr();
                        minData = minData < item.getDMI().getAdxr() ? minData
                                : item.getDMI().getAdxr();
                    }
                } else if (tagKLI == KLineDeputyIndex.CR) {
                    int p0 = tagIndexParameter.get(tagKLI.getValue())[0],
                            pm0 = p0 - 1,
                            p1 = tagIndexParameter.get(tagKLI.getValue())[1],
                            pm1 = p1 - 1,
                            p2 = tagIndexParameter.get(tagKLI.getValue())[2],
                            pm2 = p2 - 1,
                            p3 = tagIndexParameter.get(tagKLI.getValue())[3];
                    int ref0 = (int) (p1 / 2.5f + 1);
                    int r0 = pm0 + p1 + ref0;

                    int ref1 = (int) (p2 / 2.5f + 1);
                    int r1 = pm1 + p2 + ref1;

                    int ref2 = (int) (p3 / 2.5f + 1);
                    int r2 = pm2 + p3 + ref2;
                    if (location >= p0) {
                        maxData = kLineDataList.get(location).getCR().getCr();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0]) {
                            continue;
                        }
                        maxData = maxData > item.getCR().getCr() ? maxData
                                : item.getCR().getCr();
                        minData = minData < item.getCR().getCr() ? minData
                                : item.getCR().getCr();

                        if (position >= r0) {
                            maxData = maxData > item.getCR().getMa1() ? maxData
                                    : item.getCR().getMa1();
                            minData = minData < item.getCR().getMa1() ? minData
                                    : item.getCR().getMa1();
                        }

                        if (position >= r1) {
                            maxData = maxData > item.getCR().getMa2() ? maxData
                                    : item.getCR().getMa2();
                            minData = minData < item.getCR().getMa2() ? minData
                                    : item.getCR().getMa2();
                        }

                        if (position >= r2) {
                            maxData = maxData > item.getCR().getMa3() ? maxData
                                    : item.getCR().getMa3();
                            minData = minData < item.getCR().getMa3() ? minData
                                    : item.getCR().getMa3();
                        }
                    }
                } else if (tagKLI == KLineDeputyIndex.PSY) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[0]) {
                        maxData = kLineDataList.get(location).getPSY().getPsy();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[0]) {
                            continue;
                        }
                        maxData = maxData > item.getPSY().getPsy() ? maxData
                                : item.getPSY().getPsy();
                        minData = minData < item.getPSY().getPsy() ? minData
                                : item.getPSY().getPsy();
                    }
                } else if (tagKLI == KLineDeputyIndex.DMA) {
                    if (location >= tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                        maxData = kLineDataList.get(location).getDMA().getDif();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < tagIndexParameter.get(tagKLI.getValue())[1] - 1) {
                            continue;
                        }
                        maxData = maxData > item.getDMA().getDif() ? maxData
                                : item.getDMA().getDif();
                        minData = minData < item.getDMA().getDif() ? minData
                                : item.getDMA().getDif();

                        if (position < tagIndexParameter.get(tagKLI.getValue())[1] +
                                tagIndexParameter.get(tagKLI.getValue())[2] - 2) {
                            continue;
                        }
                        maxData = maxData > item.getDMA().getAma() ? maxData
                                : item.getDMA().getAma();
                        minData = minData < item.getDMA().getAma() ? minData
                                : item.getDMA().getAma();
                    }
                } else if (tagKLI == KLineDeputyIndex.TRIX) {
                    int p0 = tagIndexParameter.get(tagKLI.getValue())[0],
                            p1 = tagIndexParameter.get(tagKLI.getValue())[1];
                    int pm0 = p0 - 1;
                    int pm1 = pm0 * 2;
                    int pm2 = pm0 * 3;
                    if (location > pm2) {
                        maxData = kLineDataList.get(location).getTRIX().getTrix();
                        minData = maxData;
                    }
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);

                        if (position < pm2 + 1) {
                            continue;
                        }
                        maxData = maxData > item.getTRIX().getTrix() ? maxData
                                : item.getTRIX().getTrix();
                        minData = minData < item.getTRIX().getTrix() ? minData
                                : item.getTRIX().getTrix();

                        if (position < pm2 + p1) {
                            continue;
                        }
                        maxData = maxData > item.getTRIX().getMaTrix() ? maxData
                                : item.getTRIX().getMaTrix();
                        minData = minData < item.getTRIX().getMaTrix() ? minData
                                : item.getTRIX().getMaTrix();
                    }
                } else if (tagKLI == KLineDeputyIndex.VOM) {
                    maxData = kLineDataList.get(location).getCje();
                    minData = maxData;
                    for (int i = 0; i < count; i++) {
                        position = location + i;
                        if (position >= kLineDataList.size()) {
                            break;
                        }
                        KLineDataValid item = kLineDataList.get(position);
                        maxData = maxData > item.getCje() ? maxData : item.getCje();
                        minData = minData < item.getCje() ? minData : item.getCje();
                    }
                }
                if (maxData == 0 && minData > maxData) {
                    minData = 0;
                }
                if (maxData == minData) {
                    if (maxData > 0) {
                        minData = 0;
                    } else if (maxData < 0) {
                        maxData = 0;
                    } else {
                        maxData = 1;
                    }
                }
                if (tagKLI == KLineDeputyIndex.VOM
                        || tagKLI == KLineDeputyIndex.VOL) {
                    this.maxData = Math.max(maxData + minData * 0.1f, maxData);
                    this.minData = Math.max(minData - (maxData - minData) * 0.01f, 0);
                    this.minData = this.minData < 0 ? 0 : this.minData;
                } else {
                    double diff = maxData - minData;
                    this.maxData = Math.max(maxData + diff * topRate, maxData);
                    this.minData = minData - diff * 0.05f;
                }

                if (this.maxData == this.minData) {
                    this.maxData = this.minData + 1f;
                    this.minData = this.minData - 1f;
                }
                scale = (float) (height / (this.maxData - this.minData));
                if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                    scale = 0;
                }
            } else {
                this.maxData = 2;
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
                ondraw(mCanvas);
                isDraw = false;
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
     * @param mKLineDeputyIndex
     * @param parameter
     * @return true成功，false失败
     */
    public boolean setParameter(KLineDeputyIndex mKLineDeputyIndex, int... parameter) {
        switch (mKLineDeputyIndex.getValue()) {
            case "MACD": {
                configMACD = KLineSetUtils.INSTANCE.getConfig("MACD",false);
            }
            case "KDJ": {
                configKDJ = KLineSetUtils.INSTANCE.getConfig("KDJ",false);
            }
            case "RSI": {
                configRSI = KLineSetUtils.INSTANCE.getConfig("RSI",false);
            }
        }
        if (mKLineDeputyIndex == null || mKLineDeputyIndex.getParameter() == null ||
                parameter == null) {
            return false;
        }
        if (mKLineDeputyIndex.getParameter().length != parameter.length) {
            return false;
        }
        indexParameter.put(mKLineDeputyIndex.getValue(), parameter);
        SaveObject.getInstance().saveObject(paramenterFile, indexParameter);
        if (mKLineChartFrameLayout != null && mKLineDeputyIndex == this.mKLineDeputyIndex) {
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
        int[] pm = indexParameter.get(mKLineDeputyIndex.getValue());
        if (pm == null || pm.length != mKLineDeputyIndex.getParameter().length) {
            return mKLineDeputyIndex.getParameter();
        } else {
            return pm;
        }
    }

    /**
     * 获取最新指定指标参数（可能正在计算中或者还未进行计算）
     *
     * @return
     */
    public int[] getNewParameter(KLineDeputyIndex mKLineDeputyIndex) {
        int[] pm = indexParameter.get(mKLineDeputyIndex.getValue());
        if (pm == null || pm.length != mKLineDeputyIndex.getParameter().length) {
            return mKLineDeputyIndex.getParameter();
        } else {
            return pm;
        }
    }

    /**
     * 设置指标
     *
     * @param mKLineDeputyIndex
     */
    public void setKLineDeputyIndex(KLineDeputyIndex mKLineDeputyIndex) {
        setKLineDeputyIndex(mKLineDeputyIndex, true);
    }

    /**
     * 设置指标
     *
     * @param mKLineDeputyIndex
     * @param isNotifyRefresh   是否通知刷新指标
     */
    public void setKLineDeputyIndex(KLineDeputyIndex mKLineDeputyIndex, boolean isNotifyRefresh) {
        if (mKLineDeputyIndex == null || mKLineDeputyIndex == this.mKLineDeputyIndex) {
            return;
        }
        this.mKLineDeputyIndex = mKLineDeputyIndex;
        if (indexParameter.get(mKLineDeputyIndex.getValue()) == null
                || (mKLineDeputyIndex.getParameter() != null &&
                indexParameter.get(mKLineDeputyIndex.getValue()).length !=
                        mKLineDeputyIndex.getParameter().length)
                || (indexParameter.get(mKLineDeputyIndex.getValue()) != null
                && mKLineDeputyIndex.getParameter() == null)) {
            indexParameter.put(mKLineDeputyIndex.getValue(), mKLineDeputyIndex.getParameter());
            tagIndexParameter = indexParameter;
            SaveObject.getInstance().saveObject(paramenterFile, indexParameter);
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
    public KLineDeputyIndex getKLineDeputyIndex() {
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
     * 获取最新设置的指标（可能正在计算中或者还未进行计算）
     *
     * @return
     */
    public KLineDeputyIndex getNewIndex() {
        return mKLineDeputyIndex;
    }

    /**
     * 计算好的指标
     *
     * @param tagKLI
     */
    void setTagKLI(KLineDeputyIndex tagKLI) {
        this.tagKLI = tagKLI;
        tagIndexParameter = indexParameter;
    }

    /**
     * 设置币种汇率
     *
     * @param symbolExrate
     * @return
     */
    public KLineChartDeputyView setSymbolExrate(double symbolExrate) {
        if (this.symbolExrate != symbolExrate && symbolExrate > 0) {
            this.symbolExrate = symbolExrate;
        }
        return this;
    }

    @Override
    public ChartBaseView setDecPlace(int decPlace) {
        super.setDecPlace(decPlace);
        return this;
    }

    /**
     * 清除数据
     */
    public void clear() {
        if (kLineDataList != null) {
            kLineDataList.clear();
        }
        setData(kLineDataList);
    }
}
