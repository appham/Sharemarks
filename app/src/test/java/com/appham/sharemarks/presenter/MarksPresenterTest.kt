package com.appham.sharemarks.presenter

import android.content.Intent
import com.appham.sharemarks.model.MarksDataSource
import com.nhaarman.mockito_kotlin.*
import org.junit.Test

/**
 * @author thomas
 */
class MarksPresenterTest {

    val view: MarksContract.View = mock()
    val dataSource: MarksDataSource = mock()
    val presenter: MarksPresenter = spy(MarksPresenter(view, dataSource, mutableListOf()))

    @Test
    fun `handleSendText with shared url in text is called correctly`() {
        presenter.handleSharedData(Intent.ACTION_SEND, "text/plain", "http://appham.com", "x")
        verify(presenter).handleSendText("http://appham.com", "x")
    }

    @Test
    fun `handleSendText with no shared url is called correctly`() {
        presenter.handleSharedData(Intent.ACTION_SEND, "text/plain", "no url here..", "x")
        verify(presenter).handleSendText("no url here..", "x")
    }

    @Test
    fun `handleSendText with empty text and null referrer is called correctly`() {
        presenter.handleSharedData(Intent.ACTION_SEND, "text/plain", "", null)
        verify(presenter).handleSendText("", null)
    }

    @Test
    fun `handleSendText with for image type action should not be called`() {
        presenter.handleSharedData(Intent.ACTION_SEND, "image/", "", "")
        verify(presenter, never()).handleSendText(anyOrNull(), anyOrNull())
    }

}