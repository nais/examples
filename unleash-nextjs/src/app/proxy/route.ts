import type { NextRequest } from "next/server";

const server = process.env.NEXT_PUBLIC_UNLEASH_SERVER_API_URL
const token = process.env.UNLEASH_SERVER_API_TOKEN

export async function GET(request: NextRequest) {
  console.log(token)
  const response = await fetch(`${server}/proxy`, {
    headers: {
      "content-type": "application/json",
      authorization: `${token}`,
    },
  });

  const text = await response.text();
  return new Response(text, {
    headers: { "content-type": "application/json" },
  });
}

export async function POST(request: NextRequest) {
  const response = await fetch(`${server}/proxy`, {
    headers: {
      "content-type": "application/json",
      authorization: `${token}`,
    },
    method: "POST",
    body: JSON.stringify({
      path: request.nextUrl.pathname,
      method: request.method,
      query: request.nextUrl.searchParams,
      body: request.body,
      headers: request.headers,
    }),
  });

  const text = await response.text();
  return new Response(text, {
    headers: { "content-type": "application/json" },
  });
};
