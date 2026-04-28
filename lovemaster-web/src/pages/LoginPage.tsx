import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Heart, LockKeyhole, UserRound } from 'lucide-react';
import { login } from '../api/auth';
import { fetchMe } from '../api/user';
import { useAuthStore } from '../stores/authStore';
import { toFriendlyError } from '../utils/error';

export default function LoginPage() {
  const navigate = useNavigate();
  const setToken = useAuthStore((state) => state.setToken);
  const setUser = useAuthStore((state) => state.setUser);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const token = await login({ username, password });
      setToken(token);
      const user = await fetchMe();
      setUser(user);
      navigate('/', { replace: true });
    } catch (err) {
      setError(toFriendlyError(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-panel">
        <div className="brand-mark">
          <Heart fill="currentColor" />
        </div>
        <h1>恋爱增效器</h1>
        <p className="auth-subtitle">让重要日子不再错过</p>

        <form className="form-stack" onSubmit={handleSubmit}>
          <label className="input-field">
            <UserRound size={18} />
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="用户名" autoComplete="username" />
          </label>
          <label className="input-field">
            <LockKeyhole size={18} />
            <input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="密码" type="password" autoComplete="current-password" />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" disabled={submitting || !username || !password}>
            {submitting ? '登录中...' : '登录'}
          </button>
          <Link className="secondary-button" to="/register">
            注册账号
          </Link>
        </form>
        <p className="auth-footer">用心经营，让爱更长久</p>
      </section>
    </main>
  );
}
