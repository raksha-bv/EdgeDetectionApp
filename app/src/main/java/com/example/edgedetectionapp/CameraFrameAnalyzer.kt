package com.example.edgedetectionapp

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class CameraFrameAnalyzer(
    private val glSurfaceView: CameraGLSurfaceView,
    private val onFpsUpdate: (Float) -> Unit
) : ImageAnalysis.Analyzer {

    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0f

    companion object {
        private const val TAG = "FrameAnalyzer"
    }

    override fun analyze(image: ImageProxy) {
        try {
            val mat = imageProxyToMat(image)
            
            if (mat != null && !mat.empty()) {
                glSurfaceView.updateFrame(mat)
                
                // Calculate FPS
                frameCount++
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastFpsTime
                
                if (elapsed >= 1000) { // Update every second
                    currentFps = (frameCount * 1000f) / elapsed
                    onFpsUpdate(currentFps)
                    frameCount = 0
                    lastFpsTime = currentTime
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing frame: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun imageProxyToMat(image: ImageProxy): Mat? {
        val planes = image.planes
        if (planes.isEmpty()) return null

        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuvMat.put(0, 0, nv21)

        val rgbaMat = Mat()
        Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
        
        // Rotate if needed
        val rotatedMat = Mat()
        val rotationCode = when (image.imageInfo.rotationDegrees) {
            90 -> org.opencv.core.Core.ROTATE_90_CLOCKWISE
            180 -> org.opencv.core.Core.ROTATE_180
            270 -> org.opencv.core.Core.ROTATE_90_COUNTERCLOCKWISE
            else -> -1
        }
        
        if (rotationCode != -1) {
            org.opencv.core.Core.rotate(rgbaMat, rotatedMat, rotationCode)
            rgbaMat.release()
            yuvMat.release()
            return rotatedMat
        }
        
        yuvMat.release()
        return rgbaMat
    }
}