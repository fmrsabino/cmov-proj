package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class ViewersActivity extends ActionBarActivity {

    private String ws_name;
    private ListView listView ;
    private EditText viewer;
    private List<String> viewers;
    private ArrayAdapter<String> adapter;
    private Workspace ws;
    WorkspaceManager wsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewers);

        listView = (ListView) findViewById(R.id.viewers_list);
        viewer = (EditText) findViewById(R.id.viewer);
        TextView tv = (TextView) findViewById(R.id.workspace_name);

        wsManager = new WorkspaceManager(getApplicationContext());

        Intent intent = getIntent();
        ws_name = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
        tv.setText(ws_name + " Viewers");

        ws = wsManager.retrieveWorkspace(ws_name);

        viewers = new ArrayList<>(ws.getUsers());

        adapter = new ArrayAdapter<>(this,
                R.layout.activity_viewers_list_item, R.id.selected_item, viewers);

        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_browse_workspace_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void deleteSelectedItems() {
        AirDeskDbHelper dbHelper = new AirDeskDbHelper(getApplicationContext());
        SparseBooleanArray checked = listView.getCheckedItemPositions();

        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                DatabaseAPI.deleteViewer(dbHelper, adapter.getItem(i), ws_name);
            }
        }

        viewers.clear();
        ws = wsManager.retrieveWorkspace(ws_name);
        viewers.addAll(ws.getUsers());
        adapter.notifyDataSetChanged();
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
        if(viewer != null) {
            WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());
            Intent intent = getIntent();
            ws_name = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
            String v = viewer.getText().toString();

            if(!TextUtils.isEmpty(v)) {
                viewers.add(v);

                wsManager.addViewer(v, ws_name);

                adapter.notifyDataSetChanged();
                viewer.setText(null);
            }
        }
    }
}
