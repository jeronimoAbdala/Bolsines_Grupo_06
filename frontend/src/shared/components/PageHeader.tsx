interface PageHeaderProps {
  title: string;
  subtitle?: string;
}

export const PageHeader = ({ title, subtitle }: PageHeaderProps) => (
  <header className="page-header">
    <h1>{title}</h1>
    {subtitle && <p>{subtitle}</p>}
  </header>
);
