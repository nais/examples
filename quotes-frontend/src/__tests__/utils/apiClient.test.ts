import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "axios";
import { getQuotes, getQuoteById, createQuote, apiClient } from "@/utils/apiClient";

vi.mock("axios", () => {
  const mockInstance = {
    get: vi.fn(),
    post: vi.fn(),
    defaults: { headers: { common: {} } },
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
  };
  return {
    default: { create: vi.fn(() => mockInstance) },
  };
});

describe("apiClient", () => {
  const mockGet = apiClient.get as ReturnType<typeof vi.fn>;
  const mockPost = apiClient.post as ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("getQuotes", () => {
    it("fetches all quotes from /api/quotes", async () => {
      const quotes = [
        { id: "1", text: "Hello", author: "World" },
        { id: "2", text: "Foo", author: "Bar" },
      ];
      mockGet.mockResolvedValue({ data: quotes });

      const result = await getQuotes();

      expect(mockGet).toHaveBeenCalledWith("/api/quotes");
      expect(result).toEqual(quotes);
    });

    it("propagates errors", async () => {
      mockGet.mockRejectedValue(new Error("Network error"));

      await expect(getQuotes()).rejects.toThrow("Network error");
    });
  });

  describe("getQuoteById", () => {
    it("fetches a specific quote by id", async () => {
      const quote = { id: "42", text: "Test quote", author: "Tester" };
      mockGet.mockResolvedValue({ data: quote });

      const result = await getQuoteById("42");

      expect(mockGet).toHaveBeenCalledWith("/api/quotes/42");
      expect(result).toEqual(quote);
    });

    it("propagates errors for missing quotes", async () => {
      mockGet.mockRejectedValue(new Error("Not found"));

      await expect(getQuoteById("999")).rejects.toThrow("Not found");
    });
  });

  describe("createQuote", () => {
    it("posts a new quote", async () => {
      const newQuote = { text: "New quote", author: "Author" };
      const created = { id: "3", ...newQuote };
      mockPost.mockResolvedValue({ data: created });

      const result = await createQuote(newQuote);

      expect(mockPost).toHaveBeenCalledWith("/api/quotes", newQuote);
      expect(result).toEqual(created);
    });

    it("propagates errors on creation failure", async () => {
      mockPost.mockRejectedValue(new Error("Server error"));

      await expect(
        createQuote({ text: "Test", author: "Author" })
      ).rejects.toThrow("Server error");
    });
  });
});
