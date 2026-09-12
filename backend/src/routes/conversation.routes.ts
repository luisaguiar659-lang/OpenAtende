import { Router } from "express";

const router = Router();

router.get("/", (_req, res) => {
  res.json({
    module: "conversations",
    conversations: []
  });
});

router.post("/", (req, res) => {
  const conversation = req.body;

  res.status(201).json({
    message: "Conversation created",
    conversation
  });
});

export default router;
