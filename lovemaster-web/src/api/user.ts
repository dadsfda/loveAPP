import { apiClient } from './client';
import type { UserResponse } from '../types/api';

export function fetchMe() {
  return apiClient.get<UserResponse, UserResponse>('/users/me');
}
