import { computeBmi, formatCurrency, formatDate } from '../src/utils/format';

describe('formatCurrency', () => {
  it('formats a number with two decimal places and a dollar sign', () => {
    expect(formatCurrency(49.5)).toBe('$49.50');
  });

  it('treats null/undefined as zero', () => {
    expect(formatCurrency(null)).toBe('$0.00');
    expect(formatCurrency(undefined)).toBe('$0.00');
  });

  it('handles string numeric input', () => {
    expect(formatCurrency('120')).toBe('$120.00');
  });
});

describe('formatDate', () => {
  it('formats an ISO date string into a readable date', () => {
    const formatted = formatDate('2026-06-20');
    expect(formatted).toContain('Jun');
    expect(formatted).toContain('20');
    expect(formatted).toContain('2026');
  });

  it('returns a placeholder for missing input', () => {
    expect(formatDate(null)).toBe('-');
    expect(formatDate(undefined)).toBe('-');
  });

  it('returns a placeholder for invalid date strings', () => {
    expect(formatDate('not-a-date')).toBe('-');
  });
});

describe('computeBmi', () => {
  it('computes BMI from height in cm and weight in kg', () => {
    expect(computeBmi(180, 81)).toBe(25);
  });

  it('rounds to two decimal places', () => {
    expect(computeBmi(165, 60)).toBe(22.04);
  });

  it('returns null when height is missing', () => {
    expect(computeBmi(null, 70)).toBeNull();
  });

  it('returns null when weight is missing', () => {
    expect(computeBmi(170, null)).toBeNull();
  });

  it('returns null when height is zero or negative', () => {
    expect(computeBmi(0, 70)).toBeNull();
    expect(computeBmi(-5, 70)).toBeNull();
  });
});
