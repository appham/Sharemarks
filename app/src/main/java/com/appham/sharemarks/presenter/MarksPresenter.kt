package com.appham.sharemarks.presenter

import android.content.Intent
import android.util.Log
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

    override fun setMarkDeleted(item: MarkItem): Int {
        view.removeMarkItem(item)
        updateDrawerItems()
        return dataSource.setMarkDeleted(item)
    }
//endregion

    private fun handleSendText(sharedText: String?, referrer: String?) {
        if (sharedText != null) {

            // get url from text
            val url = Utils.extractUrl(sharedText) ?: return

            //TODO: what to do if there is no URL found?

            val item = MarkItem.buildMarkItem(sharedText, url, null, referrer)
            parseHtml(url, item)

//
//            putAndShowItem(item)
        }
    }

    private fun parseHtml(url: URL, item: MarkItem) {
        val subscription = htmlManager.parseHtml(url, item)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { parsedItem ->
                            Log.d("MarksPresenter", "parsed item: " + parsedItem)
                            putAndShowItem(parsedItem)
                        },
                        { e ->
                            //                            Snackbar.make(news_list, e.message ?: "", Snackbar.LENGTH_LONG).show()
                            e.printStackTrace()
                            Log.e("MarksPresenter", "e: " + e)
                            putAndShowItem(item)
                        }
                )
    }

    private fun putAndShowItem(item: MarkItem) {
        dataSource.putMark(item)
        view.addMarkItem(item)
        updateDrawerItem(item)
    }


//    private fun buildMarkItem(sharedText: String, url: URL, drawable: BitmapDrawable?, referrer: String?): MarkItem {
//
//        val split = sharedText.replaceFirst(url.toString().toRegex(), "")
//                .split("\\n+".toRegex(), 2).toTypedArray()
//
//        if (split.size > 1) {
//            return MarkItem.create(split[0], split[1], referrer, url, null)
//        } else {
//            return MarkItem.create(sharedText, sharedText, referrer, url, null)
//        }
//    }
}