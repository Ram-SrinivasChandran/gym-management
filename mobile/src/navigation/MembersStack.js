import { createNativeStackNavigator } from '@react-navigation/native-stack';
import MembersListScreen from '../screens/members/MembersListScreen';
import MemberDetailScreen from '../screens/members/MemberDetailScreen';
import MemberFormScreen from '../screens/members/MemberFormScreen';
import MembershipFormScreen from '../screens/memberships/MembershipFormScreen';
import PaymentFormScreen from '../screens/payments/PaymentFormScreen';
import PaymentHistoryScreen from '../screens/payments/PaymentHistoryScreen';

const Stack = createNativeStackNavigator();

export default function MembersStack() {
  return (
    <Stack.Navigator>
      <Stack.Screen name="MembersList" component={MembersListScreen} options={{ title: 'Members' }} />
      <Stack.Screen name="MemberDetail" component={MemberDetailScreen} options={{ title: 'Member' }} />
      <Stack.Screen name="MemberForm" component={MemberFormScreen} options={{ title: 'New Member' }} />
      <Stack.Screen name="MembershipForm" component={MembershipFormScreen} options={{ title: 'New Membership' }} />
      <Stack.Screen name="PaymentForm" component={PaymentFormScreen} options={{ title: 'Record Payment' }} />
      <Stack.Screen name="PaymentHistory" component={PaymentHistoryScreen} options={{ title: 'Payment History' }} />
    </Stack.Navigator>
  );
}
