import { useContext } from 'react';
import { BrandingContext } from './BrandingContext.js';

export function useBranding() {
  const context = useContext(BrandingContext);

  if (!context) {
    throw new Error('useBranding deve ser usado dentro de BrandingProvider.');
  }

  return context;
}
