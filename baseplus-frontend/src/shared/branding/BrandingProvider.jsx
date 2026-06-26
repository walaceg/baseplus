import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  clearBrandingRuntime,
  setBrandingRuntime,
} from '../../core/theme/themeResolution.js';
import { getBranding, getPublicBranding } from '../../modules/branding/brandingService.js';
import { getJwtPermissions, getJwtRoles } from '../auth/jwt.js';
import { tokenStorage } from '../storage/tokenStorage.js';
import { BrandingContext } from './BrandingContext.js';
import {
  applyBrandingFavicon,
  applyBrandingSettings,
  defaultBranding,
  normalizeBrandingSettings,
  resetBrandingFavicon,
  resetBrandingSettings,
} from './branding.js';

export function BrandingProvider({ children }) {
  const [branding, setBranding] = useState(() => defaultBranding);
  const [isLoading, setIsLoading] = useState(true);
  const [assetVersion, setAssetVersion] = useState(() => Date.now());

  const refreshBranding = useCallback(async () => {
    const loadBranding = canReadBranding() ? getBranding : getPublicBranding;

    try {
      const nextBranding = normalizeBrandingSettings(await loadBranding());
      setBranding(nextBranding);
      setBrandingRuntime({
        density: nextBranding.densidadeVisual,
        theme: nextBranding.tema,
      });
      setAssetVersion(Date.now());
      return nextBranding;
    } catch {
      setBranding(defaultBranding);
      setBrandingRuntime({
        density: defaultBranding.densidadeVisual,
        theme: defaultBranding.tema,
      });
      setAssetVersion(Date.now());
      return defaultBranding;
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshBranding();
  }, [refreshBranding]);

  useEffect(() => {
    applyBrandingSettings(branding);
    applyBrandingFavicon({ ...branding, assetVersion });
  }, [assetVersion, branding]);

  useEffect(() => {
    return () => {
      clearBrandingRuntime();
      resetBrandingSettings();
      resetBrandingFavicon();
    };
  }, []);

  const value = useMemo(
    () => ({
      branding,
      assetVersion,
      isLoading,
      refreshBranding,
    }),
    [assetVersion, branding, isLoading, refreshBranding],
  );

  return <BrandingContext.Provider value={value}>{children}</BrandingContext.Provider>;
}

function canReadBranding() {
  const accessToken = tokenStorage.getAccessToken();
  if (!accessToken) {
    return false;
  }

  return getJwtRoles(accessToken).includes('ADMIN') || getJwtPermissions(accessToken).includes('BRANDING_VIEW');
}
