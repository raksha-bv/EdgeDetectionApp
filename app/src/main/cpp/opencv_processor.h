#ifndef EDGEDETECTION_OPENCV_PROCESSOR_H
#define EDGEDETECTION_OPENCV_PROCESSOR_H

// #include <opencv2/opencv.hpp>
// #include <opencv2/imgproc.hpp>
#include <jni.h>

class OpenCVProcessor {
public:
    OpenCVProcessor();
    ~OpenCVProcessor();
    
    // Stub methods that return void* instead of cv::Mat
    void* processFrame(void* inputFrame, bool applyEdgeDetection);
    void* applyEdgeDetection(void* frame);
    void* convertToGray(void* frame);
    
private:
    // Remove OpenCV-specific members for now
    // cv::Mat processedMat;
    // cv::Mat grayMat;
    // cv::Mat edgesMat;
};

#endif // EDGEDETECTION_OPENCV_PROCESSOR_H