package com.epam.imageloader;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;

/**
 * Created by jafar_qaddoumi on 5/19/15.
 */
class MemoryCache {

    private final LinkedHashMap<String, Bitmap> mMap;

    MemoryCache() {
        mMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    Bitmap add(Bitmap bitmap, String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        if (null == bitmap) {
            throw new NullPointerException("null == bitmap");
        }

        synchronized (this) {
            return mMap.put(fileName, bitmap);
        }
    }

    Bitmap get(String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        synchronized (this) {
            return mMap.get(fileName);
        }
    }

    Bitmap remove(String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        synchronized (this) {
            return mMap.remove(fileName);
        }
    }

    boolean contain(String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        return mMap.containsKey(fileName);
    }

    void clear() {
        synchronized (this) {
            mMap.clear();
        }
    }
}
