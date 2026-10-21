import type { ReactNode } from 'react';

interface AlertProps {
  tone: 'error' | 'success' | 'info';
  children: ReactNode;
}

export const Alert = ({ tone, children }: AlertProps) => (
  <div className={`alert alert-${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
    {children}
  </div>
);
