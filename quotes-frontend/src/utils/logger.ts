import pino, { Logger } from "pino";

export const logger: Logger = pino({
  base: undefined, // remove default fields
  messageKey: "message",
  errorKey: "message",
  formatters: {    // display level as a string
    level: (label) => {
      return {
        level: label
      }
    }
  }
});

export default logger;
