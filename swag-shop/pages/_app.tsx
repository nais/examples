import "@navikt/ds-css";
import "@navikt/ds-css-internal";
import "../styles/global.css";

import type { AppProps } from "next/app";

function MyApp({ Component, pageProps }: AppProps) {
  return <Component {...pageProps} />;
}

export default MyApp;
