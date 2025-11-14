#include "opencv_processor.h"
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

#define LOG_TAG "OpenCVProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;

OpenCVProcessor::OpenCVProcessor() {
    LOGI("OpenCVProcessor initialized");
}

OpenCVProcessor::~OpenCVProcessor() {
    LOGI("OpenCVProcessor destroyed");
}

jlong OpenCVProcessor::processFrame(jlong inputMatAddr, bool applyEdgeDetection) {
    try {
        Mat* inputMat = (Mat*)inputMatAddr;
        if (inputMat == nullptr) {
            LOGE("Input Mat is null");
            return 0;
        }
        
        // Convert to grayscale if needed
        if (inputMat->channels() == 3) {
            cvtColor(*inputMat, grayMat, COLOR_BGR2GRAY);
        } else if (inputMat->channels() == 4) {
            cvtColor(*inputMat, grayMat, COLOR_BGRA2GRAY);
        } else {
            grayMat = inputMat->clone();
        }
        
        if (applyEdgeDetection) {
            // Apply Gaussian blur to reduce noise
            Mat blurredMat;
            GaussianBlur(grayMat, blurredMat, Size(5, 5), 1.4);
            
            // Apply Canny edge detection
            Canny(blurredMat, edgesMat, 50.0, 150.0);
            processedMat = edgesMat.clone();
        } else {
            processedMat = grayMat.clone();
        }
        
        LOGI("Frame processing completed successfully");
        return (jlong)&processedMat;
        
    } catch (const cv::Exception& e) {
        LOGE("OpenCV error in frame processing: %s", e.what());
        return 0;
    }
}