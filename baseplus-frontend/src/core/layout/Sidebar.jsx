import { useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { ChevronDown, ChevronLeft, ChevronRight } from 'lucide-react';
import { BrandingLogo, Button } from '../../shared/components/index.js';
import { useBranding } from '../../shared/branding/useBranding.js';
import { NavItem } from './NavItem.jsx';
import { moduleNavigation, getAuthorizedNavigationGroups } from './navigation.js';
import { useAuthorization } from '../auth/useAuthorization.js';

export function Sidebar({ collapsed = false, menuPrincipal = 'sidebar', onToggleCollapsed }) {
  const location = useLocation();
  const { branding, assetVersion } = useBranding();
  const { hasPermission } = useAuthorization();
  const ModuleIcon = moduleNavigation.icon;
  const [moduleOpen, setModuleOpen] = useState(false);
  const brandLogoUrl = collapsed ? branding.compactLogoUrl || branding.logoUrl : branding.logoUrl;
  const navigationGroups = useMemo(() => getAuthorizedNavigationGroups(hasPermission), [hasPermission]);
  const moduleHasActiveRoute = useMemo(() => {
    return navigationGroups.some((group) => group.items.some((item) => isActivePath(location.pathname, item.to)));
  }, [location.pathname]);
  const moduleActive = moduleHasActiveRoute || moduleOpen;
  const moduleExpanded = moduleOpen;

  if (menuPrincipal !== 'sidebar') {
    return null;
  }

  return (
    <aside className={['bp-sidebar', collapsed ? 'bp-sidebar--collapsed' : ''].filter(Boolean).join(' ')}>
      <div className="bp-sidebar__brand">
        <BrandingLogo
          className="bp-sidebar__brand-logo"
          logoUrl={brandLogoUrl}
          assetVersion={assetVersion}
          name={branding.nomePlataforma}
          subtitle={branding.subtituloInstitucional}
          showSubtitle={!collapsed}
          showText={!collapsed}
          size={collapsed ? 'sm' : 'md'}
          variant="inline"
        />
        <Button
          aria-label={collapsed ? 'Expandir sidebar' : 'Recolher sidebar'}
          className="bp-sidebar__toggle"
          size="sm"
          variant="ghost"
          onClick={onToggleCollapsed}
        >
          {collapsed ? (
            <ChevronRight className="bp-sidebar__toggle-icon" size={16} strokeWidth={2.3} aria-hidden="true" />
          ) : (
            <ChevronLeft className="bp-sidebar__toggle-icon" size={16} strokeWidth={2.3} aria-hidden="true" />
          )}
        </Button>
      </div>

      <nav className="bp-sidebar__nav" aria-label="Navegação principal">
        <NavItem
          active={moduleActive}
          as="button"
          className="bp-sidebar__module"
          collapsed={collapsed}
          expanded={moduleExpanded}
          icon={ModuleIcon}
          label={moduleNavigation.title}
          trailingIcon={moduleExpanded ? ChevronDown : ChevronRight}
          onClick={() => setModuleOpen((current) => !current)}
        />
        {moduleExpanded
          ? navigationGroups.map((group) => (
              <section className={['bp-sidebar__group', collapsed ? 'bp-sidebar__group--compact' : ''].filter(Boolean).join(' ')} key={group.title}>
                <p className="bp-sidebar__group-title">
                  <span className="bp-sidebar__group-icon" aria-hidden="true">
                    <group.icon size={13} strokeWidth={2.2} />
                  </span>
                  <span>{group.title}</span>
                </p>
                <div className="bp-sidebar__group-items">
                  {group.items.map((item) => (
                    <NavItem key={item.to} collapsed={collapsed} icon={item.icon} label={item.label} to={item.to} />
                  ))}
                </div>
              </section>
            ))
          : null}
      </nav>
    </aside>
  );
}

function isActivePath(pathname, to) {
  return pathname === to || pathname.startsWith(`${to}/`);
}
