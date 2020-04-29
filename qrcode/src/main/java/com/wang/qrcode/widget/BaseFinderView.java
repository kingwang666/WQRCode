package com.wang.qrcode.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.DrawableUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wang.qrcode.R;
import com.wang.qrcode.camera.CameraManager;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/23
 */

public abstract class BaseFinderView extends View implements IViewfinder {

    private static final String TAG = "finderView";

    protected int mScanWidth;
    protected int mScanHeight;

    private Rect mScanRect;

    private CameraManager mCameraManager;

    public BaseFinderView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BaseFinderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BaseFinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseFinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseFinderView, defStyleAttr, defStyleRes);
        mScanWidth = a.getDimensionPixelSize(R.styleable.BaseFinderView_scanWidth, 0);
        mScanHeight = a.getDimensionPixelSize(R.styleable.BaseFinderView_scanHeight, 0);
        final int scanViewId = a.getResourceId(R.styleable.BaseFinderView_scanView, View.NO_ID);
        a.recycle();
        if (scanViewId != View.NO_ID)
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    Activity activity = findActivity(context);
                    try {
                        if (activity != null) {
                            View view = activity.findViewById(scanViewId);
                            if (view != null && view instanceof ScanView) {
                                ((ScanView) view).bindViewfinder(BaseFinderView.this);
                                Log.d(TAG, "auto bind camera manager");
                            } else {
                                Log.e(TAG, "bind camera manager failure: dont find ScanView");
                            }
                        } else {
                            Log.e(TAG, "bind camera manager failure: dont find ScanView");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    return true;
                }
            });
    }

    @Override
    public void bind(@NonNull CameraManager cameraManager) {
        mCameraManager = cameraManager;
        if (mScanWidth > 0 && mScanHeight > 0) {
            cameraManager.setManualFramingRect(mScanWidth, mScanHeight);
        }
    }

    public void setScanWidth(int scanWidth) {
        mScanWidth = scanWidth;
    }

    public void setScanHeight(int scanHeight) {
        mScanHeight = scanHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScanRect == null) {
            if (isInEditMode()) {
                int width = mScanWidth > 0 ? mScanWidth : CameraManager.findDesiredWidthInRange(getWidth());
                int height = mScanHeight > 0 ? mScanHeight : CameraManager.findDesiredHeightInRange(getHeight());
                int offsetX = (getWidth() - width) / 2;
                int offsetY = (getHeight() - height) / 2;
                mScanRect = new Rect(offsetX, offsetY, offsetX + width, offsetY + height);
            } else if (mCameraManager != null) {
                mScanRect = mCameraManager.getFramingRect();
            }
            if (mScanRect != null) {
                onFinderDraw(canvas, mScanRect);
            }
        } else {
            onFinderDraw(canvas, mScanRect);
        }
    }

    protected abstract void onFinderDraw(@NonNull Canvas canvas, @NonNull Rect rect);

    @Nullable
    public Rect getScanRect() {
        return mScanRect;
    }

    @SuppressLint("RestrictedApi")
    public void tintDrawable(@NonNull Drawable drawable, @ColorInt int color, @Nullable PorterDuff.Mode mode) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable)
                && drawable.mutate() != drawable) {
            Log.e(TAG, "Mutated drawable is not the same instance as the input.");
            return;
        }
        drawable.setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(color, mode == null ? PorterDuff.Mode.SRC_IN : mode));
    }

    public float dp2px(@NonNull Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    public int dp2pxSize(@NonNull Context context, float dpValue) {
        final float f = dp2px(context, dpValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (dpValue == 0) return 0;
        if (dpValue > 0) return 1;
        return -1;
    }

    @Nullable
    private Activity findActivity(@NonNull Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }
}
