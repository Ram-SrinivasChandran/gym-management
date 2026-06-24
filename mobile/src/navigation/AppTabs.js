import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { MaterialCommunityIcons as Icon } from '@expo/vector-icons';
import DashboardScreen from '../screens/dashboard/DashboardScreen';
import MembersStack from './MembersStack';
import MoreStack from './MoreStack';
import { brand } from '../theme/colors';

const Tab = createBottomTabNavigator();

function tabIcon(name) {
  function TabIcon({ color, size }) {
    return <Icon name={name} color={color} size={size} />;
  }
  TabIcon.displayName = `TabIcon(${name})`;
  return TabIcon;
}

export default function AppTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: brand.primary,
        tabBarInactiveTintColor: '#94A3B8',
      }}
    >
      <Tab.Screen
        name="Dashboard"
        component={DashboardScreen}
        options={{ tabBarIcon: tabIcon('view-dashboard-outline') }}
      />
      <Tab.Screen
        name="Members"
        component={MembersStack}
        options={{ tabBarIcon: tabIcon('account-group-outline') }}
      />
      <Tab.Screen name="More" component={MoreStack} options={{ tabBarIcon: tabIcon('dots-horizontal') }} />
    </Tab.Navigator>
  );
}
