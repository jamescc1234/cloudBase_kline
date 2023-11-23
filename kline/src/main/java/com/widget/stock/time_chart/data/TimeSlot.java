package com.widget.stock.time_chart.data;

import java.io.Serializable;

/**
 * Created by dingrui on 16/9/26.
 * 分时时间段
 * 时间格式:HH:mm
 * 必须24小时制时间
 */

public class TimeSlot implements Serializable {

    private static final long serialVersionUID = -1405515153692778101L;

    public TimeSlot(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 获取开始时间
     *
     * @return
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间
     *
     * @param startTime
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间
     *
     * @return
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     *
     * @param endTime
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
