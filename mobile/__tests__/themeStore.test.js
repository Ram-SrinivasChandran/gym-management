import { useThemeStore } from '../src/store/themeStore';

describe('themeStore', () => {
  beforeEach(() => {
    useThemeStore.setState({ darkMode: false });
  });

  it('defaults to light mode', () => {
    expect(useThemeStore.getState().darkMode).toBe(false);
  });

  it('toggles dark mode on and off', () => {
    useThemeStore.getState().toggleDarkMode();
    expect(useThemeStore.getState().darkMode).toBe(true);

    useThemeStore.getState().toggleDarkMode();
    expect(useThemeStore.getState().darkMode).toBe(false);
  });
});
