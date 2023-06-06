import { Heading } from "@navikt/ds-react";
import { GuidePanel } from "@navikt/ds-react";
import type { NextPage } from "next";
import Image from "next/image";
import { useState, useEffect } from "react";
import Layout from "../components/layout";
import { Swag } from "../types";

const Home: NextPage = () => {
  const [swags, setSwags] = useState<Swag[]>([]);

  useEffect(() => {
    const fetchSwags = async () => {
      const response = await fetch("/api/swag");
      const data = await response.json();
      setSwags(data);
    };

    fetchSwags();
  }, []);

  return (
    <Layout>
      <Heading spacing level="1" size="large">
        nais swag shop
      </Heading>
      <GuidePanel>
        nais swag shop er en demoapplikasjon laget for å teste ut ny frontend-funksjonalitet i NAIS og skal være et eksempel for hvordan komme i gang med frontendutvikling i NAV.
      </GuidePanel>
      <h2 className="mt-6 text-2xl font-bold tracking-tight text-gray-900">Anbefalt for deg</h2>
      <div className="mt-6 grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:gap-x-8">
        {swags.map((swag) => (
          <div key={swag.id} className="group relative">
            <div className="aspect-h-1 aspect-w-1 w-full overflow-hidden rounded-md bg-gray-200 lg:aspect-none group-hover:opacity-75 lg:h-60">
              <Image
                src={swag.images[0].src}
                alt={swag.images[0].alt}
                width="1200"
                height="1200"
                className="h-full w-full object-cover object-center lg:h-full lg:w-full"
              />
            </div>
            <div className="mt-4 flex justify-between">
              <div>
                <h3 className="text-sm text-gray-700">
                  <a href={swag.href}>
                    <span aria-hidden="true" className="absolute inset-0" />
                    {swag.name}
                  </a>
                </h3>
                <h4 className="text-sm text-gray-700 sr-only">Colors</h4>
                <div className="mt-4 flex justify-between">
                  <ul className="flex items-center space-x-3 flex-row">
                    {swag.colors.map((color) => (
                      <li key={color.name} className={`h-4 w-4 rounded-full flex items-center justify-center ring-1 ${color.selectedClass}`}>
                        <span className="sr-only">{color.name}</span>
                        <span aria-hidden="true" className={`h-full w-full rounded-full ${color.class}`} />
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
              <p className="text-sm font-medium text-gray-900">{swag.price}</p>
            </div>
          </div>
        ))}
      </div>
    </Layout>
  );
};

export default Home;
