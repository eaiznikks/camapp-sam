package com.example.camerasamsungapp

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class PhotoServer(
    port: Int,
    context: Context
) : NanoHTTPD(port) {

    companion object {
        private const val TAG = "PhotoServer"
        const val PORT = 8080
    }

    private val latestImageFile: File = File(context.cacheDir, "latest.jpg")

    override fun serve(session: IHTTPSession?): Response {
        return try {
            when (session?.uri) {
                null -> text(Response.Status.BAD_REQUEST, "Bad request")
                "/", "/viewer" -> viewerPage()
                "/latest.jpg" -> latestImage()
                "/status" -> status()
                else -> text(Response.Status.NOT_FOUND, "Not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${session?.uri}", e)
            text(Response.Status.INTERNAL_ERROR, "Server error: ${e.message ?: "unknown"}")
        }
    }

    private fun viewerPage(): Response {
        return newFixedLengthResponse(
            Response.Status.OK,
            "text/html; charset=utf-8",
            ViewerHtml.page()
        ).withoutCache()
    }

    private fun latestImage(): Response {
        if (!latestImageFile.exists() || latestImageFile.length() <= 0L) {
            return newFixedLengthResponse(
                Response.Status.OK,
                "image/png",
                transparentPng().inputStream(),
                transparentPng().size.toLong()
            ).withoutCache()
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "image/jpeg",
            FileInputStream(latestImageFile),
            latestImageFile.length()
        ).withoutCache()
    }

    private fun status(): Response {
        val body = """
            {
              "server": "ok",
              "hasImage": ${latestImageFile.exists() && latestImageFile.length() > 0L},
              "imageBytes": ${if (latestImageFile.exists()) latestImageFile.length() else 0}
            }
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=utf-8", body).withoutCache()
    }

    private fun text(status: Response.Status, body: String): Response {
        return newFixedLengthResponse(status, "text/plain; charset=utf-8", body).withoutCache()
    }

    fun replaceLatestImage(source: File): Boolean {
        return try {
            if (!source.exists() || source.length() <= 0L) {
                Log.w(TAG, "Captured file missing or empty: ${source.absolutePath}")
                return false
            }
            source.copyTo(latestImageFile, overwrite = true)
            Log.i(TAG, "Updated latest image: ${latestImageFile.length()} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Could not update latest image", e)
            false
        }
    }

    private fun Response.withoutCache(): Response {
        addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        addHeader("Pragma", "no-cache")
        addHeader("Expires", "0")
        addHeader("Access-Control-Allow-Origin", "*")
        return this
    }

    private fun transparentPng(): ByteArray = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4.toByte(),
        0x89.toByte(), 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
        0x54, 0x08, 0x99.toByte(), 0x63, 0x60, 0x00, 0x00,
        0x00, 0x02, 0x00, 0x01, 0xE2.toByte(), 0x21, 0xBC.toByte(), 0x33,
        0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
        0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
    )
}
