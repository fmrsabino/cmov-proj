package pt.ulisboa.tecnico.cmov.airdesk.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public class ManageQuotaDialogFragment extends DialogFragment {

    private EditText quotaEditText;

    public interface ManageQuotaDialogListener {
        public void onQuotaDialogPositiveClick(DialogFragment dialog);
        public void onQuotaDialogNegativeClick(DialogFragment dialog);
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

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_quota, null);
        quotaEditText = (EditText) view.findViewById(R.id.quota);
        builder.setTitle("New Quota (in bytes)");
        builder.setView(view)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the positive button event back to the host activity
                        mListener.onQuotaDialogPositiveClick(ManageQuotaDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the negative button event back to the host activity
                        mListener.onQuotaDialogNegativeClick(ManageQuotaDialogFragment.this);
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
}
