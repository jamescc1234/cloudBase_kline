package com.utils

import com.drawing.DrawingData
import com.drawing.DrawingStatusData

/**
 * @author James Chen
 * @date 12/5/2023
 */
enum class TouchArea {
    MAIN, SUB
}

interface DrawingStatusListener {
    fun notifyDrawingCompleted()
    fun notifyDrawingSelectedStatus(drawingData: DrawingData?)
    fun reportDrawingStatusChanged(drawingStatusData: DrawingStatusData)
    fun reportDoubleTap(area: TouchArea)
}