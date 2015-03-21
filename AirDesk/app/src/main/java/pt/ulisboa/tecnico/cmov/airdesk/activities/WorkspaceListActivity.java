package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.listViewMulticolAdapter;


public class WorkspaceListActivity extends ActionBarActivity {

    public final static String workspace_name = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    ListView listView ;
    ArrayList<listViewMulticolAdapter.Content> content;
    String selectedWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_list);

        listView = (ListView) findViewById(R.id.workspace_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.workspace);
                selectedWorkspace = textView.getText().toString();

                //Transfer control to BrowseWorkspace
        Intent intent = new Intent(WorkspaceListActivity.this, BrowseWorkspaceActivity.class);
        String message = selectedWorkspace;
        intent.putExtra(workspace_name, message);
        startActivity(intent);
            }});



        content = new ArrayList<>();

        populateWorkspaceList();
    }

    private void populateWorkspaceList() {
        listViewMulticolAdapter.Content temp = new listViewMulticolAdapter.Content("Snow", "30%");
        listViewMulticolAdapter.Content temp2 = new listViewMulticolAdapter.Content("Sun", "36%");

        content.add(temp);
        content.add(temp2);


        listViewMulticolAdapter adapter = new listViewMulticolAdapter(this, content);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addWorkspace(View view) {
        //Transfer control to CreateWorkspaceActivity
        Intent intent = new Intent(this, CreateWorkspaceActivity.class);
        startActivity(intent);
    }
}