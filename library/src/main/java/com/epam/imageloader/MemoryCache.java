package com.epam.imageloader;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.LinkedList;

class MemoryCache {
    private static final String TAG = "MemoryCache";

    private int mSize;
    private int mMaxSize;
    private final LinkedHashMap<String, Bitmap> mMap;
    private final LinkedList<String> mKeys;

    /**
     * @param maxSize in bytes
     */
    MemoryCache(int maxSize) {
        Log.i(TAG, "Max memory cache size " + maxSize + " bytes");

        mSize = 0;
        mMaxSize = maxSize;
        mKeys = new LinkedList<>();
        mMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    Bitmap add(@NonNull Bitmap bitmap, @NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        if (null == bitmap) {
            throw new NullPointerException("null == bitmap");
        }

        synchronized (this) {
            mSize += sizeOf(bitmap);
            if (!mKeys.contains(fileName)) {
                mKeys.add(fileName);
            }
            final Bitmap oldBitmap = mMap.put(fileName, bitmap);
            if (null != oldBitmap) {
                mSize -= sizeOf(oldBitmap);
            }

            if (mSize > mMaxSize) {
                trimCacheSize();
            }

            return oldBitmap;
        }
    }

    Bitmap get(@NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        synchronized (this) {
            return mMap.get(fileName);
        }
    }

    Bitmap remove(@NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        synchronized (this) {
            mKeys.remove(fileName);
            Bitmap bitmap = mMap.remove(fileName);
            if (null != bitmap) {
                mSize -= sizeOf(bitmap);
            }

            return bitmap;
        }
    }

    boolean contain(@NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("null == fileName");
        }

        return mMap.containsKey(fileName);
    }

    void clear() {
        synchronized (this) {
            mKeys.clear();
            mMap.clear();
        }
    }

    private void trimCacheSize() {
        while (mSize > mMaxSize) {
            String firstEntry = mKeys.get(0);
            remove(firstEntry);
        }
    }

    private int sizeOf(@Nullable Bitmap bitmap) {
        if (null == bitmap) {
            return 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    /**
     * Calculate the size of the heap your app should use if it wants to be properly respectful
     *
     * @param context The AppContext
     * @return App heap size in bytes
     */
    public static int getAppHeap(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass(); // megabytes

        return memoryClass * (1024 * 1024); // bytes
    }
}
