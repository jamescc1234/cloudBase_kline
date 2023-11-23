package com.widget.stock.k_line.adapter;


import com.widget.stock.adapter.ChartAdapter;
import com.widget.stock.k_line.data.KLineData;

import java.util.ArrayList;

/**
 * Created by dingrui on 2016/10/25.
 * K线适配器
 */

public class KLineChartAdapter extends ChartAdapter {

    private ArrayList<KLineData> list;
    private int location = -1;

    private boolean isDataSet = false;

    public KLineChartAdapter() {
    }

    public KLineChartAdapter(ArrayList<KLineData> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public KLineData getData(int position) {
        try {
            if (list != null && list.size() > position) {
                return list.get(position);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getLocation() {
        return location;
    }

    /**
     * 由于设置适配器后会经过子线程进行计算，所以在KLineChartFrmeLayout控件里面直接设置position不能响应新的数据长度，
     * 所以建议在this中设置，子线程执行完毕后会从this中获取location来设置K线位置
     *
     * @return
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * 获取数据
     *
     * @return
     */
    public ArrayList<KLineData> getDataList() {
        return this.list;
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public synchronized void setData(ArrayList<KLineData> list) {
        this.list = list;
        notifyChangeData();
        isDataSet = true;
    }

    /**
     * 清除数据
     */
    public void clear() {
        if (list != null) {
            list.clear();
            notifyChangeData();
        }
    }

    /**
     * 置空
     */
    public void empty() {
        setData(null);
    }

    /**
     * 添加数据
     *
     * @param list
     */
    public void addData(ArrayList<KLineData> list) {
        if (!isDataSet) {
            return;
        }
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        this.list.addAll(list);
        notifyChangeData();
    }

    /**
     * 添加一条数据
     *
     * @param item
     */
    public void add(KLineData item) {
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        this.list.add(item);
        notifyChangeData();
    }

    /**
     * 更新最后一条数据
     *
     * @param item
     */
    public void updateLast(KLineData item) {
        if (!isDataSet) {
            return;
        }

        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        if (this.list.size() > 0) {
            if (this.list.get(this.list.size() - 1).getTime() == item.getTime()) {
                this.list.set(this.list.size() - 1, item);
            } else {
                this.list.add(item);
            }
        } else {
            this.list.add(item);
        }
        notifyChangeData();
    }
}
