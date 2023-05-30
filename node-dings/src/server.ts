"user strict";

import { setup } from "./telemetry";
setup("example-express-server");

// Require in rest of modules
import express, { Express, Request, Response } from "express";
import { authMiddleware } from "./middleware";
import { getCatsController } from "./routes";
import { default as axios } from "axios";

// Setup express
const app: Express = express();
const PORT = 8080;

app.use(express.json());
app.get("/health", (req, res) => res.status(200).send("HEALTHY")); // endpoint that is called by framework/cluster
app.get("/run_test", async (req, res) => {
  // Calls another endpoint of the same API, somewhat mimicking an external API call
  const createdCat = await axios.post(
    `http://localhost:${PORT}/cats`,
    {
      name: "Tom",
      friends: ["Jerry"],
    },
    {
      headers: {
        Authorization: "secret_token",
      },
    }
  );

  return res.status(201).send(createdCat.data);
});
app.use("/cats", authMiddleware, getCatsController());

app.listen(PORT, () => {
  console.log(`Application listening on http://localhost:${PORT}`);
});
