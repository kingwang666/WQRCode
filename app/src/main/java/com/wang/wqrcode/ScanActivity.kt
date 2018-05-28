package com.wang.wqrcode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import com.google.zxing.client.result.ParsedResult
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.wang.qrcode.camera.CameraManager
import com.wang.qrcode.camera.Light
import com.wang.qrcode.widget.ScanView
import com.wang.qrcode.widget.ViewfinderView
import kotlinx.android.synthetic.main.activity_scan.*
import permissions.dispatcher.*

/**
 * Author: wangxiaojie6
 * Date: 2018/5/14
 */
@RuntimePermissions
class ScanActivity : AppCompatActivity(),
        Toolbar.OnMenuItemClickListener,
        CameraManager.OnLightChangeListener,
        ScanView.OnScanListener,
        ScanActivityPresenter.IView {

    companion object {
        private const val ALBUM = 100
    }

    private val mProvider = AndroidLifecycle.createLifecycleProvider(this)

    private lateinit var mPresenter: ScanActivityPresenter

    private lateinit var mResult: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        afterView()
    }

    override fun onResume() {
        super.onResume()
        scan_view.onResume()
    }

    private fun afterView() {
        mPresenter = ScanActivityPresenter(this, mProvider)
        toolbar.inflateMenu(R.menu.menu_scan)
        toolbar.setOnMenuItemClickListener(this)
        viewfinder_view.setScanWidth(intent.getIntExtra("scan_width", 0))
        viewfinder_view.setScanHeight(intent.getIntExtra("scan_height", 0))
        viewfinder_view.setMaskColor(intent.getIntExtra("mask_color", 0))
        intent.getIntExtra("scan_line", ViewfinderView.LINE).let {
            viewfinder_view.setScanLineMode(it)
            if (it == ViewfinderView.LINE) {
                viewfinder_view.setScanLine(ContextCompat.getDrawable(this, R.mipmap.ic_scan_line))
            } else {
                viewfinder_view.setScanLine(ContextCompat.getDrawable(this, R.mipmap.ic_scan_grid))
            }
        }
        viewfinder_view.setScanLineColor(intent.getIntExtra("scan_line_color", 0))
        viewfinder_view.setBorderSize(intent.getIntExtra("border_size", 0))
        viewfinder_view.setBorderColor(intent.getIntExtra("border_color", 0))
        viewfinder_view.setCornerSize(intent.getIntExtra("corner_size", 0))
        viewfinder_view.setCornerLength(intent.getIntExtra("corner_length", 0))
        viewfinder_view.setCornerColor(intent.getIntExtra("corner_color", 0))
        viewfinder_view.setReverse(intent.getBooleanExtra("reverse", false))

        scan_view.setLightMode(intent.getIntExtra("light", Light.AUTO))
        scan_view.setAutoFocus(intent.getBooleanExtra("focus", true))
        scan_view.setClickFocus(!intent.getBooleanExtra("focus", false))
        scan_view.setPlayBeep(intent.getBooleanExtra("beep", true))
        scan_view.setVibrate(intent.getBooleanExtra("vibrate", true))
        scan_view.isContinued = intent.getBooleanExtra("continued", true)
        scan_view.setDelay(intent.getIntExtra("delay", 0))
        scan_view.setOnScanListener(this)
        scan_view.setOnLightChangeListener(this)

        light_btn.setOnClickListener {
            scan_view.setLight(!it.isSelected)
        }
        result_btn.setOnClickListener {
            showResult()
        }

        mResult = ArrayList()

    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            ALBUM -> {
                data?.data?.let {
                    scan_view.stopPreview()
                    mPresenter.parseQRCode(this, it)

                }

            }
        }
    }

    override fun onLightChange(open: Boolean) {
        light_btn.isSelected = open
        if (open){
            light_btn.text = "关闭闪光灯"
        }else{
            light_btn.text = "打开闪光灯"
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        openAlbumWithPermissionCheck()
        return true
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"//相片类型
        startActivityForResult(intent, ALBUM)
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onStorageDenied() {
        Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_storage_rationale, request)
    }


    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onStorageNeverAskAgain() {
        Toast.makeText(this, R.string.permission_storage_never_ask_again, Toast.LENGTH_SHORT).show()
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
                .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
                .setCancelable(false)
                .setMessage(messageResId)
                .show()
    }

    private fun showResult() {
        Intent(this, ScanResultActivity::class.java).apply {
            putExtra("result", mResult)
        }.also {
            startActivity(it)
        }
        onBackPressed()
    }

    override fun parseQRCodeSuccess(t: String) {
        mResult.add(t)
        if (scan_view.isContinued) {
            showToast(t)
            scan_view.startPreview()
        } else {
            showResult()
        }
    }

    override fun parseQRCodeError(t: Throwable) {
        mResult.add("error: ${t.message}")
        if (scan_view.isContinued) {
            showToast("error: ${t.message}")
            scan_view.startPreview()
        } else {
            showResult()
        }
    }

    override fun onScanNext(result: ParsedResult, continued: Boolean) {
        mResult.add(result.displayResult)
        if (continued) {
            showToast(result.displayResult)
        } else {
            showResult()
        }
    }

    override fun onScanError(throwable: Throwable) {
        mResult.add("error: ${throwable.message}")
        if (scan_view.isContinued) {
            showToast("error: ${throwable.message}")
        } else {
            showResult()
        }
    }

    override fun onPause() {
        super.onPause()
        scan_view.onPause()
    }
}