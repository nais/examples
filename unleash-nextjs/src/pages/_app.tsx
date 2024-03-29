import type { AppProps } from "next/app";
import { Page } from "@vercel/examples-ui";
import "@vercel/examples-ui/globals.css";

export default function SwagShop({ Component, pageProps }: AppProps) {
  return (
    <Page>
      <Component {...pageProps} />
    </Page>
  );
}
