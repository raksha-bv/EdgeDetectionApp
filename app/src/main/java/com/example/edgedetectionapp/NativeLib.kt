package com.example.edgedetectionapp

import android.util.Log

object NativeLib {
    private var isNativeLoaded = false
    private var isProcessorInitialized = false
    
    init {
        try {
            System.loadLibrary("native-lib")
            isNativeLoaded = true
            Log.d("NativeLib", "Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("NativeLib", "Failed to load native library: ${e.message}")
            isNativeLoaded = false
        }
    }
    
    fun initializeProcessor(): Boolean {
        return try {
            if (isNativeLoaded) {
                initProcessor()
                isProcessorInitialized = true
                Log.d("NativeLib", "Processor initialized successfully")
                true
            } else {
                Log.w("NativeLib", "Cannot initialize processor - native library not loaded")
                false
            }
        } catch (e: Exception) {
            Log.e("NativeLib", "Failed to initialize processor: ${e.message}")
            isProcessorInitialized = false
            false
        }
    }
    
    fun isLibraryLoaded(): Boolean = isNativeLoaded
    fun isProcessorReady(): Boolean = isProcessorInitialized

    external fun stringFromJNI(): String
    external fun initProcessor()
    external fun processFrame(matAddr: Long, applyEdgeDetection: Boolean): Long
    external fun processFrameData(imageData: ByteArray, width: Int, height: Int, applyEdgeDetection: Boolean): ByteArray?
    external fun releaseProcessor()
    external fun initRenderer(width: Int, height: Int)
    external fun renderFrame(matAddr: Long)
    external fun releaseRenderer()
}