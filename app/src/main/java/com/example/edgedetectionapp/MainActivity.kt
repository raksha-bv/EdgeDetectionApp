package com.example.edgedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edgedetectionapp.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var glSurfaceView: CameraGLSurfaceView
    private var isEdgeDetectionEnabled = false
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize OpenCV (optional for emulator)
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed - continuing without OpenCV")
            Toast.makeText(this, "Running without OpenCV", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "OpenCV initialized successfully")
        }

        // Initialize Native Library (optional)
        try {
            val testString = NativeLib.stringFromJNI()
            Log.d(TAG, testString)
            NativeLib.initProcessor()
        } catch (e: Exception) {
            Log.e(TAG, "Native library error: ${e.message} - continuing without native features")
            // Don't show error toast, just log
        }

        // Setup GLSurfaceView
        glSurfaceView = binding.glSurfaceView
        
        // Setup UI
        setupUI()

        // Request camera permissions (optional for emulator)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
            // Continue even without camera permissions for emulator testing
            Toast.makeText(this, "Camera permission needed for full functionality", Toast.LENGTH_LONG).show()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        binding.toggleButton.setOnClickListener {
            isEdgeDetectionEnabled = !isEdgeDetectionEnabled
            binding.toggleButton.text = if (isEdgeDetectionEnabled) {
                "Show Raw Feed"
            } else {
                "Show Edge Detection"
            }
            glSurfaceView.setEdgeDetectionEnabled(isEdgeDetectionEnabled)
        }
        
        binding.fpsText.text = "FPS: --"
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, CameraFrameAnalyzer(glSurfaceView) { fps ->
                            runOnUiThread {
                                binding.fpsText.text = String.format("FPS: %.1f", fps)
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalyzer
                    )
                    Log.d(TAG, "Camera started successfully")
                } catch (exc: Exception) {
                    Log.e(TAG, "Camera binding failed - app will continue without camera", exc)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Camera unavailable (emulator?)", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Camera initialization failed", exc)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Camera not available", Toast.LENGTH_LONG).show()
                }
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        NativeLib.releaseProcessor()
        NativeLib.releaseRenderer()
    }
}