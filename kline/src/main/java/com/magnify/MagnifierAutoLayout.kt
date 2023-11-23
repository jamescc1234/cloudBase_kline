package com.magnify

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import binance.stock.library.R
import com.widget.stock.k_line.view.DrawingComponent
import com.widget.stock.k_line.view.KLineChartFrameLayout
import com.widget.stock.k_line.view.Position

/**
 * @author James Chen
 * @date 22/3/2023
 */
class MagnifierAutoLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
): RelativeLayout(context, attributeSet, defStyle){
    companion object {
        private val TAG = MagnifierAutoLayout::class.java.simpleName
    }
    private var mBitmap: Bitmap? = null
    private var mPaintShadow: Paint? = null
    private var mShowTime: Long = 0 //用于判断显示放大镜的时间

    private var mIsShowMagnifier = false //是否显示放大镜

    private var mPath //用于裁剪画布的路径
            : Path? = null
    private var mShowMagnifierX = 0f //显示放大镜的X坐标

    private var mShowMagnifierY = 0f //显示放大镜的Y坐标


    private var mX = 0f
    private var mY = 0f


    private var paint: Paint? = null
    private var scaledBitmap: Bitmap? = null
    private var cutBitmap: Bitmap? = null
    private var blackBitMap: Bitmap? = null
    private var roundBitmap: Bitmap? = null
    private var builder: MagnifierBuilder? = null
    var mainChartFrameLayout: KLineChartFrameLayout? = null


    fun setMagnifierBuilder(magnifierBuilder: MagnifierBuilder?) {
        builder = magnifierBuilder
        init()
    }


    private fun init() {
        //绘制放大镜边缘投影的画笔
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.color = Color.argb(255, 237, 126, 51)
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = builder!!.getStrokeWidth()
        mPaintShadow = Paint(Paint.ANTI_ALIAS_FLAG)
        //设置放大镜边缘的投影
        mPaintShadow!!.setShadowLayer(20f, 6f, 6f, Color.BLACK)
        //绘制Bitmap的画笔
        mPath = Path()
    }


    override fun dispatchDraw(canvas: Canvas) {
        if (mIsShowMagnifier && builder != null) {
            //创建整个布局内容的Bitmap
            Log.d(
                "JamesDebug",
                "mShowManifierX is $mShowMagnifierX, mShowMagnifierY is $mShowMagnifierY"
            )
            val magnifierWidthAndHeight =
                builder!!.getMagnifierRadius() * 2 / builder!!.getScaleRate()
            Log.d("dispatchDraw", "magnifierWidthAndHeight is $magnifierWidthAndHeight")
            //计算出该裁剪的区域(就是使手指所在的点在要裁剪的Bitmap的中央)，并进行边界值处理(开始裁剪的X点不能小于0和大于Bitmap的宽度，并且X点的位置加上要裁剪的宽度不能大于Bitmap的宽度，Y点也是一样)
//            int cutX = Math.max((int) (mShowMagnifierX - builder.getMagnifierRadius() - magnifierWidthAndHeight / 2), 0);
            var cutX =
                Math.max(
                    (mShowMagnifierX - builder!!.getMagnifierRadius() - magnifierWidthAndHeight / 2).toInt(), 0)

            var cutY = Math.min(
                Math.max(
                    (mShowMagnifierY + builder!!.getMagnifierRadius() - magnifierWidthAndHeight / 2).toInt(),
                    0
                ), mBitmap!!.height
            )

            //适配边界值
            val cutWidth = magnifierWidthAndHeight.toInt()
            val cutHeight = magnifierWidthAndHeight.toInt()
            if (cutX + cutWidth > mBitmap!!.width) {
                cutX = mBitmap!!.width - cutWidth
            }

            if (cutY + cutHeight > mBitmap!!.height) {
                cutY = mBitmap!!.height - cutHeight
            }

            Log.d("dispatchDraw", "cutX is $cutX, cutY is $cutY, mx is $mX, my is $mY")


            // make sure mX and mY is in the main view
            if (mainChartFrameLayout?.mainViewOffsetViewBounds != null) {
                val mainRect = mainChartFrameLayout!!.mainViewOffsetViewBounds!!
                if (mY < mainRect.top) {
                    mY = mainRect.top.toFloat()
                }

                if (mY > mainRect.bottom) {
                    mY = mainRect.bottom.toFloat()
                }
            }

            if (cutWidth <= 0 || cutHeight == 0) {
                return
            }

            Log.d("dispatchDraw", "cutWidth is $cutWidth, cutHeight is $cutHeight")
            Bitmap.createBitmap(mBitmap!!, cutX, cutY, cutWidth, cutHeight)?.let { cutBitmap ->
                //将裁剪出来的区域放大
                scaledBitmap = Bitmap.createScaledBitmap(
                    cutBitmap,
                    (cutBitmap.width * builder!!.getScaleRate()).toInt(),
                    (cutBitmap.height * builder!!.getScaleRate()).toInt(), true
                )
                //绘制放大后的Bitmap，由于Bitmap的缩放是从左上点开始的因此还要根据缩放的比例进行相应的偏移展示
//                roundBitmap = BitmapHelper.GetRoundedCornerBitmap(scaledBitmap!!)
                blackBitMap = BitmapHelper.getBlackBackgroundBitmap(scaledBitmap!!,context.resources.getColor(R.color.Color_Bg))
                roundBitmap = BitmapHelper.getRoundedCornerBitmap(blackBitMap!!, Position(mX, mY), Position(cutX.toFloat(), cutY.toFloat()))
                // still need to handle top/down situation
                if (cutX + builder!!.getMagnifierRadius() > canvas.width / 2) {
                    canvas.drawCircle(
                        builder!!.getLeftSpace() + builder!!.getMagnifierRadius(),
                        builder!!.realTopSpace + builder!!.getMagnifierRadius(),
                        builder!!.getMagnifierRadius(),
                        paint!!
                    )
                    canvas.save()
                    //            canvas.drawBitmap(roundBitmap, builder.getLeftSpace() + max, builder.getTopSpace(), null);
                    canvas.drawBitmap(
                        roundBitmap!!,
                        builder!!.getLeftSpace() - mX + builder!!.getMagnifierRadius() + cutX,
                        builder!!.realTopSpace - mY + builder!!.getMagnifierRadius() + cutY,
                        null
                    )
                    canvas.restore()
                } else {
                    canvas.drawCircle(
                        canvas.width - builder!!.getLeftSpace() - builder!!.getMagnifierRadius(),
                        builder!!.realTopSpace + builder!!.getMagnifierRadius(),
                        builder!!.getMagnifierRadius(),
                        paint!!
                    )
                    canvas.save()
                    canvas.drawBitmap(
                        roundBitmap!!,
                        canvas.width - builder!!.getLeftSpace() - cutWidth - mX + builder!!.getMagnifierRadius() + cutX,
                        builder!!.realTopSpace - mY + builder!!.getMagnifierRadius() + cutY,
                        null
                    )
                }
            }
        } else {
            super.dispatchDraw(canvas)
        }
    }


    fun setTouch(event: MotionEvent, viewGroup: ViewGroup, mainRect: Rect) {
//        if (!(event.x >= mainRect.left  && event.x <= mainRect.right && event.y >= mainRect.top + DrawingComponent.pointTolerance && event.y <= mainRect.bottom - DrawingComponent.pointTolerance)) {
//            Log.d(TAG, "event.y is " + event.y)
//            return
//        }

        release()
        BitmapHelper.recycler(mBitmap)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mShowTime = System.currentTimeMillis()
                mShowMagnifierX = event.x + builder!!.getMagnifierRadius()
                mShowMagnifierY = event.y - builder!!.getMagnifierRadius()
                mX = event.x
                mY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                mIsShowMagnifier = true
                Log.d(TAG, "event.x is ${event.x}, event.y is ${event.y}, magnifierRadius is ${builder!!.getMagnifierRadius()}")

                mShowMagnifierX = event.x + builder!!.getMagnifierRadius()
                mShowMagnifierY = if (mainChartFrameLayout == null) {
                    event.y - builder!!.getMagnifierRadius()
                } else {
                    if (event.y > mainChartFrameLayout!!.mainViewOffsetViewBounds.bottom - DrawingComponent.pointTolerance - DrawingComponent.pointRadius) {
                        mainChartFrameLayout!!.mainViewOffsetViewBounds.bottom - DrawingComponent.pointTolerance - DrawingComponent.pointRadius - builder!!.getMagnifierRadius()
                    } else if (event.y < mainChartFrameLayout!!.mainViewOffsetViewBounds.top + DrawingComponent.pointTolerance + DrawingComponent.pointRadius) {
                        mainChartFrameLayout!!.mainViewOffsetViewBounds.top + DrawingComponent.pointTolerance + DrawingComponent.pointRadius - builder!!.getMagnifierRadius()
                    } else {
                        event.y - builder!!.getMagnifierRadius()
                    }
                }

                mX = event.x
                mY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mIsShowMagnifier = false
        }
        if (!BitmapHelper.isNotEmpty(mBitmap)) {
            mBitmap =
                Bitmap.createBitmap(viewGroup.width, viewGroup.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(mBitmap!!)

            Log.d("clipRect", "left: 0, top: $mShowMagnifierY, right: ${viewGroup.width}, bottom: ${mShowMagnifierY + builder!!.getTopSpace()}")
            canvas.clipRect(
                0f,
                mShowMagnifierY,
                viewGroup.width.toFloat(),
                mShowMagnifierY + builder!!.getTopSpace()
            )
            viewGroup.draw(canvas)
        }
        postInvalidate()
    }

    private fun release() {
        BitmapHelper.recycler(cutBitmap, scaledBitmap, roundBitmap, mBitmap)
    }
}