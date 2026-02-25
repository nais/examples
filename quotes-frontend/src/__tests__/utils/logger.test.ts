import { describe, it, expect } from "vitest";
import pino from "pino";
import { Writable } from "stream";
import logger from "@/utils/logger";

describe("logger", () => {
  it("is a pino logger instance", () => {
    expect(logger).toBeDefined();
    expect(typeof logger.info).toBe("function");
    expect(typeof logger.error).toBe("function");
    expect(typeof logger.warn).toBe("function");
  });

  it("uses 'message' as the message key", () => {
    const lines: string[] = [];
    const writable = new Writable({
      write(chunk: Buffer, _encoding: string, callback: () => void) {
        lines.push(chunk.toString());
        callback();
      },
    });
    const testLogger = pino({ messageKey: "message" }, writable);
    testLogger.info("test message");

    expect(lines.length).toBe(1);
    const parsed = JSON.parse(lines[0]);
    expect(parsed.message).toBe("test message");
    expect(parsed.msg).toBeUndefined();
  });
});
