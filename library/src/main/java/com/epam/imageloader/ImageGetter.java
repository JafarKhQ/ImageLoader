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
        InputStream is = null;
        File resultFile = null;
        Bitmap resultBitmap = null;
        final ImageTarget imageTarget = mRequest.getImageTarget();

        try {
            if (mRequest.isRecycled()) {
                return;
            }

            if (ImageTarget.FILE != imageTarget) {
                resultBitmap = fromCache();
                if (null != resultBitmap) {
                    /*
                     * Bitmap founded in the Cache, go to finally part
                     */
                    return;
                }
            }

            if (mRequest.isRecycled()) {
                return;
            }

            /*
             * Load data from URL or File into InputStream
             */
            is = streamTheRequest(mRequest);
            if (mRequest.isRecycled()) {
                return;
            }

            if (ImageTarget.FILE == imageTarget) {
                /*
                 * Save the InputStream into File
                 */
                resultFile = fileTheStream(is, mRequest);
            } else if (ImageTarget.VIEW == imageTarget || ImageTarget.MEMORY == imageTarget) {
                /*
                 * Save the InputStream into Bitmap, resize and cache.
                 */
                final File tempCacheFile = mFileCache.add(is, mRequest.getCacheName());
                if (mRequest.isRecycled()) {
                    if (false == mRequest.isDiskCache()) {
                        tempCacheFile.delete();
                    }

                    return;
                }

                if (null == tempCacheFile) {
                    resultBitmap = bitmapTheStream(is);
                } else {
                    resultBitmap = bitmapTheFile(tempCacheFile, mRequest);

                    if (false == mRequest.isDiskCache()) {
                        tempCacheFile.delete();
                    }
                }

                if (true == mRequest.isMemoryCache()) {
                    mMemoryCache.add(resultBitmap, mRequest.getCacheName());
                }

                if (mRequest.isRecycled()) {
                    resultBitmap = null;
                    return;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "run: " + e.getMessage());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            if (ImageTarget.FILE == imageTarget) {
                ImageLoader.sUiHandler.post(new FileNotifier(resultFile, mRequest.getFileListener()));
            } else if (ImageTarget.VIEW == imageTarget) {
                ImageLoader.sUiHandler.post(new ImageSetter(resultBitmap, mRequest.getTargetView()));
            } else if (ImageTarget.MEMORY == imageTarget) {
                ImageLoader.sUiHandler.post(new BitmapNotifier(resultBitmap, mRequest.getBitmapListener()));
            }

            mRequest.resetTag();
        }
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
            if (null != imageFile) {
                final Bitmap result = bitmapTheFile(imageFile, mRequest, false);
                if (null != result) {
                    Log.i(TAG, "Image loaded form Disk cache");
                    return result;
                }
            }
        }

        Log.i(TAG, "Image not available in the cache");
        return null;
    }

    private static InputStream streamTheRequest(ImageRequest request) throws IOException {
        InputStream is = null;
        ImageSource source = request.getImageSource();

        if (ImageSource.WEB == source) {
            Log.i(TAG, "Downloading image " + request.getUrl().toString());
            HttpURLConnection httpConnection = (HttpURLConnection) request.getUrl().openConnection();

            is = httpConnection.getInputStream();
        } else if (ImageSource.LOCAL == source) {
            File imageFile = request.getTempFile();
            if (null == imageFile) {
                imageFile = request.getFile();
            }

            is = new FileInputStream(imageFile);
        }

        return is;
    }

    private static Bitmap bitmapTheStream(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }

    private static Bitmap bitmapTheFile(File file, ImageRequest request) {
        return bitmapTheFile(file, request, true);
    }

    private static Bitmap bitmapTheFile(File file, ImageRequest request, boolean useSampleSize) {
        BitmapFactory.Options options = null;

        if (true == useSampleSize) {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            ImageUtils.prepareInSampleSize(options,
                    request.getTargetWidth(), request.getTargetHeight());
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private static File fileTheStream(InputStream is, ImageRequest request) throws IOException {
        OutputStream os = null;
        final File outFile = request.getTargetFile();

        try {
            os = new FileOutputStream(outFile);
            StreamUtils.copyStream(is, os);
        } finally {
            if (null != os) {
                os.close();
            }
        }

        return outFile;
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

    private static class BitmapNotifier implements Runnable {

        private Bitmap bm;
        private ImageRequest.OnLoadBitmapListener listener;

        public BitmapNotifier(Bitmap bm, ImageRequest.OnLoadBitmapListener listener) {
            this.bm = bm;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (null == bm) {
                listener.onBitmapFailed();
            } else {
                listener.onBitmapSuccess(bm);
            }
        }
    }

    private static class FileNotifier implements Runnable {

        private File file;
        private ImageRequest.OnLoadFileListener listener;

        public FileNotifier(File file, ImageRequest.OnLoadFileListener listener) {
            this.file = file;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (null == file) {
                listener.onFileFailed();
            } else {
                listener.onFileSuccess(file);
            }
        }
    }
}
