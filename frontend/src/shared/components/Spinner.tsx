export const Spinner = ({ label = 'Cargando…' }: { label?: string }) => (
  <div className="spinner" role="status">
    <span className="spinner-dot" />
    {label}
  </div>
);
