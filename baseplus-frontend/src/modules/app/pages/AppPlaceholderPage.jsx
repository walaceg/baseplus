import { Card } from '../../../shared/components/index.js';

export function AppPlaceholderPage({ title, description }) {
  return (
    <div className="bp-app-page">
      <Card>
        <Card.Body>
          <div className="bp-app-page__panel">
            <h1>{title}</h1>
            <p>{description}</p>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}
