package com.wang.wqrcode

import android.content.Context
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import kotlinx.android.synthetic.main.dialog_color_picker.*

/**
 * Author: wangxiaojie6
 * Date: 2018/5/18
 */
class ColorPickerDialog(context: Context,
                        @ColorInt private val mColor: Int,
                        private val mView: View,
                        private val mListener: OnColorSelectListener?
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)
        picker.addOpacityBar(opacity_bar)
        picker.addSaturationBar(saturation_bar)
        picker.addValueBar(value_bar)
        picker.color = mColor
        picker.oldCenterColor = mColor
        negative_btn.setOnClickListener {
            dismiss()
        }
        positive_btn.setOnClickListener {
            mListener?.onColorSelect(mView, picker.color)
            dismiss()
        }
        picker.setOnColorSelectedListener {  }
    }

    interface OnColorSelectListener {

        fun onColorSelect(view: View, @ColorInt color: Int)

    }
}
