import type { ButtonHTMLAttributes } from 'react';

type Variant = 'primary' | 'success' | 'danger' | 'ghost';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  loading?: boolean;
}

export const Button = ({ variant = 'primary', loading = false, disabled, children, className = '', ...rest }: ButtonProps) => (
  <button className={`btn btn-${variant} ${className}`} disabled={disabled || loading} {...rest}>
    {loading ? 'Procesando…' : children}
  </button>
);
