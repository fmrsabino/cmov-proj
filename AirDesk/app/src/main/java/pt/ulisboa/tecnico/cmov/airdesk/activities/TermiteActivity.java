package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteConnector;

public class TermiteActivity extends ActionBarActivity {

    protected TermiteConnector termiteConnector;
    private SimWifiP2pBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        termiteConnector = TermiteConnector.getInstance(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        receiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
