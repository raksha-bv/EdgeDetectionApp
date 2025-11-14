# Edge Detection App

A comprehensive edge detection application featuring both Android mobile app and web interface implementations. This project demonstrates real-time image processing capabilities using OpenCV, native C++ processing, and modern web technologies.

## Live Demo

🌐 **Web Application**: [https://edge-detection-app.vercel.app/](https://edge-detection-app.vercel.app/)

Try the edge detection features directly in your browser without any installation required!

### Demo Video

[![Android App Demo](https://img.youtube.com/vi/e7IrhoX7bJE/0.jpg)](https://www.youtube.com/shorts/e7IrhoX7bJE)

_Live demonstration of Android app edge detection capabilities - Click to watch on YouTube_

## Screenshots

### Android Application

![Android App - Edge Detection](media/screenshots/android/working_screenshot.webp)

_Real-time edge detection processing on Android device_

### Web Interface

![Web Interface - Edge Detection](media/screenshots/web/working_web.png)
_Browser-based edge detection with interactive controls_

## Features

### Android Application

- **Real-time Camera Processing**: Live edge detection using device camera
- **Native C++ Backend**: High-performance image processing with OpenCV integration
- **OpenGL Rendering**: Hardware-accelerated graphics rendering
- **Toggle Edge Detection**: Switch between normal camera view and edge detection mode
- **Optimized Performance**: Frame processing with intelligent buffering (3fps processing on 30fps camera)
- **Camera Permissions**: Seamless permission handling for camera access

### Web Interface

- **Modern Web App**: Next.js-based web application with TypeScript
- **Image Upload**: Drag & drop or click to upload images
- **Client-side Processing**: Real-time edge detection in the browser
- **Interactive Controls**: Adjustable threshold and kernel size parameters
- **Download Results**: Save processed images
- **Responsive Design**: Works on desktop and mobile devices

## Tech Stack

### Android App

- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **UI Framework**: Android Views with CameraX
- **Image Processing**: OpenCV 4.x
- **Native Code**: C++17 with CMake
- **Graphics**: OpenGL ES 2.0
- **Target SDK**: Android API 34 (Android 14)
- **Minimum SDK**: Android API 24 (Android 7.0)

### Web Application

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Image Processing**: OpenCV.js
- **Build Tool**: npm/yarn

### Native Backend

- **Language**: C++17
- **Image Processing**: OpenCV
- **Graphics**: OpenGL ES, EGL
- **Build System**: CMake 3.22.1

## Project Structure

```
EdgeDetectionApp/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── java/com/example/edgedetectionapp/
│   │   │   ├── MainActivity.kt   # Main activity with camera integration
│   │   │   └── NativeLib.kt      # JNI interface for native code
│   │   ├── cpp/                  # Native C++ code
│   │   │   ├── native-lib.cpp    # JNI implementation
│   │   │   ├── opencv_processor.* # OpenCV edge detection
│   │   │   └── gl_renderer.*     # OpenGL rendering
│   │   └── res/                  # Android resources
│   └── build.gradle.kts          # Android app build configuration
├── opencv/                       # OpenCV Android module
├── web-interface/                # Next.js web application
│   ├── src/
│   │   ├── app/                  # Next.js app router pages
│   │   └── components/           # React components
│   ├── package.json
│   └── README.md
├── gradle/
└── build.gradle.kts             # Root build configuration
```

## Quick Start

### Prerequisites

- **Android Development**:

  - Android Studio 2023.1+
  - Android SDK 34
  - NDK 25+
  - CMake 3.22.1+
  - OpenCV Android SDK 4.x

- **Web Development**:
  - Node.js 18+
  - npm or yarn

### Android App Setup

1. **Clone the repository**:

   ```bash
   git clone https://github.com/raksha-bv/EdgeDetectionApp.git
   cd EdgeDetectionApp
   ```

2. **Open in Android Studio**:

   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the EdgeDetectionApp folder

3. **Configure OpenCV** (if not already set up):

   - Download OpenCV Android SDK
   - Import the opencv module
   - Sync project

4. **Build and Run**:

   ```bash
   ./gradlew clean
   ./gradlew :app:assembleDebug
   ```

5. **Install on Device**:
   - Connect Android device via USB
   - Enable Developer Options and USB Debugging
   - Run the app from Android Studio

### Web Interface Setup

1. **Navigate to web interface**:

   ```bash
   cd web-interface
   ```

2. **Install dependencies**:

   ```bash
   npm install
   ```

3. **Start development server**:

   ```bash
   npm run dev
   ```

4. **Open in browser**:
   - Visit `http://localhost:3000`

## Configuration

### Android Configuration

**Gradle Configuration** (`app/build.gradle.kts`):

```kotlin
android {
    compileSdk = 34
    minSdk = 24
    targetSdk = 34

    externalNativeBuild {
        cmake {
            cppFlags += "-std=c++17"
        }
    }

    ndk {
        abiFilters += listOf("arm64-v8a", "armeabi-v7a")
    }
}
```

**Native Dependencies** (`CMakeLists.txt`):

- OpenCV 4.x
- OpenGL ES 2.0
- EGL
- Android NDK

### Web Configuration

**Package Dependencies** (`package.json`):

- Next.js 14
- React 18
- TypeScript 5
- Tailwind CSS 3

## Usage

### Android App

1. **Launch Application**: Open the Edge Detection App on your Android device
2. **Grant Permissions**: Allow camera access when prompted
3. **Start Camera**: Tap the camera button to begin live preview
4. **Toggle Edge Detection**: Use the switch to enable/disable edge detection processing
5. **View Results**: Processed frames appear in real-time with edge detection overlay

### Web Interface

1. **Upload Image**: Drag and drop or click to select an image file
2. **Adjust Parameters**: Use controls to modify edge detection settings
3. **Process Image**: Real-time processing shows edge detection results
4. **Download Result**: Save the processed image to your device

## Architecture

### Android Architecture

```
User Interface (Kotlin)
        ↓
Camera API (CameraX) → Image Analysis
        ↓
JNI Bridge (NativeLib.kt)
        ↓
Native C++ Processing
        ↓
OpenCV Edge Detection ← → OpenGL Rendering
        ↓
Processed Frame Output
```

### Web Architecture

```
React Components (TypeScript)
        ↓
Image Upload Handler
        ↓
Client-side Edge Detection
        ↓
Canvas Rendering
        ↓
Download Handler
```

## Edge Detection Algorithms

### Implemented Techniques

1. **Canny Edge Detection**: Primary algorithm for robust edge detection
2. **Sobel Operators**: Gradient-based edge detection
3. **Gaussian Blur**: Pre-processing for noise reduction
4. **Threshold Adjustment**: Customizable sensitivity settings

### Performance Optimizations

- **Frame Rate Control**: Process every 10th frame (3fps on 30fps camera)
- **Memory Management**: Efficient bitmap recycling
- **Background Processing**: Non-blocking camera operations
- **Native Code**: C++ implementation for computational efficiency

## Supported Platforms

### Android

- **Minimum**: Android 7.0 (API 24)
- **Target**: Android 14 (API 34)
- **Architectures**: ARM64-v8a, ARMv7
- **Permissions**: Camera access required

### Web

- **Browsers**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **Devices**: Desktop, tablet, and mobile responsive
- **File Formats**: JPEG, PNG, WebP

## Development

### Building from Source

**Android**:

```bash
# Clean build
./gradlew clean

# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease
```

**Web**:

```bash
# Development
npm run dev

# Production build
npm run build

# Start production server
npm run start
```

### Testing

**Android**:

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

**Web**:

```bash
# Lint check
npm run lint

# Type check
npx tsc --noEmit
```

### Device Testing

The Android application has been tested on:

- **Physical Device**: Real Android device with camera functionality
- **Android Emulator**: Android Studio emulator with camera simulation
- **Multiple API Levels**: Tested on Android API 24-34

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit a Pull Request

### Development Guidelines

- Follow Kotlin coding conventions for Android code
- Use TypeScript strict mode for web development
- Implement proper error handling and logging
- Write unit tests for new features
- Update documentation for API changes

## Acknowledgments

- **OpenCV**: Computer vision library for image processing
- **Android CameraX**: Modern camera API for Android
- **Next.js**: React framework for web development
- **Tailwind CSS**: Utility-first CSS framework

## Support

For questions, issues, or contributions:

- Create an issue on GitHub
- Check existing documentation
- Review the code comments for implementation details
