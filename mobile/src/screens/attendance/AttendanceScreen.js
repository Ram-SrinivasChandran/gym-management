import { useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import { useCheckIn, useCheckOut } from '../../features/attendance/useAttendance';
import { useMembersSearch } from '../../features/members/useMembers';
import { useToastStore } from '../../store/toastStore';

export default function AttendanceScreen() {
  const [search, setSearch] = useState('');
  const { data } = useMembersSearch({ search, size: 5 });
  const checkInMutation = useCheckIn();
  const checkOutMutation = useCheckOut();
  const showToast = useToastStore((state) => state.showToast);
  const members = data?.content ?? [];

  const handleCheckIn = (memberId) => {
    checkInMutation.mutate(
      { memberId, method: 'MANUAL' },
      {
        onSuccess: () => showToast('Checked in'),
        onError: (error) => showToast(`Check-in failed: ${error.response?.data?.message ?? 'Please try again.'}`),
      }
    );
  };

  const handleCheckOut = (memberId) => {
    checkOutMutation.mutate(memberId, {
      onSuccess: () => showToast('Checked out'),
      onError: (error) => showToast(`Check-out failed: ${error.response?.data?.message ?? 'Please try again.'}`),
    });
  };

  return (
    <ScrollView style={styles.flex}>
      <GradientHeader title="Attendance" subtitle="Front-desk check-in / check-out" />
      <View style={styles.section}>
        <TextInput
          label="Search member by name, phone, or ID"
          value={search}
          onChangeText={setSearch}
          style={styles.input}
          testID="attendance-search-input"
        />

        {members.map((member) => (
          <GlassCard key={member.id} style={styles.card}>
            <Text variant="titleMedium">{member.fullName}</Text>
            <Text style={styles.meta}>
              {member.memberCode} · {member.phone}
            </Text>
            <View style={styles.row}>
              <Button
                mode="contained"
                style={styles.halfButton}
                onPress={() => handleCheckIn(member.id)}
                testID={`checkin-${member.id}`}
              >
                Check In
              </Button>
              <Button
                mode="outlined"
                style={styles.halfButton}
                onPress={() => handleCheckOut(member.id)}
                testID={`checkout-${member.id}`}
              >
                Check Out
              </Button>
            </View>
          </GlassCard>
        ))}

        {search && members.length === 0 ? <Text style={styles.empty}>No members found.</Text> : null}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#F8FAFC' },
  section: { padding: 16 },
  input: { marginBottom: 16, backgroundColor: '#FFFFFF' },
  card: { marginBottom: 12 },
  meta: { color: '#64748B', marginTop: 4, marginBottom: 12 },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  halfButton: { flexBasis: '48%' },
  empty: { textAlign: 'center', marginTop: 24, color: '#64748B' },
});
