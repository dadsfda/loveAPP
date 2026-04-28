import { useEffect, useState } from 'react';
import { Bell, ChevronRight, HeartHandshake, Info, Lock, LogOut, Settings, UserRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { logout } from '../api/auth';
import { fetchPairing } from '../api/pairing';
import { fetchMe } from '../api/user';
import PageHeader from '../components/PageHeader';
import type { PairingResponse, UserResponse } from '../types/api';
import { useAuthStore } from '../stores/authStore';
import { toFriendlyError } from '../utils/error';

const menu = [
  { label: '我们的关系', icon: HeartHandshake, to: '/pairing' },
  { label: '个人资料', icon: UserRound },
  { label: '恋爱设置', icon: Settings },
  { label: '隐私设置', icon: Lock },
  { label: '提醒统计', icon: Bell },
  { label: '关于我们', icon: Info }
];

export default function ProfilePage() {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const setUser = useAuthStore((state) => state.setUser);
  const [user, setLocalUser] = useState<UserResponse>();
  const [pairing, setPairing] = useState<PairingResponse>();
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchMe(), fetchPairing()])
      .then(([me, pair]) => {
        setLocalUser(me);
        setUser(me);
        setPairing(pair);
      })
      .catch((err) => setError(toFriendlyError(err)));
  }, [setUser]);

  async function handleLogout() {
    try {
      await logout();
    } catch {
      // 登出失败也清理本地登录态，避免用户被困在当前页面。
    } finally {
      clearAuth();
      window.location.assign('/login');
    }
  }

  return (
    <section className="page">
      <PageHeader title="我们的空间" />
      {error ? <p className="form-error">{error}</p> : null}
      <section className="profile-hero soft-card">
        <div className="avatar">{(user?.nickname || user?.username || '我').slice(0, 1)}</div>
        <div className="profile-heart">❤</div>
        <div className="avatar muted">{(pairing?.partner?.nickname || pairing?.partner?.username || 'TA').slice(0, 1)}</div>
        <p>
          {user?.nickname || user?.username || '我'} {pairing?.paired ? `和 ${pairing.partner?.nickname || 'TA'}` : '还未配对'}
        </p>
      </section>

      <section className="menu-list soft-card">
        {menu.map((item) => {
          const Icon = item.icon;
          return (
            <button key={item.label} className="menu-row" onClick={() => item.to && navigate(item.to)}>
              <Icon size={18} />
              <span>{item.label}</span>
              <ChevronRight size={18} />
            </button>
          );
        })}
      </section>

      <button className="logout-button" onClick={handleLogout}>
        <LogOut size={18} /> 退出登录
      </button>
    </section>
  );
}
