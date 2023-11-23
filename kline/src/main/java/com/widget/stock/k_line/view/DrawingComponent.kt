package com.widget.stock.k_line.view

import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import com.drawing.*
import com.utils.TouchArea
import com.widget.stock.k_line.data.KLineDataValid
import java.lang.Double.max
import java.lang.Double.min
import java.lang.Long.max
import java.lang.Long.min
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author James Chen
 * @date 18/4/2023
 */

data class Position(
    val x: Float,
    val y: Float,
    val timeStamp: Long = 0
)

class DrawingComponent (private val frameLayout: KLineChartFrameLayout){
    companion object {
        private val TAG = DrawingComponent::class.java.simpleName

        //点击位置距离线到位置容错距离 (处理不一定要点到直线上才能选中直线)
        private const val distanceTolerance = 5

        //点和点的容差距离
        const val pointTolerance = 30
        const val pointRadius = 10
        const val minDragDistance = 2f
    }

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var maxPrice: Double = 0.0
    private var minPrice: Double = 0.0
    private var yRange: Double = 0.0
    private var yAxisRange: Int = 0
    private var xRange: Long = 0
    private var xAxisRange: Int = 0

    private var lastDownPosition: Position? = null
    private var movingElement = false
    private var movingPoint: DrawingPoint? = null
    var klineDataList: ArrayList<KLineDataValid>? = null
    var unit = 0.1

    fun hasMovingPoint(): Boolean {
        return movingPoint != null
    }

    fun performPointerDown(position: Position) {
        val currentSelected = frameLayout.drawingStatus.currentSelectedDrawingData
        Log.d(TAG, "performPointerDown: $position")
        Log.d(TAG, "currentSelected is $currentSelected")
        if (currentSelected != null) {
            Log.d(TAG, "isNearPoint: ${checkPointNearAnyPoint(currentSelected.points, position)}")
            lastDownPosition = position
        }
    }

    fun performDrag(disX: Double, disY: Double, position: Position): Boolean {
        Log.d(TAG, "performDrag **************")
        if (lastDownPosition != null && frameLayout.drawingStatus.currentSelectedDrawingData != null) {
            val currentSelected = frameLayout.drawingStatus.currentSelectedDrawingData!!
            if (movingElement) {
                Log.d("ElementDrag", "disX: $disX, disY: $disY, position: $position")
                moveElement(
                    frameLayout.drawingStatus.currentSelectedDrawingData,
                    lastDownPosition!!,
                    disX,
                    disY,
                    position
                )
            } else if(movingPoint != null) {
                movePoint(frameLayout.drawingStatus.currentSelectedDrawingData, movingPoint, position)
                Log.d("MovingPoint", "disX: $disX, disY: $disY, position: $position")
            } else if (!frameLayout.drawingStatus.isDrawing() && frameLayout.drawingStatus.currentSelectedDrawingData == null) {
                return false
            } else {
                if (frameLayout.drawingStatus.currentSelectedDrawingData != null) {
                    for (element in frameLayout.drawingStatus.currentSelectedDrawingData!!.points) {
                        if (checkPointNearPoint(Position(element.realX!!.toFloat(), element.realY!!.toFloat() ), position)) {
                            movingPoint = element
                            break
                        }
                    }
                }

                if (movingPoint == null) {
                    val type = currentSelected.type
                    movingElement = if (type!!.isPolygonType()) {
                        currentSelected.realPath?.let {
                            checkInnerPolygon(it, position)
                        } ?: false
                    } else if (type.isMultiLineType()) {
                        checkPointIsNearMultiLine(currentSelected, lastDownPosition!! )
                    } else {
                        if (currentSelected.start != null && currentSelected.end != null) {
                            checkPointIsNearLine(
                                currentSelected.start!!,
                                currentSelected.end!!,
                                lastDownPosition!!
                            )
                        } else {
                            false
                        }
                    }

                    Log.d("DrawingMoving", "movingElement: $movingElement")
                }
            }
        }

        return true
    }

    fun performDragUp() {
        Log.d(TAG, "performDragUp **************")
        Log.d("onTouchEvent", "performDragUp to clearMovingPoint")
        clearMovingStatus()
        checkDrawingComplete()
    }

    fun performTap(position: Position) {
        if (frameLayout.drawingStatus.isDrawing()) {
            checkDrawingComplete()
        } else {
            //非画线中，判断点击的位置是否会选中某个
            checkIsSelected(position)
        }
    }

    fun performDoubleTap(position: Position) {
        // todo
        // need to notify app about which area has been double tapped
        // The position is in main board
        if (position.x >= frameLayout.mainViewOffsetViewBounds.left && position.x <= frameLayout.mainViewOffsetViewBounds.right
            && position.y >= frameLayout.mainViewOffsetViewBounds.top && position.y <= frameLayout.mainViewOffsetViewBounds.bottom) {
            Log.d(TAG, "performDoubleTap: double tap in main view")
            frameLayout.drawingStatus.reportDoubleTap(TouchArea.MAIN)
        } else {
            Log.d(TAG, "performDoubleTap: double tap in sub view")
            frameLayout.drawingStatus.reportDoubleTap(TouchArea.SUB)
        }

    }

    fun deleteSelected() {
        for (element in frameLayout.drawingStatus.drawingDataList) {
            if (element.isSelected) {
                frameLayout.drawingStatus.drawingDataList.remove(element)
                break
            }
        }

        frameLayout.drawingStatus.currentSelectedDrawingData = null
        frameLayout.notifyPointChange()
    }

    fun deleteAll() {
        frameLayout.drawingStatus.drawingDataList.clear()
        frameLayout.drawingStatus.currentSelectedDrawingData = null
        frameLayout.notifyPointChange()
    }

    //检查当前点是否可以选中某个元素
    private fun checkIsSelected(position: Position) {
        val lastHasSelected =
            frameLayout.drawingStatus.currentSelectedDrawingData != null
        var alreadySelected = false
        val sortedData = mutableListOf<DrawingData>()

        frameLayout.drawingStatus.drawingDataList.forEach { element ->
            if (element.type?.isLineType() == true) {
                sortedData.add(0, element)
            } else {
                sortedData.add(element)
            }
        }

        for (data in sortedData) {
            if (!alreadySelected) {
                //如果是线段类型，判断是否点击在某个点或者边上
                val lineSelected =
                    if (data.start != null && data.end != null) {
                        (data.type?.isLineType() == true &&
                                (checkPointIsNearLine(data.start!!, data.end!!, position) ||
                                        checkPointNearAnyPoint(data.points, position)))
                    } else {
                        false
                    }

                //如果是多条线段类型
                val multiLineSelected = (data.type?.isMultiLineType() == true &&
                        (checkPointIsNearMultiLine(data, position) ||
                                checkPointNearAnyPoint(data.points, position)))

                //如果是多边形类型，判断是否点击在图形内部或者点上
                val polygonSelected = (data.type?.isPolygonType() == true &&
                        (checkInnerPolygon(data.realPath, position) ||
                                checkPointNearAnyPoint(data.points, position)))

                if (lineSelected || multiLineSelected || polygonSelected) {
                    frameLayout.drawingStatus.selectDrawingData(data)
                    alreadySelected = true
                } else {
                    data.isSelected = false
                }
            } else {
                data.isSelected = false
            }
        }

        if (!alreadySelected && lastHasSelected) {
            Log.d("onTouchEvent", "checkIsSelected to clearSelected")
            clearMovingStatus()
            frameLayout.drawingStatus.clearSelectStatus()
        }

        frameLayout.notifyPointChange()
    }

    private fun checkInnerPolygon(path: Path?, position: Position): Boolean {
        val rectF = RectF()
        path?.computeBounds(rectF, true)
        return rectF.contains(position.x, position.y)
    }

    private fun checkPointIsNearMultiLine(data: DrawingData, point: Position): Boolean {
        var near = false
        for (index in data.points.indices) {
            if (index != 0) {
                val prePoint = data.points[index - 1]
                val currentPoint = data.points[index]
                near = checkPointIsNearLine(Position(prePoint.realX!!.toFloat(), prePoint.realY!!.toFloat()),
                    Position(currentPoint.realX!!.toFloat(), currentPoint.realY!!.toFloat()), point)

                if (near) {
                    return true
                }
            }
        }

        return near
    }

    private fun checkPointIsNearLine(start: Position, end: Position, point: Position): Boolean {
        val lineDistance = sqrt((start.x - end.x).toDouble().pow(2.0) + (start.y - end.y).toDouble()
            .pow(2.0)
        )

        val pointDistance = sqrt((start.x - point.x).toDouble().pow(2.0) + (start.y - point.y).toDouble()
            .pow(2.0)
        ) + sqrt((end.x - point.x).toDouble().pow(2.0) + (end.y - point.y).toDouble().pow(2.0))

        return pointDistance > (lineDistance - distanceTolerance) && pointDistance < (lineDistance + distanceTolerance)
    }

    fun isDragMode(position: Position): Boolean {
        lastDownPosition?.let {
            val disX = position.x - it.x
            val disY = position.y - it.y

            return disX.absoluteValue >= minDragDistance || disY.absoluteValue >= minDragDistance
        }

        return false
    }

    private fun movePoint(data: DrawingData?, point: DrawingPoint?, newPos: Position) {
        if (data?.isLocked == true) {
            return
        }

        val mainBoardArea = frameLayout.mainViewOffsetViewBounds

        Log.d(
            "KlineChartMainView", "onDraw: top is " + mainBoardArea.top +
                    ", bottom is " + mainBoardArea.bottom +
                    ", left is " + mainBoardArea.left +
                    ", right is " + mainBoardArea.right
        )

        Log.d("KlineChartMainView", "onDraw: newPos.x is " + newPos.x + ", newPos.y is " + newPos.y)

        var newPosition = newPos
        if (newPos.y > mainBoardArea.bottom - pointTolerance - pointRadius) {
            newPosition = Position(newPos.x, mainBoardArea.bottom.toFloat() - pointTolerance - pointRadius)
        } else if (newPos.y < mainBoardArea.top + pointTolerance + pointRadius) {
            newPosition = Position(newPos.x, mainBoardArea.top.toFloat() + pointTolerance + pointRadius)
        }

        val timePricePoint = frameLayout.getRealPosition(newPosition)
        frameLayout.drawingStatus.movingPoint = newPosition
        point?.timeStamp = timePricePoint.timeStamp
        point?.price = timePricePoint.price.toDouble()

        if (data?.type == DrawingType.HORIZONTAL_EXTENDED) {
            for (element in data.points) {
                if (element != point) {
                    element.price = timePricePoint.price.toDouble()
                }
            }
        }

        if (data?.type == DrawingType.VERTICAL_EXTENDED) {
            for (element in data.points) {
                if (element != point) {
                    element.timeStamp = timePricePoint.timeStamp
                }
            }
        }

        //如果是拖动点平行线的最后一个点，需要不超过第一条线的边缘
        if (data?.type == DrawingType.PARALLEL_LINE) {
            if (data == null) {
                return
            }

            val last = data.points.last()
            val start = data.points.first()
            val end = data.points[1]

            if (last.timeStamp > max(start.timeStamp, end.timeStamp)) {
                last.timeStamp = max(start.timeStamp, end.timeStamp)
            }

            if (last.timeStamp < min(start.timeStamp, end.timeStamp)) {
                last.timeStamp = min(start.timeStamp, end.timeStamp)
            }
        }

        frameLayout.notifyPointChange()
    }

    private fun moveElement(data: DrawingData?, startPosition: Position, distanceX: Double, distanceY: Double, newPosition: Position) {
        if (data == null) {
            return
        }

        if (data.isLocked) {
            return
        }

        val mainBoardArea = frameLayout.mainViewOffsetViewBounds
        var latestPosition = newPosition
        var disY = distanceY
        if (newPosition.y > mainBoardArea.bottom) {
            latestPosition = Position(newPosition.y, mainBoardArea.bottom.toFloat())
            disY = 0.0
        }

        if (distanceX == 0.0 && disY == 0.0) {
            return
        }
        Log.d(TAG, "distanceX: $distanceX, disY: $disY")

        for (element in data.points) {
            element.timeStamp = element.timeStamp + (distanceX * getTimeRange() / mainBoardArea.width()).toLong()
            element.price = element.price - (disY * getPriceRange() / mainBoardArea.height())
        }

        frameLayout.notifyPointChange()
    }

    private fun getPriceRange(): Double {
        return maxPrice - minPrice
    }

    private fun checkPointNearAnyPoint(points: List<DrawingPoint>, position: Position): Boolean{
        for (point in points) {
            if (checkPointNearPoint(Position(point.realX!!.toFloat(), point.realY!!.toFloat()), position)) {
                return true
            }
        }

        return false
    }

    private fun checkPointNearPoint(pointA: Position, pointB: Position): Boolean {
       return (pointA.x > pointB.x - pointTolerance) &&
                (pointA.x < pointB.x + pointTolerance) &&
                (pointA.y > pointB.y - pointTolerance) &&
                (pointA.y < pointB.y + pointTolerance)
    }

    // When user enables drawing mode and drawing type is not null
    fun handleDrawingTouch(event: MotionEvent) {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val position = Position(event.x, event.y)
                Log.d(TAG, "touch down: $position")

                if (frameLayout.drawingStatus.currentSelectedDrawingData != null) {
                    lastDownPosition = position
                }

                if (frameLayout.drawingStatus.isDrawing()) {
                    if (position.y > frameLayout.mainViewOffsetViewBounds.bottom || position.y < frameLayout.mainViewOffsetViewBounds.top)
                        Log.d(TAG, "touch out of main board")
                    else {
                        Log.d(TAG, "touch in main board")
                        generatePoint(position)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (frameLayout.drawingStatus.isDrawing()) {
                    dragDrawPoint(Position(event.x, event.y))
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.d("onTouchEvent", "touch up for drawing touch")
                clearMovingStatus()
                checkDrawingComplete()
            }
        }
    }

    private fun checkDrawingComplete() {
        if (frameLayout.drawingStatus.isDrawing() &&
            frameLayout.drawingStatus.drawingType != null &&
            frameLayout.drawingStatus.currentDrawingData != null) {
            val currentType = frameLayout.drawingStatus.drawingType!!
            val drawingData = frameLayout.drawingStatus.currentDrawingData!!
            if ((currentType.isOnePointType() && drawingData.points.size == 1) ||
                (currentType.isTwoPointType() && drawingData.points.size == 2) ||
                (currentType.isThreePointType() && drawingData.points.size == 3) ||
                (currentType.isFourPointType() && drawingData.points.size == 4) ||
                (currentType.isSixPointType() && drawingData.points.size == 6)) {
                frameLayout.drawingStatus.drawingDataList.add(drawingData)
                frameLayout.drawingStatus.completeDrawing(drawingData)
                frameLayout.drawingStatus.saveToCache(frameLayout.symbol, frameLayout.flagTime)
                if (frameLayout.drawingStatus.continueDrawing) {
                    frameLayout.drawingStatus.startDrawing(drawingData.type!!);
                }
            } else {
                frameLayout.drawingStatus.continueDrawing(drawingData);
            }

            frameLayout.notifyPointChange()
        }
    }

    private fun clearMovingStatus() {
        if ((movingElement || movingPoint != null)&&
                frameLayout.symbol != null) {
            frameLayout.drawingStatus.saveToCache(frameLayout.symbol, frameLayout.flagTime)
        }
        lastDownPosition = null
        movingElement = false
        movingPoint = null

        frameLayout.drawingStatus.movingPoint = null

        // need to set value for image
        frameLayout.notifyPointChange()
    }

    //生成真正的价格和时间做为一个点进行存储
    //@param replaceLast 替换点位数据中的最后一个，而不是新建一个点
    private fun generatePoint(position: Position, replaceLast: Boolean = false) {
        // 首先需要将x,y position转换成真正的价格和时间 Position
        val timePricePoint = frameLayout.getRealPosition(position)
        var time = timePricePoint?.timeStamp
        val price = timePricePoint?.price

        val currentType = frameLayout.drawingStatus.drawingType
        if (currentType == null) {
            Log.d(TAG, "currentType is null")
            return
        }

        var drawingData = frameLayout.drawingStatus.currentDrawingData
        if (drawingData == null) {
            drawingData = DrawingData()
            drawingData.updateConfigFromJson(frameLayout.drawingStatus.defaultConfig.toJson())
        }

        drawingData.type = currentType

        // handle some edge cases for horizontal extended and vertical extended
        if ((currentType == DrawingType.HORIZONTAL_EXTENDED && drawingData.points.size == 2) ||
            (currentType == DrawingType.VERTICAL_EXTENDED && drawingData.points.size == 2)) {
            return
        }

        if (currentType == DrawingType.HORIZONTAL_EXTENDED &&
                drawingData.points.size == 1) {
            //horizontal_extended 横行扩展类型需要特殊处理一下，用户点击第二个点的位置价格应该始终和第一个点一致
            val previousPoint = drawingData.points[0]
            if (drawingData.points.size > 0 && replaceLast) {
                drawingData.points.removeAt(drawingData.points.size - 1)
                drawingData.points.add(DrawingPoint(time!!, previousPoint.price))
            } else {
                drawingData.points.add(DrawingPoint(time!!, previousPoint.price))
            }
        } else if (currentType == DrawingType.VERTICAL_EXTENDED &&
                drawingData.points.size == 1) {
            //vertical_extended 纵向扩展类型需要特殊处理一下，用户点击第二个点的位置时间应该始终和第一个点一致
            val previousPoint = drawingData.points[0]

            if (drawingData.points.size > 0 && replaceLast) {
                drawingData.points.removeLast();
                drawingData.points.add(DrawingPoint(previousPoint.timeStamp, price!!.toDouble()))
            } else {
                drawingData.points.add(DrawingPoint(previousPoint.timeStamp, price!!.toDouble()))
            }
        } else if (currentType == DrawingType.PARALLEL_LINE &&
            drawingData.points.size == 2) {
            //如果是平行线，不能超过第一条线的x点
            val start = drawingData.points[0]
            val end = drawingData.points[1]
            if (time!! > max(start.timeStamp, end.timeStamp)) {
                time = max(start.timeStamp, end.timeStamp)
            }
            if (time < min(start.timeStamp, end.timeStamp)) {
                time = min(start.timeStamp, end.timeStamp)
            }

            if (drawingData.points.size > 0 && replaceLast) {
                drawingData.points.removeLast();
                drawingData.points.add(DrawingPoint(time, price!!.toDouble()))
            } else {
                drawingData.points.add(DrawingPoint(time, price!!.toDouble()))
            }
        } else {
            if (drawingData.points.size > 0 && replaceLast) {
                drawingData.points.removeLast();
                drawingData.points.add(DrawingPoint(time!!, price!!.toDouble()))
            } else {
                drawingData.points.add(DrawingPoint(time!!, price!!.toDouble()))
            }
        }

        frameLayout.drawingStatus.continueDrawing(drawingData, false)
        frameLayout.drawingStatus.movingPoint = position

        frameLayout.notifyPointChange()
    }

    private fun dragDrawPoint(position: Position) {
        val bottom = frameLayout.mainViewOffsetViewBounds.bottom
        val top = frameLayout.mainViewOffsetViewBounds.top
        var dy = position.y

        if (dy > bottom - pointTolerance - pointRadius) {
            dy = (bottom - pointTolerance - pointRadius).toFloat()
        }

        if (dy < top + pointTolerance + pointRadius) {
            dy = (top + pointTolerance + pointRadius).toFloat()
        }

        Log.d("dragDrawPoint", "dx: ${position.x}, dy: $dy")
        generatePoint(Position(position.x, dy), true)
        // need to send movingPoint
        frameLayout.drawingStatus.movingPoint = position
    }

    fun getTimeRange(): Double {
        if (klineDataList != null && klineDataList!!.isNotEmpty()) {
            if (klineDataList!!.size > 2) {
                val unitTime: Double =
                    (klineDataList!![1].time - klineDataList!![0].time).toDouble()
                return frameLayout.mainViewOffsetViewBounds.width() / unit * unitTime
            }
        }
        return 0.0
    }

    fun getTimeUnit(): Double {
        if (klineDataList != null && klineDataList!!.isNotEmpty()) {
            if (klineDataList!!.size > 2) {
                return (klineDataList!![1].time - klineDataList!![0].time).toDouble()
            }
        }
        return 0.0
    }

    private fun setRenderRange(klineDataList: ArrayList<KLineDataValid>, timeRange: Double) {
        val location = frameLayout.location
        startTime = if (location >= 0) {
            klineDataList[location].time
        } else {
            (klineDataList[0].time + location * getTimeUnit()).toLong()
        }
        endTime = startTime + timeRange.toLong()
    }

    // 是否在当前可见范围
    private fun timeInScreen(point: DrawingPoint): Boolean {
        return point.timeStamp in startTime..endTime
    }

    private fun priceInScreen(point: DrawingPoint): Boolean {
        return point.price in minPrice..maxPrice
    }

    private fun mapToMainBoardX(time: Double): Double {
        return (time - startTime) / xRange * xAxisRange
    }

    private fun mapToMainBoardY(price: Double): Double {
        return (maxPrice - price) / yRange * yAxisRange + frameLayout.mainViewOffsetViewBounds.top
    }

    private fun updateRealAxis(points: List<DrawingPoint>) {
        points.forEach {
            it.realX = mapToMainBoardX(it.timeStamp.toDouble())
            it.realY = mapToMainBoardY(it.price)
        }
    }

    fun drawCustomPath(canvas: Canvas,
                       klineDataList: ArrayList<KLineDataValid>,
                       timeRange: Double,
                       timeUnit: Double,
                        maxData: Double,
                        minData: Double) {
        setRenderRange(klineDataList, timeRange)

        if (klineDataList.size > 2) {
            // todo
            // may need to care about offset
        }

        this.maxPrice = maxData
        this.minPrice = minData

        yRange = maxData - minData
        yAxisRange = frameLayout.mainViewOffsetViewBounds.height()

        xRange = timeRange.toLong()
        xAxisRange = frameLayout.mainViewOffsetViewBounds.width()

        val currentData = frameLayout.drawingStatus.currentDrawingData
        if (currentData != null && currentData.points.size > 0) {
            updateRealAxis(currentData.points)

            currentData.points.forEach { element ->
                // draw circle here
                drawCirclePoint(canvas, element, currentData.color)
            }

            if (currentData.points.size >= 2) {
                lineToEveryPoint(canvas, currentData,frameLayout.mainViewOffsetViewBounds,
                    currentData.color, currentData.width, currentData.dashGap.toFloat(), currentData.dashWidth)
            }
        }

        if (frameLayout.drawingStatus.hideAllDrawing) {
            return
        }

//        frameLayout.drawingStatus.drawingDataList.forEach { element ->
//            if (element.type!!.isOnePointType() && element.points.isEmpty()) {
//
//            }
//        }
        for (element in frameLayout.drawingStatus.drawingDataList) {
            if (element.type!!.isOnePointType() && element.points.isEmpty()) {
                continue
            }
            if (element.type!!.isTwoPointType() && element.points.size < 2) {
                continue
            }
            //更新该点在屏幕中的真实坐标
            updateRealAxis(element.points)

            if (element.isSelected && frameLayout.drawingStatus.getDrawingMode()) {
                for (point in element.points) {
                    drawCirclePoint(canvas, point, element.color)
                }
            }

            when(element.type) {
                DrawingType.TREND -> {
                    line(canvas, Position(element.points[0].realX!!.toFloat(), element.points[0].realY!!.toFloat()),
                        Position(element.points[1].realX!!.toFloat(),
                            element.points[1].realY!!.toFloat()), element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                    element.start = Position(element.points[0].realX!!.toFloat(), element.points[0].realY!!.toFloat())
                    element.end = Position(element.points[1].realX!!.toFloat(), element.points[1].realY!!.toFloat())
                }

                DrawingType.EXTENDED -> {
                    val startPosition = element.points[0]
                    val endPosition = element.points[1]

                    val slope =
                        (startPosition.realY!! - endPosition.realY!!) / (startPosition.realX!! - endPosition.realX!!)
                    val b = startPosition.realY!! - (slope * startPosition.realX!!)

                    val screenLeftY = (slope * frameLayout.mainViewOffsetViewBounds.left) + b
                    val screenRightY = (slope * frameLayout.mainViewOffsetViewBounds.right) + b
                    line(canvas, Position(frameLayout.mainViewOffsetViewBounds.left.toFloat(),
                        screenLeftY.toFloat()), Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(),
                        screenRightY.toFloat()), element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                    element.start = Position(frameLayout.mainViewOffsetViewBounds.left.toFloat(), screenLeftY.toFloat())
                    element.end = Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(), screenRightY.toFloat())
                }

                DrawingType.RAY -> {
                    val startPosition = element.points[0]
                    val endPosition = element.points[1]

                    val slope =
                        (startPosition.realY!! - endPosition.realY!!) / (startPosition.realX!! - endPosition.realX!!)
                    val b = startPosition.realY!! - (slope * startPosition.realX!!)

                    val screenLeftY = (slope * frameLayout.mainViewOffsetViewBounds.left) + b
                    val screenRightY = (slope * frameLayout.mainViewOffsetViewBounds.right) + b
                    element.start = Position(startPosition.realX!!.toFloat(), startPosition.realY!!.toFloat())
                    element.end = if (endPosition.realX!! > startPosition.realX!!) {
                        Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(), screenRightY.toFloat())
                    } else {
                        Position(frameLayout.mainViewOffsetViewBounds.left.toFloat(), screenLeftY.toFloat())
                    }

                    line(canvas, element.start!!, element.end!!, element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                }

                DrawingType.HORIZONTAL_LINE -> {
                    val point = element.points[0]
                    if (!priceInScreen(point)) {
                        continue
                    }

                    element.start = Position(frameLayout.mainViewOffsetViewBounds.left.toFloat(), point.realY!!.toFloat())
                    element.end = Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(), point.realY!!.toFloat())

                    line(canvas, element.start!!, element.end!!, element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                }

                DrawingType.HORIZONTAL_EXTENDED -> {
                    val startPosition = element.points[0]
                    val endPosition = element.points[1]

                    line(canvas, Position(startPosition.realX!!.toFloat(), startPosition.realY!!.toFloat()),
                        Position(endPosition.realX!!.toFloat(), endPosition.realY!!.toFloat()),
                        element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                    element.start = Position(startPosition.realX!!.toFloat(), startPosition.realY!!.toFloat())
                    element.end = Position(endPosition.realX!!.toFloat(), endPosition.realY!!.toFloat())
                }

                DrawingType.VERTICAL_EXTENDED -> {
                    if (element.points.size < 2) {
                        continue
                    }
                    val start = element.points[0]
                    if (!timeInScreen(start)) {
                        continue
                    }
                    val end = element.points[1]
                    line(canvas, Position(start.realX!!.toFloat(), start.realY!!.toFloat()),
                        Position(end.realX!!.toFloat(), end.realY!!.toFloat()),
                        element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                    element.start = Position(start.realX!!.toFloat(), frameLayout.mainViewOffsetViewBounds.top.toFloat())
                    element.end = Position(end.realX!!.toFloat(), frameLayout.mainViewOffsetViewBounds.bottom.toFloat())
                }

                DrawingType.PRICE_LINE -> {
                    val point = element.points[0]
                    if (!priceInScreen(point) || point.timeStamp > endTime) {
                        continue
                    }

                    line(canvas, Position(point.realX!!.toFloat(), point.realY!!.toFloat()),
                        Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(), point.realY!!.toFloat()),
                        element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
                    val textSize = 24
                    val textPadding = 2

                    // 这就是price line价格精度问题的源泉
                    text(canvas, String.format("%.${frameLayout.decPlace}f", point.price.toFloat()), Position(point.realX!!.toFloat(), point.realY!!.toFloat()), element.color, textSize, textPadding)
                    element.start = Position(point.realX!!.toFloat(), point.realY!!.toFloat())
                    element.end = Position(frameLayout.mainViewOffsetViewBounds.right.toFloat(), point.realY!!.toFloat())
                }

                DrawingType.PARALLEL_LINE -> {
                    if (element.points.size < 3) {
                        return
                    }

                    val start = element.points[0]
                    val end = element.points[1]
                    val parallel = element.points[2]

                    // 绘制第一条线 上轨
                    line(canvas, Position(start.realX!!.toFloat(), start.realY!!.toFloat()),
                        Position(end.realX!!.toFloat(), end.realY!!.toFloat()),
                        element.color, element.width, element.dashGap.toFloat(), element.dashWidth)

                    // 找到过点3的平行线
                    val slope = (start.realY!! - end.realY!!) / (start.realX!! - end.realX!!)
                    val b = parallel.realY!! - (slope * parallel.realX!!)

                    // 平行点1
                    val y1 = slope * start.realX!! + b
                    // 平行点2
                    val y2 = slope * end.realX!! + b

                    val pEnd = Position(start.realX!!.toFloat(), y1.toFloat())
                    val pStart = Position(end.realX!!.toFloat(), y2.toFloat())

                    // 绘制第二条线 下轨
                    line(canvas, pStart, pEnd, element.color, element.width, element.dashGap.toFloat(), element.dashWidth)

                    // 绘制中间虚线
                    val midStart = Position((start.realX!!.toFloat() + pEnd.x) / 2, (start.realY!!.toFloat() + pEnd.y) / 2)
                    val midEnd = Position((end.realX!!.toFloat() + pStart.x) / 2, (end.realY!!.toFloat() + pStart.y) / 2)
                    line(canvas, midStart, midEnd, element.color, 1f, 5f, element.dashWidth)

                    // 绘制填充半透明背景色
                    val path = Path()
                    path.lineTo(start.realX!!.toFloat(), start.realY!!.toFloat())
                    path.lineTo(end.realX!!.toFloat(), end.realY!!.toFloat())
                    path.lineTo(pStart.x, pStart.y)
                    path.lineTo(pEnd.x, pEnd.y)
                    path.lineTo(start.realX!!.toFloat(), start.realY!!.toFloat())
                    path.close()
                    path(canvas, path, element.fillColor, true, element.width, element.dashGap.toFloat(), element.dashWidth)
                    element.realPath = path
                }

                DrawingType.RECT -> {
                    if (element.points.size < 2) {
                        return
                    }
                    val p1 = element.points[0]
                    val p2 = element.points[1]
                    val left = min(p1.realX!!, p2.realX!!)
                    val top = max(p1.realY!!, p2.realY!!)
                    val right = max(p1.realX!!, p2.realX!!)
                    val bottom = min(p1.realY!!, p2.realY!!)

                    val path = Path()
                    path.moveTo(left.toFloat(), bottom.toFloat())
                    path.lineTo(left.toFloat(), top.toFloat())
                    path.lineTo(right.toFloat(), top.toFloat())
                    path.lineTo(right.toFloat(), bottom.toFloat())
                    path.lineTo(left.toFloat(), bottom.toFloat())
                    path.close()
                    element.realPath = path
                    path(canvas, path, element.color, false, element.width, element.dashGap.toFloat(), element.dashWidth)
                    rect(canvas, left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), element.fillColor, true)
                }

                DrawingType.TRIPLE_WAVES, DrawingType.PENTA_WAVES -> {
                    lineToEveryPoint(canvas, element, frameLayout.mainViewOffsetViewBounds,
                        element.color, element.width,
                        element.dashGap.toFloat(), element.dashWidth)
                }

                DrawingType.FIBONACCI -> {
                    if (element.points.size < 2) {
                        return
                    }
                    drawFibonacci(canvas, element, element.fillColor)
                }
            }
        }
    }

    private fun drawFibonacci(canvas: Canvas,  element: DrawingData, filledColor: Int) {
        val start = element.points[0]
        val end = element.points[1]
        val startX = min(start.realX!!, end.realX!!)
        val endX = max(start.realX!!, end.realX!!)
        val startY = min(start.realY!!, end.realY!!)
        val endY = max(start.realY!!, end.realY!!)

        val ySpace = endY - startY
        val yPriceSpace = (start.price - end.price).absoluteValue

        val fiboParam = arrayOf(0, 0.236, 0.382, 0.5, 0.618, 0.786, 1)

        for (index in fiboParam.indices) {
            val param: Double = fiboParam[index].toDouble()
            line(canvas, Position(startX.toFloat(), (startY + ySpace * param).toFloat()),
                Position(endX.toFloat(), (startY + ySpace * param).toFloat()),
                element.color, element.width, element.dashGap.toFloat(), element.dashWidth)
            val price = max(start.price, end.price) - yPriceSpace * param
            val priceString = String.format("%.${frameLayout.decPlace}f", price)
            val text = if (start.price > end.price) {
                "${fiboParam[fiboParam.size - index - 1]}($priceString)"
            } else {
                "$param($priceString)"
            }

            val paint = Paint()
            paint.color = filledColor
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
            paint.textSize = 20f
            paint.textAlign = Paint.Align.LEFT

            val textWidth = getTextWidth(paint, text) + 6
            val textHeight = getTextHeight(paint, text)

            text(canvas, text, Position((startX - textWidth).toFloat(),
                (startY + ySpace * param - textHeight / 2 - element.width / 2).toFloat()), element.color, 20, 6)

            val path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())
            path.lineTo(endX.toFloat(), startY.toFloat())
            path.lineTo(endX.toFloat(), endY.toFloat())
            path.lineTo(startX.toFloat(), endY.toFloat())
            path.lineTo(startX.toFloat(), startY.toFloat())
            path.close()
            element.realPath = path
            rect(canvas, startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), element.fillColor, true)
        }
    }

    private fun getTextWidth(paint: Paint, text: String): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.width()
    }

    private fun getTextHeight(paint: Paint, text: String): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.height()
    }

    private fun text(canvas: Canvas, text: String, position: Position, color: Int, textSize: Int, textPadding: Int) {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.textSize = textSize.toFloat()
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText(text, position.x, position.y - textSize - textPadding, paint)
    }

    private fun rect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, color: Int, fill: Boolean) {
        val paint = Paint()
        if (fill) {
            paint.style = Paint.Style.FILL
        } else {
            paint.style = Paint.Style.STROKE
        }
        paint.color = color
        paint.isAntiAlias = true

        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun path(canvas: Canvas, path: Path, color: Int, fill: Boolean, width: Float, dashGap: Float, dashWidth: Float) {
        val paint = Paint()

        if (fill) {
            paint.style = Paint.Style.FILL
        } else {
            paint.style = Paint.Style.STROKE
        }
        paint.strokeWidth = width
        paint.color = color
        paint.isAntiAlias = true

        if (dashGap > 0) {
            paint.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        }

        canvas.drawPath(path, paint)
    }

    private fun line(canvas: Canvas, point1: Position, point2: Position, color: Int, width: Float, dashGap: Float, dashWidth: Float) {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.strokeWidth = width

        if (dashGap > 0) {
            paint.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        }

        canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint)
    }

    private fun lineToEveryPoint(canvas: Canvas, drawingData: DrawingData, mainBoardArea: Rect, color: Int, width: Float, dashGap: Float, dashWidth: Float) {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.strokeWidth = width

        if (dashGap > 0) {
            paint.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        }

        val path = Path()
        path.moveTo(drawingData.points[0].realX!!.toFloat(), drawingData.points[0].realY!!.toFloat())
        for (i in 1 until drawingData.points.size) {
            path.lineTo(drawingData.points[i].realX!!.toFloat(), drawingData.points[i].realY!!.toFloat())
        }
        canvas.drawPath(path, paint)
    }

    private fun drawCirclePoint(canvas: Canvas, point: DrawingPoint, color: Int) {
        val paint = Paint()
//        paint.color = Color.parseColor(color)
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawCircle(point.realX!!.toFloat(), point.realY!!.toFloat(), pointRadius.toFloat(), paint)
    }
}