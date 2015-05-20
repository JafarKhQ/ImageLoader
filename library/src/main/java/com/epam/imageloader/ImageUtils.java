package com.epam.imageloader;

import android.graphics.BitmapFactory;


final class ImageUtils {

    private ImageUtils() {
    }

    static void prepareInSampleSize(BitmapFactory.Options options,
                                           int targetWidth, int targetHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        final int height = options.outHeight;
        final int width = options.outWidth;

        if (height > targetHeight || width > targetWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > targetHeight &&
                    (halfWidth / inSampleSize) > targetWidth) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
    }
}
