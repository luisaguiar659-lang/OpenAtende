import { Request, Response } from "express";
import { createUser, listUsers } from "./user.service";

export async function createUserController(req: Request, res: Response) {
  try {
    const user = await createUser(req.body);
    return res.status(201).json(user);
  } catch (error) {
    return res.status(400).json({ error: "Não foi possível criar usuário" });
  }
}

export async function listUsersController(req: Request, res: Response) {
  const users = await listUsers();
  return res.json(users);
}
