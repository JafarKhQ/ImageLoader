package com.epam.sample.imageloader;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.epam.imageloader.ImageLoader;

/**
 * Created by jafar_qaddoumi on 5/20/15.
 */
public class SampleListActivity extends ActionBarActivity {

    ImageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        loader = ImageLoader.getInstance(this);

        ListView lsv = (ListView) findViewById(R.id.list);
        lsv.setAdapter(new SampleAdapter(this));
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
}
