import { useEffect, useMemo, useRef } from 'react';
import { Check, LogOut, PanelLeft, PanelTop, UserRound } from 'lucide-react';
import { Avatar, Button } from '../../shared/components/index.js';

function getGreeting() {
  const hour = new Date().getHours();

  if (hour < 12) {
    return 'Bom dia';
  }

  if (hour < 18) {
    return 'Boa tarde';
  }

  return 'Boa noite';
}

function getDisplayName(user) {
  return user?.nome?.trim() || user?.email?.trim() || 'Usuário';
}

function getDisplayEmail(user) {
  return user?.email?.trim() || '';
}

export function AccountMenu({
  activeMenuPrincipal = 'sidebar',
  onChangeMenuPrincipal,
  onOpenChange,
  onRequestAccount,
  onRequestSignOut,
  open = false,
  user,
}) {
  const menuRef = useRef(null);
  const userName = useMemo(() => getDisplayName(user), [user]);
  const userEmail = useMemo(() => getDisplayEmail(user), [user]);
  const greeting = useMemo(() => getGreeting(), []);
  const avatarVersion = user?.avatarVersion ?? user?.avatarUpdatedAt ?? 0;

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    function handlePointerDown(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        onOpenChange(false);
      }
    }

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        onOpenChange(false);
      }
    }

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [onOpenChange, open]);

  function handleAccount() {
    onRequestAccount();
    onOpenChange(false);
  }

  function handleSignOut() {
    onRequestSignOut();
    onOpenChange(false);
  }

  function handleMenuPrincipal(nextMenuPrincipal) {
    onChangeMenuPrincipal(nextMenuPrincipal);
    onOpenChange(false);
  }

  return (
    <div className="bp-account-menu" ref={menuRef}>
      <Button
        aria-controls="bp-account-menu-panel"
        aria-expanded={open}
        aria-haspopup="menu"
        className="bp-account-menu__trigger"
        aria-label={`Abrir menu da conta de ${userName}`}
        variant="ghost"
        onClick={() => onOpenChange(!open)}
      >
        <Avatar
          alt={userName}
          className="bp-account-menu__trigger-avatar"
          name={userName}
          showStatus
          size="md"
          src={user?.avatarUrl}
          version={avatarVersion}
          variant="premium"
        />
      </Button>

      {open ? (
        <div className="bp-account-menu__panel" id="bp-account-menu-panel" role="menu" aria-label="Menu da conta">
          <div className="bp-account-menu__hero">
            <Avatar
              alt={userName}
              className="bp-account-menu__hero-avatar"
              name={userName}
              showStatus
              size="lg"
              src={user?.avatarUrl}
              version={avatarVersion}
              variant="premium"
            />
            <div className="bp-account-menu__hero-copy">
              <span className="bp-account-menu__greeting">{greeting}</span>
              <strong className="bp-account-menu__name">{userName}</strong>
              <span className="bp-account-menu__email">{userEmail || 'Sem email cadastrado'}</span>
            </div>
          </div>

          <div className="bp-account-menu__section bp-account-menu__section--primary">
            <Button className="bp-account-menu__primary-action" onClick={handleAccount}>
              <UserRound size={16} strokeWidth={2.1} />
              <span>Minha conta</span>
            </Button>
          </div>

          <div className="bp-account-menu__section">
            <p className="bp-account-menu__section-label">Navegação</p>
            <div className="bp-account-menu__options">
              <button
                className={[
                  'bp-account-menu__option',
                  activeMenuPrincipal === 'sidebar' ? 'bp-account-menu__option--active' : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                role="menuitemradio"
                aria-checked={activeMenuPrincipal === 'sidebar'}
                type="button"
                onClick={() => handleMenuPrincipal('sidebar')}
              >
                <span className="bp-account-menu__option-icon" aria-hidden="true">
                  <PanelLeft size={16} strokeWidth={2.1} />
                </span>
                <span className="bp-account-menu__option-copy">
                  <span className="bp-account-menu__option-title">Menu lateral</span>
                  <span className="bp-account-menu__option-description">Navegação fixa à esquerda</span>
                </span>
                {activeMenuPrincipal === 'sidebar' ? (
                  <Check className="bp-account-menu__option-check" size={14} aria-hidden="true" />
                ) : null}
              </button>

              <button
                className={[
                  'bp-account-menu__option',
                  activeMenuPrincipal === 'topbar' ? 'bp-account-menu__option--active' : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                role="menuitemradio"
                aria-checked={activeMenuPrincipal === 'topbar'}
                type="button"
                onClick={() => handleMenuPrincipal('topbar')}
              >
                <span className="bp-account-menu__option-icon" aria-hidden="true">
                  <PanelTop size={16} strokeWidth={2.1} />
                </span>
                <span className="bp-account-menu__option-copy">
                  <span className="bp-account-menu__option-title">Menu superior</span>
                  <span className="bp-account-menu__option-description">Navegação concentrada no topo</span>
                </span>
                {activeMenuPrincipal === 'topbar' ? (
                  <Check className="bp-account-menu__option-check" size={14} aria-hidden="true" />
                ) : null}
              </button>
            </div>
          </div>

          <div className="bp-account-menu__section bp-account-menu__section--logout">
            <Button className="bp-account-menu__logout" variant="ghost" onClick={handleSignOut}>
              <LogOut size={16} strokeWidth={2.1} />
              <span>Sair</span>
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
