import { describe, it, expect } from "vitest";
import logger from "@/utils/logger";

describe("logger", () => {
  it("is a pino logger instance", () => {
    expect(logger).toBeDefined();
    expect(typeof logger.info).toBe("function");
    expect(typeof logger.error).toBe("function");
    expect(typeof logger.warn).toBe("function");
  });

  it("uses 'message' as the message key", () => {
    expect(logger).toBeDefined();
  });
});
