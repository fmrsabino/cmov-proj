package pt.ulisboa.tecnico.cmov.airdesk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.UserManager;


public class MainActivity extends ActionBarActivity {

    private UserManager userManager;

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

        return super.onOptionsItemSelected(item);
    }

    public void signIn(View view){
        Intent intent = new Intent(this, WelcomeActivity.class);
        EditText email = (EditText) findViewById(R.id.userIn);
        EditText password = (EditText) findViewById(R.id.userPwd);

        if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            if (userManager.userLogin( email.getText().toString(), password.getText().toString())) {
                Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
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
