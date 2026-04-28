import axios, { AxiosError } from 'axios';
import { ApiBusinessError, type ApiResponse } from '../types/api';
import { getAccessToken, useAuthStore } from '../stores/authStore';

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: apiBaseURL,
  timeout: 12000
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (body && typeof body.code === 'number') {
      if (body.code !== 200) {
        throw new ApiBusinessError(body.message || '请求失败', body.code, response.status);
      }
      return body.data;
    }
    return response.data;
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status;
    if (status === 401) {
      useAuthStore.getState().clearAuth();
      window.location.assign('/login');
    }

    const code = error.response?.data?.code ?? status ?? 0;
    const message = error.response?.data?.message || error.message || '网络请求失败';
    return Promise.reject(new ApiBusinessError(message, code, status));
  }
);

export function getUploadUrl(path?: string | null) {
  if (!path) {
    return undefined;
  }
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  return apiBaseURL.replace('/api/v1', '') + path;
}
