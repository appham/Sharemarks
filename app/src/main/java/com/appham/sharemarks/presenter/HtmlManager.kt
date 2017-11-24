package com.appham.sharemarks.presenter

import android.content.Context
import android.util.Log
import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.utils.Utils
import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Document
import java.net.MalformedURLException
import java.net.URL

/**
 * @author thomas
 */
class HtmlManager {

    private val TAG = "HtmlManager"

    companion object {
        var userAgentStr: String = "user-agent-string"
        fun initUserAgentStr(context: Context) {
            userAgentStr = Utils.buildUserAgentStr(context)
        }
    }

    fun parseHtml(url: URL, item: MarkItem): Observable<MarkItem> {
        return Observable.create { subscriber ->

            val doc = Jsoup.connect(url.toString())
                    .followRedirects(true).userAgent(userAgentStr).get()

            if (StringUtil.isBlank(item.title)) item.title = parseTitle(doc)
            if (StringUtil.isBlank(item.content)) item.content = parseContent(doc)
            if (StringUtil.isBlank(item.imageUrl) || "null".equals(item.imageUrl)) {
                item.imageUrl = parseImgUrl(url, doc).toString()
            }

            subscriber.onNext(item)
        }
    }

    private fun parseTitle(doc: Document): String? {
        val titleElement = doc.select("title")
        return titleElement?.text()?.trim { it <= ' ' }
    }

    private fun parseContent(doc: Document): String? {
        var metaDescElement = doc.select("meta[name='description']")
        if (!metaDescElement.hasAttr("content")) {
            metaDescElement = doc.select("meta[property='og:description']")
        }
        return metaDescElement?.attr("content")?.trim { it <= ' ' }
    }

    private fun parseImgUrl(pageUrl: URL, doc: Document): URL? {

        var imageUrl: String? = null

        // Look for the og:image declaration
        imageUrl = doc.select("meta[property='og:image']")?.attr("content")
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            return getUrl(pageUrl, imageUrl)
        }

        // Look for meta itemprop image declaration
        imageUrl = doc.select("meta[itemprop='image']")?.attr("content")
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            return getUrl(pageUrl, imageUrl)
        }

        // Look for Apple touch icon declarations
        imageUrl = doc.select("link[rel='apple-touch-icon']")?.attr("href")
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            return getUrl(pageUrl, imageUrl)
        }

        // Look for image icon link declaration
        imageUrl = doc.select("link[rel='icon']")?.attr("href")
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            return getUrl(pageUrl, imageUrl)
        }

        // Look for shortcut icon link declaration
        imageUrl = doc.select("link[rel='shortcut icon']")?.attr("href")
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            return getUrl(pageUrl, imageUrl)
        }

        // return favicon url
        try {
            val faviconUrl = URL(pageUrl.protocol + "://" + pageUrl.host + "/favicon.ico")
            Log.d(TAG, "favicon url: " + faviconUrl)
            return faviconUrl
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }

        //TODO: very last fallback: parse any image url from first image in html code
    }

    private fun getUrl(pageUrl: URL, imgUrl: String): URL? {
        Log.d(TAG, "protocol: " + pageUrl.protocol + " host: " + pageUrl.host + " imgUrl: " + imgUrl)
        if (imgUrl.startsWith("//")) {
            try {
                return URL(pageUrl.protocol + ":" + imgUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

        } else if (imgUrl.startsWith("/")) {
            try {
                return URL(pageUrl.protocol + "://" + pageUrl.host + imgUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

        } else if (imgUrl.startsWith("http")) {
            try {
                return URL(imgUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

        } else {
            try {
                return URL(pageUrl.protocol + "://" + pageUrl.host + "/" + imgUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

        }
        return null
    }

}