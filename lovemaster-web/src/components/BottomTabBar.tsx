import { CalendarHeart, Home, Images, Lightbulb, MessageCircleHeart, UserRound } from 'lucide-react';
import { NavLink } from 'react-router-dom';

const tabs = [
  { to: '/', label: '首页', icon: Home },
  { to: '/preferences', label: '喜好', icon: CalendarHeart },
  { to: '/ideas', label: '灵感', icon: Lightbulb },
  { to: '/memories', label: '回忆', icon: Images },
  { to: '/ai', label: '沟通', icon: MessageCircleHeart },
  { to: '/profile', label: '我的', icon: UserRound }
];

export default function BottomTabBar() {
  return (
    <nav className="bottom-tab" aria-label="主导航">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        return (
          <NavLink key={tab.to} to={tab.to} className={({ isActive }) => `tab-item ${isActive ? 'active' : ''}`}>
            <Icon size={21} strokeWidth={2.2} />
            <span>{tab.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
}
