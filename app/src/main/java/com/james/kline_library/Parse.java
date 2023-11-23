package com.james.kline_library;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Parse {

    private static Parse parse;

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
     * String
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
     * @param obj
     * @param decNum
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
     * String
     *
     * @param obj
     * @param rsp
     * @return String
     */
    public String toString(Object obj, String rsp) {
        if (obj == null) {
            if (rsp == null) {
                obj = "";
            } else {
                obj = rsp;
            }
        }
        return obj.toString();
    }

    /**
     * Map<String,Object>
     *
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseMap(Object obj) {
        Map<String, Object> map = null;
        if (obj == null) {
            map = new HashMap<>();
            return map;
        }
        try {
            map = (Map<String, Object>) obj;
        } catch (Exception e) {
            map = new HashMap<>();
        }
        return map;
    }

    /**
     * ArrayList<Map<String, Object>>
     *
     * @param obj
     * @return ArrayList
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Map<String, Object>> parseList(Object obj) {
        ArrayList<Map<String, Object>> list = null;
        if (obj == null) {
            list = new ArrayList<>();
            return list;
        }
        try {
            list = (ArrayList<Map<String, Object>>) obj;
        } catch (Exception e) {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * float
     *
     * @param obj
     * @return float
     */
    public float parseFloat(Object obj) {
        float fl;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
                fl = Float.parseFloat(str);
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
        return fl;
    }

    /**
     * double
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
                if (Double.isInfinite(db) || Double.isNaN(db)) {
                    return ZERO;
                }
                return db;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * double
     *
     * @param obj
     * @param num A few decimal places; Example: "#.##","#.###"……
     * @return double
     */
    public double parseDouble(Object obj, String num) {
        double db;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
//                String numLength = num.replace("#.", "");
//                db = Double.parseDouble(str)
//                     + Math.pow(0.1, numLength.length() + 1);
                db = Double.parseDouble(str);
                if (Double.isInfinite(db) || Double.isNaN(db)) {
                    return ZERO;
                }
                DecimalFormat df = new DecimalFormat(num);
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
                df.setDecimalFormatSymbols(symbols);
                db = Double.parseDouble(df.format(db));
                return db;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * double
     *
     * @param obj
     * @param num A few decimal places
     * @return double
     */
    public double parseDouble(Object obj, int num) {
        double db;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
//                String numLength = num.replace("#.", "");
//                db = Double.parseDouble(str)
//                     + Math.pow(0.1, numLength.length() + 1);
                db = Double.parseDouble(str);
                if (Double.isNaN(db) || Double.isInfinite(db)) {
                    return ZERO;
                }
                StringBuffer sb = new StringBuffer("#.");
                for (int i = 0; i < num; i++) {
                    sb.append("#");
                }
                DecimalFormat df = new DecimalFormat(sb.toString());
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
                df.setDecimalFormatSymbols(symbols);
                db = Double.parseDouble(df.format(db));
                return db;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * double（down）
     *
     * @param obj
     * @param num A few decimal places
     * @return double
     */
    public double parseDoubleDown(Object obj, int num) {
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
                str = String.format(Locale.ENGLISH, "%." + num + 5 + "f", db);
                str = str.substring(ZERO, str.length() - 5);
//                BigDecimal bg = new BigDecimal(db).setScale(num, RoundingMode.DOWN);
//                db = bg.doubleValue();
                return Double.parseDouble(str);
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * byte
     *
     * @param obj
     * @return byte
     */
    public byte parseByte(Object obj) {
        byte b;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
                b = Byte.parseByte(str);
                return b;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * int
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
     * long
     *
     * @param obj
     * @return long
     */
    public long parseLong(Object obj) {
        long lo;
        String str = toString(obj);
        if ("".equals(str)) {
            return ZERO;
        } else {
            try {
                lo = Long.parseLong(str);
                return lo;
            } catch (Exception e) {
                e.printStackTrace();
                return ZERO;
            }
        }
    }

    /**
     * boolean
     *
     * @param obj
     * @return boolean
     */
    public boolean parseBool(Object obj) {
        boolean bool;
        String str = toString(obj);
        if ("".equals(str)) {
            bool = false;
        } else {
            try {
                bool = Boolean.parseBoolean(str);
            } catch (Exception e) {
                e.printStackTrace();
                bool = false;
            }
        }
        return bool;
    }

    /**
     * String and Remove spaces
     *
     * @param obj
     * @return String
     */
    public String toStringReSpaces(Object obj) {
        if (obj == null) {
            obj = "";
        }
        return obj.toString().replaceAll(" ","");
    }

}
