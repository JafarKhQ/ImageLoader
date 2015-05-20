package com.epam.imageloader;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileCache {
    private static final String DIR_CACHE_NAME = "_cache_images";

    private File mCacheDir;

    FileCache(Context context) {
        File appCacheDir = context.getCacheDir();
        if (null != appCacheDir) {
            mCacheDir = new File(appCacheDir, DIR_CACHE_NAME);
            if (null != mCacheDir && false == mCacheDir.exists()) {
                mCacheDir.mkdirs();
            }
        }
    }

    File add(InputStream is, String fileName) throws IOException {
        if (null == fileName) {
            throw new NullPointerException("key == null");
        }

        if (null == is) {
            throw new NullPointerException("is == null");
        }

        OutputStream os = null;

        synchronized (this) {
            File imageFile = getCacheFile(fileName);
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

            return imageFile;
        }
    }

    File get(String fileName) {
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

    void remove(String fileName) {
        if (null == fileName) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            File file = getCacheFile(fileName);
            if (null != file) {
                file.delete();
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

    private File getCacheFile(String fileName) {
        return new File(mCacheDir, fileName);
    }
}
