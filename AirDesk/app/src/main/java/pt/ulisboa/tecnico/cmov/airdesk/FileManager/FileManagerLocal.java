package pt.ulisboa.tecnico.cmov.airdesk.FileManager;

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

    public void createFile(String name) {
        File file = new File(mContext.getFilesDir(), name);
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.e(TAG, "File already exists!");
        }
    }

    public void createFolder(String name) {
        File folder = new File(name);

        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success) {
            Log.i(TAG, "Folder created successfuly");
        } else {
            Log.e(TAG, "Error creating folder");
        }
    }

    public void removeFile(String name) {
        Log.d(TAG, "removeFile()");

        File file = new File(mContext.getFilesDir(), name);
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                Log.d(TAG, "File " + name + " removed successfully");
            } else {
                Log.e(TAG, "File " + name + "NOT removed");
            }
        } else {
            Log.e(TAG, "The file " + name + " doesn't exist");
        }
    }

    public void formatWorkspace() {
        for (String file : listFilesNames()) {
            removeFile(file);
        }
    }

    public String[] listFilesNames() {
        Log.d(TAG, "listFilesNames()");
        File file = new File(mContext.getFilesDir(), "");
        return file.list();
    }
}