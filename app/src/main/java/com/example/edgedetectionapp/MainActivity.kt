package com.example.edgedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.View
import android.opengl.GLSurfaceView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EdgeDetectionApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    
    private lateinit var statusText: TextView
    private lateinit var startCameraButton: ImageButton
    private lateinit var edgeDetectionSwitch: Switch
    private lateinit var cameraPreview: PreviewView
    private lateinit var processedImageView: ImageView
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var effectSpinner: Spinner
    private lateinit var glRenderer: GLRenderer
    private lateinit var cameraExecutor: ExecutorService
    
    private var isCameraStarted = false
    private var isEdgeDetectionEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_camera)

        Log.d(TAG, "MainActivity created")

        // Initialize UI components
        statusText = findViewById(R.id.statusText)
        startCameraButton = findViewById<ImageButton>(R.id.startCameraButton)
        edgeDetectionSwitch = findViewById(R.id.edgeDetectionSwitch)
        cameraPreview = findViewById(R.id.cameraPreview)
        processedImageView = findViewById(R.id.processedImageView)
        glSurfaceView = findViewById(R.id.glSurfaceView)
        effectSpinner = findViewById(R.id.effectSpinner)
        
        // Initialize OpenGL renderer
        glRenderer = GLRenderer()
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        
        // Setup effect spinner
        val effects = arrayOf("Normal", "Grayscale", "Invert", "Sepia", "Blur")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, effects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        effectSpinner.adapter = adapter
        
        effectSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val effect = when (position) {
                    0 -> GLRenderer.EffectType.NORMAL
                    1 -> GLRenderer.EffectType.GRAYSCALE
                    2 -> GLRenderer.EffectType.INVERT
                    3 -> GLRenderer.EffectType.SEPIA
                    4 -> GLRenderer.EffectType.BLUR
                    else -> GLRenderer.EffectType.NORMAL
                }
                glRenderer.setEffect(effect)
                glSurfaceView.requestRender()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Camera button click
        startCameraButton.setOnClickListener {
            if (isCameraStarted) {
                stopCamera()
            } else {
                if (allPermissionsGranted()) {
                    startCamera()
                } else {
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                }
            }
        }
        
        // Edge detection switch
        edgeDetectionSwitch.setOnCheckedChangeListener { _, isChecked ->
            isEdgeDetectionEnabled = isChecked
            // When switching modes, hide both views initially to avoid overlap
            if (isChecked) {
                // Edge detection mode - will show processedImageView when frame is processed
                glSurfaceView.visibility = View.GONE
                processedImageView.visibility = View.GONE
                Log.d(TAG, "Edge detection enabled - waiting for processed frame")
            } else {
                // Effects mode - will show glSurfaceView when frame is processed  
                processedImageView.visibility = View.GONE
                glSurfaceView.visibility = View.GONE
                Log.d(TAG, "OpenGL effects enabled - waiting for camera frame")
            }
            Log.d(TAG, "Mode switched to ${if (isChecked) "edge detection" else "OpenGL effects"}")
        }

        // Initial status
        updateStatus()
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }
            
            // Add image analysis for edge detection
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { 
                    it.setAnalyzer(cameraExecutor, EdgeDetectionAnalyzer())
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                
                isCameraStarted = true
                startCameraButton.setImageResource(android.R.drawable.ic_media_pause)
                
                statusText.text = "Camera active - Ready for edge detection!"
                statusText.visibility = View.VISIBLE
                Log.d(TAG, "Camera started successfully with image analysis")
                
            } catch (exc: Exception) {
                Log.e(TAG, "Camera start failed", exc)
                statusText.text = "Camera start failed: ${exc.message}"
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            
            isCameraStarted = false
            startCameraButton.setImageResource(R.drawable.ic_camera)
            
            statusText.text = "Camera stopped"
            statusText.visibility = View.GONE
            Log.d(TAG, "Camera stopped")
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun updateStatus() {
        if (NativeLib.isLibraryLoaded()) {
            try {
                val result = NativeLib.stringFromJNI()
                val processorInit = NativeLib.initializeProcessor()
                val processorStatus = if (processorInit) "✅ Processor Ready" else "⚠️ Processor Pending"
                statusText.text = "✅ $result\n$processorStatus\nTap buttons to test"
            } catch (e: Exception) {
                statusText.text = "⚠️ Native lib error: ${e.message}"
            }
        } else {
            statusText.text = "❌ Native library not loaded"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    private inner class EdgeDetectionAnalyzer : ImageAnalysis.Analyzer {
        private var frameCount = 0
        private var lastProcessTime = System.currentTimeMillis()
        
        override fun analyze(image: ImageProxy) {
            frameCount++
            val currentTime = System.currentTimeMillis()
            
            // Process every 10th frame to avoid overloading (3fps on 30fps camera)
            if (frameCount % 10 == 0) {
                try {
                    // Always process frames for display (for OpenGL effects or edge detection)
                    if (NativeLib.isProcessorReady()) {
                        processImageForEffects(image)
                        
                        // Update status every 2 seconds
                        if (currentTime - lastProcessTime > 2000) {
                            runOnUiThread {
                                val status = if (isEdgeDetectionEnabled) {
                                    "Processing with edge detection"
                                } else {
                                    "Processing with OpenGL effects"
                                }
                                statusText.text = "$status - Frame: $frameCount"
                            }
                            lastProcessTime = currentTime
                        }
                    } else if (currentTime - lastProcessTime > 2000) {
                        runOnUiThread {
                            statusText.text = "Camera active - Processor not ready - Frame: $frameCount"
                        }
                        lastProcessTime = currentTime
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Frame processing error: ${e.message}")
                }
            }
            
            image.close()
        }
        
        private fun processImageForEffects(image: ImageProxy) {
            try {
                // Convert ImageProxy to byte array
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                
                // Log frame processing
                Log.d(TAG, "Processing frame: ${image.width}x${image.height}, bytes: ${bytes.size}")
                
                // Call native processor (always process for bitmap creation)
                val processedData = NativeLib.processFrameData(bytes, image.width, image.height, isEdgeDetectionEnabled)
                
                if (processedData != null) {
                    Log.d(TAG, "Frame processed successfully, output size: ${processedData.size}")
                    // Convert processed data to bitmap and display with appropriate effects
                    convertAndDisplayBitmap(processedData, bytes, image.width, image.height)
                    Log.d(TAG, "Frame processed and displayed with ${if (isEdgeDetectionEnabled) "edge detection" else "OpenGL effects"}")
                } else {
                    Log.w(TAG, "Frame processing returned null")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}")
            }
        }
        
        private fun createOriginalBitmap(cameraData: ByteArray, width: Int, height: Int): Bitmap? {
            return try {
                // Convert YUV camera data to grayscale for simplicity
                val pixels = IntArray(width * height)
                for (i in 0 until width * height) {
                    if (i < cameraData.size) {
                        val gray = cameraData[i].toInt() and 0xFF
                        pixels[i] = Color.argb(255, gray, gray, gray)
                    }
                }
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                
                // Rotate to match camera orientation
                val matrix = Matrix().apply { postRotate(90f) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                
                if (bitmap != rotatedBitmap) {
                    bitmap.recycle()
                }
                
                rotatedBitmap
            } catch (e: Exception) {
                Log.e(TAG, "Error creating original bitmap: ${e.message}")
                null
            }
        }
        
        private fun convertAndDisplayBitmap(processedData: ByteArray, cameraData: ByteArray, width: Int, height: Int) {
            try {
                // Create bitmap from processed data
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                
                // Convert grayscale data to ARGB format
                val pixels = IntArray(width * height)
                for (i in processedData.indices.take(width * height)) {
                    val gray = processedData[i].toInt() and 0xFF
                    pixels[i] = Color.argb(255, gray, gray, gray)
                }
                
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                
                // Fix rotation - rotate 90 degrees clockwise to match camera orientation
                val matrix = Matrix().apply {
                    postRotate(90f)
                }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                
                // Update display with bitmap - show edge detection OR OpenGL effects
                runOnUiThread {
                    if (isEdgeDetectionEnabled) {
                        // Show edge detection result in regular ImageView
                        processedImageView.setImageBitmap(rotatedBitmap)
                        processedImageView.visibility = View.VISIBLE
                        processedImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        processedImageView.adjustViewBounds = true
                        
                        glSurfaceView.visibility = View.GONE
                        Log.d(TAG, "Displaying edge detection result")
                    } else {
                        // Show original camera feed with OpenGL effects applied
                        // For effects, we want the original image, so get it from the raw camera data
                        val originalBitmap = createOriginalBitmap(cameraData, width, height)
                        if (originalBitmap != null) {
                            glRenderer.updateTexture(originalBitmap)
                            glSurfaceView.requestRender()
                            glSurfaceView.visibility = View.VISIBLE
                            processedImageView.visibility = View.GONE
                            Log.d(TAG, "Displaying OpenGL effects")
                            originalBitmap.recycle()
                        }
                    }
                }
                
                // Clean up original bitmap
                if (bitmap != rotatedBitmap) {
                    bitmap.recycle()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating bitmap: ${e.message}")
            }
        }
    }
}