package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.domain.*;

public class DatabaseAPI {

    private static SQLiteDatabase db;

    public static boolean login(AirDeskDbHelper dbHelper, String nick){
        db = dbHelper.getReadableDatabase();
        String query = "Select * from " + AirDeskContract.Users.TABLE_NAME +
                " where " + AirDeskContract.Users.COLUMN_NAME_NICK + " = '" + nick + "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            return false;
        }
        else{
            ContentValues values = new ContentValues();
            values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 1);

            String selection = AirDeskContract.Users.COLUMN_NAME_NICK + " = ?";
            String[] selectionArgs = { nick };

            int count = db.update(
                    AirDeskContract.Users.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            if(count != 0)
                return true;
            else return false;
        }
    }

    public static boolean signOut(AirDeskDbHelper dbHelper){
        ContentValues values = new ContentValues();
        values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 0);

        String selection = AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?";
        String[] selectionArgs = { "1" };

        int count = db.update(
                AirDeskContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if(count != 0)
            return true;
        else return false;
    }

    public static String getLoggedUser(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();

        String[] projection = {
                AirDeskContract.Users.COLUMN_NAME_NICK};

        String[] selectionArgs = { "1" };

        Cursor c = db.query(
                AirDeskContract.Users.TABLE_NAME,
                projection,
                AirDeskContract.Users.COLUMN_NAME_LOGGED + " = ?",
                selectionArgs,
                null,
                null,
                null
        );


        if(c.moveToFirst())
            return c.getString(0);
        else
        return null;
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

        return new User(nick,email);
    }

    public static boolean register(AirDeskDbHelper dbHelper, String nick, String email){

        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AirDeskContract.Users.COLUMN_NAME_NICK, nick);
        values.put(AirDeskContract.Users.COLUMN_NAME_EMAIL, email);
        values.put(AirDeskContract.Users.COLUMN_NAME_LOGGED, 1);

        long row = db.insert(AirDeskContract.Users.TABLE_NAME, null,values);
        if(row!= -1)
            return true;
        else return false;
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

        addUsersToWorkspace(dbHelper, viewers, name);


        long row = db.insert(AirDeskContract.Workspaces.TABLE_NAME, null,values);
        if(row!= -1)
            return true;
        else return false;
    }


    public static boolean addUserToWorkspace(AirDeskDbHelper dbHelper, String viewer, String wsname){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean smoothInsert = true;

            values.put(AirDeskContract.Viewers.COLUMN_NAME_NICK, viewer);
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

        for(String v : viewers){
            values.put(AirDeskContract.Viewers.COLUMN_NAME_NICK, v);
            values.put(AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE, wsname);
            long row = db.insert(AirDeskContract.Viewers.TABLE_NAME, null,values);
            if(row == -1)
                smoothInsert = false;
        }

    return smoothInsert;
    }

    public static List<Workspace> getOwnedWorkspaces(AirDeskDbHelper dbHelper){
        db = dbHelper.getReadableDatabase();
        List<String> viewers;
        List<Workspace> wsList= new ArrayList<Workspace>();

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
                AirDeskContract.Viewers.COLUMN_NAME_NICK};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_NICK + " ASC";




        if (c.moveToFirst())
            do { //get viewers from specific workspace
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

                if (c2.moveToFirst())
                    do { //add such viewers to a list
                    viewers.add(c2.getString(0));
                    } while(c2.moveToNext());

            wsList.add(new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers));
        } while(c.moveToNext());

       return wsList;
    }

    public List<Workspace> getForeignWorkspaces(AirDeskDbHelper dbHelper) {
        db = dbHelper.getReadableDatabase();
        List<String> workspaces = new ArrayList<>();
        List<String> viewers;
        List<Workspace> wsList= new ArrayList<Workspace>();


        String[] v_projection = {
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE};

        String[] v_selectionArgs = { getLoggedUser(dbHelper) };

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE + " ASC";

        Cursor c = db.query(
                AirDeskContract.Viewers.TABLE_NAME,  // The table to query
                v_projection,                               // The columns to return
                AirDeskContract.Viewers.COLUMN_NAME_NICK + " LIKE ?",  // The columns for the WHERE clause
                v_selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                v_sortOrder                                 // The sort order
        );

        if(c.moveToFirst())
            do {
                workspaces.add(c.getString(0));
            } while (c.moveToNext());




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


            if (c2.moveToFirst())
                do { //get viewers from specific workspace
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

                    if (c3.moveToFirst())
                        do { //add such viewers to a list
                            viewers.add(c3.getString(0));
                        } while (c3.moveToNext());

                    wsList.add(new Workspace(c2.getString(0), c2.getInt(2), c2.getInt(3), c2.getString(4), viewers));
                } while (c2.moveToNext());
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
                AirDeskContract.Viewers.COLUMN_NAME_NICK};

        String v_sortOrder =
                AirDeskContract.Viewers.COLUMN_NAME_NICK + " ASC";


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

        if (c2.moveToFirst())
            do { //add such viewers to a list
                 viewers.add(c2.getString(0));
            } while(c2.moveToNext());

        ws = new Workspace(c.getString(0), c.getInt(2), c.getInt(3), c.getString(4), viewers);

        return ws;
    }
}
