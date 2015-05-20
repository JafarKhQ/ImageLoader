package com.epam.sample.imageloader;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.epam.imageloader.ImageLoader;


public class MainActivity extends ActionBarActivity {


    static final String BASE = "http://i.imgur.com/";
    static final String EXT = ".jpg";
    static final String[] URLS = {
            BASE + "CqmBjo5" + EXT, BASE + "zkaAooq" + EXT, BASE + "0gqnEaY" + EXT,
            BASE + "9gbQ7YR" + EXT, BASE + "aFhEEby" + EXT, BASE + "0E2tgV7" + EXT,
            BASE + "P5JLfjk" + EXT, BASE + "nz67a4F" + EXT, BASE + "dFH34N5" + EXT,
            BASE + "FI49ftb" + EXT, BASE + "DvpvklR" + EXT, BASE + "DNKnbG8" + EXT,
            BASE + "yAdbrLp" + EXT, BASE + "55w5Km7" + EXT, BASE + "NIwNTMR" + EXT,
            BASE + "DAl0KB8" + EXT, BASE + "xZLIYFV" + EXT, BASE + "HvTyeh3" + EXT,
            BASE + "Ig9oHCM" + EXT, BASE + "7GUv9qa" + EXT, BASE + "i5vXmXp" + EXT,
            BASE + "glyvuXg" + EXT, BASE + "u6JF6JZ" + EXT, BASE + "ExwR7ap" + EXT,
            BASE + "Q54zMKT" + EXT, BASE + "9t6hLbm" + EXT, BASE + "F8n3Ic6" + EXT,
            BASE + "P5ZRSvT" + EXT, BASE + "jbemFzr" + EXT, BASE + "8B7haIK" + EXT,
            BASE + "aSeTYQr" + EXT, BASE + "OKvWoTh" + EXT, BASE + "zD3gT4Z" + EXT,
            BASE + "z77CaIt" + EXT,
    };

    ImageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loader = ImageLoader.getInstance(this);


        findViewById(R.id.b1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("default");
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i1));
            }
        });

        findViewById(R.id.b2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("no cache");
                loader.from(URLS[1]).diskCache(false).memoryCache(false).into((ImageView) findViewById(R.id.i2));
            }
        });

        findViewById(R.id.b3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("disk cache");
                loader.from(URLS[2]).diskCache(true).memoryCache(false).into((ImageView) findViewById(R.id.i3));
            }
        });

        findViewById(R.id.b4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("memory cache");
                loader.from(URLS[3]).diskCache(false).memoryCache(true).into((ImageView) findViewById(R.id.i4));
            }
        });

        findViewById(R.id.b5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i5));
            }
        });

        findViewById(R.id.b6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i6));
            }
        });

        findViewById(R.id.b7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i7));
            }
        });

        findViewById(R.id.b8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i8));
            }
        });

        findViewById(R.id.b9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i9));
            }
        });

        findViewById(R.id.b10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i10));
            }
        });

        findViewById(R.id.b11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.from(URLS[0]).into((ImageView) findViewById(R.id.i11));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mem_clear:
                loader.clearMemoryCache();
                return true;

            case R.id.file_clear:
                loader.clearDiskCache();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
