# Edge Detection App

A cross-platform app for real-time edge detection on Android and web, using OpenCV, C++, and modern web tech.

## Live Demo

- [Web App](https://edge-detection-app.vercel.app/)
- [Android Demo Video](https://youtube.com/shorts/Y6cHxYe5d8w)

## Screenshots

- Android: ![Android](media/screenshots/android/working_screenshot.webp)
- Web: ![Web](media/screenshots/web/working_web.png)

## Features

**Android:**

- Real-time camera edge detection
- Native C++ (OpenCV) backend
- OpenGL rendering
- Toggle edge detection
- Performance optimized (3fps on 30fps camera)
- Camera permission handling
- **New:** Invert and Edge Enhancer filters

**Web:**

- Next.js + TypeScript
- Image upload & processing
- Real-time edge detection in browser
- Adjustable controls (threshold, kernel)
- Download results
- Responsive design
- **New:** Invert and Edge Enhancer filters

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

- Canny, Sobel, Gaussian blur, threshold adjustment
- **New:** Invert and Edge Enhancer filters
- Optimized: frame rate control, memory management, background/native processing

## Supported Platforms

- Android 7.0+ (API 24+), ARM64-v8a/ARMv7, camera required
- Web: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+, desktop/mobile, JPEG/PNG/WebP

## Development

- Android: `./gradlew clean`, `./gradlew :app:assembleDebug`, `./gradlew :app:assembleRelease`
- Web: `npm run dev`, `npm run build`, `npm run start`
- Android tests: `./gradlew test` (unit), `./gradlew connectedAndroidTest` (instrumented)
- Web tests: `npm run lint`, `npx tsc --noEmit`
- Tested on real devices, emulators, API 24-34

## Contributing

- Fork, branch, commit, push, and open a PR
- Follow Kotlin/TypeScript conventions, add tests, update docs

## Acknowledgments

- OpenCV, CameraX, Next.js, Tailwind CSS

## Support

- For questions or issues: open a GitHub issue or check docs/comments
