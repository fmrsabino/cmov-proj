package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.domain.User;
import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.TermiteMessage;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;

public class WelcomeActivity extends TermiteActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final static String WORKSPACE_ACCESS_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSACCESS";
    private UserManager userManager;
    private User loggedUser;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        userManager = new UserManager(getApplicationContext());

        loggedUser = userManager.getLoggedDomainUser();

        TextView email = (TextView) findViewById(R.id.emailView);
        email.setText(loggedUser.getEmail());

        TextView nick = (TextView) findViewById(R.id.nickView);
        nick.setText("Welcome " + loggedUser.getNick() + "!");

        if(AirDeskDriveAPI.getClient() == null){
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            AirDeskDriveAPI.setClient(mGoogleApiClient);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.getClient().connect();
        }
    }

    @Override
    protected void onPause() {
        if (AirDeskDriveAPI.getClient() != null) {
            AirDeskDriveAPI.getClient().disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("drive", "API client connected.");
        if(loggedUser.getDriveID() == null) {
            loggedUser = userManager.getLoggedDomainUser();
            if(loggedUser.getDriveID() == null) {
                AirDeskDriveAPI.setContext(this);
                AirDeskDriveAPI.createUserFolder(loggedUser.getEmail().toString());
                loggedUser = userManager.getLoggedDomainUser();
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("drive", "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                Log.d("drive", "started resolution");
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("drive", "activity result code: " + String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    AirDeskDriveAPI.getClient().connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoff) {
            if(userManager.signOut()){
                AirDeskDriveAPI.disconnect();
                Toast.makeText(this, "Successful LogOut", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else Toast.makeText(this, "LogOut Failed", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void listWorkspaces(View view){
        Intent intent = new Intent(this, WorkspaceListActivity.class);
        intent.putExtra(WORKSPACE_ACCESS_KEY, view.getTag().toString());
        startActivity(intent);
    }

    @Override
    public void processMessage(TermiteMessage message) {
        Toast.makeText(this, "Received Termite Message!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Contents: " + message.contents, Toast.LENGTH_LONG).show();
    }
}
