package com.appham.sharemarks.presenter

import android.util.Log
import android.util.Patterns
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

    private val TAG = this::class.simpleName

    companion object {
        var userAgentStr: String = ""
        var deskUaStr: String = ""
        fun initUserAgentStr(appId: String, screenLayout: Int) {
            userAgentStr = Utils.buildUserAgentStr(appId, screenLayout)
            deskUaStr = Utils.buildDeskUaStr(appId)
        }
    }

    fun parseHtml(url: URL, item: MarkItem): Observable<MarkItem> {
        return Observable.create { subscriber ->

            val doc = Jsoup.connect(url.toString())
                    .followRedirects(true).userAgent(userAgentStr).get()

            val title = item.title
            if (StringUtil.isBlank(title) ||
                    (title != null && title.matches(Regex(Patterns.WEB_URL.pattern())))) {
                item.title = parseTitle(doc)
            }
            if (StringUtil.isBlank(item.content)) item.content = parseContent(doc)
            if (StringUtil.isBlank(item.imageUrl) || "null".equals(item.imageUrl)) {
                item.imageUrl = parseImgUrl(url, doc, userAgentStr).toString()
            }

            subscriber.onNext(item)
        }
    }

    private fun parseTitle(doc: Document): String? {
        val title = doc.select("title")?.text()?.trim()
        if (!title.isNullOrBlank()) return title

        return doc.select("h1,h2,h3,h4,h5,h6")?.first()?.text()?.trim()
    }

    private fun parseContent(doc: Document): String? {

        var metaDescElement = doc.select("meta[name='description']")?.attr("content")
        if (metaDescElement?.isNotBlank() == true) return metaDescElement

        metaDescElement = doc.select("meta[property='og:description']")?.attr("content")
        if (metaDescElement?.isNotBlank() == true) return metaDescElement

        return doc.select("h1,h2,h3,h4,h5,h6,title")?.text()?.trim()
    }

    /**
     * _Desperately_ tries to find a proper image url in the given html doc
     */
    private fun parseImgUrl(pageUrl: URL, doc: Document, uaStr: String): URL? {

        Log.d(TAG, "parsing url: " + pageUrl)
        Log.d(TAG, " --> with ua string: " + uaStr)

        // Look for the twitter:image declaration
        var imageUrl: String? = doc.select("meta[name='twitter:image']")?.attr("content")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for the og:image property declaration
        imageUrl = doc.select("meta[property='og:image']")?.attr("content")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for the og:image name declaration
        imageUrl = doc.select("meta[name='og:image']")?.attr("content")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for meta itemprop image declaration
        imageUrl = doc.select("meta[itemprop='image']")?.attr("content")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for amp-img id="feat-img"
        imageUrl = doc.select("amp-img[id='feat-img']")?.attr("src")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Try again with Tablet ua string, if mobile site doesn't have a proper image
        if (uaStr.contains("Mobile")) {
            val tabUaStr = userAgentStr.replace("Mobile", "Tablet")
            val newDoc = Jsoup.connect(pageUrl.toString())
                    .followRedirects(true).userAgent(tabUaStr).get()
            return parseImgUrl(pageUrl, newDoc, tabUaStr)
        } else if (uaStr.contains("Tablet")) { // Try with desktop ua string with canonical url
            val canonicalStr = doc.select("link[rel='canonical']")?.attr("href")
            if (canonicalStr?.isNotBlank() == true) {
                return try {
                    val canonical = URL(canonicalStr)
                    val newDoc = Jsoup.connect(canonical.toString())
                            .followRedirects(true).userAgent(deskUaStr).get()
                    parseImgUrl(canonical, newDoc, deskUaStr)
                } catch (e: Exception) {
                    e.printStackTrace()
                    parseImgUrl(pageUrl, doc, deskUaStr)
                }
            }
        }

        // Look for Apple touch icon declarations
        imageUrl = doc.select("link[rel='apple-touch-icon']")?.attr("href")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for image icon link declaration
        imageUrl = doc.select("link[rel='icon']")?.attr("href")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for shortcut icon link declaration
        imageUrl = doc.select("link[rel='shortcut icon']")?.attr("href")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for any amp-img
        imageUrl = doc.select("amp-img")?.attr("src")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for any img
        imageUrl = doc.select("img")?.attr("src")
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // return favicon url
        try {
            val faviconUrl = URL(pageUrl.protocol + "://" + pageUrl.host + "/favicon.ico")
            Log.d(TAG, "favicon url: " + faviconUrl)
            return faviconUrl
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }
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