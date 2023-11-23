package com.kedll.stock.library.base;

public abstract class BaseThread implements Runnable {

    private boolean isAlive;

    @Override
    public final void run() {
        isAlive = true;
        running();
        isAlive = false;
    }

    public boolean isAlive() {
        return isAlive;
    }

    protected abstract void running();

    public static boolean isAlive(BaseThread thread) {
        if (thread == null) {
            return false;
        } else {
            if (thread.isAlive()) {
                return true;
            } else {
                return false;
            }
        }
    }

}
