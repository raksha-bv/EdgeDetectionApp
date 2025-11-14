#ifndef EDGEDETECTION_GL_RENDERER_H
#define EDGEDETECTION_GL_RENDERER_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
// #include <opencv2/opencv.hpp>
#include <jni.h>

class GLRenderer {
public:
    GLRenderer();
    ~GLRenderer();
    
    bool initialize();
    void renderFrame(void* frame, int width, int height);
    void cleanup();
    
private:
    GLuint program;
    GLuint textureId;
    GLuint vbo;
    GLint positionLoc;
    GLint texCoordLoc;
    GLint textureLoc;
    
    int viewportWidth;
    int viewportHeight;
    
    bool createShaderProgram();
    GLuint loadShader(GLenum type, const char* shaderSrc);
    void setupGeometry();
    // Remove OpenCV-specific method
    // void updateTexture(cv::Mat& mat);
};

#endif // EDGEDETECTION_GL_RENDERER_H