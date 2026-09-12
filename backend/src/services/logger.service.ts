export function logger(message: string) {
  console.log(`[OpenAtende] ${new Date().toISOString()} - ${message}`);
}
