package com.widget.stock.k_line.data;

import java.io.Serializable;

/**
 * Created by dingrui on 2016/10/25.
 * <p>
 * K线基本数据
 */

public class KLineData implements Serializable {

    private static final long serialVersionUID = 8489268064546068952L;
    private String id;// K线ID
    private long time;// 时间戳
    private double hig,// 高
            open,// 开
            low,// 低
            close,// 收
            vol,// 成交量
            cje;// 成交额
    private byte buyAndSell = 0;// 0：无；1：买；-1：卖
    private double max,// 最大坐标
            min;// 最小坐标

    public KLineData() {

    }

    public KLineData(String id, long time, double hig, double open, double low, double close
            , double vol, double cje) {
        this.id = id;
        this.time = time;
        this.hig = hig;
        this.open = open;
        this.low = low;
        this.close = close;
        this.vol = vol;
        this.cje = cje;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getHig() {
        return hig;
    }

    public void setHig(double hig) {
        this.hig = hig;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public double getCje() {
        return cje;
    }

    public void setCje(double cje) {
        this.cje = cje;
    }

    public byte getBuyAndSell() {
        return buyAndSell;
    }

    public void setBuyAndSell(byte buyAndSell) {
        this.buyAndSell = buyAndSell;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }
}
