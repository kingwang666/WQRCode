package com.wang.wqrcode

import android.app.Activity
import android.widget.Toast

/**
 * Author: wangxiaojie6
 * Date: 2018/5/23
 */
fun Activity.showToast(content: String){
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
}