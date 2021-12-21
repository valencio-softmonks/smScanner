/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.valencio.smscannermodule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public final class BarcodeUtils {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    private BarcodeUtils() {
    }

    @Nullable
    public static Result decodeBitmap(@NonNull final Bitmap bitmap) {
        return decodeBitmap(bitmap, null);
    }

    @Nullable
    public static Result decodeBitmap(@NonNull final Bitmap bitmap,
            @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(bitmap);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return decodeRgb(pixels, width, height, hints);
    }

    @Nullable
    public static Result decodeRgb(@NonNull final int[] pixels, final int width, final int height) {
        return decodeRgb(pixels, width, height, null);
    }

    @Nullable
    public static Result decodeRgb(@NonNull final int[] pixels, final int width, final int height,
            @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(pixels);
        final MultiFormatReader reader = createReader(hints);
        try {
            return Utils
                    .decodeLuminanceSource(reader, new RGBLuminanceSource(width, height, pixels));
        } catch (final ReaderException e) {
            return null;
        }
    }

    @Nullable
    public static Result decodeYuv(@NonNull final byte[] pixels, final int width,
            final int height) {
        return decodeYuv(pixels, width, height, ROTATION_0, false, null);
    }

    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public static Result decodeYuv(@NonNull final byte[] pixels, final int width, final int height,
            @Rotation final int rotation, final boolean reverseHorizontal,
            @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(pixels);
        final byte[] rotatedPixels = Utils.rotateYuv(pixels, width, height, rotation);
        final int rotatedWidth;
        final int rotatedHeight;
        if (rotation == ROTATION_90 || rotation == ROTATION_270) {
            rotatedWidth = height;
            rotatedHeight = width;
        } else {
            rotatedWidth = width;
            rotatedHeight = height;
        }
        final MultiFormatReader reader = createReader(hints);
        try {
            return Utils.decodeLuminanceSource(reader,
                    new PlanarYUVLuminanceSource(rotatedPixels, rotatedWidth, rotatedHeight, 0, 0,
                            rotatedWidth, rotatedHeight, reverseHorizontal));
        } catch (final ReaderException e) {
            return null;
        }
    }

    @Nullable
    public static BitMatrix encodeBitMatrix(@NonNull final String content,
            @NonNull final BarcodeFormat format, final int width, final int height) {
        return encodeBitMatrix(content, format, width, height, null);
    }


    @Nullable
    public static BitMatrix encodeBitMatrix(@NonNull final String content,
            @NonNull final BarcodeFormat format, final int width, final int height,
            @Nullable final Map<EncodeHintType, ?> hints) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(format);
        final MultiFormatWriter writer = new MultiFormatWriter();
        try {
            if (hints != null) {
                return writer.encode(content, format, width, height, hints);
            } else {
                return writer.encode(content, format, width, height);
            }
        } catch (final WriterException e) {
            return null;
        }
    }

    @Nullable
    public static Bitmap encodeBitmap(@NonNull final String content,
            @NonNull final BarcodeFormat format, final int width, final int height) {
        return encodeBitmap(content, format, width, height, null);
    }


    @Nullable
    public static Bitmap encodeBitmap(@NonNull final String content,
            @NonNull final BarcodeFormat format, final int width, final int height,
            @Nullable final Map<EncodeHintType, ?> hints) {
        final BitMatrix matrix = encodeBitMatrix(content, format, width, height, hints);
        if (matrix != null) {
            return createBitmap(matrix);
        } else {
            return null;
        }
    }

    @NonNull
    public static Bitmap createBitmap(@NonNull final BitMatrix matrix) {
        Objects.requireNonNull(matrix);
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final int length = width * height;
        final int[] pixels = new int[length];
        for (int i = 0; i < length; i++) {
            pixels[i] = matrix.get(i % width, i / height) ? Color.BLACK : Color.WHITE;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    @NonNull
    private static MultiFormatReader createReader(@Nullable final Map<DecodeHintType, ?> hints) {
        final MultiFormatReader reader = new MultiFormatReader();
        if (hints != null) {
            reader.setHints(hints);
        } else {
            reader.setHints(Collections
                    .singletonMap(DecodeHintType.POSSIBLE_FORMATS, CodeScanner.ALL_FORMATS));
        }
        return reader;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270})
    public @interface Rotation {
    }
}
