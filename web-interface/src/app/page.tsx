'use client'

import { useState, useRef, useEffect } from 'react'
import ImageUploader from '../components/ImageUploader'
import EdgeDetectionProcessor from '../components/EdgeDetectionProcessor'
import ResultsDisplay from '../components/ResultsDisplay'

export default function Home() {
  const [uploadedImage, setUploadedImage] = useState<string | null>(null)
  const [processedImage, setProcessedImage] = useState<string | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const [processingTime, setProcessingTime] = useState<number | null>(null)

  const handleImageUpload = (imageDataUrl: string) => {
    setUploadedImage(imageDataUrl)
    setProcessedImage(null)
    setProcessingTime(null)
  }

  const handleProcessingComplete = (result: string, timeMs: number) => {
    setProcessedImage(result)
    setProcessingTime(timeMs)
    setIsProcessing(false)
  }

  const handleProcessingStart = () => {
    setIsProcessing(true)
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            Edge Detection Web Interface
          </h1>
          <p className="text-lg text-gray-600">
            Upload an image and apply edge detection using OpenCV.js
          </p>
        </div>

        {/* Main Content */}
        <div className="max-w-6xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Upload Section */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                Upload Image
              </h2>
              <ImageUploader onImageUpload={handleImageUpload} />
              
              {uploadedImage && (
                <div className="mt-4">
                  <h3 className="text-lg font-medium text-gray-700 mb-2">
                    Original Image
                  </h3>
                  <img
                    src={uploadedImage}
                    alt="Original"
                    className="w-full max-h-96 object-contain rounded-lg border"
                  />
                </div>
              )}
            </div>

            {/* Processing Section */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                Edge Detection
              </h2>
              
              {uploadedImage && (
                <EdgeDetectionProcessor
                  imageData={uploadedImage}
                  onProcessingStart={handleProcessingStart}
                  onProcessingComplete={handleProcessingComplete}
                  disabled={isProcessing}
                />
              )}
              
              {!uploadedImage && (
                <div className="text-center py-12 text-gray-500">
                  Upload an image to enable edge detection processing
                </div>
              )}
              
              {processedImage && (
                <ResultsDisplay
                  processedImage={processedImage}
                  processingTime={processingTime}
                />
              )}
            </div>
          </div>

          {/* Processing Status */}
          {isProcessing && (
            <div className="mt-8 bg-blue-50 border border-blue-200 rounded-xl p-6">
              <div className="flex items-center justify-center">
                <div className="processing-spinner w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full mr-3"></div>
                <span className="text-blue-800 font-medium">
                  Processing image with OpenCV.js...
                </span>
              </div>
            </div>
          )}

          {/* Feature Info */}
          <div className="mt-12 bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">
              Features
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="text-center p-4">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-3">
                  <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <h3 className="font-semibold text-gray-800">Image Upload</h3>
                <p className="text-sm text-gray-600">Drag & drop or click to upload images</p>
              </div>
              
              <div className="text-center p-4">
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-3">
                  <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <h3 className="font-semibold text-gray-800">Fast Processing</h3>
                <p className="text-sm text-gray-600">Real-time edge detection using OpenCV.js</p>
              </div>
              
              <div className="text-center p-4">
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-3">
                  <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4" />
                  </svg>
                </div>
                <h3 className="font-semibold text-gray-800">Customizable</h3>
                <p className="text-sm text-gray-600">Adjust threshold and parameters</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  )
}