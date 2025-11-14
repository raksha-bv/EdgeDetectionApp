#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include <cmath>
#include "opencv_processor.h"
#include "gl_renderer.h"

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Sobel edge detection kernels
const int sobelX[3][3] = {
    {-1, 0, 1},
    {-2, 0, 2},
    {-1, 0, 1}
};

const int sobelY[3][3] = {
    {-1, -2, -1},
    { 0,  0,  0},
    { 1,  2,  1}
};

// Gaussian blur kernel for noise reduction
const float gaussianKernel[5][5] = {
    {1, 4, 6, 4, 1},
    {4, 16, 24, 16, 4},
    {6, 24, 36, 24, 6},
    {4, 16, 24, 16, 4},
    {1, 4, 6, 4, 1}
};

// Apply Gaussian blur to reduce noise
void applyGaussianBlur(unsigned char* input, unsigned char* output, int width, int height) {
    const float kernelSum = 256.0f; // Sum of all kernel values
    
    for (int y = 2; y < height - 2; y++) {
        for (int x = 2; x < width - 2; x++) {
            float sum = 0;
            
            for (int ky = -2; ky <= 2; ky++) {
                for (int kx = -2; kx <= 2; kx++) {
                    int pixelIdx = (y + ky) * width + (x + kx);
                    sum += input[pixelIdx] * gaussianKernel[ky + 2][kx + 2];
                }
            }
            
            output[y * width + x] = (unsigned char)(sum / kernelSum);
        }
    }
}

// Apply advanced Sobel edge detection with non-maximum suppression
void applySobelEdgeDetection(unsigned char* input, unsigned char* output, int width, int height) {
    unsigned char* gradientMag = new unsigned char[width * height];
    float* gradientDir = new float[width * height];
    
    // Step 1: Calculate gradients
    for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            int gradientX = 0;
            int gradientY = 0;
            
            // Apply Sobel kernels
            for (int ky = -1; ky <= 1; ky++) {
                for (int kx = -1; kx <= 1; kx++) {
                    int pixelIdx = (y + ky) * width + (x + kx);
                    unsigned char pixelValue = input[pixelIdx];
                    
                    gradientX += pixelValue * sobelX[ky + 1][kx + 1];
                    gradientY += pixelValue * sobelY[ky + 1][kx + 1];
                }
            }
            
            // Calculate magnitude and direction
            float magnitude = sqrt(gradientX * gradientX + gradientY * gradientY);
            float direction = atan2(gradientY, gradientX);
            
            int idx = y * width + x;
            gradientMag[idx] = (magnitude > 255) ? 255 : (unsigned char)magnitude;
            gradientDir[idx] = direction;
        }
    }
    
    // Step 2: Non-maximum suppression for thinner edges
    for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            int idx = y * width + x;
            float angle = gradientDir[idx];
            unsigned char mag = gradientMag[idx];
            
            // Determine neighbors based on gradient direction
            unsigned char neighbor1, neighbor2;
            
            if ((angle >= -22.5 && angle <= 22.5) || (angle >= 157.5 || angle <= -157.5)) {
                // Horizontal edge
                neighbor1 = gradientMag[y * width + (x + 1)];
                neighbor2 = gradientMag[y * width + (x - 1)];
            } else if ((angle >= 22.5 && angle <= 67.5) || (angle >= -157.5 && angle <= -112.5)) {
                // Diagonal edge (/)
                neighbor1 = gradientMag[(y + 1) * width + (x - 1)];
                neighbor2 = gradientMag[(y - 1) * width + (x + 1)];
            } else if ((angle >= 67.5 && angle <= 112.5) || (angle >= -112.5 && angle <= -67.5)) {
                // Vertical edge
                neighbor1 = gradientMag[(y + 1) * width + x];
                neighbor2 = gradientMag[(y - 1) * width + x];
            } else {
                // Diagonal edge (\)
                neighbor1 = gradientMag[(y + 1) * width + (x + 1)];
                neighbor2 = gradientMag[(y - 1) * width + (x - 1)];
            }
            
            // Suppress non-maximum pixels
            if (mag >= neighbor1 && mag >= neighbor2 && mag > 30) {
                // Apply enhanced contrast for strong edges
                int enhanced = (int)(mag * 1.5);
                output[idx] = (enhanced > 255) ? 255 : (unsigned char)enhanced;
            } else {
                output[idx] = 0;
            }
        }
    }
    
    delete[] gradientMag;
    delete[] gradientDir;
}

OpenCVProcessor* processor = nullptr;
GLRenderer* renderer = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_edgedetectionapp_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "EdgeDetection Native Library Ready (OpenCV Integration Prepared)";
    LOGD("stringFromJNI called");
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_initProcessor(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Initializing OpenCV Processor");
    try {
        if (processor == nullptr) {
            processor = new OpenCVProcessor();
            LOGD("OpenCV Processor created successfully");
        } else {
            LOGD("OpenCV Processor already initialized");
        }
    } catch (const std::exception& e) {
        LOGE("Failed to create OpenCV Processor: %s", e.what());
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionapp_NativeLib_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr,
        jboolean applyEdgeDetection) {
    
    if (processor == nullptr) {
        LOGE("Processor not initialized");
        return 0;
    }
    
    // Convert jlong to void* and back for stub implementation
    void* result = processor->processFrame((void*)matAddr, applyEdgeDetection);
    return (jlong)result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetectionapp_NativeLib_processFrameData(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height,
        jboolean applyEdgeDetection) {
    
    LOGD("Processing frame data: %dx%d, edge detection: %s", width, height, applyEdgeDetection ? "ON" : "OFF");
    
    if (processor == nullptr) {
        LOGE("Processor not initialized");
        return nullptr;
    }
    
    // Get input data
    jbyte* inputData = env->GetByteArrayElements(imageData, nullptr);
    jsize dataSize = env->GetArrayLength(imageData);
    
    if (inputData == nullptr) {
        LOGE("Failed to get input data");
        return nullptr;
    }
    
    // Create output array
    jbyteArray result = env->NewByteArray(width * height);
    jbyte* outputData = env->GetByteArrayElements(result, nullptr);
    
    if (applyEdgeDetection) {
        // Convert input to unsigned char
        unsigned char* inputBuffer = new unsigned char[width * height];
        unsigned char* tempBuffer = new unsigned char[width * height];
        unsigned char* outputBuffer = new unsigned char[width * height];
        
        // Extract luminance from YUV format (first plane)
        for (int i = 0; i < width * height && i < dataSize; i++) {
            inputBuffer[i] = (unsigned char)(inputData[i] & 0xFF);
        }
        
        // Step 1: Apply Gaussian blur to reduce noise
        applyGaussianBlur(inputBuffer, tempBuffer, width, height);
        
        // Step 2: Apply advanced Sobel edge detection
        applySobelEdgeDetection(tempBuffer, outputBuffer, width, height);
        
        // Copy processed data back to output
        for (int i = 0; i < width * height; i++) {
            outputData[i] = (jbyte)outputBuffer[i];
        }
        
        delete[] inputBuffer;
        delete[] tempBuffer;
        delete[] outputBuffer;
        
        LOGD("Applied advanced Sobel edge detection with noise reduction");
    } else {
        // Pass through unchanged
        for (int i = 0; i < width * height && i < dataSize; i++) {
            outputData[i] = inputData[i];
        }
        LOGD("Pass-through mode (no edge detection)");
    }
    
    env->ReleaseByteArrayElements(imageData, inputData, JNI_ABORT);
    env->ReleaseByteArrayElements(result, outputData, 0);
    
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_releaseProcessor(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Releasing OpenCV Processor");
    if (processor != nullptr) {
        delete processor;
        processor = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_initRenderer(
        JNIEnv* env,
        jobject /* this */,
        jint width,
        jint height) {
    LOGD("Initializing OpenGL Renderer: %dx%d", width, height);
    if (renderer == nullptr) {
        renderer = new GLRenderer();
    }
    renderer->initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_renderFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {
    if (renderer != nullptr) {
        renderer->renderFrame((void*)matAddr, 0, 0);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_releaseRenderer(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Releasing OpenGL Renderer");
    if (renderer != nullptr) {
        renderer->cleanup();
        delete renderer;
        renderer = nullptr;
    }
}

// Apply grayscale shader effect
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetectionapp_NativeLib_applyGrayscaleShader(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height) {
    
    LOGD("Applying grayscale shader: %dx%d", width, height);
    
    // Get input data
    jbyte* inputData = env->GetByteArrayElements(imageData, nullptr);
    jsize dataSize = env->GetArrayLength(imageData);
    
    if (inputData == nullptr) {
        LOGE("Failed to get input data for grayscale shader");
        return nullptr;
    }
    
    // Create output array
    jbyteArray result = env->NewByteArray(width * height * 3); // RGB output
    jbyte* outputData = env->GetByteArrayElements(result, nullptr);
    
    // Apply grayscale conversion (luminance formula: 0.299*R + 0.587*G + 0.114*B)
    for (int i = 0; i < width * height && i < dataSize; i++) {
        unsigned char gray = (unsigned char)(inputData[i] & 0xFF);
        // Convert to RGB format for shader compatibility
        outputData[i * 3] = gray;     // R
        outputData[i * 3 + 1] = gray; // G
        outputData[i * 3 + 2] = gray; // B
    }
    
    env->ReleaseByteArrayElements(imageData, inputData, JNI_ABORT);
    env->ReleaseByteArrayElements(result, outputData, 0);
    
    return result;
}

// Apply invert shader effect
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetectionapp_NativeLib_applyInvertShader(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height) {
    
    LOGD("Applying invert shader: %dx%d", width, height);
    
    // Get input data
    jbyte* inputData = env->GetByteArrayElements(imageData, nullptr);
    jsize dataSize = env->GetArrayLength(imageData);
    
    if (inputData == nullptr) {
        LOGE("Failed to get input data for invert shader");
        return nullptr;
    }
    
    // Create output array
    jbyteArray result = env->NewByteArray(width * height * 3); // RGB output
    jbyte* outputData = env->GetByteArrayElements(result, nullptr);
    
    // Apply color inversion
    for (int i = 0; i < width * height && i < dataSize; i++) {
        unsigned char original = (unsigned char)(inputData[i] & 0xFF);
        unsigned char inverted = 255 - original;
        // Convert to RGB format for shader compatibility
        outputData[i * 3] = inverted;     // R
        outputData[i * 3 + 1] = inverted; // G
        outputData[i * 3 + 2] = inverted; // B
    }
    
    env->ReleaseByteArrayElements(imageData, inputData, JNI_ABORT);
    env->ReleaseByteArrayElements(result, outputData, 0);
    
    return result;
}