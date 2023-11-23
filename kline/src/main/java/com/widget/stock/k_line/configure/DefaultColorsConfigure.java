package com.widget.stock.k_line.configure;

import com.kedll.stock.library.base.BaseColorsConfigure;

/**
 * Created by dingrui on 2016/10/29.
 */

public final class DefaultColorsConfigure extends BaseColorsConfigure {
    private static final int UP_COLOR = 0xff5eba89;// 涨的颜色
    private static final int LOW_COLOR = 0xffd15140;// 跌的颜色
    private static final int FLAT_COLOR = 0xff868e9b;// 不涨不跌的颜色

    private static final int UP_BACKGROUND = 0xffe4f4ed;// 涨的颜色
    private static final int LOW_BACKGROUND = 0xfff7e4e2;// 跌的颜色
    private static final int FLAT_BACKGROUND = 0xffd1d7e3;// 不涨不跌的颜色

    /**
     * K线主图配置颜色
     */
    private static final int[] MAIN_MA_COLORS = {0xffd15140, 0xffe8ba41, 0xff4f8fdc,
            0xfffdde10, 0xffe67c36,
            0xff01afba};// MA指标颜色，顺序：MA1,MA2,MA3,MA4,MA5,MA6
    private static final int[] MAIN_EMA_COLORS = {MAIN_MA_COLORS[0], MAIN_MA_COLORS[1], MAIN_MA_COLORS[2],
            MAIN_MA_COLORS[3], MAIN_MA_COLORS[4],
            MAIN_MA_COLORS[5]};// EMA指标颜色，顺序：EMA1,EMA2,EMA3,EMA4,EMA5,EMA6
    private static final int[] BOLL_COLORS = {MAIN_MA_COLORS[1], UP_COLOR,
            LOW_COLOR};// BOLL指标颜色，顺序：MID,UPPER,LOWER
    private static final int[] SAR_COLORS = {0xff2871db};// sar颜色
    private static final int[] GAME_COLORS = {0xff30D5FF, 0xffda6262, 0xff2871DB,
            0xffD36060, 0xff142D52,
            0xff382A3A};// 游戏指标颜色，顺序：买,卖,买背景边线色,卖背景边线色,买背景色,卖背景色

    /**
     * K线副图配置颜色
     */
    private static final int[] VOL_COLORS = {UP_COLOR, LOW_COLOR, 0xffd15140, 0xffe8ba41,
            0xff4f8fdc};// VOL指标颜色，顺序：Vol涨的颜色,Vol跌的颜色,MA1,MA2,MA3
    private static final int[] MACD_COLORS = {VOL_COLORS[2], VOL_COLORS[3], UP_COLOR,
            LOW_COLOR};// MACD指标颜色，顺序：DIFF,DEA,涨的MACD,跌的MACD
    private static final int[] DMI_COLORS = {VOL_COLORS[2], VOL_COLORS[3], VOL_COLORS[4],
            0xff7cc059};// DMI指标颜色，顺序：PDI,MDI,ADX,ADXR
    private static final int[] KDJ_COLORS = {DMI_COLORS[0], DMI_COLORS[1],
            DMI_COLORS[2]};// KDJ指标颜色，顺序：K,D,J
    private static final int[] RSI_COLORS = KDJ_COLORS;// RSI指标颜色，顺序：RSI1,RSI2,RSI3
    private static final int[] BIAS_COLORS = RSI_COLORS;// BIAS指标颜色，顺序：BIAS1,BIAS2,BIAS3
    private static final int[] ARBR_COLORS = {BIAS_COLORS[0], BIAS_COLORS[1]};// ARBR指标颜色，顺序：AR,BR
    private static final int[] CCI_COLORS = {ARBR_COLORS[0]};// CCI指标颜色，顺序：CCI
    private static final int[] CR_COLORS = DMI_COLORS;// CR指标颜色，顺序：CR,MA1,MA2,MA3
    private static final int[] PSY_COLORS = CCI_COLORS;// PSY指标颜色，顺序：PSY
    private static final int[] DMA_COLORS = ARBR_COLORS;// DMA指标颜色，顺序：DIF,MA
    private static final int[] TRIX_COLORS = DMA_COLORS;// TRIX指标颜色，顺序：TRIX,TRMA
    private static final int[] VOM_COLORS = {UP_COLOR, LOW_COLOR};// VOM指标颜色，顺序：Vol涨的颜色,Vol跌的颜色

    private static final int[] KDJ_BS_COLORS = {DMI_COLORS[0], DMI_COLORS[1],
            DMI_COLORS[2], UP_COLOR,
            LOW_COLOR};// KDJ指标颜色，顺序：K,D,J,买,卖
    private static final int[] RSI_BS_COLORS = KDJ_BS_COLORS;// RSI指标颜色，顺序：RSI1,RSI2,RSI3,买,卖
    private static final int[] MACD_BS_COLORS = {MACD_COLORS[0], MACD_COLORS[1], UP_COLOR,
            LOW_COLOR, UP_COLOR,
            LOW_COLOR};// MACD指标颜色，顺序：DIFF,DEA,涨的MACD,跌的MACD,买,卖

    @Override
    public int getUpColor() {
        return UP_COLOR;
    }

    @Override
    public int getLowColor() {
        return LOW_COLOR;
    }

    @Override
    public int getFlatColor() {
        return FLAT_COLOR;
    }

    @Override
    public int getUpBackground() {
        return UP_BACKGROUND;
    }

    @Override
    public int getLowBackground() {
        return LOW_BACKGROUND;
    }

    @Override
    public int getFlatBackground() {
        return FLAT_BACKGROUND;
    }

    @Override
    public int[] getMainMAColors() {
        return MAIN_MA_COLORS;
    }

    @Override
    public int[] getMainEMAColors() {
        return MAIN_EMA_COLORS;
    }

    @Override
    public int[] getBOLLColors() {
        return BOLL_COLORS;
    }

    @Override
    public int[] getSARColors() {
        return SAR_COLORS;
    }

    @Override
    public int[] getGameColors() {
        return GAME_COLORS;
    }

    @Override
    public int[] getVOLColors() {
        return VOL_COLORS;
    }

    @Override
    public int[] getMACDColors() {
        return MACD_COLORS;
    }

    @Override
    public int[] getDMIColors() {
        return DMI_COLORS;
    }

    @Override
    public int[] getKDJColors() {
        return KDJ_COLORS;
    }

    @Override
    public int[] getRSIColors() {
        return RSI_COLORS;
    }

    @Override
    public int[] getBIASColors() {
        return BIAS_COLORS;
    }

    @Override
    public int[] getARBRColors() {
        return ARBR_COLORS;
    }

    @Override
    public int[] getCCIColors() {
        return CCI_COLORS;
    }

    @Override
    public int[] getCRColors() {
        return CR_COLORS;
    }

    @Override
    public int[] getPSYColors() {
        return PSY_COLORS;
    }

    @Override
    public int[] getDMAColors() {
        return DMA_COLORS;
    }

    @Override
    public int[] getTRIXColors() {
        return TRIX_COLORS;
    }

    @Override
    public int[] getVOMColors() {
        return VOM_COLORS;
    }

    @Override
    public int[] getKDJBSColors() {
        return KDJ_BS_COLORS;
    }

    @Override
    public int[] getRSIBSColors() {
        return RSI_BS_COLORS;
    }

    @Override
    public int[] getMACDBSColors() {
        return MACD_BS_COLORS;
    }
}
