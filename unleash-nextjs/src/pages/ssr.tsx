import {
  flagsClient,
  evaluateFlags,
  getDefinitions,
  type IVariant,
} from "@unleash/nextjs";
import type { GetServerSideProps, NextPage } from "next";
import { UNLEASH_COOKIE_NAME } from "../utils";

type Data = {
  isEnabled: boolean;
  variant: IVariant;
  percent: number;
};

const ServerSideRenderedPage: NextPage<Data> = ({
  isEnabled,
  variant,
  percent,
}) => (
  <>
    Flag status: {isEnabled ? "ENABLED" : "DISABLED"}
    <br />
    Variant: {variant.name}
    <br />
    How many feature toggles are enabled: {percent}%
  </>
);

export const getServerSideProps: GetServerSideProps<Data> = async (ctx) => {
  const sessionId =
    ctx.req.cookies[UNLEASH_COOKIE_NAME] ||
    `${Math.floor(Math.random() * 1_000_000_000)}`;
  ctx.res.setHeader(
    "set-cookie",
    `${UNLEASH_COOKIE_NAME}=${sessionId}; path=/;`
  );

  const definitions = await getDefinitions();
  const { toggles } = evaluateFlags(definitions, {
    sessionId,
  });
  const flags = flagsClient(toggles);

  return {
    props: {
      isEnabled: flags.isEnabled("nextjs-example"),
      variant: flags.getVariant("nextjs-example"),
      percent: Math.round((toggles.length / definitions.features.length) * 100),
    },
  };
};

export default ServerSideRenderedPage;
