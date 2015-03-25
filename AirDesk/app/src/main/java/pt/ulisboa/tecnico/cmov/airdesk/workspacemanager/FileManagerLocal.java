package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManagerLocal {

    public static String TAG = "FILE_MANAGER";

    private Context mContext;

    public FileManagerLocal(Context context) {
        mContext = context;
    }

    public boolean createFile(String name, String workspaceId) {
        Log.d(TAG, mContext.getFilesDir() + File.separator + workspaceId);
        File file = new File(mContext.getFilesDir() + File.separator + workspaceId, name);

        boolean success = false;
        try {
            success = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    //Only creates folder at the application root directory
    public boolean createFolder(String name) {
        File folder = new File(mContext.getFilesDir() + File.separator + name);

        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        return success;
    }

    public boolean removeFile(String name, String directory) {
        File file = new File(mContext.getFilesDir() + File.separator + directory, name);
        boolean success = false;
        if (file.exists()) {
            success = file.delete();
        } else {
            Log.d(TAG, "The file " + name + " doesn't exist");
        }

        return success;
    }

    public boolean removeDirectory(String directory) {
        boolean success = false;

        File file = new File(mContext.getFilesDir(), directory);

        if (file.isDirectory()) {
            formatDirectory(directory);
            success = file.delete();
        }

        return success;
    }

    public void formatDirectory(String directory) {
        for (String file : getFilesNames(directory)) {
            removeFile(file, directory);
        }
    }

    //Returns files and directory names in directory
    public List<String> getFilesNames(String directory) {
        File file = new File(mContext.getFilesDir() + File.separator + directory, "");
        String[] files = file.list();
        List<String> result = new ArrayList<>();
        if (files != null) {
            result.addAll(Arrays.asList(files));
        }
        return result;
    }

    //Returns files and directory names in root
    public List<String> getWorkspaces() {
        return getFilesNames("");
    }

    public String getSystemAvailableSpace() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());

        //Methods are deprecated as of API 18 but min support is API 14
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();

        return Formatter.formatFileSize(mContext, availableBlocks * blockSize);
    }
}