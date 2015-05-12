package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteConnector;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteTaskManager;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public abstract class TermiteActivity extends ActionBarActivity {
    private static final String TAG = "TermiteActivity";

    protected TermiteConnector termiteConnector;
    private TermiteTaskManager taskManager;
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
        } catch (IllegalArgumentException e) {}
    }

    public void feedSubscribedWorkspaces(Object messageContent) {
        String requestingUser = (String) messageContent;
        List<String> workspaces = wsManager.remoteRetrieveForeignWorkspaces(requestingUser);
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_LIST_REPLY, workspaces);

        taskManager.sendMessage(msg);
    }

    public void feedWorkspaceFiles(Object messageContent) {
        String workspace = (String) messageContent;
        List<String> files = null; // go to file system and fetch files from demanded workspace
        TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_LIST_REPLY, files);

        taskManager.sendMessage(msg);
    }

    //Called when the TaskManager doesn't know how to handle the TermiteMessage (ie.: no generic)
    public abstract void processMessage(TermiteMessage message);
}
