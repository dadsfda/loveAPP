import { FormEvent, useEffect, useRef, useState } from 'react';
import { CheckCircle2, ImagePlus, LoaderCircle, Pencil, Plus, Trash2, X } from 'lucide-react';
import { createMemory, deleteMemory, fetchMemories, updateMemory, uploadImage } from '../api/memory';
import { getUploadUrl } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { MemoryResponse, Visibility } from '../types/api';
import { formatDate } from '../utils/date';
import { toFriendlyError } from '../utils/error';
import { parseTagsInput } from '../utils/tags';

export default function MemoriesPage() {
  const [items, setItems] = useState<MemoryResponse[]>([]);
  const [title, setTitle] = useState('');
  const [memoryDate, setMemoryDate] = useState(new Date().toISOString().slice(0, 10));
  const [location, setLocation] = useState('');
  const [content, setContent] = useState('');
  const [tagsInput, setTagsInput] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [previewUrl, setPreviewUrl] = useState('');
  const [visibility, setVisibility] = useState<Visibility>('PRIVATE');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editingItem, setEditingItem] = useState<MemoryResponse | null>(null);
  const previewUrlRef = useRef('');

  async function load() {
    setLoading(true);
    try {
      setItems(await fetchMemories());
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setLoading(false);
    }
  }

  function clearLocalPreview() {
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
      previewUrlRef.current = '';
    }
  }

  function resetForm() {
    setTitle('');
    setMemoryDate(new Date().toISOString().slice(0, 10));
    setLocation('');
    setContent('');
    setTagsInput('');
    setImageUrl('');
    clearLocalPreview();
    setPreviewUrl('');
    setVisibility('PRIVATE');
    setEditingItem(null);
  }

  function openCreateForm() {
    resetForm();
    setError('');
    setSuccess('');
    setEditing(true);
  }

  function openEditForm(item: MemoryResponse) {
    clearLocalPreview();
    setTitle(item.title);
    setMemoryDate(item.memoryDate);
    setLocation(item.location || '');
    setContent(item.content || '');
    setTagsInput(item.tags.join(' '));
    setImageUrl(item.imageUrl || '');
    setPreviewUrl(getUploadUrl(item.imageUrl) || '');
    setVisibility(item.visibility);
    setEditingItem(item);
    setError('');
    setSuccess('');
    setEditing(true);
  }

  async function handleUpload(file?: File) {
    if (!file) {
      return;
    }
    setError('');
    setSuccess('');
    const localPreview = URL.createObjectURL(file);
    clearLocalPreview();
    previewUrlRef.current = localPreview;
    setPreviewUrl(localPreview);
    setUploading(true);
    try {
      const result = await uploadImage(file);
      setImageUrl(result.url);
      setSuccess('图片上传成功，保存后会出现在回忆里');
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setUploading(false);
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);
    const payload = { title, memoryDate, location, content, imageUrl, visibility, tags: parseTagsInput(tagsInput) };
    try {
      if (editingItem) {
        await updateMemory(editingItem.id, payload);
      } else {
        await createMemory(payload);
      }
      resetForm();
      setSuccess(editingItem ? '回忆已更新' : '回忆已保存');
      setEditing(false);
      await load();
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(item: MemoryResponse) {
    if (!window.confirm(`删除“${item.title}”？`)) {
      return;
    }
    setError('');
    setSuccess('');
    try {
      await deleteMemory(item.id);
      if (editingItem?.id === item.id) {
        resetForm();
        setEditing(false);
      }
      setSuccess('回忆已删除');
      await load();
    } catch (err) {
      setError(toFriendlyError(err));
    }
  }

  useEffect(() => {
    void load();
    return () => {
      clearLocalPreview();
    };
  }, []);

  return (
    <section className="page">
      <PageHeader
        title="专属回忆"
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
            aria-label={editing ? '关闭回忆表单' : '新增回忆'}
          >
            {editing ? <X size={21} /> : <Plus size={21} />}
          </button>
        }
      />
      {error ? <p className="form-error">{error}</p> : null}
      {success ? <p className="form-success">{success}</p> : null}

      {!editing ? (
        <button className="add-inline-card" onClick={openCreateForm}>
          <ImagePlus size={18} />
          <span>记录一段新的回忆</span>
          <Plus size={18} />
        </button>
      ) : (
        <form className="compact-form soft-card" onSubmit={handleSubmit}>
          <div className="section-title">
            <h2>{editingItem ? '编辑回忆' : '记录这一刻'}</h2>
            <button
              type="button"
              className="plain-icon-button"
              onClick={() => {
                resetForm();
                setEditing(false);
              }}
              aria-label="关闭回忆表单"
            >
              <X size={18} />
            </button>
          </div>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="标题" />
          <input value={memoryDate} onChange={(event) => setMemoryDate(event.target.value)} type="date" />
          <input value={location} onChange={(event) => setLocation(event.target.value)} placeholder="地点，可选" />
          <textarea value={content} onChange={(event) => setContent(event.target.value)} placeholder="写一点此刻想记住的话" rows={3} />
          <input value={tagsInput} onChange={(event) => setTagsInput(event.target.value)} placeholder="标签，可用空格或逗号分隔，如：旅行 日落" />
          {previewUrl ? (
            <div className="upload-preview">
              <img src={previewUrl} alt="待保存的回忆图片预览" />
              <span>{uploading ? '正在上传图片...' : '图片已上传'}</span>
            </div>
          ) : null}
          <label className={`upload-box ${uploading ? 'busy' : ''}`}>
            {uploading ? <LoaderCircle className="spin-icon" size={20} /> : imageUrl ? <CheckCircle2 size={20} /> : <ImagePlus size={20} />}
            <span>{uploading ? '正在上传...' : imageUrl ? '图片已上传，可以重新选择' : '上传一张图片'}</span>
            <input type="file" accept="image/*" onChange={(event) => void handleUpload(event.target.files?.[0])} />
          </label>
          <div className="segmented">
            <button type="button" className={visibility === 'PRIVATE' ? 'active' : ''} onClick={() => setVisibility('PRIVATE')}>仅自己</button>
            <button type="button" className={visibility === 'COUPLE' ? 'active' : ''} onClick={() => setVisibility('COUPLE')}>双方可见</button>
          </div>
          <button className="primary-button compact" disabled={!title || !memoryDate || uploading || submitting}>
            {submitting ? '保存中...' : editingItem ? '更新回忆' : '保存回忆'}
          </button>
        </form>
      )}

      {loading ? <StatusBlock title="正在整理回忆..." /> : null}
      {!loading && items.length === 0 ? <StatusBlock title="还没有回忆" description="一张照片、一句话，都可以成为以后回看的入口。" /> : null}
      <div className="memory-grid">
        {items.map((item) => (
          <article className="soft-card memory-card" key={item.id}>
            {item.imageUrl ? <img src={getUploadUrl(item.imageUrl)} alt={item.title} /> : <div className="memory-placeholder">❤</div>}
            <div className="card-heading">
              <div>
                <strong>{item.title}</strong>
                <span>{formatDate(item.memoryDate)}</span>
              </div>
              <div className="card-actions">
                <button type="button" onClick={() => openEditForm(item)} aria-label={`编辑${item.title}`}>
                  <Pencil size={15} />
                </button>
                <button type="button" onClick={() => void handleDelete(item)} aria-label={`删除${item.title}`}>
                  <Trash2 size={15} />
                </button>
              </div>
            </div>
            {item.location ? <small className="memory-location">{item.location}</small> : null}
            {item.content ? <p>{item.content}</p> : null}
            {item.tags.length > 0 ? (
              <div className="tag-row">
                {item.tags.map((tag) => <small key={tag}>{tag}</small>)}
              </div>
            ) : null}
          </article>
        ))}
      </div>
    </section>
  );
}
