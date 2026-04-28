import { describe, expect, test } from 'vitest';
import { getPreferenceCategoryLabel, preferenceCategories } from './preference';

describe('preference categories', () => {
  test('uses backend enum values for visible category options', () => {
    expect(preferenceCategories.map((item) => item.value)).toEqual([
      'GIFT',
      'FAVORITE_FOOD',
      'HOBBY',
      'LIFE_BOUNDARY',
      'CUSTOM'
    ]);
  });

  test('renders backend enum values as Chinese labels', () => {
    expect(getPreferenceCategoryLabel('FAVORITE_FOOD')).toBe('饮食');
    expect(getPreferenceCategoryLabel('UNKNOWN')).toBe('UNKNOWN');
  });
});
