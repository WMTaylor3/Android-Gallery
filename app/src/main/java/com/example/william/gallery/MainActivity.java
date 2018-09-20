package com.example.william.gallery;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;


public class MainActivity extends AppCompatActivity {

    public class imageAdaptor extends BaseAdapter
    {
        class imageContainer
        {
            int position;
            ImageView image;
        }

        //Required overrides for extension of BaseAdaptor
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
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            imageContainer photo;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.photo, viewGroup, false);
                photo = new imageContainer();
                photo.image = convertView.findViewById(R.id.photo);
                convertView.setTag(photo);
            } else {
                photo = (imageContainer) convertView.getTag();
            }
            photo.position = i;
            photo.image.setImageBitmap(null);
            new AsyncTask<imageContainer, Void, Bitmap>()
            {

            }
            return convertView;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}