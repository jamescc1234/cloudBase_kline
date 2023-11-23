package com.widget.stock;

/**
 * Created by dingrui on 2016/10/10.
 * 时间位置
 */

public enum TimeLocation {
    CENTER(0x0),// 主图与第一个副图之间
    BOTOOM(0x1),// 最底部
    NONE(0x2);// 不绘制时间

    private int value;

    TimeLocation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
