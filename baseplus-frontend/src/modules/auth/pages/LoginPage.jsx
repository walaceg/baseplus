import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../core/auth/useAuth.js';
import { useBranding } from '../../../shared/branding/useBranding.js';
import { BrandingLogo, Button, Input } from '../../../shared/components/index.js';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { signIn } = useAuth();
  const { branding, assetVersion } = useBranding();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname ?? '/app';
  const platformName = branding.nomePlataforma || 'Base+';
  const platformSubtitle = branding.subtituloInstitucional || 'Painel administrativo';
  const loginName = branding.whiteLabelEnabled && branding.whiteLabelName ? branding.whiteLabelName : platformName;
  const loginSubtitle = branding.whiteLabelEnabled && branding.whiteLabelSubtitle
    ? branding.whiteLabelSubtitle
    : platformSubtitle;
  const logoUrl = branding.loginLogoUrl || branding.logoUrl || branding.faviconUrl;

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage('');

    if (!email.trim() || !password) {
      setErrorMessage('Informe email e senha.');
      return;
    }

    try {
      setIsSubmitting(true);
      const session = await signIn({ email: email.trim(), password });
      if (session.mustChangePassword) {
        navigate('/change-initial-password', { replace: true });
        return;
      }
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setErrorMessage(error.response?.data?.message ?? 'Nao foi possivel entrar.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main
      className="auth-page"
      data-login-background-mode={branding.loginBackgroundMode}
      style={{
        '--auth-primary-color': branding.corPrimaria || 'var(--color-primary)',
        '--auth-secondary-color': branding.corSecundaria || 'var(--color-secondary)',
      }}
    >
      <section className="auth-shell" aria-labelledby="login-title">
        <div className="auth-shell__media" aria-label="Identidade institucional">
          <div className="auth-shell__media-content">
            <span className="auth-shell__kicker">{loginName}</span>
            <div className="auth-shell__copy">
              <h1>{loginName}</h1>
              <p className="auth-shell__headline">{loginSubtitle}</p>
              <p className="auth-shell__support">
                Ambiente seguro para acesso aos recursos corporativos da plataforma.
              </p>
            </div>
          </div>

          {errorMessage ? (
            <div className="auth-shell__status" role="status">
              <strong>Não foi possível autenticar</strong>
              <span>Verifique os dados informados ou tente novamente em instantes.</span>
            </div>
          ) : null}
        </div>

        <div className="auth-shell__form-panel">
          <div className="auth-panel">
            <div className="auth-panel__brand">
              <BrandingLogo
                className="auth-page__logo"
                logoUrl={logoUrl}
                assetVersion={assetVersion}
                name={loginName}
                subtitle={loginSubtitle}
                size="lg"
                showText={false}
              />
              <div>
                <span className="auth-panel__eyebrow">ACESSO AO SISTEMA</span>
                <strong>{loginName}</strong>
              </div>
            </div>

            <div className="auth-panel__header">
              <h2 id="login-title">Entrar</h2>
              <p>Informe suas credenciais para continuar.</p>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="auth-form">
                <Input
                  autoComplete="email"
                  id="email"
                  label="Email"
                  name="email"
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                />
                <Input
                  autoComplete="current-password"
                  id="password"
                  label="Senha"
                  name="password"
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
                {errorMessage ? <p className="form-error" role="alert">{errorMessage}</p> : null}
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? 'Entrando...' : 'Entrar'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      </section>
    </main>
  );
}
