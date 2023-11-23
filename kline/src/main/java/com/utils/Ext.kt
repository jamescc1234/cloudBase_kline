package com.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.annotation.StringRes

/**
 * @author James Chen
 * @date 17/3/2023
 */

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Context.getStringRes(@StringRes resId: Int): String {
    return this.getString(resId)
}

fun makeVibrate(context: Context, millisecond:Long) {
    val vibrator =  context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
    if (android.os.Build.VERSION.SDK_INT >= 26){
        vibrator.vibrate(VibrationEffect.createOneShot(millisecond, 150))
    }else{
        vibrator.vibrate(millisecond)
    }
}