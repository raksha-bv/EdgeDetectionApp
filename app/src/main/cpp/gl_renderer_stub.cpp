#include "gl_renderer.h"
// #include <opencv2/opencv.hpp>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define LOG_TAG "GLRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

GLRenderer::GLRenderer() {
    LOGI("GLRenderer initialized (stub)");
}

GLRenderer::~GLRenderer() {
    LOGI("GLRenderer destroyed");
}

bool GLRenderer::initialize() {
    LOGI("GLRenderer initialize called (stub implementation)");
    return true;
}

void GLRenderer::renderFrame(void* frame, int width, int height) {
    LOGI("renderFrame called (stub implementation)");
    // Clear the screen with black color
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
}

void GLRenderer::cleanup() {
    LOGI("GLRenderer cleanup called (stub implementation)");
}