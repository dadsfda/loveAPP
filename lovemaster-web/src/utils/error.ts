import { ApiBusinessError } from '../types/api';

export type ErrorTone = 'error' | 'notice';

export interface ErrorPresentation {
  message: string;
  tone: ErrorTone;
}

export function toFriendlyError(error: unknown) {
  return getErrorPresentation(error).message;
}

export function getErrorPresentation(error: unknown): ErrorPresentation {
  if (error instanceof ApiBusinessError) {
    if (error.code === 1801) {
      return {
        message: 'AI 服务暂未启用，可以稍后再试',
        tone: 'notice'
      };
    }
    return {
      message: error.message,
      tone: 'error'
    };
  }
  if (error instanceof Error) {
    return {
      message: error.message,
      tone: 'error'
    };
  }
  return {
    message: '操作失败，请稍后再试',
    tone: 'error'
  };
}
