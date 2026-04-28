import { FormEvent, useState } from 'react';
import { WandSparkles } from 'lucide-react';
import { requestCommunicationAdvice } from '../api/ai';
import PageHeader from '../components/PageHeader';
import type { AiAdviceResponse } from '../types/api';
import { getErrorPresentation, type ErrorPresentation } from '../utils/error';

export default function AiAdvicePage() {
  const [scenario, setScenario] = useState('');
  const [myFeeling, setMyFeeling] = useState('');
  const [partnerFeeling, setPartnerFeeling] = useState('');
  const [goal, setGoal] = useState('');
  const [result, setResult] = useState<AiAdviceResponse>();
  const [feedback, setFeedback] = useState<ErrorPresentation>();
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setFeedback(undefined);
    setResult(undefined);
    setSubmitting(true);
    try {
      setResult(await requestCommunicationAdvice({ scenario, myFeeling, partnerFeeling, goal }));
    } catch (err) {
      setFeedback(getErrorPresentation(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page">
      <PageHeader eyebrow="情绪缓冲带" title="先把话说轻一点" action={<WandSparkles className="header-icon" size={22} />} />
      {feedback ? (
        <section className={feedback.tone === 'notice' ? 'notice-card' : 'form-error'}>
          <strong>{feedback.tone === 'notice' ? '暂时还不能生成 AI 建议' : '生成失败'}</strong>
          <span>{feedback.message}</span>
          {feedback.tone === 'notice' ? <small>你仍然可以先把想说的话写下来，等后端开启 AI 服务后再生成参考话术。</small> : null}
        </section>
      ) : null}

      <form className="compact-form soft-card" onSubmit={handleSubmit}>
        <textarea value={scenario} onChange={(event) => setScenario(event.target.value)} placeholder="发生了什么？例如：因为晚回消息吵架了" rows={3} />
        <input value={myFeeling} onChange={(event) => setMyFeeling(event.target.value)} placeholder="我的感受：委屈、生气、担心..." />
        <input value={partnerFeeling} onChange={(event) => setPartnerFeeling(event.target.value)} placeholder="TA 可能的感受，可选" />
        <input value={goal} onChange={(event) => setGoal(event.target.value)} placeholder="我想达到的目标，可选" />
        <button className="primary-button compact" disabled={!scenario.trim() || submitting}>
          {submitting ? '生成中...' : '生成沟通建议'}
        </button>
      </form>

      {result ? (
        <section className="soft-card advice-card">
          <span className={`risk-pill ${result.riskLevel}`}>{result.riskLevel}</span>
          <h2>参考建议</h2>
          <p>{result.advice}</p>
          <h2>可以这样说</h2>
          <blockquote>{result.messageTemplate}</blockquote>
          <small>{result.reminder}</small>
        </section>
      ) : null}
    </section>
  );
}
