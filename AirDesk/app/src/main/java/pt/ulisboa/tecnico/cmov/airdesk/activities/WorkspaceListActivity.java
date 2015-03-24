package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.WorkspacesListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class WorkspaceListActivity extends ActionBarActivity {

    private static final String TAG = "WorkspaceListActivity";

    public final static String WORKSPACE_NAME_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    private String repo;
    private ListView listView;
    private List<WorkspacesListAdapter.Content> directories = new ArrayList<>();
    private WorkspacesListAdapter listAdapter;
    private String selectedWorkspace;
    private FileManagerLocal fileManagerLocal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_list);

        fileManagerLocal = new FileManagerLocal(this);
        Intent intent = getIntent();
        repo = intent.getStringExtra(WelcomeActivity.WORKSPACE_ACCESS_KEY);
        listView = (ListView) findViewById(R.id.workspace_list);
        listAdapter = new WorkspacesListAdapter(this, directories);
        listView.setAdapter(listAdapter);
        // refreshDirectoryListing();

        populateWorkspaceList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.workspace);

                if (textView != null) {
                    selectedWorkspace = textView.getText().toString();

                    //Transfer control to BrowseWorkspace
                    Intent intent = new Intent(WorkspaceListActivity.this, BrowseWorkspaceActivity.class);
                    String message = selectedWorkspace;
                    intent.putExtra(WORKSPACE_NAME_KEY, message);
                    startActivity(intent);
                }}});
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        populateWorkspaceList();
    }

    private void populateWorkspaceList() {
        directories.clear();
        WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());
        List<Workspace> wsList = new ArrayList<>();

        if(TextUtils.equals(repo, "owned"))
            wsList = wsManager.retrieveOwnedWorkspaces();
        else if(TextUtils.equals(repo, "foreign"))
            wsList= wsManager.retrieveForeignWorkspaces();

        for (Workspace w : wsList) {
            directories.add(new WorkspacesListAdapter.Content(w.getName(), Integer.toString(w.getQuota())));
        }

        //WorkspacesListAdapter adapter = new WorkspacesListAdapter(this, directories);
        listAdapter.notifyDataSetChanged();
        //listView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_workspace:
                Intent intent = new Intent(this, CreateWorkspaceActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshDirectoryListing() {
        directories.clear();
        for (String name : fileManagerLocal.getWorkspaces()) {
            directories.add(new WorkspacesListAdapter.Content(name, "42%"));
        }
        listAdapter.notifyDataSetChanged();
    }
}