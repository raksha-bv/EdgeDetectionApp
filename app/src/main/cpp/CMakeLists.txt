cmake_minimum_required(VERSION 3.22.1)
project("edgedetectionapp")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add native library
add_library(native-lib SHARED
        native-lib.cpp
        opencv_processor_stub.cpp
        gl_renderer_stub.cpp)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(EGL-lib EGL)
find_library(GLESv2-lib GLESv2)

# Link libraries
target_link_libraries(native-lib
        ${log-lib}
        ${android-lib}
        ${EGL-lib}
        ${GLESv2-lib})