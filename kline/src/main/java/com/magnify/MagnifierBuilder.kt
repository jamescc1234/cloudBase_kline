package com.magnify

import android.content.Context
import com.widget.stock.utils.Utils

/**
 * @author James Chen
 * @date 22/3/2023
 */
class MagnifierBuilder(context: Context?) {
    private var utils = Utils.getInstance()
    private var mMagnifierRadius //放大镜的半径
            = 0f
    private var mScaleRate //放大比例
            = 0f

    private var strokeWidth //左上角放大镜边大小
            = 0f
    private var leftSpace //放大镜距离左边距离
            = 0f
    private var topSpace
            = 0f

    var realTopSpace = 0f //放大镜距离上方距离
    private var shouldAutoMoveMagnifier //是否支持触摸到放大镜范围后自动移动边框
            = false


    fun getMagnifierRadius(): Float {
        return mMagnifierRadius
    }

    fun getScaleRate(): Float {
        return mScaleRate
    }

    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun getLeftSpace(): Float {
        return leftSpace
    }

    fun getTopSpace(): Float {
        return topSpace
    }

    fun getShouldAutoMoveMagnifier(): Boolean {
        return shouldAutoMoveMagnifier
    }

    init {
        widthMagnifierRadius(utils.dp2px(context, 50f))
        widthMagnifierScaleRate(1.3f)
        widthMagnifierStrokeWidth(utils.dp2px(context, 5f))
        widthMagnifierLeftSpace(utils.dp2px(context, 10f))
        widthMagnifierTopSpace(utils.dp2px(context, 100f))
        realTopSpace = utils.dp2px(context, 10f).toFloat()
        widthMagnifierShouldAutoMoveMagnifier(false)
    }

    /**
     * 设置是否支持手指触摸到放大镜范围自动移动到右边
     *
     * @param b
     */
    fun widthMagnifierShouldAutoMoveMagnifier(b: Boolean): MagnifierBuilder? {
        shouldAutoMoveMagnifier = b
        return this
    }

    /**
     * 设置距离顶部距离
     *
     * @param topSpace
     */
    fun widthMagnifierTopSpace(topSpace: Int): MagnifierBuilder? {
        this.topSpace = topSpace.toFloat()
        return this
    }

    /**
     * 设置距离左边距离
     *
     * @param leftSpace
     */
    fun widthMagnifierLeftSpace(leftSpace: Int): MagnifierBuilder? {
        this.leftSpace = leftSpace.toFloat()
        return this
    }


    /**
     * 设置放大镜边框粗细
     *
     * @param strokeWidth
     * @return
     */
    fun widthMagnifierStrokeWidth(strokeWidth: Int): MagnifierBuilder? {
        this.strokeWidth = strokeWidth.toFloat()
        return this
    }


    /**
     * 设置放大镜图像缩放比例
     *
     * @param rate
     */
    fun widthMagnifierScaleRate(rate: Float): MagnifierBuilder? {
        mScaleRate = rate
        return this
    }


    /**
     * 设置放大镜半径
     *
     * @param radius
     */
    fun widthMagnifierRadius(radius: Int): MagnifierBuilder? {
        mMagnifierRadius = radius.toFloat()
        return this
    }
}