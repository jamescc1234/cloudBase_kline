package com.james.kline_library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by dingrui on 2017/6/6.
 */
public final class HandlerUtils {

    private static HandlerUtils instance;

    private HandlerUtils() {

    }

    public static synchronized HandlerUtils getInstance() {
        if (instance == null) {
            instance = new HandlerUtils();
        }
        return instance;
    }

    public final void removeMessages(Handler handler, int what) {
        if (handler != null) {
            handler.removeMessages(what);
        }
    }

    public final void removeCallbacks(Handler handler, Runnable runnable) {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public void sendMessage(Handler handler, int what, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    public void sendMessage(Handler handler, int what, int arg1, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = arg1;
            msg.obj = obj;
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    public void sendMessage(Handler handler, int what, Object obj, Bundle data) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            msg.setData(data);
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    public void sendMessageDelayed(Handler handler, int what, Object obj, long delayMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            if (handler != null) {
                handler.sendMessageDelayed(msg, delayMillis);
            }
        }
    }

    public void sendMessageDelayed(Handler handler, int what, int arg1, Object obj, long delayMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = arg1;
            msg.obj = obj;
            if (handler != null) {
                handler.sendMessageDelayed(msg, delayMillis);
            }
        }
    }

    public void sendMessageAtTime(Handler handler, int what, Object obj, long uptimeMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            if (handler != null) {
                handler.sendMessageAtTime(msg, uptimeMillis);
            }
        }
    }

    public void sendMessage(Handler handler, int what, Bundle data) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.setData(data);
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    public void sendMessageDelayed(Handler handler, int what, Bundle data, long delayMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.setData(data);
            if (handler != null) {
                handler.sendMessageDelayed(msg, delayMillis);
            }
        }
    }

    public void sendMessageAtTime(Handler handler, int what, Bundle data, long uptimeMillis) {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.setData(data);
            if (handler != null) {
                handler.sendMessageAtTime(msg, uptimeMillis);
            }
        }
    }

    public void sendEmptyMessage(Handler handler, int what) {
        if (handler != null) {
            handler.sendEmptyMessage(what);
        }
    }

    public void sendEmptyMessageDelayed(Handler handler, int what, long delayMillis) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    public void sendEmptyMessageAtTime(Handler handler, int what, long uptimeMillis) {
        if (handler != null) {
            handler.sendEmptyMessageAtTime(what, uptimeMillis);
        }
    }

    public void postDelayed(Handler handler, Runnable runnable, long delayMillis) {
        if (runnable != null && handler != null) {
            handler.postDelayed(runnable, delayMillis);
        }
    }

    public void removeCallbacksAndMessagesEmpty(Handler handler, Object token) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(token);
            handler = null;
        }
    }

    public void removeCallbacksAndMessages(Handler handler, Object token) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(token);
        }
    }
}
