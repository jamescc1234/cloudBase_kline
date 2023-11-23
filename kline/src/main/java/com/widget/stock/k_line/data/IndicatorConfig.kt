package com.widget.stock.k_line.data

import java.io.Serializable

/**
 * @author: Galon
 * @date: 2022/12/23
 */
data class KLineSetModel(
    var indicatorName: String,
    var indicatorNum: Int,
    var lineWidth: String,
    var lineColor: String,
    var isCheck: Boolean
): Serializable {
    override fun toString(): String {
        return "indicatorNum = $indicatorNum , lineWidth = $lineWidth , lineColor = $lineColor"
    }
}

data class KLineSetModelConfig(
    var indicatorType: String,
    var models: List<KLineSetModel>,
    var period1: Int = 0,
    var period2: Int = 0,
    var period3: Int = 0
): Serializable {
    override fun toString(): String {
        return "indicatorType = $indicatorType , models = $models , period1 = $period1 , period2 = $period2 , period3 = $period3"
    }
}