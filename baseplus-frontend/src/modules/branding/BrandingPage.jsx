import { useEffect, useMemo, useRef, useState } from 'react';
import { Upload, Trash2 } from 'lucide-react';
import { ActionIconButton, Alert, Badge, Button, BrandingColorPicker, BrandingLogo, Card, Input, Loading, Select } from '../../shared/components/index.js';
import { useBranding } from '../../shared/branding/useBranding.js';
import {
  brandingPresets,
  buildBrandingPreviewStyle,
  isPresetActive,
  normalizeBrandingSettings,
  resolveBrandingAssetUrl,
} from '../../shared/branding/branding.js';
import {
  updateBranding,
  uploadBrandingCompactLogo,
  uploadBrandingFavicon,
  uploadBrandingLoginBackground,
  uploadBrandingLoginLogo,
  uploadBrandingLogo,
} from './brandingService.js';
import { useAuthorization } from '../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../shared/auth/permissions.js';
import './branding.css';

const themeOptions = [
  { label: 'Claro', value: 'light' },
  { label: 'Escuro', value: 'dark' },
];

const densityOptions = [
  { label: 'Regular', value: 'regular' },
  { label: 'Compacta', value: 'compact' },
];

const loginBackgroundOptions = [
  { label: 'Padrão', value: 'DEFAULT', description: 'Mantém a composição atual do preview.' },
  { label: 'Gradiente institucional', value: 'INSTITUTIONAL_GRADIENT', description: 'Reforça cor primária e secundária.' },
  { label: 'Superfície neutra', value: 'NEUTRAL_SURFACE', description: 'Reduz o peso visual do fundo.' },
];

export function BrandingPage() {
  const { branding, assetVersion, isLoading, refreshBranding } = useBranding();
  const { hasPermission } = useAuthorization();
  const canEditBranding = hasPermission(PERMISSIONS.BRANDING_EDIT);
  const canUploadAssets = hasPermission(PERMISSIONS.BRANDING_UPLOAD_ASSETS);
  const normalizedBranding = useMemo(() => normalizeBrandingSettings(branding), [branding]);
  const [draft, setDraft] = useState(normalizedBranding);
  const [colorValidity, setColorValidity] = useState({ corPrimaria: true, corSecundaria: true });
  const [uploadingLogo, setUploadingLogo] = useState(false);
  const [uploadingCompactLogo, setUploadingCompactLogo] = useState(false);
  const [uploadingFavicon, setUploadingFavicon] = useState(false);
  const [uploadingLoginLogo, setUploadingLoginLogo] = useState(false);
  const [uploadingLoginBackground, setUploadingLoginBackground] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const logoUploadRef = useRef(null);
  const compactLogoUploadRef = useRef(null);
  const faviconUploadRef = useRef(null);
  const loginLogoUploadRef = useRef(null);
  const loginBackgroundUploadRef = useRef(null);

  useEffect(() => {
    setDraft(normalizedBranding);
    setColorValidity({ corPrimaria: true, corSecundaria: true });
  }, [normalizedBranding]);

  const hasColorErrors = !colorValidity.corPrimaria || !colorValidity.corSecundaria;
  const canSave = useMemo(
    () =>
      Boolean(draft.nomePlataforma && draft.subtituloInstitucional) &&
      !hasColorErrors &&
      !uploadingLogo &&
      !uploadingCompactLogo &&
      !uploadingFavicon &&
      !uploadingLoginLogo &&
      !uploadingLoginBackground,
    [draft.nomePlataforma, draft.subtituloInstitucional, hasColorErrors, uploadingCompactLogo, uploadingFavicon, uploadingLoginBackground, uploadingLoginLogo, uploadingLogo],
  );
  const previewStyle = useMemo(() => buildBrandingPreviewStyle({ ...draft, assetVersion }), [assetVersion, draft]);
  const loginPreviewName = draft.whiteLabelEnabled && draft.whiteLabelName ? draft.whiteLabelName : draft.nomePlataforma;
  const loginPreviewSubtitle = draft.whiteLabelEnabled && draft.whiteLabelSubtitle
    ? draft.whiteLabelSubtitle
    : draft.subtituloInstitucional;
  const loginLogoPreviewUrl = draft.loginLogoUrl || draft.logoUrl;
  const backgroundPreviewUrl = resolveBrandingAssetUrl(draft.loginBackgroundUrl, assetVersion);

  function clearFeedback() {
    setMessage('');
    setError('');
  }

  function triggerLogoUpload() {
    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar assets de branding.');
      return;
    }

    logoUploadRef.current?.click();
  }

  function triggerCompactLogoUpload() {
    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar assets de branding.');
      return;
    }

    compactLogoUploadRef.current?.click();
  }

  function triggerFaviconUpload() {
    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar assets de branding.');
      return;
    }

    faviconUploadRef.current?.click();
  }

  function triggerLoginLogoUpload() {
    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar assets de branding.');
      return;
    }

    loginLogoUploadRef.current?.click();
  }

  function triggerLoginBackgroundUpload() {
    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar assets de branding.');
      return;
    }

    loginBackgroundUploadRef.current?.click();
  }

  function resetFileInput(event) {
    event.target.value = '';
  }

  function isSupportedLogoFile(file) {
    if (!file) {
      return false;
    }

    if (['image/png', 'image/jpeg'].includes(file.type)) {
      return true;
    }

    const name = file.name.toLowerCase();
    return name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg');
  }

  function isSupportedCompactLogoFile(file) {
    return isSupportedLogoFile(file);
  }

  function isSupportedFaviconFile(file) {
    if (!file) {
      return false;
    }

    if (['image/png', 'image/jpeg', 'image/x-icon', 'image/vnd.microsoft.icon'].includes(file.type)) {
      return true;
    }

    const name = file.name.toLowerCase();
    return name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.ico');
  }

  function isSupportedBackgroundFile(file) {
    if (!file) {
      return false;
    }

    if (['image/png', 'image/jpeg'].includes(file.type)) {
      return true;
    }

    const name = file.name.toLowerCase();
    return name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg');
  }

  async function handleLogoUpload(event) {
    const file = event.target.files?.[0];
    resetFileInput(event);

    if (!file) {
      return;
    }

    clearFeedback();

    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar logo.');
      return;
    }

    if (!isSupportedLogoFile(file)) {
      setError('Use PNG, JPG ou JPEG para a logo.');
      return;
    }

    setUploadingLogo(true);

    try {
      const saved = await uploadBrandingLogo(file);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Logo institucional atualizada com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel enviar a logo.');
    } finally {
      setUploadingLogo(false);
    }
  }

  async function handleCompactLogoUpload(event) {
    const file = event.target.files?.[0];
    resetFileInput(event);

    if (!file) {
      return;
    }

    clearFeedback();

    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar marca reduzida.');
      return;
    }

    if (!isSupportedCompactLogoFile(file)) {
      setError('Use PNG, JPG ou JPEG para a logo reduzida.');
      return;
    }

    setUploadingCompactLogo(true);

    try {
      const saved = await uploadBrandingCompactLogo(file);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Marca reduzida atualizada com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel enviar a marca reduzida.');
    } finally {
      setUploadingCompactLogo(false);
    }
  }

  async function handleClearLogo() {
    clearFeedback();

    if (!canEditBranding) {
      setError('Permissao BRANDING_EDIT necessaria para remover a logo.');
      return;
    }

    setUploadingLogo(true);

    try {
      const saved = await updateBranding({ ...draft, logoUrl: '' });
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Logo removida com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel remover a logo.');
    } finally {
      setUploadingLogo(false);
    }
  }

  async function handleFaviconUpload(event) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    clearFeedback();

    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar favicon.');
      return;
    }

    if (!isSupportedFaviconFile(file)) {
      setError('Use PNG, JPG, JPEG ou ICO para o favicon.');
      return;
    }

    setUploadingFavicon(true);

    try {
      const saved = await uploadBrandingFavicon(file);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Favicon atualizado com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel enviar o favicon.');
    } finally {
      setUploadingFavicon(false);
    }
  }

  async function handleLoginLogoUpload(event) {
    const file = event.target.files?.[0];
    resetFileInput(event);

    if (!file) {
      return;
    }

    clearFeedback();

    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar logo do login.');
      return;
    }

    if (!isSupportedLogoFile(file)) {
      setError('Use PNG, JPG ou JPEG para a logo do login.');
      return;
    }

    setUploadingLoginLogo(true);

    try {
      const saved = await uploadBrandingLoginLogo(file);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Logo do login atualizada com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel enviar a logo do login.');
    } finally {
      setUploadingLoginLogo(false);
    }
  }

  async function handleLoginBackgroundUpload(event) {
    const file = event.target.files?.[0];
    resetFileInput(event);

    if (!file) {
      return;
    }

    clearFeedback();

    if (!canUploadAssets) {
      setError('Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar background.');
      return;
    }

    if (!isSupportedBackgroundFile(file)) {
      setError('Use PNG, JPG ou JPEG para o background.');
      return;
    }

    setUploadingLoginBackground(true);

    try {
      const saved = await uploadBrandingLoginBackground(file);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Background institucional atualizado com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel enviar o background.');
    } finally {
      setUploadingLoginBackground(false);
    }
  }

  function applyPreset(preset) {
    if (!canEditBranding) {
      setError('Permissao BRANDING_EDIT necessaria para alterar o tema.');
      return;
    }

    setDraft((current) => ({
      ...current,
      tema: preset.tema,
      corPrimaria: preset.corPrimaria.toLowerCase(),
      corSecundaria: preset.corSecundaria.toLowerCase(),
    }));
    setColorValidity({ corPrimaria: true, corSecundaria: true });
    clearFeedback();
  }

  async function handleSubmit(event) {
    event.preventDefault();
    clearFeedback();

    if (!canEditBranding) {
      setError('Acesso nao autorizado para editar branding.');
      return;
    }

    if (hasColorErrors) {
      setError('Corrija os campos de cor em hexadecimal antes de salvar.');
      return;
    }

    setSaving(true);

    try {
      const saved = await updateBranding(draft);
      const nextBranding = normalizeBrandingSettings(saved);
      setDraft(nextBranding);
      setColorValidity({ corPrimaria: true, corSecundaria: true });
      await refreshBranding();
      setMessage('Branding atualizado com sucesso.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel salvar o branding.');
    } finally {
      setSaving(false);
    }
  }

  if (isLoading) {
    return (
      <Card>
        <Card.Body>
          <Loading label="Carregando branding..." />
        </Card.Body>
      </Card>
    );
  }

  return (
    <div className="bp-branding-page">
      <header className="bp-branding-page__header">
        <div>
          <h1>Branding</h1>
          <p>Estruture a identidade institucional e o login de forma separada para reduzir ruído visual.</p>
        </div>
      </header>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}
      {!canEditBranding ? <Alert variant="warning">Permissao BRANDING_EDIT necessaria para salvar dados basicos de branding.</Alert> : null}
      {!canUploadAssets ? <Alert variant="warning">Permissao BRANDING_UPLOAD_ASSETS necessaria para enviar logo, favicon ou background.</Alert> : null}

      <form className="bp-branding-layout" onSubmit={handleSubmit}>
        <div className="bp-branding-panel">
          <Card className="bp-branding-section">
            <Card.Header>
              <div>
                <strong>Identidade institucional</strong>
                <p>Nome e subtítulo exibidos na plataforma e nas áreas institucionais.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <div className="bp-branding-grid bp-branding-grid--institutional">
                <Input
                  id="branding-name"
                  label="Nome da plataforma"
                  disabled={!canEditBranding}
                  value={draft.nomePlataforma}
                  onChange={(event) => {
                    setDraft((current) => ({ ...current, nomePlataforma: event.target.value }));
                    clearFeedback();
                  }}
                />
                <Input
                  id="branding-subtitle"
                  label="Subtítulo institucional"
                  disabled={!canEditBranding}
                  value={draft.subtituloInstitucional}
                  onChange={(event) => {
                    setDraft((current) => ({ ...current, subtituloInstitucional: event.target.value }));
                    clearFeedback();
                  }}
                />
              </div>
            </Card.Body>
          </Card>

          <Card className="bp-branding-section">
            <Card.Header>
              <div>
                <strong>Login</strong>
                <p>Pré-visualização e configurações visuais que moldam a autenticação da plataforma.</p>
              </div>
              <Badge variant={draft.tema === 'dark' ? 'secondary' : 'primary'}>
                {draft.tema === 'dark' ? 'Escuro' : 'Claro'}
              </Badge>
            </Card.Header>
            <Card.Body>
              <div className="bp-branding-login-layout">
                <div
                  className="bp-branding-login-preview"
                  data-density={draft.densidadeVisual}
                  data-login-background={draft.loginBackgroundMode}
                  data-theme={draft.tema}
                  style={previewStyle}
                >
                  <div className="bp-branding-login-preview__frame">
                    <div className="bp-branding-login-preview__backdrop" aria-hidden="true" />
                    <div className="bp-branding-login-preview__grid" aria-hidden="true" />
                    <div className="bp-branding-login-preview__chrome">
                      <div className="bp-branding-login-preview__chrome-brand">
                        <BrandingLogo
                          className="bp-branding-login-preview__chrome-logo"
                          logoUrl={loginLogoPreviewUrl}
                          assetVersion={assetVersion}
                          name={loginPreviewName}
                          size="sm"
                          showText={false}
                        />
                        <div>
                          <strong>{loginPreviewName}</strong>
                          <span>{loginPreviewSubtitle}</span>
                        </div>
                      </div>
                      <Badge variant={draft.tema === 'dark' ? 'secondary' : 'primary'}>Ambiente seguro</Badge>
                    </div>
                    <div className="bp-branding-login-preview__hero">
                      <div className="bp-branding-login-preview__copy">
                        <div className="bp-branding-login-preview__copy-head">
                          <Badge variant={draft.tema === 'dark' ? 'secondary' : 'primary'}>Autenticação corporativa</Badge>
                          <span>Preview ativo</span>
                        </div>
                        <strong>{loginPreviewName}</strong>
                        <span>{loginPreviewSubtitle}</span>
                        <p>Uma experiência de acesso com contraste, hierarquia visual e presença de produto SaaS enterprise.</p>
                        <div className="bp-branding-login-preview__trust" aria-label="Recursos de autenticação em destaque">
                          <span>SSO</span>
                          <span>Auditoria</span>
                          <span>Criptografia</span>
                        </div>
                      </div>
                      <div className="bp-branding-login-preview__form">
                        <div className="bp-branding-login-preview__form-header">
                          <span>Entrar</span>
                          <strong>Acesso seguro</strong>
                        </div>
                        <div className="bp-branding-login-preview__form-body">
                          <Input id="login-email-preview" label="Email" placeholder="usuario@empresa.com" />
                          <Input id="login-password-preview" label="Senha" type="password" placeholder="••••••••" />
                        </div>
                        <div className="bp-branding-login-preview__form-footer">
                          <Button type="button" size={draft.densidadeVisual === 'compact' ? 'sm' : 'md'}>
                            Entrar
                          </Button>
                          <span>Recuperação e SSO entram aqui em versões futuras.</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bp-branding-login-settings">
          <Card className="bp-branding-subsection">
            <Card.Header>
              <div>
                <strong>Logo</strong>
                <p>Reserva visual para a marca institucional no login.</p>
                      </div>
                    </Card.Header>
            <Card.Body>
              <div className="bp-branding-logo-preview">
                <div className="bp-branding-logo-preview__slot" aria-label="Prévia da logo institucional">
                  <BrandingLogo
                    className="bp-branding-logo-preview__brand"
                    logoUrl={draft.logoUrl}
                    assetVersion={assetVersion}
                    name={draft.nomePlataforma}
                    subtitle={draft.subtituloInstitucional}
                    size="md"
                    showText
                    showSubtitle
                  />
                </div>
                {draft.logoUrl ? (
                  <div className="bp-branding-logo-preview__empty bp-branding-logo-preview__empty--success">
                    <strong>Logo institucional ativa</strong>
                    <span>A marca enviada aparece no preview, no login real e no favicon dinâmico.</span>
                  </div>
                ) : (
                  <div className="bp-branding-logo-preview__empty">
                    <strong>Sem logo enviada</strong>
                    <span>O espaço acima funciona como fallback com iniciais e nome da plataforma.</span>
                  </div>
                )}
              </div>

              <input
                ref={logoUploadRef}
                accept="image/png,image/jpeg"
                className="bp-branding-file-input"
                type="file"
                onChange={handleLogoUpload}
              />

              <div className="bp-branding-asset-actions bp-action-group">
                {canUploadAssets ? (
                  <ActionIconButton
                    disabled={uploadingLogo}
                    icon={Upload}
                    label="Substituir logo"
                    title={uploadingLogo ? 'Enviando logo...' : 'Substituir logo'}
                    variant="primary"
                    onClick={triggerLogoUpload}
                  />
                ) : null}
                {canEditBranding ? (
                  <ActionIconButton
                    disabled={!draft.logoUrl || uploadingLogo}
                    icon={Trash2}
                    label="Limpar logo"
                    title="Limpar logo"
                    variant="danger"
                    onClick={handleClearLogo}
                  />
                ) : null}
              </div>

              <p className="bp-branding-asset-hint">
                PNG, JPG ou JPEG. A logo enviada também atualiza o favicon enquanto não houver favicon dedicado.
              </p>

              <div className="bp-branding-ghost-grid bp-branding-ghost-grid--compact bp-branding-ghost-grid--background">
                <div className="bp-branding-ghost-card">
                  <strong>Upload futuro</strong>
                          <span>Estrutura preparada para envio real da logo sem alterar o layout.</span>
                        </div>
                        <div className="bp-branding-ghost-card">
                          <strong>Marca reduzida</strong>
                          <span>Espaço para versão simplificada da identidade em telas compactas.</span>
                        </div>
              </div>
            </Card.Body>
          </Card>

          <Card className="bp-branding-subsection">
            <Card.Header>
              <div>
                <strong>Marca reduzida</strong>
                <p>Versão compacta da identidade para sidebar recolhida, topbar compacta e áreas densas.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <div className="bp-branding-logo-preview">
                <div className="bp-branding-logo-preview__slot" aria-label="Prévia da marca reduzida">
                  <BrandingLogo
                    className="bp-branding-logo-preview__brand"
                    logoUrl={draft.compactLogoUrl}
                    assetVersion={assetVersion}
                    name={draft.nomePlataforma}
                    subtitle={draft.subtituloInstitucional}
                    size="md"
                    showText
                    showSubtitle
                    variant="mark"
                  />
                </div>
                {draft.compactLogoUrl ? (
                  <div className="bp-branding-logo-preview__empty bp-branding-logo-preview__empty--success">
                    <strong>Marca reduzida ativa</strong>
                    <span>A versão compacta aparece em navegação recolhida e superfícies compactas.</span>
                  </div>
                ) : (
                  <div className="bp-branding-logo-preview__empty">
                    <strong>Sem marca reduzida enviada</strong>
                    <span>O fallback usa iniciais enquanto não houver asset dedicado.</span>
                  </div>
                )}
              </div>

              <input
                ref={compactLogoUploadRef}
                accept="image/png,image/jpeg"
                className="bp-branding-file-input"
                type="file"
                onChange={handleCompactLogoUpload}
              />

              <div className="bp-branding-asset-actions bp-action-group">
                {canUploadAssets ? (
                  <ActionIconButton
                    disabled={uploadingCompactLogo}
                    icon={Upload}
                    label="Substituir marca reduzida"
                    title={uploadingCompactLogo ? 'Enviando marca reduzida...' : 'Substituir marca reduzida'}
                    variant="primary"
                    onClick={triggerCompactLogoUpload}
                  />
                ) : null}
              </div>

              <p className="bp-branding-asset-hint">
                PNG, JPG ou JPEG. A marca reduzida é usada pela sidebar recolhida e pela topbar compacta.
              </p>
            </Card.Body>
          </Card>

          <Card className="bp-branding-subsection">
            <Card.Header>
              <div>
                <strong>Logo do login</strong>
                <p>Marca dedicada para a autenticação, com fallback para a logo institucional.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <div className="bp-branding-logo-preview">
                <div className="bp-branding-logo-preview__slot" aria-label="Prévia da logo do login">
                  <BrandingLogo
                    className="bp-branding-logo-preview__brand"
                    logoUrl={loginLogoPreviewUrl}
                    assetVersion={assetVersion}
                    name={loginPreviewName}
                    subtitle={loginPreviewSubtitle}
                    size="md"
                    showText
                    showSubtitle
                  />
                </div>
                <div className={['bp-branding-logo-preview__empty', draft.loginLogoUrl ? 'bp-branding-logo-preview__empty--success' : '']
                  .filter(Boolean)
                  .join(' ')}
                >
                  <strong>{draft.loginLogoUrl ? 'Logo do login ativa' : 'Usando logo institucional'}</strong>
                  <span>{draft.loginLogoUrl ? 'O login usa uma marca dedicada.' : 'Envie uma logo para personalizar apenas a tela de acesso.'}</span>
                </div>
              </div>

              <input
                ref={loginLogoUploadRef}
                accept="image/png,image/jpeg"
                className="bp-branding-file-input"
                type="file"
                onChange={handleLoginLogoUpload}
              />

              <div className="bp-branding-asset-actions bp-action-group">
                {canUploadAssets ? (
                  <ActionIconButton
                    disabled={uploadingLoginLogo}
                    icon={Upload}
                    label="Substituir logo do login"
                    title={uploadingLoginLogo ? 'Enviando logo do login...' : 'Substituir logo do login'}
                    variant="primary"
                    onClick={triggerLoginLogoUpload}
                  />
                ) : null}
                <Badge variant={draft.loginLogoUrl ? 'primary' : 'secondary'}>
                  {draft.loginLogoUrl ? 'Dedicada' : 'Fallback ativo'}
                </Badge>
              </div>

              <p className="bp-branding-asset-hint">
                PNG, JPG ou JPEG. Use quando o login precisar de composição diferente da navegação interna.
              </p>
            </Card.Body>
          </Card>

          <Card className="bp-branding-subsection">
            <Card.Header>
              <div>
                <strong>Favicon</strong>
                <p>O navegador usa a logo institucional até existir um favicon dedicado.</p>
              </div>
            </Card.Header>
            <Card.Body>
              <div className="bp-branding-favicon-preview">
                <div className="bp-branding-favicon-preview__slot">
                  <BrandingLogo
                    className="bp-branding-favicon-preview__brand"
                    logoUrl={draft.faviconUrl || draft.logoUrl}
                    assetVersion={assetVersion}
                    name={draft.nomePlataforma}
                    size="sm"
                    showSubtitle={false}
                    showText={false}
                    variant="mark"
                  />
                  <div className="bp-branding-favicon-preview__copy">
                    <strong>Favicon atual</strong>
                    <span>
                      {draft.faviconUrl
                        ? 'Usa o favicon dedicado enviado.'
                        : draft.logoUrl
                          ? 'Derivado da logo institucional enquanto não houver favicon dedicado.'
                          : 'Fallback Base+ enquanto não houver asset enviado.'}
                    </span>
                  </div>
                </div>
                <Badge variant={draft.faviconUrl ? 'primary' : draft.logoUrl ? 'secondary' : 'secondary'}>
                  {draft.faviconUrl ? 'Favicon dedicado' : draft.logoUrl ? 'Derivado da logo' : 'Fallback ativo'}
                </Badge>
              </div>

              <input
                ref={faviconUploadRef}
                accept="image/png,image/jpeg,image/x-icon,.ico"
                className="bp-branding-file-input"
                type="file"
                onChange={handleFaviconUpload}
              />

              <div className="bp-branding-asset-actions bp-action-group">
                {canUploadAssets ? (
                  <ActionIconButton
                    disabled={uploadingFavicon}
                    icon={Upload}
                    label="Substituir favicon"
                    title={uploadingFavicon ? 'Enviando favicon...' : 'Substituir favicon'}
                    variant="primary"
                    onClick={triggerFaviconUpload}
                  />
                ) : null}
              </div>

              <div className="bp-branding-ghost-grid bp-branding-ghost-grid--compact bp-branding-ghost-grid--background">
                <div className="bp-branding-ghost-card">
                  <strong>Atualização dinâmica</strong>
                  <span>Quando favicon ou logo mudam, o favicon do navegador acompanha automaticamente.</span>
                </div>
                <div className="bp-branding-ghost-card">
                  <strong>Upload dedicado</strong>
                  <span>Estrutura preparada para um favicon específico no futuro.</span>
                </div>
              </div>
            </Card.Body>
          </Card>

          <Card className="bp-branding-subsection">
            <Card.Header>
              <div>
                <strong>Visual</strong>
                        <p>Estrutura do layout e composição da tela de autenticação.</p>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <div className="bp-branding-ghost-grid bp-branding-ghost-grid--compact">
                        <div className="bp-branding-ghost-card">
                          <strong>Hero institucional</strong>
                          <span>Área principal da tela de login, com marca e mensagem.</span>
                        </div>
                        <div className="bp-branding-ghost-card">
                          <strong>Viewport</strong>
                          <span>Base para fundo, imagem e variações por dispositivo.</span>
                        </div>
                      </div>
                    </Card.Body>
                  </Card>

                  <Card className="bp-branding-subsection">
                    <Card.Header>
                      <div>
                        <strong>Background</strong>
                        <p>Define a atmosfera visual da viewport de autenticação.</p>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <div className="bp-branding-background-options" role="radiogroup" aria-label="Background do login">
                        {loginBackgroundOptions.map((option) => {
                          const active = draft.loginBackgroundMode === option.value;

                          return (
                            <button
                              key={option.value}
                              disabled={!canEditBranding}
                              className={['bp-branding-background-option', active ? 'bp-branding-background-option--active' : '']
                                .filter(Boolean)
                                .join(' ')}
                              type="button"
                              aria-pressed={active}
                              onClick={() => {
                                if (!canEditBranding) {
                                  setError('Permissao BRANDING_EDIT necessaria para alterar o background.');
                                  return;
                                }

                                setDraft((current) => ({ ...current, loginBackgroundMode: option.value }));
                                clearFeedback();
                              }}
                            >
                              <strong>{option.label}</strong>
                              <span>{option.description}</span>
                            </button>
                          );
                        })}
                      </div>

                      <input
                        ref={loginBackgroundUploadRef}
                        accept="image/png,image/jpeg"
                        className="bp-branding-file-input"
                        type="file"
                        onChange={handleLoginBackgroundUpload}
                      />

                      <div className="bp-branding-asset-actions bp-action-group">
                        {canUploadAssets ? (
                          <ActionIconButton
                            disabled={uploadingLoginBackground}
                            icon={Upload}
                            label="Substituir background"
                            title={uploadingLoginBackground ? 'Enviando background...' : 'Substituir background'}
                            variant="primary"
                            onClick={triggerLoginBackgroundUpload}
                          />
                        ) : null}
                        <Badge variant={draft.loginBackgroundUrl ? 'primary' : 'secondary'}>
                          {draft.loginBackgroundUrl ? 'Imagem ativa' : 'Sem imagem dedicada'}
                        </Badge>
                      </div>

                      <div className="bp-branding-ghost-grid bp-branding-ghost-grid--compact bp-branding-ghost-grid--background">
                        <div
                          className={['bp-branding-background-preview', draft.loginBackgroundUrl ? 'bp-branding-background-preview--active' : '']
                            .filter(Boolean)
                            .join(' ')}
                          style={backgroundPreviewUrl ? { '--background-preview-image': `url("${backgroundPreviewUrl}")` } : undefined}
                        >
                          <strong>Imagem institucional</strong>
                          <span>{draft.loginBackgroundUrl ? 'Background dedicado publicado no login.' : 'Use upload de assets para enviar um fundo personalizado.'}</span>
                        </div>
                        <div className="bp-branding-ghost-card">
                          <strong>Surface layer</strong>
                          <span>Camada neutra para cenários com menos ruído visual.</span>
                        </div>
                      </div>
                    </Card.Body>
                  </Card>

                  <Card className="bp-branding-subsection">
                    <Card.Header>
                      <div>
                        <strong>Tema</strong>
                        <p>Variantes visuais que moldam o contraste e a atmosfera do login.</p>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <div className="bp-branding-presets">
                        {brandingPresets.map((preset) => {
                          const active = isPresetActive(draft, preset);

                          return (
                            <button
                              key={preset.id}
                              aria-pressed={active}
                              disabled={!canEditBranding}
                              className={['bp-branding-preset', active ? 'bp-branding-preset--active' : '']
                                .filter(Boolean)
                                .join(' ')}
                              type="button"
                              onClick={() => applyPreset(preset)}
                              style={{
                                '--preset-primary': preset.corPrimaria,
                                '--preset-secondary': preset.corSecundaria,
                              }}
                            >
                              <span className="bp-branding-preset__swatches" aria-hidden="true">
                                <span className="bp-branding-preset__swatch bp-branding-preset__swatch--primary" />
                                <span className="bp-branding-preset__swatch bp-branding-preset__swatch--secondary" />
                              </span>
                              <span className="bp-branding-preset__name">{preset.name}</span>
                              <span className="bp-branding-preset__meta">
                                {preset.tema === 'dark' ? 'Tema escuro' : 'Tema claro'}
                              </span>
                            </button>
                          );
                        })}
                      </div>

                      <div className="bp-branding-login-controls">
                        <Select
                          id="branding-theme"
                          label="Tema padrão"
                          disabled={!canEditBranding}
                          options={themeOptions}
                          value={draft.tema}
                          onChange={(event) => {
                            setDraft((current) => ({ ...current, tema: event.target.value }));
                            clearFeedback();
                          }}
                        />
                        <Select
                          id="branding-density"
                          label="Densidade visual"
                          disabled={!canEditBranding}
                          options={densityOptions}
                          value={draft.densidadeVisual}
                          onChange={(event) => {
                            setDraft((current) => ({ ...current, densidadeVisual: event.target.value }));
                            clearFeedback();
                          }}
                        />
                      </div>
                    </Card.Body>
                  </Card>

                  <Card className="bp-branding-subsection">
                    <Card.Header>
                      <div>
                        <strong>Cores</strong>
                        <p>Aplicação de marca em tempo real para a tela de autenticação.</p>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <div className="bp-branding-colors">
                        <BrandingColorPicker
                          id="branding-primary"
                          label="Cor primária"
                          value={draft.corPrimaria}
                          onChange={(nextColor) => {
                            setDraft((current) => ({ ...current, corPrimaria: nextColor }));
                            clearFeedback();
                          }}
                          onValidityChange={(valid) => {
                            setColorValidity((current) => ({ ...current, corPrimaria: valid }));
                          }}
                        />
                        <BrandingColorPicker
                          id="branding-secondary"
                          label="Cor secundária"
                          value={draft.corSecundaria}
                          onChange={(nextColor) => {
                            setDraft((current) => ({ ...current, corSecundaria: nextColor }));
                            clearFeedback();
                          }}
                          onValidityChange={(valid) => {
                            setColorValidity((current) => ({ ...current, corSecundaria: valid }));
                          }}
                        />
                      </div>
                    </Card.Body>
                  </Card>
                  <Card className="bp-branding-subsection">
                    <Card.Header>
                      <div>
                        <strong>White-label</strong>
                        <p>Personalize o nome e subtitulo exibidos apenas no login.</p>
                      </div>
                    </Card.Header>
                    <Card.Body>
                      <div className="bp-branding-white-label">
                        <label className="bp-branding-switch">
                          <input
                            checked={Boolean(draft.whiteLabelEnabled)}
                            disabled={!canEditBranding}
                            type="checkbox"
                            onChange={(event) => {
                              setDraft((current) => ({ ...current, whiteLabelEnabled: event.target.checked }));
                              clearFeedback();
                            }}
                          />
                          <span aria-hidden="true" />
                          <strong>Ativar white-label</strong>
                        </label>

                        <div className="bp-branding-login-controls">
                          <Input
                            id="branding-white-label-name"
                            label="Nome exibido no login"
                            disabled={!canEditBranding || !draft.whiteLabelEnabled}
                            value={draft.whiteLabelName}
                            placeholder={draft.nomePlataforma}
                            onChange={(event) => {
                              setDraft((current) => ({ ...current, whiteLabelName: event.target.value }));
                              clearFeedback();
                            }}
                          />
                          <Input
                            id="branding-white-label-subtitle"
                            label="Subtitulo exibido no login"
                            disabled={!canEditBranding || !draft.whiteLabelEnabled}
                            value={draft.whiteLabelSubtitle}
                            placeholder={draft.subtituloInstitucional}
                            onChange={(event) => {
                              setDraft((current) => ({ ...current, whiteLabelSubtitle: event.target.value }));
                              clearFeedback();
                            }}
                          />
                        </div>

                        <div className="bp-branding-logo-preview__empty">
                          <strong>{draft.whiteLabelEnabled ? 'White-label ativo no preview' : 'White-label inativo'}</strong>
                          <span>
                            {draft.whiteLabelEnabled
                              ? 'O login usa os textos acima; o restante da plataforma mantem a identidade global.'
                              : 'Quando inativo, o login usa nome e subtitulo institucionais globais.'}
                          </span>
                        </div>
                      </div>
                    </Card.Body>
                  </Card>
                </div>
              </div>
            </Card.Body>
            <Card.Footer>
              <div className="bp-branding-actions">
                {canEditBranding ? (
                  <Button type="submit" disabled={saving || !canSave}>
                    {saving ? 'Salvando...' : 'Salvar branding'}
                  </Button>
                ) : null}
              </div>
            </Card.Footer>
          </Card>
        </div>
      </form>
    </div>
  );
}
