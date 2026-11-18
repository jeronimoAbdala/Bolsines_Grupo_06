import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout, type NavItem } from '@/shared/components';
import { InicioPage } from '@/features/inicio';
import { RecepcionPage } from '@/features/recepcion';
import { AgregarRemitoPage, BolsinesPage, CrearBolsinPage } from '@/features/bolsines';
import { ComisionesPage } from '@/features/comisiones';
import { UsuariosPage } from '@/features/usuarios';

const navItems: NavItem[] = [
  { to: '/', label: 'Inicio' },
  { to: '/recepcion', label: 'Registrar recepcion' },
  { to: '/bolsines', label: 'Bolsines' },
  { to: '/comisiones', label: 'Comisiones medicas' },
  { to: '/usuarios', label: 'Usuarios' },
];

export const router = createBrowserRouter([
  {
    element: <Layout navItems={navItems} />,
    children: [
      { path: '/', element: <InicioPage /> },
      { path: '/recepcion', element: <RecepcionPage /> },
      { path: '/bolsines', element: <BolsinesPage /> },
      { path: '/bolsines/nuevo', element: <CrearBolsinPage /> },
      { path: '/bolsines/remito', element: <AgregarRemitoPage /> },
      { path: '/comisiones', element: <ComisionesPage /> },
      { path: '/usuarios', element: <UsuariosPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
