package com.example.edgedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var shaderEffectSpinner: Spinner
    private lateinit var cameraPreview: PreviewView
    private lateinit var processedImageView: ImageView
    private lateinit var cameraExecutor: ExecutorService
    
    private var isCameraStarted = false
    private var isEdgeDetectionEnabled = false
    private var currentShaderEffect = 0  // 0=Normal, 1=Grayscale, 2=Invert, 3=Edge Enhance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_camera)

        Log.d(TAG, "MainActivity created")

        // Initialize UI components
        statusText = findViewById(R.id.statusText)
        startCameraButton = findViewById(R.id.startCameraButton)
        edgeDetectionSwitch = findViewById(R.id.edgeDetectionSwitch)
        shaderEffectSpinner = findViewById(R.id.shaderEffectSpinner)
        cameraPreview = findViewById(R.id.cameraPreview)
        processedImageView = findViewById(R.id.processedImageView)
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Setup shader effects spinner
        setupShaderEffects()

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
            Log.d(TAG, "Edge detection ${if (isChecked) "enabled" else "disabled"}")
            
            // Show/hide processed view based on edge detection or shader effect
            if (!isChecked && currentShaderEffect == 0) {
                processedImageView.visibility = View.GONE
            }
        }

        // Initial status
        updateStatus()
    }
    
    private fun setupShaderEffects() {
        val effects = arrayOf("Normal", "Grayscale", "Invert", "Edge Enhance")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, effects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shaderEffectSpinner.adapter = adapter
        
        shaderEffectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentShaderEffect = position
                Log.d(TAG, "Shader effect changed to: ${effects[position]}")
                
                // Show processed view if any effect is selected
                if (position != 0 || isEdgeDetectionEnabled) {
                    processedImageView.visibility = View.VISIBLE
                } else {
                    processedImageView.visibility = View.GONE
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            // Use FILL_CENTER to remove letterboxing
            cameraPreview.scaleType = PreviewView.ScaleType.FILL_CENTER
            
            // Use 16:9 aspect ratio for better screen coverage
            val aspectRatio = AspectRatio.RATIO_16_9
            
            val preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.surfaceProvider)
                }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
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
                statusText.text = "Camera active - Ready for effects!"
                statusText.visibility = View.VISIBLE
                Log.d(TAG, "Camera started successfully with 16:9 aspect ratio")
                
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
            processedImageView.visibility = View.GONE
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
                statusText.text = "✅ $result\n$processorStatus\nTap camera button to start"
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
            
            // Process every 5th frame for better performance (6fps on 30fps camera)
            if (frameCount % 5 == 0) {
                try {
                    // Only process if edge detection or shader effect is enabled
                    if (isEdgeDetectionEnabled || currentShaderEffect != 0) {
                        if (NativeLib.isProcessorReady()) {
                            processImageForEffects(image)
                            
                            // Update status every 2 seconds
                            if (currentTime - lastProcessTime > 2000) {
                                runOnUiThread {
                                    val effectName = when (currentShaderEffect) {
                                        1 -> "Grayscale"
                                        2 -> "Invert"
                                        3 -> "Edge Enhance"
                                        else -> if (isEdgeDetectionEnabled) "Edge Detection" else "Normal"
                                    }
                                    statusText.text = "Effect: $effectName | Frame: $frameCount"
                                }
                                lastProcessTime = currentTime
                            }
                        } else if (currentTime - lastProcessTime > 2000) {
                            runOnUiThread {
                                statusText.text = "Camera active - Processor not ready - Frame: $frameCount"
                            }
                            lastProcessTime = currentTime
                        }
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
                
                // Call native processor
                val processedData = NativeLib.processFrameData(
                    bytes, 
                    image.width, 
                    image.height, 
                    isEdgeDetectionEnabled
                )
                
                if (processedData != null) {
                    // Convert processed data to bitmap and apply shader effects
                    convertAndDisplayBitmap(processedData, bytes, image.width, image.height)
                } else {
                    Log.w(TAG, "Frame processing returned null")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}")
            }
        }
        
        private fun convertAndDisplayBitmap(
            processedData: ByteArray, 
            cameraData: ByteArray, 
            width: Int, 
            height: Int
        ) {
            try {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                
                val pixels = IntArray(width * height)
                
                // Apply shader effects
                for (i in processedData.indices.take(width * height)) {
                    val gray = processedData[i].toInt() and 0xFF
                    
                    val color = when (currentShaderEffect) {
                        1 -> {
                            // Grayscale (already grayscale, just apply)
                            Color.argb(255, gray, gray, gray)
                        }
                        2 -> {
                            // Invert
                            val inverted = 255 - gray
                            Color.argb(255, inverted, inverted, inverted)
                        }
                        3 -> {
                            // Edge Enhance (boost contrast)
                            val enhanced = ((gray - 128) * 1.8 + 128).toInt().coerceIn(0, 255)
                            Color.argb(255, enhanced, enhanced, enhanced)
                        }
                        else -> {
                            // Normal (no shader effect)
                            Color.argb(255, gray, gray, gray)
                        }
                    }
                    pixels[i] = color
                }
                
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                
                // Rotate to match camera orientation
                val matrix = Matrix().apply {
                    postRotate(90f)
                }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                
                runOnUiThread {
                    // Show processed view if edge detection or any shader effect is active
                    if (isEdgeDetectionEnabled || currentShaderEffect != 0) {
                        processedImageView.apply {
                            setImageBitmap(rotatedBitmap)
                            visibility = View.VISIBLE
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    } else {
                        processedImageView.visibility = View.GONE
                    }
                }
                
                // Clean up
                if (bitmap != rotatedBitmap) {
                    bitmap.recycle()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating bitmap: ${e.message}")
            }
        }
    }
}
