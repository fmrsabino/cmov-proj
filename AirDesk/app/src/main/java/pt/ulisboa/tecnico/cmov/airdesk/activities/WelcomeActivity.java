package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.User;

public class WelcomeActivity extends ActionBarActivity {

    public final static String WORKSPACE_ACCESS_KEY = "pt.ulisboa.tecnico.cmov.airdesk.WSACCESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        User loggedUser = DatabaseAPI.getLoggedDomainUser(new AirDeskDbHelper(this));

        TextView email = (TextView) findViewById(R.id.emailView);
        email.setText(loggedUser.getEmail());

        TextView nick = (TextView) findViewById(R.id.nickView);
        nick.setText(loggedUser.getNick());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logoff) {
            if(DatabaseAPI.signOut(new AirDeskDbHelper(this))){
                Toast.makeText(this, "Successful LogOut", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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
}