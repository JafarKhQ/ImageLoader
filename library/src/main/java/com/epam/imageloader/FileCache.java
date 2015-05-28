package com.epam.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileCache {
    private static final String DIR_CACHE_NAME = "_cache_images";

    private long mSize;
    private long mMaxSize;
    private File mCacheDir;

    /**
     * Currently maxSize feature not implemented
     *
     * @param context The app Context
     * @param maxSize Max cache size in MB, less than 1 means infinite
     */
    FileCache(@NonNull Context context, int maxSize) {
        File appCacheDir = context.getCacheDir();
        if (null != appCacheDir) {
            mCacheDir = new File(appCacheDir, DIR_CACHE_NAME);
            if (null != mCacheDir && false == mCacheDir.exists()) {
                mCacheDir.mkdirs();
            }
        }

        mMaxSize = maxSize * 1024 * 1024; // MB to bytes
        if (mMaxSize <= 0) {
            mSize = mMaxSize = 0;
        } else {
            mSize = calcCacheDirSize();
        }
    }

    @Nullable
    File add(@NonNull InputStream is, @NonNull String fileName) throws IOException {
        if (null == fileName) {
            throw new NullPointerException("key == null");
        }

        if (null == is) {
            throw new NullPointerException("is == null");
        }

        OutputStream os = null;

        synchronized (this) {
            final File imageFile = getCacheFile(fileName);
            if (null == imageFile) {
                return null;
            }

            try {
                os = new FileOutputStream(imageFile);
                StreamUtils.copyStream(is, os);
            } finally {
                if (null != os) {
                    os.close();
                }
            }

            mSize += sizeOf(imageFile);
            trimCacheSize();
            return imageFile;
        }
    }

    @Nullable
    File get(@NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            File file = getCacheFile(fileName);
            if (null == file || false == file.exists()) {
                return null;
            }

            return file;
        }
    }

    void remove(@NonNull String fileName) {
        if (null == fileName) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            final File file = getCacheFile(fileName);
            if (null != file) {
                long size = sizeOf(file);
                boolean success = file.delete();
                if (true == success) {
                    mSize -= size;
                }
            }
        }
    }

    void clear() {
        String[] fileNames = mCacheDir.list();
        if (null == fileNames) {
            return;
        }

        for (int i = 0; i < fileNames.length; i++) {
            remove(fileNames[i]);
        }
    }

    private File getCacheFile(@NonNull String fileName) {
        return new File(mCacheDir, fileName);
    }

    private long calcCacheDirSize() {
        if (null == mCacheDir) {
            return 0L;
        }

        File[] files = mCacheDir.listFiles();
        if (null == files) {
            return 0L;
        }

        long size = 0L;
        for (int i = 0; i < files.length; i++) {
            size += sizeOf(files[i]);
        }

        return size;
    }

    private long sizeOf(File file) {
        if (null == file || false == file.isFile()) {
            return 0L;
        }

        return file.length();
    }

    private void trimCacheSize() {
        //todo
    }
}
