export function getJwtPayload(token) {
  if (!token) {
    return null;
  }

  const parts = token.split('.');
  if (parts.length < 2) {
    return null;
  }

  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getJwtRoles(token) {
  const payload = getJwtPayload(token);
  return Array.isArray(payload?.roles) ? payload.roles : [];
}

export function getJwtPermissions(token) {
  const payload = getJwtPayload(token);
  return Array.isArray(payload?.permissions) ? payload.permissions : [];
}
