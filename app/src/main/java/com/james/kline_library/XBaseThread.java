package com.james.kline_library;

public abstract class XBaseThread extends BaseThread {
    private boolean isCancel = false;

    public void cancel() {
        isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }
}
