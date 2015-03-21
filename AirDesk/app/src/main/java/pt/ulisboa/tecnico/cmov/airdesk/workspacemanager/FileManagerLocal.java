package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManagerLocal {

    public static String TAG = "FILE_MANAGER";

    private Context mContext;

    public FileManagerLocal(Context context) {
        mContext = context;
    }

    public boolean createFile(String name) {
        File file = new File(mContext.getFilesDir(), name);

        boolean success = false;
        if (!file.exists()) {
            FileOutputStream fos  = null;
            try {
                fos = mContext.openFileOutput(name, Context.MODE_PRIVATE);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.e(TAG, "File already exists!");
        }

        return success;
    }

    public boolean createFolder(String name) {
        File folder = new File(name);

        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        return success;
    }

    public boolean removeFile(String name) {
        File file = new File(mContext.getFilesDir(), name);
        boolean success = false;
        if (file.exists()) {
            success = file.delete();
        } else {
            Log.d(TAG, "The file " + name + " doesn't exist");
        }

        return success;
    }

    public void formatWorkspace() {
        for (String file : getFilesNames()) {
            removeFile(file);
        }
    }

    public String[] getFilesNames() {
        File file = new File(mContext.getFilesDir(), "");
        return file.list();
    }
}