package com.example.camerasamsungapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CAMERA_PERMISSION = 100
    }
    
    private lateinit var previewView: PreviewView
    private lateinit var statusTextView: TextView
    private lateinit var serverStatusTextView: TextView
    private lateinit var ipAddressTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var previewContainer: FrameLayout
    
    private var imageCapture: ImageCapture? = null
    private lateinit var serverThread: PhotoServerThread
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        try {
            // Initialize UI elements FIRST
            statusTextView = findViewById(R.id.statusText)
            serverStatusTextView = findViewById(R.id.serverStatusText)
            ipAddressTextView = findViewById(R.id.ipAddressText)
            captureButton = findViewById(R.id.captureButton)
            previewContainer = findViewById(R.id.previewContainer)
            
            setStatusMessage("Initializing...", R.color.ink_secondary)
            setServerMessage(getString(R.string.server_idle), R.color.ink_primary)
            
            // Initialize camera preview (without starting camera yet)
            try {
                previewView = PreviewView(this)
                previewContainer.addView(previewView, 0)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating PreviewView", e)
                setStatusMessage("Preview error: ${e.message}", R.color.status_error_text)
                return
            }
            
            // Set up capture button
            captureButton.setOnClickListener {
                capturePhoto()
            }
            
            // Check permissions and start app
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initializeAppAfterPermissions()
            } else {
                setStatusMessage("Requesting camera permission...", R.color.status_warning_text)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            try {
                if (::statusTextView.isInitialized) {
                    setStatusMessage("Initialization error: ${e.message}", R.color.status_error_text)
                }
            } catch (uiException: Exception) {
                Log.e(TAG, "Error updating UI with error message", uiException)
            }
        }
    }
    
    private fun initializeAppAfterPermissions() {
        try {
            // Now initialize server after permissions are granted
            if (!::serverThread.isInitialized) {
                serverThread = PhotoServerThread(this)
            }
            initializeApp()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing after permissions", e)
            setStatusMessage("Init error: ${e.message}", R.color.status_error_text)
        }
    }

    private fun initializeApp() {
        try {
            setStatusMessage(getString(R.string.starting_camera), R.color.ink_primary)
            
            // Start HTTP server
            startHttpServer()
            
            // Display IP address
            updateIpAddress()
            
            // Initialize camera
            initializeCamera()
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeApp", e)
            setStatusMessage("Error: ${e.message}", R.color.status_error_text)
        }
    }

    private fun startHttpServer() {
        try {
            serverThread.startServer { success, port, error ->
                runOnUiThread {
                    if (success) {
                        setServerMessage(getString(R.string.server_running, port), R.color.status_success_text)
                        setStatusMessage(getString(R.string.ready_to_capture), R.color.ink_primary)
                    } else {
                        setServerMessage(getString(R.string.server_error, error ?: "Unknown error"), R.color.status_error_text)
                        setStatusMessage(getString(R.string.server_failed), R.color.status_error_text)
                        Log.e(TAG, "Server error: $error")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting HTTP server", e)
            setServerMessage("Server Error: ${e.message}", R.color.status_error_text)
        }
    }

    private fun updateIpAddress() {
        try {
            val ip = IpAddressHelper.getLocalIpAddress(this)
            if (ip != null) {
                ipAddressTextView.text = getString(R.string.ip_address, ip)
                ipAddressTextView.setTextColor(getColorCompat(R.color.ink_primary))
                Log.d(TAG, "Phone IP: $ip")
            } else {
                ipAddressTextView.text = getString(R.string.ip_not_connected)
                ipAddressTextView.setTextColor(getColorCompat(R.color.status_warning_text))
                Log.w(TAG, "Could not determine IP address")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating IP address", e)
            ipAddressTextView.text = getString(R.string.ip_not_connected)
            ipAddressTextView.setTextColor(getColorCompat(R.color.status_error_text))
        }
    }

    private fun initializeCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization error", e)
                setStatusMessage(getString(R.string.error_camera_access), R.color.status_error_text)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        try {
            val cameraProvider = cameraProvider ?: return
            
            // Preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            // Image capture use case - get rotation from display
            @Suppress("DEPRECATION")
            val rotation = windowManager.defaultDisplay.rotation
            
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(rotation)
                .build()
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Unbind all and bind camera
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
            
            setStatusMessage(getString(R.string.camera_ready), R.color.ink_primary)
            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Bind camera error", e)
            setStatusMessage(getString(R.string.error_bind, e.message ?: "Unknown error"), R.color.status_error_text)
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: run {
            setStatusMessage(getString(R.string.error_camera_not_ready), R.color.status_error_text)
            return
        }
        
        captureButton.isEnabled = false
        setStatusMessage(getString(R.string.capturing_photo), R.color.status_warning_text)
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "capture_${System.currentTimeMillis()}.jpg")
            }
        ).build()
        
        imageCapture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        // Read the captured image
                        val savedUri = output.savedUri ?: return
                        val imageBytes = contentResolver.openInputStream(savedUri)?.readBytes() ?: return
                        
                        // Save to server cache
                        val saved = serverThread.saveImage(imageBytes)
                        
                        runOnUiThread {
                            if (saved) {
                                setStatusMessage(getString(R.string.photo_captured), R.color.status_success_text)
                                Log.d(TAG, "Photo saved to server (${imageBytes.size} bytes)")
                            } else {
                                setStatusMessage(getString(R.string.photo_captured_but_failed), R.color.status_error_text)
                            }
                            captureButton.isEnabled = true
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing captured image", e)
                        runOnUiThread {
                            setStatusMessage(getString(R.string.error_process_image), R.color.status_error_text)
                            captureButton.isEnabled = true
                        }
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture error", exception)
                    runOnUiThread {
                        setStatusMessage(getString(R.string.capture_error, exception.message ?: "Unknown error"), R.color.status_error_text)
                        captureButton.isEnabled = true
                    }
                }
            }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeAppAfterPermissions()
                } else {
                    setStatusMessage(getString(R.string.camera_permission_denied), R.color.status_error_text)
                    Log.w(TAG, "Camera permission denied")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onRequestPermissionsResult", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::serverThread.isInitialized) {
                serverThread.stopServer()
                Log.d(TAG, "Activity destroyed, server stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server on destroy", e)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Activity paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed")
    }

    private fun setStatusMessage(message: String, @ColorRes colorRes: Int) {
        statusTextView.text = message
        statusTextView.setTextColor(getColorCompat(colorRes))
    }

    private fun setServerMessage(message: String, @ColorRes colorRes: Int) {
        serverStatusTextView.text = message
        serverStatusTextView.setTextColor(getColorCompat(colorRes))
    }

    private fun getColorCompat(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(this, colorRes)
    }
}
