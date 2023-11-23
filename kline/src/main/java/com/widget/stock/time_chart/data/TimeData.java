package com.widget.stock.time_chart.data;

import java.io.Serializable;

/**
 * Created by dingrui on 16/9/27.
 * 分时数据
 */

public class TimeData implements Serializable {

    private static final long serialVersionUID = -2792931679320402756L;
    private long timeStamp;// 时间戳(毫秒级别)
    private double close;// 收
    private double vol;// 成交量（递增形式）
    private double cje;// 成交额（递增形式）

    public TimeData(long timeStamp, double close, double vol, double cje) {
        this.timeStamp = timeStamp;
        this.close = close;
        this.vol = vol;
        this.cje = cje;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getClose() {
        return close;
    }

    public double getVol() {
        return vol;
    }

    public double getCje() {
        return cje;
    }

}
