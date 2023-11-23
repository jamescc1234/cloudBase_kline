package com.drawing

/**
 * @author James Chen
 * @date 18/4/2023
 */
enum class DrawingType(type: String) {
    TREND("trend"),
    EXTENDED("extended"),
    RAY("ray"),
    HORIZONTAL_LINE("horizontal_line"),
    HORIZONTAL_EXTENDED("horizontal_extended"),
    VERTICAL_EXTENDED("vertical_extended"),
    PRICE_LINE("price_line"),
    PARALLEL_LINE("parallel_line"),
    RECT("rect"),
    TRIPLE_WAVES("triple_waves"),
    PENTA_WAVES("penta_waves"),
    FIBONACCI("fibonacci")
}

// 只需6个点绘制
fun DrawingType.isSixPointType(): Boolean {
    return this == DrawingType.PENTA_WAVES
}

// 只需4个点绘制
fun DrawingType.isFourPointType(): Boolean {
    return this == DrawingType.TRIPLE_WAVES
}

// 只需3个点绘制
fun DrawingType.isThreePointType(): Boolean {
    return this == DrawingType.PARALLEL_LINE
}

// 需要2个点绘制
fun DrawingType.isTwoPointType(): Boolean {
    return when(this) {
        DrawingType.TREND,
        DrawingType.EXTENDED,
        DrawingType.RAY,
        DrawingType.HORIZONTAL_EXTENDED,
        DrawingType.VERTICAL_EXTENDED,
        DrawingType.RECT,
        DrawingType.FIBONACCI-> true
        else -> false
    }
}

// 需要1个点绘制
fun DrawingType.isOnePointType(): Boolean {
    return when(this) {
        DrawingType.HORIZONTAL_LINE,
        DrawingType.PRICE_LINE -> true
        else -> false
    }
}

//=========用来判断绘制的类型==========>>>>>>>>

//<<<<========== 用来判断是否能选中的类型 =========
//判断是否是线
fun DrawingType.isLineType(): Boolean {
    return when(this) {
        DrawingType.TREND,
        DrawingType.EXTENDED,
        DrawingType.RAY,
        DrawingType.HORIZONTAL_LINE,
        DrawingType.HORIZONTAL_EXTENDED,
        DrawingType.VERTICAL_EXTENDED,
        DrawingType.PRICE_LINE, -> true
        else -> false
    }
}

//判断是否是封闭的多边形
fun DrawingType.isPolygonType(): Boolean {
    return when(this) {
        DrawingType.RECT,
        DrawingType.PARALLEL_LINE,
        DrawingType.FIBONACCI -> true
        else -> false
    }
}

//判断是否由多条线段组成的开放图形
fun DrawingType.isMultiLineType(): Boolean {
    return when(this) {
        DrawingType.TRIPLE_WAVES,
        DrawingType.PENTA_WAVES -> true
        else -> false
    }
}

fun DrawingType.needPoint(): Int {
    if (isOnePointType()) {
        return 1
    }
    if (isTwoPointType()) {
        return 2
    }
    if (isThreePointType()) {
        return 3
    }
    if (isFourPointType()) {
        return 4
    }
    if (isSixPointType()) {
        return 6
    }
    return 0
}