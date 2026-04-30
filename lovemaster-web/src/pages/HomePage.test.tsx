import { cleanup, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import HomePage from './HomePage';
import { deleteAnniversary, fetchAnniversaries, updateAnniversary } from '../api/anniversary';
import { fetchPairing } from '../api/pairing';

vi.mock('../api/anniversary', () => ({
  fetchAnniversaries: vi.fn(),
  createAnniversary: vi.fn(),
  updateAnniversary: vi.fn(),
  deleteAnniversary: vi.fn()
}));

vi.mock('../api/pairing', () => ({
  fetchPairing: vi.fn()
}));

const anniversary = {
  id: 12,
  creatorId: 1,
  coupleId: 3,
  title: '第一次旅行',
  date: '2026-05-20',
  type: 'CUSTOM',
  visibility: 'COUPLE' as const,
  remindDays: [0, 1, 3],
  surpriseMode: false,
  remark: null,
  createdAt: '2026-04-29T10:00:00',
  updatedAt: '2026-04-29T10:00:00'
};

const pairing = {
  paired: true,
  coupleId: 3,
  pairCode: 'ABC12345',
  partner: {
    id: 2,
    username: 'partner',
    nickname: 'TA'
  },
  pairedAt: '2026-04-29T10:00:00'
};

describe('HomePage anniversaries', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(fetchPairing).mockResolvedValue(pairing);
    vi.mocked(fetchAnniversaries).mockResolvedValue([anniversary]);
    vi.mocked(updateAnniversary).mockResolvedValue(anniversary);
    vi.mocked(deleteAnniversary).mockResolvedValue(undefined);
  });

  test('编辑纪念日时带入当前内容并提交更新', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    );

    await screen.findByText('第一次旅行');
    await user.click(screen.getByRole('button', { name: '编辑第一次旅行' }));

    expect(screen.getByRole('heading', { name: '编辑纪念日' })).toBeInTheDocument();
    expect(screen.getByDisplayValue('第一次旅行')).toBeInTheDocument();
    expect(screen.getByDisplayValue('2026-05-20')).toBeInTheDocument();

    await user.clear(screen.getByDisplayValue('第一次旅行'));
    await user.type(screen.getByPlaceholderText('例如：第一次旅行纪念日'), '第一次看海');
    await user.click(screen.getByRole('button', { name: '更新' }));

    await waitFor(() => {
      expect(updateAnniversary).toHaveBeenCalledWith(
        12,
        expect.objectContaining({
          title: '第一次看海',
          date: '2026-05-20',
          type: 'CUSTOM',
          visibility: 'COUPLE'
        })
      );
    });
  });

  test('确认后可以删除纪念日', async () => {
    const user = userEvent.setup();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    );

    await screen.findByText('第一次旅行');
    await user.click(screen.getByRole('button', { name: '删除第一次旅行' }));

    await waitFor(() => {
      expect(deleteAnniversary).toHaveBeenCalledWith(12);
    });
  });
});
