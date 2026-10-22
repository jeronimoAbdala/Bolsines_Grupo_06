type Tone = 'info' | 'success' | 'warning' | 'neutral';

interface BadgeProps {
  label: string;
}

const toneFor = (label: string): Tone => {
  if (label.startsWith('RECIBID')) return 'success';
  if (label.startsWith('ENVIAD')) return 'warning';
  if (label.startsWith('RECHAZ')) return 'info';
  return 'neutral';
};

/** Muestra un estado del dominio (ENVIADO, RECIBIDO, ...) con un color segun su significado. */
export const Badge = ({ label }: BadgeProps) => (
  <span className={`badge badge-${toneFor(label)}`}>{label.replaceAll('_', ' ')}</span>
);
