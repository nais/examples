export interface Swag {
  id: number;
  name: string;
  description: string;
  highlights: string[];
  details: string;
  href: string;
  images: {
    src: string;
    alt: string;
  }[];
  colors: {
    name: string;
    class: string;
    selectedClass: string;
  }[];
  sizes: {
    name: string;
    inStock: boolean;
  }[];
  breadcrumbs: {
    id: number;
    name: string;
    href: string;
  }[];
  price: string;
  reviews: {
    average: number;
    total: number;
  };
}