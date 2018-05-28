package com.wang.qrcode.encoder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/27
 */

public class CodeEncoder {

    public static final int NORMAL = 1;
    public static final int GREY = 2;
    public static final int BINARY = 3;

    public static final float DEFAULT_DTA_DOT_SCALE = 0.3f;

    @IntDef({NORMAL, GREY, BINARY})
    public @interface BitmapMode {

    }

    public static class Options {


        /**
         * this is character set {@link CharacterSetECI}. when it is null,zxing use "ISO-8859-1".
         * default = {@link CharacterSetECI#UTF8}
         */
        @Nullable
        public CharacterSetECI characterSet;
        /**
         * this is error correction level {@link ErrorCorrectionLevel }. when it is null,zxing use "L". only for {@link NormalQRCodeEncoder}
         * default = {@link ErrorCorrectionLevel#H}
         */
        @Nullable
        public ErrorCorrectionLevel errorLevel;
        /**
         * Margin to add around the QR code.
         * default = 20
         */
        @IntRange(from = 0)
        public int margin;
        /**
         * Scale the data blocks and makes them appear smaller. only for {@link AwesomeQRCodeEncoder},
         * default = 0.3
         */
        public float dataDotScale;
        /**
         * Color of blocks. in {@link AwesomeQRCodeEncoder} will be override by autoColor.
         * ({@link AwesomeQRCodeEncoder#BYTE_DTA},
         * {@link AwesomeQRCodeEncoder#BYTE_POS},
         * {@link AwesomeQRCodeEncoder#BYTE_AGN},
         * {@link AwesomeQRCodeEncoder#BYTE_TMG}).
         * when {@link #backgroundMode} is {@link #BINARY} or {@link #GREY}, it will be override to {@link android.graphics.Color#BLACK}
         * default = {@link android.graphics.Color#BLACK}
         */
        @ColorInt
        public int colorDark;
        /**
         * Color of empty space. in {@link AwesomeQRCodeEncoder}
         * ({@link AwesomeQRCodeEncoder#BYTE_EPT})
         * when {@link #backgroundMode} is {@link #BINARY} or {@link #GREY}, it will be override to {@link android.graphics.Color#WHITE}
         * default = {@link android.graphics.Color#WHITE}
         */
        @ColorInt
        public int colorLight;
        /**
         * The background image to embed in the QR code. If null, no background image will be embedded.
         * only for {@link AwesomeQRCodeEncoder}
         */
        @Nullable
        public Bitmap background;
        /**
         * if true the background will recycle. only for {@link AwesomeQRCodeEncoder}
         * default = true
         */
        public boolean recycleBackground;
        /**
         * background color mode{@link CodeEncoder.BitmapMode}. only for {@link AwesomeQRCodeEncoder}
         * default = {@link #NORMAL}
         */
        @BitmapMode
        public int backgroundMode;
        /**
         * If true, background image will not be drawn on the margin area.
         * default = true
         */
        public boolean whiteMargin;
        /**
         * If true, colorDark will be set to the dominant color of backgroundImage. only for {@link AwesomeQRCodeEncoder},
         * and the background mode is {@link #NORMAL}
         * default = true
         */
        public boolean autoColor;
        /**
         * the center logo
         */
        @Nullable
        public Bitmap logo;
        /**
         * logo color mode{@link CodeEncoder.BitmapMode}
         * default = {@link #NORMAL}
         */
        @BitmapMode
        public int logoMode;
        /**
         * if true logo will scale. to 1/4
         * default =  true
         */
        public boolean scale;
        /**
         * if true logo will recycle
         * default = true
         */
        public boolean recycleLogo;

        public Options() {
            characterSet = CharacterSetECI.UTF8;
            errorLevel = ErrorCorrectionLevel.H;
            margin = 20;
            dataDotScale = 0.3f;
            colorDark = Color.BLACK;
            colorLight = Color.WHITE;
            background = null;
            recycleBackground = true;
            backgroundMode = NORMAL;
            whiteMargin = true;
            autoColor = true;
            logo = null;
            logoMode = NORMAL;
            scale = true;
            recycleLogo = true;
        }

    }

    public static Bitmap createQRCode(boolean awesome, String content, int size) {
        return createQRCode(awesome, content, size, null);
    }

    public static Bitmap createQRCode(boolean awesome, String content, int size, @Nullable Options options) {
        if (awesome) {
            return AwesomeQRCodeEncoder.create(content, size, options);
        } else {
            return NormalQRCodeEncoder.create(content, size, options);
        }
    }

}
