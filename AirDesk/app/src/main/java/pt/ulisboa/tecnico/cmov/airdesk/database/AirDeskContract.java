package pt.ulisboa.tecnico.cmov.airdesk.database;

import android.provider.BaseColumns;

public final class AirDeskContract {

    public AirDeskContract() {}

    public static abstract class Viewers implements BaseColumns {
        public static final String TABLE_NAME = "viewer";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_WORKSPACE = "workspace";
        public static final String COLUMN_NAME_WORKSPACE_OWNER = "workspace_owner";
    }

    public static abstract class Users implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NICK = "nick";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_SUBSCRIPTION = "subscription";
        public static final String COLUMN_NAME_LOGGED = "logged";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String COLUMN_NAME_FOLDERID = "folderID";
    }

    public static abstract class Workspaces implements BaseColumns {
        public static final String TABLE_NAME = "workspaces";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_OWNER = "owner";
        public static final String COLUMN_NAME_QUOTA = "quota";
        public static final String COLUMN_NAME_PUBLIC = "public";
        public static final String COLUMN_NAME_KEYWORDS = "keywords";
    }
}