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

export function updateAnniversary(id: number, payload: CreateAnniversaryPayload) {
  return apiClient.put<AnniversaryResponse, AnniversaryResponse>(`/anniversaries/${id}`, payload);
}

export function deleteAnniversary(id: number) {
  return apiClient.delete<void, void>(`/anniversaries/${id}`);
}
