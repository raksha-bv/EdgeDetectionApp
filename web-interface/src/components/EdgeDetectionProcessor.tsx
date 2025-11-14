"use client";

import { useState } from "react";

interface EdgeDetectionProcessorProps {
  imageData: string;
  onProcessingStart: () => void;
  onProcessingComplete: (result: string, timeMs: number) => void;
  disabled?: boolean;
}

export default function EdgeDetectionProcessor({
  imageData,
  onProcessingStart,
  onProcessingComplete,
  disabled = false,
}: EdgeDetectionProcessorProps) {
  const [threshold1, setThreshold1] = useState(50);
  const [threshold2, setThreshold2] = useState(150);
  const [kernelSize, setKernelSize] = useState(3);

  const processImage = async () => {
    onProcessingStart();
    const startTime = performance.now();

    try {
      // Create image element from data URL
      const img = new Image();
      img.crossOrigin = "anonymous";

      await new Promise((resolve, reject) => {
        img.onload = resolve;
        img.onerror = reject;
        img.src = imageData;
      });

      // Create canvas for processing
      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d")!;

      canvas.width = img.width;
      canvas.height = img.height;
      ctx.drawImage(img, 0, 0);

      // Get image data
      const imageDataObj = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const data = imageDataObj.data;

      // Simple edge detection algorithm (Sobel-like)
      const output = new Uint8ClampedArray(data.length);
      const width = canvas.width;
      const height = canvas.height;

      // Convert to grayscale and apply edge detection
      for (let y = 1; y < height - 1; y++) {
        for (let x = 1; x < width - 1; x++) {
          const idx = (y * width + x) * 4;

          // Get surrounding pixels for edge detection
          const gx =
            -1 * getGray(data, x - 1, y - 1, width) +
            1 * getGray(data, x + 1, y - 1, width) +
            -2 * getGray(data, x - 1, y, width) +
            2 * getGray(data, x + 1, y, width) +
            -1 * getGray(data, x - 1, y + 1, width) +
            1 * getGray(data, x + 1, y + 1, width);

          const gy =
            -1 * getGray(data, x - 1, y - 1, width) +
            -2 * getGray(data, x, y - 1, width) +
            -1 * getGray(data, x + 1, y - 1, width) +
            1 * getGray(data, x - 1, y + 1, width) +
            2 * getGray(data, x, y + 1, width) +
            1 * getGray(data, x + 1, y + 1, width);

          const magnitude = Math.sqrt(gx * gx + gy * gy);
          const edge = magnitude > threshold1 ? 255 : 0;

          output[idx] = edge; // R
          output[idx + 1] = edge; // G
          output[idx + 2] = edge; // B
          output[idx + 3] = 255; // A
        }
      }

      // Create result canvas
      const resultCanvas = document.createElement("canvas");
      resultCanvas.width = width;
      resultCanvas.height = height;
      const resultCtx = resultCanvas.getContext("2d")!;

      const resultImageData = new ImageData(output, width, height);
      resultCtx.putImageData(resultImageData, 0, 0);

      const endTime = performance.now();
      const processingTime = endTime - startTime;

      onProcessingComplete(resultCanvas.toDataURL(), processingTime);
    } catch (error) {
      console.error("Edge detection failed:", error);
      onProcessingComplete("", 0);
    }
  };

  const getGray = (
    data: Uint8ClampedArray,
    x: number,
    y: number,
    width: number
  ): number => {
    const idx = (y * width + x) * 4;
    return (data[idx] + data[idx + 1] + data[idx + 2]) / 3;
  };

  return (
    <div className="space-y-4">
      {/* Controls */}
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Threshold 1: {threshold1}
          </label>
          <input
            type="range"
            min="1"
            max="255"
            value={threshold1}
            onChange={(e) => setThreshold1(Number(e.target.value))}
            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            disabled={disabled}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Threshold 2: {threshold2}
          </label>
          <input
            type="range"
            min="1"
            max="255"
            value={threshold2}
            onChange={(e) => setThreshold2(Number(e.target.value))}
            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            disabled={disabled}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Kernel Size: {kernelSize}
          </label>
          <input
            type="range"
            min="3"
            max="7"
            step="2"
            value={kernelSize}
            onChange={(e) => setKernelSize(Number(e.target.value))}
            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            disabled={disabled}
          />
        </div>
      </div>

      {/* Process Button */}
      <button
        onClick={processImage}
        disabled={disabled}
        className={`w-full py-3 px-4 rounded-lg font-medium transition-all duration-200 ${
          disabled
            ? "bg-gray-300 text-gray-500 cursor-not-allowed"
            : "bg-blue-600 hover:bg-blue-700 text-white shadow-lg hover:shadow-xl transform hover:scale-[1.02]"
        }`}
      >
        {disabled ? "Processing..." : "Apply Edge Detection"}
      </button>
    </div>
  );
}
