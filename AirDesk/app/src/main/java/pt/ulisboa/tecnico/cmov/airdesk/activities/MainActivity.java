package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;

import pt.inesc.termite.wifidirect.service.SimWifiP2pService;


public class MainActivity extends ActionBarActivity {

    private UserManager userManager;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userManager = new UserManager(getApplicationContext());

        if(userManager.getLoggedDomainUser()!=null){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
        }

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent wifiintent = new Intent(this, SimWifiP2pService.class);
        bindService(wifiintent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void signIn(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        EditText email = (EditText) findViewById(R.id.userIn);
        EditText plaintext = (EditText) findViewById(R.id.userPwd);


        if(!email.getText().toString().isEmpty() && !plaintext.getText().toString().isEmpty()) {
            try{
            String password = hashPassword(plaintext.getText().toString());
            if (userManager.userLogin( email.getText().toString(), password)) {
                Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            } catch(NoSuchAlgorithmException e){
                Toast.makeText(this, "Login Failed - Internal Error", Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(this, "Please write your nick", Toast.LENGTH_SHORT).show();
    }

    public void register(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        EditText nick = (EditText) findViewById(R.id.userNick);
        EditText email = (EditText) findViewById(R.id.userEmail);
        EditText plaintext = (EditText) findViewById(R.id.userPass);


        if(!nick.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !plaintext.getText().toString().isEmpty()) {
            try {
                String password = hashPassword(plaintext.getText().toString());
                if (userManager.registerUser(nick.getText().toString(), email.getText().toString(), password)) {
                    Toast.makeText(this, "Successful Registration", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                } else Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
            } catch(NoSuchAlgorithmException e){
                Toast.makeText(this, "Registration Failed - Internal Error", Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(this,"Name and Email are Required",Toast.LENGTH_SHORT).show();
    }

    private String hashPassword(String plaintext)throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plaintext.getBytes());

        byte byteData[] = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
