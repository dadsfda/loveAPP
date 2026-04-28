import { create } from 'zustand';
import type { TokenResponse, UserResponse } from '../types/api';

const TOKEN_KEY = 'lovemaster_tokens';

interface AuthState {
  token?: TokenResponse;
  user?: UserResponse;
  setToken: (token: TokenResponse) => void;
  setUser: (user?: UserResponse) => void;
  clearAuth: () => void;
}

function readStoredToken(): TokenResponse | undefined {
  const raw = localStorage.getItem(TOKEN_KEY);
  if (!raw) {
    return undefined;
  }

  try {
    return JSON.parse(raw) as TokenResponse;
  } catch {
    localStorage.removeItem(TOKEN_KEY);
    return undefined;
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  token: readStoredToken(),
  user: undefined,
  setToken: (token) => {
    localStorage.setItem(TOKEN_KEY, JSON.stringify(token));
    set({ token });
  },
  setUser: (user) => set({ user }),
  clearAuth: () => {
    localStorage.removeItem(TOKEN_KEY);
    set({ token: undefined, user: undefined });
  }
}));

export function getAccessToken() {
  return useAuthStore.getState().token?.accessToken;
}
