package com.appham.sharemarks.presenter

import android.content.Intent
import android.util.Log
import android.view.View
import com.appham.sharemarks.R
import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.model.MarksDataSource
import com.appham.sharemarks.utils.Utils
import io.reactivex.schedulers.Schedulers
import java.net.URL

/**
 * @author thomas
 */
class MarksPresenter(private val view: MarksContract.View,
                     private val dataSource: MarksDataSource,
                     private val marks: MutableList<MarkItem>) : MarksContract.Presenter {

    private val TAG = this::class.simpleName

    private val drawerItems = mutableListOf<String>()

    private val htmlManager by lazy { HtmlManager() }

    init {
        syncMarksFromDb(0)
    }

    //region MarksContract methods
    override fun parseItem(): MarkItem {
        //dummy test item for now:
        return MarkItem.create("Another Test title", "that is some really awesome content!",
                "http://appham.com", URL("http://google.com"), URL("http://img"))
    }

    override fun handleSharedData(action: String?, type: String?, sharedText: String?, referrer: String?) {
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(sharedText, referrer) // Handle text being sent
            } else if (type.startsWith("image/")) {
                //                    handleSendImage(sharedText, wDb) // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            if (type.startsWith("image/")) {
                //                    handleSendMultipleImages(sharedText) // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    override fun syncMarksFromDb(deleted: Int) {
        marks.clear()
        marks.addAll(dataSource.getMarks(deleted))
        updateDrawerItems()
        view.notifyDataChanged()
    }

    override fun updateDrawerItems() {
        for (item: MarkItem in marks) {
            updateDrawerItem(item)
        }
    }

    override fun updateDrawerItem(item: MarkItem) {
        if (!drawerItems.contains(item.domain)) {
            drawerItems.add(item.domain)
            view.addDrawerItem(item.domain)
        }
    }

    override fun queryMarksByDomain(domain: String) {
        marks.clear()
        marks.addAll(dataSource.getMarksByDomain(domain))
        updateDrawerItems()
        view.notifyDataChanged()
    }

    override fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Boolean {

        val undoAction = { _: View ->
            setMarkDeleted(item, !toDelete)
            Unit
        }

        if (item.deleted && toDelete) { //permanently drop item
            removeItemView(item)
            view.showSnackbar(R.string.mark_dropped, undoAction)
            item.deleted = false
            return dataSource.dropItem(item)
        } else if (!item.deleted && !toDelete && view.isDeletedFilter()) { //restore dropped item
            view.showSnackbar(R.string.mark_moved_to_deleted, undoAction)
            item.deleted = true
            return putAndShowItem(item) > 0
        }

        val isItemModified = dataSource.setMarkDeleted(item, toDelete) > 0

        if (isItemModified && toDelete) { //set item to deleted
            if (view.isDeletedFilter()) putAndShowItem(item) else removeItemView(item)
            view.showSnackbar(R.string.mark_moved_to_deleted, undoAction)
        } else if (isItemModified && !toDelete) { //set item to not deleted
            if (view.isDeletedFilter()) removeItemView(item) else putAndShowItem(item)
            view.showSnackbar(R.string.mark_restored, undoAction)
        }

        item.deleted = toDelete

        return isItemModified
    }
    //endregion

    private fun handleSendText(sharedText: String?, referrer: String?) {
        if (sharedText != null) {

            // get url from text
            val url = Utils.extractUrl(sharedText) ?: return

            //TODO: what to do if there is no URL found?

            val item = MarkItem.buildMarkItem(
                    sharedText.replace(url.toString(), "", true),
                    url,
                    null,
                    referrer)
            parseHtml(url, item)
        }
    }

    private fun parseHtml(url: URL, item: MarkItem) {
        htmlManager.parseHtml(url, item)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { parsedItem ->
                            Log.d(TAG, "parsed item: " + parsedItem)
                            putAndShowItem(parsedItem)
                        },
                        { e ->
                            e.printStackTrace()
                            Log.e(TAG, "e: " + e)
                            putAndShowItem(item)
                        }
                )
    }

    private fun putAndShowItem(item: MarkItem): Long {
        view.addMarkItem(item)
        updateDrawerItem(item)
        return dataSource.putMark(item)
    }


    private fun removeItemView(item: MarkItem) {
        view.removeMarkItem(item)
        updateDrawerItems()
    }

}