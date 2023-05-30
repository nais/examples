import express from "express";

export const getCatsController = () => {
  const router = express.Router();
  const resources: any[] = [];
  router.get("/", (req, res) => res.send(resources));
  router.post("/", (req, res) => {
    resources.push(req.body);
    return res.status(201).send(req.body);
  });
  return router;
};
