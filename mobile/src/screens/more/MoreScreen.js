import { StyleSheet, View } from 'react-native';
import { List, Switch } from 'react-native-paper';
import GradientHeader from '../../components/GradientHeader';
import { useLogout } from '../../features/auth/useAuth';
import { useThemeStore } from '../../store/themeStore';

export default function MoreScreen({ navigation }) {
  const logout = useLogout();
  const darkMode = useThemeStore((state) => state.darkMode);
  const toggleDarkMode = useThemeStore((state) => state.toggleDarkMode);

  return (
    <View style={styles.flex}>
      <GradientHeader title="More" subtitle="Settings and management" />
      <List.Section>
        <List.Item
          title="Membership Plans"
          left={(props) => <List.Icon {...props} icon="card-account-details-outline" />}
          onPress={() => navigation.navigate('Plans')}
          testID="more-nav-plans"
        />
        <List.Item
          title="Reports"
          left={(props) => <List.Icon {...props} icon="chart-bar" />}
          onPress={() => navigation.navigate('Reports')}
          testID="more-nav-reports"
        />
        <List.Item
          title="Staff Accounts"
          left={(props) => <List.Icon {...props} icon="account-group-outline" />}
          onPress={() => navigation.navigate('Staff')}
          testID="more-nav-staff"
        />
        <List.Item
          title="Notifications"
          left={(props) => <List.Icon {...props} icon="bell-outline" />}
          onPress={() => navigation.navigate('Notifications')}
          testID="more-nav-notifications"
        />
        <List.Item
          title="Dark Mode"
          left={(props) => <List.Icon {...props} icon="theme-light-dark" />}
          right={() => <Switch value={darkMode} onValueChange={toggleDarkMode} testID="dark-mode-switch" />}
        />
        <List.Item
          title="Log Out"
          left={(props) => <List.Icon {...props} icon="logout" color="#EF4444" />}
          titleStyle={styles.logoutText}
          onPress={() => logout.mutate()}
          testID="logout-button"
        />
      </List.Section>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  logoutText: { color: '#EF4444' },
});
