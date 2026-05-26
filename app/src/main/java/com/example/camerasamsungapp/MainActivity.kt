package com.example.camerasamsungapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.animation.DecelerateInterpolator
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }

    private lateinit var previewContainer: FrameLayout
    private lateinit var statusTextView: TextView
    private lateinit var serverStatusTextView: TextView
    private lateinit var ipAddressTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var previewView: PreviewView

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var serverThread: PhotoServerThread
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
        setContentView(R.layout.activity_main)

        previewContainer = findViewById(R.id.previewContainer)
        statusTextView = findViewById(R.id.statusText)
        serverStatusTextView = findViewById(R.id.serverStatusText)
        ipAddressTextView = findViewById(R.id.ipAddressText)
        captureButton = findViewById(R.id.captureButton)

        cameraExecutor = Executors.newSingleThreadExecutor()
        serverThread = PhotoServerThread(applicationContext)
        previewView = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        previewContainer.addView(
            previewView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        captureButton.isEnabled = false
        installOneUiPressFeedback(captureButton)
        captureButton.setOnClickListener { capturePhoto() }

        startPhotoServer()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            setStatusMessage(getString(R.string.camera_permission_needed), R.color.status_warning_text)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun startPhotoServer() {
        setServerMessage(getString(R.string.server_starting), R.color.ink_primary)
        serverThread.start { success, port, error ->
            runOnUiThread {
                if (success) {
                    setServerMessage(getString(R.string.server_running, port), R.color.status_success_text)
                    updateViewerUrl(port)
                } else {
                    setServerMessage(getString(R.string.server_error, error ?: "Unknown error"), R.color.status_error_text)
                    setStatusMessage(getString(R.string.server_failed), R.color.status_error_text)
                }
            }
        }
    }

    private fun updateViewerUrl(port: Int = PhotoServer.PORT) {
        val ip = IpAddressHelper.getLocalIpAddress(applicationContext)
        if (ip.isNullOrBlank()) {
            ipAddressTextView.text = getString(R.string.ip_not_connected)
            ipAddressTextView.setTextColor(color(R.color.status_warning_text))
        } else {
            ipAddressTextView.text = getString(R.string.ip_address, ip, port)
            ipAddressTextView.setTextColor(color(R.color.ink_primary))
        }
    }

    private fun startCamera() {
        setStatusMessage(getString(R.string.starting_camera), R.color.ink_primary)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

                captureButton.isEnabled = true
                setStatusMessage(getString(R.string.camera_ready), R.color.ink_primary)
            } catch (e: Exception) {
                Log.e(TAG, "Camera start failed", e)
                captureButton.isEnabled = false
                setStatusMessage(getString(R.string.error_camera_access), R.color.status_error_text)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val capture = imageCapture
        if (capture == null) {
            setStatusMessage(getString(R.string.error_camera_not_ready), R.color.status_error_text)
            return
        }

        if (!serverThread.isRunning()) {
            setStatusMessage(getString(R.string.error_server_not_ready), R.color.status_error_text)
            return
        }

        val outputFile = File(cacheDir, "capture-${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        captureButton.isEnabled = false
        setStatusMessage(getString(R.string.capturing_photo), R.color.status_warning_text)

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedForServer = serverThread.replaceLatestImage(outputFile)
                    outputFile.delete()

                    runOnUiThread {
                        captureButton.isEnabled = true
                        if (savedForServer) {
                            updateViewerUrl()
                            setStatusMessage(getString(R.string.photo_captured), R.color.status_success_text)
                        } else {
                            setStatusMessage(getString(R.string.photo_captured_but_failed), R.color.status_error_text)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exception)
                    outputFile.delete()
                    runOnUiThread {
                        captureButton.isEnabled = true
                        setStatusMessage(
                            getString(R.string.capture_error, exception.message ?: "Unknown error"),
                            R.color.status_error_text
                        )
                    }
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                captureButton.isEnabled = false
                setStatusMessage(getString(R.string.camera_permission_denied), R.color.status_error_text)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            serverThread.stop()
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed", e)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun configureSystemBars() {
        window.statusBarColor = color(R.color.oneui_screen)
        window.navigationBarColor = color(R.color.oneui_surface)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun installOneUiPressFeedback(button: Button) {
        val interpolator = DecelerateInterpolator()
        button.setOnTouchListener { view, event ->
            if (!view.isEnabled) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .setDuration(90L)
                    .setInterpolator(interpolator)
                    .start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(140L)
                    .setInterpolator(interpolator)
                    .start()
            }
            false
        }
    }

    private fun setStatusMessage(message: String, @ColorRes colorRes: Int) {
        statusTextView.text = message
        statusTextView.setTextColor(color(colorRes))
    }

    private fun setServerMessage(message: String, @ColorRes colorRes: Int) {
        serverStatusTextView.text = message
        serverStatusTextView.setTextColor(color(colorRes))
    }

    private fun color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)
}
