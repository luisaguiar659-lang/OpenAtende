import { Router } from "express";
import { getDashboardStats } from "./dashboard.service";

const router = Router();

router.get("/stats", (_req, res) => {
  res.json(getDashboardStats());
});

export default router;
