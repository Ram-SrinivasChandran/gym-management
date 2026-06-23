import { NavigationContainer } from '@react-navigation/native';
import { useSessionStore } from '../store/sessionStore';
import { useThemeStore } from '../store/themeStore';
import { navDarkTheme, navLightTheme } from '../theme';
import AuthNavigator from './AuthNavigator';
import AppTabs from './AppTabs';

export default function RootNavigator() {
  const accessToken = useSessionStore((state) => state.accessToken);
  const darkMode = useThemeStore((state) => state.darkMode);

  return (
    <NavigationContainer theme={darkMode ? navDarkTheme : navLightTheme}>
      {accessToken ? <AppTabs /> : <AuthNavigator />}
    </NavigationContainer>
  );
}
