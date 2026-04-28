import { apiClient } from './client';
import type { AiAdviceResponse } from '../types/api';

export interface AiAdvicePayload {
  scenario: string;
  myFeeling?: string;
  partnerFeeling?: string;
  goal?: string;
}

export function requestCommunicationAdvice(payload: AiAdvicePayload) {
  return apiClient.post<AiAdviceResponse, AiAdviceResponse>('/ai/communication-advice', payload);
}
