package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.R;


public class ViewersActivity extends ActionBarActivity {

    private TextView tv;
    private String ws_name;
    private ListView listView ;
    private EditText viewer;
    private ArrayList<String> viewers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewers);

        listView = (ListView) findViewById(R.id.viewers_list);
        viewer = (EditText) findViewById(R.id.viewer);
        tv = (TextView) findViewById(R.id.workspace_name);
        viewers = new ArrayList<String>();

        Intent intent = getIntent();
        ws_name = intent.getStringExtra(BrowseWorkspaceActivity.workspace_name);
        tv.setText(ws_name + " Viewers");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        listView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void inviteUser(View view) {
        String v = viewer.getText().toString();

        viewers.add(v);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
}
