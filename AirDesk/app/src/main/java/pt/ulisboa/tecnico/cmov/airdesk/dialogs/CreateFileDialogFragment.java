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

public class CreateFileDialogFragment extends DialogFragment {

    private EditText editText;

    public interface CreateFileDialogListener  {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    CreateFileDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CreateFileDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_file, null);
        editText = (EditText) view.findViewById(R.id.fileName);
        builder.setTitle("New File");
        builder.setView(view)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(CreateFileDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(CreateFileDialogFragment.this);
                    }
                });

        return builder.create();
    }

    public String getFileName() {
        String fileName = null;
        if (editText != null) {
            fileName = editText.getText().toString();
        }
        return fileName;
    }
}
