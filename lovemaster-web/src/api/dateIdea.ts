import { apiClient } from './client';
import type { DateIdeaResponse } from '../types/api';

export interface DateIdeaQuery {
  budgetLevel?: string;
  durationLevel?: string;
  scene?: string;
  tags?: string;
}

export function fetchDateIdeas(params?: DateIdeaQuery) {
  return apiClient.get<DateIdeaResponse[], DateIdeaResponse[]>('/date-ideas', { params });
}

export function fetchRecommendedDateIdeas(params?: DateIdeaQuery) {
  return apiClient.get<DateIdeaResponse[], DateIdeaResponse[]>('/date-ideas/recommend', { params });
}
