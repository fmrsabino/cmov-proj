package pt.ulisboa.tecnico.cmov.airdesk.drive;

import android.content.Context;
import android.text.Editable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;

public abstract class AirDeskDriveAPI {

    private static GoogleApiClient mGoogleApiClient;
    private static String userDriveID = null;
    private static Context driveContext = null;
    private static DriveId folderDriveID = null;

    public static void setClient(GoogleApiClient client){
        mGoogleApiClient = client;
    }

    public static GoogleApiClient getClient(){
        return mGoogleApiClient;
    }

    private static void setUserDriveID(String driveID){
        userDriveID = driveID;
    }

    public static String getUserDriveID(){
        return userDriveID;
    }

    public static void setContext(Context context){
        driveContext = context;
    }

    public static void disconnect(){
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }


    //add file to folder by folderID
    public static void createFile(String folderID, final String fileName, final String fileContents) {

        final ResultCallback<DriveFolder.DriveFileResult> fileCallback =
                new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d("drive", "Error while trying to create the file");
                            return;
                        }
                        Log.d("drive", "Created a file: " + result.getDriveFile().getDriveId());
                    }
                };

        final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
           new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.d("drive", "Error while trying to create new file contents");
                        return;
                    }

                    final DriveContents driveContents = result.getDriveContents();

                    // write content to DriveContents
                    OutputStream outputStream = driveContents.getOutputStream();
                    Writer writer = new OutputStreamWriter(outputStream);
                    try {
                        writer.write(fileContents);
                        writer.close();
                    } catch (IOException e) {
                        Log.d("drive", e.getMessage());
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(fileName)
                            .setMimeType("text/plain")
                            .setStarred(true).build();

                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, folderDriveID);
                    folder.createFile(mGoogleApiClient, changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
           };

        folderDriveID = DriveId.decodeFromString(folderID);
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                 .setResultCallback(driveContentsCallback);
    }


    //add folder to root (easy one)
    public static void createUserFolder(String user) {

        final ResultCallback<DriveFolder.DriveFolderResult> createUserFolderCallback = new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.d("drive", "Error while trying to create the user folder");
                    return;
                }
                Log.d("drive", "Created user folder with id:" + result.getDriveFolder().getDriveId());
                setUserDriveID(result.getDriveFolder().getDriveId().encodeToString());
                if(userDriveID != null){
                    DatabaseAPI.setUserDriveID(AirDeskDbHelper.getInstance(driveContext),userDriveID);
                }
            }
        };

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(user).build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
                mGoogleApiClient, changeSet).setResultCallback(createUserFolderCallback);

    }


}
