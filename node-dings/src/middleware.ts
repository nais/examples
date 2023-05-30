import { RequestHandler } from "express";
import opentelemetry from "@opentelemetry/api";
import { count } from "console";

const meter = opentelemetry.metrics.getMeter("app.middleware");
const authFailuresCounter = meter.createCounter("auth.failures", {
  description: "Authorization failures",
});

export const authMiddleware: RequestHandler = (req, res, next) => {
  const { authorization } = req.headers;
  if (authorization && authorization.includes("secret_token")) {
    next();
  } else {
    authFailuresCounter.add(1);
    res.sendStatus(401);
  }
};
