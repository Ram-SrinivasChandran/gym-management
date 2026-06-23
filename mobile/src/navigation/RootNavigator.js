import { NavigationContainer } from '@react-navigation/native';
import { useSessionStore } from '../store/sessionStore';
import AuthNavigator from './AuthNavigator';
import AppTabs from './AppTabs';

export default function RootNavigator() {
  const accessToken = useSessionStore((state) => state.accessToken);

  return <NavigationContainer>{accessToken ? <AppTabs /> : <AuthNavigator />}</NavigationContainer>;
}
