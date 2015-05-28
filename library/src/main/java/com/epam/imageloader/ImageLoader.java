package com.epam.imageloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int MEMORY_CACHE_MULTIPLIER = 5;
    private static final Object mLock = new Object();

    static ImageLoader sInstance;

    static Handler sUiHandler;
    static ExecutorService sExecutorService;

    static {
        sUiHandler = new Handler(Looper.getMainLooper());

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors > 1) {
            sExecutorService = Executors.newFixedThreadPool(availableProcessors - 1);
        } else {
            sExecutorService = Executors.newFixedThreadPool(1);
        }
    }

    public static ImageLoader getInstance(@NonNull Context context) {
        if (null == sInstance) {
            synchronized (mLock) {
                if (null == sInstance) {
                    sInstance = new ImageLoader(context);
                }
            }
        }

        return sInstance;
    }

    private boolean mUseDiskCache;
    private boolean mUseMemoryCache;

    private Context mAppContext;
    private FileCache mFileCache;
    private MemoryCache mMemoryCache;

    private ImageLoader(@NonNull Context context) {
        mUseDiskCache = true;
        mUseMemoryCache = true;
        mAppContext = context.getApplicationContext();

        mFileCache = new FileCache(mAppContext);
        mMemoryCache = new MemoryCache(MemoryCache.getAppHeap(mAppContext) / MEMORY_CACHE_MULTIPLIER); // 1/5=20%
    }

    public ImageLoader setGlobalDiskCache(boolean diskCache) {
        mUseDiskCache = diskCache;
        return sInstance;
    }

    public ImageLoader setGlobalMemoryCache(boolean memoryCache) {
        mUseMemoryCache = memoryCache;
        return sInstance;
    }

    public void clearMemoryCache() {
        mMemoryCache.clear();
    }

    public void clearDiskCache() {
        mFileCache.clear();
    }

    public ImageRequest from(@NonNull String where) {
        if (null == where) {
            throw new IllegalArgumentException("where cant be null");
        }

        ImageSource source = ImageSource.getImageSource(where);
        if (ImageSource.WEB == source) {
            URL url = null;
            try {
                url = new URL(where);
            } catch (MalformedURLException e) {
                Log.w(TAG, "from: " + e.getMessage());
            }

            if (null == url) {
                throw new IllegalArgumentException("Invalid URL " + where);
            } else {
                return new ImageRequest(url);
            }
        } else if (ImageSource.LOCAL == source) {
            where = ImageSource.removeScheme(where);
            File file = new File(where);
            if (null != file && file.exists() && file.canRead()) {
                return new ImageRequest(file);
            } else {
                throw new IllegalArgumentException("File cant be accessed " + where);
            }
        } else {
            throw new IllegalArgumentException("Unknown Image source " + where);
        }
    }

    boolean isDiskCache() {
        return mUseDiskCache;
    }

    boolean isMemoryCache() {
        return mUseMemoryCache;
    }

    Context getAppContext() {
        return mAppContext;
    }

    FileCache getFileCache() {
        return mFileCache;
    }

    MemoryCache getMemoryCache() {
        return mMemoryCache;
    }
}
