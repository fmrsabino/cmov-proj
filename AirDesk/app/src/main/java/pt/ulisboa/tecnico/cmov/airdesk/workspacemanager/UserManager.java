package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;


import android.content.Context;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.User;

public class UserManager {

    private AirDeskDbHelper dbHelper;

    public UserManager(Context context) {
        this.dbHelper = AirDeskDbHelper.getInstance(context);
    }


    public String getLoggedUserSubscription(){
        return DatabaseAPI.getLoggedUserSubscription(dbHelper);
    }

    public void clearSubscribedWorkspaces() {
        DatabaseAPI.clearSubscribedWorkspaces(dbHelper);
    }

    public void setLoggedUserSubscription(String tags){
        DatabaseAPI.setLoggedUserSubscription(dbHelper, tags);
    }
    public boolean userLogin(String email, String password) {
        return DatabaseAPI.login(dbHelper, email, password);
    }


    public boolean registerUser(String nick, String email, String password) {
        return DatabaseAPI.register(dbHelper, nick, email, password);
    }


    public User getLoggedDomainUser() {
        return DatabaseAPI.getLoggedDomainUser(dbHelper);
    }

    public String getLoggedUser(){
        return DatabaseAPI.getLoggedUser(dbHelper);
    }

    public boolean signOut() {
        return DatabaseAPI.signOut(dbHelper);
    }

    public void registerDriveOptions(int option){
          DatabaseAPI.setUserDriveOption(dbHelper, option);
    }
}
