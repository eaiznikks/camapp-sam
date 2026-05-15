package com.example.camerasamsungapp

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class PhotoServer(port: Int, private val context: Context) : NanoHTTPD(port) {
    companion object {
        private const val TAG = "PhotoServer"
    }
    
    private val cacheDir: File = context.cacheDir
    private val latestImageFile: File = File(cacheDir, "latest.jpg")

    init {
        Log.d(TAG, "PhotoServer initialized on port $port")
    }

    override fun serve(session: IHTTPSession?): Response {
        return try {
            when (session?.uri) {
                "/latest.jpg" -> serveLatestImage()
                "/viewer" -> serveViewerPage()
                else -> {
                    Log.w(TAG, "Unknown URI: ${session?.uri}")
                    newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving request", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error: ${e.message}")
        }
    }

    private fun serveLatestImage(): Response {
        return if (latestImageFile.exists() && latestImageFile.length() > 0) {
            try {
                val inputStream = FileInputStream(latestImageFile)
                val response = newFixedLengthResponse(Response.Status.OK, "image/jpeg", inputStream, latestImageFile.length())
                response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                response.addHeader("Pragma", "no-cache")
                response.addHeader("Expires", "0")
                Log.d(TAG, "Serving latest.jpg (${latestImageFile.length()} bytes)")
                response
            } catch (e: Exception) {
                Log.e(TAG, "Error reading image file", e)
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading image")
            }
        } else {
            Log.w(TAG, "No image available yet")
            val placeholderResponse = generatePlaceholder()
            val response = newFixedLengthResponse(Response.Status.OK, "image/png", placeholderResponse.inputStream(), placeholderResponse.size.toLong())
            response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
            response
        }
    }

    private fun serveViewerPage(): Response {
        val htmlContent = HtmlViewerGenerator.generateViewerHtml("http://localhost:8080/latest.jpg")
        val response = newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", htmlContent)
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        Log.d(TAG, "Serving viewer page")
        return response
    }

    private fun generatePlaceholder(): ByteArray {
        // Minimal 1x1 PNG placeholder (transparent)
        return byteArrayOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(), 0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x08.toByte(), 0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x1F.toByte(), 0x15.toByte(), 0xC4.toByte(),
            0x89.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(), 0x49.toByte(), 0x44.toByte(), 0x41.toByte(),
            0x54.toByte(), 0x08.toByte(), 0x99.toByte(), 0x01.toByte(), 0x02.toByte(), 0x00.toByte(), 0xFD.toByte(), 0xFF.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x01.toByte(), 0xE5.toByte(), 0x27.toByte(), 0xDE.toByte(),
            0xFC.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x49.toByte(), 0x45.toByte(), 0x4E.toByte(),
            0x44.toByte(), 0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte()
        )
    }

    fun saveImage(data: ByteArray): Boolean {
        return try {
            latestImageFile.writeBytes(data)
            Log.d(TAG, "Image saved successfully (${data.size} bytes)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            false
        }
    }

    fun getImageFile(): File = latestImageFile
}
