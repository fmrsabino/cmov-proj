package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.User;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public class CreateWorkspaceActivity extends ActionBarActivity {

    public final static String WORKSPACE_NAME_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    private static final String TAG = "CreateWorkspaceActivity";

    private ListView listView;
    private TextView quota;
    private EditText name;
    private LinearLayout keyHolder;
    private CheckBox checkbox;
    private EditText viewer;
    private EditText keywords;
    private List<String> viewers;
    private ArrayAdapter<String> adapter;
    private String username;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);
        UserManager userManager = new UserManager(getApplicationContext());

        username = userManager.getLoggedUser();
        user = userManager.getLoggedDomainUser();
        listView = (ListView) findViewById(R.id.invitation_list);
        quota = (TextView) findViewById(R.id.activity_create_workspace_quota);
        name = (EditText) findViewById(R.id.name);
        keyHolder = (LinearLayout) findViewById(R.id.keyHolder);
        checkbox = (CheckBox) findViewById(R.id.is_public);
        viewer = (EditText) findViewById(R.id.viewer);
        keywords = (EditText) findViewById(R.id.keywords);
        TextView availableStorage = (TextView) findViewById(R.id.activity_create_workspace_available_space);
        availableStorage.setText(new FileManagerLocal(this).getSystemAvailableSpace());

        viewers = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                R.layout.activity_viewers_list_item, R.id.selected_item, viewers);

        listView.setAdapter(adapter);

        checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    keyHolder.setVisibility(LinearLayout.VISIBLE);
                else
                    keyHolder.setVisibility(LinearLayout.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.getClient().connect();
        }
    }

    @Override
    protected void onPause() {
        if (AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.getClient().disconnect();
        }
        super.onPause();
    }

    public void createWorkspace(View view) {
        //get workspace parameters
        int quotaValue = 0;
        if (quota != null) {
            String quotaText = quota.getText().toString();
            try {
                quotaValue = Integer.parseInt(quotaText);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Quota value is not a number");
            }

        }
        String workspace = name.getText().toString();
        boolean is_public = checkbox.isChecked();
        String tags = keywords.getText().toString();
        int isPublic = (is_public) ? 1 : 0;

        Workspace ws = new Workspace(workspace, quotaValue, isPublic, tags, viewers, username);
        WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());
        List<Workspace> wsList = wsManager.retrieveOwnedWorkspaces();
        List<String> wsNameList = new ArrayList<>();

        for (Workspace w : wsList) {
            wsNameList.add(w.getName());
        }

        if(wsManager.sanitizeBlankInputs(ws)){
            new AlertDialog.Builder(this)
                    .setTitle("Blank fields")
                    .setMessage("Please fill all fields")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return;
        }

        if(wsNameList.contains(ws.getName())) {
            new AlertDialog.Builder(this)
                    .setTitle("Duplicate name")
                    .setMessage("Please choose another name for your workspace")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return;
        }


        if(!wsManager.addWorkspace(ws)){
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
        fileManagerLocal.createWorkspace(workspace, username);

        if(AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.setContext(this);
            AirDeskDriveAPI.createWorkspaceFolder(workspace, username, user.getDriveID());
        }

        //launch workspace browsing
        Intent intent = new Intent(CreateWorkspaceActivity.this, BrowseWorkspaceActivity.class);
        intent.putExtra(WORKSPACE_NAME_KEY, workspace);
        intent.putExtra(WorkspaceListActivity.ACCESS_KEY, "owned");
        intent.putExtra(WorkspaceListActivity.OWNER_KEY, username);
        startActivity(intent);
    }

    public void inviteUser(View view) {
        if(viewer != null) {

            String v = viewer.getText().toString();

            if(!TextUtils.isEmpty(v)) {
                if(viewers.contains(v)) {
                    Toast.makeText(this, "User already invited", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewers.add(v);

                adapter.notifyDataSetChanged();
                setListViewHeight(listView);
                viewer.setText(null);
            }
        }
    }

    public static boolean setListViewHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

}
