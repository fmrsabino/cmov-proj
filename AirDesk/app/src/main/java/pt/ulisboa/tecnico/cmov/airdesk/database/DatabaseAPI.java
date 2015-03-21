package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pt.ulisboa.tecnico.cmov.airdesk.domain.Workspace;

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
        else return true;
    }

    public static boolean register(AirDeskDbHelper dbHelper, String nick, String email){

        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AirDeskContract.Users.COLUMN_NAME_NICK, nick);
        values.put(AirDeskContract.Users.COLUMN_NAME_EMAIL, email);

        long row = db.insert(AirDeskContract.Users.TABLE_NAME, null,values);
        if(row!= -1)
            return true;
        else return false;
    }

    public static boolean createWorkspace(AirDeskDbHelper dbHelper, String name, String owner, int quota, int is_public){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AirDeskContract.Workspaces.COLUMN_NAME_NAME, name);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_OWNER, owner);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_QUOTA, quota);
        values.put(AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC, is_public);

        long row = db.insert(AirDeskContract.Workspaces.TABLE_NAME, null,values);
        if(row!= -1)
            return true;
        else return false;
    }

}
