import { Router } from "express";
import { BotController } from "./bot.controller";

const router = Router();
const controller = new BotController();

router.post("/message", controller.handle);

export default router;
