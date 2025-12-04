import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { CodeBracketIcon, DocumentMagnifyingGlassIcon, ChartBarIcon } from '@heroicons/react/24/solid';

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Quotes Frontend",
  description: "An example project showcasing functionality in the Nais platform.",
};

const grafanaDashboardUrl = process.env.NEXT_PUBLIC_GRAFANA_DASHBOARD_URL;
const githubUrl = process.env.NEXT_PUBLIC_GITHUB_URL;

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased flex flex-col min-h-screen`}
      >
        <div className="flex-grow">{children}</div>
        <footer className="bg-gray-800 text-white py-4 text-center sticky bottom-0">
          <div className="container mx-auto flex flex-row justify-center items-center space-x-4">
            <a href="/analytics" className="text-gray-400 hover:text-white flex items-center">
              <DocumentMagnifyingGlassIcon className="h-5 w-5 mr-1" />
              <ChartBarIcon className="h-5 w-5 mr-1" />
              Analytics
            </a>
            <a href={grafanaDashboardUrl} className="text-gray-400 hover:text-white flex items-center" target="_blank" rel="noopener noreferrer">
              <DocumentMagnifyingGlassIcon className="h-5 w-5 mr-1" /> Grafana Dashboard
            </a>
            <a href={githubUrl} className="text-gray-400 hover:text-white flex items-center" target="_blank" rel="noopener noreferrer">
              <CodeBracketIcon className="h-5 w-5 mr-1" /> View Source
            </a>
          </div>
        </footer>
      </body>
    </html>
  );
}
