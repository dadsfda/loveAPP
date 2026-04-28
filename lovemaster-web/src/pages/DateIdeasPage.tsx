import { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { fetchDateIdeas, fetchRecommendedDateIdeas } from '../api/dateIdea';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { DateIdeaResponse } from '../types/api';
import { toFriendlyError } from '../utils/error';

const scenes = ['全部', '室内', '室外', '雨天', '居家', '异地'];

export default function DateIdeasPage() {
  const [items, setItems] = useState<DateIdeaResponse[]>([]);
  const [scene, setScene] = useState('全部');
  const [recommend, setRecommend] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const params = scene === '全部' ? undefined : { scene };
    const request = recommend ? fetchRecommendedDateIdeas(params) : fetchDateIdeas(params);
    request.then(setItems).catch((err) => setError(toFriendlyError(err)));
  }, [scene, recommend]);

  return (
    <section className="page">
      <PageHeader title="今天去哪儿" action={<Search className="header-icon" size={22} />} />
      {error ? <p className="form-error">{error}</p> : null}

      <div className="chip-row">
        {scenes.map((item) => (
          <button key={item} className={`chip ${scene === item ? 'active' : ''}`} onClick={() => setScene(item)}>
            {item}
          </button>
        ))}
      </div>
      <div className="segmented page-toggle">
        <button className={recommend ? 'active' : ''} onClick={() => setRecommend(true)}>推荐</button>
        <button className={!recommend ? 'active' : ''} onClick={() => setRecommend(false)}>全部</button>
      </div>

      {items.length === 0 ? <StatusBlock title="暂时没有匹配灵感" description="换个场景再看看。" /> : null}
      <div className="list-stack">
        {items.map((item) => (
          <article className="soft-card idea-card" key={item.id}>
            <div>
              <strong>{item.title}</strong>
              <span>{item.scene} · {item.budgetLevel} · {item.durationLevel}</span>
            </div>
            <p>{item.tips || item.steps[0] || '找一个舒服的节奏，一起完成这件小事。'}</p>
            <div className="tag-row">
              {item.interestTags.slice(0, 4).map((tag) => <small key={tag}>{tag}</small>)}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
