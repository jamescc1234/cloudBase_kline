package com.widget.stock.k_line.data;

import java.io.Serializable;

/**
 * Created by dingrui on 2016/10/25.
 * 经过指标计算的数据对象
 */

public class KLineDataValid extends KLineData {

    private static final long serialVersionUID = 3423692164922078905L;
    private MainMA mMainMA;
    private MainEMA mMainEMA;
    private MainBOLL mMainBOLL;
    private VolMA mVolMA;
    private MACD mMACD;
    private KDJ mKDJ;
    private RSI mRSI;
    private BIAS mBIAS;
    private BRAR mBRAR;
    private CCI mCCI;
    private DMI mDMI;
    private CR mCR;
    private PSY mPSY;
    private DMA mDMA;
    private TRIX mTRIX;
    private ReturnRate mReturnRate;
    private SAR mSAR;

    private KDJ_BS mKDJ_BS;
    private RSI_BS mRSI_BS;
    private MACD_BS mMACD_BS;

    public KLineDataValid() {

    }

    public KLineDataValid(String id, long time, double hig, double open, double low, double close, double vol, double cje) {
        super(id, time, hig, open, low, close, vol, cje);
    }

    public MainMA getMainMA() {
        return mMainMA;
    }

    public void setMainMA(MainMA mMainMA) {
        this.mMainMA = mMainMA;
    }

    public MainEMA getMainEMA() {
        return mMainEMA;
    }

    public void setMainEMA(MainEMA mMainEMA) {
        this.mMainEMA = mMainEMA;
    }

    public MainBOLL getMainBOLL() {
        return mMainBOLL;
    }

    public void setMainBOLL(MainBOLL mMainBOLL) {
        this.mMainBOLL = mMainBOLL;
    }

    public VolMA getVolMA() {
        return mVolMA;
    }

    public void setVolMA(VolMA mVolMA) {
        this.mVolMA = mVolMA;
    }

    public MACD getMACD() {
        return mMACD;
    }

    public void setMACD(MACD mMACD) {
        this.mMACD = mMACD;
    }

    public KDJ getKDJ() {
        return mKDJ;
    }

    public void setKDJ(KDJ mKDJ) {
        this.mKDJ = mKDJ;
    }

    public RSI getRSI() {
        return mRSI;
    }

    public void setRSI(RSI mRSI) {
        this.mRSI = mRSI;
    }

    public BIAS getBIAS() {
        return mBIAS;
    }

    public void setBIAS(BIAS mBIAS) {
        this.mBIAS = mBIAS;
    }

    public BRAR getBRAR() {
        return mBRAR;
    }

    public void setBRAR(BRAR mBRAR) {
        this.mBRAR = mBRAR;
    }

    public CCI getCCI() {
        return mCCI;
    }

    public void setCCI(CCI mCCI) {
        this.mCCI = mCCI;
    }

    public DMI getDMI() {
        return mDMI;
    }

    public void setDMI(DMI mDMI) {
        this.mDMI = mDMI;
    }

    public CR getCR() {
        return mCR;
    }

    public void setCR(CR mCR) {
        this.mCR = mCR;
    }

    public PSY getPSY() {
        return mPSY;
    }

    public void setPSY(PSY mPSY) {
        this.mPSY = mPSY;
    }

    public DMA getDMA() {
        return mDMA;
    }

    public void setDMA(DMA mDMA) {
        this.mDMA = mDMA;
    }

    public TRIX getTRIX() {
        return mTRIX;
    }

    public void setTRIX(TRIX mTRIX) {
        this.mTRIX = mTRIX;
    }

    public ReturnRate getReturnRate() {
        return mReturnRate;
    }

    public void setReturnRate(ReturnRate mReturnRate) {
        this.mReturnRate = mReturnRate;
    }

    public SAR getSar() {
        return mSAR;
    }

    public void setSar(SAR mSAR) {
        this.mSAR = mSAR;
    }

    public KDJ_BS getKDJ_BS() {
        return mKDJ_BS;
    }

    public void setKDJ_BS(KDJ_BS mKDJ_BS) {
        this.mKDJ_BS = mKDJ_BS;
    }

    public RSI_BS getRSI_BS() {
        return mRSI_BS;
    }

    public void setRSI_BS(RSI_BS mRSI_BS) {
        this.mRSI_BS = mRSI_BS;
    }

    public MACD_BS getMACD_BS() {
        return mMACD_BS;
    }

    public void setMACD_BS(MACD_BS mMACD_BS) {
        this.mMACD_BS = mMACD_BS;
    }

    /**
     * 主图指标
     */
    /**
     * 主图MA指标
     */
    public static class MainMA implements Serializable {
        private static final long serialVersionUID = -2310096005389511554L;
        private double mA1, mA2, mA3, mA4, mA5, mA6;

        public MainMA(double mA1, double mA2, double mA3, double mA4, double mA5, double mA6) {
            this.mA1 = mA1;
            this.mA2 = mA2;
            this.mA3 = mA3;
            this.mA4 = mA4;
            this.mA5 = mA5;
            this.mA6 = mA6;
        }

        public double getmA1() {
            return mA1;
        }

        public double getmA2() {
            return mA2;
        }

        public double getmA3() {
            return mA3;
        }

        public double getmA4() {
            return mA4;
        }

        public double getmA5() {
            return mA5;
        }

        public double getmA6() {
            return mA6;
        }
    }

    /**
     * 主图EMA指标
     */
    public static class MainEMA implements Serializable {
        private static final long serialVersionUID = -7521078555926572123L;
        private double eMA1, eMA2, eMA3, eMA4, eMA5, eMA6;

        public MainEMA(double eMA1, double eMA2, double eMA3, double eMA4, double eMA5, double eMA6) {
            this.eMA1 = eMA1;
            this.eMA2 = eMA2;
            this.eMA3 = eMA3;
            this.eMA4 = eMA4;
            this.eMA5 = eMA5;
            this.eMA6 = eMA6;
        }

        public double geteMA1() {
            return eMA1;
        }

        public double geteMA2() {
            return eMA2;
        }

        public double geteMA3() {
            return eMA3;
        }

        public double geteMA4() {
            return eMA4;
        }

        public double geteMA5() {
            return eMA5;
        }

        public double geteMA6() {
            return eMA6;
        }
    }

    /**
     * 主图布林指标
     */
    public static class MainBOLL implements Serializable {
        private static final long serialVersionUID = 5303314719221290641L;
        private double upper,// 上轨线
                mID,// 中轨线
                lower;// 下轨线

        public MainBOLL(double upper, double mID, double lower) {
            this.upper = upper;
            this.mID = mID;
            this.lower = lower;
        }

        public double getUpper() {
            return upper;
        }

        public double getmID() {
            return mID;
        }

        public double getLower() {
            return lower;
        }
    }

    /**
     * 副图指标
     */
    /**
     * 成交量MA指标
     */
    public static class VolMA implements Serializable {
        private static final long serialVersionUID = 1723526674128421302L;
        private double mA1, mA2, mA3;

        public VolMA(double mA1, double mA2, double mA3) {
            this.mA1 = mA1;
            this.mA2 = mA2;
            this.mA3 = mA3;
        }

        public double getmA1() {
            return mA1;
        }

        public double getmA2() {
            return mA2;
        }

        public double getmA3() {
            return mA3;
        }
    }

    /**
     * 指数平滑异同平均线（MACD指标）
     */
    public static class MACD implements Serializable {
        private static final long serialVersionUID = -3007996793784072715L;
        private double diff,
                dea,
                macd;

        public MACD(double diff, double dea, double macd) {
            this.diff = diff;
            this.dea = dea;
            this.macd = macd;
        }

        public double getDiff() {
            return diff;
        }

        public double getDea() {
            return dea;
        }

        public double getMacd() {
            return macd;
        }
    }

    /**
     * 指数平滑异同平均线（MACD买卖指标）
     */
    public static class MACD_BS extends MACD {

        private static final long serialVersionUID = 1407338499026859633L;
        private boolean isDrawBuy,// 是否绘制买点
                isDrawSell;// 是否绘制卖点

        public MACD_BS(double diff, double dea, double macd, boolean isDrawBuy, boolean isDrawSell) {
            super(diff, dea, macd);
            this.isDrawBuy = isDrawBuy;
            this.isDrawSell = isDrawSell;
        }

        public boolean isDrawBuy() {
            return isDrawBuy;
        }

        public boolean isDrawSell() {
            return isDrawSell;
        }
    }

    /**
     * 随机指标(KDJ)
     */
    public static class KDJ implements Serializable {
        private static final long serialVersionUID = 2066731389524143499L;
        private double K,
                D,
                J;

        public KDJ(double K, double D, double J) {
            this.K = K;
            this.D = D;
            this.J = J;
        }

        public double getK() {
            return K;
        }

        public double getD() {
            return D;
        }

        public double getJ() {
            return J;
        }
    }

    /**
     * KDJ买卖
     */
    public static class KDJ_BS extends KDJ {

        private static final long serialVersionUID = -8920765758852191546L;
        private boolean isDrawBuy = false;
        private boolean isDrawSell = false;

        public KDJ_BS(double K, double D, double J, boolean isDrawBuy, boolean isDrawSell) {
            super(K, D, J);
            this.isDrawBuy = isDrawBuy;
            this.isDrawSell = isDrawSell;
        }

        public boolean isDrawBuy() {
            return isDrawBuy;
        }

        public boolean isDrawSell() {
            return isDrawSell;
        }
    }

    /**
     * 强弱指标
     */
    public static class RSI implements Serializable {
        private static final long serialVersionUID = 1189234064169314661L;
        private double rsi1,
                rsi2,
                rsi3;

        public RSI(double rsi1, double rsi2, double rsi3) {
            this.rsi1 = rsi1;
            this.rsi2 = rsi2;
            this.rsi3 = rsi3;
        }

        public double getRsi1() {
            return rsi1;
        }

        public double getRsi2() {
            return rsi2;
        }

        public double getRsi3() {
            return rsi3;
        }
    }

    /**
     * RSI买卖
     */
    public static class RSI_BS extends RSI {
        private static final long serialVersionUID = -5441002665735313153L;
        private boolean isDrawBuy = false;
        private boolean isDrawSell = false;

        public RSI_BS(double rsi1, double rsi2, double rsi3, boolean isDrawBuy, boolean isDrawSell) {
            super(rsi1, rsi2, rsi3);
            this.isDrawBuy = isDrawBuy;
            this.isDrawSell = isDrawSell;
        }

        public boolean isDrawBuy() {
            return isDrawBuy;
        }

        public boolean isDrawSell() {
            return isDrawSell;
        }
    }

    /**
     * 乖离率（BIAS）是测量股价偏离均线大小程度的指标
     */
    public static class BIAS implements Serializable {
        private static final long serialVersionUID = -4313310310126929826L;
        private double bias1,// 5
                bias2,// 10
                bias3;// 20

        public BIAS(double bias1, double bias2, double bias3) {
            this.bias1 = bias1;
            this.bias2 = bias2;
            this.bias3 = bias3;
        }

        public double getBias1() {
            return bias1;
        }

        public double getBias2() {
            return bias2;
        }

        public double getBias3() {
            return bias3;
        }
    }

    /**
     * 情绪指标（BRAR）也称为人气意愿指标
     */
    public static class BRAR implements Serializable {
        private static final long serialVersionUID = -228924196157734299L;
        private double br,
                ar;

        public BRAR(double br, double ar) {
            this.br = br;
            this.ar = ar;
        }

        public double getBr() {
            return br;
        }

        public double getAr() {
            return ar;
        }
    }

    /**
     * 顺势指标
     */
    public static class CCI implements Serializable {
        private static final long serialVersionUID = 620521784283169203L;
        private double cci;

        public CCI(double cci) {
            this.cci = cci;
        }

        public double getCci() {
            return cci;
        }
    }

    /**
     * 动向指标
     */
    public static class DMI implements Serializable {
        private static final long serialVersionUID = 2573541207547958605L;
        private double pdi,
                mdi,
                adx,
                adxr;

        public DMI(double pdi, double mdi, double adx, double adxr) {
            this.pdi = pdi;
            this.mdi = mdi;
            this.adx = adx;
            this.adxr = adxr;
        }

        public double getPdi() {
            return pdi;
        }

        public double getMdi() {
            return mdi;
        }

        public double getAdx() {
            return adx;
        }

        public double getAdxr() {
            return adxr;
        }
    }

    /**
     * 能量指标
     */
    public static class CR implements Serializable {
        private static final long serialVersionUID = 7195979772289672968L;
        private double cr,
                ma1,
                ma2,
                ma3;

        public CR(double cr, double ma1, double ma2, double ma3) {
            this.cr = cr;
            this.ma1 = ma1;
            this.ma2 = ma2;
            this.ma3 = ma3;
        }

        public double getCr() {
            return cr;
        }

        public double getMa1() {
            return ma1;
        }

        public double getMa2() {
            return ma2;
        }

        public double getMa3() {
            return ma3;
        }
    }

    /**
     * 心理线（PSY）指标是研究投资者对股市涨跌产生心理波动的情绪指标
     */
    public static class PSY implements Serializable {
        private static final long serialVersionUID = -7641164565465620421L;
        private double psy;

        public PSY(double psy) {
            this.psy = psy;
        }

        public double getPsy() {
            return psy;
        }
    }

    /**
     * 平行线差指标
     */
    public static class DMA implements Serializable {
        private static final long serialVersionUID = 4883229662180676026L;
        private double dif,
                ama;

        public DMA(double dif, double ama) {
            this.dif = dif;
            this.ama = ama;
        }

        public double getDif() {
            return dif;
        }

        public double getAma() {
            return ama;
        }
    }

    /**
     * 三重指数平滑平均线（TRIX）属于长线指标
     */
    public static class TRIX implements Serializable {
        private static final long serialVersionUID = -2488641973882040328L;
        private double trix,
                maTrix;

        public TRIX(double trix, double maTrix) {
            this.trix = trix;
            this.maTrix = maTrix;
        }

        public double getTrix() {
            return trix;
        }

        public double getMaTrix() {
            return maTrix;
        }
    }

    /**
     * 收益率
     */
    public static class ReturnRate implements Serializable {
        private static final long serialVersionUID = 6563743764527388727L;
        private double rate;

        public ReturnRate(double rate) {
            this.rate = rate;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }
    }

    /**
     * SAR指标
     */
    public static class SAR implements Serializable {
        private static final long serialVersionUID = -764142146316961606L;
        private double sar;
        private boolean isUp;// 是否在上面

        public SAR(double sar, boolean isUp) {
            this.sar = sar;
            this.isUp = isUp;
        }

        public double getSar() {
            return sar;
        }

        public boolean isUp() {
            return isUp;
        }
    }
}
