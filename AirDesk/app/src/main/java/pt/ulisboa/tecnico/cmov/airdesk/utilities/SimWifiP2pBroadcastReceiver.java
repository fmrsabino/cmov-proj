package pt.ulisboa.tecnico.cmov.airdesk.utilities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmov.airdesk.activities.TermiteActivity;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private TermiteActivity mActivity;

    private final static String TAG = "BroadcastReceiver";

    public SimWifiP2pBroadcastReceiver(TermiteActivity activity) {
        super();
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION:
                Log.d(TAG, "Received WIFI_P2P_STATE_CHANGED_ACTION");
                // This action is triggered when the WDSim service changes state:
                // - creating the service generates the WIFI_P2P_STATE_ENABLED event
                // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

                int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
                if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(mActivity, "WiFi Direct enabled",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity, "WiFi Direct disabled",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION:
                Log.d(TAG, "Received WIFI_P2P_PEERS_CHANGED_ACTION");
                // Request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()

                Toast.makeText(mActivity, "Peer list changed",
                        Toast.LENGTH_SHORT).show();

                break;
            case SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION: {
                Log.d(TAG, "Received WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION");

                SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                        SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
                ginfo.print();
                mActivity.handleMembershipChange(ginfo);
                Toast.makeText(mActivity, "Network membership changed",
                        Toast.LENGTH_SHORT).show();

                break;
            }
            case SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION: {
                Log.d(TAG, "Received WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION");

                SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                        SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
                ginfo.print();
                mActivity.handleMembershipChange(ginfo);
                Toast.makeText(mActivity, "Group ownership changed",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
}
