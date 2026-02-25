import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import QuoteDisplay from "@/components/QuoteDisplay";

afterEach(cleanup);

describe("QuoteDisplay", () => {
  const defaultQuote = { id: "1", text: "Test quote text", author: "Test Author" };

  const defaultProps = () => ({
    quote: defaultQuote,
    handleThumbsUp: vi.fn(),
    handleThumbsDown: vi.fn(),
    fetchQuote: vi.fn(),
  });

  it("renders quote text and author", () => {
    render(<QuoteDisplay {...defaultProps()} />);

    expect(screen.getByText("Test quote text")).toBeInTheDocument();
    expect(screen.getByText("- Test Author")).toBeInTheDocument();
  });

  it("renders 'Unknown' when author is empty", () => {
    render(
      <QuoteDisplay
        {...defaultProps()}
        quote={{ id: "1", text: "No author", author: "" }}
      />
    );

    expect(screen.getByText("- Unknown")).toBeInTheDocument();
  });

  it("calls handleThumbsUp on first button click", async () => {
    const user = userEvent.setup();
    const props = defaultProps();
    render(<QuoteDisplay {...props} />);

    const buttons = screen.getAllByRole("button");
    await user.click(buttons[0]);

    expect(props.handleThumbsUp).toHaveBeenCalledOnce();
  });

  it("calls handleThumbsDown on second button click", async () => {
    const user = userEvent.setup();
    const props = defaultProps();
    render(<QuoteDisplay {...props} />);

    const buttons = screen.getAllByRole("button");
    await user.click(buttons[1]);

    expect(props.handleThumbsDown).toHaveBeenCalledOnce();
  });

  it("calls fetchQuote on refresh button click", async () => {
    const user = userEvent.setup();
    const props = defaultProps();
    render(<QuoteDisplay {...props} />);

    const buttons = screen.getAllByRole("button");
    await user.click(buttons[2]);

    expect(props.fetchQuote).toHaveBeenCalledOnce();
  });

  it("disables refresh button when disableRandomQuote is true", () => {
    render(<QuoteDisplay {...defaultProps()} disableRandomQuote={true} />);

    const buttons = screen.getAllByRole("button");
    const refreshButton = buttons.find((b) => (b as HTMLButtonElement).disabled);
    expect(refreshButton).toBeDefined();
  });

  it("renders submit link pointing to /submit-quote", () => {
    render(<QuoteDisplay {...defaultProps()} />);

    const links = screen.getAllByRole("link");
    const submitLink = links.find((l) => l.textContent?.includes("Submit a New Quote"));
    expect(submitLink).toBeDefined();
    expect(submitLink).toHaveAttribute("href", "/submit-quote");
  });

  it("applies error background when quote has no id", () => {
    const { container } = render(
      <QuoteDisplay
        {...defaultProps()}
        quote={{ id: "", text: "Error", author: "N/A" }}
      />
    );

    const wrapper = container.firstElementChild;
    expect(wrapper?.className).toContain("bg-red-200");
  });
});
