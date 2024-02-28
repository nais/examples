import "@navikt/ds-css";
import "@navikt/ds-css-internal";
import "../styles/global.css";

import type { AppProps } from "next/app";

import { initInstrumentation } from '../lib/faro'

initInstrumentation()

function SwagShop({ Component, pageProps }: AppProps) {
  return <Component {...pageProps} />;
}

export default SwagShop;
