package pdt.autoreg.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.app.App;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.model.CloneInfo;

public class ClonesDBHelper extends SQLiteOpenHelper {
    private static String TAG = "DatabaseHelper";
    private static ClonesDBHelper sInstance = null;
    public static final String DATABASE_NAME = "pdt.db";

    public static final String STORED_CLONES_TABLE = "stored_clones";
    public static final String COLUMN_PACKAGE_ID = "package_id";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_CLONE_INFO = "clone_info";


    private ClonesDBHelper(Context context) {
        super(context, AppDefines.PDT_DATABASE_FOLDER + DATABASE_NAME, null, 7);
    }

    public static ClonesDBHelper instance() {
        if(sInstance == null) {
            sInstance = new ClonesDBHelper(App.getContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(db != null && !db.isReadOnly()) {
            db.execSQL(
                    "create table " + STORED_CLONES_TABLE + " " +
                            "(" + COLUMN_PACKAGE_ID + " integer, " +
                            COLUMN_PACKAGE_NAME + " text, " +
                            COLUMN_CLONE_INFO + " text default null)"
            );
        } else {
            LOG.E(TAG, "Database is not valid for writing!");
        }
        LOG.D(TAG, "--- AF SQLiteDatabase created ---");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOG.D(TAG, "onUpgrade oldVersion: " + oldVersion + " -- newVersion: " + newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
//        printTable(db, STORED_CLONES_TABLE);
    }

    public String getCloneInfo(int packageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = getDataCursor(db, STORED_CLONES_TABLE, COLUMN_PACKAGE_ID, String.valueOf(packageId));
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COLUMN_CLONE_INFO));
        }
        return null;
    }

    public boolean updateCloneInfo(int packageId, String cloneInfo) {
        SQLiteDatabase database = this.getWritableDatabase();
        if(database != null && !database.isReadOnly()) {
            String[] whereArgs = new String[]{String.valueOf(packageId)};
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_PACKAGE_ID, packageId);
            contentValues.put(COLUMN_CLONE_INFO, cloneInfo);

            if (getCount(database, STORED_CLONES_TABLE, COLUMN_PACKAGE_ID, whereArgs) > 0) {
                database.update(STORED_CLONES_TABLE, contentValues, COLUMN_PACKAGE_ID + " = ? ", whereArgs);
            } else {
                database.insertWithOnConflict(STORED_CLONES_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
            }
            return true;
        } else {
            LOG.E(TAG, "Database is not valid for writing!");
            return false;
        }
    }

    private Cursor getDataCursor(SQLiteDatabase db, String table, String key, String value) {
        String query = "select * from " + table + " where " + key + "=" + "'" + value + "'";
        LOG.D(TAG, "query: " + query);
        Cursor res = db.rawQuery(query, null);
        return res;
    }

    public static int getCount(SQLiteDatabase db, String tableName, String key, String[] args) {
        Cursor c = null;
        try {
            String query = String.format("select count(*) from %s where %s = ?",tableName,key);
            c = db.rawQuery(query, args);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String printTable(SQLiteDatabase db, String tableName) {
        String tableString = String.format("Table %s:\n", tableName);
        LOG.D(TAG, "printTable: " + tableString);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                tableString = "";
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";
                LOG.D(TAG, "printTable: " + tableString);
            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
