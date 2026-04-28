import { FormEvent, useEffect, useState } from 'react';
import { Copy, HeartHandshake } from 'lucide-react';
import { bindPair, fetchPairing } from '../api/pairing';
import PageHeader from '../components/PageHeader';
import StatusBlock from '../components/StatusBlock';
import type { PairingResponse } from '../types/api';
import { formatDate } from '../utils/date';
import { toFriendlyError } from '../utils/error';

export default function PairingPage() {
  const [pairing, setPairing] = useState<PairingResponse>();
  const [pairCode, setPairCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function load() {
    try {
      setPairing(await fetchPairing());
    } catch (err) {
      setError(toFriendlyError(err));
    }
  }

  async function handleBind(event: FormEvent) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const data = await bindPair(pairCode.trim());
      setPairing(data);
      setPairCode('');
      setMessage('配对成功，欢迎进入你们的空间');
    } catch (err) {
      setError(toFriendlyError(err));
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <section className="page">
      <PageHeader title="我们的关系" />
      {error ? <p className="form-error">{error}</p> : null}
      {message ? <p className="form-success">{message}</p> : null}

      <section className="soft-card invite-card">
        <span>我的邀请码</span>
        <strong>{pairing?.pairCode || '--------'}</strong>
        <button className="ghost-button" onClick={() => pairing?.pairCode && navigator.clipboard.writeText(pairing.pairCode)}>
          <Copy size={16} /> 复制
        </button>
      </section>

      {!pairing?.paired ? (
        <form className="compact-form soft-card" onSubmit={handleBind}>
          <h2>输入对方邀请码</h2>
          <input value={pairCode} onChange={(event) => setPairCode(event.target.value.toUpperCase())} placeholder="请输入 8 位邀请码" maxLength={20} />
          <button className="primary-button compact" disabled={pairCode.trim().length < 8}>完成配对</button>
        </form>
      ) : (
        <section className="soft-card paired-card">
          <HeartHandshake size={32} />
          <div>
            <span>当前状态</span>
            <strong>已配对</strong>
            <p>你正在和 {pairing.partner?.nickname || pairing.partner?.username || 'TA'} 共同维护这段关系。</p>
            <small>配对时间：{formatDate(pairing.pairedAt)}</small>
          </div>
        </section>
      )}

      {!pairing ? <StatusBlock title="正在读取配对状态..." /> : null}
    </section>
  );
}
