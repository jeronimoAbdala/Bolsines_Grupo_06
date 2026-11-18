import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout, type NavItem } from '@/shared/components';
import { InicioPage } from '@/features/inicio';
import { RecepcionPage } from '@/features/recepcion';

const navItems: NavItem[] = [
  { to: '/', label: 'Inicio' },
  { to: '/recepcion', label: 'Registrar recepcion' },
];

export const router = createBrowserRouter([
  {
    element: <Layout navItems={navItems} />,
    children: [
      { path: '/', element: <InicioPage /> },
      { path: '/recepcion', element: <RecepcionPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
