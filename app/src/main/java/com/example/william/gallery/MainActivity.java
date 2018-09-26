package com.example.william.gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //GridView in activity_main.xml
    GridView gridView;

    //ImageAdapter feeds images into gridView.
    ImageAdapter imageAdapter;

    //ArrayList containing the URIs for all images on the phone, in date order.
    private ArrayList<String> listOfURIs = new ArrayList<>();

    //Response for request for storage permission.
    private static final int REQUEST_TO_READ_EXTERNAL_STORAGE = 1;

    public class ImageAdapter extends BaseAdapter
    {
        private BitmapFactory.Options pixel = new BitmapFactory.Options();

        public ImageAdapter()
        {
            super();
            pixel.inSampleSize = 6;
        }

        class imageContainer {
            int position;
            ImageView thumbnail;
            String path;
        }

        //Required overrides for extension of BaseAdapter
        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return 100; //TODO: Fix this;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup)
        {
            Log.i("Message", "Enter Get View");
            imageContainer photo;
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.photo, viewGroup, false);
                photo = new imageContainer();
                photo.thumbnail = convertView.findViewById(R.id.photo);
                convertView.setTag(photo);
                return convertView;
            }
            else
            {
                photo = (imageContainer) convertView.getTag();
                photo.position = i;
                photo.thumbnail.setImageBitmap(null);
                photo.path = listOfURIs.get(i);
            }
            new AsyncTask<imageContainer, Void, Bitmap>()
            {
                private imageContainer photo;

                @Override
                protected Bitmap doInBackground(imageContainer... params)
                {
                    photo = params[0];
                    Bitmap bmp = null;
                    try
                    {
                        if (photo.position != i)
                        {
                            return null;
                        }
                        bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo.path, pixel), photo.thumbnail.getWidth(), photo.thumbnail.getHeight());
                    }
                    catch (Exception exc)
                    {
                        exc.printStackTrace();
                    }
                    return bmp;
                }

                @Override
                protected void onPostExecute(Bitmap bmp)
                {
                    if(photo.position == i)
                    {
                        photo.thumbnail.setImageBitmap(bmp);
                    }
                }
            }.execute(photo);
            return convertView;
        }
    }

    public ArrayList<String> indexURIs()
    {
        String dateAdded = MediaStore.Images.Media.DATE_ADDED;
        String[] detailsToPull = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, detailsToPull, null, null, dateAdded);

        ArrayList<String> pathsToReturn = new ArrayList<>();
        ArrayList<String> paths = new ArrayList<>();

        int imageCount = cursor.getCount();

        for(int i = 0; i < imageCount; i++)
        {
            cursor.moveToPosition(i);
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            pathsToReturn.add(cursor.getString(index));
        }
        cursor.close();
        return pathsToReturn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("Message", "Load");

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_TO_READ_EXTERNAL_STORAGE);
            }
        }

        Log.i("Message", "Permission");

        this.listOfURIs = indexURIs();

        imageAdapter = new ImageAdapter();
        gridView = findViewById(R.id.photoGrid);
        gridView.setAdapter(imageAdapter);
    }
}