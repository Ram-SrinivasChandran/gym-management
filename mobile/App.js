import { MaterialCommunityIcons } from '@expo/vector-icons';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { PaperProvider } from 'react-native-paper';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import GlobalToast from './src/components/GlobalToast';
import RootNavigator from './src/navigation/RootNavigator';
import { useSessionStore } from './src/store/sessionStore';
import { useThemeStore } from './src/store/themeStore';
import { darkTheme, lightTheme } from './src/theme';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1 } },
});

export default function App() {
  const hydrate = useSessionStore((state) => state.hydrate);
  const darkMode = useThemeStore((state) => state.darkMode);

  const [bootstrapped, setBootstrapped] = useState(false);

  useEffect(() => {
    // Bootstrap proceeds regardless of hydrate() outcome — a storage read failure should
    // fall back to an unauthenticated app, never hang on the splash screen forever.
    hydrate()
      .catch(() => {})
      .finally(() => setBootstrapped(true));
  }, [hydrate]);

  if (!bootstrapped) {
    return (
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  const theme = darkMode ? darkTheme : lightTheme;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <QueryClientProvider client={queryClient}>
          <PaperProvider theme={theme} settings={{ icon: (props) => <MaterialCommunityIcons {...props} /> }}>
            <StatusBar style={darkMode ? 'light' : 'dark'} />
            <RootNavigator />
            <GlobalToast />
          </PaperProvider>
        </QueryClientProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
