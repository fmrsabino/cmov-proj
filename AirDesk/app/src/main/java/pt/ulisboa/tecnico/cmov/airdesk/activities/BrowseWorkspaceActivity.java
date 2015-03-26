package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.DialogFragment;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.dialogs.CreateFileDialogFragment;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;


public class BrowseWorkspaceActivity extends ActionBarActivity
        implements CreateFileDialogFragment.CreateFileDialogListener {


    private GridView gridView;
    private String workspaceName;
    private String access;
    private List<String> files = new ArrayList<>();
    private ArrayAdapter<String> gridAdapter;
    private FileManagerLocal fileManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_workspace);

        fileManager = new FileManagerLocal(getApplicationContext());

        Intent intent = getIntent();
        access = intent.getStringExtra(WorkspaceListActivity.ACCESS_KEY);
        workspaceName = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
        getSupportActionBar().setTitle(workspaceName);

        files.addAll(fileManager.getFilesNames(workspaceName));
        gridView = (GridView) findViewById(R.id.workspace_files);
        gridAdapter = new ArrayAdapter<>(this,
                R.layout.activity_browse_workspace_grid_item, R.id.text1, files);

        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(BrowseWorkspaceActivity.this, FileEditorActivity.class);
                intent.putExtra("file_name", gridAdapter.getItem(position));
                intent.putExtra("workspace_name", workspaceName);
                startActivity(intent);
            }
        });

        if(TextUtils.equals(access, "owned")) {
            gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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
        refreshFilesList();

    }

    private void deleteSelectedItems() {
        SparseBooleanArray checked = gridView.getCheckedItemPositions();
        for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                fileManager.deleteFile(gridAdapter.getItem(i), workspaceName);
            }
        }
        refreshFilesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_workspace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case (R.id.action_settings):
                return true;
            case (R.id.action_add_file):
                showCreateFileDialog();
                return true;
        }

        if (id == R.id.action_viewers){
            Intent intent = new Intent(this, ViewersActivity.class);
            intent.putExtra(WorkspaceListActivity.ACCESS_KEY, this.access);
            intent.putExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY, this.workspaceName);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCreateFileDialog() {
        CreateFileDialogFragment dialog = new CreateFileDialogFragment();
        dialog.show(getFragmentManager(), "CreateFileDialogFragment");
    }

    private void refreshFilesList() {
        files.clear();
        files.addAll(fileManager.getFilesNames(workspaceName));
        gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        fileManager.createFile(((CreateFileDialogFragment) dialog).getFileName(), workspaceName);
        refreshFilesList();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {}
}
