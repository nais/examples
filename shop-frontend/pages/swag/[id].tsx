import { StarIcon } from '@heroicons/react/20/solid'
import { RadioGroup } from '@headlessui/react'
import type { NextPage } from "next";
import Image from 'next/image';
import { useState, useEffect, Fragment } from "react";
import { useRouter } from "next/router";
import Layout from "../../components/layout";
import { Swag as SwagType } from "../../types/swag";
import { Review, ReviewPost } from "../../types/review";
import { Dialog, Transition } from '@headlessui/react'
import { getReviews, postReview } from '../../lib/reviews';

function classNames(...classes: string[]) {
  return classes.filter(Boolean).join(' ')
}

const Swag: NextPage = () => {
  const [swag, setSwag] = useState<SwagType | null>(null);
  const [reviews, setReviews] = useState<Review | null>(null);
  const [selectedColor, setSelectedColor] = useState(swag?.colors[0] ?? { name: '', class: '', selectedClass: '' });
  const [selectedSize, setSelectedSize] = useState(swag?.sizes[2] ?? { name: '', inStock: false });
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [myHooverRating, setMyHooverRating] = useState(0);
  const [myRating, setMyRating] = useState(0);

  const router = useRouter();
  const { id } = router.query;

  const submitReview = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);
    const name = formData.get('name');
    const review = formData.get('review');

    const request = await postReview(id as string, {
      name,
      review,
      rating: myRating
    } as ReviewPost)

    setReviews(await getReviews(id as string));
    setMyHooverRating(0);
    setMyRating(0);
    setIsModalOpen(false);
  }

  useEffect(() => {
    const fetchSwag = async (validatedId: string) => {
      const response = await fetch(`/api/swag/${validatedId}`);
      const data = await response.json();
      setSwag(data);
    };

    const fetchReviews = async (validatedId: string) => {
      setReviews(await getReviews(validatedId));
    }

    const validateId = (id: any): string | null => {
      const idPattern = /^[a-zA-Z0-9_-]+$/;
      return idPattern.test(id) ? id : null;
    };

    if (id) {
      const validatedId = validateId(id);
      if (validatedId) {
        fetchSwag(validatedId);
        fetchReviews(validatedId);
      } else {
        console.error("Invalid ID parameter");
      }
    }
  }, [id]);

  if (!swag || !reviews) {
    return <div>Loading...</div>;
  }

  return (
    <Layout>
      <div className="bg-white">
        <div className="pt-6">
          <nav aria-label="Breadcrumb">
            <ol role="list" className="mx-auto flex max-w-2xl items-center space-x-2 px-4 sm:px-6 lg:max-w-7xl lg:px-8">
              {swag.breadcrumbs.map((breadcrumb) => (
                <li key={breadcrumb.id}>
                  <div className="flex items-center">
                    <a href={breadcrumb.href} className="mr-2 text-sm font-medium text-gray-900">
                      {breadcrumb.name}
                    </a>
                    <svg
                      width={16}
                      height={20}
                      viewBox="0 0 16 20"
                      fill="currentColor"
                      aria-hidden="true"
                      className="h-5 w-4 text-gray-300"
                    >
                      <path d="M5.697 4.34L8.98 16.532h1.327L7.025 4.341H5.697z" />
                    </svg>
                  </div>
                </li>
              ))}
              <li className="text-sm">
                <a href={swag.href} aria-current="page" className="font-medium text-gray-500 hover:text-gray-600">
                  {swag.name}
                </a>
              </li>
            </ol>
          </nav>

          {/* Image gallery */}
          <div className="mx-auto mt-6 max-w-2xl sm:px-6 lg:grid lg:max-w-7xl lg:grid-cols-3 lg:gap-x-8 lg:px-8">
            <div className="aspect-h-4 aspect-w-3 bg-gray-200 hidden overflow-hidden rounded-lg lg:block">
              <Image
                src={swag.images[0].src}
                alt={swag.images[0].alt}
                width={500}
                height={500}
                className="h-full w-full object-cover object-center"
              />
            </div>
            <div className="hidden lg:grid lg:grid-cols-1 lg:gap-y-8">
              <div className="aspect-h-2 aspect-w-3 bg-gray-200 overflow-hidden rounded-lg">
                <Image
                  src={swag.images[1].src}
                  alt={swag.images[1].alt}
                  width={500}
                  height={500}
                  className="h-full w-full object-cover object-center"
                />
              </div>
              <div className="aspect-h-2 aspect-w-3 bg-gray-200 overflow-hidden rounded-lg">
                <Image
                  src={swag.images[2].src}
                  alt={swag.images[2].alt}
                  width={500}
                  height={500}
                  className="h-full w-full object-cover object-center"
                />
              </div>
            </div>
            <div className="aspect-h-5 aspect-w-4 bg-gray-200 lg:aspect-h-4 lg:aspect-w-3 sm:overflow-hidden sm:rounded-lg">
              <Image
                src={swag.images[3].src}
                alt={swag.images[3].alt}
                width={500}
                height={500}
                className="h-full w-full object-cover object-center"
              />
            </div>
          </div>

          {/* Swag info */}
          <div className="mx-auto max-w-2xl px-4 pb-10 pt-10 sm:px-6 lg:grid lg:max-w-7xl lg:grid-cols-3 lg:grid-rows-[auto,auto,1fr] lg:gap-x-8 lg:px-8">
            <div className="lg:col-span-2 lg:border-r lg:border-gray-200 lg:pr-8">
              <h1 className="text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl">{swag.name}</h1>
            </div>

            {/* Options */}
            <div className="mt-4 lg:row-span-3 lg:mt-0">
              <h2 className="sr-only">Swag information</h2>
              <p className="text-3xl tracking-tight text-gray-900">{swag.price}</p>

              {/* Reviews */}
              <div className="mt-6">
                <h3 className="sr-only">Reviews</h3>
                <div className="flex items-center">
                  <div className="flex items-center">
                    {[0, 1, 2, 3, 4].map((rating) => (
                      <StarIcon
                        key={rating}
                        className={classNames(
                          reviews.average > rating ? 'text-gray-900' : 'text-gray-200',
                          'h-5 w-5 flex-shrink-0'
                        )}
                        aria-hidden="true"
                      />
                    ))}
                  </div>
                  <p className="sr-only">{reviews.average} out of 5 stars</p>
                  <a href={reviews.href} className="ml-3 text-sm font-medium text-indigo-600 hover:text-indigo-500">
                    {reviews.totalCount} reviews
                  </a>
                </div>
              </div>

              <form className="mt-10">
                {/* Colors */}
                <div>
                  <h3 className="text-sm font-medium text-gray-900">Color</h3>

                  <RadioGroup value={selectedColor} onChange={setSelectedColor} className="mt-4">
                    <RadioGroup.Label className="sr-only">Choose a color</RadioGroup.Label>
                    <div className="flex items-center space-x-3">
                      {swag.colors.map((color) => (
                        <RadioGroup.Option
                          key={color.name}
                          value={color}
                          className={({ active, checked }) =>
                            classNames(
                              color.selectedClass,
                              active && checked ? 'ring ring-offset-1' : '',
                              !active && checked ? 'ring-2' : '',
                              'relative -m-0.5 flex cursor-pointer items-center justify-center rounded-full p-0.5 focus:outline-none'
                            )
                          }
                        >
                          <RadioGroup.Label as="span" className="sr-only">
                            {color.name}
                          </RadioGroup.Label>
                          <span
                            aria-hidden="true"
                            className={classNames(
                              color.class,
                              'h-8 w-8 rounded-full border border-black border-opacity-10'
                            )}
                          />
                        </RadioGroup.Option>
                      ))}
                    </div>
                  </RadioGroup>
                </div>

                {/* Sizes */}
                <div className="mt-10">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-medium text-gray-900">Size</h3>
                    <a href="#" className="text-sm font-medium text-indigo-600 hover:text-indigo-500">
                      Size guide
                    </a>
                  </div>

                  <RadioGroup value={selectedSize} onChange={setSelectedSize} className="mt-4">
                    <RadioGroup.Label className="sr-only">Choose a size</RadioGroup.Label>
                    <div className="grid grid-cols-4 gap-4 sm:grid-cols-8 lg:grid-cols-3">
                      {swag.sizes.map((size) => (
                        <RadioGroup.Option
                          key={size.name}
                          value={size}
                          disabled={!size.inStock}
                          className={({ active }) =>
                            classNames(
                              size.inStock
                                ? 'cursor-pointer bg-white text-gray-900 shadow-sm'
                                : 'cursor-not-allowed bg-gray-50 text-gray-200',
                              active ? 'ring-2 ring-indigo-500' : '',
                              'group relative flex items-center justify-center rounded-md border py-3 px-4 text-sm font-medium uppercase hover:bg-gray-50 focus:outline-none sm:flex-1 sm:py-6'
                            )
                          }
                        >
                          {({ active, checked }) => (
                            <>
                              <RadioGroup.Label as="span">{size.name}</RadioGroup.Label>
                              {size.inStock ? (
                                <span
                                  className={classNames(
                                    active ? 'border' : 'border-2',
                                    checked ? 'border-indigo-500' : 'border-transparent',
                                    'pointer-events-none absolute -inset-px rounded-md'
                                  )}
                                  aria-hidden="true"
                                />
                              ) : (
                                <span
                                  aria-hidden="true"
                                  className="pointer-events-none absolute -inset-px rounded-md border-2 border-gray-200"
                                >
                                  <svg
                                    className="absolute inset-0 h-full w-full stroke-2 text-gray-200"
                                    viewBox="0 0 100 100"
                                    preserveAspectRatio="none"
                                    stroke="currentColor"
                                  >
                                    <line x1={0} y1={100} x2={100} y2={0} vectorEffect="non-scaling-stroke" />
                                  </svg>
                                </span>
                              )}
                            </>
                          )}
                        </RadioGroup.Option>
                      ))}
                    </div>
                  </RadioGroup>
                </div>

                <button
                  type="submit"
                  className="mt-10 flex w-full items-center justify-center rounded-md border border-transparent bg-indigo-600 px-8 py-3 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                >
                  Add to bag
                </button>
              </form>
            </div>

            <div className="py-10 lg:col-span-2 lg:col-start-1 lg:border-r lg:border-gray-200 lg:pb-16 lg:pr-8 lg:pt-6">
              {/* Description and details */}
              <div>
                <h3 className="sr-only">Description</h3>

                <div className="space-y-6">
                  <p className="text-base text-gray-900">{swag.description}</p>
                </div>
              </div>

              <div className="mt-10">
                <h3 className="text-sm font-medium text-gray-900">Highlights</h3>

                <div className="mt-4">
                  <ul role="list" className="list-disc space-y-2 pl-4 text-sm">
                    {swag.highlights.map((highlight) => (
                      <li key={highlight} className="text-gray-400">
                        <span className="text-gray-600">{highlight}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              <div className="mt-10">
                <h2 className="text-sm font-medium text-gray-900">Details</h2>

                <div className="mt-4 space-y-6">
                  <p className="text-sm text-gray-600">{swag.details}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Reviews */}
        <div className="mx-auto max-w-2xl pb-16 sm:px-6 lg:grid lg:max-w-7xl lg:grid-cols-12 lg:gap-x-8 lg:px-8">
          <div className="lg:col-span-4">
            <h2 className="text-2xl font-bold tracking-tight text-gray-900">Customer Reviews</h2>

            <div className="mt-3 flex items-center">
              <div>
                <div className="flex items-center">
                  {[0, 1, 2, 3, 4].map((rating) => (
                    <StarIcon
                      key={rating}
                      className={classNames(
                        reviews.average > rating ? 'text-yellow-400' : 'text-gray-300',
                        'h-5 w-5 flex-shrink-0'
                      )}
                      aria-hidden="true"
                    />
                  ))}
                </div>
                <p className="sr-only">{reviews.average} out of 5 stars</p>
              </div>
              <p className="ml-2 text-sm text-gray-900">Based on {reviews.totalCount} reviews</p>
            </div>

            <div className="mt-6">
              <h3 className="sr-only">Review data</h3>

              <dl className="space-y-3">
                {reviews.counts.map((count) => (
                  <div key={count.rating} className="flex items-center text-sm">
                    <dt className="flex flex-1 items-center">
                      <p className="w-3 font-medium text-gray-900">
                        {count.rating}
                        <span className="sr-only"> star reviews</span>
                      </p>
                      <div aria-hidden="true" className="ml-1 flex flex-1 items-center">
                        <StarIcon
                          className={classNames(
                            count.count > 0 ? 'text-yellow-400' : 'text-gray-300',
                            'h-5 w-5 flex-shrink-0'
                          )}
                          aria-hidden="true"
                        />

                        <div className="relative ml-3 flex-1">
                          <div className="h-3 rounded-full border border-gray-200 bg-gray-100" />
                          {count.count > 0 ? (
                            <div
                              className="absolute inset-y-0 rounded-full border border-yellow-400 bg-yellow-400"
                              style={{ width: `calc(${count.count} / ${reviews.totalCount} * 100%)` }}
                            />
                          ) : null}
                        </div>
                      </div>
                    </dt>
                    <dd className="ml-3 w-10 text-right text-sm tabular-nums text-gray-900">
                      {Math.round((count.count / reviews.totalCount) * 100) || 0}%
                    </dd>
                  </div>
                ))}
              </dl>
            </div>

            <div className="mt-10">
              <h3 className="text-lg font-medium text-gray-900">Share your thoughts</h3>
              <p className="mt-1 text-sm text-gray-600">
                If you’ve used this product, share your thoughts with other customers
              </p>

              <button
                className="mt-6 inline-flex w-full items-center justify-center rounded-md border border-gray-300 bg-white px-8 py-2 text-sm font-medium text-gray-900 hover:bg-gray-50 sm:w-auto lg:w-full"
                onClick={() => setIsModalOpen(true)}
              >
                Write a review
              </button>

              <Transition.Root show={isModalOpen} as={Fragment}>
                <Dialog as="div" className="relative z-10" onClose={setIsModalOpen}>
                  <Transition.Child
                    as={Fragment}
                    enter="ease-out duration-300"
                    enterFrom="opacity-0"
                    enterTo="opacity-100"
                    leave="ease-in duration-200"
                    leaveFrom="opacity-100"
                    leaveTo="opacity-0"
                  >
                    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
                  </Transition.Child>

                  <div className="fixed inset-0 z-10 w-screen overflow-y-auto">
                    <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
                      <Transition.Child
                        as={Fragment}
                        enter="ease-out duration-300"
                        enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                        enterTo="opacity-100 translate-y-0 sm:scale-100"
                        leave="ease-in duration-200"
                        leaveFrom="opacity-100 translate-y-0 sm:scale-100"
                        leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                      >
                        <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6">
                          <div>
                            <div className="mt-3 sm:mt-5">
                              <Dialog.Title as="h3" className="text-base font-semibold leading-6 text-gray-900">
                                Write a review
                              </Dialog.Title>
                              <div className="mt-2">
                                <form onSubmit={submitReview}>
                                  <div className="space-y-12">
                                    <div className="border-b border-gray-900/10 pb-12">
                                      <p className="mt-1 text-sm leading-6 text-gray-600">
                                        This information will be displayed publicly so be careful what you share.
                                      </p>

                                      <div className="mt-10 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
                                        <div className="col-span-full">
                                          <label htmlFor="name" className="block text-sm font-medium leading-6 text-gray-900">
                                            Name
                                          </label>
                                          <div className="mt-2">
                                            <div className="flex rounded-md shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600 sm:max-w-md">
                                              <input
                                                type="text"
                                                name="name"
                                                id="name"
                                                autoComplete="name"
                                                className="block flex-1 border-0 bg-transparent py-1.5 pl-1 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm sm:leading-6"
                                                placeholder="Your name"
                                              />
                                            </div>
                                          </div>
                                        </div>

                                        <div className="col-span-full">
                                          <label htmlFor="review" className="block text-sm font-medium leading-6 text-gray-900">
                                            Review
                                          </label>
                                          <div className="mt-2">
                                            <textarea
                                              id="review"
                                              name="review"
                                              rows={3}
                                              className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                              defaultValue={''}
                                            />
                                          </div>
                                          <p className="mt-3 text-sm leading-6 text-gray-600">Write a few sentences about the product.</p>
                                        </div>

                                        <div className="col-span-full">
                                          <label htmlFor="rating" className="block text-sm font-medium leading-6 text-gray-900">
                                            Your rating
                                          </label>
                                          <div className="mt-2 flex items-center">
                                            {[0, 1, 2, 3, 4].map((rating) => (
                                              <StarIcon
                                                key={rating}
                                                className={classNames(
                                                  myHooverRating >= rating + 1 ? 'text-yellow-400' : 'text-gray-300',
                                                  'h-5 w-5 flex-shrink-0 transition-colors duration-200 ease-in-out transform hover:text-yellow-400'
                                                )}
                                                aria-hidden="true"
                                                onMouseEnter={() => setMyHooverRating(rating + 1)}
                                                onMouseLeave={() => setMyHooverRating(myRating)}
                                                onClick={() => setMyRating(rating + 1)}
                                              />
                                            ))}
                                            <span className="ml-3 text-sm text-gray-600">Select a rating</span>
                                          </div>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                  <div className="mt-6 flex items-center justify-end gap-x-6">
                                    <button
                                      type="button"
                                      className="text-sm font-semibold leading-6 text-gray-900"
                                      onClick={() => setIsModalOpen(false)}
                                    >
                                      Cancel
                                    </button>
                                    <button
                                      type="submit"
                                      className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                                    >
                                      Publish
                                    </button>
                                  </div>
                                </form>
                              </div>
                            </div>
                          </div>
                        </Dialog.Panel>
                      </Transition.Child>
                    </div>
                  </div>
                </Dialog>
              </Transition.Root>
            </div>
          </div>

          <div className="mt-16 lg:col-span-7 lg:col-start-6 lg:mt-0">
            <h3 className="sr-only">Recent reviews</h3>

            <div className="flow-root">
              <div className="-my-12 divide-y divide-gray-200">
                {reviews.featured.length === 0 && (
                  <div className="py-12">
                    <div className="items-center">
                      <h4 className="text-2xl font-bold tracking-tight text-gray-900">No reviews yet.</h4>
                      <p className="text-md font-medium text-gray-900">No reviews yet.</p>
                    </div>
                  </div>
                )}

                {reviews.featured.map((review) => (
                  <div key={review.id} className="py-12">
                    <div className="flex items-center">
                      <img src={review.avatarSrc} alt={`${review.author}.`} className="h-12 w-12 rounded-full" />
                      <div className="ml-4">
                        <h4 className="text-sm font-bold text-gray-900">{review.author}</h4>
                        <div className="mt-1 flex items-center">
                          {[0, 1, 2, 3, 4].map((rating) => (
                            <StarIcon
                              key={rating}
                              className={classNames(
                                review.rating > rating ? 'text-yellow-400' : 'text-gray-300',
                                'h-5 w-5 flex-shrink-0'
                              )}
                              aria-hidden="true"
                            />
                          ))}
                        </div>
                        <p className="sr-only">{review.rating} out of 5 stars</p>
                      </div>
                    </div>

                    <div
                      className="mt-4 space-y-6 text-base italic text-gray-600"
                      dangerouslySetInnerHTML={{ __html: review.content }}
                    />
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}

export default Swag;