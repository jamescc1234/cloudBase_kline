package com.widget.stock.adapter;


import com.widget.stock.OnChartDataObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingrui on 16/9/27.
 */

public abstract class ChartAdapter {
    private List<OnChartDataObserver> observerList = new ArrayList<>();

    /**
     * 注册观察者
     *
     * @param mOnChartDataObserver
     */
    public void registerObserver(OnChartDataObserver mOnChartDataObserver) {
        if (mOnChartDataObserver == null)
            return;
        if (!observerList.contains(mOnChartDataObserver)) {
            observerList.add(mOnChartDataObserver);
        }
    }

    /**
     * 取消观察者
     *
     * @param mOnChartDataObserver
     */
    public void unRegisterObserver(OnChartDataObserver mOnChartDataObserver) {
        if (mOnChartDataObserver == null)
            return;
        if (observerList.contains(mOnChartDataObserver)) {
            observerList.remove(mOnChartDataObserver);
        }
    }

    /**
     * 通知到观察者
     */
    public void notifyChangeData() {
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).onChartData();
        }
    }

    public abstract int getCount();

    public abstract Serializable getData(int position);
}
