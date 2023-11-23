package com.widget.stock.long_time_chart.adapter;


import com.widget.stock.adapter.ChartAdapter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dingrui on 2016/10/10.
 */

public abstract class LongTimeBaseAdapter extends ChartAdapter {
    @Override
    @Deprecated
    public int getCount() {
        return getGroupCount();
    }

    @Override
    @Deprecated
    public Serializable getData(int position) {
        return (Serializable) getGroupData(position);
    }

    public abstract int getGroupCount();

    public abstract List getGroupData(int groupPosition);

    public abstract int getChildCount(int groupPosition);

    public abstract Serializable getChildData(int groupPosition, int childPosition);
}
