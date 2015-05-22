package com.epam.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;


class ImageGetter implements Runnable {
    private static final String TAG = "ImageGetter";

    private ImageRequest mRequest;

    private FileCache mFileCache;
    private MemoryCache mMemoryCache;

    ImageGetter(ImageRequest request) {
        mRequest = request;

        mFileCache = ImageLoader.sInstance.getFileCache();
        mMemoryCache = ImageLoader.sInstance.getMemoryCache();
    }

    @Override
    public void run() {
        if (null != mRequest.getTargetView()) {
            loadToView();
        } else if (null != mRequest.getTargetFile()) {
            loadToFile();
        }
    }

    private void loadToFile() {
        ImageSource source = mRequest.getImageSource();
        switch (source) {
            case WEB:
                downloadFile();
                break;

            case LOCAL:
                loadFile();
                break;
        }
    }

    private void loadToView() {
        if (mRequest.isRecycled()) {
            return;
        }

        Bitmap result = fromCache();
        if (mRequest.isRecycled()) {
            return;
        }

        if (null == result) {
            ImageSource source = mRequest.getImageSource();
            switch (source) {
                case WEB:
                    result = downloadImage();
                    break;

                case LOCAL:
                    result = loadImage();
                    break;
            }
        }

        if (null != result) {
            if (true == mRequest.isMemoryCache() /*&& false == mMemoryCache.contain(mRequest.getCacheName())*/) {
                mMemoryCache.add(result, mRequest.getCacheName());
            }

            if (mRequest.isRecycled()) {
                return;
            }

            ImageLoader.sUiHandler.post(new ImageSetter(result, mRequest.getTargetView()));
        }

        mRequest.resetTag();
    }

    private Bitmap fromCache() {
        if (true == mRequest.isMemoryCache()) {
            final Bitmap result = mMemoryCache.get(mRequest.getCacheName());
            if (null != result) {
                Log.i(TAG, "Image loaded form Memory cache");
                return result;
            }
        }

        if (true == mRequest.isDiskCache()) {
            File imageFile = mFileCache.get(mRequest.getCacheName());
            if (null == imageFile) {
                return null;
            }

            mRequest.setTempFile(imageFile);
            final Bitmap result = loadImage();
            mRequest.setTempFile(null);

            if (null != result) {
                Log.i(TAG, "Image loaded form Disk cache");
                return result;
            }
        }

        Log.i(TAG, "Image not available in the cache");
        return null;
    }

    private Bitmap downloadImage() {
        Bitmap bitmap = null;
        HttpURLConnection httpConnection = null;

        try {
            Log.i(TAG, "Downloading image " + mRequest.getUrl().toString());
            httpConnection = (HttpURLConnection) mRequest.getUrl().openConnection();

            InputStream is = httpConnection.getInputStream();
            File imageFile = mFileCache.add(is, mRequest.getCacheName());
            if (null == imageFile) {
                // Cant create temp file/cache file for the image, load it from stream
                bitmap = BitmapFactory.decodeStream(is);
            } else {
                mRequest.setTempFile(imageFile);
                bitmap = loadImage();
                if (false == mRequest.isDiskCache()) {
                    mRequest.deleteTemp();
                }
                mRequest.setTempFile(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "downloadImage: " + e.getMessage());
        } finally {
            if (null != httpConnection) {
                httpConnection.disconnect();
            }
        }

        return bitmap;
    }

    private void downloadFile() {
        OutputStream os = null;
        HttpURLConnection httpConnection = null;

        try {
            Log.i(TAG, "Downloading image " + mRequest.getUrl().toString());
            httpConnection = (HttpURLConnection) mRequest.getUrl().openConnection();

            InputStream is = httpConnection.getInputStream();
            os = new FileOutputStream(mRequest.getTargetFile());
            StreamUtils.copyStream(is, os);
        } catch (Exception e) {
            Log.e(TAG, "downloadImage: " + e.getMessage());
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            if (null != httpConnection) {
                httpConnection.disconnect();
            }
        }
    }

    private Bitmap loadImage() {
        File imageFile = mRequest.getTempFile();
        if (null == imageFile) {
            imageFile = mRequest.getFile();
        }

        Log.i(TAG, "Loading image " + imageFile.toString());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        ImageUtils.prepareInSampleSize(options,
                mRequest.getTargetWidth(), mRequest.getTargetHeight());

        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
    }

    private void loadFile() {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(mRequest.getFile());
            os = new FileOutputStream(mRequest.getTargetFile());
            StreamUtils.copyStream(is, os);
        } catch (Exception e) {
            Log.e(TAG, "loadFile: " + e.getMessage());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static class ImageSetter implements Runnable {

        private Bitmap bm;
        private ImageView iv;

        public ImageSetter(Bitmap bm, ImageView iv) {
            this.bm = bm;
            this.iv = iv;
        }

        @Override
        public void run() {
            iv.setImageBitmap(bm);
        }
    }
}
