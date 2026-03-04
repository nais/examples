import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockIsEnabled = vi.fn();
const mockGetVariant = vi.fn();
const mockOn = vi.fn();
const mockInitialize = vi.fn(() => ({
  isEnabled: mockIsEnabled,
  getVariant: mockGetVariant,
  on: mockOn,
}));

vi.mock('unleash-client', () => ({
  initialize: mockInitialize,
}));

vi.mock('@/utils/logger', () => ({
  default: { info: vi.fn(), warn: vi.fn(), error: vi.fn() },
}));

describe('unleash', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.resetModules();
    delete process.env.UNLEASH_SERVER_API_URL;
    delete process.env.UNLEASH_SERVER_API_TOKEN;
    delete process.env.UNLEASH_SERVER_API_ENVIRONMENT;
  });

  it('returns default when env vars are missing', async () => {
    const { isEnabled, FEATURE_FLAGS } = await import('@/utils/unleash');
    expect(isEnabled(FEATURE_FLAGS.QUOTES_SUBMIT)).toBe(true);
    expect(isEnabled(FEATURE_FLAGS.QUOTES_ERRORS)).toBe(false);
    expect(mockInitialize).not.toHaveBeenCalled();
  });

  it('initializes client and checks flags', async () => {
    process.env.UNLEASH_SERVER_API_URL = 'http://unleash';
    process.env.UNLEASH_SERVER_API_TOKEN = 'test-token';
    mockIsEnabled.mockReturnValue(true);

    const { isEnabled, FEATURE_FLAGS } = await import('@/utils/unleash');
    const result = isEnabled(FEATURE_FLAGS.QUOTES_SUBMIT);

    expect(mockInitialize).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'http://unleash/api/',
        appName: 'quotes-frontend',
      })
    );
    expect(result).toBe(true);
  });

  it('registers lifecycle listeners', async () => {
    process.env.UNLEASH_SERVER_API_URL = 'http://unleash';
    process.env.UNLEASH_SERVER_API_TOKEN = 'test-token';
    mockIsEnabled.mockReturnValue(true);

    const { isEnabled, FEATURE_FLAGS } = await import('@/utils/unleash');
    isEnabled(FEATURE_FLAGS.QUOTES_SUBMIT);

    const registeredEvents = mockOn.mock.calls.map((call: unknown[]) => call[0]);
    expect(registeredEvents).not.toContain('impression');
    expect(registeredEvents).toContain('ready');
    expect(registeredEvents).toContain('error');
  });

  it('getVariant returns disabled when no client', async () => {
    const { getVariant, FEATURE_FLAGS } = await import('@/utils/unleash');
    const variant = getVariant(FEATURE_FLAGS.QUOTES_SUBMIT);
    expect(variant).toEqual({ name: 'disabled', enabled: false });
  });

  it('getVariant delegates to client when initialized', async () => {
    process.env.UNLEASH_SERVER_API_URL = 'http://unleash';
    process.env.UNLEASH_SERVER_API_TOKEN = 'test-token';
    mockGetVariant.mockReturnValue({ name: 'variantA', enabled: true, payload: { type: 'string', value: 'hello' } });
    mockIsEnabled.mockReturnValue(true);

    const { getVariant, isEnabled, FEATURE_FLAGS } = await import('@/utils/unleash');
    isEnabled(FEATURE_FLAGS.QUOTES_SUBMIT); // trigger init
    const variant = getVariant(FEATURE_FLAGS.QUOTES_SUBMIT);

    expect(variant.name).toBe('variantA');
    expect(variant.enabled).toBe(true);
  });
});
