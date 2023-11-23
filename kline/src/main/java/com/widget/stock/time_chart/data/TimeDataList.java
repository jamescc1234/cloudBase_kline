package com.widget.stock.time_chart.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingrui on 16/9/27.
 * 分时数据列表
 */

public class TimeDataList<E> extends ArrayList<E> {
    private static final long serialVersionUID = -1027279437326886982L;
    private double preStatement;// 昨结
    private double preClose;// 昨收
    private double open;// 开盘价
    private double hig = -1.0f;// 最高
    private double low = -1.0f;// 最低
    private boolean isDaPan = false;// 是否大盘数据

    private Map<String, HigLow> indexHigLow = new HashMap<>();

    public void setPreStatement(double preStatement) {
        this.preStatement = preStatement;
    }

    public double getPreStatement() {
        return preStatement;
    }

    public void setPreClose(double preClose) {
        this.preClose = preClose;
    }

    public double getPreClose() {
        return preClose;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getOpen() {
        return open;
    }

    /**
     * 获取基准价,首先取昨结,没有昨结取昨收,没有昨收取开盘价,肯定有一个有值
     *
     * @return 返回基准价
     */
    public double getPre() {
        if (preStatement > 0) {
            return preStatement;
        } else if (preClose > 0) {
            return preClose;
        } else {
            return open;
        }
    }

    public double getHig() {
        return hig;
    }

    public void setHig(double hig) {
        this.hig = hig;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public boolean isDaPan() {
        return isDaPan;
    }

    public void setDaPan(boolean daPan) {
        isDaPan = daPan;
    }

    /**
     * 设置分时指标最高、最低
     *
     * @param key
     * @param hig
     * @param low
     */
    public void setIndexHigLow(String key, double hig, double low) {
        indexHigLow.put(key, new HigLow(hig, low));
    }

    /**
     * 获取分时指标最高
     *
     * @param key 以指标value作为key值传入
     * @return 返回当前指标最大值
     */
    public double getIndexHig(String key) {
        HigLow mHigLow = indexHigLow.get(key);
        if (mHigLow == null) {
            return 0;
        } else {
            return mHigLow.getHig();
        }
    }

    /**
     * 获取分时指标最低
     *
     * @param key 以指标value作为key值传入
     * @return 返回当前指标最小值
     */
    public double getIndexLow(String key) {
        HigLow mHigLow = indexHigLow.get(key);
        if (mHigLow == null) {
            return 0;
        } else {
            return mHigLow.getLow();
        }
    }

    /**
     * 指标相关
     */
    public class HigLow {
        private double hig, low;// 最大最小值

        public HigLow(double hig, double low) {
            this.hig = hig;
            this.low = low;
        }

        public double getHig() {
            return hig;
        }

        public double getLow() {
            return low;
        }
    }

}
