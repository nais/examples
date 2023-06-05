import { NextApiRequest, NextApiResponse } from "next";
import { swags } from "../swag";

export default function handler(req: NextApiRequest, res: NextApiResponse) {
  const { id } = req.query;
  const swag = swags.find((swag) => swag.id === Number(id));

  if (req.method === "GET" && swag) {
    res.status(200).json(swag);
  } else if (req.method === "GET" && !swag) {
    res.status(404).json({ message: "Swag not found" });
  } else {
    res.status(405).json({ message: "Method not allowed" });
  }
}