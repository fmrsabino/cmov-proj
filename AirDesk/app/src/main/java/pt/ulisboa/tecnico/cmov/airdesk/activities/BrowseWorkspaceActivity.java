package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.dialogs.CreateFileDialogFragment;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;


public class BrowseWorkspaceActivity extends ActionBarActivity
        implements CreateFileDialogFragment.CreateFileDialogListener {

    public final static String workspace_name = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";

    private TextView tv;
    private GridView gv;
    private String ws_name;
    private List<String> files = new ArrayList<>();
    private ArrayAdapter<String> gridAdapter;
    private FileManagerLocal fileManager = null;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_workspace);

        fileManager = new FileManagerLocal(getApplicationContext());

        tv = (TextView) findViewById(R.id.workspace_name);
        Intent intent = getIntent();

        ws_name = intent.getStringExtra(WorkspaceListActivity.workspace_name);
        tv.setText(ws_name);

        files.addAll(Arrays.asList(fileManager.getFilesNames()));
        gv = (GridView) findViewById(R.id.workspace_files);
        gridAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, files);

        gv.setAdapter(gridAdapter);

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
            intent.putExtra(workspace_name, this.ws_name);
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
        files.addAll(Arrays.asList(fileManager.getFilesNames()));
        gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        fileManager.createFile(((CreateFileDialogFragment) dialog).getFileName());
        refreshFilesList();
        counter++;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
