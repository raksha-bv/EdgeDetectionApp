package com.example.edgedetectionapp

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import org.opencv.core.Mat
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var currentMat: Mat? = null
    private var applyEdgeDetection = false
    private val lock = Object()

    companion object {
        private const val TAG = "CameraGLSurfaceView"
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(CameraRenderer())
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateFrame(mat: Mat) {
        synchronized(lock) {
            currentMat?.release()
            currentMat = mat.clone()
        }
        requestRender()
    }

    fun setEdgeDetectionEnabled(enabled: Boolean) {
        applyEdgeDetection = enabled
        Log.d(TAG, "Edge detection: $enabled")
    }

    private inner class CameraRenderer : Renderer {
        private var initialized = false

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d(TAG, "Surface created")
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.d(TAG, "Surface changed: ${width}x${height}")
            NativeLib.initRenderer(width, height)
            initialized = true
        }

        override fun onDrawFrame(gl: GL10?) {
            if (!initialized) return

            synchronized(lock) {
                currentMat?.let { mat ->
                    try {
                        // Process frame
                        val processedMatAddr = NativeLib.processFrame(
                            mat.nativeObjAddr,
                            applyEdgeDetection
                        )
                        
                        // Render frame
                        if (processedMatAddr != 0L) {
                            NativeLib.renderFrame(processedMatAddr)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Render error: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        synchronized(lock) {
            currentMat?.release()
            currentMat = null
        }
    }
}