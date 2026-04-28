import { apiClient } from './client';
import type { MemoryResponse, Visibility } from '../types/api';

export interface CreateMemoryPayload {
  title: string;
  memoryDate: string;
  location?: string;
  content?: string;
  imageUrl?: string;
  visibility: Visibility;
  tags?: string[];
  remark?: string;
}

export function fetchMemories() {
  return apiClient.get<MemoryResponse[], MemoryResponse[]>('/memories');
}

export function createMemory(payload: CreateMemoryPayload) {
  return apiClient.post<MemoryResponse, MemoryResponse>('/memories', payload);
}

export function updateMemory(id: number, payload: CreateMemoryPayload) {
  return apiClient.put<MemoryResponse, MemoryResponse>(`/memories/${id}`, payload);
}

export function deleteMemory(id: number) {
  return apiClient.delete<void, void>(`/memories/${id}`);
}

export function uploadImage(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return apiClient.post<{ url: string }, { url: string }>('/files/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
