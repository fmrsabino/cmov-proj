package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

public class WorkspaceManager {

    private Workspace workspace;
    private Context context;
    private DatabaseAPI dbAPI;
    AirDeskDbHelper dbHelper;

    public WorkspaceManager(Workspace workspace, Context context) {
        this.workspace = workspace;
        this.context = context;
    }

    //Adds or updates a workspace in a DB
    public void addWorkspace() {
        dbHelper = new AirDeskDbHelper(context);
        dbAPI = new DatabaseAPI();


        //TODO Change to find REAL logged-in user
        String loggedUser ="nick";

        dbAPI.createWorkspace(dbHelper, workspace.getName(), loggedUser, workspace.getQuota(), workspace.isPublic(), workspace.getKeywords(), workspace.getUsers());
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
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
