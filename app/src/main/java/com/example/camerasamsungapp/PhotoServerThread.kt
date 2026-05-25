package com.example.camerasamsungapp

import android.content.Context
import android.util.Log
import java.io.File
import kotlin.concurrent.thread

class PhotoServerThread(private val context: Context) {

    companion object {
        private const val TAG = "PhotoServerThread"
    }

    private var server: PhotoServer? = null
    @Volatile private var running: Boolean = false

    fun start(callback: (success: Boolean, port: Int, error: String?) -> Unit) {
        if (running) {
            callback(true, PhotoServer.PORT, null)
            return
        }

        thread(start = true, name = "photo-server") {
            try {
                val createdServer = PhotoServer(PhotoServer.PORT, context.applicationContext)
                createdServer.start(5000, false)
                server = createdServer
                running = true
                Log.i(TAG, "HTTP server running on port ${PhotoServer.PORT}")
                callback(true, PhotoServer.PORT, null)
            } catch (e: Exception) {
                running = false
                server = null
                val message = if (e.message?.contains("Address already in use", ignoreCase = true) == true) {
                    "Port ${PhotoServer.PORT} is already in use. Restart phone or close the app using it."
                } else {
                    e.message ?: "Unknown server error"
                }
                Log.e(TAG, "HTTP server failed", e)
                callback(false, PhotoServer.PORT, message)
            }
        }
    }

    fun stop() {
        try {
            server?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error while stopping HTTP server", e)
        } finally {
            server = null
            running = false
        }
    }

    fun replaceLatestImage(source: File): Boolean {
        val activeServer = server
        if (!running || activeServer == null) {
            Log.w(TAG, "Cannot replace latest image because server is not running")
            return false
        }
        return activeServer.replaceLatestImage(source)
    }

    fun isRunning(): Boolean = running
}
