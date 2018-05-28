package com.wang.wqrcode

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Author: wangxiaojie6
 * Date: 2018/5/19
 */
class MainActivity : AppCompatActivity(), MainActivityPresenter.IView {



    private lateinit var mPresenter: MainActivityPresenter

    private val mProvider = AndroidLifecycle.createLifecycleProvider(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = MainActivityPresenter(this, mProvider)
        mPresenter.getHomeQRCode(this, "https://github.com/kingwang666")

        qr_img.setOnLongClickListener {
            AlertDialog.Builder(this)
                    .setTitle("保存")
                    .setMessage("手否保存该图片")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { dialog, _ ->
                        mPresenter.saveBitmap((qr_img.drawable as BitmapDrawable).bitmap, "home.jpg")
                    }.show()
            true
        }

        scan_btn.setOnClickListener {
            val intent = Intent(this, ScanSettingActivity::class.java)
            startActivity(intent)
        }

        get_btn.setOnClickListener {
            val intent = Intent(this, GetQRCodeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getQRCodeSuccess(bitmap: Bitmap) {
        qr_img.setImageBitmap(bitmap)
    }

    override fun saveBitmapSuccess(path: String) {
        PhotoScannerManager.get(this).connect(path)
        showToast(path)
    }

}