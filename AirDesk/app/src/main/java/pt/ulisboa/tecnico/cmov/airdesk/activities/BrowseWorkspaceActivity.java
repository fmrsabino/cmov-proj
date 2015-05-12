package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.dialogs.CreateFileDialogFragment;
import pt.ulisboa.tecnico.cmov.airdesk.dialogs.ManageWorkspaceDialogFragment;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.WorkspacesListAdapter;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;


public class BrowseWorkspaceActivity extends TermiteActivity
        implements CreateFileDialogFragment.CreateFileDialogListener,
        ManageWorkspaceDialogFragment.ManageQuotaDialogListener,
        SimWifiP2pManager.GroupInfoListener {

    private GridView gridView;
    private String workspaceName;
    private String access;
    private List<String> files = new ArrayList<>();
    private ArrayAdapter<String> gridAdapter;
    private FileManagerLocal fileManager = null;
    private WorkspaceManager wsManager;
    private String user;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_workspace);

        fileManager = new FileManagerLocal(getApplicationContext());
        wsManager = new WorkspaceManager(getApplicationContext());

        Intent intent = getIntent();
        access = intent.getStringExtra(WorkspaceListActivity.ACCESS_KEY);
        workspaceName = intent.getStringExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY);
        getSupportActionBar().setTitle(workspaceName);
        user = intent.getStringExtra(WorkspaceListActivity.OWNER_KEY);
        ip = intent.getStringExtra(WorkspaceListActivity.IP_KEY);

        invalidateOptionsMenu();



        gridView = (GridView) findViewById(R.id.workspace_files);
        gridAdapter = new ArrayAdapter<>(this,
                R.layout.activity_browse_workspace_grid_item, R.id.text1, files);

        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(BrowseWorkspaceActivity.this, FileViewerActivity.class);
                intent.putExtra("file_name", gridAdapter.getItem(position));
                intent.putExtra("workspace_name", workspaceName);
                intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
                intent.putExtra(WorkspaceListActivity.ACCESS_KEY, access);
                intent.putExtra(WorkspaceListActivity.IP_KEY, ip);
                startActivity(intent);
            }
        });


            gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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

        refreshFilesList();

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
        SparseBooleanArray checked = gridView.getCheckedItemPositions();
        for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                String fileName = gridAdapter.getItem(i);
                wsManager.updateWorkspaceQuota(workspaceName, fileManager.getFileSize(fileName, workspaceName, user), user);
                fileManager.deleteFile(fileName, workspaceName, user, wsManager.getDriveID(workspaceName,user));
            }
        }
        refreshFilesList();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (access.equals("foreign")) {
            menu.removeItem(R.id.action_manage_settings);
        }
        return super.onPrepareOptionsMenu(menu);
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

        switch (id) {
            case (R.id.action_add_file):
                showCreateFileDialog();
                return true;
            case(R.id.action_manage_settings):
                showManageWorkspaceSettingsDialog();
                return true;
        }

        if (id == R.id.action_viewers){
            Intent intent = new Intent(this, ViewersActivity.class);
            intent.putExtra(WorkspaceListActivity.ACCESS_KEY, this.access);
            intent.putExtra(WorkspaceListActivity.WORKSPACE_NAME_KEY, this.workspaceName);
            intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCreateFileDialog() {
        CreateFileDialogFragment dialog = new CreateFileDialogFragment();
        dialog.show(getFragmentManager(), "CreateFileDialogFragment");
    }

    private void showManageWorkspaceSettingsDialog() {
        Bundle args = new Bundle();
        args.putString("workspace", workspaceName);
        args.putString(WorkspaceListActivity.OWNER_KEY, user);

        ManageWorkspaceDialogFragment dialog = new ManageWorkspaceDialogFragment();
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "ManageWorkspaceDialogFragment");
    }

    private void refreshFilesList() {
        files.clear();
        if (!TextUtils.isEmpty(ip)) {
            if (ip.equals(WorkspacesListAdapter.IP_LOCALHOST)) {
                files.addAll(fileManager.getFilesNames(workspaceName, user));
            } else {
                retrieveForeignFiles();
            }
        }
        gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        fileManager.createFile(((CreateFileDialogFragment) dialog).getFileName(), workspaceName, user);
        if(AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.createEmptyFile(wsManager.getDriveID(workspaceName, user), ((CreateFileDialogFragment) dialog).getFileName());
        }
        refreshFilesList();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {}

    @Override
    public void onWorkspaceSettingsDialogPositiveClick(DialogFragment dialog) {
        String newQuota = ((ManageWorkspaceDialogFragment)dialog).getUpdatedQuota();
        if(newQuota != null) {
            long updatedQuota = Long.parseLong(newQuota);
            long workspaceSize = fileManager.getWorkspaceSize(workspaceName, user);

            if (updatedQuota >= workspaceSize) {
                wsManager.setWorkspaceQuota(workspaceName, (updatedQuota - workspaceSize), user);
            } else
                Toast.makeText(this, "New quota must be greater than current workspace size", Toast.LENGTH_SHORT).show();
        }

        boolean newVisibility = ((ManageWorkspaceDialogFragment)dialog).getWorkspaceVisibility();
        int visibility = (newVisibility)? 1: 0;
        wsManager.setWorkspaceVisibility(workspaceName, visibility, user);

        String keywords = ((ManageWorkspaceDialogFragment)dialog).getWorkspaceKeywords();
        wsManager.setWorkspaceKeywords(workspaceName, keywords, user);
    }

    @Override
    public void onWorkspaceSettingsDialogNegativeClick(DialogFragment dialog) {
      // do nothing
    }

    private void retrieveForeignFiles(){
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
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_LIST, myVirtualIp, ip, new String[]{workspaceName, user});
        taskManager.sendMessage(msg);
    }

    @Override
    public void processMessage(TermiteMessage receivedMessage) {
        if (receivedMessage.type == TermiteMessage.MSG_TYPE.WS_FILE_LIST_REPLY) {
            List<String> filesNames = (List<String>) receivedMessage.contents;
            files.addAll(filesNames);
            gridAdapter.notifyDataSetChanged();
        }
    }
}
