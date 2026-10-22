import type { ReactNode } from 'react';

interface CardProps {
  title?: string;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
}

export const Card = ({ title, actions, children, className = '' }: CardProps) => (
  <section className={`card ${className}`}>
    {(title || actions) && (
      <header className="card-header">
        {title && <h2>{title}</h2>}
        {actions}
      </header>
    )}
    {children}
  </section>
);
