package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;

public class FileViewerActivity extends ActionBarActivity {
    private FileManagerLocal fileManagerLocal;
    private String file_name = null;
    private String workspace_name = null;
    private String user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("file_name");
        workspace_name = intent.getStringExtra("workspace_name");
        user = intent.getStringExtra(WorkspaceListActivity.OWNER_KEY);

        getSupportActionBar().setTitle(file_name);

        fileManagerLocal = new FileManagerLocal(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_file) {
            Intent intent = new Intent(FileViewerActivity.this, FileEditorActivity.class);
            intent.putExtra("file_name", file_name);
            intent.putExtra("workspace_name", workspace_name);
            intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFile(){
        String fileContents = fileManagerLocal.getFileContents(file_name, workspace_name, user);
        TextView fileView = (TextView) findViewById(R.id.fileViewContents);
        fileView.setText(fileContents);
    }
}
