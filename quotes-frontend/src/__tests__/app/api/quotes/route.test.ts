import { describe, it, expect, vi, beforeEach } from "vitest";

vi.mock("@/utils/apiClient", () => ({
  getQuotes: vi.fn(),
  getQuoteById: vi.fn(),
  createQuote: vi.fn(),
}));

vi.mock("@/utils/logger", () => ({
  default: {
    info: vi.fn(),
    warn: vi.fn(),
    error: vi.fn(),
  },
}));

import { getQuotes, getQuoteById, createQuote } from "@/utils/apiClient";
import { GET, POST } from "@/app/api/quotes/route";

const mockGetQuotes = getQuotes as ReturnType<typeof vi.fn>;
const mockGetQuoteById = getQuoteById as ReturnType<typeof vi.fn>;
const mockCreateQuote = createQuote as ReturnType<typeof vi.fn>;

describe("GET /api/quotes", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns a random quote", async () => {
    const quotes = [
      { id: "1", text: "Quote 1", author: "A1" },
      { id: "2", text: "Quote 2", author: "A2" },
    ];
    mockGetQuotes.mockResolvedValue(quotes);
    mockGetQuoteById.mockResolvedValue(quotes[0]);

    const response = await GET();
    const data = await response.json();

    expect(response.status).toBe(200);
    expect(data).toHaveProperty("id");
    expect(data).toHaveProperty("text");
  });

  it("returns 404 when no quotes available", async () => {
    mockGetQuotes.mockResolvedValue([]);

    const response = await GET();
    const data = await response.json();

    expect(response.status).toBe(404);
    expect(data.error).toBe("No quotes available.");
  });

  it("returns 500 on fetch error", async () => {
    mockGetQuotes.mockRejectedValue(new Error("Backend down"));

    const response = await GET();
    const data = await response.json();

    expect(response.status).toBe(500);
    expect(data.error).toBe("Failed to fetch random quote.");
  });
});

describe("POST /api/quotes", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("creates a new quote", async () => {
    const newQuote = { id: "3", text: "New", author: "Author" };
    mockCreateQuote.mockResolvedValue(newQuote);

    const request = new Request("http://localhost/api/quotes", {
      method: "POST",
      body: JSON.stringify({ text: "New", author: "Author" }),
    });

    const response = await POST(request);
    const data = await response.json();

    expect(response.status).toBe(200);
    expect(data.quote).toEqual(newQuote);
  });

  it("returns 400 when text is missing", async () => {
    const request = new Request("http://localhost/api/quotes", {
      method: "POST",
      body: JSON.stringify({ author: "Author" }),
    });

    const response = await POST(request);
    const data = await response.json();

    expect(response.status).toBe(400);
    expect(data.error).toBe("Quote text is required.");
  });

  it("returns 500 on creation error", async () => {
    mockCreateQuote.mockRejectedValue(new Error("DB error"));

    const request = new Request("http://localhost/api/quotes", {
      method: "POST",
      body: JSON.stringify({ text: "Test", author: "A" }),
    });

    const response = await POST(request);
    const data = await response.json();

    expect(response.status).toBe(500);
    expect(data.error).toBe("Failed to add quote.");
  });
});
