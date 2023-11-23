package com.widget.stock;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.contants.KlineConfig;
import com.contants.Language;
import com.kedll.stock.library.base.BaseColorsConfigure;
import com.utils.KlineLanguageUtils;
import com.widget.stock.k_line.configure.DefaultColorsConfigure;
import com.widget.stock.k_line.data.KLineDataValid;
import com.widget.stock.utils.Parse;
import java.util.Locale;


/**
 * Created by dingrui on 16/9/27.
 * 股票工具类
 */

public final class StockUtils {

    private static StockUtils instance;
    public static final String GG = "--";
    public static final String NULL = "null";

    private BaseColorsConfigure colorsConfigure = new DefaultColorsConfigure();

    private StockUtils() {

    }

    public static synchronized StockUtils getInstance() {
        if (instance == null) {
            instance = new StockUtils();
        }
        return instance;
    }

    /**
     * 设置颜色配置
     *
     * @param colorsConfigure
     */
    public void setColorsConfigure(BaseColorsConfigure colorsConfigure) {
        this.colorsConfigure = colorsConfigure;
    }

    public BaseColorsConfigure getColorsConfigure() {
        return this.colorsConfigure;
    }

    /**
     * 计算涨跌幅
     *
     * @param preClose
     * @param close
     * @return
     */
    public double getZDF(Object preClose, Object close) {
        if (preClose == null) {
            return 0;
        }
        double pC = Parse.getInstance().parseDouble(preClose);
        double c = Parse.getInstance().parseDouble(close);
        double zdf = (c - pC) / pC * 100.0f;
        if (Double.isInfinite(zdf) || Double.isNaN(zdf)) {
            zdf = 0;
        }
        return zdf;
    }

    /**
     * 计算涨跌幅并设置颜色，格式:涨幅
     *
     * @param obj 涨跌幅
     * @param tv  TextView
     */
    public String formatTextZDFOfZDF(Object obj, @Nullable TextView tv) {
        return formatTextZDFOfZDF(obj, tv, true);
    }

    /**
     * 计算涨跌幅并设置颜色，格式:涨幅
     *
     * @param obj 涨跌幅
     * @param tv  TextView
     */
    public String formatTextZDFOfZDF(Object obj, @Nullable TextView tv, boolean isShowPlus) {
        int color = 0;
        String str;
        String strObj = Parse.getInstance().toString(obj);
        String plus = "";
        if (TextUtils.isEmpty(strObj)
                || GG.equals(obj) || NULL.equals(strObj.toLowerCase())) {
            if (tv != null)
                color = tv.getCurrentTextColor();
            str = GG + "%";
        } else {
            double were1 = Parse.getInstance().parseDouble(obj);
            if (tv != null) {
                if (were1 > 0) {
                    color = colorsConfigure.getUpColor();
                    if (isShowPlus)
                        plus = "+";
                } else if (were1 < 0) {
                    color = colorsConfigure.getLowColor();
                } else {
                    color = tv.getCurrentTextColor();
                    if (isShowPlus)
                        plus = "+";
                }
            } else {
                if (were1 >= 0) {
                    if (isShowPlus)
                        plus = "+";
                }
            }
            str = plus + String.format(Locale.ENGLISH, "%.2f", were1) + "%";
        }
        if (tv != null) {
            SpannableString s = new SpannableString(str);
            s.setSpan(new ForegroundColorSpan(color), 0, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv.setText(s);
        }
        return str;
    }

    /**
     * 将数字转为四舍五入保留2位小数的字符串
     *
     * @param obj
     * @return string
     */
    public String parse2String(Object obj) {
        return parse2String(obj, 2);
    }

    /**
     * 将数字4舍5入到任意小数位
     *
     * @param obj
     * @param decPlace 保留小数位数
     * @return
     */
    public String parse2String(Object obj, int decPlace) {
        if (TextUtils.isEmpty(Parse.getInstance().toString(obj))
                || GG.equals(obj) || NULL.equals(obj)) {
            return GG;
        }
        if (decPlace < 0) {
            decPlace = Math.abs(decPlace);
        }
        double d = Parse.getInstance().parseDouble(obj);
        String str = String.format(Locale.ENGLISH,
                "%." + decPlace + "f", (Double.isNaN(d) || Double.isInfinite(d)) ? 0 : d);
        StringBuffer sb = new StringBuffer("-0");
        if (decPlace > 0) {
            sb.append(".");
            for (int i = 0; i < decPlace; i++) {
                sb.append("0");
            }
        }
        return str.equals(sb.toString()) ? sb.substring(1, sb.length()) : str;
    }

    /**
     * 将数字4舍5入到任意小数位
     *
     * @param obj
     * @param decPlace    保留小数位数
     * @param isAddSymbol 返回的价格前是否追加ExrateType中的价格类型
     * @return
     */
    public String parse2String(Object obj, int decPlace, boolean isAddSymbol) {
        String strObj = Parse.getInstance().toString(obj);
        if (TextUtils.isEmpty(strObj)
                || GG.equals(obj) || NULL.equals(strObj.toLowerCase())) {
            return GG;
        }
        double d = Parse.getInstance().parseDouble(obj);
        if (decPlace < 0) {
            decPlace = Math.abs(decPlace);
        }
        String str = String.format(Locale.ENGLISH,
                "%." + decPlace + "f", (Double.isNaN(d) || Double.isInfinite(d)) ? 0 : d);
        StringBuffer sb = new StringBuffer("-0");
        if (decPlace > 0) {
            sb.append(".");
            for (int i = 0; i < decPlace; i++) {
                sb.append("0");
            }
        }
        str = str.equals(sb.toString()) ? sb.substring(1, sb.length()) : str;
        return str;
    }

    /**
     * 将数字转换成任意有效位数（注意针对价格规则）
     *
     * @param close       价格
     * @param isAddSymbol 返回的价格前是否追加ExrateType中的价格类型
     * @param num         保留有效位数（并非小数位数）
     */
    public String parse2String(Object close, boolean isAddSymbol, int num) {
        String str;
        String close1 = Parse.getInstance().toString(close);
        if (TextUtils.isEmpty(close1)
                || GG.equals(close1) || NULL.equals(close1.toLowerCase())) {
            str = GG;
            return str;
        } else {
            double c = Parse.getInstance().parseDouble(close);
            if (c >= 10) {
                str = parse2String(c);
            } else {
                str = parseEffectiveNumbers(c, num);
            }
        }
        return str;
    }

    /**
     * 格式化成交量
     * 数字转化为保留decPlace位小数的字符串，自动解析亿，万，四舍五入
     *
     * @param decPlace 小数的位数
     * @param div100   主要针对成交量需要除以100，转换为手；true为去掉最后两位
     * @return 例如：10000000 -》 1.0亿
     */
    public String parse2CNStringVol(Object obj, int decPlace, boolean div100) {
        if (TextUtils.isEmpty(Parse.getInstance().toString(obj))
                || GG.equals(obj) || NULL.equals(obj)) {
            return GG;
        }

        double dou = Parse.getInstance().parseDouble(obj);
        if (Double.isNaN(dou) || Double.isInfinite(dou)) {
            return GG;
        }
        if (decPlace < 0) {
            decPlace = Math.abs(decPlace);
        }
        String oldstr = String.format(Locale.ENGLISH, "%.0f", dou);
        if (div100) {
            // 两位数字以下，返回0手
            if (oldstr.length() <= 2) {
                oldstr = "0";
            } else {
                dou = dou / 100f;
                oldstr = oldstr.substring(0, oldstr.length() - 2);
            }
        }
        String newStr = oldstr;
        if (Language.CHINA == KlineLanguageUtils.INSTANCE.getCurLanguage()) {
            if (oldstr.length() > 12) {
                newStr = String.format(Locale.ENGLISH, "%." + decPlace + "f",
                        dou / 1000000000000.0) + "万亿";
            } else if (oldstr.length() > 8) {
                newStr = String.format(Locale.ENGLISH, "%." + decPlace + "f",
                        dou / 100000000.0) + "亿";
            } else if (oldstr.length() > 4) {
                newStr = String.format(Locale.ENGLISH, "%." + decPlace + "f",
                        dou / 10000.0) + "万";
            }
        } else {
            if (oldstr.length() > 12) {
                newStr = String.format(Locale.ENGLISH,
                        "%." + decPlace + "f",
                        dou / 1000000000.0) + "b";
            } else if (oldstr.length() > 9) {
                newStr = String.format(Locale.ENGLISH,
                        "%." + decPlace + "f",
                        dou / 1000000000.0) + "b";
            } else if (oldstr.length() > 6) {
                newStr = String.format(Locale.ENGLISH,
                        "%." + decPlace + "f",
                        dou / 1000000.0) + "m";
            } else if (oldstr.length() > 3) {
                newStr = String.format(Locale.ENGLISH,
                        "%." + decPlace + "f", dou / 1000.0) +
                        "k";
            }
        }
        return newStr;
    }

    /**
     * 格式化成交量
     * 数字转化为保留num位小数的字符串，自动解析亿，万，四舍五入
     *
     * @param obj
     * @param decPlace 小数的位数
     * @return 例如：10000000 -》 1.0亿
     */
    public String parse2CNStringVol(Object obj, int decPlace) {
        return parse2CNStringVol(obj, decPlace, false);
    }

    /**
     * 格式化成交额
     *
     * @param obj
     * @param isAddSymbol
     * @return
     */
    public String parse2CNStringCJE(Object obj, boolean isAddSymbol) {
        String objStr = Parse.getInstance().toString(obj);
        if (TextUtils.isEmpty(objStr)
                || GG.equals(objStr) || NULL.equals(objStr.toLowerCase())) {
            return GG;
        }
        double dou = Parse.getInstance().parseDouble(obj);
        String newstr;
        if (Language.CHINA == KlineLanguageUtils.INSTANCE.getCurLanguage()) {
            if (Math.abs(dou) >= 100000000000L) {
                newstr = String.format(Locale.ENGLISH, "%.0f", dou / 100000000.0) + "亿";
            } else if (Math.abs(dou) >= 100000000) {
                newstr = parseEffectiveNumbers(dou / 100000000.0, 4) + "亿";
            } else if (Math.abs(dou) >= 10000) {
                newstr = parseEffectiveNumbers(dou / 10000.0, 4) + "万";
            } else {
                newstr = parseEffectiveNumbers(dou, 4);
            }
        } else {
            if (Math.abs(dou) >= 1000000000000L) {
                newstr = String.format(Locale.ENGLISH, "%.0f", dou / 1000000000.0) + "b";
            } else if (Math.abs(dou) >= 1000000000L) {
                newstr = parseEffectiveNumbers(dou / 1000000000.0, 4) + "b";
            } else if (Math.abs(dou) >= 1000000L) {
                newstr = parseEffectiveNumbers(dou / 1000000.0, 4) + "m";
            } else if (Math.abs(dou) >= 1000L) {
                newstr = parseEffectiveNumbers(dou / 1000.0, 4) + "k";
            } else {
                newstr = parseEffectiveNumbers(dou);
            }
        }
        return newstr;
    }

    /**
     * 格式化成美国数字显示方式：100,000,000
     *
     * @param obj
     * @param isAddSymbol
     * @param decNum      保留小数位数
     * @return
     */
    public String parse2USString(Object obj, boolean isAddSymbol, int decNum) {
        return parse2USString(obj, isAddSymbol, false, decNum);
    }

    /**
     * 格式化成美国数字显示方式：100,000,000
     *
     * @param obj         数值
     * @param isAddSymbol 是否添加法币符号
     * @param isValid     是否保留有效位数
     * @param decNum      保留有效/小数位数
     * @return
     */
    public String parse2USString(Object obj, boolean isAddSymbol, boolean isValid, int decNum) {
        String objStr = Parse.getInstance().toString(obj);
        if (TextUtils.isEmpty(objStr)
                || GG.equals(objStr) || NULL.equals(objStr.toLowerCase())) {
            return GG;
        }
        double dou = Parse.getInstance().parseDouble(obj);
        String str;
        if (isValid && decNum > 0) {
            if (dou >= 10 || dou <= -10) {
                decNum = 2;
            } else {
                str = parseEffectiveNumbers(dou, decNum);
                if (TextUtils.isEmpty(str) || GG.equals(str)) {
                    return GG;
                }
                if (str.contains(".")) {
                    decNum = str.split("\\.")[1].length();
                }
            }
        }
        str = Parse.getInstance().toString(dou, decNum);
//        String[] newStrs = str.split("\\.");
//        String decStr = "";
//        if (newStrs.length > 1) {
//            boolean isHas = false;
//            for (int i = newStrs[1].length() - 1; i > -1; i--) {
//                char n = newStrs[1].charAt(i);
//                if (n == 48 && !isHas) {
//                    continue;
//                } else if (n < 58 && n > 48) {
//                    isHas = true;
//                }
//                if (isHas) {
//                    decStr = n + decStr;
//                }
//            }
//        }
//        if (TextUtils.isEmpty(decStr)) {
//            str = newStrs[0];
//        } else {
//            str = newStrs[0] + "." + decStr;
//        }
        return str;
    }

    /**
     * 保留4位有效位数
     *
     * @param obj
     * @return
     */
    public String parseEffectiveNumbers(Object obj) {
        return parseEffectiveNumbers(obj, 4);
    }

    /**
     * 保留有效位数
     *
     * @param obj
     * @param num
     * @return
     */
    public String parseEffectiveNumbers(Object obj, int num) {
        String objStr = Parse.getInstance().toString(obj);
        if (TextUtils.isEmpty(objStr)
                || GG.equals(objStr) || NULL.equals(objStr.toLowerCase())) {
            return GG;
        }
        double price = Parse.getInstance().parseDouble(obj);
        return parseEffectiveNumbers(price, num);
    }

    /**
     * 保留有效位数
     *
     * @param price
     * @param num
     * @return
     */
    private String parseEffectiveNumbers(double price, int num) {
        int sum = 0;
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            return GG;
        }
        if (num < 0) {
            num = Math.abs(num);
        }
        String strs = String.format(Locale.ENGLISH, "%.15f", price);
        String[] strings = strs.split("\\.");
        if (strings[0].length() >= num) {
            if (strings[0].substring(0, 1).equals("-")) {
                if (strings[0].length() > num) {
                    return String.format(Locale.ENGLISH, "%.0f", price);
                }
            } else {
                return String.format(Locale.ENGLISH, "%.0f", price);
            }
        } else if (price == 0) {
            return "0.00";
        }
        StringBuffer newStrs = new StringBuffer();
        for (int i = 0; i < strs.length(); i++) {
            char n = strs.charAt(i);
            newStrs.append(n);
            if (sum == 0) {
                if (n < 58 && n > 48) {
                    ++sum;
                }
            } else {
                if (n < 58 && n > 47) {
                    ++sum;
                }
            }
            if (sum == num) {
                String[] newStrings = newStrs.toString().split("\\.");
                String string = String.format(Locale.ENGLISH, "%." + newStrings[1].length() + "f", price);
                return string;
            }
        }
        String[] newStrings = newStrs.toString().split("\\.");
        String string = newStrs.toString();
        for (int i = 0; i < num - (newStrings[0].length() + newStrings[1].length()); i++) {
            string += "0";
        }
        return string;
    }

    /**
     * 计算涨跌幅并设置背景颜色
     *
     * @param were       涨跌幅
     * @param tv         TextView
     * @param isShowPlus 涨跌幅为正时是否显示加号
     */
    public int formatTextZDFOfZDFBackground(Object were, TextView tv, boolean isShowPlus) {
        int color;
        String str;
        int upDown = 0;
        if (TextUtils.isEmpty(Parse.getInstance().toString(were))
                || GG.equals(were) || NULL.equals(were)) {
            color = colorsConfigure.getFlatBackground();
            str = GG + "%";
        } else {
            String show = "";
            double wereD = Parse.getInstance().parseDouble(were);
            if (wereD > 0) {
                color = colorsConfigure.getUpBackground();
                if (isShowPlus) {
                    show = "+";
                }
                upDown = 1;
            } else if (wereD < 0) {
                color = colorsConfigure.getLowBackground();
                upDown = -1;
            } else {
                color = colorsConfigure.getFlatBackground();
                if (isShowPlus) {
                    show = "+";
                }
                upDown = 0;
            }
            str = show + String.format(Locale.ENGLISH, "%.2f", wereD) + "%";
        }
        tv.setBackgroundColor(color);
        tv.setText(str);
        return upDown;
    }

    /**
     * 去除末尾0，如果小数位数不满decNum，则保留decNum小数位
     *
     * @param obj
     * @param decNum
     * @return
     */
    public String deleteEndZero(Object obj, int decNum) {
        String str = deleteEndZero(obj);
        if (GG.equals(str)) {
            return str;
        }
        if (decNum < 0) {
            decNum = Math.abs(decNum);
        }
        if (!str.contains(".")) {
            if (decNum > 0) {
                str = parse2String(str, decNum);
            }
            if (KlineConfig.SHOW_US_PRICE && !GG.equals(str)) {
                str = Parse.getInstance().toString(str, decNum);
            }
        } else {
            String[] strs = str.split("\\.");
            if (strs == null || strs.length < 2 || strs[1].length() < decNum) {
                str = parse2String(str, decNum);
                if (KlineConfig.SHOW_US_PRICE && !GG.equals(str)) {
                    str = Parse.getInstance().toString(str, decNum);
                }
            } else {
                if (KlineConfig.SHOW_US_PRICE && !GG.equals(str)) {
                    str = Parse.getInstance().toString(str, strs[1].length());
                }
            }
        }
        return str;
    }

    /**
     * 去除末尾0
     *
     * @param obj
     * @return
     */
    public String deleteEndZero(Object obj) {
        String str = Parse.getInstance().toString(obj);
        if (TextUtils.isEmpty(str)
                || GG.equals(str) || NULL.equals(str.toLowerCase())) {
            return GG;
        }
        if (obj != null && obj instanceof String) {
            str = Parse.getInstance().toString(obj);
            if (!str.contains(".")) {
                return str;
            }
        } else {
            double dou = Parse.getInstance().parseDouble(obj);
            str = String.format(Locale.ENGLISH, "%.15f", dou);
        }
        String[] newStrs = str.split("\\.");
        if (newStrs == null || newStrs.length == 0) {
            return str;
        } else if (newStrs.length == 1) {
            return newStrs[0];
        }
        if (TextUtils.isEmpty(newStrs[0])) {
            newStrs[0] = "0";
        }
        String decStr = "";
        boolean isHas = false;
        for (int i = newStrs[1].length() - 1; i > -1; i--) {
            char n = newStrs[1].charAt(i);
            if (n == 48 && !isHas) {
                continue;
            } else if (n < 58 && n > 48) {
                isHas = true;
            }
            if (isHas) {
                decStr = n + decStr;
            }
        }
        if (TextUtils.isEmpty(decStr)) {
            str = newStrs[0];
        } else {
            str = newStrs[0] + "." + decStr;
        }
        return str;
    }

    /**
     * 去除末尾0，如果小数位数不满decNum，则保留decNum小数位
     *
     * @param obj
     * @param decNum
     * @return
     */
    public String deleteEndZero(Object obj, int decNum, Boolean showUS) {
        String str = deleteEndZero(obj);
        if (GG.equals(str)) {
            return str;
        }
        if (decNum < 0) {
            decNum = Math.abs(decNum);
        }
        if (!str.contains(".")) {
            if (decNum > 0) {
                str = parse2String(str, decNum);
            }
            if (KlineConfig.SHOW_US_PRICE && !GG.equals(str) && showUS) {
                str = Parse.getInstance().toString(str, decNum);
            }
        } else {
            String[] strs = str.split("\\.");
            if (strs == null || strs.length < 2 || strs[1].length() < decNum) {
                str = parse2String(str, decNum);
                if (KlineConfig.SHOW_US_PRICE && !GG.equals(str) && showUS) {
                    str = Parse.getInstance().toString(str, decNum);
                }
            } else {
                if (KlineConfig.SHOW_US_PRICE && !GG.equals(str) && showUS) {
                    str = Parse.getInstance().toString(str, strs[1].length());
                }
            }
        }
        return str;
    }

    /**
     * 获取震幅
     */
    public String getAmpl(KLineDataValid item) {
        if (item.getOpen() == 0.0) {
            return "0.00%";
        } else {
            return parse2String((item.getHig() - item.getLow()) / item.getOpen() * 100, 2) + "%";
        }
    }
}
