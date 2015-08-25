package com.opentrain.app.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.opentrain.app.adapter.LogAdapter;
import com.opentrain.app.utils.Logger;
import com.opentrain.app.R;

/**
 * Created by noam on 07/06/15.
 */
public class LogActivity extends AppCompatActivity {

    LogAdapter logAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        ListView listView;
        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(android.R.id.empty));

        logAdapter = new LogAdapter(this);
        listView.setAdapter(logAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        if (logAdapter != null) {
            logAdapter.setItems(Logger.getLogItems());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh_log_list) {
            refreshList();
            return true;
        } else if (id == R.id.action_clear_log_list) {
            clearList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearList() {
        Logger.clearItems();
        refreshList();
    }
}
