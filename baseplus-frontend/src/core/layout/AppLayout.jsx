import { useEffect, useMemo, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { useTheme } from '../theme/useTheme.js';
import { Sidebar } from './Sidebar.jsx';
import { Topbar } from './Topbar.jsx';
import './layout.css';

const SIDEBAR_COLLAPSED_KEY = 'baseplus.sidebar.collapsed';

export function AppLayout() {
  const { menuPrincipal } = useTheme();
  const [collapsed, setCollapsed] = useState(() => {
    if (typeof window === 'undefined') {
      return true;
    }

    const storedValue = window.localStorage.getItem(SIDEBAR_COLLAPSED_KEY);
    return storedValue === null ? true : storedValue === 'true';
  });

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    window.localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(collapsed));
    document.documentElement.dataset.sidebar = collapsed ? 'collapsed' : 'expanded';
  }, [collapsed]);

  const showSidebar = menuPrincipal === 'sidebar';
  const sidebarWidth = collapsed ? '72px' : '260px';
  const layoutClassName = useMemo(() => {
    return [
      'bp-app-layout',
      showSidebar && collapsed ? 'bp-app-layout--collapsed' : '',
      showSidebar ? 'bp-app-layout--navigation-sidebar' : 'bp-app-layout--navigation-topbar',
    ]
      .filter(Boolean)
      .join(' ');
  }, [collapsed, showSidebar]);

  return (
    <div className={layoutClassName} style={showSidebar ? { '--sidebar-width': sidebarWidth } : undefined}>
      {showSidebar ? <Sidebar collapsed={collapsed} menuPrincipal={menuPrincipal} onToggleCollapsed={() => setCollapsed((current) => !current)} /> : null}
      <div className="bp-app-layout__content">
        <Topbar menuPrincipal={menuPrincipal} />
        <main className="bp-app-layout__main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
