import express, { Express, Request, Response } from "express";
import dotenv from "dotenv";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { ExpressInstrumentation } from "@opentelemetry/instrumentation-express";
import { HttpInstrumentation } from "@opentelemetry/instrumentation-http";
import { exporter, counter, histogram } from "./metrics";

dotenv.config();

const app: Express = express();
const port = process.env.PORT || 8080;

// Register instrumentations
registerInstrumentations({
  instrumentations: [new HttpInstrumentation(), new ExpressInstrumentation()],
});

app.get("/", (req: Request, res: Response) => {
  counter.add(1, { route: "/" });
  const start = Date.now();
  res.send("Express + TypeScript Server");
  const end = Date.now();
  histogram.record(end - start, { route: "/" });
});

app.listen(port, () => {
  console.log(`⚡️[server]: Server is running at http://localhost:${port}`);
});
