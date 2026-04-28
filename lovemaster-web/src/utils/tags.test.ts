import { describe, expect, test } from 'vitest';
import { parseTagsInput } from './tags';

describe('parseTagsInput', () => {
  test('splits tags by comma spaces and Chinese punctuation', () => {
    expect(parseTagsInput('辣, 礼物 安静，拍照')).toEqual(['辣', '礼物', '安静', '拍照']);
  });

  test('removes empty and duplicate tags', () => {
    expect(parseTagsInput('礼物,,礼物  纪念感')).toEqual(['礼物', '纪念感']);
  });
});
