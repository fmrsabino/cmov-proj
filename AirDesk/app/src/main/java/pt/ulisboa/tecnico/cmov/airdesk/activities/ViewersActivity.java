package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class ViewersActivity extends ActionBarActivity {

    private TextView tv;
    private String ws_name;
    private ListView listView ;
    private EditText viewer;
    private List<String> viewers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewers);

        listView = (ListView) findViewById(R.id.viewers_list);
        viewer = (EditText) findViewById(R.id.viewer);
        tv = (TextView) findViewById(R.id.workspace_name);

        WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());

        Intent intent = getIntent();
        ws_name = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
        tv.setText(ws_name + " Viewers");

        Workspace ws = wsManager.retrieveWorkspace(ws_name);

        viewers = new ArrayList<>(ws.getUsers());

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
        WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());
        Intent intent = getIntent();
        ws_name = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
        String v = viewer.getText().toString();

        viewers.add(v);

        wsManager.addViewer(v, ws_name);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
}
