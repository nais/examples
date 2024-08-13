import getConfig from 'next/config';
import type { NextRequest } from 'next/server';

const { serverRuntimeConfig: c } = getConfig();

export async function GET(request: NextRequest) {
  return await fetch(c.flakyServiceUrl, { cache: 'no-store' })
    .then((response) => {
      return response;
    }).catch((error) => {
      console.error(error?.cause?.message);
      return new Response(
        JSON.stringify({
          error: error?.cause?.message || "Internal Server Error",
        }),
        {
          status: 500,
          statusText: "Internal Server Error",
        },
      );
    });
}