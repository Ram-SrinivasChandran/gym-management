import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import GradientHeader from '../../components/GradientHeader';
import InlineSelect from '../../components/InlineSelect';
import { usePlans, useCreateMembership } from '../../features/memberships/useMemberships';
import { formatCurrency } from '../../utils/format';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default function MembershipFormScreen({ route, navigation }) {
  const { memberId } = route.params;
  const { data: plans } = usePlans();
  const createMembership = useCreateMembership(memberId);
  const [planPickerOpen, setPlanPickerOpen] = useState(false);
  const [planId, setPlanId] = useState(null);
  const [startDate, setStartDate] = useState(todayIso());
  const [error, setError] = useState(null);

  const selectedPlan = plans?.find((p) => p.id === planId);

  const onSubmit = () => {
    if (!planId) {
      setError('Select a plan');
      return;
    }
    setError(null);
    createMembership.mutate(
      { memberId, planId, startDate },
      {
        onSuccess: () => navigation.goBack(),
        onError: (err) => setError(err.response?.data?.message ?? 'Failed to create membership'),
      }
    );
  };

  return (
    <View style={styles.flex}>
      <GradientHeader title="New Membership" subtitle="Assign a plan to this member" />
      <View style={styles.form}>
        <InlineSelect
          testID="membership-plan-picker"
          placeholder="Select plan"
          selectedLabel={selectedPlan ? `${selectedPlan.name} (${formatCurrency(selectedPlan.price)})` : null}
          open={planPickerOpen}
          onToggle={() => setPlanPickerOpen((prev) => !prev)}
          options={(plans ?? []).map((plan) => ({
            id: plan.id,
            label: `${plan.name} — ${formatCurrency(plan.price)}`,
          }))}
          onSelect={(option) => {
            setPlanId(option.id);
            setPlanPickerOpen(false);
          }}
        />

        <TextInput
          label="Start Date (YYYY-MM-DD)"
          value={startDate}
          onChangeText={setStartDate}
          style={styles.input}
          testID="membership-start-date-input"
        />

        {error ? <Text style={styles.errorText}>{error}</Text> : null}

        <Button
          mode="contained"
          onPress={onSubmit}
          loading={createMembership.isPending}
          disabled={createMembership.isPending}
          style={styles.submitButton}
          testID="create-membership-button"
        >
          Create Membership
        </Button>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#F8FAFC' },
  form: { padding: 20 },
  input: { marginTop: 12, backgroundColor: '#FFFFFF' },
  errorText: { color: '#EF4444', marginTop: 8, fontSize: 12 },
  submitButton: { marginTop: 24, borderRadius: 10 },
});
