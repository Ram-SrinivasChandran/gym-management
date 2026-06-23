import { useState } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { apiClient } from '../../api/client';
import { formatDate } from '../../utils/format';

export default function NotificationsScreen() {
  const { data: notifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => apiClient.get('/notifications').then((r) => r.data),
  });
  const queryClient = useQueryClient();
  const [message, setMessage] = useState('');

  const broadcast = useMutation({
    mutationFn: (payload) => apiClient.post('/notifications/broadcast', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      setMessage('');
    },
  });

  return (
    <FlatList
      style={styles.flex}
      data={notifications ?? []}
      keyExtractor={(item) => item.id}
      ListHeaderComponent={
        <View>
          <GradientHeader title="Notifications" subtitle="Send promos, view delivery log" />
          <View style={styles.form}>
            <TextInput
              label="Broadcast message"
              value={message}
              onChangeText={setMessage}
              style={styles.input}
              multiline
              testID="broadcast-message-input"
            />
            <Button
              mode="contained"
              onPress={() => message && broadcast.mutate({ message, channel: 'IN_APP' })}
              loading={broadcast.isPending}
              style={styles.submitButton}
              testID="send-broadcast-button"
            >
              Send to All Members
            </Button>
          </View>
        </View>
      }
      contentContainerStyle={styles.listContent}
      renderItem={({ item }) => (
        <GlassCard style={styles.card}>
          <Text variant="titleMedium">{item.type}</Text>
          <Text style={styles.meta}>
            {item.channel} · {item.status}
          </Text>
          <Text style={styles.meta}>{formatDate(item.createdAt)}</Text>
        </GlassCard>
      )}
    />
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  form: { padding: 16 },
  input: { marginBottom: 12, backgroundColor: '#FFFFFF' },
  submitButton: { borderRadius: 10 },
  listContent: { paddingHorizontal: 16, paddingBottom: 24 },
  card: { marginBottom: 12 },
  meta: { color: '#64748B', marginTop: 4 },
});
