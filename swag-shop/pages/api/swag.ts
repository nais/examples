import { NextApiRequest, NextApiResponse } from "next";
import { Swag } from "../../types";

export const swags: Swag[] = [
  {
    id: 1,
    name: 'Basic Tee',
    description: 'Comfort Colors introduces its garment-dyed t-shirt; a fully customizable tee made 100% with ring-spun cotton. The soft-washed, garment-dyed fabric brings extra coziness to your wardrobe while the relaxed fit makes it an excellent daily choice. The double-needle stitching throughout the tee makes it highly durable while the lack of side-seams helps the shirt retain its tubular shape.',
    highlights: [
      '100% ring-spun cotton',
      'Medium fabric (6.1 oz/yd² (206.8 g/m²))',
      'Relaxed fit',
      'Sewn-in twill label',
    ],
    details: 'Details TBA',
    href: '/swag/1',
    price: '350,-',
    images: [
      {
        src: '/images/swag/product-1-1.png',
        alt: '',
      },
      {
        src: '/images/swag/product-1-2.png',
        alt: '',
      },
      {
        src: '/images/swag/product-1-3.png',
        alt: '',
      },
      {
        src: '/images/swag/product-1-4.png',
        alt: '',
      },
    ],
    colors: [
      { name: 'White', class: 'bg-white', selectedClass: 'ring-gray-400' },
      { name: 'Blue', class: 'bg-blue-900', selectedClass: 'ring-blue-950' },
      { name: 'Black', class: 'bg-gray-900', selectedClass: 'ring-gray-900' },
    ],
    sizes: [
      { name: 'XXS', inStock: false },
      { name: 'XS', inStock: true },
      { name: 'S', inStock: true },
      { name: 'M', inStock: true },
      { name: 'L', inStock: true },
      { name: 'XL', inStock: true },
    ],
    breadcrumbs: [
      { id: 1, name: 'Swag', href: '/' },
    ],
  },
  {
    id: 2,
    name: 'Basic Hoodie',
    description: 'This unisex heavy blend hooded sweatshirt is relaxation itself. Made with a thick blend of cotton and polyester, it feels plush, soft and warm, a perfect choice for any cold day. In the front, the spacious kangaroo pocket adds daily practicality while the hood\'s drawstring is the same color as the base sweater for extra style points.',
    highlights: [
      '50% cotton, 50% polyester (fiber content may vary for different colors)',
      'Medium-heavy fabric (8.0 oz/yd² (271 g/m²))',
      'Classic fit',
      'Tear-away label',
      'Runs true to size',
    ],
    details: 'Details TBA',
    href: '/swag/2',
    price: '650,-',
    images: [
      {
        src: '/images/swag/product-2-1.png',
        alt: '',
      },
      {
        src: '/images/swag/product-2-2.png',
        alt: '',
      },
      {
        src: '/images/swag/product-2-3.png',
        alt: '',
      },
      {
        src: '/images/swag/product-2-4.png',
        alt: '',
      },
    ],
    colors: [
      { name: 'White', class: 'bg-white', selectedClass: 'ring-gray-400' },
      { name: 'Red', class: 'bg-red-900', selectedClass: 'ring-red-950' },
      { name: 'Black', class: 'bg-gray-900', selectedClass: 'ring-gray-900' },
    ],
    sizes: [
      { name: 'XXS', inStock: false },
      { name: 'XS', inStock: true },
      { name: 'S', inStock: true },
      { name: 'M', inStock: true },
      { name: 'L', inStock: true },
      { name: 'XL', inStock: true },
    ],
    breadcrumbs: [
      { id: 1, name: 'Swag', href: '/' },
    ],
  },
  {
    id: 3,
    name: 'Premium Tee',
    description: 'The unisex heavy cotton tee is the basic staple of any wardrobe. It is the foundation upon which casual fashion grows. All it needs is a personalized design to elevate things to profitability. The specially spun fibers provide a smooth surface for premium printing vividity and sharpness. No side seams mean there are no itchy interruptions under the arms. The shoulders have tape for improved durability.',
    highlights: [
      '100% cotton (fiber content may vary for different colors)',
      'Medium fabric (5.3 oz/yd² (180 g/m²))',
      'Classic fit',
      'Tear away label',
      'Runs true to size',
    ],
    details: 'Details TBA',
    href: '/swag/3',
    price: '450,-',
    images: [
      {
        src: '/images/swag/product-3-1.png',
        alt: '',
      },
      {
        src: '/images/swag/product-3-2.png',
        alt: '',
      },
      {
        src: '/images/swag/product-3-3.png',
        alt: '',
      },
      {
        src: '/images/swag/product-3-4.png',
        alt: '',
      },
    ],
    colors: [
      { name: 'White', class: 'bg-white', selectedClass: 'ring-gray-400' },
      { name: 'Gray', class: 'bg-gray-200', selectedClass: 'ring-gray-400' },
      { name: 'Black', class: 'bg-gray-900', selectedClass: 'ring-gray-900' },
    ],
    sizes: [
      { name: 'XXS', inStock: false },
      { name: 'XS', inStock: true },
      { name: 'S', inStock: true },
      { name: 'M', inStock: true },
      { name: 'L', inStock: true },
      { name: 'XL', inStock: true },
    ],
    breadcrumbs: [
      { id: 1, name: 'Swag', href: '/' },
    ],
  },
  {
    id: 4,
    name: 'Basic Socks',
    description: 'These high-quality socks with sublimated print provide optimum comfort with style wherever one might go - a subtle accent to complement an office look or an eye-catching statement for an extravagant outfit.',
    highlights: [
      '95% Polyester 5% Spandex',
      '3 different sizes',
      'Ribbed tube',
      'Cushioned bottoms',
    ],
    details: 'Details TBA',
    href: '/swag/4',
    price: '150,-',
    images: [
      {
        src: '/images/swag/product-4-1.png',
        alt: '',
      },
      {
        src: '/images/swag/product-4-2.png',
        alt: '',
      },
      {
        src: '/images/swag/product-4-3.png',
        alt: '',
      },
      {
        src: '/images/swag/product-4-4.png',
        alt: '',
      },
    ],
    colors: [
      { name: 'White', class: 'bg-white', selectedClass: 'ring-gray-400' },
      { name: 'Gray', class: 'bg-gray-200', selectedClass: 'ring-gray-400' },
      { name: 'Black', class: 'bg-gray-900', selectedClass: 'ring-gray-900' },
    ],
    sizes: [
      { name: 'S', inStock: true },
      { name: 'M', inStock: true },
      { name: 'L', inStock: true },
    ],
    breadcrumbs: [
      { id: 1, name: 'Swag', href: '/' },
    ],
  },
  {
    id: 5,
    name: 'Basic Scarf',
    description: 'Create the perfect accessory that ties together any outfit with these long, lightweight scarves. Each scarf is transparent and printed in great detail making it perfect for intricate patterns and exotic designs. Add your take and create a wardrobe-favorite.',
    highlights: [
      '100% polyester',
      '27" × 73" (68.5cm × 185.5cm)',
      'One sided print',
      'Seamless construction',
    ],
    details: 'Details TBA',
    href: '/swag/5',
    price: '250,-',
    images: [
      {
        src: '/images/swag/product-5-1.png',
        alt: '',
      },
      {
        src: '/images/swag/product-5-2.png',
        alt: '',
      },
      {
        src: '/images/swag/product-5-3.png',
        alt: '',
      },
      {
        src: '/images/swag/product-5-4.png',
        alt: '',
      },
    ],
    colors: [
      { name: 'White', class: 'bg-white', selectedClass: 'ring-gray-400' },
      { name: 'Gray', class: 'bg-gray-200', selectedClass: 'ring-gray-400' },
      { name: 'Black', class: 'bg-gray-900', selectedClass: 'ring-gray-900' },
    ],
    sizes: [
      { name: 'M', inStock: true },
    ],
    breadcrumbs: [
      { id: 1, name: 'Swag', href: '/' },
    ],
  },
]

export default function handler(req: NextApiRequest, res: NextApiResponse) {
  if (req.method === "GET") {
    res.status(200).json(swags);
  } else {
    res.status(405).json({ message: "Method not allowed" });
  }
}