import { Router } from "express";

const router = Router();

router.get("/", (_req, res) => {
  res.json({ tickets: [] });
});

router.post("/", (req, res) => {
  res.status(201).json({
    message: "Ticket criado",
    data: req.body
  });
});

export default router;
