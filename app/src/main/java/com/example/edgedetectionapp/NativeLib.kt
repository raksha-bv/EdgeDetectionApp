package com.example.edgedetectionapp

object NativeLib {
    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("native-lib")
    }

    external fun stringFromJNI(): String
    external fun initProcessor()
    external fun processFrame(matAddr: Long, applyEdgeDetection: Boolean): Long
    external fun releaseProcessor()
    external fun initRenderer(width: Int, height: Int)
    external fun renderFrame(matAddr: Long)
    external fun releaseRenderer()
}