type Tone = 'info' | 'success' | 'warning' | 'neutral';

interface BadgeProps {
  label: string;
}

// Nombres de estado de los diagramas (DME): RecibidoEnCMDestino, RecibidoYAceptado, RecibidaYAceptada,
// Enviado, EnBolsinEnviado, RecibidaYRechazada, ...
const toneFor = (label: string): Tone => {
  if (label.includes('Rechaz')) return 'info';
  if (label.startsWith('Recibido') || label === 'RecibidaYAceptada') return 'success';
  if (label === 'Enviado' || label === 'EnBolsinEnviado') return 'warning';
  return 'neutral';
};

/** Muestra un estado del dominio (Enviado, RecibidoEnCMDestino, ...) con un color segun su significado. */
export const Badge = ({ label }: BadgeProps) => (
  <span className={`badge badge-${toneFor(label)}`}>{label.replaceAll('_', ' ')}</span>
);
