import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Gift, Plus, X } from 'lucide-react';
import { createPreference, fetchPreferences } from '../api/preference';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { PreferenceResponse, PreferenceTarget, Visibility } from '../types/api';
import { toFriendlyError } from '../utils/error';
import { getPreferenceCategoryLabel, preferenceCategories, type PreferenceCategory } from '../utils/preference';
import { parseTagsInput } from '../utils/tags';

export default function PreferencePage() {
  const [items, setItems] = useState<PreferenceResponse[]>([]);
  const [category, setCategory] = useState<PreferenceCategory>('GIFT');
  const [content, setContent] = useState('');
  const [tagsInput, setTagsInput] = useState('');
  const [visibility, setVisibility] = useState<Visibility>('PRIVATE');
  const [target, setTarget] = useState<PreferenceTarget>('PARTNER_OBSERVED');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [editing, setEditing] = useState(false);

  const filteredItems = useMemo(() => items.filter((item) => item.category === category), [category, items]);

  async function load() {
    setLoading(true);
    try {
      setItems(await fetchPreferences());
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);
    try {
      await createPreference({ category, content, visibility, target, tags: parseTagsInput(tagsInput) });
      setContent('');
      setTagsInput('');
      setSuccess('偏好已保存');
      setEditing(false);
      await load();
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setSubmitting(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <section className="page">
      <PageHeader
        title="TA 的小偏好"
        action={
          <button className="icon-action" onClick={() => setEditing((value) => !value)} aria-label={editing ? '关闭新增偏好' : '新增偏好'}>
            {editing ? <X size={21} /> : <Plus size={21} />}
          </button>
        }
      />
      {error ? <p className="form-error">{error}</p> : null}
      {success ? <p className="form-success">{success}</p> : null}

      <div className="chip-row">
        {preferenceCategories.map((item) => (
          <button key={item.value} className={`chip ${category === item.value ? 'active' : ''}`} onClick={() => setCategory(item.value)}>
            {item.label}
          </button>
        ))}
      </div>

      {!editing ? (
        <button className="add-inline-card" onClick={() => setEditing(true)}>
          <Gift size={18} />
          <span>记录一条新的偏好</span>
          <Plus size={18} />
        </button>
      ) : (
        <form className="compact-form soft-card" onSubmit={handleSubmit}>
          <div className="section-title">
            <h2>新增偏好</h2>
            <button type="button" className="plain-icon-button" onClick={() => setEditing(false)} aria-label="取消新增">
              <X size={18} />
            </button>
          </div>
          <textarea value={content} onChange={(event) => setContent(event.target.value)} placeholder="例如：不喜欢太吵的餐厅，喜欢手写卡片" rows={3} />
          <input value={tagsInput} onChange={(event) => setTagsInput(event.target.value)} placeholder="标签，可用空格或逗号分隔，如：辣 礼物 安静" />
          <div className="segmented">
            <button type="button" className={target === 'PARTNER_OBSERVED' ? 'active' : ''} onClick={() => setTarget('PARTNER_OBSERVED')}>TA 的偏好</button>
            <button type="button" className={target === 'SELF' ? 'active' : ''} onClick={() => setTarget('SELF')}>我的偏好</button>
          </div>
          <div className="segmented">
            <button type="button" className={visibility === 'PRIVATE' ? 'active' : ''} onClick={() => setVisibility('PRIVATE')}>仅自己</button>
            <button type="button" className={visibility === 'COUPLE' ? 'active' : ''} onClick={() => setVisibility('COUPLE')}>双方可见</button>
          </div>
          <button className="primary-button compact" disabled={!content.trim() || submitting}>
            {submitting ? '保存中...' : '保存偏好'}
          </button>
        </form>
      )}

      {loading ? <StatusBlock title="正在整理偏好..." /> : null}
      {!loading && filteredItems.length === 0 ? <StatusBlock title="这个分类还没有记录" description="先记下一条不容易忘的小线索。" /> : null}
      <div className="list-stack">
        {filteredItems.map((item) => (
          <article className="soft-card preference-card" key={item.id}>
            <div className="card-heading">
              <strong>{getPreferenceCategoryLabel(item.category)}</strong>
              <small>{item.target === 'SELF' ? '我的偏好' : 'TA 的偏好'}</small>
            </div>
            <p>{item.content}</p>
            {item.tags.length > 0 ? (
              <div className="tag-row">
                {item.tags.map((tag) => <small key={tag}>{tag}</small>)}
              </div>
            ) : null}
            <span>{item.visibility === 'COUPLE' ? '双方可见' : '仅自己可见'}</span>
          </article>
        ))}
      </div>
    </section>
  );
}
