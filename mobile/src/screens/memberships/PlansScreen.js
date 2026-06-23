import { useState } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { Button, SegmentedButtons, Text, TextInput } from 'react-native-paper';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { useQueryClient, useMutation } from '@tanstack/react-query';
import { apiClient } from '../../api/client';
import { usePlans } from '../../features/memberships/useMemberships';
import { formatCurrency } from '../../utils/format';

const PLAN_TYPES = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'QUARTERLY', label: 'Quarterly' },
  { value: 'HALF_YEARLY', label: 'Half-Yr' },
  { value: 'ANNUAL', label: 'Annual' },
];

export default function PlansScreen() {
  const { data: plans, isLoading } = usePlans();
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [planType, setPlanType] = useState('MONTHLY');
  const [durationDays, setDurationDays] = useState('30');
  const [price, setPrice] = useState('');
  const [error, setError] = useState(null);

  const createPlan = useMutation({
    mutationFn: (payload) => apiClient.post('/membership-plans', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plans'] });
      setName('');
      setPrice('');
    },
  });

  const onSubmit = () => {
    if (!name || !price) {
      setError('Name and price are required');
      return;
    }
    setError(null);
    createPlan.mutate(
      { name, planType, durationDays: Number(durationDays), price: Number(price) },
      { onError: (err) => setError(err.response?.data?.message ?? 'Failed to create plan') }
    );
  };

  return (
    <FlatList
      style={styles.flex}
      data={plans ?? []}
      keyExtractor={(item) => item.id}
      ListHeaderComponent={
        <View>
          <GradientHeader title="Membership Plans" subtitle={isLoading ? 'Loading…' : `${plans?.length ?? 0} plans`} />
          <View style={styles.form}>
            <TextInput
              label="Plan Name"
              value={name}
              onChangeText={setName}
              style={styles.input}
              testID="plan-name-input"
            />
            <SegmentedButtons value={planType} onValueChange={setPlanType} buttons={PLAN_TYPES} style={styles.segmented} />
            <View style={styles.row}>
              <TextInput
                label="Duration (days)"
                keyboardType="numeric"
                value={durationDays}
                onChangeText={setDurationDays}
                style={[styles.input, styles.halfInput]}
                testID="plan-duration-input"
              />
              <TextInput
                label="Price"
                keyboardType="numeric"
                value={price}
                onChangeText={setPrice}
                style={[styles.input, styles.halfInput]}
                testID="plan-price-input"
              />
            </View>
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
            <Button
              mode="contained"
              onPress={onSubmit}
              loading={createPlan.isPending}
              style={styles.submitButton}
              testID="add-plan-button"
            >
              Add Plan
            </Button>
          </View>
        </View>
      }
      contentContainerStyle={styles.listContent}
      renderItem={({ item }) => (
        <GlassCard style={styles.card}>
          <Text variant="titleMedium">{item.name}</Text>
          <Text style={styles.meta}>
            {item.planType} · {item.durationDays} days · {formatCurrency(item.price)}
          </Text>
        </GlassCard>
      )}
    />
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#F8FAFC' },
  form: { padding: 16 },
  input: { marginTop: 12, backgroundColor: '#FFFFFF' },
  segmented: { marginTop: 12 },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  halfInput: { flexBasis: '48%' },
  errorText: { color: '#EF4444', marginTop: 8, fontSize: 12 },
  submitButton: { marginTop: 16, borderRadius: 10 },
  listContent: { paddingHorizontal: 16, paddingBottom: 24 },
  card: { marginBottom: 12 },
  meta: { color: '#64748B', marginTop: 4 },
});
