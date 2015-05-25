package com.epam.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ImageRequest {
    private static final String HEXES = "0123456789ABCDEF";

    private static MessageDigest sDigest;

    static {
        try {
            sDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            sDigest = null;
        }
    }

    public interface OnLoadBitmapListener {
        void onBitmapSuccess(String where, Bitmap bitmap);

        void onBitmapFailed(String where);
    }

    public interface OnLoadFileListener {
        void onFileSuccess(String where, File file);

        void onFileFailed(String where);
    }

    private URL mUrl;
    private File mFile;

    private File mTempFile;
    private String mCacheName;

    private boolean mDiskCache;
    private boolean mMemoryCache;

    private Object mOldTag;
    private File mTargetFile;
    private ImageView mTargetView;

    private ImageSource mImageSource;
    private int mTargetWidth, mTargetHeight;

    private OnLoadBitmapListener mBitmapListener;
    private OnLoadFileListener mFileListener;

    ImageRequest(URL url) {
        mUrl = url;
        mImageSource = ImageSource.WEB;

        init();
    }

    ImageRequest(File file) {
        mFile = file;
        mImageSource = ImageSource.LOCAL;

        init();
    }

    private void init() {
        if (null != mUrl) {
            try {
                byte[] sha = sDigest.digest(mUrl.toString().getBytes("UTF-8"));
                mCacheName = toHexString(sha);
            } catch (Exception e) {
                mCacheName = String.valueOf(mUrl.hashCode());
            }
        } else if (null != mFile) {
            try {
                byte[] sha = sDigest.digest(mFile.getAbsolutePath().getBytes("UTF-8"));
                mCacheName = toHexString(sha);
            } catch (UnsupportedEncodingException e) {
                mCacheName = String.valueOf(mFile.hashCode());
            }
        }

        mDiskCache = ImageLoader.sInstance.isDiskCache();
        mMemoryCache = ImageLoader.sInstance.isMemoryCache();
        mTargetWidth = mTargetHeight = Integer.MAX_VALUE;
    }

    public ImageRequest diskCache(boolean diskCache) {
        mDiskCache = diskCache;

        return this;
    }

    public ImageRequest memoryCache(boolean memoryCache) {
        mMemoryCache = memoryCache;

        return this;
    }

    public ImageRequest resize(int targetWidth, int targetHeight) {
        if (targetHeight < 0 && targetWidth < 0) {
            throw new IllegalArgumentException("One of targetWidth and targetHeight must be larger than -1");
        }

        if (targetWidth > 0) {
            mTargetWidth = targetWidth;
        }
        if (targetHeight > 0) {
            mTargetHeight = targetHeight;
        }

        return this;
    }

    public void into(ImageView targetView) {
        if (null == targetView) {
            throw new NullPointerException("null == targetView");
        }

        targetView.setImageBitmap(null);
        mOldTag = targetView.getTag();
        targetView.setTag(getCacheName());

        mTargetView = targetView;

        ImageLoader.sExecutorService.submit(new ImageGetter(this));
    }

    public void into(File file, OnLoadFileListener fileListener) {
        if (null == file) {
            throw new NullPointerException("null == file");
        }

        mTargetFile = file;
        mFileListener = fileListener;
        ImageLoader.sExecutorService.submit(new ImageGetter(this));
    }

    void setTempFile(File tempFile) {
        mTempFile = tempFile;
    }

    File getTempFile() {
        return mTempFile;
    }

    URL getUrl() {
        return mUrl;
    }

    File getFile() {
        return mFile;
    }

    ImageView getTargetView() {
        return mTargetView;
    }

    File getTargetFile() {
        return mTargetFile;
    }

    ImageSource getImageSource() {
        return mImageSource;
    }

    int getTargetWidth() {
        return mTargetWidth;
    }

    int getTargetHeight() {
        return mTargetHeight;
    }

    boolean isDiskCache() {
        return mDiskCache;
    }

    boolean isMemoryCache() {
        return mMemoryCache;
    }

    void deleteTemp() {
        if (null != mTempFile) {
            mTempFile.delete();
            mTempFile = null;
        }
    }

    String getCacheName() {
        return mCacheName;
    }

    boolean isRecycled() {
        if (null == mTargetView) {
            return false;
        }

        return (false == getCacheName().equals(mTargetView.getTag()));
    }

    void resetTag() {
        if (null == mTargetView) {
            return;
        }

        mTargetView.setTag(mOldTag);
        mOldTag = null;
    }

    public OnLoadFileListener getFileListener() {
        return mFileListener;
    }

    public OnLoadBitmapListener getBitmapListener() {
        return mBitmapListener;
    }

    private String toHexString(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);

        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }
}
