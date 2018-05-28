package com.wang.wqrcode

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_scan_result.*

/**
 * Author: wangxiaojie6
 * Date: 2018/5/23
 */
class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)
        recycler_view.layoutManager = LinearLayoutManager(this)
        intent.getStringArrayListExtra("result")?.also {
            recycler_view.adapter = ResultAdapter(it)
        }
    }
}