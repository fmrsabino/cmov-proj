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

    public static boolean login(AirDeskDbHelper dbHelper, String email){
        db = dbHelper.getReadableDatabase();
        String query = "Select * from " + AirDeskContract.Users.TABLE_NAME +
                " where " + AirDeskContract.Users.COLUMN_NAME_EMAIL + " = '" + email + "'";

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

        String[] projection = {AirDeskContract.Users.COLUMN_NAME_EMAIL,
                AirDeskContract.Users.COLUMN_NAME_NICK};

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
        }

        c.close();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(nick)){
            return null;
        } else return new User(nick,email);
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

    public static boolean register(AirDeskDbHelper dbHelper, String nick, String email){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AirDeskContract.Users.COLUMN_NAME_NICK, nick);
        values.put(AirDeskContract.Users.COLUMN_NAME_EMAIL, email);
        values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 1);

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
            long row = db.insert(AirDeskContract.Workspaces.TABLE_NAME, null,values);
            //second insert
            boolean status = addUsersToWorkspace(dbHelper, viewers, name);
            //check status and decide on commit or rollback
            if(status && (row != -1))
                //this means -> commit transaction
                db.setTransactionSuccessful();
        //close transaction
        db.endTransaction();

        return (row != -1 && status);
    }

    public static boolean addUserToWorkspace(AirDeskDbHelper dbHelper, String viewer, String wsname){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean smoothInsert = true;

        values.put(AirDeskContract.Viewers.COLUMN_NAME_EMAIL, viewer);
        values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, wsname);
        long row = db.insert(AirDeskContract.Viewers.TABLE_NAME, null,values);
        if(row == -1)
            smoothInsert = false;

        return smoothInsert;
    }


    public static boolean addUsersToWorkspace(AirDeskDbHelper dbHelper, List<String> viewers, String wsname){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean smoothInsert = true;

        for (String v : viewers) {
            values.put(AirDeskContract.Viewers.COLUMN_NAME_EMAIL, v);
            values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, wsname);
            long row = db.insert(AirDeskContract.Viewers.TABLE_NAME, null,values);
            if(row == -1)
                smoothInsert = false;
        }

        return smoothInsert;
    }

    public static void deleteViewer(AirDeskDbHelper dbHelper, String v_email, String ws_name) {
        db = dbHelper.getWritableDatabase();

        db.delete(AirDeskContract.Viewers.TABLE_NAME, AirDeskContract.Viewers.COLUMN_NAME_EMAIL + "= \'" + v_email + "\' AND " +
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + "= \'" + ws_name +"\'", null);

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

        String[] selectionArgs = { getLoggedUser(dbHelper) };

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

            wsList.add(new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers));
        }
        c.close();
        return wsList;
    }

    public static List<Workspace> getForeignWorkspaces(AirDeskDbHelper dbHelper) {
        db = dbHelper.getReadableDatabase();
        List<String> workspaces = new ArrayList<>();
        List<String> viewers;
        List<Workspace> wsList= new ArrayList<>();


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE};

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
            workspaces.add(c.getString(0));
        }
        c.close();

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER,
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

        for(String ws : workspaces) {
            String[] selectionArgs = {ws};

            String ws_sortOrder =
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME + " DESC";

            Cursor c2 = db.query(
                    AirDeskContract.Workspaces.TABLE_NAME,  // The table to query
                    ws_projection,                               // The columns to return
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME + " LIKE ?",  // The columns for the WHERE clause
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

                wsList.add(new Workspace(c2.getString(0), c2.getInt(2), c2.getInt(3), c2.getString(4), viewers));
            }
            c2.close();
        }
        return wsList;
    }

    public static Workspace getWorkspace(AirDeskDbHelper dbHelper, String ws_name){
        db = dbHelper.getReadableDatabase();
        List<String> viewers = new ArrayList<>();
        Workspace ws;

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_OWNER,
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA,
                AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

        String[] selectionArgs = { ws_name };

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                ws_projection,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " LIKE ?",
                selectionArgs,
                null,
                null,
                null
        );


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_EMAIL + " ASC";


        c.moveToFirst();

        Cursor c2 = db.query(
                AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                v_projection,
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " LIKE ?",
                selectionArgs,
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                v_sortOrder                                 // The sort order
        );

        while (c2.moveToNext()) {
            viewers.add(c2.getString(0));
        }
        
        ws = new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers);

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
    public static boolean updateWorkspaceQuota(AirDeskDbHelper dbHelper, String workspace, long bytes) {
        double currentQuota = getCurrentQuota(dbHelper, workspace);

        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, currentQuota + bytes);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ?",
                new String[]{workspace}
        );

        return true;
    }

    public static boolean setWorkspaceQuota(AirDeskDbHelper dbHelper, String workspace, long bytes) {
        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, bytes);

        db.update(
                AirDeskContract.Workspaces.TABLE_NAME,
                values,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ?",
                new String[]{workspace}
        );

        return true;
    }

    // Returns the current quota value in bytes for workspace
    public static long getCurrentQuota(AirDeskDbHelper dbHelper, String workspace) {
        db = dbHelper.getReadableDatabase();

        long workspaceQuota = 0;
        String[] projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_QUOTA
        };

        Cursor c = db.query(
                AirDeskContract.Workspaces.TABLE_NAME,
                projection,
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " = ?",
                new String[]{workspace},
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




    public static void subscribeWorkspaces(AirDeskDbHelper dbHelper, List<String> ws_keywords){
        db = dbHelper.getReadableDatabase();
        String loggedInUser = getLoggedUser(dbHelper);
        List<String> ws_tags = new ArrayList<>();
        boolean contains;

        String[] ws_projection = {
                AirDeskContract.Workspaces.COLUMN_NAME_NAME,
                AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS};

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
                addUserToWorkspace(dbHelper, loggedInUser, c.getString(0));
        }

        c.close();
    }

    public static void clearSubscribedWorkspaces(AirDeskDbHelper dbHelper) {
        db = dbHelper.getWritableDatabase();

        String loggedUser = getLoggedUser(dbHelper);

        db.delete(AirDeskContract.Viewers.TABLE_NAME, AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE +" in (SELECT "+
                AirDeskContract.Workspaces.COLUMN_NAME_NAME + " FROM " + AirDeskContract.Workspaces.TABLE_NAME +
                " WHERE "+ AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC+ "= 1)" + "AND "+ AirDeskContract.Viewers.COLUMN_NAME_EMAIL + "= \'" + loggedUser +"\'",null);
    }

}

