package pt.ulisboa.tecnico.cmov.airdesk.workspacemanager;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

    public boolean createFile(String name, String workspace) {
        Log.d(TAG, mContext.getFilesDir() + File.separator + workspace);
        File file = new File(mContext.getFilesDir() + File.separator + workspace, name);

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

    public boolean deleteFile(String name, String workspace) {
        File file = new File(mContext.getFilesDir() + File.separator + workspace, name);
        boolean success = false;
        if (file.exists()) {
            success = file.delete();
        } else {
            Log.d(TAG, "The file " + name + " doesn't exist");
        }

        return success;
    }

    public long getWorkspaceSize(String workspace){
        List<String> files = getFilesNames(workspace);
        long workspaceSize = 0;
        for (String file : files) {
            workspaceSize += getFileSize(file, workspace);
        }
        return workspaceSize;
    }

    public long getFileSize(String name, String workspace) {
        return new File(mContext.getFilesDir() + File.separator + workspace, name).length();
    }

    public String getFileContents(String name, String workspace){
        File file = new File(mContext.getFilesDir() + File.separator + workspace, name);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader reader = new FileReader(file);
            BufferedReader buf = new BufferedReader(reader);
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public void saveFileContents(String name, String workspace, String contents){
        File file = new File(mContext.getFilesDir() + File.separator + workspace, name);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDirectory(String directory) {
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
            deleteFile(file, directory);
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