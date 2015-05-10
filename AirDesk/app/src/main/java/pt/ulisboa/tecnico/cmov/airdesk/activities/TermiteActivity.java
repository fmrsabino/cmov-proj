package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteConnector;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;

public abstract class TermiteActivity extends ActionBarActivity {

    protected TermiteConnector termiteConnector;
    private SimWifiP2pBroadcastReceiver receiver;

    protected SimWifiP2pSocketServer mSrvSocket;
    protected SimWifiP2pSocket mCliSocket;

    protected ReceiveCommTask receiveTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        termiteConnector = TermiteConnector.getInstance(getApplicationContext());
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
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
        //unregisterReceiver(receiver);
    }

    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mSrvSocket = new SimWifiP2pSocketServer(10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    if (mCliSocket != null && mCliSocket.isClosed()) {
                        mCliSocket = null;
                    }
                    if (mCliSocket != null) {
                        Log.d("INC_TASK", "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        publishProgress(sock);
                    }
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            mCliSocket = values[0];
            receiveTask = new ReceiveCommTask();

            receiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }


    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, Void, TermiteMessage> {
        SimWifiP2pSocket s;

        @Override
        protected TermiteMessage doInBackground(SimWifiP2pSocket... params) {
            ObjectInputStream ois = null;
            TermiteMessage message = null;
            try{
                s = params[0];

                ois = new ObjectInputStream(s.getInputStream());
                message = (TermiteMessage) ois.readObject();
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(TermiteMessage message) {
            processMessage(message);
            if (!s.isClosed()) {
                try {
                    s.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            s = null;
        }
    }

    public abstract void processMessage(TermiteMessage message);
}
