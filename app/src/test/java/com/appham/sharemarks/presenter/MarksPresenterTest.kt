package com.appham.sharemarks.presenter

import android.content.Intent
import com.appham.sharemarks.model.MarksDataSource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

/**
 * @author thomas
 */
class MarksPresenterTest {

    val view: MarksContract.View = mock()
    val dataSource: MarksDataSource = mock()
    val presenter: MarksPresenter = spy(MarksPresenter(view, dataSource, mutableListOf()))

    @Test
    fun `Verify handleSendText method is called with the shared text and referrer`() {
        presenter.handleSharedData(Intent.ACTION_SEND, "text/plain", "http://appham.com", "x")
        verify(presenter).handleSendText("http://appham.com", "x")
    }


}