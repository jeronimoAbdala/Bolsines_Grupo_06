import { NavLink, Outlet } from 'react-router-dom';

export interface NavItem {
  to: string;
  label: string;
}

interface LayoutProps {
  navItems: NavItem[];
}

export const Layout = ({ navItems }: LayoutProps) => (
  <div className="layout">
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-logo">B</span>
        <div>
          <strong>Bolsines</strong>
          <small>PPAI · Grupo 6</small>
        </div>
      </div>
      <nav>
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === '/'} className="nav-link">
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
    <main className="content">
      <Outlet />
    </main>
  </div>
);
