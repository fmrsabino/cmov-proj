package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
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
        wsManager = WorkspaceManager.getInstance(this);
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
        taskManager.setActivity(this);
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

    public void subscribeViewer(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        String subscribingUser = contents[0];
        List<String> items = new ArrayList<>(Arrays.asList(contents[1].split("\\s*,\\s*")));

        wsManager.subscribeWorkspaces(subscribingUser, items);

        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_SUBSCRIBE_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, null);
        taskManager.sendMessage(msg);
    }

    public void unsubscribeViewer(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        wsManager.deleteWorkspaceViewer(contents[0], contents[1], contents[2]);
        Workspace w = wsManager.retrieveWorkspace(contents[1], contents[2]);
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_UNSUBSCRIBE_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, new String[]{w.getName(), ""+w.getQuota(), w.getOwner()});
        taskManager.sendMessage(msg);

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

            FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
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

            FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
            String fileContent = fileManagerLocal.getFileContents(fileName, wsName, wsOwner);
            TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_CONTENT_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, fileContent);
            taskManager.sendMessage(msg);
        }
    }

    public void changeFileContent(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        if (contents.length == 4) { //file_name, ws_name, owner, newFileContent
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];
            String fileContent = contents[3];

            TermiteMessage msg;
            boolean inList = false;

            FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
            List<String[]> lockedFiles = fileManagerLocal.getLockedFiles();
            String[] file = new String[]{fileName, wsName, wsOwner};

            for (String[] f : lockedFiles) {
                if (f[0].equals(fileName) && f[1].equals(wsName) && f[2].equals(wsOwner)) {
                    inList = true;
                    break;
                }
            }

            if (inList) {
                boolean quota_exceeded = isQuotaExceeded(fileContent, fileName, wsName, wsOwner);
                //Check whether the quota is exceeded in local WS
                if (!quota_exceeded) {
                    msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_EDIT_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, fileName);
                } else {
                    msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_ERROR, receivedMessage.rcvIp, receivedMessage.srcIp, "Quota Exceeded");
                }

                fileManagerLocal.removeLock(file);
                taskManager.sendMessage(msg);
            }
        }
    }

    public void lockFile(TermiteMessage receivedMessage){
        String[] contents = (String[]) receivedMessage.contents;
        if(contents.length == 3) { //file_name, ws_name, owner
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];

            FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
            TermiteMessage msg;
            if(fileManagerLocal.lockFile(fileName, wsName, wsOwner)) {
                String file = fileManagerLocal.getFileContents(fileName, wsName, wsOwner); // file content
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_EDIT_LOCK_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, file);
                Log.d("TA TUDO BEM", "AMIGO");
            }
            else{
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_ERROR, receivedMessage.rcvIp, receivedMessage.srcIp, "Please try later");
                Log.d("TA NADA BEM", "AMIGO");
            }

            taskManager.sendMessage(msg);
        }
    }


    private boolean isQuotaExceeded(String fileContent, String fileName, String wsName, String wsOwner){
        byte[] fileBytes;
        long finalFileSize;
        try {
            fileBytes = fileContent.getBytes("UTF-8");
            finalFileSize = fileBytes.length;
        } catch (UnsupportedEncodingException e) {
            return true;
        }

            FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
            long initialFileSize = fileManagerLocal.getFileSize(fileName, wsName, wsOwner);
            long updatedBytes = finalFileSize - initialFileSize;
            long currentQuota = wsManager.getCurrentWorkspaceQuota(wsName, wsOwner);

            if (currentQuota - updatedBytes < 0) {
                return true;
            }

            wsManager.updateWorkspaceQuota(wsName, -updatedBytes, wsOwner);
            fileManagerLocal.saveFileContents(fileName, wsName, wsOwner, fileContent);
            if (AirDeskDriveAPI.getClient() != null) {
                AirDeskDriveAPI.updateFile(wsManager.getDriveID(wsName, wsOwner), fileName, fileContent);
            }
            return false;
    }


    public void releaseLock(TermiteMessage receivedMessage) {
        FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
        fileManagerLocal.removeLock((String[]) receivedMessage.contents);
    }

    public void createFile(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);
        if (contents.length == 3) { //file_name, ws_name, owner
            String fileName = contents[0];
            String wsName = contents[1];
            String wsOwner = contents[2];

            TermiteMessage msg;

            if(fileManagerLocal.createFile(fileName, wsName, wsOwner))
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_CREATE_REPLY, receivedMessage.rcvIp, receivedMessage.srcIp, fileName);
            else
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_ERROR, receivedMessage.rcvIp, receivedMessage.srcIp, new String[] {"creationError", fileName});

            if (AirDeskDriveAPI.getClient() != null) {
                AirDeskDriveAPI.createEmptyFile(wsManager.getDriveID(wsName, wsOwner), fileName);
            }
            taskManager.sendMessage(msg);
        }
    }


    public void deleteFiles(TermiteMessage receivedMessage) {
        String[] contents = (String[]) receivedMessage.contents;
        FileManagerLocal fileManagerLocal = FileManagerLocal.getInstance(this);

        String wsName = contents[0];
        String wsOwner = contents[1];

        TermiteMessage msg;

        for (int i = 2; i < contents.length; i++) {
            if (fileManagerLocal.lockFile(contents[i], wsName, wsOwner)) {
                wsManager.updateWorkspaceQuota(wsName, fileManagerLocal.getFileSize(contents[i], wsName, wsOwner), wsOwner);
                fileManagerLocal.deleteFile(contents[i], wsName, wsOwner, wsManager.getDriveID(wsName, wsOwner));
                fileManagerLocal.removeLock(new String[]{contents[i], wsName, wsOwner});
            }
            else {
                msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_ERROR, receivedMessage.rcvIp, receivedMessage.srcIp, new String[] {"deletionError", contents[i]});
                taskManager.sendMessage(msg);
            }
        }
    }

    //Called when the TaskManager doesn't know how to handle the TermiteMessage (ie.: no generic)
    public abstract void processMessage(TermiteMessage receivedMessage);

    public abstract void handleMembershipChange(SimWifiP2pInfo newGroupInfo);
}
