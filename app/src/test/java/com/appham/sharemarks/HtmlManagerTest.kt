package com.appham.sharemarks

import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.presenter.HtmlManager
import org.junit.*
import org.junit.Assume.assumeTrue
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


val testPath = "http://localhost:8765/"

class HtmlManagerTest {

    //region test server setup etc.
    companion object {

        @BeforeClass
        @JvmStatic
        fun setup() {
            val proc = Runtime.getRuntime().exec("curl -Is " + testPath + " | head -1",
                    null, File("testHtml/server"))
            Scanner(proc.inputStream).use {
                while (it.hasNextLine()) {
                    val line = it.nextLine()
                    println(line)
                    if (line.endsWith("200 OK")) {
                        println("Server is running!")
                        return
                    }
                }
            }
            proc.waitFor(1, TimeUnit.SECONDS)
            System.out.println("Setting up server")
            runCommand("npm restart")
        }

        @AfterClass
        @JvmStatic
        fun shutdown() {
            System.out.println("Stop server")
            runCommand("npm stop")
        }

        private fun runCommand(cmd: String) {
            val proc = Runtime.getRuntime().exec(cmd, null, File("testHtml/server"))
            proc.waitFor(1, TimeUnit.SECONDS)
        }

        @get:Rule
        val htmlManager = HtmlManager()

    }

    @Before
    fun `assume a localhost is serving test pages`() {
        var stream: InputStream? = null
        var isRunning = true
        try {
            stream = URL(testPath).openConnection().content as InputStream
        } catch (e: UnknownHostException) {
            isRunning = false
        } finally {
            stream?.close()
            assumeTrue("Assuming a running host at: " + testPath, isRunning)
        }
    }

    //endregion

    @Test
    (expected = RuntimeException::class)
    fun `not found page should lead to exception`() {
        parse("notfoundpage404")
    }

    @Test
    fun `title field should be set to html title tag`() {
        assertEquals("title tag", parse("test-no-img.html").title)
    }

    @Test
    fun `title field should be set to first html h tag if there is no title tag`() {
        assertEquals("h1 content", parse("test-no-title.html").title)
    }

    @Test
    fun `content field should be set to meta description`() {
        assertEquals("meta desc", parse("test-with-img.html").content)
    }

    @Test
    fun `content field should be set to html h tags if there is no title tag`() {
        assertEquals("h1 content h2 content", parse("test-no-title.html").content)
    }

    @Test
    fun `image url field should be favicon url for pages without image`() {
        assertEquals("http://localhost/favicon.ico", parse("test-no-img.html").imageUrl)
    }

    @Test
    fun `image url field should be valid url for pages with featured image`() {
        assertEquals("http://localhost:8765/ok-img.jpg", parse("test-with-img.html").imageUrl)
    }

    private fun parse(path: String): MarkItem {
        val url = URL(testPath.plus(path))
        val observable = htmlManager.parseHtml(url, MarkItem.create(null, null, null, url, null))
        return observable.blockingFirst()
    }
}
