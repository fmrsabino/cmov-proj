package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.activities.CreateWorkspaceActivity;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

public class WorkspaceManager {

    private Workspace workspace;

    public WorkspaceManager(Workspace workspace) {
        this.workspace = workspace;
    }

    //Adds or updates a workspace in a DB
    public void addWorkspace(Workspace workspace) {

    }

    public boolean sanitizeInputs(){
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
