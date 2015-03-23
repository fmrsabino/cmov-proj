package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;


public class FileManagerTestActivity extends ActionBarActivity {

    private FileManagerLocal fileManagerLocal;
    private List<String> files = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private static final String WORKSPACEID = "TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager_test);

        fileManagerLocal = new FileManagerLocal(getApplicationContext());
        if (fileManagerLocal.createFolder(WORKSPACEID)) {
            Log.i("TEST", "DIRECTORY CREATED");
        } else {
            Log.e("TEST", "FAILED DIRECTORY CREATION");
        }
        listView = (ListView) findViewById(R.id.filesList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);
        refreshListFiles();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_manager_test, menu);
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

    public void createFile(View view) {
        fileManagerLocal = new FileManagerLocal(this);
        EditText et = (EditText) findViewById(R.id.addFile);
        String text = et.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (fileManagerLocal.createFile(text, WORKSPACEID)) {
                Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: couldn't create file", Toast.LENGTH_SHORT).show();
            }
            et.setText("");
            refreshListFiles();
        }
    }

    public void removeFile(View view) {
        Log.d("Main", "removeFile()");
        fileManagerLocal = new FileManagerLocal(this);
        EditText et = (EditText) findViewById(R.id.removeFile);
        String text = et.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (fileManagerLocal.removeFile(text, WORKSPACEID)) {
                Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: couldn't remove file", Toast.LENGTH_SHORT).show();
            }

            et.setText("");
        }
        refreshListFiles();
    }

    public void refreshListFiles() {
        files.clear();
        files.addAll(fileManagerLocal.getFilesNames(WORKSPACEID));
        adapter.notifyDataSetChanged();
    }

    public void format(View view) {
        fileManagerLocal.formatDirectory(WORKSPACEID);
        refreshListFiles();
    }
}
