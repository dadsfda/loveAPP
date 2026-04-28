import { apiClient } from './client';
import type { TokenResponse, UserResponse } from '../types/api';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  nickname?: string;
  phone?: string;
  email?: string;
}

export function login(payload: LoginPayload) {
  return apiClient.post<TokenResponse, TokenResponse>('/auth/login', payload);
}

export function register(payload: RegisterPayload) {
  return apiClient.post<UserResponse, UserResponse>('/auth/register', payload);
}

export function logout() {
  return apiClient.post<void, void>('/auth/logout');
}
