package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class CreateWorkspaceActivity extends ActionBarActivity {

    public final static String workspace_name = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    ListView listView ;
    EditText name;
    EditText quota;
    CheckBox checkbox;
    EditText viewer;
    EditText keywords;
    ArrayList<String> viewers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);

        listView = (ListView) findViewById(R.id.invitation_list);

        name = (EditText) findViewById(R.id.name);
        quota = (EditText) findViewById(R.id.quota);
        checkbox = (CheckBox)findViewById(R.id.is_public);
        viewer = (EditText) findViewById(R.id.viewer);
        keywords = (EditText) findViewById(R.id.keywords);

        viewers = new ArrayList<String>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        listView.setAdapter(adapter);
    }


    public void createWorkspace(View view) {
        //get workspace parameters
        String workspace = name.getText().toString();
        String ws_quota = quota.getText().toString();
        boolean is_public = checkbox.isChecked();
        String tags = keywords.getText().toString();

        //result of sanitization
        if(sanitizeInputs(workspace,ws_quota,tags))
            return;

        //write workspace specs to database


        //launch workspace browsing
        Intent intent = new Intent(CreateWorkspaceActivity.this, BrowseWorkspace.class);
        intent.putExtra(workspace_name, workspace);
        startActivity(intent);
    }


    public void inviteUser(View view) {
        String v = viewer.getText().toString();

        viewers.add(v);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    public boolean sanitizeInputs(String wsname, String wsquota, String wstags){
        boolean incompleteFields = false;

        //sanitize inputs
        if(wsname.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("No Workspace name")
                    .setMessage("Please choose a name to your workspace")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            incompleteFields=true;
        }

        if(wsquota.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("No Storage Quota defined")
                    .setMessage("Please choose a maximum storage for your workspace")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            incompleteFields=true;
        }

        if(wstags.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("No Keywords defined")
                    .setMessage("Please insert some keywords to describe your workspace")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            incompleteFields=true;
        }
        return incompleteFields;
    }

}