import { FormEvent, useEffect, useMemo, useState } from 'react';
import { CalendarHeart, HeartHandshake, Images, MessageCircleHeart, Pencil, Plus, Sparkles, Trash2, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import { createAnniversary, deleteAnniversary, fetchAnniversaries, updateAnniversary } from '../api/anniversary';
import { fetchPairing } from '../api/pairing';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { AnniversaryResponse, PairingResponse } from '../types/api';
import { daysBetween, daysUntil, formatDate } from '../utils/date';
import { toFriendlyError } from '../utils/error';

export default function HomePage() {
  const [pairing, setPairing] = useState<PairingResponse>();
  const [anniversaries, setAnniversaries] = useState<AnniversaryResponse[]>([]);
  const [title, setTitle] = useState('');
  const [date, setDate] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editingItem, setEditingItem] = useState<AnniversaryResponse | null>(null);

  const loveDays = useMemo(() => {
    const love = anniversaries.find((item) => item.type === 'LOVE_ANNIVERSARY') ?? anniversaries[0];
    return daysBetween(love?.date);
  }, [anniversaries]);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [pairingData, anniversaryData] = await Promise.all([fetchPairing(), fetchAnniversaries()]);
      setPairing(pairingData);
      setAnniversaries(anniversaryData);
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setLoading(false);
    }
  }

  function resetForm() {
    setTitle('');
    setDate('');
    setEditingItem(null);
  }

  function openCreateForm() {
    resetForm();
    setError('');
    setSuccess('');
    setEditing(true);
  }

  function openEditForm(item: AnniversaryResponse) {
    setTitle(item.title);
    setDate(item.date);
    setEditingItem(item);
    setError('');
    setSuccess('');
    setEditing(true);
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);
    const payload = {
      title,
      date,
      type: editingItem?.type || (anniversaries.length === 0 ? 'LOVE_ANNIVERSARY' : 'CUSTOM'),
      visibility: editingItem?.visibility || (pairing?.paired ? 'COUPLE' : 'PRIVATE'),
      remindDays: editingItem?.remindDays || [0, 1, 3],
      surpriseMode: editingItem?.surpriseMode || false,
      remark: editingItem?.remark || undefined
    };
    try {
      if (editingItem) {
        await updateAnniversary(editingItem.id, payload);
      } else {
        await createAnniversary(payload);
      }
      resetForm();
      setSuccess(editingItem ? '纪念日已更新' : '纪念日已保存');
      setEditing(false);
      await load();
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(item: AnniversaryResponse) {
    if (!window.confirm(`删除“${item.title}”？`)) {
      return;
    }
    setError('');
    setSuccess('');
    try {
      await deleteAnniversary(item.id);
      if (editingItem?.id === item.id) {
        resetForm();
        setEditing(false);
      }
      setSuccess('纪念日已删除');
      await load();
    } catch (err) {
      setError(toFriendlyError(err));
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <section className="page">
      <PageHeader
        eyebrow="LoveMaster"
        title="我们在一起"
        action={
          <button
            className="icon-action"
            onClick={() => {
              if (editing) {
                resetForm();
                setEditing(false);
              } else {
                openCreateForm();
              }
            }}
            aria-label={editing ? '关闭纪念日表单' : '新增纪念日'}
          >
            {editing ? <X size={21} /> : <Plus size={21} />}
          </button>
        }
      />
      <div className="hero-card">
        <div>
          <span className="hero-kicker">{pairing?.paired ? '已配对' : '等待配对'}</span>
          <strong>{loveDays || '--'}</strong>
          <span>天</span>
        </div>
        <p>{pairing?.paired ? `和 ${pairing.partner?.nickname || pairing.partner?.username || 'TA'} 一起记录这些小日子` : '先完成配对，再开启双人空间'}</p>
      </div>

      {error ? <p className="form-error">{error}</p> : null}
      {success ? <p className="form-success">{success}</p> : null}
      {loading ? <StatusBlock title="正在加载日子..." /> : null}

      <section className="quick-grid" aria-label="快捷入口">
        <Link className="quick-card" to="/preferences">
          <CalendarHeart size={20} />
          <span>喜好</span>
        </Link>
        <Link className="quick-card" to="/memories">
          <Images size={20} />
          <span>回忆</span>
        </Link>
        <Link className="quick-card" to="/ai">
          <MessageCircleHeart size={20} />
          <span>沟通</span>
        </Link>
        <Link className="quick-card" to="/pairing">
          <HeartHandshake size={20} />
          <span>{pairing?.paired ? '关系' : '配对'}</span>
        </Link>
      </section>

      {!loading && !pairing?.paired ? (
        <Link className="notice-card link-notice" to="/pairing">
          <strong>还没有完成配对</strong>
          <span>去“我的 - 我们的关系”或点这里输入对方邀请码。</span>
        </Link>
      ) : null}

      <section className="panel-section">
        <div className="section-title">
          <h2>即将到来的日子</h2>
          <Sparkles size={18} />
        </div>
        {anniversaries.length === 0 && !loading ? <StatusBlock title="还没有纪念日" description="先记录一个重要日子吧。" /> : null}
        <div className="list-stack">
          {anniversaries.map((item) => (
            <article className="soft-card row-card" key={item.id}>
              <div className="card-icon">❤</div>
              <div>
                <strong>{item.title}</strong>
                <span>{formatDate(item.date)}</span>
              </div>
              <div className="row-card-side">
                <em>{daysUntil(item.date) === 0 ? '今天' : `还有 ${daysUntil(item.date)} 天`}</em>
                <div className="card-actions">
                  <button type="button" onClick={() => openEditForm(item)} aria-label={`编辑${item.title}`}>
                    <Pencil size={15} />
                  </button>
                  <button type="button" onClick={() => void handleDelete(item)} aria-label={`删除${item.title}`}>
                    <Trash2 size={15} />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      {!editing ? (
        <button className="add-inline-card" onClick={openCreateForm}>
          <CalendarHeart size={18} />
          <span>记录一个重要日子</span>
          <Plus size={18} />
        </button>
      ) : (
        <form className="compact-form soft-card" onSubmit={handleSubmit}>
          <div className="section-title">
            <h2>{editingItem ? '编辑纪念日' : '新增纪念日'}</h2>
            <button
              type="button"
              className="plain-icon-button"
              onClick={() => {
                resetForm();
                setEditing(false);
              }}
              aria-label="关闭纪念日表单"
            >
              <X size={18} />
            </button>
          </div>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="例如：第一次旅行纪念日" />
          <input value={date} onChange={(event) => setDate(event.target.value)} type="date" />
          <button className="primary-button compact" disabled={!title || !date || submitting}>
            {submitting ? '保存中...' : editingItem ? '更新' : '保存'}
          </button>
        </form>
      )}
    </section>
  );
}
