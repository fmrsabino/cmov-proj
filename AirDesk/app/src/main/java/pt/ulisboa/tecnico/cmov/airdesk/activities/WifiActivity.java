package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;

public class WifiActivity extends TermiteActivity implements SimWifiP2pManager.GroupInfoListener, SimWifiP2pManager.PeerListListener {

    private TextView outputText = null;
    private EditText inputText = null;
    private EditText ipAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi);

        outputText = (TextView) findViewById(R.id.textOutput);
        inputText = (EditText) findViewById(R.id.textInput);
        ipAddress = (EditText) findViewById(R.id.activity_wifi_ip_address);

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

	/*
	 * Classes implementing message exchange
	 */

    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt("10001"));
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

    //Connection class
    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            outputText.setText("Connecting...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt("10001"));
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                outputText.setText(result);
            }
            else {
                receiveTask = new ReceiveCommTask();
                receiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
            }
        }
    }

    public void sendMessage(View view) {
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(mCliSocket.getOutputStream());
            TermiteMessage msg = new TermiteMessage(TermiteMessage.MSG_TYPE.WS_FILE_LIST, "Isto é um teste");
            oos.writeObject(msg);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        inputText.setText("");
    }

    @Override
    public void processMessage(TermiteMessage message) {
        Toast.makeText(this, "Received Termite Message!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Contents: " + message.contents, Toast.LENGTH_LONG).show();
    }

    public void connectHost(View view) {
        new OutgoingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                ipAddress.getText().toString());
    }

    public void listPeers(View view) {
        if (termiteConnector.isBound()) {
            termiteConnector.getManager().requestPeers(termiteConnector.getChannel(), WifiActivity.this);
        } else {
            Toast.makeText(view.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        StringBuilder peersStr = new StringBuilder();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }

        // display list of devices in range
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Range")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        Log.i("WIFI", simWifiP2pInfo.getDeviceName());
    }
}
