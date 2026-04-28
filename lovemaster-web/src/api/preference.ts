import { apiClient } from './client';
import type { PreferenceResponse, PreferenceTarget, Visibility } from '../types/api';
import type { PreferenceCategory } from '../utils/preference';

export interface CreatePreferencePayload {
  target: PreferenceTarget;
  category: PreferenceCategory;
  content: string;
  visibility: Visibility;
  tags?: string[];
  remark?: string;
}

export function fetchPreferences() {
  return apiClient.get<PreferenceResponse[], PreferenceResponse[]>('/preferences');
}

export function createPreference(payload: CreatePreferencePayload) {
  return apiClient.post<PreferenceResponse, PreferenceResponse>('/preferences', payload);
}
