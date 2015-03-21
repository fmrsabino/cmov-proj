package pt.ulisboa.tecnico.cmov.airdesk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;


public class BrowseWorkspace extends ActionBarActivity {

    private TextView tv;
    private GridView gv;
    private ArrayList<String> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_workspace);

        tv = (TextView) findViewById(R.id.workspace_name);
        Intent intent = getIntent();
        String workspace_name = intent.getStringExtra(WorkspaceList.workspace_name);
        tv.setText(workspace_name);

        gv = (GridView) findViewById(R.id.workspace_files);

        //populate with your spaces
        files.add("ficheiro fixe");
        files.add("ficheiro cool");
        files.add("ficheiro com SWAG");
        files.add("ficheiro hipster");
        files.add("ficheiro #YOLO");
        files.add("ultimo ficheiro");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, files);

        gv.setAdapter(adapter);
        gv.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                gv.setSelection(pos);
                Log.v("long clicked","pos: " + pos);
                return true;
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
