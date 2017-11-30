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
import java.util.*

/**
 * @author thomas
 */
class HtmlManager {

    private val TAG = this::class.simpleName

    companion object {
        val acceptLang = "Accept-Language"
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
                    .followRedirects(true)
                    .userAgent(userAgentStr)
                    .header(acceptLang, Locale.getDefault().language)
                    .get()

            //set domain to the redirected one
            item.domain = URL(doc.location()).host.replace(Regex("^www."), "")

            val title = item.title
            if (StringUtil.isBlank(title) ||
                    (title != null && title.matches(Regex(Patterns.WEB_URL.pattern())))) {
                item.title = parseTitle(doc)
            }
            if (StringUtil.isBlank(item.content)) item.content = parseContent(doc)
            if (StringUtil.isBlank(item.imageUrl) || "null".equals(item.imageUrl)) {
                item.imageUrl = parseImgUrl(doc, userAgentStr).toString()
            }

            subscriber.onNext(item)
        }
    }

    private fun parseTitle(doc: Document): String? =
            doc.select("title,h1,h2,h3,h4,h5,h6")?.first()?.text()?.trim()

    private fun parseContent(doc: Document): String? {

        var metaDescElement = doc.select("meta[name='description']")?.attr("content")?.trim()
        if (metaDescElement?.isNotBlank() == true) return metaDescElement

        metaDescElement = doc.select("meta[property='og:description']")?.attr("content")?.trim()
        if (metaDescElement?.isNotBlank() == true) return metaDescElement

        return doc.select("title,h1,h2,h3,h4,h5,h6")?.text()?.trim()
    }

    /**
     * _Desperately_ tries to find a proper image url in the given html doc
     */
    private fun parseImgUrl(doc: Document, uaStr: String): URL? {

        val pageUrl = URL(doc.location())

        Log.d(TAG, "parsing url: " + pageUrl)
        Log.d(TAG, " --> with ua string: " + uaStr)

        // Look for the twitter:image declaration
        var imageUrl: String? = doc.select("meta[name='twitter:image']")?.attr("content")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for the og:image property declaration
        imageUrl = doc.select("meta[property='og:image']")?.attr("content")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for the og:image name declaration
        imageUrl = doc.select("meta[name='og:image']")?.attr("content")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for meta itemprop image declaration
        imageUrl = doc.select("meta[itemprop='image']")?.attr("content")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for itemprop image declaration
        imageUrl = doc.select("img[itemprop='image']")?.attr("src")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for amp-img id="feat-img"
        imageUrl = doc.select("amp-img[id='feat-img']")?.attr("src")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Try again with Tablet ua string, if mobile site doesn't have a proper image
        if (uaStr.contains("Mobile")) {
            val tabUaStr = userAgentStr.replace("Mobile", "Tablet")
            val newDoc = Jsoup.connect(pageUrl.toString())
                    .followRedirects(true)
                    .userAgent(tabUaStr)
                    .header(acceptLang, Locale.getDefault().language)
                    .get()
            return parseImgUrl(newDoc, tabUaStr)
        } else if (uaStr.contains("Tablet")) { // Try with desktop ua string with canonical url
            val canonicalStr = doc.select("link[rel='canonical']")?.attr("href")
            if (canonicalStr?.isNotBlank() == true) {
                return try {
                    val newDoc = Jsoup.connect(canonicalStr)
                            .followRedirects(true)
                            .userAgent(deskUaStr)
                            .header(acceptLang, Locale.getDefault().language)
                            .get()
                    parseImgUrl(newDoc, deskUaStr)
                } catch (e: Exception) {
                    e.printStackTrace()
                    parseImgUrl(doc, deskUaStr)
                }
            }
        }

        // Look for any amp-img
        imageUrl = doc.select("amp-img")?.attr("src")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for any img
        imageUrl = doc.select("img")?.attr("src")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for Apple touch icon declarations
        imageUrl = doc.select("link[rel='apple-touch-icon']")?.attr("href")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for image icon link declaration
        imageUrl = doc.select("link[rel='icon']")?.attr("href")?.trim()
        if (imageUrl?.isNotEmpty() == true) return getUrl(pageUrl, imageUrl)

        // Look for shortcut icon link declaration
        imageUrl = doc.select("link[rel='shortcut icon']")?.attr("href")?.trim()
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