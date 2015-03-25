package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;

public class FileEditorActivity extends ActionBarActivity {

    private String file_name = null;
    private String workspace_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_editor);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("file_name");
        workspace_name = intent.getStringExtra("workspace_name");

        getSupportActionBar().setTitle(file_name);

        FileManagerLocal manager = new FileManagerLocal(this);
        String fileContents = manager.getFileContents(file_name, workspace_name);

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
            FileManagerLocal manager = new FileManagerLocal(this);
            manager.saveFileContents(file_name, workspace_name, fileView.getText().toString());
            Toast.makeText(this, "Saved File", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
