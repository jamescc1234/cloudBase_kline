package com.widget.stock.k_line

import android.content.Context
import android.graphics.Color
import binance.stock.library.R
import com.google.gson.Gson
import com.utils.LocalStore
import com.utils.getStringRes
import com.widget.stock.k_line.data.KLineSetModel
import com.widget.stock.k_line.data.KLineSetModelConfig

/**
 *
 * @ProjectName:    bnb_android
 * @Package:        com.prometheus.account.activities.klineset
 * @ClassName:      KLineSetUtils
 * @Description:    K线设置工具类
 * @Author:         Jerry Wang
 * @CreateDate:     2020/3/23 3:31 PM
 * @Version:        1.0
 */
object KLineSetUtils {

    private const val TAG = "KLineSetUtils"

    const val KLINE_WIDTH_SMALL = "small"
    const val KLINE_WIDTH_MID = "mid"
    const val KLINE_WIDTH_BIG = "big"


    const val KLINE_COLOR_YELLOW = "yellow"//黄色
    const val KLINE_COLOR_PINK = "pink"//粉色
    const val KLINE_COLOR_PURPLE = "purple"//紫色
    const val KLINE_COLOR_GREEN = "green"//绿色
    const val KLINE_COLOR_RED = "red"//红色
    const val KLINE_COLOR_LIGHT_GREEN = "light_green"//浅绿色
    const val KLINE_COLOR_LIGHT_PURPLE = "light_purple"//浅紫色
    const val KLINE_COLOR_LIGHT_YELLOW = "light_yellow"//浅黄色
    const val KLINE_COLOR_ORANGE = "orange"//橙色
    const val KLINE_COLOR_BLUE = "blue"//蓝色

    const val BOLL_DEFAULT_CYCLE = 21
    const val BOLL_DEFAULT_BANDWIDTH = 2

    const val MACD_DEFAULT_SHORT_PERIOD = 12
    const val MACD_DEFAULT_LONG_PERIOD = 26
    const val MACD_DEFAULT_MA_PERIOD = 9

    const val MACD_STYLE_HOLLOW = "Hollow"
    const val MACD_STYLE_SOLID = "Solid"

    const val KDJ_DEFAULT_CYCLE = 9
    const val KDJ_DEFAULT_MA_PERIOD1 = 3
    const val KDJ_DEFAULT_MA_PERIOD2 = 3

    const val STOCH_RSI_DEFAULT_LENGTH_RSI = 14
    const val STOCH_RSI_DEFAULT_LENGTH_STOCH = 14
    const val STOCH_RSI_DEFAULT_SMOOTH_K = 3
    const val STOCH_RSI_DEFAULT_SMOOTH_D = 3

    // 魔法值：-98代表不显示编辑框  -99不显示编辑框，线条宽度，线条颜色
    const val GONE_EDIT = -98
    const val GONE_EDIT_WIDTH_COLOR = -99

    fun getColorByName(name: String): String = when (name) {
        KLINE_COLOR_YELLOW -> "#F0B90B"
        KLINE_COLOR_PINK -> "#E840B5"
        KLINE_COLOR_PURPLE -> "#8B68C4"
        KLINE_COLOR_GREEN -> "#44DB5E"
        KLINE_COLOR_RED -> "#A61B61"
        KLINE_COLOR_LIGHT_GREEN -> "#81E3CA"
        KLINE_COLOR_LIGHT_PURPLE -> "#949EDE"
        KLINE_COLOR_LIGHT_YELLOW -> "#CCDC6C"
        KLINE_COLOR_ORANGE -> "#E77552"
        KLINE_COLOR_BLUE -> "#4A5BEE"
        else -> "#828A98"
    }


    fun getWidthIntByName(name: String): Int = when (name) {
        KLINE_WIDTH_SMALL -> 2
        KLINE_WIDTH_MID -> 3
        KLINE_WIDTH_BIG -> 4
        else -> 1
    }

    fun saveConfig(config: KLineSetModelConfig, indicatorName: String) {
        LocalStore.getInstance().put(indicatorName, Gson().toJson(config, KLineSetModelConfig::class.java))
    }

    fun getConfig(indicatorName: String, userDefault: Boolean = false): KLineSetModelConfig {
        val gsonCache = LocalStore.getInstance().getString(indicatorName, "")
        return if (gsonCache.isNullOrEmpty() || userDefault) when (indicatorName) {
            "MA" -> KLineSetModelConfig(indicatorType = "MA", models = getMADefault())
            "EMA" -> KLineSetModelConfig(indicatorType = "EMA", models = getEMADefault())
            "BOLL" -> KLineSetModelConfig(indicatorType = "BOLL", models = getBOLLDefault()
                , period1 = BOLL_DEFAULT_CYCLE
                , period2 = BOLL_DEFAULT_BANDWIDTH
            )
            "MACD" -> KLineSetModelConfig(indicatorType = "MACD", models = getMACDDefault()
                , period1 = MACD_DEFAULT_SHORT_PERIOD
                , period2 = MACD_DEFAULT_LONG_PERIOD
                , period3 = MACD_DEFAULT_MA_PERIOD
            )
            "KDJ" -> KLineSetModelConfig(indicatorType = "KDJ", models = getKDJDefault()
                , period1 = KDJ_DEFAULT_CYCLE
                , period2 = KDJ_DEFAULT_MA_PERIOD1
                , period3 = KDJ_DEFAULT_MA_PERIOD2
            )
            "RSI" -> KLineSetModelConfig(indicatorType = "RSI", models = getRSIDefault())
            else -> KLineSetModelConfig(indicatorType = "", models = arrayListOf())
        } else {
            Gson().fromJson(gsonCache, KLineSetModelConfig::class.java)
        }
    }

    fun getParams(config: KLineSetModelConfig): Array<Int>{
        return when (config.indicatorType) {
            "MA" -> arrayOf(config.models[0].indicatorNum, config.models[1].indicatorNum,config.models[2].indicatorNum,0,0,0,1,1,1,0,0,0)
            "EMA" -> arrayOf(config.models[0].indicatorNum, config.models[1].indicatorNum,config.models[2].indicatorNum,0,0,0,1,1,1,0,0,0)
            "BOLL" -> arrayOf(config.period1, config.period2)
            "MACD" -> arrayOf(config.models[0].indicatorNum, config.models[1].indicatorNum)
            "KDJ" -> arrayOf(config.models[0].indicatorNum, config.models[1].indicatorNum)
            "RSI" -> arrayOf(config.models[0].indicatorNum, config.models[1].indicatorNum)
            else -> arrayOf(0)
        }
    }

    fun getLineColors(config: KLineSetModelConfig): Array<Int> {
        return when (config.indicatorType) {
            "MA" -> arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)), Color.parseColor(getColorByName(config.models[2].lineColor)))
            "EMA" -> arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)),Color.parseColor(getColorByName(config.models[2].lineColor)))
            "BOLL" -> arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)),Color.parseColor(getColorByName(config.models[2].lineColor)))
            "MACD" -> arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)),Color.parseColor(getColorByName(config.models[2].lineColor)))
            "KDJ" ->  arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)),Color.parseColor(getColorByName(config.models[2].lineColor)))
            "RSI" ->  arrayOf(Color.parseColor(getColorByName(config.models[0].lineColor)), Color.parseColor(getColorByName(config.models[1].lineColor)),Color.parseColor(getColorByName(config.models[2].lineColor)))
            else -> arrayOf(0)
        }
    }

    fun getLineWidth(config: KLineSetModelConfig): Array<Int> {
        return when (config.indicatorType) {
            "MA" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            "EMA" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            "BOLL" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            "MACD" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            "KDJ" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            "RSI" -> arrayOf(getWidthIntByName(config.models[0].lineWidth), getWidthIntByName(config.models[1].lineWidth), getWidthIntByName(config.models[2].lineWidth))
            else -> arrayOf(1,1,1)
        }
    }

    internal fun getMADefault(): List<KLineSetModel> = listOf(
        KLineSetModel("MA1", 9, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("MA2", 30, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("MA3", 60, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getEMADefault(): List<KLineSetModel> = listOf(
        KLineSetModel("EMA1", 5, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("EMA2", 10, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("EMA3", 20, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getBOLLDefault(): List<KLineSetModel> = listOf(
        KLineSetModel("UP", 21, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("MB", 2, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("DN", 2, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getBOLLDefaultPeriod(context: Context): List<Pair<String, Int>> = listOf(
        Pair(context.getStringRes(R.string.calculating_period), BOLL_DEFAULT_CYCLE),
        Pair(context.getStringRes(R.string.bandwidth), BOLL_DEFAULT_BANDWIDTH)
    )

    internal fun getMACDDefault(): List<KLineSetModel> = listOf(
        KLineSetModel("DIF", 12, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("DEM", 26, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("MACD", 9, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getMACDDefaultPeriod(context: Context): List<Pair<String, Int>> = listOf(
        Pair(context.getStringRes(R.string.short_period), MACD_DEFAULT_SHORT_PERIOD),
        Pair(context.getStringRes(R.string.long_period), MACD_DEFAULT_LONG_PERIOD),
        Pair(context.getStringRes(R.string.ma_period), MACD_DEFAULT_MA_PERIOD)
    )

    internal fun getKDJDefault(): List<KLineSetModel> = listOf(
        KLineSetModel("%K", 9, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("%D", 3, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("%J", 3, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getKDJDefaultPeriod(context: Context): List<Pair<String, Int>> = listOf(
        Pair(context.getStringRes(R.string.calculating_period), KDJ_DEFAULT_CYCLE),
        Pair(context.getStringRes(R.string.ma1_period), KDJ_DEFAULT_MA_PERIOD1),
        Pair(context.getStringRes(R.string.ma2_period), KDJ_DEFAULT_MA_PERIOD2)
    )

    internal fun getRSIDefault(): List<KLineSetModel> = listOf(
        KLineSetModel("RSI1", 6, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true),
        KLineSetModel("RSI2", 14, KLINE_WIDTH_SMALL, KLINE_COLOR_PINK, true),
        KLineSetModel("RSI3", 24, KLINE_WIDTH_SMALL, KLINE_COLOR_PURPLE, true)
    )

    internal fun getOBVDefault(): List<KLineSetModel> = listOf(
            KLineSetModel("OBV", 6, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true)
    )

    internal fun getWRDefault(): List<KLineSetModel> = listOf(
            KLineSetModel("WR1", 6, KLINE_WIDTH_SMALL, KLINE_COLOR_YELLOW, true)
    )

    internal fun getSARDefault(): KLineSetModel = KLineSetModel("SAR", 0, "", KLINE_COLOR_YELLOW, true)

    internal fun getStochRSIDefault(): List<KLineSetModel> = listOf(
            KLineSetModel("K%", 0, KLINE_WIDTH_MID, KLINE_COLOR_YELLOW, true),
            KLineSetModel("D%", 0, KLINE_WIDTH_MID, KLINE_COLOR_PURPLE, true),
    )

    fun getDrawableId(context: Context, resName: String) = context.resources.getIdentifier(resName, "drawable", context.packageName)
    fun getColorId(context: Context, resName: String) = context.resources.getIdentifier(resName, "color", context.packageName)


    private val allColorList = listOf(KLINE_COLOR_YELLOW, KLINE_COLOR_RED, KLINE_COLOR_GREEN, KLINE_COLOR_PURPLE, KLINE_COLOR_PINK)
}