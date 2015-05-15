package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;

public class FileViewerActivity extends TermiteActivity implements SimWifiP2pManager.GroupInfoListener {
    private FileManagerLocal fileManagerLocal;
    private String file_name = null;
    private String workspace_name = null;
    private String user;
    private String access;
    private String ip;
    private boolean editAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("file_name");
        workspace_name = intent.getStringExtra("workspace_name");
        user = intent.getStringExtra(WorkspaceListActivity.OWNER_KEY);
        access = intent.getStringExtra(WorkspaceListActivity.ACCESS_KEY);
        ip = intent.getStringExtra(WorkspaceListActivity.IP_KEY);

        getSupportActionBar().setTitle(file_name);
        fileManagerLocal = FileManagerLocal.getInstance(this);

        if(AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.setContext(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFile();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_file) {
            if(access.equals("foreign")) {
                editAttempt = true;
                retrieveForeignFileContent();
            }
            else{
                if(fileManagerLocal.lockFile(file_name, workspace_name, user)) {
                    Intent intent = new Intent(FileViewerActivity.this, FileEditorActivity.class);
                    intent.putExtra("file_name", file_name);
                    intent.putExtra("workspace_name", workspace_name);
                    intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
                    intent.putExtra(WorkspaceListActivity.ACCESS_KEY, access);
                    intent.putExtra(WorkspaceListActivity.IP_KEY, ip);
                    startActivity(intent);
                }
                else
                    Toast.makeText(this, "Error acquiring lock", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFile(){
        if(access.equals("foreign"))
            retrieveForeignFileContent();
        else {
            String fileContents = fileManagerLocal.getFileContents(file_name, workspace_name, user);
            TextView fileView = (TextView) findViewById(R.id.fileViewContents);
            fileView.setText(fileContents);
        }
    }

    private void retrieveForeignFileContent(){
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
        TermiteMessage msg;

        if(editAttempt)
            msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_EDIT_LOCK, myVirtualIp, ip, new String[]{file_name, workspace_name, user});
        else
            msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_CONTENT, myVirtualIp, ip, new String[]{file_name, workspace_name, user});

        editAttempt = false;
        taskManager.sendMessage(msg);
    }

    @Override
    public void processMessage(TermiteMessage receivedMessage) {
        switch (receivedMessage.type) {
            case WS_FILE_CONTENT_REPLY:
                String fileContents = (String) receivedMessage.contents;
                TextView fileView = (TextView) findViewById(R.id.fileViewContents);
                fileView.setText(fileContents);
                break;
            case WS_FILE_EDIT_LOCK_REPLY:
                String contents = (String) receivedMessage.contents;
                Intent intent = new Intent(FileViewerActivity.this, FileEditorActivity.class);
                intent.putExtra("file_name", file_name);
                intent.putExtra("workspace_name", workspace_name);
                intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
                intent.putExtra(WorkspaceListActivity.ACCESS_KEY, access);
                intent.putExtra(WorkspaceListActivity.IP_KEY, ip);
                intent.putExtra("file_content", contents);
                startActivity(intent);
                break;
            case WS_ERROR:
                Toast.makeText(this, "ERROR: " + receivedMessage.contents, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void handleMembershipChange(SimWifiP2pInfo newGroupInfo) {
        if(access.equals("foreign")) {
            Set<String> devices = newGroupInfo.getDevicesInNetwork();
            if(!devices.contains(ip)) {
                Intent intent = new Intent(this, WorkspaceListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }
}
