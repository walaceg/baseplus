import { useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Avatar, Badge, Button, Card, Input, Loading, Select } from '../../shared/components/index.js';
import { brandingPresets, isPresetActive } from '../../shared/branding/branding.js';
import { useAuth } from '../../core/auth/useAuth.js';
import { useTheme } from '../../core/theme/useTheme.js';
import * as contaService from './contaService.js';
import './conta.css';

const idiomaOptions = [
  { label: 'Português (Brasil)', value: 'pt-BR' },
  { label: 'English', value: 'en-US' },
  { label: 'Español', value: 'es-ES' },
];

const temaOptions = [
  { label: 'Padrão da aplicação', value: 'APP_DEFAULT' },
  { label: 'Claro', value: 'LIGHT' },
  { label: 'Escuro', value: 'DARK' },
];

const densidadeOptions = [
  { label: 'Padrão da aplicação', value: 'APP_DEFAULT' },
  { label: 'Regular', value: 'REGULAR' },
  { label: 'Compacta', value: 'COMPACT' },
];

const notificacaoOptions = [
  { label: 'Ativadas', value: 'true' },
  { label: 'Desativadas', value: 'false' },
];

export function ContaPage() {
  const { user, updateUser } = useAuth();
  const {
    effectiveDensity,
    effectiveTheme,
    setUserDensityPreference,
    setUserThemePreference,
  } = useTheme();
  const avatarInputRef = useRef(null);
  const [account, setAccount] = useState({ nome: '', email: '' });
  const [preferences, setPreferences] = useState({
    userThemePreference: 'APP_DEFAULT',
    idioma: 'pt-BR',
    notificacoes: true,
    corPrimaria: '#2563eb',
    corSecundaria: '#1e40af',
    userDensityPreference: 'APP_DEFAULT',
  });
  const [password, setPassword] = useState({ senhaAtual: '', novaSenha: '' });
  const [loading, setLoading] = useState(true);
  const [savingAccount, setSavingAccount] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [savingPreferences, setSavingPreferences] = useState(false);
  const [savingAvatar, setSavingAvatar] = useState(false);
  const [success, setSuccess] = useState({ account: '', password: '', preferences: '' });
  const [error, setError] = useState('');
  const [avatarFeedback, setAvatarFeedback] = useState({ type: '', message: '' });

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const [conta, prefs] = await Promise.all([contaService.getConta(), contaService.getPreferencias()]);

        if (!active) {
          return;
        }

        setAccount({ nome: conta.nome ?? '', email: conta.email ?? '' });
        setPreferences({
          userThemePreference: prefs.tema ?? 'APP_DEFAULT',
          idioma: prefs.idioma ?? 'pt-BR',
          notificacoes: Boolean(prefs.notificacoes),
          corPrimaria: prefs.corPrimaria ?? '#2563eb',
          corSecundaria: prefs.corSecundaria ?? '#1e40af',
          userDensityPreference: prefs.preferenciaVisual ?? 'APP_DEFAULT',
        });
        setUserThemePreference(prefs.tema);
        setUserDensityPreference(prefs.preferenciaVisual);
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Não foi possível carregar a conta.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [setUserDensityPreference, setUserThemePreference]);

  const avatarName = useMemo(
    () => account.nome || user?.nome || user?.email || 'Usuário',
    [account.nome, user?.email, user?.nome],
  );

  const avatarSrc = user?.avatarUrl ?? null;
  const avatarVersion = user?.avatarVersion ?? user?.avatarUpdatedAt ?? 0;
  const activePreset = useMemo(
    () =>
      preferences.userThemePreference === 'APP_DEFAULT'
        ? null
        : brandingPresets.find((preset) =>
            isPresetActive(
              {
                tema: preferences.userThemePreference,
                corPrimaria: preferences.corPrimaria,
                corSecundaria: preferences.corSecundaria,
              },
              preset,
            ),
          ) ?? null,
    [preferences.corPrimaria, preferences.corSecundaria, preferences.userThemePreference],
  );

  useEffect(() => {
    if (loading) {
      return;
    }

    setUserThemePreference(preferences.userThemePreference);
    setUserDensityPreference(preferences.userDensityPreference);
  }, [
    loading,
    preferences.userDensityPreference,
    preferences.userThemePreference,
    setUserDensityPreference,
    setUserThemePreference,
  ]);

  function clearMessages() {
    setError('');
    setSuccess({ account: '', password: '', preferences: '' });
  }

  function clearAvatarFeedback() {
    setAvatarFeedback({ type: '', message: '' });
  }

  function setAvatarError(message) {
    setAvatarFeedback({ type: 'error', message });
  }

  function setAvatarSuccess(message) {
    setAvatarFeedback({ type: 'success', message });
  }

  function isValidAvatarFile(file) {
    if (!file) {
      return false;
    }

    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    const fileName = (file.name ?? '').toLowerCase();
    const allowedByExtension = fileName.endsWith('.png') || fileName.endsWith('.jpg') || fileName.endsWith('.jpeg');

    return allowedTypes.includes(file.type) || allowedByExtension;
  }

  function openAvatarPicker() {
    clearAvatarFeedback();
    avatarInputRef.current?.click();
  }

  async function handleAvatarChange(event) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    clearMessages();
    clearAvatarFeedback();

    if (!isValidAvatarFile(file)) {
      setAvatarError('Envie uma imagem PNG, JPG ou JPEG.');
      return;
    }

    setSavingAvatar(true);

    try {
      const uploadedAvatar = await contaService.uploadAvatar(file);
      updateUser({ ...(user ?? {}), avatarUrl: uploadedAvatar.avatarUrl, avatarVersion: Date.now() });
      setAvatarSuccess('Avatar atualizado com sucesso.');
    } catch (requestError) {
      setAvatarError(requestError.response?.data?.message ?? 'Não foi possível enviar a foto.');
    } finally {
      setSavingAvatar(false);
    }
  }

  async function handleAvatarRemove() {
    clearMessages();
    clearAvatarFeedback();
    setSavingAvatar(true);

    try {
      const updatedAvatar = await contaService.deleteAvatar();
      updateUser({ ...(user ?? {}), avatarUrl: updatedAvatar.avatarUrl, avatarVersion: Date.now() });
      setAvatarSuccess('Avatar removido com sucesso.');
    } catch (requestError) {
      setAvatarError(requestError.response?.data?.message ?? 'Não foi possível remover a foto.');
    } finally {
      setSavingAvatar(false);
    }
  }

  async function handleAccountSubmit(event) {
    event.preventDefault();
    clearMessages();
    setSavingAccount(true);

    try {
      const updatedAccount = await contaService.updateConta(account);
      updateUser({ ...user, nome: updatedAccount.nome, email: updatedAccount.email });
      setAccount({ nome: updatedAccount.nome, email: updatedAccount.email });
      setSuccess((current) => ({ ...current, account: 'Dados atualizados com sucesso.' }));
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível atualizar a conta.');
    } finally {
      setSavingAccount(false);
    }
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault();
    clearMessages();
    setSavingPassword(true);

    try {
      await contaService.changeSenha(password);
      setPassword({ senhaAtual: '', novaSenha: '' });
      setSuccess((current) => ({ ...current, password: 'Senha alterada com sucesso.' }));
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível alterar a senha.');
    } finally {
      setSavingPassword(false);
    }
  }

  async function handlePreferencesSubmit(event) {
    event.preventDefault();
    clearMessages();
    setSavingPreferences(true);

    try {
      const updatedPreferences = await contaService.updatePreferencias({
        tema: preferences.userThemePreference,
        idioma: preferences.idioma,
        notificacoes: Boolean(preferences.notificacoes),
        corPrimaria: preferences.corPrimaria,
        corSecundaria: preferences.corSecundaria,
        preferenciaVisual: preferences.userDensityPreference,
      });

      setPreferences({
        userThemePreference: updatedPreferences.tema ?? 'APP_DEFAULT',
        idioma: updatedPreferences.idioma ?? 'pt-BR',
        notificacoes: Boolean(updatedPreferences.notificacoes),
        corPrimaria: updatedPreferences.corPrimaria ?? '#2563eb',
        corSecundaria: updatedPreferences.corSecundaria ?? '#1e40af',
        userDensityPreference: updatedPreferences.preferenciaVisual ?? 'APP_DEFAULT',
      });
      setUserThemePreference(updatedPreferences.tema);
      setUserDensityPreference(updatedPreferences.preferenciaVisual);
      setSuccess((current) => ({ ...current, preferences: 'Preferências atualizadas com sucesso.' }));
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível atualizar as preferências.');
    } finally {
      setSavingPreferences(false);
    }
  }

  if (loading) {
    return (
      <Card>
        <Card.Body>
          <Loading label="Carregando conta..." />
        </Card.Body>
      </Card>
    );
  }

  return (
    <div className="bp-account-page">
      <Card>
        <Card.Body>
          <section className="bp-account-page__hero">
            <div className="bp-account-page__avatar-block">
              <button
                className="bp-account-page__avatar-button"
                disabled={savingAvatar}
                aria-busy={savingAvatar}
                type="button"
                aria-label="Alterar foto do perfil"
                onClick={openAvatarPicker}
              >
                <Avatar
                  alt={avatarName}
                  className="bp-account-page__avatar"
                  name={avatarName}
                  size="lg"
                  src={avatarSrc}
                  version={avatarVersion}
                />
                {savingAvatar ? (
                  <span className="bp-account-page__avatar-loading" aria-hidden="true">
                    <span className="bp-loading__spinner" />
                  </span>
                ) : null}
              </button>

              <div className="bp-account-page__avatar-actions">
                <input
                  ref={avatarInputRef}
                  accept="image/png,image/jpeg,.jpg,.jpeg"
                  aria-label="Selecionar foto do perfil"
                  className="bp-account-page__avatar-input"
                  type="file"
                  onChange={handleAvatarChange}
                />
                <div className="bp-account-page__avatar-actions-row">
                  <Button disabled={savingAvatar} size="sm" onClick={openAvatarPicker}>
                    {savingAvatar ? 'Enviando...' : 'Alterar foto'}
                  </Button>
                  {avatarSrc ? (
                    <Button disabled={savingAvatar} size="sm" type="button" variant="ghost" onClick={handleAvatarRemove}>
                      {savingAvatar ? 'Removendo...' : 'Remover foto'}
                    </Button>
                  ) : null}
                </div>
                <p className="bp-account-page__avatar-hint">PNG, JPG ou JPEG até o limite permitido.</p>
                {avatarFeedback.message ? (
                  <p
                  className={[
                      'bp-account-page__avatar-feedback',
                      avatarFeedback.type === 'error'
                        ? 'bp-account-page__avatar-feedback--error'
                        : 'bp-account-page__avatar-feedback--success',
                    ]
                      .filter(Boolean)
                      .join(' ')}
                  role="status"
                  aria-live="polite"
                >
                  {avatarFeedback.message}
                </p>
              ) : null}
              </div>
            </div>
            <div className="bp-account-page__hero-content">
              <div className="bp-account-page__hero-title">
                <h1>Conta</h1>
                <div className="bp-account-page__badges">
                  <Badge variant="primary">{effectiveTheme}</Badge>
                  <Badge variant="secondary">{effectiveDensity}</Badge>
                  <Badge variant={preferences.notificacoes ? 'success' : 'warning'}>
                    {preferences.notificacoes ? 'Notificações ativas' : 'Notificações desativadas'}
                  </Badge>
                </div>
              </div>
              <p>Dados do usuário autenticado e preferências da Base+.</p>
              <div className="bp-account-page__meta">
                <span>{user?.email ?? account.email ?? '-'}</span>
                <span>{account.nome || user?.nome || 'Usuário autenticado'}</span>
              </div>
            </div>
          </section>
        </Card.Body>
      </Card>

      {error ? <Alert variant="error">{error}</Alert> : null}

      <div className="bp-account-layout">
        <Card className="bp-account-layout__primary">
          <Card.Header>
            <div>
              <strong>Dados da conta</strong>
              <p>Nome e email do perfil atual.</p>
            </div>
          </Card.Header>
          <Card.Body>
            <form className="bp-account-form" onSubmit={handleAccountSubmit}>
              <Input
                id="account-name"
                label="Nome"
                value={account.nome}
                onChange={(event) => {
                  setAccount((current) => ({ ...current, nome: event.target.value }));
                  setSuccess((current) => ({ ...current, account: '' }));
                }}
              />
              <Input
                id="account-email"
                label="Email"
                type="email"
                value={account.email}
                onChange={(event) => {
                  setAccount((current) => ({ ...current, email: event.target.value }));
                  setSuccess((current) => ({ ...current, account: '' }));
                }}
              />
              {success.account ? <Alert variant="success">{success.account}</Alert> : null}
              <div className="bp-account-form__actions">
                <Button type="submit" disabled={savingAccount}>
                  {savingAccount ? 'Salvando...' : 'Salvar dados'}
                </Button>
              </div>
            </form>
          </Card.Body>
        </Card>

        <div className="bp-account-layout__stack">
          <Card>
            <Card.Header>
              <div>
                <strong>Alterar senha</strong>
                <p>Atualize a credencial de acesso com segurança.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <form className="bp-account-form" onSubmit={handlePasswordSubmit}>
                <Input
                  id="current-password"
                  label="Senha atual"
                  type="password"
                  value={password.senhaAtual}
                  onChange={(event) => {
                    setPassword((current) => ({ ...current, senhaAtual: event.target.value }));
                    setSuccess((current) => ({ ...current, password: '' }));
                  }}
                />
                <Input
                  id="new-password"
                  label="Nova senha"
                  type="password"
                  value={password.novaSenha}
                  onChange={(event) => {
                    setPassword((current) => ({ ...current, novaSenha: event.target.value }));
                    setSuccess((current) => ({ ...current, password: '' }));
                  }}
                />
                {success.password ? <Alert variant="success">{success.password}</Alert> : null}
                <div className="bp-account-form__actions">
                  <Button type="submit" variant="secondary" disabled={savingPassword}>
                    {savingPassword ? 'Alterando...' : 'Alterar senha'}
                  </Button>
                </div>
              </form>
            </Card.Body>
          </Card>

          <Card>
            <Card.Header>
              <div>
                <strong>Preferências</strong>
                <p>Organizadas em blocos visuais alinhados ao Branding Runtime.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <form className="bp-account-form" onSubmit={handlePreferencesSubmit}>
                <div className="bp-account-preferences">
                  <section className="bp-account-preferences__section">
                    <div className="bp-account-preferences__header">
                      <div className="bp-account-preferences__header-row">
                        <strong>Aparência</strong>
                        {activePreset ? <Badge variant="primary">{activePreset.name}</Badge> : null}
                      </div>
                      <span>Define tema, cores e densidade visual.</span>
                    </div>
                    <div className="bp-account-preferences__divider" aria-hidden="true" />
                    <div className="bp-account-preferences__presets" aria-label="Presets de aparência">
                      {brandingPresets.map((preset) => {
                        const active = preferences.userThemePreference !== 'APP_DEFAULT' && isPresetActive(
                          {
                            tema: preferences.userThemePreference,
                            corPrimaria: preferences.corPrimaria,
                            corSecundaria: preferences.corSecundaria,
                          },
                          preset,
                        );

                        return (
                          <button
                            key={preset.id}
                            className={[
                              'bp-account-preferences__preset',
                              active ? 'bp-account-preferences__preset--active' : '',
                            ]
                              .filter(Boolean)
                              .join(' ')}
                            style={{
                              '--preset-primary': preset.corPrimaria,
                              '--preset-secondary': preset.corSecundaria,
                            }}
                            type="button"
                            onClick={() => {
                              setPreferences((current) => ({
                                ...current,
                                userThemePreference: preset.tema === 'dark' ? 'DARK' : 'LIGHT',
                                corPrimaria: preset.corPrimaria,
                                corSecundaria: preset.corSecundaria,
                              }));
                              setSuccess((current) => ({ ...current, preferences: '' }));
                            }}
                          >
                            <span className="bp-account-preferences__preset-swatches" aria-hidden="true">
                              <span className="bp-account-preferences__preset-swatch bp-account-preferences__preset-swatch--primary" />
                              <span className="bp-account-preferences__preset-swatch bp-account-preferences__preset-swatch--secondary" />
                            </span>
                            <span className="bp-account-preferences__preset-name">{preset.name}</span>
                            <span className="bp-account-preferences__preset-meta">
                              {preset.tema === 'dark' ? 'Tema escuro' : 'Tema claro'}
                            </span>
                          </button>
                        );
                      })}
                    </div>
                    <div className="bp-account-preferences__controls">
                      <Select
                        id="theme"
                        label="Tema"
                        options={temaOptions}
                        value={preferences.userThemePreference}
                        onChange={(event) => {
                          const nextTheme = event.target.value;
                          setPreferences((current) => ({ ...current, userThemePreference: nextTheme }));
                          setUserThemePreference(nextTheme);
                          setSuccess((current) => ({ ...current, preferences: '' }));
                        }}
                      />
                      <Select
                        id="density"
                        label="Densidade visual"
                        options={densidadeOptions}
                        value={preferences.userDensityPreference}
                        onChange={(event) => {
                          const nextDensity = event.target.value;
                          setPreferences((current) => ({ ...current, userDensityPreference: nextDensity }));
                          setUserDensityPreference(nextDensity);
                          setSuccess((current) => ({ ...current, preferences: '' }));
                        }}
                      />
                    </div>
                  </section>

                  <section className="bp-account-preferences__section">
                    <div className="bp-account-preferences__header">
                      <strong>Regionalização</strong>
                      <span>Idioma usado na interface.</span>
                    </div>
                    <div className="bp-account-preferences__divider" aria-hidden="true" />
                    <div className="bp-account-preferences__controls bp-account-preferences__controls--single">
                      <Select
                        id="language"
                        label="Idioma"
                        options={idiomaOptions}
                        value={preferences.idioma}
                        onChange={(event) => {
                        setPreferences((current) => ({ ...current, idioma: event.target.value }));
                        setSuccess((current) => ({ ...current, preferences: '' }));
                      }}
                    />
                    </div>
                  </section>

                  <section className="bp-account-preferences__section bp-account-preferences__section--last">
                    <div className="bp-account-preferences__header">
                      <strong>Comunicação</strong>
                      <span>Controle simples de alertas e avisos.</span>
                    </div>
                    <div className="bp-account-preferences__divider" aria-hidden="true" />
                    <div className="bp-account-preferences__controls bp-account-preferences__controls--single">
                      <Select
                        id="notifications"
                        label="Notificações"
                        options={notificacaoOptions}
                        value={String(preferences.notificacoes)}
                        onChange={(event) => {
                          setPreferences((current) => ({
                            ...current,
                            notificacoes: event.target.value === 'true',
                          }));
                          setSuccess((current) => ({ ...current, preferences: '' }));
                        }}
                      />
                    </div>
                  </section>
                </div>

                {success.preferences ? <Alert variant="success">{success.preferences}</Alert> : null}
                <div className="bp-account-form__actions bp-account-form__actions--sticky bp-account-form__actions--footer">
                  <Button type="submit" disabled={savingPreferences}>
                    {savingPreferences ? 'Salvando...' : 'Salvar preferências'}
                  </Button>
                </div>
              </form>
            </Card.Body>
          </Card>
        </div>
      </div>
    </div>
  );
}
