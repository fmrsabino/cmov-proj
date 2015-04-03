package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.WorkspacesListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class WorkspaceListActivity extends ActionBarActivity {

    public final static String WORKSPACE_NAME_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    public final static String ACCESS_KEY = "pt.ulisboa.tecnico.cmov.airdesk.ACCESS";
    private String repo;
    private ListView listView;
    private List<WorkspacesListAdapter.Content> directories = new ArrayList<>();
    private WorkspacesListAdapter listAdapter;
    private String selectedWorkspace;
    private EditText tagTxt;
    private WorkspaceManager wsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_list);

        Intent intent = getIntent();
        repo = intent.getStringExtra(WelcomeActivity.WORKSPACE_ACCESS_KEY);
        wsManager = new WorkspaceManager(getApplicationContext());
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
                    String access = repo;
                    String message = selectedWorkspace;
                    intent.putExtra(ACCESS_KEY, access);
                    intent.putExtra(WORKSPACE_NAME_KEY, message);

                    startActivity(intent);
                }}});

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
                String workspaceName = listAdapter.getItem(i).getWs_name();

                if(!wsManager.deleteOwnedWorkspace(workspaceName)){
                    new AlertDialog.Builder(this)
                            .setTitle("Database Error")
                            .setMessage("Please try again")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                    return;
                }
                new FileManagerLocal(this).deleteDirectory(workspaceName);
            }
        }

        populateWorkspaceList();
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

        listAdapter.notifyDataSetChanged();


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
                if(TextUtils.equals(repo, "owned")) {
                    Intent intent = new Intent(this, CreateWorkspaceActivity.class);
                    startActivity(intent);
                }
                else if(TextUtils.equals(repo, "foreign")){
                   tagTxt = new EditText(this);
                    tagTxt.setHint("Tag1, Tag2, ...");
                    new AlertDialog.Builder(this)
                            .setTitle("Subscribe")
                            .setMessage("Please enter workspace tags, separated by a comma")
                            .setView(tagTxt)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String tags = tagTxt.getText().toString();
                                    subscribeWorkspaces(tags);
                                }
                            }).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void subscribeWorkspaces(String tags) {
        String[] tokens = tags.split("\\,");
        for(String s : tokens)
        Log.d("TOKENS", s);
    }


}