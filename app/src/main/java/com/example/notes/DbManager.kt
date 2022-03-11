package com.example.notes

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.widget.Toast

class DbManager(context: Context) {
    //database name
    var dbName = "MyNotes"
    //table name
    var dbTable = "Notes"
    //columns
    private var colID = "ID"
    private var colTitle = "Title"
    private var colDes = "Description"
    private var colPin = "Pin"
    //database version
    var dbVersion = 1
    //CREATE TABLE IF NOT EXISTS MyNotes (ID INTEGER PRIMARY KEY,title TEXT, Description TEXT)
    val sqlCreateTable = "CREATE TABLE IF NOT EXISTS " +
            "$dbTable ($colID INTEGER PRIMARY KEY, $colTitle TEXT, $colDes TEXT, $colPin INTEGER);"
    private var sqlDB: SQLiteDatabase? = null

    init {
        val db = DatabaseHelperNotes(context)
        sqlDB = db.writableDatabase
    }
    inner class DatabaseHelperNotes(context: Context) :
        SQLiteOpenHelper(context, dbName, null, dbVersion) {
        private var context: Context? = context
        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(sqlCreateTable)
            Toast.makeText(this.context, "database created...",
                Toast.LENGTH_SHORT).show()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion:
        Int) {
            db!!.execSQL("Drop table if Exists $dbTable")
        }
    }

    fun insert(values: ContentValues): Long {
        return sqlDB!!.insert(dbTable, "", values)
    }
    fun query(
        projection: Array<String>, selection: String, selectionArgs:
        Array<String>, sorOrder: String
    ): Cursor {
        val qb = SQLiteQueryBuilder()
        qb.tables = dbTable
        return qb.query(
            sqlDB, projection, selection, selectionArgs, null,
            null, sorOrder
        )
    }
    fun delete(selection: String, selectionArgs: Array<String>): Int {
        return sqlDB!!.delete(dbTable, selection, selectionArgs)
    }
    fun update(values: ContentValues, selection: String, selectionArgs:
    Array<String>): Int {
        return sqlDB!!.update(dbTable, values, selection, selectionArgs)
    }
    fun reset() {
        sqlDB!!.execSQL("Drop table if Exists $dbTable")
        return sqlDB!!.execSQL(sqlCreateTable)
    }
}