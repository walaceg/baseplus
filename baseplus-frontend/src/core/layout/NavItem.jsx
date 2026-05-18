import { NavLink } from 'react-router-dom';

export function NavItem({
  active = false,
  as = 'link',
  collapsed = false,
  className = '',
  expanded,
  icon,
  label,
  onClick,
  to,
  trailingIcon,
  variant = 'sidebar',
}) {
  const Icon = icon;
  const TrailingIcon = trailingIcon;
  const baseClassName = ['bp-nav-item', collapsed ? 'bp-nav-item--collapsed' : '', className]
    .filter(Boolean)
    .join(' ');
  const labelClassName = ['bp-nav-item__label', collapsed && variant === 'sidebar' ? 'bp-nav-item__label--hidden' : '']
    .filter(Boolean)
    .join(' ');
  const trailingClassName = 'bp-nav-item__trailing';

  function renderContent() {
    return (
      <>
        <span className="bp-nav-item__icon" aria-hidden="true">
          <Icon size={16} strokeWidth={2.1} />
        </span>
        <span className={labelClassName}>{label}</span>
        {TrailingIcon ? (
          <span className={trailingClassName} aria-hidden="true">
            <TrailingIcon size={14} strokeWidth={2.2} />
          </span>
        ) : null}
      </>
    );
  }

  if (as === 'button') {
    return (
      <button
        aria-expanded={typeof expanded === 'boolean' ? expanded : undefined}
        aria-label={collapsed ? label : undefined}
        className={['bp-nav-item', 'bp-nav-item--button', active ? 'bp-nav-item--active' : '', collapsed ? 'bp-nav-item--collapsed' : '', className]
          .filter(Boolean)
          .join(' ')}
        type="button"
        onClick={onClick}
      >
        {renderContent()}
      </button>
    );
  }

  return (
    <NavLink
      className={({ isActive }) =>
        [baseClassName, isActive || active ? 'bp-nav-item--active' : '', variant === 'topbar' ? 'bp-nav-item--topbar' : 'bp-nav-item--sidebar']
          .filter(Boolean)
          .join(' ')
      }
      aria-label={collapsed && variant === 'sidebar' ? label : undefined}
      title={collapsed && variant === 'sidebar' ? label : undefined}
      onClick={onClick}
      to={to}
    >
      {renderContent()}
    </NavLink>
  );
}
