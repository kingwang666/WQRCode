package com.wang.qrcode.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;

import com.wang.qrcode.R;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/23
 */

public class ViewfinderView extends BaseFinderView {

    public static final int LINE = 1;
    public static final int GRID = 2;

    @IntDef({LINE, GRID})
    public @interface ScanLineMode {

    }

    private int mMaskColor;
    private int mBorderSize;
    private int mBorderColor;
    private int mCornerSize;
    private int mCornerLength;
    private int mCornerColor;

    @ScanLineMode
    private int mScanLineMode = LINE;
    private int mScanLineTop;
    private Drawable mScanLine;
    private int mScanLineColor;
    private boolean mReverse = false;
    private int mStepUnit;
    private int mStepTime = 1;
    private int mAllTime;

    private Paint mPaint;


    public ViewfinderView(Context context) {
        super(context);
        init(context, null, 0, R.style.ViewfinderViewStyle);
    }

    public ViewfinderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, R.style.ViewfinderViewStyle);
    }

    public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.ViewfinderViewStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView, defStyleAttr, defStyleRes);
        mScanLineMode = a.getInteger(R.styleable.ViewfinderView_vfv_lineMode, LINE);
        mScanLine = a.getDrawable(R.styleable.ViewfinderView_vfv_lineDrawable);
        mScanLineColor = a.getColor(R.styleable.ViewfinderView_vfv_lineColor, Color.TRANSPARENT);
        if (mScanLineColor != Color.TRANSPARENT && mScanLine != null) {
            tintDrawable(mScanLine, mScanLineColor, PorterDuff.Mode.SRC_IN);
        }
        mStepUnit = a.getDimensionPixelSize(R.styleable.ViewfinderView_vfv_stepUnit, dp2pxSize(context, 2f));
        mAllTime = a.getInteger(R.styleable.ViewfinderView_vfv_animTime, 1000);
        mReverse = a.getBoolean(R.styleable.ViewfinderView_vfv_reverse, false);
        mMaskColor = a.getColor(R.styleable.ViewfinderView_vfv_maskColor, ContextCompat.getColor(context, R.color.mask_color));
        mBorderColor = a.getColor(R.styleable.ViewfinderView_vfv_borderColor, Color.WHITE);
        mBorderSize = a.getDimensionPixelSize(R.styleable.ViewfinderView_vfv_borderSize, 1);
        mCornerColor = a.getColor(R.styleable.ViewfinderView_vfv_cornerColor, Color.WHITE);
        mCornerSize = a.getDimensionPixelSize(R.styleable.ViewfinderView_vfv_cornerSize, dp2pxSize(context, 3f));
        mCornerLength = a.getDimensionPixelSize(R.styleable.ViewfinderView_vfv_cornerLength, dp2pxSize(context, 10f));
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }


    public void setMaskColor(@ColorInt int maskColor) {
        mMaskColor = maskColor;
    }

    public void setBorderSize(@Px int borderSize) {
        mBorderSize = borderSize;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        mBorderColor = borderColor;
    }

    public void setCornerSize(@Px int cornerSize) {
        mCornerSize = cornerSize;
    }

    public void setCornerLength(@Px int cornerLength) {
        mCornerLength = cornerLength;
    }

    public void setCornerColor(@ColorInt int cornerColor) {
        mCornerColor = cornerColor;
    }

    public void setScanLineMode(@ScanLineMode int scanLineMode) {
        mScanLineMode = scanLineMode;
    }

    public void setScanLine(@Nullable Drawable scanLine) {
        mScanLine = scanLine;
        if (mScanLineColor != Color.TRANSPARENT && mScanLine != null) {
            tintDrawable(mScanLine, mScanLineColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setScanLineColor(@ColorInt int scanLineColor) {
        mScanLineColor = scanLineColor;
        if (mScanLineColor != Color.TRANSPARENT && mScanLine != null) {
            tintDrawable(mScanLine, mScanLineColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setReverse(boolean reverse) {
        mReverse = reverse;
    }

    public void setStepUnit(int stepUnit) {
        mStepUnit = stepUnit;
    }

    public void setAllTime(int allTime) {
        mAllTime = allTime;
    }

    @Override
    protected void onFinderDraw(@NonNull Canvas canvas, @NonNull Rect rect) {
        if (isInEditMode() && mScanLineTop == 0){
            mScanLineTop = rect.height() / 2;
        }
        if (mStepTime <= 1) {
            mStepTime = Math.max((mAllTime * mStepUnit / rect.height()), 1);
        }
        drawMask(canvas, rect);
        drawBorder(canvas, rect);
        drawCorner(canvas, rect);
        drawScanLine(canvas, rect);
    }

    /**
     * 画遮罩层
     */
    private void drawMask(Canvas canvas, Rect rect) {
        if (mMaskColor != Color.TRANSPARENT) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mMaskColor);
            canvas.drawRect(0f, 0f, width, rect.top - mBorderSize, mPaint);
            canvas.drawRect(0f, rect.top - mBorderSize, rect.left - mBorderSize, rect.bottom + mBorderSize, mPaint);
            canvas.drawRect(rect.right + mBorderSize, rect.top - mBorderSize, width, rect.bottom + mBorderSize, mPaint);
            canvas.drawRect(0f, rect.bottom + mBorderSize, width, height, mPaint);

        }
    }

    /**
     * 画边框线
     */
    private void drawBorder(Canvas canvas, Rect rect) {
        float half = mBorderSize * 0.5f;
        if (mBorderColor != Color.TRANSPARENT && half > 0f) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderSize);
            canvas.drawRect(rect.left - half, rect.top - half, rect.right + half, rect.bottom + half, mPaint);
        }
    }

    /**
     * 画边上四个直角
     */
    private void drawCorner(Canvas canvas, Rect rect) {
        float min = (Math.min(Math.min(rect.width() * 0.5f, rect.height() * 0.5f), mCornerLength));
        float half = mCornerSize * 0.5f;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mCornerColor);
        mPaint.setStrokeWidth(mCornerSize);

        canvas.drawLine(rect.left + half - mBorderSize, rect.top - mBorderSize, rect.left + half - mBorderSize, rect.top + min - mBorderSize, mPaint);
        canvas.drawLine(rect.left + mCornerSize - mBorderSize, rect.top + half - mBorderSize, rect.left + min - mBorderSize, rect.top + half - mBorderSize, mPaint);

        canvas.drawLine(rect.right - min + mBorderSize, rect.top + half - mBorderSize, rect.right + mBorderSize, rect.top + half - mBorderSize, mPaint);
        canvas.drawLine(rect.right - half + mBorderSize, rect.top + mCornerSize - mBorderSize, rect.right - half + mBorderSize, rect.top + min - mBorderSize, mPaint);

        canvas.drawLine(rect.right - half + mBorderSize, rect.bottom - min + mBorderSize, rect.right - half + mBorderSize, rect.bottom + mBorderSize, mPaint);
        canvas.drawLine(rect.right - min + mBorderSize, rect.bottom - half + mBorderSize, rect.right - mCornerSize + mBorderSize, rect.bottom - half + mBorderSize, mPaint);

        canvas.drawLine(rect.left + mCornerSize - mBorderSize, rect.bottom - half + mBorderSize, rect.left + min - mBorderSize, rect.bottom - half + mBorderSize, mPaint);
        canvas.drawLine(rect.left + half - mBorderSize, rect.bottom - min + mBorderSize, rect.left + half - mBorderSize, rect.bottom + mBorderSize, mPaint);
    }

    /**
     * 画扫描线
     */
    private void drawScanLine(Canvas canvas, Rect rect) {
        if (mScanLine != null) {
            switch (mScanLineMode) {
                case LINE:
                    int lineHeight = mScanLine.getIntrinsicHeight();
                    mScanLine.setBounds(rect.left, rect.top + mScanLineTop, rect.right, rect.top + mScanLineTop + lineHeight);
                    mScanLine.draw(canvas);
                    postInvalidateDelayed(mStepTime, rect, lineHeight);
                    break;
                case GRID:
                    mScanLine.setBounds(rect.left, rect.top, rect.right, rect.top + mScanLineTop);
                    mScanLine.draw(canvas);
                    postInvalidateDelayed(mStepTime, rect, 0);
                    break;
            }
        }
    }

    private void postInvalidateDelayed(long delayMilliseconds, Rect rect, int lineHeight) {
        mScanLineTop += mStepUnit;
        int bottom = rect.top + mScanLineTop + lineHeight;
        if (bottom > rect.bottom) {
            if (mReverse) {
                mStepUnit = -mStepUnit;
                mScanLineTop = mScanLineTop - (bottom - rect.bottom);
            } else {
                mScanLineTop = 0;
            }
        } else if (mScanLineTop < 0) {
            mScanLineTop = -mScanLineTop;
            mStepUnit = -mStepUnit;
        }
        super.postInvalidateDelayed(delayMilliseconds, rect.left, rect.top, rect.right, rect.bottom);
    }


}
