/**
 * Sistema de Tipografía IPROSS
 * Usando fuentes del sistema (fallback temporal)
 * TODO: Cargar Figtree con expo-font
 */

export const fontFamily = {
  light: 'System',
  regular: 'System',
  medium: 'System',
  semibold: 'System',
  bold: 'System',
  extrabold: 'System',
  black: 'System',
};

export const fontSize = {
  xs: 12,
  sm: 14,
  md: 16,
  lg: 18,
  xl: 20,
  xxl: 24,
  xxxl: 32,
};

export const fontWeight = {
  light: '300',
  regular: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
  extrabold: '800',
  black: '900',
};

export const lineHeight = {
  tight: 1.2,
  normal: 1.5,
  relaxed: 1.75,
};

export default {
  fontFamily,
  fontSize,
  fontWeight,
  lineHeight,
};
