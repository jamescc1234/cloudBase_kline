package com.widget.stock.k_line.data

/**
 * @author: Galon
 * @date: 2022/11/15
 */
data class TradeInfo(var isBuyOrder: Boolean = false, var price: String = "", var amount: String = "", var time: Long = -1)