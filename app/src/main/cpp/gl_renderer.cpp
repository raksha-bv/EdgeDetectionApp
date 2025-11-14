#include "gl_renderer.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define LOG_TAG "GLRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Vertex shader source
const char* vertexShaderSource = R"(
attribute vec4 a_position;
attribute vec2 a_texCoord;
varying vec2 v_texCoord;
void main() {
    gl_Position = a_position;
    v_texCoord = a_texCoord;
}
)";

// Fragment shader source
const char* fragmentShaderSource = R"(
precision mediump float;
varying vec2 v_texCoord;
uniform sampler2D u_texture;
void main() {
    gl_FragColor = texture2D(u_texture, v_texCoord);
}
)";

GLRenderer::GLRenderer() : program(0), textureId(0), vbo(0), 
                          positionLoc(-1), texCoordLoc(-1), textureLoc(-1),
                          viewportWidth(0), viewportHeight(0) {
    LOGI("GLRenderer created");
}

GLRenderer::~GLRenderer() {
    release();
    LOGI("GLRenderer destroyed");
}

void GLRenderer::init(int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
    
    if (!createShaderProgram()) {
        LOGE("Failed to create shader program");
        return;
    }
    
    // Get attribute and uniform locations
    positionLoc = glGetAttribLocation(program, "a_position");
    texCoordLoc = glGetAttribLocation(program, "a_texCoord");
    textureLoc = glGetUniformLocation(program, "u_texture");
    
    // Generate texture
    glGenTextures(1, &textureId);
    
    setupGeometry();
    
    LOGI("GLRenderer initialized with viewport %dx%d", width, height);
}

void GLRenderer::render(jlong matAddr) {
    cv::Mat* mat = (cv::Mat*)matAddr;
    if (mat == nullptr || program == 0) {
        LOGE("Invalid Mat pointer or shader program not initialized");
        return;
    }
    
    glViewport(0, 0, viewportWidth, viewportHeight);
    glClear(GL_COLOR_BUFFER_BIT);
    
    glUseProgram(program);
    
    // Update texture with Mat data
    updateTexture(*mat);
    
    // Bind VBO and set vertex attributes
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glEnableVertexAttribArray(positionLoc);
    glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(texCoordLoc);
    glVertexAttribPointer(texCoordLoc, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    
    // Bind texture
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glUniform1i(textureLoc, 0);
    
    // Draw the quad
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    
    // Cleanup
    glDisableVertexAttribArray(positionLoc);
    glDisableVertexAttribArray(texCoordLoc);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void GLRenderer::release() {
    if (program != 0) {
        glDeleteProgram(program);
        program = 0;
    }
    
    if (textureId != 0) {
        glDeleteTextures(1, &textureId);
        textureId = 0;
    }
    
    if (vbo != 0) {
        glDeleteBuffers(1, &vbo);
        vbo = 0;
    }
    
    LOGI("GLRenderer released");
}

bool GLRenderer::createShaderProgram() {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderSource);
    if (vertexShader == 0) return false;
    
    GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
    if (fragmentShader == 0) {
        glDeleteShader(vertexShader);
        return false;
    }
    
    program = glCreateProgram();
    if (program == 0) {
        LOGE("Failed to create shader program");
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return false;
    }
    
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    
    GLint linkStatus;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus != GL_TRUE) {
        GLchar infoLog[512];
        glGetProgramInfoLog(program, sizeof(infoLog), nullptr, infoLog);
        LOGE("Shader program linking failed: %s", infoLog);
        glDeleteProgram(program);
        program = 0;
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return false;
    }
    
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    
    return true;
}

GLuint GLRenderer::loadShader(GLenum type, const char* shaderSrc) {
    GLuint shader = glCreateShader(type);
    if (shader == 0) {
        LOGE("Failed to create shader of type %d", type);
        return 0;
    }
    
    glShaderSource(shader, 1, &shaderSrc, nullptr);
    glCompileShader(shader);
    
    GLint compileStatus;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (compileStatus != GL_TRUE) {
        GLchar infoLog[512];
        glGetShaderInfoLog(shader, sizeof(infoLog), nullptr, infoLog);
        LOGE("Shader compilation failed: %s", infoLog);
        glDeleteShader(shader);
        return 0;
    }
    
    return shader;
}

void GLRenderer::setupGeometry() {
    // Vertices for full-screen quad (position + texture coordinates)
    float vertices[] = {
        -1.0f, -1.0f, 0.0f, 1.0f,  // Bottom-left
         1.0f, -1.0f, 1.0f, 1.0f,  // Bottom-right
        -1.0f,  1.0f, 0.0f, 0.0f,  // Top-left
         1.0f,  1.0f, 1.0f, 0.0f   // Top-right
    };
    
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void GLRenderer::updateTexture(cv::Mat& mat) {
    if (textureId == 0 || mat.empty()) {
        LOGE("Invalid texture or empty Mat");
        return;
    }
    
    glBindTexture(GL_TEXTURE_2D, textureId);
    
    // Determine format based on Mat channels
    GLenum format = (mat.channels() == 1) ? GL_LUMINANCE : GL_RGB;
    
    glTexImage2D(GL_TEXTURE_2D, 0, format, mat.cols, mat.rows, 0, format, GL_UNSIGNED_BYTE, mat.data);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
}