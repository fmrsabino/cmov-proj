package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAPI {

    private static SQLiteDatabase db;

    public static boolean login(AirDeskDbHelper dbHelper, String nick){
        db = dbHelper.getReadableDatabase();
        String query = "Select * from " + AirDeskContract.Users.TABLE_NAME +
                " where " + AirDeskContract.Users.COLUMN_NAME_NICK + " = " + nick;
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
}
