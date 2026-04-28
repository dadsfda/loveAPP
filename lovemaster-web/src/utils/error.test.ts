import { describe, expect, test } from 'vitest';
import { ApiBusinessError } from '../types/api';
import { getErrorPresentation } from './error';

describe('error presentation', () => {
  test('treats disabled AI service as a notice instead of a failure', () => {
    const result = getErrorPresentation(new ApiBusinessError('AI服务未启用', 1801, 200));

    expect(result.tone).toBe('notice');
    expect(result.message).toBe('AI 服务暂未启用，可以稍后再试');
  });

  test('keeps regular business errors as error tone', () => {
    const result = getErrorPresentation(new ApiBusinessError('用户名已存在', 400, 400));

    expect(result.tone).toBe('error');
    expect(result.message).toBe('用户名已存在');
  });
});
