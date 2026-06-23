import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { Button, SegmentedButtons, Text, TextInput } from 'react-native-paper';
import GradientHeader from '../../components/GradientHeader';
import { useRecordPayment } from '../../features/payments/usePayments';

const PAYMENT_TYPES = [
  { value: 'FULL', label: 'Full' },
  { value: 'PARTIAL', label: 'Partial' },
  { value: 'ADVANCE', label: 'Advance' },
];

const PAYMENT_METHODS = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CARD', label: 'Card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'BANK_TRANSFER', label: 'Bank' },
  { value: 'OTHER', label: 'Other' },
];

export default function PaymentFormScreen({ route, navigation }) {
  const { membershipId } = route.params;
  const recordPayment = useRecordPayment(membershipId);

  const [amount, setAmount] = useState('');
  const [paymentType, setPaymentType] = useState('FULL');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [error, setError] = useState(null);

  const onSubmit = () => {
    const numericAmount = Number(amount);
    if (!numericAmount || numericAmount <= 0) {
      setError('Enter a valid amount');
      return;
    }
    setError(null);
    recordPayment.mutate(
      { membershipId, amount: numericAmount, paymentType, paymentMethod },
      {
        onSuccess: () => navigation.goBack(),
        onError: (err) => setError(err.response?.data?.message ?? 'Failed to record payment'),
      }
    );
  };

  return (
    <View style={styles.flex}>
      <GradientHeader title="Record Payment" subtitle="Manual entry — cash, card, UPI, or transfer" />
      <View style={styles.form}>
        <TextInput
          label="Amount"
          keyboardType="numeric"
          value={amount}
          onChangeText={setAmount}
          style={styles.input}
          testID="payment-amount-input"
        />

        <Text style={styles.label}>Payment Type</Text>
        <SegmentedButtons value={paymentType} onValueChange={setPaymentType} buttons={PAYMENT_TYPES} />

        <Text style={[styles.label, styles.labelSpacing]}>Payment Method</Text>
        <SegmentedButtons value={paymentMethod} onValueChange={setPaymentMethod} buttons={PAYMENT_METHODS} />

        {error ? <Text style={styles.errorText}>{error}</Text> : null}

        <Button
          mode="contained"
          onPress={onSubmit}
          loading={recordPayment.isPending}
          disabled={recordPayment.isPending}
          style={styles.submitButton}
          testID="submit-payment-button"
        >
          Record Payment
        </Button>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  form: { padding: 20 },
  input: { marginTop: 4, marginBottom: 16, backgroundColor: '#FFFFFF' },
  label: { color: '#64748B', marginBottom: 8 },
  labelSpacing: { marginTop: 16 },
  errorText: { color: '#EF4444', marginTop: 12, fontSize: 12 },
  submitButton: { marginTop: 24, borderRadius: 10 },
});
