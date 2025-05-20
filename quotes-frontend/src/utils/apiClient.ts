import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface Quote {
  id?: string;
  text: string;
  author: string;
}

export const getQuotes = async (): Promise<Quote[]> => {
  const response = await apiClient.get<Quote[]>('/api/quotes');
  return response.data;
};

export const getQuoteById = async (id: string): Promise<Quote> => {
  const response = await apiClient.get<Quote>(`/api/quotes/${id}`);
  return response.data;
};

export const createQuote = async (quote: Omit<Quote, 'id'>): Promise<Quote> => {
  const response = await apiClient.post<Quote>('/api/quotes', quote);
  return response.data;
};
