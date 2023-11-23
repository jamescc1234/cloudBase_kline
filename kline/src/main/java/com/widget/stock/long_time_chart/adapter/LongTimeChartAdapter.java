package com.widget.stock.long_time_chart.adapter;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.widget.stock.long_time_chart.data.LongTimeDataChildList;
import com.widget.stock.long_time_chart.data.LongTimeDataGroupList;
import com.widget.stock.time_chart.data.TimeData;

/**
 * Created by dingrui on 2016/10/10.
 * 多日分时适配器
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LongTimeChartAdapter extends LongTimeBaseAdapter {

    private LongTimeDataGroupList<LongTimeDataChildList<TimeData>> longTimeDataList;

    public LongTimeChartAdapter(LongTimeDataGroupList<LongTimeDataChildList<TimeData>> longTimeDataList) {
        this.longTimeDataList = longTimeDataList;
    }

    @Override
    public int getGroupCount() {
        return longTimeDataList == null ? 0 : longTimeDataList.size();
    }

    @Override
    public LongTimeDataChildList<TimeData> getGroupData(int groupPosition) {
        if (getGroupCount() > groupPosition) {
            return longTimeDataList.get(groupPosition);
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount(int groupPosition) {
        LongTimeDataChildList<TimeData> list = getGroupData(groupPosition);
        if (list == null) {
            return 0;
        } else {
            return longTimeDataList.get(groupPosition).size();
        }
    }

    @Override
    public TimeData getChildData(int groupPosition, int childPosition) {
        if (getChildCount(groupPosition) > childPosition) {
            return longTimeDataList.get(groupPosition).get(childPosition);
        } else {
            return null;
        }
    }

    /**
     * 设置数据
     *
     * @param longTimeDataList
     */
    public void setLongTimeDataList(LongTimeDataGroupList<LongTimeDataChildList<TimeData>> longTimeDataList) {
        this.longTimeDataList = longTimeDataList;
        notifyChangeData();
    }

    /**
     * 添加数据
     *
     * @param longTimeDataList
     */
    public void addLongTimeDataList(LongTimeDataGroupList<LongTimeDataChildList<TimeData>> longTimeDataList) {
        if (longTimeDataList == null) {
            return;
        }
        if (this.longTimeDataList == null) {
            this.longTimeDataList = new LongTimeDataGroupList<>();
        }
        this.longTimeDataList.addAll(longTimeDataList);
        notifyChangeData();
    }

    /**
     * 获取多日分时数据
     *
     * @return 返回多日分时数据
     */
    public LongTimeDataGroupList getLongTimeDataList() {
        return this.longTimeDataList;
    }
}
