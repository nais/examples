import { log } from "console";
import { trace } from '@opentelemetry/api'
import { NextApiRequest, NextApiResponse } from "next";
import getConfig from 'next/config';

const { serverRuntimeConfig: c } = getConfig();

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { id } = req.query;

  const response = await trace
    .getTracer('shop-frontend')
    .startActiveSpan('fetchGithubStars', async (span) => {
      try {
        return await fetch(`${c.backendApiUrl}/api/products/${id}/ratings`);
      } finally {
        span.end()
      }
    })
  const json = await response.json();

  if (response.status == 200) {
    res.status(200).json({
      average: json.reduce((acc: number, review: any) => acc + review.stars, 0) / json.length,
      totalCount: json.length,
      counts: json.reduce((acc: any, review: any) => {
        const { stars } = review;
        const index = 5 - stars;
        acc[index].count++;
        return acc;
      }, [
        { rating: 5, count: 0 },
        { rating: 4, count: 0 },
        { rating: 3, count: 0 },
        { rating: 2, count: 0 },
        { rating: 1, count: 0 },
      ]),
      featured:  json.slice(0, 5).map((review: any) => ({
        id: review.id,
        rating: review.stars,
        content: `${review.comment} [sentiment = ${review.sentiment}]`,
        author: 'Emily Selman',
        avatarSrc: 'https://images.unsplash.com/photo-1502685104226-ee32379fefbe?ixlib=rb-=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=8&w=256&h=256&q=80'
      })),
    });
  } else {
    log(`Error fetching reviews for product ${id}: ${JSON.stringify(json)}`);
    res.status(200).json([]);
  }
}
