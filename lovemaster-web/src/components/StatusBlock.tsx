interface StatusBlockProps {
  title: string;
  description?: string;
}

export default function StatusBlock({ title, description }: StatusBlockProps) {
  return (
    <div className="status-block">
      <strong>{title}</strong>
      {description ? <span>{description}</span> : null}
    </div>
  );
}
