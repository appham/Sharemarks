package com.appham.sharemarks.presenter

import com.appham.sharemarks.model.MarkItem

/**
 * @author thomas
 */

interface MarksContract {

    interface View {
        fun addMarkItem(item: MarkItem)
        fun removeMarkItem(item: MarkItem)
        fun addDrawerItems(items: List<String>)
        fun notifyDataChanged()
        fun notifyItemChanged(index: Int)
        fun showToast(resId: Int)
        fun showSnackbar(resId: Int, action: (android.view.View) -> Unit)
        fun isDeletedFilter(): Boolean
        fun updateAppTitleToFilter()
        fun showShareChooser(item: MarkItem)
    }

    interface Presenter {
        fun handleSharedData(action: String?, type: String?, sharedText: String?, referrer: String?)
        fun updateDrawerItems()
        fun updateDrawerItem(item: MarkItem)
        fun syncMarksFromDb(deleted: Int)
        fun queryMarksByDomain(domain: String, deleted: Int)
        fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Boolean
    }

    interface Model {
        fun open()
        fun close()
        fun getMarks(deleted: Int): MutableList<MarkItem>
        fun getMarksByDomain(domain: String, deleted: Int): MutableList<MarkItem>
        fun putMark(item: MarkItem): Long
        fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Int
        fun dropItem(item: MarkItem): Boolean
    }
}