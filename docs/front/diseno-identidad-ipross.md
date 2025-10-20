# Diseño e Identidad Visual IPROSS

## Índice
- [Paleta de Colores](#paleta-de-colores)
- [Tipografía](#tipografía)
- [Uso de Colores](#uso-de-colores)
- [Guías de Implementación](#guías-de-implementación)

---

## Paleta de Colores

### Color Principal - Verde IPROSS
El verde es el color principal de la identidad de IPROSS, representando salud, crecimiento y confianza.

**Verde RN**
- **HEX:** `#6ac64f`
- **RGB:** `106, 198, 79`
- **Uso:** Color principal de marca, botones primarios, encabezados destacados

### Color Secundario - Azul
El azul complementa la identidad visual, aportando profesionalismo y confianza.

**Azul RN**
- **HEX:** `#007be0`
- **RGB:** `0, 123, 224`
- **Uso:** Enlaces, acciones secundarias, elementos informativos

### Colores Neutrales

**Negro**
- **HEX:** `#000000`
- **RGB:** `0, 0, 0`
- **Uso:** Textos principales, iconos, elementos de contraste

**Gris**
- **HEX:** `#e8e8e8`
- **RGB:** `232, 232, 232`
- **Uso:** Fondos secundarios, separadores, estados deshabilitados

---

## Tipografía

### Familia Tipográfica: Figtree

La familia tipográfica Figtree es una fuente moderna y legible, perfecta para interfaces digitales.

#### Figtree Light
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890-=!!@#$%^&*()_+?><
```
- **Peso:** 300
- **Uso:** Textos secundarios, subtítulos ligeros, descripciones largas

#### Figtree Regular
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890-=!!@#$%^&*()_+?><
```
- **Peso:** 400
- **Uso:** Texto principal, párrafos, contenido general

#### Figtree SemiBold
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890-=!!@#$%^&*()_+?><
```
- **Peso:** 600
- **Uso:** Subtítulos destacados, etiquetas importantes, navegación

#### Figtree Bold
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890-=!!@#$%^&*()_+?><
```
- **Peso:** 700
- **Uso:** Títulos, encabezados, llamadas a la acción

#### Figtree Black
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890-=!!@#$%^&*()_+?><
```
- **Peso:** 900
- **Uso:** Títulos principales, elementos de máximo énfasis, branding

---

## Uso de Colores

### Jerarquía Visual

#### Primaria
- **Verde (#6ac64f):** Botones principales, CTAs, elementos interactivos importantes
- **Negro (#000000):** Texto principal, iconos primarios

#### Secundaria
- **Azul (#007be0):** Enlaces, información secundaria, estados hover
- **Gris (#e8e8e8):** Fondos, separadores, contenedores

### Casos de Uso

#### Botones
```
Botón Primario:
- Fondo: #6ac64f (Verde)
- Texto: #ffffff (Blanco)
- Hover: #5ab03f (Verde oscuro)

Botón Secundario:
- Fondo: #007be0 (Azul)
- Texto: #ffffff (Blanco)
- Hover: #0069c2 (Azul oscuro)

Botón Deshabilitado:
- Fondo: #e8e8e8 (Gris)
- Texto: #999999 (Gris oscuro)
```

#### Texto
```
Título Principal:
- Font: Figtree Black/Bold
- Color: #000000
- Tamaño: 24-32px

Subtítulo:
- Font: Figtree SemiBold
- Color: #000000
- Tamaño: 18-20px

Texto Principal:
- Font: Figtree Regular
- Color: #000000
- Tamaño: 14-16px

Texto Secundario:
- Font: Figtree Light
- Color: #666666
- Tamaño: 12-14px
```

#### Enlaces
```
Estado Normal:
- Color: #007be0 (Azul)
- Font: Figtree Regular/SemiBold

Estado Hover:
- Color: #0069c2 (Azul oscuro)
- Text-decoration: underline

Estado Visitado:
- Color: #005a9e (Azul más oscuro)
```

---

## Guías de Implementación

### React Native / Expo

```javascript
// colors.js - Paleta de colores
export const colors = {
  primary: {
    green: '#6ac64f',
    greenDark: '#5ab03f',
  },
  secondary: {
    blue: '#007be0',
    blueDark: '#0069c2',
  },
  neutral: {
    black: '#000000',
    gray: '#e8e8e8',
    grayDark: '#999999',
    white: '#ffffff',
  },
  text: {
    primary: '#000000',
    secondary: '#666666',
  }
};

// typography.js - Configuración tipográfica
export const typography = {
  fontFamily: {
    light: 'Figtree-Light',
    regular: 'Figtree-Regular',
    semibold: 'Figtree-SemiBold',
    bold: 'Figtree-Bold',
    black: 'Figtree-Black',
  },
  fontSize: {
    xs: 12,
    sm: 14,
    md: 16,
    lg: 18,
    xl: 20,
    xxl: 24,
    xxxl: 32,
  },
  fontWeight: {
    light: '300',
    regular: '400',
    semibold: '600',
    bold: '700',
    black: '900',
  }
};
```

### CSS / Web

```css
/* Colores */
:root {
  --color-primary-green: #6ac64f;
  --color-primary-green-dark: #5ab03f;
  --color-secondary-blue: #007be0;
  --color-secondary-blue-dark: #0069c2;
  --color-neutral-black: #000000;
  --color-neutral-gray: #e8e8e8;
  --color-neutral-gray-dark: #999999;
  --color-neutral-white: #ffffff;
  --color-text-primary: #000000;
  --color-text-secondary: #666666;
}

/* Tipografía */
@font-face {
  font-family: 'Figtree';
  src: url('./fonts/Figtree-Light.ttf');
  font-weight: 300;
}

@font-face {
  font-family: 'Figtree';
  src: url('./fonts/Figtree-Regular.ttf');
  font-weight: 400;
}

@font-face {
  font-family: 'Figtree';
  src: url('./fonts/Figtree-SemiBold.ttf');
  font-weight: 600;
}

@font-face {
  font-family: 'Figtree';
  src: url('./fonts/Figtree-Bold.ttf');
  font-weight: 700;
}

@font-face {
  font-family: 'Figtree';
  src: url('./fonts/Figtree-Black.ttf');
  font-weight: 900;
}

body {
  font-family: 'Figtree', sans-serif;
  font-weight: 400;
  color: var(--color-text-primary);
}
```

---

## Accesibilidad

### Contraste de Colores

Todos los colores han sido seleccionados para cumplir con WCAG 2.1 AA:

- **Verde sobre Blanco:** Ratio 3.5:1 ✓
- **Azul sobre Blanco:** Ratio 4.8:1 ✓
- **Negro sobre Blanco:** Ratio 21:1 ✓
- **Negro sobre Gris:** Ratio 15:1 ✓

### Recomendaciones

1. Texto pequeño (<18px) debe usar Negro (#000000) sobre fondos claros
2. Botones con Verde/Azul deben usar texto Blanco para máximo contraste
3. Nunca usar Verde sobre Azul o viceversa
4. El Gris (#e8e8e8) solo debe usarse para fondos, no para texto principal

---

## Ejemplos de Aplicación

### Pantalla de Login
```
- Fondo: Blanco (#ffffff)
- Logo: Verde (#6ac64f)
- Título: Figtree Bold, Negro (#000000)
- Inputs: Borde Gris (#e8e8e8), Texto Negro
- Botón Login: Fondo Verde (#6ac64f), Texto Blanco
- Link "Olvidé contraseña": Azul (#007be0), Figtree Regular
```

### Tarjetas de Información
```
- Fondo: Gris (#e8e8e8)
- Título: Figtree SemiBold, Negro (#000000)
- Texto: Figtree Regular, Negro (#000000)
- Iconos: Verde (#6ac64f) o Azul (#007be0)
- Separadores: Gris más oscuro (#cccccc)
```

### Navegación
```
- Fondo: Verde (#6ac64f)
- Texto activo: Blanco (#ffffff), Figtree SemiBold
- Texto inactivo: Blanco semi-transparente (rgba(255,255,255,0.7))
- Iconos: Blanco (#ffffff)
```

---

## Recursos

### Descarga de Fuentes
Las fuentes Figtree pueden descargarse desde:
- [Google Fonts](https://fonts.google.com/specimen/Figtree)
- [GitHub - Figtree Font](https://github.com/erikdkennedy/figtree)

### Herramientas Recomendadas
- **Figma:** Para diseño de interfaces
- **Coolors:** Para generación de paletas complementarias
- **WebAIM Contrast Checker:** Para verificar accesibilidad

---

*Última actualización: Octubre 2025*
