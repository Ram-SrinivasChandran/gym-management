import { FlatList, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Text } from 'react-native-paper';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { usePaymentHistory } from '../../features/payments/usePayments';
import { text } from '../../theme/colors';
import { formatCurrency, formatDate } from '../../utils/format';

export default function PaymentHistoryScreen({ route }) {
  const { membershipId } = route.params;
  const { data: payments, isLoading } = usePaymentHistory(membershipId);

  return (
    <View style={styles.flex}>
      <GradientHeader title="Payment History" />
      {isLoading ? (
        <ActivityIndicator style={styles.centered} size="large" />
      ) : (
        <FlatList
          data={payments ?? []}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <GlassCard style={styles.card}>
              <Text variant="titleMedium" style={styles.cardTitle}>{formatCurrency(item.amount)}</Text>
              <Text style={styles.meta}>
                {item.paymentType} · {item.paymentMethod} · {item.receiptNumber}
              </Text>
              <Text style={styles.meta}>{formatDate(item.paidAt)}</Text>
            </GlassCard>
          )}
          ListEmptyComponent={<Text style={styles.centered}>No payments recorded yet.</Text>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  centered: { textAlign: 'center', marginTop: 40 },
  listContent: { padding: 16 },
  card: { marginBottom: 12 },
  cardTitle: { color: text.title, fontWeight: '700' },
  meta: { color: text.muted, marginTop: 4 },
});
