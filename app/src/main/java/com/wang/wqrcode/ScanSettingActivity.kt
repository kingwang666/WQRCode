package com.wang.wqrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import com.wang.qrcode.camera.Light
import com.wang.qrcode.camera.Mode
import com.wang.qrcode.widget.ViewfinderView
import kotlinx.android.synthetic.main.activity_scan_setting.*
import permissions.dispatcher.*

@RuntimePermissions
class ScanSettingActivity : AppCompatActivity(),
        View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        ColorPickerDialog.OnColorSelectListener {

    @Mode
    private var mLight = Light.AUTO
    private var mFocusAuto = true
    @ViewfinderView.ScanLineMode
    private var mScanLineMode = ViewfinderView.LINE
    private var mReverse = false
    @ColorInt
    private var mScanLineColor = Color.parseColor("#FF4081")
    @ColorInt
    private var mMaskColor = Color.parseColor("#60000000")
    @ColorInt
    private var mBorderColor = Color.WHITE
    @ColorInt
    private var mCornerColor = Color.WHITE
    private var mBeep = true
    private var mVibrate = true
    private var mContinued = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_setting)
        initListener()
        afterView()
    }

    private fun initListener() {
        light_rg.setOnCheckedChangeListener(this)
        focus_rg.setOnCheckedChangeListener(this)
        scan_line_rg.setOnCheckedChangeListener(this)
        reverse_cb.setOnCheckedChangeListener(this)
        scan_color_view.setOnClickListener(this)
        mask_color_view.setOnClickListener(this)
        border_color_view.setOnClickListener(this)
        corner_color_view.setOnClickListener(this)
        beep_cb.setOnCheckedChangeListener(this)
        vibrate_cb.setOnCheckedChangeListener(this)
        continued_cb.setOnCheckedChangeListener(this)
        camera_fab.setOnClickListener(this)
    }

    private fun afterView() {
        scan_width_et.setText("0")
        scan_height_et.setText("0")
        scan_color_view.setBackgroundColor(mScanLineColor)
        mask_color_view.setBackgroundColor(mMaskColor)
        border_et.setText("1")
        border_color_view.setBackgroundColor(mBorderColor)
        corner_size_et.setText("6")
        corner_length_et.setText("20")
        corner_color_view.setBackgroundColor(mCornerColor)
        delay_et.setText("1000")
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun qrScan() {
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra("light", mLight)
        intent.putExtra("focus", mFocusAuto)
        scan_width_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("scan_width", Integer.valueOf(it.toString()))
        }
        scan_height_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("scan_height", Integer.valueOf(it.toString()))
        }
        intent.putExtra("scan_line", mScanLineMode)
        intent.putExtra("reverse", mReverse)
        intent.putExtra("scan_line_color", mScanLineColor)
        intent.putExtra("mask_color", mMaskColor)
        border_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("border_size", Integer.valueOf(it.toString()))
        }
        intent.putExtra("border_color", mBorderColor)
        corner_size_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("corner_size", Integer.valueOf(it.toString()))
        }
        corner_length_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("corner_length", Integer.valueOf(it.toString()))
        }
        intent.putExtra("corner_color", mCornerColor)
        intent.putExtra("beep", mBeep)
        intent.putExtra("vibrate", mVibrate)
        intent.putExtra("continued", mContinued)
        delay_et.text.takeIf {
            it.isNotEmpty()
        }?.let {
            intent.putExtra("delay", Integer.valueOf(it.toString()))
        }
        startActivity(intent)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_camera_rationale, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_ask_again, Toast.LENGTH_SHORT).show()
    }

    override fun onColorSelect(view: View, color: Int) {
        view.setBackgroundColor(color)
        when (view.id) {
            R.id.scan_color_view -> mScanLineColor = color
            R.id.mask_color_view -> mMaskColor = color
            R.id.border_color_view -> mBorderColor = color
            R.id.corner_color_view -> mCornerColor = color
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.camera_fab -> {
                qrScanWithPermissionCheck()
            }
            else -> {
                ColorPickerDialog(this, (v.background as ColorDrawable).color, v, this).show()

            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.light_auto_rb -> mLight = Light.AUTO
            R.id.light_off_rb -> mLight = Light.OFF
            R.id.light_on_rb -> mLight = Light.ON
            R.id.focus_auto_rb -> mFocusAuto = true
            R.id.focus_click_rb -> mFocusAuto = false
            R.id.line_rb -> mScanLineMode = ViewfinderView.LINE
            R.id.grid_rb -> mScanLineMode = ViewfinderView.GRID
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.reverse_cb -> mReverse = isChecked
            R.id.beep_cb -> mBeep = isChecked
            R.id.vibrate_cb -> mVibrate = isChecked
            R.id.continued_cb -> mContinued = isChecked
        }
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
                .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
                .setCancelable(false)
                .setMessage(messageResId)
                .show()
    }
}
