package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public class CreateWorkspaceActivity extends ActionBarActivity {

    public final static String WORKSPACE_NAME_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";

    private ListView listView;
    private TextView tb;
    private EditText name;
    private SeekBar quota;
    private CheckBox checkbox;
    private EditText viewer;
    private EditText keywords;
    private List<String> viewers;
    private Workspace ws;
    private WorkspaceManager wsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);

        listView = (ListView) findViewById(R.id.invitation_list);
        tb = (TextView) findViewById(R.id.quota_progress);
        name = (EditText) findViewById(R.id.name);
        quota = (SeekBar) findViewById(R.id.quota);
        checkbox = (CheckBox)findViewById(R.id.is_public);
        viewer = (EditText) findViewById(R.id.viewer);
        keywords = (EditText) findViewById(R.id.keywords);

        quota.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                tb.setText("Quota: "+ Integer.toString(progressChanged));
            }
        });

        viewers = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

        listView.setAdapter(adapter);
    }


    public void createWorkspace(View view) {
        //get workspace parameters
        String workspace = name.getText().toString();
        int ws_quota = quota.getProgress();
        boolean is_public = checkbox.isChecked();
        String tags = keywords.getText().toString();
        int isPublic = (is_public) ? 1 : 0;


        ws = new Workspace(workspace, ws_quota, isPublic, tags, viewers);
        wsManager = new WorkspaceManager(ws, getApplicationContext());


        if(wsManager.sanitizeBlankInputs()){
            new AlertDialog.Builder(this)
                    .setTitle("Blank fields")
                    .setMessage("Please fill all fields")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return;
        }

        if(!wsManager.addWorkspace()){
            new AlertDialog.Builder(this)
                    .setTitle("Database Error")
                    .setMessage("Please try again")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return;
        }

        FileManagerLocal fileManagerLocal = new FileManagerLocal(getApplicationContext());
        fileManagerLocal.createFolder(workspace);

        //launch workspace browsing
        Intent intent = new Intent(CreateWorkspaceActivity.this, BrowseWorkspaceActivity.class);
        intent.putExtra(WORKSPACE_NAME_KEY, workspace);
        startActivity(intent);
    }


    public void inviteUser(View view) {
        if(viewer != null) {

            String v = viewer.getText().toString();

            if(!TextUtils.isEmpty(v)) {
                viewers.add(v);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, viewers);

                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
        }
    }


}
