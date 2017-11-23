package com.appham.sharemarks.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.appham.sharemarks.presenter.MarksContract
import com.jskierbi.cupboard.cupboard

/**
 * @author thomas
 */
class MarksDataSource(context: Context) : MarksContract.Model {
    private val dbHelper: DatabaseSQLiteHelper = DatabaseSQLiteHelper(context)

    private var db: SQLiteDatabase = dbHelper.writableDatabase

    //region MarksContract interface methods
    override fun open() {
        db = dbHelper.writableDatabase
    }

    override fun close() {
        dbHelper.close()
    }

    override fun getMarks(deleted: Int): MutableList<MarkItem> =
            cupboard(db).query(MarkItem::class.java)
                    .withSelection("deleted == ?", deleted.toString()).list().asReversed()

    override fun getMarksByDomain(domain: String): MutableList<MarkItem> =
            cupboard(db).query(MarkItem::class.java)
                    .withSelection("domain = ?", domain).list().asReversed()

    override fun putMark(item: MarkItem): Long = cupboard(db).put(item)

    override fun setMarkDeleted(item: MarkItem): Int {
        val values = ContentValues(1)
        values.put("deleted", true)
        return cupboard(db).update(MarkItem::class.java, values,
                "_id = ?", item._id.toString())
    }
    //endregion

}