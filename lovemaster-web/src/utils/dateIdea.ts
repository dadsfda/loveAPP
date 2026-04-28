import type { DateIdeaQuery } from '../api/dateIdea';
import { parseTagsInput } from './tags';

export const sceneOptions = [
  { value: 'ALL', label: '全部' },
  { value: 'INDOOR', label: '室内' },
  { value: 'OUTDOOR', label: '室外' },
  { value: 'RAINY_DAY', label: '雨天' },
  { value: 'HOME', label: '居家' },
  { value: 'REMOTE', label: '异地' }
] as const;

export const budgetOptions = [
  { value: 'ALL', label: '全部预算' },
  { value: 'FREE', label: '免费' },
  { value: 'UNDER_50', label: '50元内' },
  { value: 'UNDER_100', label: '100元内' }
] as const;

export const durationOptions = [
  { value: 'ALL', label: '全部时长' },
  { value: 'ONE_HOUR', label: '1小时' },
  { value: 'TWO_HOURS', label: '2小时' },
  { value: 'HALF_DAY', label: '半天' }
] as const;

export type DateIdeaSceneFilter = (typeof sceneOptions)[number]['value'];
export type DateIdeaBudgetFilter = (typeof budgetOptions)[number]['value'];
export type DateIdeaDurationFilter = (typeof durationOptions)[number]['value'];

interface DateIdeaFilterState {
  scene: DateIdeaSceneFilter;
  budgetLevel: DateIdeaBudgetFilter;
  durationLevel: DateIdeaDurationFilter;
  tagsInput: string;
}

const budgetLabels: Record<string, string> = {
  FREE: '免费',
  UNDER_50: '50元内',
  UNDER_100: '100元内'
};

const durationLabels: Record<string, string> = {
  ONE_HOUR: '约1小时',
  TWO_HOURS: '约2小时',
  HALF_DAY: '半天'
};

const sceneLabels: Record<string, string> = {
  INDOOR: '室内',
  OUTDOOR: '室外',
  RAINY_DAY: '雨天',
  HOME: '居家',
  REMOTE: '异地'
};

export function getBudgetLabel(value: string) {
  return budgetLabels[value] ?? value;
}

export function getDurationLabel(value: string) {
  return durationLabels[value] ?? value;
}

export function getSceneLabel(value: string) {
  return sceneLabels[value] ?? value;
}

export function buildDateIdeaQuery(filters: DateIdeaFilterState): DateIdeaQuery | undefined {
  const query: DateIdeaQuery = {};
  const tags = parseTagsInput(filters.tagsInput);

  if (filters.scene !== 'ALL') {
    query.scene = filters.scene;
  }
  if (filters.budgetLevel !== 'ALL') {
    query.budgetLevel = filters.budgetLevel;
  }
  if (filters.durationLevel !== 'ALL') {
    query.durationLevel = filters.durationLevel;
  }
  if (tags.length > 0) {
    query.tags = tags.join(',');
  }

  return Object.keys(query).length > 0 ? query : undefined;
}
