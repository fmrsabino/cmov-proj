package pt.ulisboa.tecnico.cmov.airdesk.utilities;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.activities.TermiteActivity;

public class TermiteTaskManager {

    private static final String TAG = "TermiteTaskManager";

    private TermiteTaskManager() {
        new IncomingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private static TermiteTaskManager termiteTaskManager = null;
    private static TermiteActivity mActivity;

    private SimWifiP2pSocketServer mSrvSocket;
    private SimWifiP2pSocket mCliSocket;

    public static TermiteTaskManager getInstance(TermiteActivity activity) {
        if (termiteTaskManager == null) {
            termiteTaskManager = new TermiteTaskManager();
        }
        mActivity = activity;
        return termiteTaskManager;
    }

    public class IncomingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "IncomingCommTask - doInBackground()");
                mSrvSocket = new SimWifiP2pSocketServer(10001);
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
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mSrvSocket != null) {
                        mSrvSocket.close();
                        mSrvSocket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            mCliSocket = values[0];
            new ReceiveCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
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
            switch (message.type){
                case WS_LIST:
                    mActivity.sendSubscribedWorkspaces(message);
                    break;
                case WS_SUBSCRIBE:
                    mActivity.subscribeViewer(message);
                    break;
                case WS_UNSUBSCRIBE:
                    mActivity.unsubscribeViewer(message);
                    break;
                case WS_FILE_LIST:
                    mActivity.sendWorkspaceFiles(message);
                    break;
                case WS_VIEWERS:
                    mActivity.sendWorkspaceViewers(message);
                    break;
                case WS_FILE_CONTENT:
                    mActivity.sendFileContent(message);
                    break;
                case WS_FILE_EDIT:
                    mActivity.changeFileContent(message);
                    break;
                case WS_FILE_EDIT_LOCK:
                    mActivity.lockFile(message);
                    break;
                default:
                    mActivity.processMessage(message);
            }
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

    public void sendMessage(TermiteMessage msg) {
        new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);
    }

    public class SendMessageTask extends AsyncTask<TermiteMessage, Void, Void> {
        @Override
        protected Void doInBackground(TermiteMessage ... params) {
            ObjectOutputStream oos = null;

            TermiteMessage msg = params[0];

            if (TextUtils.isEmpty(msg.rcvIp)) {
                return null;
            }

            try {
                SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(msg.rcvIp, 10001);
                oos = new ObjectOutputStream(mCliSocket.getOutputStream());
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
            return null;
        }
    }

}
