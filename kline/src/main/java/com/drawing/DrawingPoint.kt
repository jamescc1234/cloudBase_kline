package com.drawing

/**
 * @author James Chen
 * @date 18/4/2023
 */
data class DrawingPoint(
    var timeStamp: Long,
    var price: Double,
    var realX: Double? = null,
    var realY: Double? = null
)
