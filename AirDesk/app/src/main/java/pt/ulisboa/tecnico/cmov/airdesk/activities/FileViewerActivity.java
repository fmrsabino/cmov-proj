package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;

public class FileViewerActivity extends TermiteActivity implements SimWifiP2pManager.GroupInfoListener {
    private FileManagerLocal fileManagerLocal;
    private String file_name = null;
    private String workspace_name = null;
    private String user;
    private String access;
    private String ip;

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

        fileManagerLocal = new FileManagerLocal(this);
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
            Intent intent = new Intent(FileViewerActivity.this, FileEditorActivity.class);
            intent.putExtra("file_name", file_name);
            intent.putExtra("workspace_name", workspace_name);
            intent.putExtra(WorkspaceListActivity.OWNER_KEY, user);
            intent.putExtra(WorkspaceListActivity.ACCESS_KEY, access);
            intent.putExtra(WorkspaceListActivity.IP_KEY, ip);
            startActivity(intent);
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
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_CONTENT, myVirtualIp, ip, new String[]{file_name, workspace_name, user});
        taskManager.sendMessage(msg);
    }

    @Override
    public void processMessage(TermiteMessage receivedMessage) {
        if (receivedMessage.type == TermiteMessage.MSG_TYPE.WS_FILE_CONTENT_REPLY) {
            String fileContents = (String) receivedMessage.contents;
            TextView fileView = (TextView) findViewById(R.id.fileViewContents);
            fileView.setText(fileContents);
        }
    }
}
