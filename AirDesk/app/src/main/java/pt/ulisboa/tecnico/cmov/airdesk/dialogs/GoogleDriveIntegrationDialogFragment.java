package pt.ulisboa.tecnico.cmov.airdesk.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public class GoogleDriveIntegrationDialogFragment extends DialogFragment {

    public interface DriveIntegrationListener {
        public void onYesClick(DialogFragment dialog);
        public void onNoClick(DialogFragment dialog);
        public void onLaterClick(DialogFragment dialog);
    }

    DriveIntegrationListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DriveIntegrationListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_drive_integration, null);

        builder.setTitle("Google Drive Integration");
        builder.setView(view)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the positive button event back to the host activity
                        mListener.onYesClick(GoogleDriveIntegrationDialogFragment.this);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the negative button event back to the host activity
                        mListener.onNoClick(GoogleDriveIntegrationDialogFragment.this);
                    }
                })
                .setNeutralButton("Remind Me Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the negative button event back to the host activity
                        mListener.onLaterClick(GoogleDriveIntegrationDialogFragment.this);
                    }
                });

        return builder.create();
    }
}
