interface ResultsDisplayProps {
  processedImage: string
  processingTime: number | null
}

export default function ResultsDisplay({ 
  processedImage, 
  processingTime 
}: ResultsDisplayProps) {
  const downloadImage = () => {
    if (processedImage) {
      const link = document.createElement('a')
      link.download = 'edge-detection-result.png'
      link.href = processedImage
      link.click()
    }
  }

  return (
    <div className="mt-6">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-medium text-gray-700">
          Edge Detection Result
        </h3>
        {processingTime && (
          <span className="text-sm text-gray-500">
            Processed in {processingTime.toFixed(1)}ms
          </span>
        )}
      </div>
      
      <div className="relative">
        <img
          src={processedImage}
          alt="Edge Detection Result"
          className="w-full max-h-96 object-contain rounded-lg border bg-white"
        />
        
        <button
          onClick={downloadImage}
          className="absolute top-2 right-2 bg-black bg-opacity-50 hover:bg-opacity-70 text-white p-2 rounded-lg transition-all duration-200"
          title="Download Result"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 10v6m0 0l-3-3m3 3l3-3M3 17V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2z"
            />
          </svg>
        </button>
      </div>

      <div className="mt-4 text-center">
        <button
          onClick={downloadImage}
          className="inline-flex items-center px-4 py-2 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-all duration-200 shadow-lg hover:shadow-xl"
        >
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 10v6m0 0l-3-3m3 3l3-3M3 17V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2z"
            />
          </svg>
          Download Result
        </button>
      </div>
    </div>
  )
}