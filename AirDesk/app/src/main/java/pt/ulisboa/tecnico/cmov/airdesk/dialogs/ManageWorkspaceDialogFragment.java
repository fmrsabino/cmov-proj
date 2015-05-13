package pt.ulisboa.tecnico.cmov.airdesk.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.activities.WorkspaceListActivity;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.FileManagerLocal;
import pt.ulisboa.tecnico.cmov.airdesk.workspacemanager.WorkspaceManager;

public class ManageWorkspaceDialogFragment extends DialogFragment {

    private EditText quotaEditText;
    private CheckBox visibilityCheckBox;
    private LinearLayout keyHolder;
    private EditText keyWords;
    private String user;

    public interface ManageQuotaDialogListener {
        public void onWorkspaceSettingsDialogPositiveClick(DialogFragment dialog);
        public void onWorkspaceSettingsDialogNegativeClick(DialogFragment dialog);
    }

    ManageQuotaDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ManageQuotaDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_workspace_settings, null);
        quotaEditText = (EditText) view.findViewById(R.id.quota);
        visibilityCheckBox = (CheckBox) view.findViewById(R.id.is_public);
        keyHolder = (LinearLayout) view.findViewById(R.id.keyHolder);
        FileManagerLocal fileManager = FileManagerLocal.getInstance(getActivity());

        String workspaceName = getArguments().getString("workspace");
        user = getArguments().getString(WorkspaceListActivity.OWNER_KEY);

        WorkspaceManager manager = WorkspaceManager.getInstance(getActivity());
        Workspace workspace = manager.retrieveWorkspace(workspaceName, user);

        long workspaceSize = fileManager.getWorkspaceSize(workspaceName, user);
        workspaceSize += manager.getCurrentWorkspaceQuota(workspaceName, user);

        keyWords = (EditText) keyHolder.findViewById(R.id.keywords);
        keyWords.setText(workspace.getKeywords());

        quotaEditText.setText(String.valueOf(workspaceSize));

        if(workspace.isPublic() == 1){
            visibilityCheckBox.setChecked(true);
            keyHolder.setVisibility(LinearLayout.VISIBLE);
        } else{
            visibilityCheckBox.setChecked(false);
            keyHolder.setVisibility(LinearLayout.GONE);
        }

        visibilityCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    keyHolder.setVisibility(LinearLayout.VISIBLE);
                else
                    keyHolder.setVisibility(LinearLayout.GONE);
            }
        });

        builder.setTitle("Workspace Settings");
        builder.setView(view)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the positive button event back to the host activity
                        mListener.onWorkspaceSettingsDialogPositiveClick(ManageWorkspaceDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the negative button event back to the host activity
                        mListener.onWorkspaceSettingsDialogNegativeClick(ManageWorkspaceDialogFragment.this);
                    }
                });

        return builder.create();
    }

    public String getUpdatedQuota() {
        String quota = null;
        if (quotaEditText != null) {
            quota = quotaEditText.getText().toString();
        }
        return quota;
    }

    public boolean getWorkspaceVisibility(){
        return visibilityCheckBox.isChecked();
    }

    public String getWorkspaceKeywords(){ return keyWords.getText().toString();}
}
