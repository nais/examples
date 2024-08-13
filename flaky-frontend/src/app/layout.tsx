import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { initInstrumentation } from "@/lib/faro";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Create Next App",
  description: "Generated by create next app",
};

initInstrumentation()

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>{children}</body>
    </html>
  );
}
