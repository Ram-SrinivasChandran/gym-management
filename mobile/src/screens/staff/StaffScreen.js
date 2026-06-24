import { useState } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { Button, SegmentedButtons, Text } from 'react-native-paper';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import AppTextInput from '../../components/AppTextInput';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { apiClient } from '../../api/client';
import { useBranches } from '../../features/branches/useBranches';
import { text } from '../../theme/colors';

const ROLES = [
  { value: 'GYM_ADMIN', label: 'Admin' },
  { value: 'TRAINER', label: 'Trainer' },
];

export default function StaffScreen() {
  const { data: staff } = useQuery({
    queryKey: ['staff'],
    queryFn: () => apiClient.get('/staff').then((r) => r.data),
  });
  const { data: branches } = useBranches();
  const queryClient = useQueryClient();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('TRAINER');
  const [error, setError] = useState(null);

  const createStaff = useMutation({
    mutationFn: (payload) => apiClient.post('/staff', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      setFullName('');
      setEmail('');
      setPassword('');
    },
  });

  const onSubmit = () => {
    const branchId = branches?.[0]?.id;
    if (!fullName || !email || !password || !branchId) {
      setError('All fields are required');
      return;
    }
    setError(null);
    createStaff.mutate(
      { branchId, fullName, email, password, role },
      { onError: (err) => setError(err.response?.data?.message ?? 'Failed to create staff account') }
    );
  };

  return (
    <FlatList
      style={styles.flex}
      data={staff ?? []}
      keyExtractor={(item) => item.id}
      ListHeaderComponent={
        <View>
          <GradientHeader title="Staff Accounts" subtitle={`${staff?.length ?? 0} staff members`} />
          <View style={styles.form}>
            <AppTextInput label="Full Name" value={fullName} onChangeText={setFullName} style={styles.input} />
            <AppTextInput
              label="Email"
              autoCapitalize="none"
              keyboardType="email-address"
              value={email}
              onChangeText={setEmail}
              style={styles.input}
            />
            <AppTextInput
              label="Temporary Password"
              secureTextEntry
              value={password}
              onChangeText={setPassword}
              style={styles.input}
            />
            <SegmentedButtons value={role} onValueChange={setRole} buttons={ROLES} style={styles.segmented} />
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
            <Button mode="contained" onPress={onSubmit} loading={createStaff.isPending} style={styles.submitButton}>
              Add Staff
            </Button>
          </View>
        </View>
      }
      contentContainerStyle={styles.listContent}
      renderItem={({ item }) => (
        <GlassCard style={styles.card}>
          <Text variant="titleMedium" style={styles.cardTitle}>{item.fullName}</Text>
          <Text style={styles.meta}>
            {item.role} · {item.email}
          </Text>
        </GlassCard>
      )}
    />
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  form: { padding: 16 },
  input: { marginTop: 12, backgroundColor: '#FFFFFF' },
  segmented: { marginTop: 12 },
  errorText: { color: '#EF4444', marginTop: 8, fontSize: 12 },
  submitButton: { marginTop: 16, borderRadius: 10 },
  listContent: { paddingHorizontal: 16, paddingBottom: 24 },
  card: { marginBottom: 12 },
  cardTitle: { color: text.title, fontWeight: '700' },
  meta: { color: text.muted, marginTop: 4 },
});
