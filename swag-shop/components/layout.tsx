import React from "react";
import { System } from "@navikt/ds-icons";
import { Dropdown, Header } from "@navikt/ds-react-internal";
import Head from "next/head";

const Layout: React.FC<React.PropsWithChildren<{}>> = ({ children }) => {
  return (
    <div>
      <Head>
        <title>nais swag shop</title>
      </Head>
      <Header>
        <Header.Title as="h1">nais swag shop</Header.Title>
        <Dropdown>
          <Header.Button as={Dropdown.Toggle} className="ml-auto">
            <System style={{ fontSize: "1.5rem" }} title="Systemer og oppslagsverk" />
          </Header.Button>

          <Dropdown.Menu>
            <Dropdown.Menu.GroupedList>
              <Dropdown.Menu.GroupedList.Heading>
                Dokumentasjon
              </Dropdown.Menu.GroupedList.Heading>
              <Dropdown.Menu.GroupedList.Item as="a" href="https://aksel.nav.no/">
                Aksel.nav.no
              </Dropdown.Menu.GroupedList.Item>
            </Dropdown.Menu.GroupedList>
          </Dropdown.Menu>
        </Dropdown>
      </Header>
      <main className="layout">{children}</main>
    </div>
  );
};

export default Layout;