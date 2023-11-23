package com.drawing
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.utils.*
import com.widget.stock.k_line.view.Position

/**
 * @author James Chen
 * @date 18/4/2023
 */
class DrawingStatus {
    companion object {
        private val TAG = DrawingStatus::class.java.simpleName
    }

    private var drawingStatusListener: DrawingStatusListener? = null
    private var drawingMode = false
    var continueDrawing = false
    var hideAllDrawing = false
    var currentDrawingData: DrawingData? = null
    var currentSelectedDrawingData: DrawingData? = null
    var drawingType: DrawingType? = null
    var drawingDataList = mutableListOf<DrawingData>()

    var movingPoint: Position? = null
    var defaultConfig = DrawingData()

    fun addDrawingStatusListener(listener: DrawingStatusListener) {
        this.drawingStatusListener = listener
    }

    init {
        readDefaultConfig()
    }

    fun isDrawing(): Boolean {
        return currentDrawingData != null && drawingType != null
    }

    fun setDrawingMode(enable: Boolean) {
        drawingMode = enable
        if (!enable) {
            clearSelectStatus()
            currentDrawingData = null
        }
    }

    fun getDrawingMode(): Boolean {
        return drawingMode
    }

    fun clearAllDrawing() {
        drawingDataList.clear()
    }

    fun clearSelectStatus() {
        var hasSelected = false

        for (element in drawingDataList) {
            if (element.isSelected) {
                element.isSelected = false
                hasSelected = true
            }
        }

        currentSelectedDrawingData = null
        drawingStatusListener?.notifyDrawingSelectedStatus(null)
    }

    fun startDrawing(type: DrawingType) {
        clearSelectStatus()
        drawingType = type
        currentDrawingData = DrawingData()
        currentDrawingData?.updateConfigFromJson(defaultConfig.toJson())
        currentDrawingData?.type = type
        drawingStatusListener?.notifyDrawingSelectedStatus(currentDrawingData)
        drawingStatusListener?.reportDrawingStatusChanged(DrawingStatusData(type, type.needPoint(), 0))
    }

    fun completeDrawing(drawingData: DrawingData) {
        currentDrawingData = null
        drawingType = null
        selectDrawingData(drawingData)

        drawingStatusListener?.notifyDrawingCompleted()
        drawingData.type?.let {
            drawingStatusListener?.reportDrawingStatusChanged(DrawingStatusData(it, it.needPoint(), drawingData.points.size))
        }
    }
    
    fun reportDoubleTap(touchArea: TouchArea) {
        drawingStatusListener?.reportDoubleTap(touchArea)
    }

    fun selectDrawingData(data: DrawingData) {
        drawingDataList.forEach {
            it.isSelected = false
        }
        data.isSelected = true
        currentSelectedDrawingData = data
        drawingStatusListener?.notifyDrawingSelectedStatus(data)
    }

    fun continueDrawing(drawingData: DrawingData, reportStatus: Boolean = true) {
        currentDrawingData = drawingData

        if (reportStatus) {
            drawingData.type?.let {
                drawingStatusListener?.reportDrawingStatusChanged(DrawingStatusData(
                    it,
                    it.needPoint(),
                    drawingData.points.size
                ))
            }
        }
    }

    private fun readDefaultConfig() {
        val data = com.binance.hydrogen.storage.shared.StorageManager.get().getDrawingDefaultConfig()
        if (data.isNotEmpty()) {
            Log.d(TAG, "readDefaultConfig: $data")
            if (data.isNotEmpty()) {
                val gson = Gson()
                val type = object : TypeToken<DrawingData?>() {}.type
                defaultConfig = gson.fromJson(data, type)
//                defaultConfig.updateConfigFromJson(config.toJson())
                Log.d(TAG, "readDefaultConfig: ${defaultConfig.color}")
            }
        }
    }

    fun updateLocalDefaultConfig() {
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(defaultConfig)
        // 保持默认配置
        com.binance.hydrogen.storage.shared.StorageManager.get().setDrawingDefaultConfig(gson)
    }

    fun saveToCache(symbol: String, flagTime: String) {
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(drawingDataList)
        com.binance.hydrogen.storage.shared.StorageManager.get().setDrawingCache(symbol, flagTime, gson)
    }

    fun readDataFromCache(symbol: String, flagTime: String) {
        val data = com.binance.hydrogen.storage.shared.StorageManager.get().getDrawingCache(symbol, flagTime)
//        Log.d(TAG, "readDataFromCache: $data")
        if (data.isNotEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<List<DrawingData?>?>() {}.type
            drawingDataList = gson.fromJson(data, type)
        } else {
            drawingDataList.clear()
        }
    }
}