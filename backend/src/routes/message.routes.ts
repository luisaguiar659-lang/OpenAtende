import { Router } from "express";

const router = Router();

router.get("/", (_req, res) => {
  res.json({
    module: "messages",
    messages: []
  });
});

router.post("/", (req, res) => {
  const message = req.body;

  res.status(201).json({
    message: "Message created",
    data: message
  });
});

export default router;
