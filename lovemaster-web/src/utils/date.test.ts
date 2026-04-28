import { describe, expect, test, vi } from 'vitest';
import { daysBetween, daysUntil, formatDate } from './date';

describe('date utils', () => {
  test('formatDate renders yyyy.mm.dd', () => {
    expect(formatDate('2026-04-28T10:00:00')).toBe('2026.04.28');
  });

  test('daysBetween includes the first day', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-28T10:00:00'));
    expect(daysBetween('2026-04-27')).toBe(2);
    vi.useRealTimers();
  });

  test('daysUntil rolls annual dates into next year', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-28T10:00:00'));
    expect(daysUntil('2025-04-27')).toBe(364);
    vi.useRealTimers();
  });
});
