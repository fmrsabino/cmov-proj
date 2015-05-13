package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteConnector;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteTaskManager;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public abstract class TermiteActivity extends ActionBarActivity {
    private static final String TAG = "TermiteActivity";

    protected TermiteConnector termiteConnector;
    protected TermiteTaskManager taskManager;
    private SimWifiP2pBroadcastReceiver receiver;

    protected WorkspaceManager wsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        termiteConnector = TermiteConnector.getInstance(getApplicationContext());
        taskManager = TermiteTaskManager.getInstance(this);
        wsManager = new WorkspaceManager(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        termiteConnector = TermiteConnector.getInstance(getApplicationContext());
        taskManager = TermiteTaskManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        receiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(receiver, filter);
        Log.d(TAG, "Registered BroadcastReceiver");
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
            Log.d(TAG, "Unregistered BroadcastReceiver");
        } catch (IllegalArgumentException ignored) {}
    }

    public void sendSubscribedWorkspaces(TermiteMessage receivedMessage) {
        String requestingUser = (String) receivedMessage.contents;
        List<Workspace> workspaces = wsManager.remoteRetrieveForeignWorkspaces(requestingUser);
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_LIST_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, workspaces);

        taskManager.sendMessage(msg);
    }

    public void unsubscribeViewer(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        wsManager.deleteWorkspaceViewer(contents[0], contents[1], contents[2]);
    }

    public void sendWorkspaceViewers(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        Workspace w = wsManager.retrieveWorkspace(contents[0], contents[1]);
        List<String> viewers = w.getUsers();
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_VIEWERS_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, viewers);

        taskManager.sendMessage(msg);
    }

    public void sendWorkspaceFiles(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        if(contents.length == 2) { //ws_name + owner
            String wsName = contents[0];
            String wsOwner = contents[1];

            FileManagerLocal fileManagerLocal = new FileManagerLocal(this);
            List<String> files = fileManagerLocal.getFilesNames(wsName, wsOwner);

            TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_LIST_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, files);
            taskManager.sendMessage(msg);
        }
    }

    public void sendFileContent(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        if(contents.length == 3) { //file_name, ws_name, owner
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];

            FileManagerLocal fileManagerLocal = new FileManagerLocal(this);
            String fileContent = fileManagerLocal.getFileContents(fileName, wsName, wsOwner);
            TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_CONTENT_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, fileContent);
            taskManager.sendMessage(msg);
        }
    }

    public void changeFileContent(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        if(contents.length == 4) { //file_name, ws_name, owner, newFileContent
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];
            String fileContent = contents[3];

            FileManagerLocal fileManagerLocal = new FileManagerLocal(this);
            fileManagerLocal.saveFileContents(fileName, wsName, wsOwner, fileContent);
        }
    }

    public void lockFile(TermiteMessage receivedMessage){
        String[] contents = (String[]) receivedMessage.contents;
        if(contents.length == 3) { //file_name, ws_name, owner
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];

            FileManagerLocal fileManagerLocal = new FileManagerLocal(this);
            TermiteMessage msg;
            if(fileManagerLocal.lockFile(fileName, wsName, wsOwner)) {
                String[] file = {fileManagerLocal.getFileContents(fileName, wsName, wsOwner),"" + fileManagerLocal.getFileSize(fileName, wsName, wsOwner)}; // file content + its size
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_EDIT_LOCK_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, file);
            }
            else
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_ERROR, receivedMessage.rcvIp, receivedMessage.srcIp, "Please try later");

            taskManager.sendMessage(msg);
        }
    }


    //Called when the TaskManager doesn't know how to handle the TermiteMessage (ie.: no generic)
    public abstract void processMessage(TermiteMessage receivedMessage);

}
