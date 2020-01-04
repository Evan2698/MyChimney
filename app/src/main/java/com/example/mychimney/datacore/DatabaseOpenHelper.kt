package com.example.mychimney.datacore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.mychimney.CustomApp
import org.jetbrains.anko.db.*


class DatabaseOpenHelper (ctx: Context = CustomApp.instance.baseContext) : ManagedSQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.dropTable(VPNTables.TABLE_NAME, true)
        onCreate(db)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.createTable(VPNTables.TABLE_NAME, true,
            VPNTables.ID to INTEGER + PRIMARY_KEY +  AUTOINCREMENT,
            VPNTables.NAME to TEXT,
            VPNTables.SERVER to TEXT,
            VPNTables.REMOTEPORT to INTEGER,
            VPNTables.PASSWORD to TEXT,
            VPNTables.DNS to TEXT
        )
    }

    companion object {
        val DB_NAME = "VPNDB"
        val DB_VERSION = 1
        val instance by lazy { DatabaseOpenHelper() }
    }
}