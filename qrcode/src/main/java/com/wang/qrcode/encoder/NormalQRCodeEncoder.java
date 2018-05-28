package com.wang.qrcode.encoder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/10
 */

class NormalQRCodeEncoder extends BaseEncoder {


    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content      字符串内容
     * @param size         位图大小,要求>=0(单位:px)
     * @return
     */
    @Nullable
    static Bitmap create(@NonNull String content, @IntRange int size, @Nullable CodeEncoder.Options options) {

        /** 1.参数合法性判断 */
        if (TextUtils.isEmpty(content)) { // 字符串内容判空
            return null;
        }

        if (size <= 0) { // 宽和高都需要>=0
            return null;
        }
        if (options == null){
            options = new CodeEncoder.Options();
        }

        try {
            /** 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();

            if (options.characterSet != null) {
                hints.put(EncodeHintType.CHARACTER_SET, options.characterSet); // 字符转码格式设置
            }

            if (options.errorLevel != null) {
                hints.put(EncodeHintType.ERROR_CORRECTION, options.errorLevel); // 容错级别设置
            }

            if (options.margin >= 0) {
                hints.put(EncodeHintType.MARGIN, options.margin); // 空白边距设置
            }
            BitMatrix bitMatrix = new WQRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int colorDark = options.colorDark;
            int colorLight = options.colorLight;

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * size + x] = colorDark; // 暗色色块像素设置
                    } else {
                        pixels[y * size + x] = colorLight; // 亮色色块像素设置
                    }
                }
            }

            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            bitmap = addLogoToQRCode(bitmap, options.logo, options.logoMode, options.scale, options.recycleLogo);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}

