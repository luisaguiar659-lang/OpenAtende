import { Router } from "express";
import { authMiddleware } from "../../middlewares/auth.middleware";
import { createUser, listUsers } from "./user.controller";

const router = Router();

router.get("/profile", authMiddleware, (req, res) => {
  res.json({
    message: "Perfil autenticado",
    user: (req as any).user
  });
});

router.get("/", authMiddleware, listUsers);

router.post("/", authMiddleware, createUser);

export default router;
