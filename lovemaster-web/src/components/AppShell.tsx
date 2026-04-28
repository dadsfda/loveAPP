import { Outlet } from 'react-router-dom';
import BottomTabBar from './BottomTabBar';

export default function AppShell() {
  return (
    <div className="app-shell">
      <main className="app-main">
        <Outlet />
      </main>
      <BottomTabBar />
    </div>
  );
}
