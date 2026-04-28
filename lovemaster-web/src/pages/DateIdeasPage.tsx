import { useEffect, useMemo, useState } from 'react';
import { Filter, Sparkles, X } from 'lucide-react';
import { fetchDateIdeas, fetchRecommendedDateIdeas } from '../api/dateIdea';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { DateIdeaResponse } from '../types/api';
import {
  budgetOptions,
  buildDateIdeaQuery,
  durationOptions,
  getBudgetLabel,
  getDurationLabel,
  getSceneLabel,
  sceneOptions,
  type DateIdeaBudgetFilter,
  type DateIdeaDurationFilter,
  type DateIdeaSceneFilter
} from '../utils/dateIdea';
import { toFriendlyError } from '../utils/error';

export default function DateIdeasPage() {
  const [items, setItems] = useState<DateIdeaResponse[]>([]);
  const [scene, setScene] = useState<DateIdeaSceneFilter>('ALL');
  const [budgetLevel, setBudgetLevel] = useState<DateIdeaBudgetFilter>('ALL');
  const [durationLevel, setDurationLevel] = useState<DateIdeaDurationFilter>('ALL');
  const [tagsInput, setTagsInput] = useState('');
  const [recommend, setRecommend] = useState(true);
  const [selectedIdea, setSelectedIdea] = useState<DateIdeaResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const query = useMemo(
    () => buildDateIdeaQuery({ scene, budgetLevel, durationLevel, tagsInput }),
    [scene, budgetLevel, durationLevel, tagsInput]
  );

  useEffect(() => {
    setLoading(true);
    setError('');
    const request = recommend ? fetchRecommendedDateIdeas(query) : fetchDateIdeas(query);
    request
      .then(setItems)
      .catch((err) => setError(toFriendlyError(err)))
      .finally(() => setLoading(false));
  }, [query, recommend]);

  const hasActiveFilters = Boolean(query);

  return (
    <section className="page">
      <PageHeader eyebrow="约会灵感" title="今天去哪儿" action={<Filter className="header-icon" size={22} />} />
      {error ? <p className="form-error">{error}</p> : null}

      <div className="segmented page-toggle">
        <button className={recommend ? 'active' : ''} onClick={() => setRecommend(true)}>推荐</button>
        <button className={!recommend ? 'active' : ''} onClick={() => setRecommend(false)}>全部</button>
      </div>

      <div className="filter-panel">
        <div>
          <div className="filter-label">场景</div>
          <div className="chip-row">
            {sceneOptions.map((item) => (
              <button key={item.value} className={`chip ${scene === item.value ? 'active' : ''}`} onClick={() => setScene(item.value)}>
                {item.label}
              </button>
            ))}
          </div>
        </div>
        <div>
          <div className="filter-label">预算</div>
          <div className="chip-row">
            {budgetOptions.map((item) => (
              <button key={item.value} className={`chip ${budgetLevel === item.value ? 'active' : ''}`} onClick={() => setBudgetLevel(item.value)}>
                {item.label}
              </button>
            ))}
          </div>
        </div>
        <div>
          <div className="filter-label">耗时</div>
          <div className="chip-row">
            {durationOptions.map((item) => (
              <button key={item.value} className={`chip ${durationLevel === item.value ? 'active' : ''}`} onClick={() => setDurationLevel(item.value)}>
                {item.label}
              </button>
            ))}
          </div>
        </div>
        <label className="filter-input">
          <Sparkles size={18} />
          <input
            value={tagsInput}
            onChange={(event) => setTagsInput(event.target.value)}
            placeholder="输入兴趣标签，比如 拍照 美食"
          />
        </label>
        {hasActiveFilters ? (
          <button
            type="button"
            className="ghost-button filter-clear"
            onClick={() => {
              setScene('ALL');
              setBudgetLevel('ALL');
              setDurationLevel('ALL');
              setTagsInput('');
            }}
          >
            清空筛选
          </button>
        ) : null}
      </div>

      {loading ? <StatusBlock title="正在寻找合适的灵感" description="马上就好。" /> : null}
      {!loading && items.length === 0 ? <StatusBlock title="暂时没有匹配灵感" description="换个筛选条件再看看。" /> : null}
      <div className="list-stack">
        {items.map((item) => (
          <button className="soft-card idea-card" key={item.id} onClick={() => setSelectedIdea(item)}>
            <div className="idea-card-top">
              <div>
                <strong>{item.title}</strong>
                <span>{getSceneLabel(item.scene)} · {getBudgetLabel(item.budgetLevel)} · {getDurationLabel(item.durationLevel)}</span>
              </div>
              {recommend && typeof item.matchScore === 'number' ? <em>{item.matchScore} 分</em> : null}
            </div>
            <p>{item.tips || item.steps[0] || '找一个舒服的节奏，一起完成这件小事。'}</p>
            <div className="tag-row">
              {item.interestTags.slice(0, 4).map((tag) => <small key={tag}>{tag}</small>)}
            </div>
          </button>
        ))}
      </div>

      {selectedIdea ? (
        <div className="sheet-backdrop" role="presentation" onClick={() => setSelectedIdea(null)}>
          <section className="detail-sheet" role="dialog" aria-modal="true" aria-label={selectedIdea.title} onClick={(event) => event.stopPropagation()}>
            <div className="sheet-header">
              <div>
                <span>{getSceneLabel(selectedIdea.scene)} · {getBudgetLabel(selectedIdea.budgetLevel)} · {getDurationLabel(selectedIdea.durationLevel)}</span>
                <h2>{selectedIdea.title}</h2>
              </div>
              <button type="button" className="plain-icon-button" onClick={() => setSelectedIdea(null)} aria-label="关闭详情">
                <X size={20} />
              </button>
            </div>
            <div className="tag-row">
              {selectedIdea.interestTags.map((tag) => <small key={tag}>{tag}</small>)}
            </div>
            <div className="detail-block">
              <h3>怎么做</h3>
              <ol>
                {selectedIdea.steps.map((step) => <li key={step}>{step}</li>)}
              </ol>
            </div>
            {selectedIdea.tips ? (
              <div className="notice-card">
                <strong>小提示</strong>
                <span>{selectedIdea.tips}</span>
              </div>
            ) : null}
          </section>
        </div>
      ) : null}
    </section>
  );
}
