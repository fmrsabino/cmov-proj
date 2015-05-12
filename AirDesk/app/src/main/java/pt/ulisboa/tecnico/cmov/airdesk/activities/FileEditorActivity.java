package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public class FileEditorActivity extends TermiteActivity implements SimWifiP2pManager.GroupInfoListener{

    private FileManagerLocal fileManagerLocal;
    private String file_name = null;
    private String workspace_name = null;
    private WorkspaceManager wsManager;
    private String user;
    private String access;
    private String ip;

    private long initialFileSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_editor);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("file_name");
        workspace_name = intent.getStringExtra("workspace_name");
        user = intent.getStringExtra(WorkspaceListActivity.OWNER_KEY);
        access = intent.getStringExtra(WorkspaceListActivity.ACCESS_KEY);
        ip = intent.getStringExtra(WorkspaceListActivity.IP_KEY);

        getSupportActionBar().setTitle(file_name);

        fileManagerLocal = new FileManagerLocal(this);
        wsManager = new WorkspaceManager(getApplicationContext());

        loadFile();
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_file) {
            EditText fileView = (EditText) findViewById(R.id.fileContents);

            byte[] fileBytes;
            try {
                fileBytes = fileView.getText().toString().getBytes("UTF-8");
                long finalFileSize = fileBytes.length;

                long updatedBytes = finalFileSize - initialFileSize;
                long currentQuota = wsManager.getCurrentWorkspaceQuota(workspace_name, user);

                if (currentQuota - updatedBytes < 0) {
                    Toast.makeText(this, "Quota Exceeded: Couldn't save file", Toast.LENGTH_LONG).show();
                    return true;
                }

                wsManager.updateWorkspaceQuota(workspace_name, -updatedBytes, user);
                fileManagerLocal.saveFileContents(file_name, workspace_name, user, fileView.getText().toString());
                if(AirDeskDriveAPI.getClient() != null) {
                    AirDeskDriveAPI.updateFile(wsManager.getDriveID(workspace_name, user), file_name, fileView.getText().toString());
                }

                Toast.makeText(this, "Saved File", Toast.LENGTH_SHORT).show();
                finish();
                return true;

            } catch (UnsupportedEncodingException e) {
               //do nothing
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFile(){
        if(access.equals("foreign"))
            retrieveForeignFileContent();
        else {
            initialFileSize = fileManagerLocal.getFileSize(file_name, workspace_name, user);
            String fileContents = fileManagerLocal.getFileContents(file_name, workspace_name, user);
            EditText fileView = (EditText) findViewById(R.id.fileContents);
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
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_EDIT_LOCK, myVirtualIp, ip, new String[]{file_name, workspace_name, user});
        taskManager.sendMessage(msg);
    }

    @Override
    public void processMessage(TermiteMessage receivedMessage) {
        if (receivedMessage.type == TermiteMessage.MSG_TYPE.WS_FILE_EDIT_LOCK_REPLY) {
            String[] contents = (String[]) receivedMessage.contents;
            String fileContents = contents[0];
            initialFileSize = Long.parseLong(contents[1], 10);
            EditText fileView = (EditText) findViewById(R.id.fileContents);
            fileView.setText(fileContents);
        }
        else if(receivedMessage.type == TermiteMessage.MSG_TYPE.WS_ERROR){
            Toast.makeText(this, "ERROR: " + receivedMessage.contents, Toast.LENGTH_LONG).show();
        }
    }
}
