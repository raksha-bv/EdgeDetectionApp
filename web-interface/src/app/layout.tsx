import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Edge Detection Web Interface",
  description: "Web interface for edge detection processing using OpenCV.js",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-gray-50">{children}</body>
    </html>
  );
}
