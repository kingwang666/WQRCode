package com.wang.qrcode.encoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import android.text.TextUtils;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Hashtable;

class AwesomeQRCodeEncoder extends BaseEncoder {
    /**
     * For more information about QR code, refer to: https://en.wikipedia.org/wiki/QR_code
     * BYTE_EPT: Empty block
     * BYTE_DTA: Data block
     * BYTE_POS: Position block
     * BYTE_AGN: Align block
     * BYTE_TMG: Timing block
     * BYTE_PTC: Protector block, translucent layer (custom block, this is not included in QR code's standards)
     */
    private static final int BYTE_EPT = 0x0;
    private static final int BYTE_DTA = 0x1;
    private static final int BYTE_POS = 0x2;
    private static final int BYTE_AGN = 0x3;
    private static final int BYTE_TMG = 0x4;
    private static final int BYTE_PTC = 0x5;

    /**
     * Create a QR matrix and render it use given configs.
     *
     * @param contents Contents to encode.
     * @param size     Width as well as the height of the output QR code, includes margin.
     * @param options  Options {@link com.wang.qrcode.encoder.CodeEncoder.Options}
     * @return Bitmap of QR code
     */
    @Nullable
    static Bitmap create(@NonNull String contents, int size, @Nullable CodeEncoder.Options options) {
        if (options == null) {
            options = new CodeEncoder.Options();
        }

        if (TextUtils.isEmpty(contents)) {
            return null;
        }
        if (size <= 0) {
            return null;
        }
        if (options.margin < 0 || size - 2 * options.margin <= 0) {
            options.margin = 0;
        }

        ByteMatrix byteMatrix = getByteMatrix(contents, options.characterSet);

        if (byteMatrix == null) {
            return null;
        }

        if (size - 2 * options.margin < byteMatrix.getWidth()) {
            options.margin = 0;
            if (size < byteMatrix.getWidth()) {
                return null;
            }
        }
        if (options.dataDotScale < 0 || options.dataDotScale > 1) {
            options.dataDotScale = CodeEncoder.DEFAULT_DTA_DOT_SCALE;
        }
        Bitmap bitmap = render(byteMatrix, size - 2 * options.margin, options);
        bitmap = addLogoToQRCode(bitmap, options.logo, options.logoMode, options.scale, options.recycleLogo);
        return bitmap;
    }

    @NonNull
    private static Bitmap render(ByteMatrix byteMatrix, int innerRenderedSize, CodeEncoder.Options options) {

        int nCount = byteMatrix.getWidth();
        float nWidth = (float) innerRenderedSize / nCount;
        float nHeight = (float) innerRenderedSize / nCount;

        Bitmap backgroundImageScaled = null;

        if (options.background != null) {
            backgroundImageScaled = Bitmap.createBitmap(
                    innerRenderedSize + (options.whiteMargin ? 0 : options.margin * 2),
                    innerRenderedSize + (options.whiteMargin ? 0 : options.margin * 2),
                    Bitmap.Config.ARGB_8888);
            scaleBitmap(options.background, backgroundImageScaled);

            if (options.backgroundMode == CodeEncoder.BINARY) {
                if (backgroundImageScaled.isMutable()) {
                    options.colorDark = Color.BLACK;
                    options.colorLight = Color.WHITE;
                    binary(backgroundImageScaled);
                }
            } else if (options.backgroundMode == CodeEncoder.GREY) {
                if (backgroundImageScaled.isMutable()) {
                    options.colorDark = Color.BLACK;
                    options.colorLight = Color.WHITE;
                    grey(backgroundImageScaled);
                }
            } else if (options.autoColor) {
                options.colorDark = getDominantColor(options.background);
            }

            if (options.recycleBackground) {
                options.background.recycle();
            }
        }


        Paint paint = new Paint();
        Paint paintDark = new Paint();
        paintDark.setColor(options.colorDark);
        paintDark.setAntiAlias(true);
        Paint paintLight = new Paint();
        paintLight.setColor(options.colorLight);
        paintLight.setAntiAlias(true);
        Paint paintProtector = new Paint();
        paintProtector.setColor(Color.argb(120, 255, 255, 255));
        paintProtector.setAntiAlias(true);

        Bitmap renderedBitmap = Bitmap.createBitmap(
                innerRenderedSize + options.margin * 2,
                innerRenderedSize + options.margin * 2,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(renderedBitmap);
        canvas.drawColor(Color.WHITE);

        if (backgroundImageScaled != null) {
            canvas.drawBitmap(backgroundImageScaled, options.whiteMargin ? options.margin : 0, options.whiteMargin ? options.margin : 0, paint);
            backgroundImageScaled.recycle();
        }

        for (int row = 0; row < byteMatrix.getHeight(); row++) {
            for (int col = 0; col < byteMatrix.getWidth(); col++) {
                switch (byteMatrix.get(col, row)) {
                    case BYTE_AGN:
                    case BYTE_POS:
                    case BYTE_TMG:
                        canvas.drawRect(
                                options.margin + col * nWidth,
                                options.margin + row * nHeight,
                                options.margin + (col + 1.0f) * nWidth,
                                options.margin + (row + 1.0f) * nHeight,
                                paintDark
                        );
                        break;
                    case BYTE_DTA:
                        canvas.drawRect(
                                options.margin + (col + 0.5f * (1 - options.dataDotScale)) * nWidth,
                                options.margin + (row + 0.5f * (1 - options.dataDotScale)) * nHeight,
                                options.margin + (col + 0.5f * (1 + options.dataDotScale)) * nWidth,
                                options.margin + (row + 0.5f * (1 + options.dataDotScale)) * nHeight,
                                paintDark
                        );
                        break;
                    case BYTE_PTC:
                        canvas.drawRect(
                                options.margin + col * nWidth,
                                options.margin + row * nHeight,
                                options.margin + (col + 1.0f) * nWidth,
                                options.margin + (row + 1.0f) * nHeight,
                                paintProtector
                        );
                        break;
                    case BYTE_EPT:
                        canvas.drawRect(
                                options.margin + (col + 0.5f * (1 - options.dataDotScale)) * nWidth,
                                options.margin + (row + 0.5f * (1 - options.dataDotScale)) * nHeight,
                                options.margin + (col + 0.5f * (1 + options.dataDotScale)) * nWidth,
                                options.margin + (row + 0.5f * (1 + options.dataDotScale)) * nHeight,
                                paintLight
                        );
                        break;
                }
            }
        }

        return renderedBitmap;
    }

    private static ByteMatrix getByteMatrix(String contents, @Nullable CharacterSetECI characterSet) {
        try {
            QRCode qrCode = getProtoQRCode(contents, characterSet, ErrorCorrectionLevel.H);
            int agnCenter[] = qrCode.getVersion().getAlignmentPatternCenters();
            ByteMatrix byteMatrix = qrCode.getMatrix();
            int matSize = byteMatrix.getWidth();
            for (int row = 0; row < matSize; row++) {
                for (int col = 0; col < matSize; col++) {
                    if (isTypeAGN(col, row, agnCenter, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_AGN);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypePOS(col, row, matSize, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_POS);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypeTMG(col, row, matSize)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_TMG);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }

                    if (isTypePOS(col, row, matSize, false)) {
                        if (byteMatrix.get(col, row) == BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }
                }
            }
            return byteMatrix;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param contents   Contents to encode.
     * @param errorLevel ErrorCorrectionLevel
     * @return QR code object.
     * @throws WriterException Refer to the messages below.
     */
    private static QRCode getProtoQRCode(String contents, @Nullable CharacterSetECI characterSet, @NonNull ErrorCorrectionLevel errorLevel) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        if (characterSet != null) {
            hints.put(EncodeHintType.CHARACTER_SET, characterSet); // 字符转码格式设置
        }
        hints.put(EncodeHintType.ERROR_CORRECTION, errorLevel); // 容错级别设置
        return Encoder.encode(contents, errorLevel, hints);
    }

    private static boolean isTypeAGN(int x, int y, int[] agnCenter, boolean edgeOnly) {
        if (agnCenter.length == 0) return false;
        int edgeCenter = agnCenter[agnCenter.length - 1];
        for (int agnY : agnCenter) {
            for (int agnX : agnCenter) {
                if (edgeOnly && agnX != 6 && agnY != 6 && agnX != edgeCenter && agnY != edgeCenter)
                    continue;
                if ((agnX == 6 && agnY == 6) || (agnX == 6 && agnY == edgeCenter) || (agnY == 6 && agnX == edgeCenter))
                    continue;
                if (x >= agnX - 2 && x <= agnX + 2 && y >= agnY - 2 && y <= agnY + 2)
                    return true;
            }
        }
        return false;
    }

    private static boolean isTypePOS(int x, int y, int size, boolean inner) {
        if (inner) {
            return ((x < 7 && (y < 7 || y >= size - 7)) || (x >= size - 7 && y < 7));
        } else {
            return ((x <= 7 && (y <= 7 || y >= size - 8)) || (x >= size - 8 && y <= 7));
        }
    }

    private static boolean isTypeTMG(int x, int y, int size) {
        return ((y == 6 && (x >= 8 && x < size - 8)) || (x == 6 && (y >= 8 && y < size - 8)));
    }

    private static void scaleBitmap(Bitmap src, Bitmap dst) {
        Paint cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);
        cPaint.setFilterBitmap(true);

        float ratioX = dst.getWidth() / (float) src.getWidth();
        float ratioY = dst.getHeight() / (float) src.getHeight();
        float middleX = dst.getWidth() * 0.5f;
        float middleY = dst.getHeight() * 0.5f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(dst);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(src, middleX - src.getWidth() / 2,
                middleY - src.getHeight() / 2, cPaint);
    }

    private static int getDominantColor(Bitmap bitmap) {
        return new Palette.Builder(bitmap)
                .resizeBitmapArea(8)
                .generate()/*.getDarkVibrantColor(0x000000)*/.getDominantColor(0x000000);
    }


}
