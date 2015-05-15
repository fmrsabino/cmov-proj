package pt.ulisboa.tecnico.cmov.airdesk.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

public class TermiteConnector {

    private static TermiteConnector termiteConnector = null;
    private Context applicationContext;

    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;
    private Messenger mService;

    public static TermiteConnector getInstance(Context appContext) {
        if (termiteConnector == null) {
            termiteConnector = new TermiteConnector(appContext);
        }
        return termiteConnector;
    }

    private TermiteConnector(Context appContext) {
        this.applicationContext = appContext;

        ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = new Messenger(service);
                mManager = new SimWifiP2pManager(mService);
                mChannel = mManager.initialize(applicationContext, applicationContext.getMainLooper(), null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mManager = null;
                mChannel = null;
            }
        };

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(applicationContext);
        Intent intent = new Intent(applicationContext, SimWifiP2pService.class);
        applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public SimWifiP2pManager getManager() {
        return mManager;
    }

    public SimWifiP2pManager.Channel getChannel() {
        return mChannel;
    }
}
