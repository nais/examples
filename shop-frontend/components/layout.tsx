import React from "react";
import { System } from "@navikt/ds-icons";
import { Dropdown, Header } from "@navikt/ds-react-internal";
import Head from "next/head";

const Layout: React.FC<React.PropsWithChildren<{}>> = ({ children }) => {
  return (
    <div className="h-full bg-white">
      <Head>
        <title>nais web shop</title>
      </Head>
      <Header>
        <Header.Title as="h1">nais web shop</Header.Title>
        <Dropdown>
          <Header.Button as={Dropdown.Toggle} className="ml-auto">
            <System style={{ fontSize: "1.5rem" }} title="Systemer og oppslagsverk" />
          </Header.Button>

          <Dropdown.Menu>
            <Dropdown.Menu.GroupedList>
              <Dropdown.Menu.GroupedList.Heading>
                Dokumentasjon
              </Dropdown.Menu.GroupedList.Heading>
              <Dropdown.Menu.GroupedList.Item as="a" href="https://docs.nais.io/">
                docs.nais.no
              </Dropdown.Menu.GroupedList.Item>
              <Dropdown.Menu.GroupedList.Item as="a" href="https://aksel.nav.no/">
                aksel.nav.no
              </Dropdown.Menu.GroupedList.Item>
            </Dropdown.Menu.GroupedList>
          </Dropdown.Menu>
        </Dropdown>
      </Header>
      <main className="flex min-h-full flex-1 flex-col justify-center px-6 py-4 lg:px-8">
        <div className="mx-auto max-w-4xl">{children}</div>
      </main>
    </div>
  );
};

export default Layout;