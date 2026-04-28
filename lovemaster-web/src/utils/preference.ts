export const preferenceCategories = [
  { label: '礼物', value: 'GIFT' },
  { label: '饮食', value: 'FAVORITE_FOOD' },
  { label: '约会', value: 'HOBBY' },
  { label: '雷区', value: 'LIFE_BOUNDARY' },
  { label: '自定义', value: 'CUSTOM' }
] as const;

export type PreferenceCategory = (typeof preferenceCategories)[number]['value'];

export function getPreferenceCategoryLabel(value: string) {
  return preferenceCategories.find((item) => item.value === value)?.label ?? value;
}
