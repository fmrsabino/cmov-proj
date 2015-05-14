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

import pt.ulisboa.tecnico.cmov.airdesk.drive.AirDeskDriveAPI;

public class FileManagerLocal {

    private static FileManagerLocal fileManager = null;
    public static String TAG = "FILE_MANAGER";

    private Context mContext;
    private List<String[]> lockedFiles;

    private FileManagerLocal(Context context) {
        mContext = context;
        lockedFiles = new ArrayList<>();
    }

    public static FileManagerLocal getInstance(Context context) {
        if (fileManager == null) {
            fileManager = new FileManagerLocal(context.getApplicationContext());
        }
        return fileManager;
    }

    public boolean createFile(String name, String workspace, String user) {
        Log.d(TAG, mContext.getFilesDir() + File.separator + workspace);
        File file = new File(mContext.getFilesDir() + File.separator + user + File.separator + workspace, name);

        boolean success = false;
        try {
            success = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    //Only creates folder at the application root directory
    public boolean createWorkspace(String name, String user) {
        File folder = new File(mContext.getFilesDir() + File.separator
                + user + File.separator + name);

        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        return success;
    }

    public boolean deleteFile(String name, String workspace, String user, String workspaceDriveID) {
        File file = new File(mContext.getFilesDir() + File.separator + user + File.separator + workspace, name);
        boolean success = false;
        if (file.exists()) {
            success = file.delete();
        } else {
            Log.d(TAG, "The file " + name + " doesn't exist");
        }
        if(AirDeskDriveAPI.getClient() != null) {
            Log.d("drive", "deleting file: " + name);
            AirDeskDriveAPI.deleteFileFromFolder(workspaceDriveID, name);
        }

        return success;
    }

    public long getWorkspaceSize(String workspace, String user){
        List<String> files = getFilesNames(workspace, user);
        long workspaceSize = 0;
        for (String file : files) {
            workspaceSize += getFileSize(file, workspace, user);
        }
        return workspaceSize;
    }

    public long getFileSize(String name, String workspace, String user) {
        return new File(mContext.getFilesDir() + File.separator +
                user + File.separator +
                workspace, name).length();
    }

    public String getFileContents(String name, String workspace, String user) {
        File file = new File(mContext.getFilesDir() + File.separator + user + File.separator + workspace, name);
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

    public void saveFileContents(String name, String workspace, String user, String contents) {
        File file = new File(mContext.getFilesDir() + File.separator + user + File.separator + workspace, name);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteWorkspace(String workspace, String user, String workspaceDriveID) {
        boolean success = false;

        File file = new File(mContext.getFilesDir() + File.separator + user, workspace);

        if (file.isDirectory()) {
            formatWorkspace(workspace, user, workspaceDriveID);
            success = file.delete();
        }

        return success;
    }

    public void formatWorkspace(String workspace, String user, String workspaceDriveID) {
        for (String file : getFilesNames(workspace, user)) {
            deleteFile(file, workspace, user, workspaceDriveID);
        }
    }

    //Returns files and directory names in directory
    public List<String> getFilesNames(String workspace, String user) {
        File file = new File(mContext.getFilesDir() + File.separator + user + File.separator + workspace, "");
        String[] files = file.list();
        List<String> result = new ArrayList<>();
        if (files != null) {
            result.addAll(Arrays.asList(files));
        }
        return result;
    }

    public String getSystemAvailableSpace() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());

        //Methods are deprecated as of API 18 but min support is API 14
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();

        return Formatter.formatFileSize(mContext, availableBlocks * blockSize);
    }

    public boolean lockFile(String fileName, String wsName, String wsOwner){
        String[] fileTuple = {fileName, wsName, wsOwner};
        boolean reserved = true;

        for(String[] s : lockedFiles){
            Log.d("LOCKS ON HOLD-LOCK FILE", s[0]+s[1]+s[2]);
        }

        for (String[] f : lockedFiles) {
            if ((f[0].equals(fileTuple[0]) && f[1].equals(fileTuple[1]) && f[2].equals(fileTuple[2]))) {
                reserved = false;
            }
        }
        if(reserved)
            lockedFiles.add(fileTuple);

        return reserved;
    }

    public List<String[]> getLockedFiles(){ return lockedFiles;}

    public void removeLock(String[] file) {
        String[] toRemove = file;
        Log.d("CONTAGEM", ""+lockedFiles.size());
        for (String[] f : lockedFiles) {
            if (f[0].equals(file[0]) && f[1].equals(file[1]) && f[2].equals(file[2])) {
                toRemove = f;
                break;
            }
        }
        lockedFiles.remove(toRemove);
        Log.d("CONTAGEM DEPOIS", "" + lockedFiles.size());
    }
}