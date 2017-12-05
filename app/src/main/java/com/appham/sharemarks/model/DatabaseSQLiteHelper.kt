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

        // add default items: Shameless plug :-P
        cupboard(db).put(MarkItem.create(title = "Get more apps from Thomas Fuchs-Martin",
                content = "There are some more apps to check out in my google play portfolio. " +
                        "Check this out or share it with your friends ;-)",
                referrer = context.packageName,
                url = URL("https://play.google.com/store/apps/developer?id=Thomas+Fuchs-Martin"),
                imageUrl = URL("https://lh3.googleusercontent.com/Pj2yvhL2_XF3pki_q4aY-BdPhVvKG9cVbZIHC7oU5rf_nrg6yLv_QVHP3TKN2qSGfg=w300-rw")))

        cupboard(db).put(MarkItem.create(title = "GitHub - appham/Sharemarks:  Visit the open source project on github.com!",
                content = "Sharemarks - A simple and minimalist bookmark manager app for Android. " +
                        "Bookmarks can be added via the Android share intents. Developed with Kotlin.",
                referrer = context.packageName,
                url = URL("https://github.com/appham/Sharemarks"),
                imageUrl = URL("https://github.com/fluidicon.png")))

        // add default items: The "tutorial"
        cupboard(db).put(MarkItem.create(title = "How to use Sharemarks: ",
                content = "1. In an external app that has an Android share feature " +
                        "(like for example Chrome Browser, Google Play Store etc.) click the share button. \n" +
                        "2. In the chooser select \"Sharemarks\" to share the bookmark with this app. \n" +
                        "3. Shared bookmark appears in the list. On click the shared bookmark link will be opened. \n" +
                        "4. Swipe left to delete the bookmark. Swipe right to share it.",
                referrer = context.packageName,
                url = URL("https://github.com/appham/Sharemarks/blob/master/README.md"),
                imageUrl = null))

        cupboard(db).put(MarkItem.create(title = "How to delete a Sharemark: ",
                content = "Swipe the item to the left side to delete it",
                referrer = context.packageName,
                url = URL("https://github.com/appham/Sharemarks/blob/master/README.md"),
                imageUrl = URL("https://publicdomainvectors.org/tn_img/arrow-left.png")))

        cupboard(db).put(MarkItem.create(title = "How to share a Sharemark: ",
                content = "Long click or swipe right to share the item with your friends.",
                referrer = context.packageName,
                url = URL("https://github.com/appham/Sharemarks/blob/master/README.md"),
                imageUrl = URL("https://publicdomainvectors.org/tn_img/long-arrow-right.png")))

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        cupboard(db).upgradeTables()
    }
}

