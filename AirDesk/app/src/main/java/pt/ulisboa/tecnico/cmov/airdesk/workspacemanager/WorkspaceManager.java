package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;
import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

public class WorkspaceManager {

    private AirDeskDbHelper dbHelper;

    public WorkspaceManager(Context context) {
        this.dbHelper = AirDeskDbHelper.getInstance(context);
    }


    public boolean addWorkspace(Workspace workspace) {
        String loggedUser = DatabaseAPI.getLoggedUser(dbHelper);

        return DatabaseAPI.createWorkspace(dbHelper, workspace.getName(), loggedUser, workspace.getQuota(), workspace.isPublic(), workspace.getKeywords(), workspace.getUsers());
    }

    public void addViewer(String viewer, String ws_name) {
        DatabaseAPI.addUserToWorkspace(dbHelper, viewer, ws_name );
    }

    public List<Workspace> retrieveOwnedWorkspaces(){
        return DatabaseAPI.getOwnedWorkspaces(dbHelper);
    }

    public List<Workspace> retrieveForeignWorkspaces() {
        return DatabaseAPI.getForeignWorkspaces(dbHelper);
    }

    public Workspace retrieveWorkspace(String ws_name){
        return DatabaseAPI.getWorkspace(dbHelper, ws_name);
    }


    public long getCurrentWorkspaceQuota(String ws_name) {
        return DatabaseAPI.getCurrentQuota(dbHelper, ws_name);
    }

    public void updateWorkspaceQuota(String ws_name, long fileSize){
        DatabaseAPI.updateWorkspaceQuota(dbHelper, ws_name, fileSize);
    }

    public boolean sanitizeBlankInputs(Workspace workspace){
        boolean incompleteFields = false;

        if(TextUtils.isEmpty(workspace.getName()))
            incompleteFields=true;
        else if(workspace.getQuota() < 0)
            incompleteFields=true;
        else if(TextUtils.isEmpty(workspace.getKeywords()) && (workspace.isPublic() == 1))
            incompleteFields=true;
        return incompleteFields;
    }

    public void deleteWorkspaceViewer(String viewerID, String ws_name) {
        DatabaseAPI.deleteViewer(dbHelper, viewerID, ws_name);

    }

    public boolean deleteOwnedWorkspace(String ws_name) {
        return DatabaseAPI.deleteLocalWorkspace(dbHelper, ws_name);
    }

    public void unregisterForeignWorkspace(String ws_name) {
        String loggedInUser = DatabaseAPI.getLoggedUser(dbHelper);
        DatabaseAPI.deleteViewer(dbHelper, loggedInUser, ws_name);
    }

    public void subscribeWorkspaces(List<String> tags){
        DatabaseAPI.subscribeWorkspaces(dbHelper, tags);
    }

    public void setWorkspaceQuota(String ws_name, long bytes) {
        DatabaseAPI.setWorkspaceQuota(dbHelper, ws_name, bytes);
    }

}
