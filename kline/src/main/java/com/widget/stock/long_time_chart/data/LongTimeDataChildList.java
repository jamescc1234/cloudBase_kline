package com.widget.stock.long_time_chart.data;

import java.util.ArrayList;

/**
 * Created by dingrui on 2016/10/12.
 */

public class LongTimeDataChildList<E> extends ArrayList<E> {

    private long timeStamp;// 对应的集合所属日期时间戳(毫秒级别)

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
