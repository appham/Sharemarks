package com.appham.sharemarks.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
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
//            result.append(System.getProperty("java.vm.version")) // such as 1.1.0
            result.append(" (Linux; U; Android ")
            val version = Build.VERSION.RELEASE // "1.0" or "3.4b5"
            result.append(if (version.length > 0) version else "1.0")
            // add the model for the release build
            if ("REL" == Build.VERSION.CODENAME) {
                val model = Build.MODEL
                if (model.length > 0) {
                    result.append("; ")
                    result.append(model)
                }
            }
            val id = Build.ID // "MASTER" or "M4-rc20"
            if (id.length > 0) {
                result.append(" Build/")
                result.append(id)
            }
            result.append(")")

            // additional AppleWebKit etc. stuff
            result.append(" AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 ")
            result.append(getTabletMobileStr(context))
            result.append(" Safari/537.36")

            // add package name
            result.append(" [").append(context.packageName).append("/").append(BuildConfig.VERSION_NAME).append("]")
            return result.toString()
        }

        /**
         *
         * @param context
         * @return "Tablet" or "Mobile" String for User Agent
         */
        fun getTabletMobileStr(context: Context): String {
            return if (isTablet(context)) {
                "Tablet"
            } else "Mobile"
        }


        /**
         *
         * @param context
         * @return true if device is a tablet
         */
        fun isTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

        fun getScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels

    }
}