export interface Review {
  href: string | '';
  average: number | 0;
  totalCount: number | 0;
  counts: [{
    rating: number;
    count: number;
  }] | [],
  featured: [{
    id: number;
    rating: number;
    content: string;
    author: string;
    avatarSrc: string;
  }] | [],
}

export interface ReviewPost {
  name: string;
  review: string;
  rating: number;
}