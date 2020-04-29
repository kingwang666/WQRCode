package com.wang.wqrcode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.wang.qrcode.encoder.CodeEncoder
import kotlinx.android.synthetic.main.activity_get_qrcode.*
import kotlinx.android.synthetic.main.activity_scan.*
import permissions.dispatcher.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author: wangxiaojie6
 * Date: 2018/5/24
 */
@RuntimePermissions
class GetQRCodeActivity : AppCompatActivity(), ColorPickerDialog.OnColorSelectListener, GetQRCodeActivityPresenter.IView {

    companion object {
        private const val ALBUM_LOGO = 100
        private const val ALBUM_BG = 101
        @CodeEncoder.BitmapMode
        const val NONE = -1
    }
    private val mProvider = AndroidLifecycle.createLifecycleProvider(this)
    val mSdf = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.getDefault())

    private var mAwesome = false
    private val mOp = CodeEncoder.Options()
    private var mLogo: Any? = null
    private var mBackground: Any? = null
    private var mLightColor: Int = Color.WHITE
    private var mDarkColor: Int = Color.BLACK


    private lateinit var mPresenter: GetQRCodeActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_qrcode)
        mPresenter = GetQRCodeActivityPresenter(this, mProvider)

        mLogo = R.mipmap.ic_launcher_round
        mBackground = R.mipmap.ic_launcher

        qr_img.setOnLongClickListener {
            AlertDialog.Builder(this)
                    .setTitle("保存")
                    .setMessage("手否保存该图片")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { dialog, _ ->
                        mPresenter.saveBitmap((qr_img.drawable as BitmapDrawable).bitmap, "qr_${mSdf.format(Date())}.jpg")
                    }.show()
            true
        }

        awesome_cb.setOnCheckedChangeListener { _, isChecked ->
            mAwesome = isChecked
        }

        logo_img.setOnClickListener {
            openAlbum(ALBUM_LOGO)
        }
        background_img.setOnClickListener {
            openAlbum(ALBUM_BG)
        }

        logo_rg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.logo_none_rb -> mOp.logoMode = NONE
                R.id.logo_norm_rb -> mOp.logoMode = CodeEncoder.NORMAL
                R.id.logo_grey_rb -> mOp.logoMode = CodeEncoder.GREY
                R.id.logo_bin_rb -> mOp.logoMode = CodeEncoder.BINARY
            }
        }

        background_rg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.bg_none_rb -> mOp.backgroundMode = NONE
                R.id.bg_norm_rb -> mOp.backgroundMode = CodeEncoder.NORMAL
                R.id.bg_grey_rb -> mOp.backgroundMode = CodeEncoder.GREY
                R.id.bg_bin_rb -> mOp.backgroundMode = CodeEncoder.BINARY
            }
        }

        color_dark_view.setOnClickListener {
            ColorPickerDialog(this, mDarkColor, it, this).show()
        }

        color_light_view.setOnClickListener {
            ColorPickerDialog(this, mLightColor, it, this).show()
        }

        refresh_fab.setOnClickListener {
            content_et.text.toString()
                    .takeUnless {
                        it.isEmpty()
                    }?.also {
                        mOp.colorLight = mLightColor
                        mOp.colorDark = mDarkColor
                        mPresenter.getQRCode(this, it, mAwesome, mLogo, mBackground, mOp)
                    }

        }
    }

    override fun onColorSelect(view: View, color: Int) {
        view.setBackgroundColor(color)
        when (view.id) {
            R.id.color_light_view -> mLightColor = color
            R.id.color_dark_view -> mDarkColor = color
        }
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
            ALBUM_LOGO -> {
                data?.data?.let {
                    logo_img.setImageURI(it)
                    mLogo = it
                }
            }
            ALBUM_BG -> {
                data?.data?.let {
                    background_img.setImageURI(it)
                    mBackground = it
                }
            }
        }
    }


    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun openAlbum(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"//相片类型
        startActivityForResult(intent, requestCode)
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

    override fun getQRCodeSuccess(bitmap: Bitmap) {
        qr_img.setImageBitmap(bitmap)
    }

    override fun saveBitmapSuccess(path: String) {
        PhotoScannerManager.get(this).connect(path)
        showToast(path)
    }
}