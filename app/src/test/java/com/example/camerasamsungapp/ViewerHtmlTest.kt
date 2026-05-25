package com.example.camerasamsungapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewerHtmlTest {

    @Test
    fun viewerUsesRelativeLatestImageUrlForTvBrowser() {
        val html = ViewerHtml.page()

        assertTrue(html.contains("/latest.jpg?t="))
        assertFalse(html.contains("localhost"))
    }

    @Test
    fun viewerRefreshesImageEverySecond() {
        val html = ViewerHtml.page()

        assertTrue(html.contains("setInterval(refreshImage, 1000)"))
    }
}
