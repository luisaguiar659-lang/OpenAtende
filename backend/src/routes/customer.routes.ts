import { Router } from "express";

const router = Router();

router.get("/", (_req, res) => {
  res.json({
    module: "customers",
    customers: []
  });
});

router.post("/", (req, res) => {
  const customer = req.body;

  res.status(201).json({
    message: "Customer created",
    customer
  });
});

export default router;
