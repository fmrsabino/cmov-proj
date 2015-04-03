package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DatabaseAPI.getLoggedDomainUser(new AirDeskDbHelper(this))!=null){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void signIn(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        EditText email = (EditText) findViewById(R.id.userIn);
        if(!email.getText().toString().isEmpty()) {
            if (DatabaseAPI.login(new AirDeskDbHelper(this), email.getText().toString())) {
                Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "Please write your nick", Toast.LENGTH_SHORT).show();
    }

    public void register(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        EditText nick = (EditText) findViewById(R.id.userNick);
        EditText email = (EditText) findViewById(R.id.userEmail);
        if(!nick.getText().toString().isEmpty() && !email.getText().toString().isEmpty()) {
            if (DatabaseAPI.register(new AirDeskDbHelper(this),
                    nick.getText().toString(), email.getText().toString())) {
                Toast.makeText(this, "Successful Registration", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else Toast.makeText(this,"Registration Failed",Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this,"Name and Email are Required",Toast.LENGTH_SHORT).show();
    }
}
