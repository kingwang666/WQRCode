package com.wang.wqrcode

import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.Display
import android.view.WindowManager

import java.lang.reflect.Field


/**
 * Created on 2015/3/9.
 * Author: wang
 */
object SizeUtil {

    fun dp2px(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale
    }

    fun dp2pxOffset(context: Context, dpValue: Float): Int {
        return dp2px(context, dpValue).toInt()
    }

    fun dp2pxSize(context: Context, dpValue: Float): Int {
        val f = dp2px(context, dpValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (dpValue == 0f) return 0
        return if (dpValue > 0) 1 else -1
    }

    fun px2dp(context: Context, pxValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return pxValue / scale
    }

    fun px2dpOffset(context: Context, pxValue: Float): Int {
        return px2dp(context, pxValue).toInt()
    }

    fun px2dpSize(context: Context, pxValue: Float): Int {
        val f = px2dp(context, pxValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (pxValue == 0f) return 0
        return if (pxValue > 0) 1 else -1
    }

    fun sp2px(context: Context, spValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return spValue * fontScale
    }

    fun sp2pxOffset(context: Context, spValue: Float): Int {
        return sp2px(context, spValue).toInt()
    }

    fun sp2pxSize(context: Context, spValue: Float): Int {
        val f = sp2px(context, spValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (spValue == 0f) return 0
        return if (spValue > 0) 1 else -1
    }

    fun px2sp(context: Context, pxValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return pxValue / fontScale
    }

    fun px2spOffset(context: Context, pxValue: Float): Int {
        return px2sp(context, pxValue).toInt()
    }

    fun px2spSize(context: Context, pxValue: Float): Int {
        val f = px2sp(context, pxValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (pxValue == 0f) return 0
        return if (pxValue > 0) 1 else -1
    }

    fun mm2px(context: Context, mmValue: Float): Float {
        val xdpi = context.resources.displayMetrics.xdpi
        return mmValue * xdpi * (1.0f / 25.4f)
    }

    fun mm2pxOffset(context: Context, mmValue: Float): Int {
        return mm2px(context, mmValue).toInt()
    }

    fun mm2pxSize(context: Context, mmValue: Float): Int {
        val f = mm2px(context, mmValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (mmValue == 0f) return 0
        return if (mmValue > 0) 1 else -1
    }

    fun px2mm(context: Context, pxValue: Float): Float {
        val xdpi = context.resources.displayMetrics.xdpi
        return 25.4f * pxValue / xdpi
    }

    fun px2mmOffset(context: Context, pxValue: Float): Int {
        return px2mm(context, pxValue).toInt()
    }

    fun px2mmSize(context: Context, pxValue: Float): Int {
        val f = px2mm(context, pxValue)
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (pxValue == 0f) return 0
        return if (pxValue > 0) 1 else -1
    }

    fun getStatusBarHeight(context: Context): Int {
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val obj = c.newInstance()
            val field = c.getField("status_bar_height")
            val x = Integer.parseInt(field.get(obj).toString())
            return context.resources.getDimensionPixelSize(x)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    fun getActionBarHeight(context: Context): Int {
        val tv = TypedValue()
        return if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data,
                    context.resources.displayMetrics)
        } else 0
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.x
    }

    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y
    }
}
