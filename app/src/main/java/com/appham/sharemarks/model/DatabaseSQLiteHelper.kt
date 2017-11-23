package com.appham.sharemarks.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jskierbi.cupboard.configureCupboard
import com.jskierbi.cupboard.cupboard
import com.jskierbi.cupboard.register

/**
 * @author thomas
 */
class DatabaseSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_NAME = "marks.db"
        private val DATABASE_VERSION = 2

        init {
            configureCupboard {
                register<MarkItem>()
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        cupboard(db).createTables()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        cupboard(db).upgradeTables()
    }
}

