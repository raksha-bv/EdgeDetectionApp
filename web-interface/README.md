# Edge Detection Web Interface

A modern web application built with Next.js and TypeScript for image edge detection processing.

## Features

- **Image Upload**: Drag & drop or click to upload images
- **Real-time Processing**: Client-side edge detection using custom algorithms
- **Interactive Controls**: Adjust threshold and kernel size parameters
- **Download Results**: Save processed images
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Clean, professional interface with Tailwind CSS

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Image Processing**: Custom JavaScript algorithms
- **Future Enhancement**: OpenCV.js integration ready

## Quick Start

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

1. **Navigate to web interface directory**:

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

4. **Open browser**:
   Visit `http://localhost:3000`

## Build for Production

```bash
npm run build
npm start
```

## Project Structure

```
web-interface/
├── src/
│   ├── app/
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   └── page.tsx
│   └── components/
│       ├── ImageUploader.tsx
│       ├── EdgeDetectionProcessor.tsx
│       └── ResultsDisplay.tsx
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── next.config.js
```

## Usage

1. **Upload Image**: Drag and drop an image or click to browse
2. **Adjust Parameters**:
   - Threshold 1: Lower edge detection threshold
   - Threshold 2: Upper edge detection threshold
   - Kernel Size: Edge detection kernel size
3. **Process**: Click "Apply Edge Detection"
4. **Download**: Save the processed result

## Features in Detail

### Image Upload

- Supports JPG, PNG, GIF formats
- Drag & drop interface
- File size validation
- Preview functionality

### Edge Detection Algorithm

- Custom Sobel edge detection implementation
- Adjustable threshold parameters
- Real-time processing
- Canvas-based image manipulation

### Results Display

- Side-by-side comparison
- Processing time display
- Download functionality
- High-quality output

## Future Enhancements

- [ ] OpenCV.js integration for advanced algorithms
- [ ] Multiple edge detection methods (Canny, Sobel, Prewitt)
- [ ] Batch processing
- [ ] Image filters and preprocessing
- [ ] Real-time camera input
- [ ] Cloud processing API integration

## Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run lint` - Run ESLint

### Adding New Features

1. Create components in `src/components/`
2. Add pages in `src/app/`
3. Use TypeScript for type safety
4. Follow existing code structure

## License

This project is part of the EdgeDetectionApp suite.
