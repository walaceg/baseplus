import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../core/auth/useAuth.js';
import { useBranding } from '../../../shared/branding/useBranding.js';
import { Alert, BrandingLogo, Button, Card, Input } from '../../../shared/components/index.js';

export function ChangeInitialPasswordPage() {
  const { changeInitialPassword, mustChangePassword } = useAuth();
  const { branding, assetVersion } = useBranding();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    senhaAtual: '',
    novaSenha: '',
    confirmarNovaSenha: '',
  });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  if (!mustChangePassword) {
    return <Navigate to="/app/dashboard" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!form.senhaAtual || !form.novaSenha || !form.confirmarNovaSenha) {
      setError('Preencha todos os campos.');
      return;
    }

    try {
      setLoading(true);
      const response = await changeInitialPassword(form);
      setMessage(response.message ?? 'Senha alterada com sucesso.');
      navigate('/app/dashboard', { replace: true });
    } catch (requestError) {
      const response = requestError.response?.data;
      setError(response?.errors?.[0] ?? response?.message ?? 'Nao foi possivel alterar a senha.');
    } finally {
      setLoading(false);
    }
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  return (
    <main className="auth-page" data-login-background-mode={branding.loginBackgroundMode}>
      <div className="auth-page__overlay">
        <section className="auth-page__hero">
          <BrandingLogo
            className="auth-page__logo"
            logoUrl={branding.logoUrl}
            assetVersion={assetVersion}
            name={branding.nomePlataforma}
            subtitle={branding.subtituloInstitucional}
            size="lg"
            showText={false}
          />
          <div>
            <h1>{branding.nomePlataforma}</h1>
            <p>{branding.subtituloInstitucional}</p>
            <strong>Troca obrigatoria de senha</strong>
          </div>
        </section>

        <Card className="auth-panel" aria-labelledby="change-password-title">
          <Card.Body>
            <p className="auth-panel__eyebrow">Primeiro acesso</p>
            <h2 id="change-password-title" className="auth-panel__title">
              Alterar senha
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="auth-form">
                <Input
                  autoComplete="current-password"
                  id="senha-atual"
                  label="Senha atual"
                  type="password"
                  value={form.senhaAtual}
                  onChange={(event) => updateField('senhaAtual', event.target.value)}
                />
                <Input
                  autoComplete="new-password"
                  id="nova-senha"
                  label="Nova senha"
                  type="password"
                  value={form.novaSenha}
                  onChange={(event) => updateField('novaSenha', event.target.value)}
                />
                <Input
                  autoComplete="new-password"
                  id="confirmar-nova-senha"
                  label="Confirmar nova senha"
                  type="password"
                  value={form.confirmarNovaSenha}
                  onChange={(event) => updateField('confirmarNovaSenha', event.target.value)}
                />
                {message ? <Alert variant="success">{message}</Alert> : null}
                {error ? <p className="form-error">{error}</p> : null}
                <Button type="submit" disabled={loading}>
                  {loading ? 'Alterando...' : 'Alterar senha'}
                </Button>
              </div>
            </form>
          </Card.Body>
        </Card>
      </div>
    </main>
  );
}
