import { BotService } from './bot.service';

const botService = new BotService();

export function processBotMessage(message: string) {
  return botService.processMessage(message);
}
