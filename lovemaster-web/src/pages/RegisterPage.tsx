import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Heart, LockKeyhole, UserRound } from 'lucide-react';
import { login, register } from '../api/auth';
import { fetchMe } from '../api/user';
import { useAuthStore } from '../stores/authStore';
import { toFriendlyError } from '../utils/error';

export default function RegisterPage() {
  const navigate = useNavigate();
  const setToken = useAuthStore((state) => state.setToken);
  const setUser = useAuthStore((state) => state.setUser);
  const [username, setUsername] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register({ username, nickname, password });
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
        <h1>创建账号</h1>
        <p className="auth-subtitle">先拥有一个只属于你们的小空间</p>

        <form className="form-stack" onSubmit={handleSubmit}>
          <label className="input-field">
            <UserRound size={18} />
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="用户名，至少 3 位" autoComplete="username" />
          </label>
          <label className="input-field">
            <UserRound size={18} />
            <input value={nickname} onChange={(event) => setNickname(event.target.value)} placeholder="昵称，可选" />
          </label>
          <label className="input-field">
            <LockKeyhole size={18} />
            <input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="密码，至少 6 位" type="password" autoComplete="new-password" />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" disabled={submitting || username.length < 3 || password.length < 6}>
            {submitting ? '注册中...' : '注册并进入'}
          </button>
          <Link className="secondary-button" to="/login">
            已有账号，去登录
          </Link>
        </form>
      </section>
    </main>
  );
}
