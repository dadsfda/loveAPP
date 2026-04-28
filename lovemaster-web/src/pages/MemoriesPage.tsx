import { FormEvent, useEffect, useRef, useState } from 'react';
import { CheckCircle2, ImagePlus, LoaderCircle, Plus, X } from 'lucide-react';
import { createMemory, fetchMemories, uploadImage } from '../api/memory';
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

  async function handleUpload(file?: File) {
    if (!file) {
      return;
    }
    setError('');
    setSuccess('');
    const localPreview = URL.createObjectURL(file);
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
    }
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
    try {
      await createMemory({ title, memoryDate, location, content, imageUrl, visibility, tags: parseTagsInput(tagsInput) });
      setTitle('');
      setLocation('');
      setContent('');
      setTagsInput('');
      setImageUrl('');
      if (previewUrlRef.current) {
        URL.revokeObjectURL(previewUrlRef.current);
        previewUrlRef.current = '';
      }
      setPreviewUrl('');
      setSuccess('回忆已保存');
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
    return () => {
      if (previewUrlRef.current) {
        URL.revokeObjectURL(previewUrlRef.current);
      }
    };
  }, []);

  return (
    <section className="page">
      <PageHeader
        title="专属回忆"
        action={
          <button className="icon-action" onClick={() => setEditing((value) => !value)} aria-label={editing ? '关闭新增回忆' : '新增回忆'}>
            {editing ? <X size={21} /> : <Plus size={21} />}
          </button>
        }
      />
      {error ? <p className="form-error">{error}</p> : null}
      {success ? <p className="form-success">{success}</p> : null}

      {!editing ? (
        <button className="add-inline-card" onClick={() => setEditing(true)}>
          <ImagePlus size={18} />
          <span>记录一段新的回忆</span>
          <Plus size={18} />
        </button>
      ) : (
        <form className="compact-form soft-card" onSubmit={handleSubmit}>
          <div className="section-title">
            <h2>记录这一刻</h2>
            <button type="button" className="plain-icon-button" onClick={() => setEditing(false)} aria-label="取消新增">
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
            {submitting ? '保存中...' : '保存回忆'}
          </button>
        </form>
      )}

      {loading ? <StatusBlock title="正在整理回忆..." /> : null}
      {!loading && items.length === 0 ? <StatusBlock title="还没有回忆" description="一张照片、一句话，都可以成为以后回看的入口。" /> : null}
      <div className="memory-grid">
        {items.map((item) => (
          <article className="soft-card memory-card" key={item.id}>
            {item.imageUrl ? <img src={getUploadUrl(item.imageUrl)} alt={item.title} /> : <div className="memory-placeholder">❤</div>}
            <strong>{item.title}</strong>
            <span>{formatDate(item.memoryDate)}</span>
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
