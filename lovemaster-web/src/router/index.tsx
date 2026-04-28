import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import AppShell from '../components/AppShell';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import HomePage from '../pages/HomePage';
import PairingPage from '../pages/PairingPage';
import ProfilePage from '../pages/ProfilePage';
import PreferencePage from '../pages/PreferencePage';
import DateIdeasPage from '../pages/DateIdeasPage';
import MemoriesPage from '../pages/MemoriesPage';
import AiAdvicePage from '../pages/AiAdvicePage';

function AuthGuard() {
  const token = useAuthStore((state) => state.token);
  return token?.accessToken ? <Outlet /> : <Navigate to="/login" replace />;
}

function GuestGuard() {
  const token = useAuthStore((state) => state.token);
  return token?.accessToken ? <Navigate to="/" replace /> : <Outlet />;
}

export const router = createBrowserRouter([
  {
    element: <GuestGuard />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> }
    ]
  },
  {
    element: <AuthGuard />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: '/', element: <HomePage /> },
          { path: '/pairing', element: <PairingPage /> },
          { path: '/preferences', element: <PreferencePage /> },
          { path: '/ideas', element: <DateIdeasPage /> },
          { path: '/memories', element: <MemoriesPage /> },
          { path: '/ai', element: <AiAdvicePage /> },
          { path: '/profile', element: <ProfilePage /> }
        ]
      }
    ]
  },
  { path: '*', element: <Navigate to="/" replace /> }
]);
