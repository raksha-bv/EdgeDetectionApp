#ifndef EDGEDETECTION_OPENCV_PROCESSOR_H
#define EDGEDETECTION_OPENCV_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <jni.h>

class OpenCVProcessor {
public:
    OpenCVProcessor();
    ~OpenCVProcessor();
    
    // Process frame and return address of processed Mat
    jlong processFrame(jlong inputMatAddr, bool applyEdgeDetection);
    
private:
    cv::Mat processedMat;
    cv::Mat grayMat;
    cv::Mat edgesMat;
};

#endif // EDGEDETECTION_OPENCV_PROCESSOR_H