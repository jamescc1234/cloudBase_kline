package com.magnify

import android.graphics.*
import com.widget.stock.k_line.view.Position

/**
 * @author James Chen
 * @date 22/3/2023
 */
object BitmapHelper {

    fun getBlackBackgroundBitmap(bitmap: Bitmap, color: Int = Color.WHITE): Bitmap? {
        return try {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            // todo
            // Need to pass background color from outside
            canvas.drawARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            output
        } catch (e: Exception) {
            bitmap
        }

    }
    /**
     * 获取圆角图片
     * @param bitmap
     * @return
     */
    fun getRoundedCornerBitmap(bitmap: Bitmap, realPosition: Position, cutPosition: Position): Bitmap? {
        return try {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
//            val color = -0xbdbdbe
            val paint = Paint()
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
//            canvas.drawCircle(
//                (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(), (
//                        bitmap.width / 2).toFloat(), paint
//            )
            canvas.drawCircle(
                realPosition.x - cutPosition.x, realPosition.y - cutPosition.y, (
                        bitmap.width / 2).toFloat(), paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            output
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * 释放支援
     * @param bitmap
     */
    fun recycler(vararg bitmap: Bitmap?) {
        if (bitmap != null && bitmap.isNotEmpty()) {
            for (i in bitmap.indices) {
                if (bitmap[i] != null && !bitmap[i]!!.isRecycled) {
                    bitmap[i]!!.recycle()
                }
            }
        }
    }

    fun isNotEmpty(bitmap: Bitmap?): Boolean {
        return bitmap != null && !bitmap.isRecycled
    }
}