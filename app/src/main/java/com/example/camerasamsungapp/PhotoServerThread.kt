package com.example.camerasamsungapp

import android.content.Context
import android.util.Log
import kotlin.concurrent.thread

class PhotoServerThread(private val context: Context) {
    companion object {
        private const val TAG = "PhotoServerThread"
    }
    
    private var server: PhotoServer? = null
    private var serverThread: Thread? = null
    private var isRunning = false

    fun startServer(callback: (success: Boolean, port: Int, error: String?) -> Unit) {
        if (isRunning) {
            Log.w(TAG, "Server is already running")
            return
        }

        serverThread = thread(start = true) {
            try {
                server = PhotoServer(8080, context)
                server?.start()
                isRunning = true
                Log.i(TAG, "HTTP Server started on port 8080")
                callback(true, 8080, null)
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("Address already in use", ignoreCase = true) == true) {
                    "Port 8080 is already in use. Try closing other apps or restarting."
                } else {
                    "Server error: ${e.message}"
                }
                Log.e(TAG, "Failed to start server", e)
                isRunning = false
                callback(false, 0, errorMsg)
            }
        }
    }

    fun stopServer() {
        try {
            if (server != null) {
                server?.stop()
                server = null
                isRunning = false
                Log.i(TAG, "HTTP Server stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }

    fun saveImage(data: ByteArray): Boolean {
        return if (isRunning && server != null) {
            server!!.saveImage(data)
        } else {
            Log.w(TAG, "Server is not running, cannot save image")
            false
        }
    }

    fun isServerRunning(): Boolean = isRunning
}
