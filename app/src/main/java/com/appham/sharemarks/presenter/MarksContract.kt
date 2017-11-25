package com.appham.sharemarks.presenter

import com.appham.sharemarks.model.MarkItem

/**
 * @author thomas
 */

interface MarksContract {

    interface View {
        fun addMarkItem(item: MarkItem)
        fun removeMarkItem(item: MarkItem)
        fun addDrawerItem(item: String)
        fun notifyDataChanged()
        fun notifyItemChanged(index: Int)
        fun showToast(resId: Int)
        fun showSnackbar(resId: Int, action: (android.view.View) -> Unit)
        fun isDeletedFilter(): Boolean
        fun updateAppTitleToFilter()
        fun showShareChooser(item: MarkItem)
    }

    interface Presenter {
        fun parseItem(): MarkItem
        fun handleSharedData(action: String?, type: String?, sharedText: String?, referrer: String?)
        fun updateDrawerItems()
        fun updateDrawerItem(item: MarkItem)
        fun syncMarksFromDb(deleted: Int)
        fun queryMarksByDomain(domain: String)
        fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Boolean
    }

    interface Model {
        fun open()
        fun close()
        fun getMarks(deleted: Int): MutableList<MarkItem>
        fun getMarksByDomain(domain: String): MutableList<MarkItem>
        fun putMark(item: MarkItem): Long
        fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Int
        fun dropItem(item: MarkItem): Boolean
    }
}