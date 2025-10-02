import { Quote } from '@/types/quote';

/**
 * Extend the globalThis object to include a shared quotes array.
 */
declare global {
  var quotes: Quote[] | undefined;
}

export const quotes = globalThis.quotes || [
  {
    id: "1a22378f-dbae-4a2a-a001-84a0538ca01a",
    text: "Deploy with confidence—let Nais handle the platform, you focus on the code.",
    author: "Nais Team"
  },
  {
    id: "2a22378f-dbae-4a2a-a001-84a0538ca01b",
    text: "Kubernetes is complex, but Nais makes it simple for developers.",
    author: "Platform Engineer"
  },
  {
    id: "3a22378f-dbae-4a2a-a001-84a0538ca01c",
    text: "With Nais, continuous delivery is not just a dream—it's the default.",
    author: "DevOps Enthusiast"
  },
  {
    id: "4a22378f-dbae-4a2a-a001-84a0538ca01d",
    text: "Secure by default—Nais integrates best practices into every deployment.",
    author: "Security Advocate"
  },
  {
    id: "5a22378f-dbae-4a2a-a001-84a0538ca01e",
    text: "From Helm charts to GitHub Actions, Nais brings it all together.",
    author: "Cloud Native Developer"
  }
];

if (!globalThis.quotes) {
  globalThis.quotes = quotes;
}
