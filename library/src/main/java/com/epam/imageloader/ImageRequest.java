package com.epam.imageloader;

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

    private URL mUrl;
    private File mFile;

    private File mTempFile;
    private String mCacheName;

    private boolean mDiskCache;
    private boolean mMemoryCache;

    private Object mOldTag;
    private ImageView mTargetView;
    private ImageSource mImageSource;
    private int mTargetWidth, mTargetHeight;

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
        targetView.setImageBitmap(null);
        mOldTag = targetView.getTag();
        targetView.setTag(getCacheName());

        mTargetView = targetView;

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
        return (false == getCacheName().equals(mTargetView.getTag()));
    }

    void resetTag() {
        mTargetView.setTag(mOldTag);
        mOldTag = null;
    }


    private String toHexString(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);

        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }
}
