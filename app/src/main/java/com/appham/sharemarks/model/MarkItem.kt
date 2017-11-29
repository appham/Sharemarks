package com.appham.sharemarks.model

import java.net.URL

/**
 * @author thomas
 */
data class MarkItem(var title: String?,
                    var content: String?,
                    var referrer: String?,
                    var url: String?,
                    var imageUrl: String?,
                    var domain: String,
                    var deleted: Boolean = false) {

    var _id: Long? = null

    companion object Factory {

        fun create(title: String?, content: String?, referrer: String?, url: URL, imageUrl: URL?): MarkItem =
                MarkItem(title, content, referrer, url.toString(), imageUrl.toString(),
                        url.host.replace(Regex("^www."), ""))

        fun buildMarkItem(sharedText: String, url: URL, imageUrl: URL?, referrer: String?): MarkItem {

            val split = sharedText.replaceFirst(url.toString().toRegex(), "")
                    .split("\\n+".toRegex(), 2).toTypedArray()

            if (split.size > 1) {
                return MarkItem.create(split[0], split[1], referrer, url, imageUrl)
            } else {
                return MarkItem.create(split[0], null, referrer, url, imageUrl)
            }
        }
    }

}
