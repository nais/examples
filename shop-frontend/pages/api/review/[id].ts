import { log } from "console";
import { NextApiRequest, NextApiResponse } from "next";
import getConfig from 'next/config';
import { trace } from '@opentelemetry/api'

const { serverRuntimeConfig: c } = getConfig();

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { id } = req.query;
  const allowedIds = ['id1', 'id2', 'id3']; // Example allowed IDs
  const sanitizedId = typeof id === 'string' ? id.replace(/[^a-zA-Z0-9_-]/g, '') : '';
  if (!allowedIds.includes(sanitizedId)) {
    res.status(400).json({ message: "Invalid ID" });
    return;
  }
  const reviewApiUrl = `${c.backendApiUrl}/api/products/${sanitizedId}/ratings`

  if (req.method === "POST") {
    // sanitize input
    const body = JSON.parse(req.body);
    const { name, review, rating } = body;

    return await trace
      .getTracer('review')
      .startActiveSpan('review.post', async (span) => {
        span.setAttribute('review.rating', rating);
        span.addEvent('review.post.start')
        try {
          const response = await fetch(reviewApiUrl, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              name,
              comment: review,
              stars: rating
            })
          });
          const json = await response.json();
          span.setAttribute('review.id', json.id);
          span.setAttribute('review.sentiment', json.sentiment);
          res.status(200).json(json);
        } catch (error) {
          span.recordException(error as Error);
          log(error);
          res.status(500).json({ message: "Internal server error" });
          return;
        } finally {
          span.addEvent('review.post.end');
          span.end();
        }
      });
  } else if (req.method === "GET") {
    try {
      if (!allowedIds.includes(sanitizedId)) {
        res.status(400).json({ message: "Invalid ID" });
        return;
      }
      const response = await fetch(`${c.backendApiUrl}/api/products/${sanitizedId}/ratings`);
      const reviews = await response.json();

      res.status(200).json({
        average: reviews.reduce((acc: number, review: any) => acc + review.stars, 0) / reviews.length,
        totalCount: reviews.length,
        counts: reviews.reduce((acc: any, review: any) => {
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
          { rating: 0, count: 0 },
        ]),
        featured: reviews.reverse().slice(0, 5).map((review: any) => ({
          id: review.id,
          rating: review.stars,
          content: `${review.comment} [sentiment = ${review.sentiment}]`,
          author: 'Emily Selman',
          avatarSrc: 'https://images.unsplash.com/photo-1502685104226-ee32379fefbe?ixlib=rb-=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=8&w=256&h=256&q=80'
        })),
      });
    } catch (error) {
      log(error);
      res.status(200).json({
        average: 0,
        totalCount: 0,
        counts: [
          { rating: 5, count: 0 },
          { rating: 4, count: 0 },
          { rating: 3, count: 0 },
          { rating: 2, count: 0 },
          { rating: 1, count: 0 },
        ],
        featured: [],
      });
      return;
    }
  } else {
    res.status(405).json({ message: "Method not allowed" });
  }
}
