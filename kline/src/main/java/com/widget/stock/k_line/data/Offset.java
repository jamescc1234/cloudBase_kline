package com.widget.stock.k_line.data;

/**
 * @author James Chen
 * @date 24/3/2023
 */
public class Offset {
    private final long timeStamp;
    private final float price;

    public Offset(long timeStamp, float price) {
        this.timeStamp = timeStamp;
        this.price = price;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public float getPrice() {
        return price;
    }

}
