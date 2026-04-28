import { apiClient } from './client';
import type { PairingResponse } from '../types/api';

export function fetchPairing() {
  return apiClient.get<PairingResponse, PairingResponse>('/pairings/me');
}

export function bindPair(pairCode: string) {
  return apiClient.post<PairingResponse, PairingResponse>('/pairings/bind', { pairCode });
}
