package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.WorkspacesListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class WorkspaceListActivity extends  TermiteActivity implements SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "WorkspaceListActivity";

    public final static String WORKSPACE_NAME_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSNAME";
    public final static String ACCESS_KEY = "pt.ulisboa.tecnico.cmov.airdesk.ACCESS";
    public final static String OWNER_KEY = "owner";
    public final static String IP_KEY = "ip";

    private String repo;
    private ListView listView;
    private List<WorkspacesListAdapter.Content> directories = new ArrayList<>();
    private WorkspacesListAdapter listAdapter;
    private String selectedWorkspace;
    private EditText tagTxt;
    private UserManager userManager;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_list);

        Intent intent = getIntent();
        repo = intent.getStringExtra(WelcomeActivity.WORKSPACE_ACCESS_KEY);
        wsManager = new WorkspaceManager(getApplicationContext());
        userManager = new UserManager(getApplicationContext());
        listView = (ListView) findViewById(R.id.workspace_list);
        listAdapter = new WorkspacesListAdapter(this, directories);
        listView.setAdapter(listAdapter);

        user = userManager.getLoggedUser();

        populateWorkspaceList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.workspace);

                if (textView != null) {
                    selectedWorkspace = textView.getText().toString();

                    //Transfer control to BrowseWorkspace
                    Intent intent = new Intent(WorkspaceListActivity.this, BrowseWorkspaceActivity.class);
                    String access = repo;
                    String message = selectedWorkspace;
                    String owner = listAdapter.getItem(position).getOwner();
                    String ip = listAdapter.getItem(position).getIp();
                    intent.putExtra(ACCESS_KEY, access);
                    intent.putExtra(WORKSPACE_NAME_KEY, message);
                    intent.putExtra(OWNER_KEY, owner);
                    intent.putExtra(IP_KEY, ip);

                    startActivity(intent);
                }}});

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_browse_workspace_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
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


    private void deleteSelectedItems() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();

        if(TextUtils.equals(repo, "owned")) {
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                if (checked.get(i)) {
                    String workspaceName = listAdapter.getItem(i).getWs_name();

                    String workspaceDriveID = wsManager.getDriveID(workspaceName, user);
                    if (!wsManager.deleteOwnedWorkspace(workspaceName)) {
                        new AlertDialog.Builder(this)
                                .setTitle("Database Error")
                                .setMessage("Please try again")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                        return;
                    }
                    new FileManagerLocal(this).deleteWorkspace(workspaceName, user, workspaceDriveID);
                    if(AirDeskDriveAPI.getClient() != null) {
                        AirDeskDriveAPI.deleteFileFromFolder(userManager.getLoggedDomainUser().getDriveID(), workspaceName);
                    }
                }
            }
        }
        else if(TextUtils.equals(repo, "foreign")){
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                if (checked.get(i)) {
                    String workspaceName = listAdapter.getItem(i).getWs_name();
                    String workspaceOwner = listAdapter.getItem(i).getOwner();
                    wsManager.unregisterForeignWorkspace(workspaceName, workspaceOwner);
                }
            }
        }

        populateWorkspaceList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        populateWorkspaceList();
    }

    private void populateWorkspaceList() {
        directories.clear();
        WorkspaceManager wsManager = new WorkspaceManager(getApplicationContext());
        List<Workspace> wsList;

        if(TextUtils.equals(repo, "owned")) {
            wsList = wsManager.retrieveOwnedWorkspaces();

            for (Workspace w : wsList) {
                directories.add(new WorkspacesListAdapter.Content(w.getName(), Integer.toString(w.getQuota()), w.getOwner(), WorkspacesListAdapter.IP_LOCALHOST));
            }

            listAdapter.notifyDataSetChanged();
        }
        else if(TextUtils.equals(repo, "foreign"))
            retrieveForeignWorkspaces();
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

        switch (id) {
            case R.id.action_add_workspace:
                if(TextUtils.equals(repo, "owned")) {
                    Intent intent = new Intent(this, CreateWorkspaceActivity.class);
                    startActivity(intent);
                }
                else if(TextUtils.equals(repo, "foreign")){
                   tagTxt = new EditText(this);
                    tagTxt.setHint(userManager.getLoggedUserSubscription());
                    new AlertDialog.Builder(this)
                            .setTitle("Subscribe")
                            .setMessage("Please enter workspace tags, separated by a comma")
                            .setView(tagTxt)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String tags = tagTxt.getText().toString();
                                    userManager.clearSubscribedWorkspaces();
                                    subscribeWorkspaces(tags);
                                    populateWorkspaceList();
                                }
                            }).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void subscribeWorkspaces(String tags) {
        List<String> items = new ArrayList<>(Arrays.asList(tags.split("\\s*,\\s*")));
        userManager.setLoggedUserSubscription(tags);
        wsManager.subscribeWorkspaces(items);
    }

    private void retrieveForeignWorkspaces(){
        //We can only send the request when we have the group information available (onGroupInfoAvailable callback)
        termiteConnector.getManager().requestGroupInfo(termiteConnector.getChannel(), this);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        SimWifiP2pDevice myDevice = simWifiP2pDeviceList.getByName(simWifiP2pInfo.getDeviceName());
        if (myDevice == null) {
            return;
        }
        String myVirtualIp = myDevice.getVirtIp();

        for (String deviceName : simWifiP2pInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = simWifiP2pDeviceList.getByName(deviceName);

            TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_LIST, myVirtualIp, device.getVirtIp(), userManager.getLoggedUser());
            taskManager.sendMessage(msg);
        }
    }

    @Override
    public void processMessage(TermiteMessage receivedMessage) {
        List<Workspace> wsList = (List<Workspace>) receivedMessage.contents;
        for (Workspace ws : wsList) {
            //TODO: Check content values
            directories.add(new WorkspacesListAdapter.Content(ws.getName(), "30",ws.getOwner(), receivedMessage.srcIp));
        }
        listAdapter.notifyDataSetChanged();
    }
}