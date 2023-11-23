package com.kedll.stock.library.base;

public abstract class BaseColorsConfigure {

    public abstract int getUpColor();// 涨的颜色

    public abstract int getLowColor();// 跌的颜色

    public abstract int getFlatColor();// 不涨不跌的颜色

    public abstract int getUpBackground();// 涨的颜色

    public abstract int getLowBackground();// 跌的颜色

    public abstract int getFlatBackground();// 不涨不跌的颜色

    /**
     * K线主图配置颜色
     */
    public abstract int[] getMainMAColors();// MA指标颜色(6)，顺序：MA1,MA2,MA3,MA4,MA5,MA6

    public abstract int[] getMainEMAColors();// EMA指标颜色(6),顺序：EMA1,EMA2,EMA3,EMA4,EMA5,EMA6

    public abstract int[] getBOLLColors();// BOLL指标颜色(3)，顺序：MID,UPPER,LOWER

    public abstract int[] getSARColors();// sar颜色(1)

    public abstract int[] getGameColors();// 游戏指标颜色(6)，顺序：买,卖,买背景边线色,卖背景边线色,买背景色,卖背景色

    /**
     * K线副图配置颜色
     */
    public abstract int[] getVOLColors();// VOL指标颜色(5)，顺序：Vol涨的颜色,Vol跌的颜色,MA1,MA2,MA3

    public abstract int[] getMACDColors();// MACD指标颜色(4)，顺序：DIFF,DEA,涨的MACD,跌的MACD

    public abstract int[] getDMIColors();// DMI指标颜色(4)，顺序：PDI,MDI,ADX,ADXR

    public abstract int[] getKDJColors();// KDJ指标颜色(3)，顺序：K,D,J

    public abstract int[] getRSIColors();// RSI指标颜色(3)，顺序：RSI1,RSI2,RSI3

    public abstract int[] getBIASColors();// BIAS指标颜色(3)，顺序：BIAS1,BIAS2,BIAS3

    public abstract int[] getARBRColors();// ARBR指标颜色(2)，顺序：AR,BR

    public abstract int[] getCCIColors();// CCI指标颜色(1)，顺序：CCI

    public abstract int[] getCRColors();// CR指标颜色(4)，顺序：CR,MA1,MA2,MA3

    public abstract int[] getPSYColors();// PSY指标颜色(1)，顺序：PSY

    public abstract int[] getDMAColors();// DMA指标颜色(2)，顺序：DIF,MA

    public abstract int[] getTRIXColors();// TRIX指标颜色(2)，顺序：TRIX,TRMA

    public abstract int[] getVOMColors();// VOM指标颜色(2)，顺序：Vol涨的颜色,Vol跌的颜色

    public abstract int[] getKDJBSColors();// KDJ指标颜色(5)，顺序：K,D,J,买,卖

    public abstract int[] getRSIBSColors();// RSI指标颜色(5)，顺序：RSI1,RSI2,RSI3,买,卖

    public abstract int[] getMACDBSColors();// MACD指标颜色(6)，顺序：DIFF,DEA,涨的MACD,跌的MACD,买,卖
}
