#ifndef EDGEDETECTION_OPENCV_PROCESSOR_H
#define EDGEDETECTION_OPENCV_PROCESSOR_H

#include <jni.h>

class OpenCVProcessor {
public:
    OpenCVProcessor();
    ~OpenCVProcessor();
    
    // Stub methods for now
    void* processFrame(void* inputFrame, bool applyEdgeDetection);
    void* applyEdgeDetection(void* frame);
    void* convertToGray(void* frame);
    
private:
    // Simple implementation without OpenCV for now
};

#endif // EDGEDETECTION_OPENCV_PROCESSOR_H