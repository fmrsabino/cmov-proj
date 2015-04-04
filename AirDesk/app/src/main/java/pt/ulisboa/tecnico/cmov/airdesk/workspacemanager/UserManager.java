package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;


import android.content.Context;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.User;

public class UserManager {

    private Context context;
    private AirDeskDbHelper dbHelper;

    public UserManager(Context applicationContext) {
        this.context = applicationContext;
    }


    public String getLoggedUserSubscription(){
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getLoggedUserSubscription(dbHelper);
    }

    public void clearSubscribedWorkspaces() {
        dbHelper = new AirDeskDbHelper(context);

        DatabaseAPI.clearSubscribedWorkspaces(dbHelper);
    }

    public void setLoggedUserSubscription(String tags){
        dbHelper = new AirDeskDbHelper(context);

        DatabaseAPI.setLoggedUserSubscription(dbHelper, tags);
    }
    public boolean userLogin(String email) {
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.login(dbHelper, email);
    }


    public boolean registerUser(String nick, String email) {
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.register(dbHelper, nick, email);
    }


    public User getLoggedDomainUser() {
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getLoggedDomainUser(dbHelper);
    }

    public String getLoggedUser(){
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getLoggedUser(dbHelper);
    }

    public boolean signOut() {
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.signOut(dbHelper);
    }
}
