package com.appham.sharemarks.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.util.Patterns
import com.appham.sharemarks.BuildConfig
import java.net.MalformedURLException
import java.net.URL

/**
 * @author thomas
 */
class Utils {

    companion object {

        fun extractUrl(sharedText: String): URL? {
            val regex = Regex(Patterns.WEB_URL.pattern())
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

        /**
         * Returns an HTTP user agent of the form
         * "Mozilla/1.1.0 (Linux; U; Android Eclair Build/MASTER) ... ".
         */
        fun buildUserAgentStr(context: Context): String {
            val result = StringBuilder(64)
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
            result.append(" AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 ")
                    .append(getTabletMobileStr(context))
                    .append(" Safari/537.36")

            // add package name
            result.append(" [").append(context.packageName).append("/")
                    .append(BuildConfig.VERSION_NAME).append("]")
            return result.toString()
        }

        /**
         *
         * @param context
         * @return "Tablet" or "Mobile" String for User Agent
         */
        fun getTabletMobileStr(context: Context): String =
                if (isTablet(context)) "Tablet" else "Mobile"


        /**
         *
         * @param context
         * @return true if device is a tablet
         */
        fun isTablet(context: Context): Boolean = context.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

    }
}