interface WorkspaceIntroductionProps {
  eyebrow: string;
  title: string;
  description: string;
  nextStep: string;
}

export function WorkspaceIntroduction({
  eyebrow,
  title,
  description,
  nextStep,
}: WorkspaceIntroductionProps) {
  return (
    <section className="workspace-introduction">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="workspace-description">{description}</p>
      </div>
      <aside className="foundation-card">
        <span>Current delivery phase</span>
        <strong>Frontend foundation</strong>
        <p>{nextStep}</p>
      </aside>
    </section>
  );
}
