package com.widget.stock.long_time_chart.data;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.widget.stock.long_time_chart.view.LongTimeChartFrameLayout;
import com.widget.stock.time_chart.data.TimeDataList;

import java.util.Collection;

/**
 * Created by dingrui on 2016/10/12.
 * 多日分时父级集合
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LongTimeDataGroupList<E> extends TimeDataList<E> {

    private final int MAX_SIZE = LongTimeChartFrameLayout.FIVE_DAYS;

    @Override
    public boolean add(E object) {
        if (size() >= MAX_SIZE) {
            return false;
        }
        return super.add(object);
    }

    @Override
    public void add(int index, E object) {
        if (size() >= MAX_SIZE) {
            return;
        }
        super.add(index, object);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (size() >= MAX_SIZE
            || MAX_SIZE - size() < collection.size()) {
            return false;
        }
        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        if (size() >= MAX_SIZE
            || MAX_SIZE - size() < collection.size()) {
            return false;
        }
        return super.addAll(index, collection);
    }
}
