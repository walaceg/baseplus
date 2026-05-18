import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, ChevronDown } from 'lucide-react';
import { BrandingLogo, Button } from '../../shared/components/index.js';
import { useBranding } from '../../shared/branding/useBranding.js';
import { useAuth } from '../auth/useAuth.js';
import { useTheme } from '../theme/useTheme.js';
import { AccountMenu } from './AccountMenu.jsx';
import { NavItem } from './NavItem.jsx';
import { getAuthorizedNavigationGroups } from './navigation.js';
import { useAuthorization } from '../auth/useAuthorization.js';

export function Topbar({ menuPrincipal = 'sidebar' }) {
  const { signOut, user } = useAuth();
  const { hasPermission } = useAuthorization();
  const { branding, assetVersion } = useBranding();
  const { menuPrincipal: activeMenuPrincipal, setMenuPrincipal } = useTheme();
  const navigate = useNavigate();
  const adminMenuRef = useRef(null);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [adminMenuOpen, setAdminMenuOpen] = useState(false);
  const showPrimaryNavigation = menuPrincipal === 'topbar';
  const compactBrandLogoUrl = branding.compactLogoUrl || branding.logoUrl;
  const navigationGroups = useMemo(() => getAuthorizedNavigationGroups(hasPermission), [hasPermission]);

  useEffect(() => {
    function handleClickOutside(event) {
      const clickedInsideAdminMenu = adminMenuRef.current && adminMenuRef.current.contains(event.target);

      if (!clickedInsideAdminMenu) {
        setAdminMenuOpen(false);
      }
    }

    function handleEscape(event) {
      if (event.key === 'Escape') {
        setAdminMenuOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  async function handleSignOut() {
    setUserMenuOpen(false);
    setAdminMenuOpen(false);
    await signOut();
  }

  function goToAccount() {
    setUserMenuOpen(false);
    navigate('/app/conta');
  }

  function changeMenuPrincipal(mode) {
    setMenuPrincipal(mode);
    setUserMenuOpen(false);
    setAdminMenuOpen(false);
  }

  function toggleAdminMenu() {
    setUserMenuOpen(false);
    setAdminMenuOpen((current) => !current);
  }

  function closeAdminMenu() {
    setAdminMenuOpen(false);
  }

  return (
    <header className={['bp-topbar', showPrimaryNavigation ? 'bp-topbar--navigation' : ''].filter(Boolean).join(' ')}>
      <div className="bp-topbar__header-row">
        <div className="bp-topbar__left">
          <BrandingLogo
            className="bp-topbar__brand-logo"
            logoUrl={compactBrandLogoUrl}
            assetVersion={assetVersion}
            name={branding.nomePlataforma}
            subtitle={branding.subtituloInstitucional}
            showSubtitle={false}
            showText={false}
            size="sm"
            variant="mark"
          />
          <div className="bp-topbar__context">
            <span className="bp-topbar__title">
              <span>{branding.nomePlataforma}</span>
            </span>
            <span className="bp-topbar__subtitle">{branding.subtituloInstitucional}</span>
          </div>

          {showPrimaryNavigation ? (
            <div className="bp-topbar__admin" ref={adminMenuRef}>
              <Button
                aria-expanded={adminMenuOpen}
                aria-haspopup="menu"
                className="bp-topbar__admin-trigger"
                variant="ghost"
                onClick={toggleAdminMenu}
              >
                <span className="bp-topbar__admin-trigger-icon" aria-hidden="true">
                  <Building2 size={16} strokeWidth={2.2} />
                </span>
                <span className="bp-topbar__admin-trigger-label">Administração</span>
                <ChevronDown className="bp-topbar__chevron" size={14} aria-hidden="true" />
              </Button>

              {adminMenuOpen ? (
                <div className="bp-topbar__admin-menu" role="menu" aria-label="Administração">
                  {navigationGroups.map((group) => (
                    <section className="bp-topbar__admin-section" key={group.title}>
                      <p className="bp-topbar__admin-section-title">
                        <span className="bp-topbar__admin-section-icon" aria-hidden="true">
                          <group.icon size={12} strokeWidth={2.2} />
                        </span>
                        <span>{group.title}</span>
                      </p>
                      <div className="bp-topbar__admin-items">
                        {group.items.map((item) => (
                          <NavItem
                            key={item.to}
                            icon={item.icon}
                            label={item.label}
                            to={item.to}
                            variant="topbar"
                            onClick={closeAdminMenu}
                          />
                        ))}
                      </div>
                    </section>
                  ))}
                </div>
              ) : null}
            </div>
          ) : null}
        </div>

        <AccountMenu
          activeMenuPrincipal={activeMenuPrincipal}
          onChangeMenuPrincipal={changeMenuPrincipal}
          onOpenChange={(nextOpen) => {
            setUserMenuOpen(nextOpen);
            if (nextOpen) {
              setAdminMenuOpen(false);
            }
          }}
          onRequestAccount={goToAccount}
          onRequestSignOut={handleSignOut}
          open={userMenuOpen}
          user={user}
        />
      </div>
    </header>
  );
}
