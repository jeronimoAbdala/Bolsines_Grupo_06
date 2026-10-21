import type { ReactNode } from 'react';

interface FieldProps {
  label: string;
  htmlFor: string;
  hint?: string;
  children: ReactNode;
}

export const Field = ({ label, htmlFor, hint, children }: FieldProps) => (
  <div className="field">
    <label htmlFor={htmlFor}>{label}</label>
    {children}
    {hint && <small>{hint}</small>}
  </div>
);
