package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

public class WorkspaceManager {

    private Workspace workspace;
    private Context context;
    AirDeskDbHelper dbHelper;

    public WorkspaceManager(Workspace workspace, Context context) {
        this.workspace = workspace;
        this.context = context;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public WorkspaceManager(Context context) {
        this.context = context;
    }


    public void addWorkspace() {
        dbHelper = new AirDeskDbHelper(context);

        String loggedUser = DatabaseAPI.getLoggedUser(dbHelper);

        DatabaseAPI.createWorkspace(dbHelper, workspace.getName(), loggedUser, workspace.getQuota(), workspace.isPublic(), workspace.getKeywords(), workspace.getUsers());
    }

    public void addViewer(String viewer, String ws_name) {
        dbHelper = new AirDeskDbHelper(context);

        DatabaseAPI.addUserToWorkspace(dbHelper, viewer, ws_name );
    }

    public List<Workspace> retrieveOwnedWorkspaces(){
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getOwnedWorkspaces(dbHelper);
    }

    public List<Workspace> retrieveForeignWorkspaces() {
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getForeignWorkspaces(dbHelper);
    }
    public Workspace retrieveWorkspace(String ws_name){
        dbHelper = new AirDeskDbHelper(context);

        return DatabaseAPI.getWorkspace(dbHelper, ws_name);
    }

    public boolean sanitizeBlankInputs(){
        boolean incompleteFields = false;

        //sanitize inputs
        if(workspace.getName().equals(""))
            incompleteFields=true;
        else if(workspace.getQuota() < 0)
            incompleteFields=true;
        else if(workspace.getKeywords().equals(""))
            incompleteFields=true;
        return incompleteFields;
    }

}
