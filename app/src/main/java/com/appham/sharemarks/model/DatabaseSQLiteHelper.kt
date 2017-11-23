package com.appham.sharemarks.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jskierbi.cupboard.configureCupboard
import com.jskierbi.cupboard.cupboard
import com.jskierbi.cupboard.register
import java.net.URL

/**
 * @author thomas
 */
class DatabaseSQLiteHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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

        // add default item
        cupboard(db).put(MarkItem.create(title = "GitHub - appham/Sharemarks:  Visit the open source project on github.com!",
                content = "Sharemarks - A simple and minimalist bookmark manager app for Android. " +
                        "Bookmarks can be added via the Android share intents. Developed with Kotlin.",
                referrer = context.packageName,
                url = URL("https://github.com/appham/Sharemarks"),
                imageUrl = URL("https://github.com/fluidicon.png")))

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        cupboard(db).upgradeTables()
    }
}

