package com.example.william.gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
{
    //GridView in activity_main.xml
    GridView gridView;

    //ImageAdapter feeds images into gridView.
    ImageAdapter imageAdapter;

    //ArrayList containing the URIs for all images on the phone, in date order.
    private ArrayList<imageDetails> listOfImageDetails = new ArrayList<>();

    //Response for request for storage permission.
    private static final int REQUEST_TO_READ_EXTERNAL_STORAGE = 1;

    private LruCache<String, Bitmap> mMemoryCache;

    private class imageDetails
    {
        String path;
        int orientation;

        imageDetails(String path, int orientation)
        {
            this.path = path;
            this.orientation = orientation;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case REQUEST_TO_READ_EXTERNAL_STORAGE:
            {
                if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    //Permission Denied. Close app.
                    finish();
                }
                else
                {
                    init();
                }
            }
        }
    }

    public class ImageAdapter extends BaseAdapter
    {
        private BitmapFactory.Options pixel = new BitmapFactory.Options();

        public ImageAdapter()
        {
            super();
            pixel.inSampleSize = 8;
        }

        class imageContainer
        {
            int position;
            ImageView thumbnail;
            String path;
        }

        //Required overrides for extension of BaseAdapter
        @Override
        public Object getItem(int i)
        {
            return null;
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public int getCount()
        {
            return countImages() ;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup)
        {
            imageContainer photo;
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.photo, viewGroup, false);
                photo = new imageContainer();
                photo.thumbnail = convertView.findViewById(R.id.photo);
                convertView.setTag(photo);
            }
            else
            {
                photo = (imageContainer) convertView.getTag();
            }

            photo.position = i;
            photo.thumbnail.setImageBitmap(null);
            photo.path = listOfImageDetails.get(i).path;

            new AsyncTask<imageContainer, Void, Bitmap>()
            {
                private imageContainer photo;

                @Override
                protected Bitmap doInBackground(imageContainer... params)
                {
                    photo = params[0];

                    if (photo.position != i)
                    {
                        return null;
                    }

                    try
                    {
                        Bitmap cachedThumbnail = getBitmapFromMemCache(photo.path);
                        if (cachedThumbnail != null)
                        {
                            return cachedThumbnail;
                        }
                        else
                        {
                            Bitmap bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo.path, pixel), Resources.getSystem().getDisplayMetrics().widthPixels / 4, Resources.getSystem().getDisplayMetrics().widthPixels / 4);
                            addBitmapToMemoryCache(photo.path, bmp);
                            return bmp;
                        }
                    }
                    catch (Exception exc)
                    {
                        exc.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bmp)
                {
                    if(photo.position == i)
                    {
                        photo.thumbnail.setImageBitmap(bmp);
                        photo.thumbnail.setRotation(listOfImageDetails.get(i).orientation);
                    }
                }
            }.execute(photo);
            return convertView;
        }
    }

    public int countImages()
    {
        String[] detailsToPull = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, detailsToPull, null, null);
        return cursor.getCount();
    }

    public ArrayList<imageDetails> catalogueImages()
    {
        String dateAdded = MediaStore.Images.Media.DATE_ADDED;
        String[] detailsToPull = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, detailsToPull, null, null, dateAdded);

        ArrayList<imageDetails> detailsToReturn = new ArrayList<>();

        int imageCount = cursor.getCount();

        for(int i = 0; i < imageCount; i++)
        {
            cursor.moveToPosition(i);
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int orientationIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
            imageDetails newImage = new imageDetails(cursor.getString(dataIndex), cursor.getInt(orientationIndex));
            detailsToReturn.add(newImage);
        }
        cursor.close();
        return detailsToReturn;
    }

    public void init()
    {
        Log.i("MESSAGE", "Reached Init");

        this.listOfImageDetails = catalogueImages();
        Collections.reverse(this.listOfImageDetails);

        imageAdapter = new ImageAdapter();
        gridView = findViewById(R.id.photoGrid);
        gridView.setAdapter(imageAdapter);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_TO_READ_EXTERNAL_STORAGE);
                return;
            }
        }
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("position", gridView.getFirstVisiblePosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        gridView.smoothScrollToPosition(savedInstanceState.getInt("position"));

        this.listOfImageDetails = catalogueImages();
        Collections.reverse(this.listOfImageDetails);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromMemCache(key) == null)
        {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key)
    {
        return mMemoryCache.get(key);
    }
}