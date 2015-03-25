package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AirDeskDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "AirDesk.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_VIEWERS_TABLE =
            "CREATE TABLE " + AirDeskContract.Viewers.TABLE_NAME + " (" +
                    AirDeskContract.Viewers.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AirDeskContract.Viewers.COLUMN_NAME_NICK + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE+ TEXT_TYPE + COMMA_SEP +
                    " FOREIGN KEY ("+AirDeskContract.Viewers.COLUMN_NAME_WORKSPACE+ ")" +
                    " REFERENCES "+AirDeskContract.Workspaces.TABLE_NAME + " ("+
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME +"));";

    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + AirDeskContract.Users.TABLE_NAME + " (" +
                    AirDeskContract.Users.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AirDeskContract.Users.COLUMN_NAME_NICK + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Users.COLUMN_NAME_LOGGED + INTEGER_TYPE + COMMA_SEP +
                    AirDeskContract.Users.COLUMN_NAME_EMAIL + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_WORKSPACES_TABLE =
            "CREATE TABLE " + AirDeskContract.Workspaces.TABLE_NAME + " (" +
                    AirDeskContract.Workspaces.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AirDeskContract.Workspaces.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Workspaces.COLUMN_NAME_OWNER + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Workspaces.COLUMN_NAME_QUOTA + INTEGER_TYPE + COMMA_SEP +
                    AirDeskContract.Workspaces.COLUMN_NAME_PUBLIC + INTEGER_TYPE + COMMA_SEP +
                    AirDeskContract.Workspaces.COLUMN_NAME_KEYWORDS + TEXT_TYPE + COMMA_SEP +
                    " FOREIGN KEY ("+AirDeskContract.Workspaces.COLUMN_NAME_OWNER+ ")" +
                    " REFERENCES "+AirDeskContract.Users.TABLE_NAME + " ("+
                    AirDeskContract.Users.COLUMN_NAME_NICK + "));";

    private static final String SQL_CREATE_FILES_TABLE =
            "CREATE TABLE " + AirDeskContract.Files.TABLE_NAME + " (" +
                    AirDeskContract.Files.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AirDeskContract.Files.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Files.COLUMN_NAME_PATH + TEXT_TYPE + COMMA_SEP +
                    AirDeskContract.Files.COLUMN_NAME_OWNER + INTEGER_TYPE + COMMA_SEP +
                    " FOREIGN KEY ("+AirDeskContract.Files.COLUMN_NAME_OWNER+ ")" +
                    " REFERENCES "+AirDeskContract.Workspaces.TABLE_NAME + " ("+
                    AirDeskContract.Workspaces.COLUMN_NAME_ID + "));";

    private static final String SQL_DELETE_OWNER_TABLE =
            "DROP TABLE IF EXISTS " + AirDeskContract.Users.TABLE_NAME;

    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + AirDeskContract.Users.TABLE_NAME;

    private static final String SQL_DELETE_WORKSPACES_TABLE =
            "DROP TABLE IF EXISTS " + AirDeskContract.Workspaces.TABLE_NAME;

    private static final String SQL_DELETE_FILES_TABLE =
            "DROP TABLE IF EXISTS " + AirDeskContract.Files.TABLE_NAME;

    public AirDeskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_VIEWERS_TABLE);
        db.execSQL(SQL_CREATE_WORKSPACES_TABLE);
        db.execSQL(SQL_CREATE_FILES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_OWNER_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE);
        db.execSQL(SQL_DELETE_WORKSPACES_TABLE);
        db.execSQL(SQL_DELETE_FILES_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}