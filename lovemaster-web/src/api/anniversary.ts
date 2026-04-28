import { apiClient } from './client';
import type { AnniversaryResponse, Visibility } from '../types/api';

export interface CreateAnniversaryPayload {
  title: string;
  date: string;
  type: string;
  visibility: Visibility;
  remindDays?: number[];
  surpriseMode?: boolean;
  remark?: string;
}

export function fetchAnniversaries() {
  return apiClient.get<AnniversaryResponse[], AnniversaryResponse[]>('/anniversaries');
}

export function createAnniversary(payload: CreateAnniversaryPayload) {
  return apiClient.post<AnniversaryResponse, AnniversaryResponse>('/anniversaries', payload);
}
