package com.wang.qrcode.encoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.wang.imageutil.ImageUtil;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/24
 */

abstract class BaseEncoder {


    /**
     * 添加logo到二维码图片上
     *
     * @param src
     * @param logo
     * @return
     */
    protected static Bitmap addLogoToQRCode(Bitmap src, Bitmap logo, @CodeEncoder.BitmapMode int logoMode, boolean scale, boolean recycleLogo) {
        if (src == null || logo == null) {
            return src;
        }
        if (logo.isMutable()) {
            switch (logoMode) {
                case CodeEncoder.BINARY:
                    binary(logo);
                    break;
                case CodeEncoder.GREY:
                    grey(logo);
                    break;
                case CodeEncoder.NORMAL:
                default:
                    break;
            }
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

//        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(src);
            canvas.drawBitmap(src, 0, 0, null);
            if (scale) {
                float scaleFactor = srcWidth * 1.0f / 4 / logoWidth;
                canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            }
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
        } catch (Exception e) {
            Log.e("Encoder", e.getMessage(), e);
        } finally {
            if (recycleLogo) {
                logo.recycle();
            }
        }
        return src;
    }

    protected static void binary(Bitmap bitmap) {
        ImageUtil.OTSU(bitmap);
    }

    protected static void grey(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int[] pixels = new int[width * height];
//        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//        int[] greys = ImageUtil.grey(pixels);
//        if (greys != null) {
//            bitmap.setPixels(greys, 0, width, 0, 0, width, height);
//        }
        ImageUtil.grey(bitmap);
    }
}
