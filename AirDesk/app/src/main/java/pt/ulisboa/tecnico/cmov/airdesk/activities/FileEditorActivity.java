package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public class FileEditorActivity extends ActionBarActivity {

    private FileManagerLocal fileManagerLocal;
    private String file_name = null;
    private String workspace_name = null;
    private WorkspaceManager wsManager;
    private String user;

    private long initialFileSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_editor);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("file_name");
        workspace_name = intent.getStringExtra("workspace_name");
        user = intent.getStringExtra(WorkspaceListActivity.OWNER_KEY);

        getSupportActionBar().setTitle(file_name);

        fileManagerLocal = new FileManagerLocal(this);
        wsManager = new WorkspaceManager(getApplicationContext());


        initialFileSize = fileManagerLocal.getFileSize(file_name, workspace_name, user);

        String fileContents = fileManagerLocal.getFileContents(file_name, workspace_name, user);
        EditText fileView = (EditText) findViewById(R.id.fileContents);
        fileView.setText(fileContents);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_file) {
            EditText fileView = (EditText) findViewById(R.id.fileContents);

            byte[] fileBytes;
            try {
                fileBytes = fileView.getText().toString().getBytes("UTF-8");
                long finalFileSize = fileBytes.length;

                Log.d("Editor", "Final size is: " + finalFileSize);
                Log.d("Editor", "Initial size is: " + initialFileSize);

                long updatedBytes = finalFileSize - initialFileSize;
                long currentQuota = wsManager.getCurrentWorkspaceQuota(workspace_name, user);

                if (currentQuota - updatedBytes < 0) {
                    Toast.makeText(this, "Quota Exceeded: Couldn't save file", Toast.LENGTH_LONG).show();
                    return true;
                }

                wsManager.updateWorkspaceQuota(workspace_name, -updatedBytes, user);
                fileManagerLocal.saveFileContents(file_name, workspace_name, user, fileView.getText().toString());

                Toast.makeText(this, "Saved File", Toast.LENGTH_SHORT).show();
                finish();
                return true;

            } catch (UnsupportedEncodingException e) {

            }
        }
        return super.onOptionsItemSelected(item);
    }
}
