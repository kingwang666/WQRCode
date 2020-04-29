package com.wang.qrcode.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.client.result.ParsedResult;
import com.wang.qrcode.decoder.CodeDecoder;
import com.wang.qrcode.R;
import com.wang.qrcode.camera.CameraManager;
import com.wang.qrcode.camera.Light;
import com.wang.qrcode.camera.Mode;
import com.wang.qrcode.model.ScanResult;

import java.io.IOException;

import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/15
 */

public class ScanView extends SurfaceView
        implements SurfaceHolder.Callback,
        View.OnClickListener,
        Camera.PreviewCallback,
        ScanViewPresenter.IView {

    private static final String TAG = ScanView.class.getSimpleName();

    private BeepManager mBeepManager;
    private CameraManager mCameraManager;
    private ScanViewPresenter mPresenter;
    private AmbientLightManager mLightManager;
    private FlowableEmitter<byte[]> mSubject;
    private Disposable mDisposable;

    private boolean mAutoFocus;
    private boolean mClickFocus;
    @Mode
    private int mLightMode;
    private boolean mPlayBeep;
    private boolean mVibrate;
    private boolean mContinued;
    private int mDelay;

    private boolean mHasSurface = false;
    private int mWidth;
    private int mHeight;

    private boolean mPreview = true;

    private OnScanListener mOnScanListener;

    private Runnable mContinueRun = new Runnable() {

        @Override
        public void run() {
            if (mCameraManager != null && mCameraManager.isOpen()) {
                mCameraManager.setOneShotPreviewCallback(ScanView.this);
            }
        }
    };


    public ScanView(Context context) {
        super(context);
        init(context, null, 0, R.style.ScanViewStyle);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, R.style.ScanViewStyle);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.ScanViewStyle);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (!isInEditMode()) {
            mCameraManager = new CameraManager(context, false);
            mPresenter = new ScanViewPresenter(mCameraManager, CodeDecoder.HINTS, this);
            mHasSurface = false;
            getHolder().addCallback(this);
            setOnClickListener(this);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScanView, defStyleAttr, defStyleRes);
            mAutoFocus = a.getBoolean(R.styleable.ScanView_sv_autoFocus, true);
            mClickFocus = a.getBoolean(R.styleable.ScanView_sv_clickFocus, false);
            mLightMode = a.getInteger(R.styleable.ScanView_sv_lightMode, Light.AUTO);
            mPlayBeep = a.getBoolean(R.styleable.ScanView_sv_playBeep, true);
            int beep = a.getResourceId(R.styleable.ScanView_sv_beep, R.raw.beep);
            mVibrate = a.getBoolean(R.styleable.ScanView_sv_vibrate, true);
            mContinued = a.getBoolean(R.styleable.ScanView_sv_continued, false);
            mDelay = a.getInteger(R.styleable.ScanView_sv_continuedDelay, 1000);
            a.recycle();

            mBeepManager = new BeepManager(context, beep);
        }
    }

    public void setOnLightChangeListener(CameraManager.OnLightChangeListener onLightChangeListener) {
        mCameraManager.setOnLightChangeListener(onLightChangeListener);
    }

    public void bindViewfinder(@NonNull IViewfinder finder) {
        if (mCameraManager != null) {
            finder.bind(mCameraManager);
        }
    }

    public void onResume() {
        if (mHasSurface && mPreview) {
            startScan(getHolder(), mWidth, mHeight);
        }
    }

    public void setAutoFocus(boolean autoFocus) {
        mAutoFocus = autoFocus;
    }

    public void setClickFocus(boolean clickFocus) {
        mClickFocus = clickFocus;
    }

    public void setLightMode(int lightMode) {
        mLightMode = lightMode;
    }

    public void setPlayBeep(boolean playBeep) {
        mPlayBeep = playBeep;
    }

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    public void setContinued(boolean continued) {
        mContinued = continued;
    }

    public boolean isContinued() {
        return mContinued;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public void startPreview() {
        mPreview = true;
        if (mCameraManager.isOpen()) {
            mCameraManager.startPreview(this);
        } else if (mWidth > 0 && mHeight > 0) {
            onResume();
        }
    }

    public void stopPreview() {
        mPreview = false;
        if (mCameraManager.isOpen()) {
            mCameraManager.stopPreview();
        }
    }

    public void setLight(boolean on) {
        mLightMode = on ? Light.ON : Light.OFF;
        if (mLightManager != null) {
            mLightManager.stop();
            mLightManager = null;
        }
        mCameraManager.setTorch(on);
    }

    public void startScan(SurfaceHolder holder, int width, int height) {
        try {
            if (!mCameraManager.isOpen()) {
                mCameraManager.open(holder, width, height, mAutoFocus, mLightMode);
            }
            if (mSubject == null) {
                mDisposable = mPresenter.initSubject();
            }
            mCameraManager.startPreview(this);
            if (mLightMode == Light.AUTO) {
                if (mLightManager == null) {
                    mLightManager = new AmbientLightManager(getContext());
                }
                mLightManager.start(mCameraManager);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void release(boolean clearRect) {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            mDisposable = null;
        }
        removeCallbacks(mContinueRun);
        if (mLightManager != null) {
            mLightManager.stop();
            mLightManager = null;
        }
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
            mCameraManager.close(clearRect);
        }
        if (mSubject != null) {
            mSubject.onComplete();
            mSubject = null;
        }
        if (mBeepManager != null) {
            mBeepManager.close();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceChanged() gave us a null surface!");
            return;
        }
        if (!mHasSurface || (width != 0 && mWidth != width) || (height != 0 && mHeight != height)) {
            release(true);
            mWidth = width;
            mHeight = height;
            mHasSurface = true;
            if (mPreview) {
                startScan(holder, width, height);
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
        release(true);
    }

    public void setOnScanListener(OnScanListener onScanListener) {
        mOnScanListener = onScanListener;
    }

    @Override
    public void onClick(View v) {
        if (mClickFocus) {
            mCameraManager.autoFocus();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mSubject != null) {
            mSubject.onNext(data);
        }
    }

    public void onPause() {
        release(false);
    }

    @Override
    public void setSubject(FlowableEmitter<byte[]> emitter) {
        mSubject = emitter;
    }

    @Override
    public void onScan(ScanResult result) {
        if (result.success) {
            if (mPlayBeep) {
                mBeepManager.PlayBeep();
            }
            if (mVibrate) {
                mBeepManager.vibrate();
            }
            if (mOnScanListener != null) {
                mOnScanListener.onScanNext(result.result, mContinued);
            }
            if (mContinued) {
                postDelayed(mContinueRun, mDelay);
            }
        } else if (mCameraManager != null && mCameraManager.isOpen()) {
            mCameraManager.setOneShotPreviewCallback(this);
        }
    }

    @Override
    public void onScanError(Throwable throwable) {
        Log.e(TAG, throwable.getMessage(), throwable);
        if (mOnScanListener != null) {
            mOnScanListener.onScanError(throwable);
        }
    }

    public interface OnScanListener {

        void onScanNext(@NonNull ParsedResult result, boolean continued);

        void onScanError(@NonNull Throwable throwable);

    }
}
