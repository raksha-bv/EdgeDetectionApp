cmake_minimum_required(VERSION 3.22.1)
project("edgedetectionapp")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV path (adjust this to your extracted OpenCV location)
# Using absolute path to ensure CMake finds OpenCV correctly
set(OpenCV_DIR "C:/Users/raksh/AndroidStudioProjects/opencv-android-sdk/sdk/native/jni")

# Find OpenCV
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add native library
add_library(native-lib SHARED
        native-lib.cpp
        opencv_processor.cpp
        gl_renderer.cpp)

# Link libraries
target_link_libraries(native-lib
        ${OpenCV_LIBS}
        android
        log
        GLESv2
        EGL)