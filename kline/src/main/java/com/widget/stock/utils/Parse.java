package com.widget.stock.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Parse {

    private static Parse parse;

    /**
     * 私有构造函数，保证单例模式
     */
    private Parse() {

    }

    public synchronized static Parse getInstance() {
        if (parse == null) {
            parse = new Parse();
        }
        return parse;
    }

    private final int ZERO = 0;

    /**
     * 判断Map中的值是否为空
     *
     * @param obj
     * @return String
     */
    public String toString(Object obj) {
        if (obj == null) {
            obj = "";
        }
        return obj.toString();
    }

    /**
     * 格式化成3位画逗号
     *
     * @param obj
     * @param decNum 保留小数位数
     * @return
     */
    public String toString(Object obj, int decNum) {
        double d = parseDouble(obj);
        StringBuffer sb = new StringBuffer();
        if (decNum > ZERO) {
            sb.append(".");
            for (int i = 0; i < decNum; i++) {
                sb.append("0");
            }
        }
        DecimalFormat df = new DecimalFormat("#,###" + sb.toString());
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        df.setDecimalFormatSymbols(symbols);
        String str = df.format(d);
        if (str.contains(".")) {
            String[] strs = str.split("\\.");
            if (strs.length > 0) {
                if ("".equals(strs[0])) {
                    str = "0" + str;
                } else if ("-".equals(strs[0])) {
                    str = str.replace("-", "-0");
                }
            }
        }
        return str;
    }

    /**
     * 转成int类型
     *
     * @param obj
     * @return int
     */
    public int parseInt(Object obj) {
        int in;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
                in = Integer.parseInt(str);
                return in;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * 转成double类型
     *
     * @param obj
     * @return double
     */
    public double parseDouble(Object obj) {
        double db;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
                db = Double.parseDouble(str);
                if (Double.isNaN(db) || Double.isInfinite(db)) {
                    return ZERO;
                }
                return db;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }
}
