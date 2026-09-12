export class AssignmentService {
  assignAgent(agents: { id: string; online: boolean }[]) {
    return agents.find((agent) => agent.online) ?? null;
  }
}
