package com.drawing

import android.graphics.Color
import android.graphics.Path
import android.util.Base64.encode
import com.google.gson.Gson
import com.widget.stock.k_line.data.Offset
import com.widget.stock.k_line.view.Position

/**
 * @author James Chen
 * @date 18/4/2023
 */
class DrawingData {
    var type: DrawingType? = null
    val points = mutableListOf<DrawingPoint>()

    // todo
    //屏幕上真正绘制的起始点和终点，比如射线，用户选中了2个点，但是真正绘制的直线为起始点，到屏幕边缘
    //所以start为起始点，end为过用户选中的两个点所在直线到屏幕边缘垂线的交点
    var start: Position? = null
    var end: Position? = null

    var realPath: Path? = null
    var isSelected = false
    var isLocked = false
//    var color = "0xFFED7E33"
    var color = Color.parseColor("#ED3333")

    //填充颜色， 在矩形和平行线有效
//    var fillColor = "0x4DED7E33"
    var fillColor = Color.parseColor("#26ED3333")
    var width: Float = 2f
    var dashGap = 0.0
    var dashWidth: Float = 5f

    fun toJson(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["type"] = type?.name ?: ""
        map["points"] = points.map { it }
        map["color"] = color
        map["fillColor"] = fillColor
        map["width"] = width
        map["dashGap"] = dashGap
        map["dashWidth"] = dashWidth
        map["isSelected"] = isSelected
        map["isLocked"] = isLocked
        return map
    }

    fun fromJson(jsonData: Map<String, Any>) {
        if (jsonData["type"] != null) {
            type = DrawingType.valueOf(jsonData["type"] as String)
        }
        val gson = Gson()

        if (jsonData["points"] != null) {
            val points = jsonData["points"] as List<Map<String, Any>>
            points.forEach {
                val point = gson.fromJson(gson.toJson(it), DrawingPoint::class.java)
                this.points.add(point)
            }
        }
        color = jsonData["color"] as Int
        fillColor = jsonData["fillColor"] as Int
        width = jsonData["width"] as Float
        dashGap = jsonData["dashGap"] as Double
        dashWidth = jsonData["dashWidth"] as Float
        isSelected = jsonData["isSelected"] as Boolean
        isLocked = jsonData["isLocked"] as Boolean
    }

    fun updateConfigFromJson(jsonData: Map<String, Any>) {
        color = jsonData["color"] as Int
        fillColor = jsonData["fillColor"] as Int
        width = jsonData["width"] as Float
        dashGap = jsonData["dashGap"] as Double
        dashWidth = jsonData["dashWidth"] as Float
        isLocked = jsonData["isLocked"] as Boolean
    }
}