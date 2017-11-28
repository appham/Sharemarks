package com.appham.sharemarks.utils

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.appham.sharemarks.BuildConfig
import java.net.MalformedURLException
import java.net.URL

/**
 * @author thomas
 */
class Utils {

    companion object {

        val regex = Regex("(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")

        fun extractUrl(sharedText: String): URL? {
            val url = regex.findAll(sharedText, 0)
                    .filter { s -> s.value.matches(Regex("(?i)http.*")) }
            try {
                return URL(url.elementAt(0).value)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            Log.w(toString(), "no url pattern found in: " + sharedText)
            return null
        }

        fun buildDeskUaStr(appId: String): String {
            val result = StringBuilder()
            result.append("Mozilla/5.0 (Windows NT 6.2; WOW64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36")
            appendAppId(result, appId)
            return result.toString()
        }

        fun buildUserAgentStr(appId: String, screenLayout: Int): String =
                buildUserAgentStr(appId, getTabletMobileStr(screenLayout))

        /**
         * Returns an HTTP user agent of the form
         * "Mozilla/5.0 (Linux; U; Android 5.1; XT1039 Build/LPBS23.13-17.3-1) ... ".
         */
        fun buildUserAgentStr(appId: String, deviceType: String): String {
            val result = StringBuilder()
            result.append("Mozilla/5.0")
                    .append(" (Linux; U; Android ")
            val version = Build.VERSION.RELEASE // "1.0" or "3.4b5"
            result.append(if (version.isNotEmpty()) version else "1.0")
            // add the model for the release build
            if ("REL" == Build.VERSION.CODENAME) {
                val model = Build.MODEL
                if (model.isNotEmpty()) {
                    result.append("; ")
                            .append(model)
                }
            }
            val id = Build.ID // "MASTER" or "M4-rc20"
            if (id.isNotEmpty()) {
                result.append(" Build/")
                        .append(id)
            }
            result.append(")")

            // additional AppleWebKit etc. stuff
            result.append(" AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.98 ")
                    .append(deviceType)
                    .append(" Safari/537.36")

            // add app id package name
            appendAppId(result, appId)

            return result.toString()
        }

        private fun appendAppId(result: StringBuilder, appId: String) {
            result.append(" [").append(appId).append("/")
                    .append(BuildConfig.VERSION_NAME).append("]")
        }

        /**
         *
         * @param context
         * @return "Tablet" or "Mobile" String for User Agent
         */
        fun getTabletMobileStr(screenLayout: Int): String =
                if (isTablet(screenLayout)) "Tablet" else "Mobile"

        /**
         *
         * @param context
         * @return true if device is a tablet
         */
        fun isTablet(screenLayout: Int): Boolean = screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

    }
}