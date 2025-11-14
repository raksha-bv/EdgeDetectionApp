#include <jni.h>
#include <string>
#include <android/log.h>
#include "opencv_processor.h"
#include "gl_renderer.h"

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

OpenCVProcessor* processor = nullptr;
GLRenderer* renderer = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_yourname_edgedetection_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "OpenCV + OpenGL Native Library Ready";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourname_edgedetection_NativeLib_initProcessor(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Initializing OpenCV Processor");
    if (processor == nullptr) {
        processor = new OpenCVProcessor();
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_yourname_edgedetection_NativeLib_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr,
        jboolean applyEdgeDetection) {
    
    if (processor == nullptr) {
        LOGE("Processor not initialized");
        return 0;
    }
    
    return processor->processFrame(matAddr, applyEdgeDetection);
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourname_edgedetection_NativeLib_releaseProcessor(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Releasing OpenCV Processor");
    if (processor != nullptr) {
        delete processor;
        processor = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourname_edgedetection_NativeLib_initRenderer(
        JNIEnv* env,
        jobject /* this */,
        jint width,
        jint height) {
    LOGD("Initializing OpenGL Renderer: %dx%d", width, height);
    if (renderer == nullptr) {
        renderer = new GLRenderer();
    }
    renderer->init(width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourname_edgedetection_NativeLib_renderFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {
    if (renderer != nullptr) {
        renderer->render(matAddr);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourname_edgedetection_NativeLib_releaseRenderer(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Releasing OpenGL Renderer");
    if (renderer != nullptr) {
        delete renderer;
        renderer = nullptr;
    }
}