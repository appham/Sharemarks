package com.appham.sharemarks.view

import android.app.Application
import android.util.Log
import com.squareup.okhttp.OkHttpClient
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

/**
 * @author thomas
 */
class MarksApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //Setup Picasso instance with timeouts
        val httpClient = OkHttpClient()
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS)
        httpClient.setReadTimeout(10, TimeUnit.SECONDS)
        val picasso = Picasso.Builder(applicationContext)
//                .loggingEnabled(true).indicatorsEnabled(true)
                .downloader(OkHttpDownloader(httpClient))
                .listener { _, uri, e ->
                    e.printStackTrace()
                    Log.e(Picasso::class.simpleName, e.message?.plus(" at uri ").plus(uri))
                }
                .build()
        Picasso.setSingletonInstance(picasso)

    }
}