import { useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Text } from 'react-native-paper';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { apiClient } from '../../api/client';
import { useQuery } from '@tanstack/react-query';
import { formatCurrency } from '../../utils/format';

function monthRange() {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10);
  const end = now.toISOString().slice(0, 10);
  return { start, end };
}

export default function ReportsScreen() {
  const [{ start, end }] = useState(monthRange());

  const { data: revenue, isLoading } = useQuery({
    queryKey: ['reports', 'revenue', start, end],
    queryFn: () =>
      apiClient.get('/reports/revenue', { params: { startDate: start, endDate: end } }).then((r) => r.data),
  });

  const { data: membership } = useQuery({
    queryKey: ['reports', 'membership'],
    queryFn: () => apiClient.get('/reports/memberships').then((r) => r.data),
  });

  return (
    <ScrollView style={styles.flex}>
      <GradientHeader title="Reports" subtitle={`${start} to ${end}`} />
      <View style={styles.section}>
        {isLoading ? (
          <ActivityIndicator size="large" />
        ) : (
          <GlassCard>
            <Text variant="titleMedium">Revenue (this month)</Text>
            <Text style={styles.bigValue} testID="revenue-total-value">
              {formatCurrency(revenue?.totalRevenue)}
            </Text>
            <Text style={styles.meta}>{revenue?.paymentCount ?? 0} payments</Text>
            {Object.entries(revenue?.revenueByPaymentMethod ?? {}).map(([method, amount]) => (
              <Text key={method} style={styles.meta}>
                {method}: {formatCurrency(amount)}
              </Text>
            ))}
          </GlassCard>
        )}

        <GlassCard style={styles.cardSpacing}>
          <Text variant="titleMedium">Memberships</Text>
          <Text style={styles.meta}>Total: {membership?.totalMemberships ?? 0}</Text>
          {Object.entries(membership?.countByStatus ?? {}).map(([status, count]) => (
            <Text key={status} style={styles.meta}>
              {status}: {count}
            </Text>
          ))}
        </GlassCard>

        <Text style={styles.exportNote}>
          PDF and Excel exports are available via the Reports API (Revenue → Export) for printing or sharing.
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#F8FAFC' },
  section: { padding: 16 },
  cardSpacing: { marginTop: 16 },
  bigValue: { fontSize: 28, fontWeight: '700', marginTop: 8, color: '#2563EB' },
  meta: { color: '#64748B', marginTop: 4 },
  exportNote: { marginTop: 16, color: '#94A3B8', fontSize: 12, textAlign: 'center' },
});
