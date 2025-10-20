/**
 * Tema Global IPROSS
 * Sistema de diseño completo basado en la identidad visual oficial
 */

import colors from './Colors';
import { fontFamily, fontSize, fontWeight } from './typography';

// Estilos de texto comunes
export const textStyles = {
  // Títulos
  h1: {
    fontFamily: fontFamily.black,
    fontSize: fontSize.xxxl,
    color: colors.text.primary,
    fontWeight: fontWeight.black,
  },
  h2: {
    fontFamily: fontFamily.bold,
    fontSize: fontSize.xxl,
    color: colors.text.primary,
    fontWeight: fontWeight.bold,
  },
  h3: {
    fontFamily: fontFamily.semibold,
    fontSize: fontSize.xl,
    color: colors.text.primary,
    fontWeight: fontWeight.semibold,
  },
  // Texto del cuerpo
  body: {
    fontFamily: fontFamily.regular,
    fontSize: fontSize.md,
    color: colors.text.primary,
    fontWeight: fontWeight.regular,
  },
  bodySmall: {
    fontFamily: fontFamily.regular,
    fontSize: fontSize.sm,
    color: colors.text.secondary,
    fontWeight: fontWeight.regular,
  },
  // Texto de énfasis
  emphasis: {
    fontFamily: fontFamily.semibold,
    fontSize: fontSize.md,
    color: colors.text.primary,
    fontWeight: fontWeight.semibold,
  },
  // Enlaces
  link: {
    fontFamily: fontFamily.semibold,
    fontSize: fontSize.md,
    color: colors.secondary.blue,
    fontWeight: fontWeight.semibold,
  },
  // Etiquetas
  label: {
    fontFamily: fontFamily.semibold,
    fontSize: fontSize.sm,
    color: colors.text.primary,
    fontWeight: fontWeight.semibold,
  },
};

// Estilos de botones
export const buttonStyles = {
  primary: {
    backgroundColor: colors.primary.green,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 24,
    shadowColor: colors.primary.greenDark,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  primaryText: {
    ...textStyles.emphasis,
    color: colors.neutral.white,
    fontSize: fontSize.lg,
  },
  secondary: {
    backgroundColor: colors.secondary.blue,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 24,
    shadowColor: colors.secondary.blueDark,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  secondaryText: {
    ...textStyles.emphasis,
    color: colors.neutral.white,
    fontSize: fontSize.lg,
  },
  outline: {
    backgroundColor: 'transparent',
    borderWidth: 2,
    borderColor: colors.primary.green,
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 24,
  },
  outlineText: {
    ...textStyles.emphasis,
    color: colors.primary.green,
    fontSize: fontSize.lg,
  },
  disabled: {
    backgroundColor: colors.neutral.grayDark,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 24,
  },
  disabledText: {
    ...textStyles.emphasis,
    color: colors.neutral.white,
    fontSize: fontSize.lg,
    opacity: 0.6,
  },
};

// Estilos de tarjetas
export const cardStyles = {
  container: {
    backgroundColor: colors.neutral.white,
    borderRadius: 16,
    padding: 20,
    marginVertical: 8,
    shadowColor: colors.neutral.black,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  elevated: {
    backgroundColor: colors.neutral.white,
    borderRadius: 20,
    padding: 24,
    marginVertical: 12,
    shadowColor: colors.neutral.black,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
  },
  outlined: {
    backgroundColor: colors.neutral.white,
    borderRadius: 16,
    padding: 20,
    marginVertical: 8,
    borderWidth: 2,
    borderColor: colors.neutral.gray,
  },
};

// Estilos de inputs
export const inputStyles = {
  container: {
    marginBottom: 20,
  },
  label: {
    ...textStyles.label,
    marginBottom: 8,
  },
  input: {
    borderWidth: 2,
    borderColor: colors.neutral.grayMedium,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: fontSize.md,
    fontFamily: fontFamily.regular,
    backgroundColor: colors.neutral.white,
    color: colors.text.primary,
  },
  inputFocused: {
    borderColor: colors.primary.green,
    borderWidth: 2,
  },
  inputError: {
    borderColor: colors.status.error,
    borderWidth: 2,
  },
};

// Espaciado consistente
export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

// Bordes redondeados
export const borderRadius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  full: 9999,
};

// Sombras
export const shadows = {
  small: {
    shadowColor: colors.neutral.black,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  medium: {
    shadowColor: colors.neutral.black,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  large: {
    shadowColor: colors.neutral.black,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 8,
  },
};

// Exportación del tema completo
export default {
  colors,
  textStyles,
  buttonStyles,
  cardStyles,
  inputStyles,
  spacing,
  borderRadius,
  shadows,
  fontFamily,
  fontSize,
  fontWeight,
};
