package com.widget.stock.time_chart.adapter;


import com.widget.stock.adapter.ChartAdapter;
import com.widget.stock.time_chart.data.TimeData;
import com.widget.stock.time_chart.data.TimeDataList;

/**
 * Created by dingrui on 16/9/27.
 * 分时适配器
 */

public class TimeChartAdapter extends ChartAdapter {

    private TimeDataList<TimeData> timeDataList;

    public TimeChartAdapter(TimeDataList<TimeData> timeDataList) {
        this.timeDataList = timeDataList;
    }

    @Override
    public int getCount() {
        return timeDataList == null ? 0 : timeDataList.size();
    }

    @Override
    public TimeData getData(int position) {
        if (getCount() > position) {
            return timeDataList.get(position);
        } else {
            return null;
        }
    }

    /**
     * 设置数据并更新适配器
     *
     * @param timeDataList
     */
    public void setTimeDataList(TimeDataList<TimeData> timeDataList) {
        this.timeDataList = timeDataList;
        notifyChangeData();
    }

    /**
     * 添加数据集合并更新适配器
     *
     * @param timeDataList
     */
    public void addTimeDataList(TimeDataList<TimeData> timeDataList) {
        if (this.timeDataList == null) {
            this.timeDataList = new TimeDataList<>();
        }
        this.timeDataList.addAll(timeDataList);
        notifyChangeData();
    }

    /**
     * 添加数据并更新适配器
     *
     * @param timeData
     */
    public void addTimeData(TimeData timeData) {
        if (this.timeDataList == null)
            this.timeDataList = new TimeDataList<>();
        this.timeDataList.add(timeData);
        notifyChangeData();
    }

    /**
     * 获取所有数据
     *
     * @return
     */
    public TimeDataList getTimeDataList() {
        return timeDataList;
    }
}
