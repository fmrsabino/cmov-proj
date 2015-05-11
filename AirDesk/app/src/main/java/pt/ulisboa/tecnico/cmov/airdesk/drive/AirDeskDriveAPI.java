package pt.ulisboa.tecnico.cmov.airdesk.drive;

import android.content.Context;
import android.text.Editable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.database.AirDeskDbHelper;
import pt.ulisboa.tecnico.cmov.airdesk.database.DatabaseAPI;
import pt.ulisboa.tecnico.cmov.airdesk.utilities.WorkspacesListAdapter;

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


    public static void createEmptyFile(String folderID, String fileName){

        final ResultCallback<DriveFolder.DriveFileResult> fileCallback =
                new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d("drive", "Error while trying to create the file");
                            return;
                        }
                        Log.d("drive", "Created an empty file: " + result.getDriveFile().getDriveId());
                    }
           };

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .setMimeType("text/plain")
                .setStarred(false).build();

        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, DriveId.decodeFromString(folderID));
        folder.createFile(mGoogleApiClient, changeSet, null)
                .setResultCallback(fileCallback);
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
                            .setStarred(false).build();

                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, folderDriveID);
                    folder.createFile(mGoogleApiClient, changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
           };

        folderDriveID = DriveId.decodeFromString(folderID);
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                 .setResultCallback(driveContentsCallback);
    }


    // add folder to user folder (workspace)
    public static void createWorkspaceFolder(final String workspace, final String user,  String userFolderID) {

        final ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
                ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d("drive", "Error while trying to create the user folder");
                            return;
                        }
                        Log.d("drive", "Created workspace folder with id:" + result.getDriveFolder().getDriveId());
                        String encodedDriveID = result.getDriveFolder().getDriveId().encodeToString();
                        if(encodedDriveID != null){
                            DatabaseAPI.setWorkspaceDriveID(AirDeskDbHelper.getInstance(driveContext), workspace, user, encodedDriveID);
                        }

                    }
        };

        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, DriveId.decodeFromString(userFolderID));
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(workspace).build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(folderCreatedCallback);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //add folder to root (user folder)
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

    //delete file from folder
    public static void deleteFileFromFolder(String folderID, String fileName){

        final ResultCallback<Status> trashStatusCallback =
            new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (!status.isSuccess()) {
                        Log.d("drive", status.getStatusMessage());
                        return;
                    }
                    Log.d("drive", "file deleted successfully");
                }
        };

        final ResultCallback<DriveApi.MetadataBufferResult> metadataCallback = new
                ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d("drive", "Problem while retrieving files");
                            return;
                        }

                        MetadataBuffer buffer = result.getMetadataBuffer();

                        if(buffer.getCount() == 0){
                            Log.d("drive", "query files returned 0 results");
                            return;
                        }
                        Metadata fileMeta = buffer.get(0);

                        DriveResource driveResource = Drive.DriveApi.getFile(mGoogleApiClient,
                                fileMeta.getDriveId());

                        driveResource.trash(mGoogleApiClient)
                                .setResultCallback(trashStatusCallback);

                        buffer.close();
                    }
                };

        DriveFolder folder;
        if(folderID == null){
            folder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        }
        else folder = Drive.DriveApi.getFolder(mGoogleApiClient, DriveId.decodeFromString(folderID));

        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(Filters.eq(SearchableField.TRASHED, false));
        filters.add(Filters.eq(SearchableField.TITLE, fileName));

        Query query = new Query.Builder()
                .addFilter(Filters.and(filters)).build();

        folder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(metadataCallback);

    }
}
