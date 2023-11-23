package com.drawing

/**
 * @author James Chen
 * @date 31/5/2023
 */
data class DrawingStatusData(
    val type: DrawingType,
    val needPoint: Int,
    val alreadyAddedPoint: Int
)
