import { Review, ReviewPost } from '../types/review';

export async function getReviews(id: string) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 1000);

  let data: Review = {
    href: '',
    average: 0,
    totalCount: 0,
    counts: [],
    featured: []
  }

  try {
    const response = await fetch(`/api/review/${id}`);
    data = await response.json();
  } catch (error) {
    console.error(error);
  } finally {
    clearTimeout(timeoutId);
    return data;
  }
}

export async function postReview(id: string, review: ReviewPost) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 1000);

  try {
    const response = await fetch(`/api/review/${id}`, {
      method: 'POST',
      body: JSON.stringify(review)
    });
    return await response.json();
  } catch (error) {
    console.error(error);
  } finally {
    clearTimeout(timeoutId);
  }
}