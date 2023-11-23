package com.widget.stock.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Utils {

    private static Utils instance;

    private Utils() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static synchronized Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    /**
     * MD5加密，32位
     *
     * @param url 需加密的字符串
     * @return 加密后的字符串
     */
    public String MD5(String url) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            // e.printStackTrace();
            return getFile(url);
        }
        char[] charArray = url.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 将URL转成能够识别的目录
     */
    public String getFile(String url) {
        String path = url;
        if (path.contains("?")) {
            path = path.replace("?", "_");
        }
        if (path.contains("/")) {
            path = path.replace("/", "_");
        }
        if (path.contains(".")) {
            path = path.replace(".", "_");
        }
        return path;
    }


    /**
     * 获取私有缓存目录中的自定义文件路径
     *
     * @param context  上下文
     * @param path     二级目录，三级目录中间用"/"分隔，前后不需要加"/"
     * @param fileName 文件名，自动转成md5的名字
     * @param isMD5    文件名是否加密
     * @return File
     */
    public File getPrivateCache(Context context, String path, String fileName,
                                boolean isMD5) {
        File file = new File(context.getCacheDir() + "/" + path);
        if (file == null)
            return null;
        if (!file.exists()) {
            file.mkdirs();
        }
        if (isMD5)
            file = new File(file.getPath() + "/" + MD5(fileName));
        else
            file = new File(file.getPath() + "/" + fileName);
        return file;
    }

    /**
     * 时间戳转换成时间字符窜
     *
     * @param format 时间格式
     * @param time   时间戳
     * @return
     */
    public String date2String(String format, long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(d);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context 上下文
     * @param dpValue dp单位
     * @return px（像素）单位
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (dpValue * scale + 0.5f);
        if (px == 0) {
            if (dpValue < 0) {
                px = -1;
            } else if (dpValue > 0) {
                px = 1;
            } else {
                px = 0;
            }
        }
        return px;
    }

    /**
     * 拆分比较
     *
     * @param base  基数
     * @param alive 与基数进行比较的字符
     * @param split 两个相比较的间隔符
     * @return alive大于等于base返回fals，alive小于base返回true
     */
    public boolean isMin(String base, String alive, String split) {
        boolean isMin = false;
        if (base == null) {
            throw new NullPointerException("比较基数不可为Null");
        }
        if (alive == null) {
            throw new NullPointerException("比较数不可为Null");
        }
        if (split == null || split.length() == 0) {
            if (Parse.getInstance().parseInt(alive) < Parse.getInstance().parseInt(base)) {
                isMin = true;
            } else {
                isMin = false;
            }
            return isMin;
        }
        if (!base.contains(split) && !alive.contains(split)) {
            if (Parse.getInstance().parseInt(alive) < Parse.getInstance().parseInt(base)) {
                isMin = true;
            } else {
                isMin = false;
            }
            return isMin;
        }
        String[] bases = null;
        String[] alives = null;
        if (base.contains(split)) {
            String splitCopy = split;
            if (".".equals(split)) {
                splitCopy = "\\.";
            }
            bases = base.split(splitCopy);
        } else {
            bases = new String[1];
            bases[0] = base;
        }

        if (alive.contains(split)) {
            String splitCopy = split;
            if (".".equals(split)) {
                splitCopy = "\\.";
            }
            alives = alive.split(splitCopy);
        } else {
            alives = new String[1];
            alives[0] = alive;
        }

        int[] baseS;
        int[] aliveS;
        if (bases.length > alives.length) {
            baseS = new int[bases.length];
            aliveS = new int[bases.length];
        } else {
            baseS = new int[alives.length];
            aliveS = new int[alives.length];
        }

        for (int i = 0; i < baseS.length; i++) {
            if (i < bases.length) {
                baseS[i] = Parse.getInstance().parseInt(bases[i]);
            } else {
                baseS[i] = 0;
            }
            if (i < alives.length) {
                aliveS[i] = Parse.getInstance().parseInt(alives[i]);
            } else {
                aliveS[i] = 0;
            }
        }

        for (int i = 0; i < baseS.length; i++) {
            if (aliveS[i] < baseS[i]) {
                isMin = true;
                break;
            } else if (aliveS[i] > baseS[i]) {
                isMin = false;
                break;
            } else {
                continue;
            }
        }
        return isMin;
    }

    /**
     * 拆分比较
     *
     * @param base  基数
     * @param alive 与base进行比较的字符
     * @param split 两个相比较的间隔符
     * @return alive小于等于base返回fals，alive大于base返回true
     */
    public boolean isMax(String base, String alive, String split) {
        boolean isMax = false;
        if (base == null) {
            throw new NullPointerException("比较基数不可为Null");
        }
        if (alive == null) {
            throw new NullPointerException("比较数不可为Null");
        }
        if (split == null || split.length() == 0) {
            if (Parse.getInstance().parseInt(alive) > Parse.getInstance().parseInt(base)) {
                isMax = true;
            } else {
                isMax = false;
            }
            return isMax;
        }
        if (!base.contains(split) && !alive.contains(split)) {
            if (Parse.getInstance().parseInt(alive) > Parse.getInstance().parseInt(base)) {
                isMax = true;
            } else {
                isMax = false;
            }
            return isMax;
        }
        String[] bases = null;
        String[] alives = null;
        if (base.contains(split)) {
            String splitCopy = split;
            if (".".equals(split)) {
                splitCopy = "\\.";
            }
            bases = base.split(splitCopy);
        } else {
            bases = new String[1];
            bases[0] = base;
        }

        if (alive.contains(split)) {
            String splitCopy = split;
            if (".".equals(split)) {
                splitCopy = "\\.";
            }
            alives = alive.split(splitCopy);
        } else {
            alives = new String[1];
            alives[0] = alive;
        }

        int[] baseS;
        int[] aliveS;
        if (bases.length > alives.length) {
            baseS = new int[bases.length];
            aliveS = new int[bases.length];
        } else {
            baseS = new int[alives.length];
            aliveS = new int[alives.length];
        }

        for (int i = 0; i < baseS.length; i++) {
            if (i < bases.length) {
                baseS[i] = Parse.getInstance().parseInt(bases[i]);
            } else {
                baseS[i] = 0;
            }
            if (i < alives.length) {
                aliveS[i] = Parse.getInstance().parseInt(alives[i]);
            } else {
                aliveS[i] = 0;
            }
        }

        for (int i = 0; i < baseS.length; i++) {
            if (aliveS[i] > baseS[i]) {
                isMax = true;
                break;
            } else if (aliveS[i] < baseS[i]) {
                isMax = false;
                break;
            } else {
                continue;
            }
        }
        return isMax;
    }
}
