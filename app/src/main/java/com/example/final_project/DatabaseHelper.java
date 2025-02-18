package com.example.final_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "project_management.db";
    private static final int DATABASE_VERSION = 1;

    // SQL statement to create the dev_task table
    private static final String CREATE_TABLE_DEV_TASK = "CREATE TABLE dev_task (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "DEV_NAME TEXT NOT NULL, " +
            "TASKID INTEGER, " +
            "STARTDATE TEXT, " +
            "ENDDATE TEXT, " +
            "FOREIGN KEY(TASKID) REFERENCES task(ID))"; // Adding foreign key constraint

    // SQL statement to create the task table
    private static final String CREATE_TABLE_TASK = "CREATE TABLE task (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "TASK_NAME TEXT NOT NULL, " +
            "ESTIMATE_DAY INTEGER NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASK);  // Create the task table first
        db.execSQL(CREATE_TABLE_DEV_TASK); // Create the dev_task table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop both tables if they exist
        db.execSQL("DROP TABLE IF EXISTS dev_task");
        db.execSQL("DROP TABLE IF EXISTS task");
        onCreate(db); // Recreate the tables
    }
}
