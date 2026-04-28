import { describe, expect, test } from 'vitest';
import {
  buildDateIdeaQuery,
  getBudgetLabel,
  getDurationLabel,
  getSceneLabel
} from './dateIdea';

describe('date idea helpers', () => {
  test('renders backend enum values as friendly Chinese labels', () => {
    expect(getBudgetLabel('FREE')).toBe('免费');
    expect(getBudgetLabel('UNDER_50')).toBe('50元内');
    expect(getDurationLabel('TWO_HOURS')).toBe('约2小时');
    expect(getSceneLabel('RAINY_DAY')).toBe('雨天');
    expect(getSceneLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  test('builds query params from selected filters and tag input', () => {
    expect(
      buildDateIdeaQuery({
        scene: 'OUTDOOR',
        budgetLevel: 'FREE',
        durationLevel: 'ONE_HOUR',
        tagsInput: '散步, 拍照 散步'
      })
    ).toEqual({
      scene: 'OUTDOOR',
      budgetLevel: 'FREE',
      durationLevel: 'ONE_HOUR',
      tags: '散步,拍照'
    });
  });

  test('omits all filters when every option is set to all', () => {
    expect(
      buildDateIdeaQuery({
        scene: 'ALL',
        budgetLevel: 'ALL',
        durationLevel: 'ALL',
        tagsInput: '   '
      })
    ).toBeUndefined();
  });
});
