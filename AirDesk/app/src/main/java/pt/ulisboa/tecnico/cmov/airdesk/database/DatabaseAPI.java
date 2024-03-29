package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.domain.User;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

public class DatabaseAPI {

    private static SQLiteDatabase db;

    public static boolean login(AirDeskDbHelper dbHelper, String email, String password){
        db = dbHelper.getReadableDatabase();
        String query = "Select * from " + AirDeskContract.Users.TABLE_NAME +
                " where " + AirDeskContract.Users.COLUMN_NAME_EMAIL + " = '" + email + "' AND " +
                AirDeskContract.Users.COLUMN_NAME_PASSWORD + " = '" + password + "'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        } else{
            ContentValues values = new ContentValues();
            values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 1);

            String selection = AirDeskContract.Users.COLUMN_NAME_EMAIL + " = ?";
            String[] selectionArgs = { email };

            int count = db.update(
                    AirDeskContract.Users.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            cursor.close();
            return count != 0;
        }
    }

    public static boolean signOut(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 0);

        String selection = AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?";
        String[] selectionArgs = { "1" };

        int count = db.update(
                AirDeskContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count != 0;
    }


    public static boolean setLoggedUserSubscription(AirDeskDbHelper dbHelper, String keywords){
        db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Users.COLUMN_NAME_SUBSCRIPTION, keywords);

        String selection = AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?";
        String[] selectionArgs = { "1" };

        int count = db.update(
                AirDeskContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count != 0;
    }

    public static boolean setUserDriveID(AirDeskDbHelper dbHelper, String driveID){
        db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Users.COLUMN_NAME_FOLDERID, driveID);

        String selection = AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?";
        String[] selectionArgs = { "1" };

        int count = db.update(
                AirDeskContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count != 0;
    }

    public static String getLoggedUser(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();

        String[] projection = {
                AirDeskContract.Users.COLUMN_NAME_EMAIL};

        Cursor c = db.query(
                AirDeskContract.Users.TABLE_NAME,
                projection,
                AirDeskContract.Users.COLUMN_NAME_LOGGED + " = 1",
                null,
                null,
                null,
                null
        );


        if(c.moveToFirst()) {
            String result = c.getString(0);
            c.close();
            return result;
        } else {
            c.close();
            return null;
        }
    }

    public static User getLoggedDomainUser(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();
        String nick = null;
        String email = null;
        String driveID = null;
        int driveOpts = 0;

        String[] projection = {AirDeskContract.Users.COLUMN_NAME_EMAIL,
                AirDeskContract.Users.COLUMN_NAME_NICK,
                AirDeskContract.Users.COLUMN_NAME_FOLDERID,
                AirDeskContract.Users.COLUMN_NAME_DRIVE_OPTS};

        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                AirDeskContract.Users.TABLE_NAME,
                projection,
                AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?",
                selectionArgs,
                null,
                null,
                null
        );

        while(c.moveToNext()){
            email = c.getString(0);
            nick = c.getString(1);
            driveID = c.getString(2);
            driveOpts = c.getInt(3);
        }

        c.close();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(nick)){
            return null;
        } else return new User(nick,email,driveID, driveOpts);
    }

    public static String getLoggedUserSubscription(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();

        String[] projection = {
                AirDeskContract.Users.COLUMN_NAME_SUBSCRIPTION};

        Cursor c = db.query(
                AirDeskContract.Users.TABLE_NAME,
                projection,
                AirDeskContract.Users.COLUMN_NAME_LOGGED + " = 1",
                null,
                null,
                null,
                null
        );


        if(c.moveToFirst()) {
            String result = c.getString(0);
            c.close();
            return result;
        } else {
            c.close();
            return null;
        }
    }

    public static boolean register(AirDeskDbHelper dbHelper, String nick, String email, String password){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "Select * from " + AirDeskContract.Users.TABLE_NAME +
                " where " + AirDeskContract.Users.COLUMN_NAME_EMAIL + " = '" + email + "'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }

        values.put(AirDeskContract.Users.COLUMN_NAME_NICK, nick);
        values.put(AirDeskContract.Users.COLUMN_NAME_EMAIL, email);
        values.put(AirDeskContract.Users.COLUMN_NAME_PASSWORD, password);
        values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 1);
        values.put(AirDeskContract.Users.COLUMN_NAME_DRIVE_OPTS, 0);

        long row = db.insert(AirDeskContract.Users.TABLE_NAME, null,values);
        return row != -1;
    }

    public static boolean createWorkspace(AirDeskDbHelper dbHelper,
                                          String name,
                                          String owner,
                                          int quota,
                                          int is_public,
                                          String keywords,
                                          List<String> viewers){

        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AirDeskContract.Workspaces.COLUMN_NAME_NAME, name);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_OWNER, owner);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, quota);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC, is_public);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS, keywords);

        //start transaction
        db.beginTransaction();
            //first insert
            long row = db.insert(AirDeskContract.Workspaces.TABLE_NAME, null, values);
            //second insert
            boolean status = addUsersToWorkspace(dbHelper, viewers, name, owner);
            //check status and decide on commit or rollback
            if(status && (row != -1))
                //this means -> commit transaction
                db.setTransactionSuccessful();
        //close transaction
        db.endTransaction();

        return (row != -1 && status);
    }

    public static boolean addUserToWorkspace(AirDeskDbHelper dbHelper, String viewer, String wsname, String user){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean smoothInsert = true;

        values.put(AirDeskContract.Viewers.COLUMN_NAME_EMAIL, viewer);
        values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, wsname);
        values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER, user);
        long row = db.insert(AirDeskContract.Viewers.TABLE_NAME, null,values);
        if(row == -1)
            smoothInsert = false;

        return smoothInsert;
    }


    public static boolean addUsersToWorkspace(AirDeskDbHelper dbHelper, List<String> viewers, String wsname, String user){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean smoothInsert = true;

        for (String v : viewers) {
            values.put(AirDeskContract.Viewers.COLUMN_NAME_EMAIL, v);
            values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, wsname);
            values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER, user);
            long row = db.insert(AirDeskContract.Viewers.TABLE_NAME, null,values);
            if(row == -1)
                smoothInsert = false;
        }

        return smoothInsert;
    }

    public static void deleteViewer(AirDeskDbHelper dbHelper, String v_email, String ws_name, String owner) {
        db = dbHelper.getWritableDatabase();

        db.delete(
                AirDeskContract.Viewers.TABLE_NAME,
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + "= \'" + v_email + "\' AND " +
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + "= \'" + ws_name +"\' AND " +
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER + " = \'" + owner + "\'",
                null);

    }


    public static List<Workspace> getOwnedWorkspaces(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();
        List<String> viewers;
        List<Workspace> wsList= new ArrayList<>();

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER,
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

        String user = getLoggedUser(dbHelper);
        String[] selectionArgs = { user };

        String ws_sortOrder =
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " DESC";

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,  // The table to query
                ws_projection,                               // The columns to return
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " LIKE ?",  // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                ws_sortOrder                                 // The sort order
        );


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " ASC";

        while (c.moveToNext()) {
            viewers = new ArrayList<>();

            String[] selectionArgs2 = { c.getString(0) };

            Cursor c2 = db.query(
                    AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                    v_projection,
                    AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " LIKE ?",
                    selectionArgs2,
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    v_sortOrder                                 // The sort order
            );

            while (c2.moveToNext()) {
                viewers.add(c2.getString(0));
            }
            c2.close();

            wsList.add(new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers, user));
        }
        c.close();
        return wsList;
    }

    public static List<Workspace> getForeignWorkspaces(AirDeskDbHelper dbHelper) {
        db = dbHelper.getReadableDatabase();
        List<Workspace> workspaces = new ArrayList<>();
        List<String> viewers;
        List<Workspace> wsList= new ArrayList<>();


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER};

        String[] v_selectionArgs = { getLoggedUser(dbHelper) };

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " ASC";

        Cursor c = db.query(
                AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                v_projection,                               // The columns to return
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " LIKE ?",  // The columns for the WHERE clause
                v_selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                v_sortOrder                                 // The sort order
        );


        while (c.moveToNext()) {
            workspaces.add(new Workspace(c.getString(0), -1, -1, null, null, c.getString(1)));
        }
        c.close();

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER,
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

        for(Workspace ws : workspaces) {
            String[] selectionArgs = {ws.getName(), ws.getOwner()};

            String ws_sortOrder =
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME + " DESC";

            Cursor c2 = db.query(
                    AirDeskContract.Workspaces.TABLE_NAME,  // The table to query
                    ws_projection,                               // The columns to return
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME + " LIKE ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",  // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    ws_sortOrder                                 // The sort order
            );

            while (c2.moveToNext()) {
                viewers = new ArrayList<>();

                String[] selectionArgs2 = {c2.getString(0)};

                Cursor c3 = db.query(
                        AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                        v_projection,
                        AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " LIKE ?",
                        selectionArgs2,
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        v_sortOrder                                 // The sort order
                );

                while(c3.moveToNext()) {
                    viewers.add(c3.getString(0));
                }
                c3.close();

                wsList.add(new Workspace(c2.getString(0), c2.getInt(2), c2.getInt(3), c2.getString(4), viewers, c2.getString(1)));
            }
            c2.close();
        }
        return wsList;
    }

    public static Workspace getWorkspace(AirDeskDbHelper dbHelper, String ws_name, String user){
        db = dbHelper.getReadableDatabase();
        List<String> viewers = new ArrayList<>();
        Workspace ws = new Workspace("0", 0, 0, "0", null, "0");

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER,
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

        String[] selectionArgs = { ws_name, user };

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                ws_projection,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " LIKE ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                selectionArgs,
                null,
                null,
                null
        );


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " ASC";



        Cursor c2 = db.query(
                AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                v_projection,
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " LIKE ? AND " + AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER + " = ?",
                selectionArgs,
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                v_sortOrder                                 // The sort order
        );

        while (c2.moveToNext()) {
            viewers.add(c2.getString(0));
        }

        if(c.moveToFirst())
        ws = new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers, c.getString(1));

        c.close();
        c2.close();
        return ws;
    }

    public static boolean deleteLocalWorkspace(AirDeskDbHelper dbHelper, String ws_name) {
        db = dbHelper.getWritableDatabase();
        boolean smoothDelete = true;

        String owner_email = getLoggedUser(dbHelper);

        if(db.delete(AirDeskContract.Viewers.TABLE_NAME, AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + "= \'" + ws_name + "\'" , null) < 0)
            smoothDelete = false;

        if(db.delete(AirDeskContract.Workspaces.TABLE_NAME, AirDeskContract.Workspaces.COLUMN_NAME_NAME + "= \'" + ws_name + "\' AND " +
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER + "= \'" + owner_email +"\'", null) < 0)
            smoothDelete = false;

        return smoothDelete;
    }

    // Updates the quota of the workspace with the remaining files
    public static boolean updateWorkspaceQuota(AirDeskDbHelper dbHelper, String workspace, long bytes, String user) {
        double currentQuota = getCurrentQuota(dbHelper, workspace, user);

        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, currentQuota + bytes);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user}
        );

        return true;
    }

    public static boolean setWorkspaceQuota(AirDeskDbHelper dbHelper, String workspace, long bytes, String user) {
        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, bytes);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user}
        );

        return true;
    }

    public static boolean setWorkspaceDriveID(AirDeskDbHelper dbHelper, String workspace, String user, String folderID) {
        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_FOLDERID, folderID);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user}
        );

        return true;
    }

    public static boolean setWorkspaceVisibility(AirDeskDbHelper dbHelper, String workspace, int visibility, String user){
        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC, visibility);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user}
        );

        return true;
    }

    public static boolean setWorkspaceKeywords(AirDeskDbHelper dbHelper, String workspace, String keywords, String user){
        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS, keywords);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " + AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user}
        );

        return true;
    }

    // Returns the current quota value in bytes for workspace
    public static long getCurrentQuota(AirDeskDbHelper dbHelper, String workspace, String user) {
        db = dbHelper.getReadableDatabase();

        long workspaceQuota = 0;
        String[] projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, AirDeskContract.Workspaces.COLUMN_NAME_OWNER
        };

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                projection,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " +  AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user},
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            workspaceQuota = c.getLong(0);
        }

        c.close();
        return workspaceQuota;
    }

    // Returns the current quota value in bytes for workspace
    public static String getWorkspaceDriveID(AirDeskDbHelper dbHelper, String workspace, String user) {
        db = dbHelper.getReadableDatabase();

        String driveID = null;
        String[] projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_FOLDERID, AirDeskContract.Workspaces.COLUMN_NAME_OWNER
        };

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                projection,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ? AND " +  AirDeskContract.Workspaces.COLUMN_NAME_OWNER + " = ?",
                new String[]{workspace, user},
                null,
                null,
                null
        );

        Log.d("DB", "OWNER IS: " + user);

        if (c.moveToFirst()) {
            driveID = c.getString(0);
        }

        c.close();
        return driveID;
    }




    public static void subscribeWorkspaces(AirDeskDbHelper dbHelper, String viewer, List<String> ws_keywords){
        db = dbHelper.getReadableDatabase();

        List<String> ws_tags = new ArrayList<>();
        boolean contains;

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER};

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                ws_projection,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC + " = 1",
                null,
                null,
                null,
                null
        );

        while (c.moveToNext()) {
            ws_tags.clear();
            contains = false;

            ws_tags = new ArrayList<>(Arrays.asList(c.getString(1).split("\\s*,\\s*")));

            for(String k : ws_keywords) {
                if (ws_tags.contains(k)) {
                    contains = true;
                    break;
                }
            }
            if(contains)
                if(!userAlreadySubscribed(dbHelper, viewer, c.getString(0), c.getString(2)))
                    addUserToWorkspace(dbHelper, viewer, c.getString(0), c.getString(2));
        }

        c.close();
    }

    private static boolean userAlreadySubscribed(AirDeskDbHelper dbHelper, String viewer, String wsName, String owner) {
        db = dbHelper.getReadableDatabase();

        String[] selectionArgs = { viewer, wsName, owner };

        String[] ws_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL,
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE,
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER};

        Cursor c = db.query(
                AirDeskContract.Viewers.TABLE_NAME,
                ws_projection,
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " LIKE ? AND " + AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " LIKE ? AND " + AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER + " LIKE ?",
                selectionArgs,
                null,
                null,
                null
        );

         return c.moveToFirst();
    }

    public static void clearSubscribedWorkspaces(AirDeskDbHelper dbHelper) {
        db = dbHelper.getWritableDatabase();

        String loggedUser = getLoggedUser(dbHelper);

        db.delete(AirDeskContract.Viewers.TABLE_NAME, AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " in (SELECT " +
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " FROM " + AirDeskContract.Workspaces.TABLE_NAME +
                " WHERE " + AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC + "= 1)" + "AND " + AirDeskContract.Viewers.COLUMN_NAME_EMAIL + "= \'" + loggedUser + "\'", null);
    }

    public static List<Workspace> remoteRetrieveForeignWorkspaces(AirDeskDbHelper dbHelper, String requestingUser) {
        db = dbHelper.getReadableDatabase();
        List<Workspace> wsList = new ArrayList<>();

        String[] projection = {
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE,
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE_OWNER};

        String[] v_selectionArgs = {requestingUser};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " ASC";

        Cursor c = db.query(
                AirDeskContract.Viewers.TABLE_NAME,
                projection,
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " LIKE ?",
                v_selectionArgs,
                null,
                null,
                v_sortOrder
        );


        while (c.moveToNext()) {
            Workspace ws = new Workspace();
            ws.setName(c.getString(0));
            ws.setOwner(c.getString(1));
            ws.setQuota((int)getCurrentQuota(dbHelper, c.getString(0), c.getString(1)));
            wsList.add(ws);
        }

        c.close();

        return wsList;
    }

    public static void setUserDriveOption(AirDeskDbHelper dbHelper, int option) {
        db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Users.COLUMN_NAME_DRIVE_OPTS, option);

        String selection = AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?";
        String[] selectionArgs = { "1" };

        int count = db.update(
                AirDeskContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
}

