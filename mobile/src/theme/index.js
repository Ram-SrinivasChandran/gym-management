import {
  DarkTheme as NavDarkTheme,
  DefaultTheme as NavLightTheme,
} from '@react-navigation/native';
import { MD3DarkTheme, MD3LightTheme } from 'react-native-paper';
import { brand } from './colors';

export const lightTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: brand.primary,
    secondary: brand.secondary,
    tertiary: brand.accent,
    background: brand.background,
  },
};

export const darkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    primary: brand.primary,
    secondary: brand.secondary,
    tertiary: brand.accent,
    background: brand.dark,
    surface: '#171717',
  },
};

// Navigation themes control the scene/tab-bar/header backgrounds. Screens no longer
// hardcode a background, so these drive the light/dark surface behind every screen.
export const navLightTheme = {
  ...NavLightTheme,
  colors: {
    ...NavLightTheme.colors,
    primary: brand.primary,
    background: brand.background,
    card: '#FFFFFF',
  },
};

export const navDarkTheme = {
  ...NavDarkTheme,
  colors: {
    ...NavDarkTheme.colors,
    primary: brand.primary,
    background: brand.dark,
    card: '#171717',
    text: '#F5F5F5',
    border: '#262626',
  },
};

export { brand, statusColors } from './colors';
