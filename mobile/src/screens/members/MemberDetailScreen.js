import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Text } from 'react-native-paper';
import GlassCard from '../../components/GlassCard';
import GradientHeader from '../../components/GradientHeader';
import StatusBadge from '../../components/StatusBadge';
import { useMember } from '../../features/members/useMembers';
import { useMembershipHistory } from '../../features/memberships/useMemberships';
import { useDueStatus } from '../../features/payments/usePayments';
import { useCheckIn, useCheckOut } from '../../features/attendance/useAttendance';
import { useToastStore } from '../../store/toastStore';
import { formatCurrency, formatDate } from '../../utils/format';

export default function MemberDetailScreen({ route, navigation }) {
  const { memberId } = route.params;
  const { data: member, isLoading } = useMember(memberId);
  const { data: history } = useMembershipHistory(memberId);
  const currentMembership = history?.[0];
  const { data: due } = useDueStatus(currentMembership?.id);
  const checkInMutation = useCheckIn();
  const checkOutMutation = useCheckOut();
  const showToast = useToastStore((state) => state.showToast);

  const handleCheckIn = () => {
    checkInMutation.mutate(
      { memberId, method: 'MANUAL' },
      {
        onSuccess: () => showToast('Checked in'),
        onError: (error) => showToast(`Check-in failed: ${error.response?.data?.message ?? 'Please try again.'}`),
      }
    );
  };

  const handleCheckOut = () => {
    checkOutMutation.mutate(memberId, {
      onSuccess: () => showToast('Checked out'),
      onError: (error) => showToast(`Check-out failed: ${error.response?.data?.message ?? 'Please try again.'}`),
    });
  };

  if (isLoading || !member) {
    return <ActivityIndicator style={styles.centered} size="large" />;
  }

  return (
    <ScrollView style={styles.flex}>
      <GradientHeader title={member.fullName} subtitle={`${member.memberCode} · ${member.phone}`} />

      <View style={styles.section}>
        <GlassCard>
          <Text variant="titleMedium">Profile</Text>
          <Text style={styles.detailRow}>Email: {member.email ?? '-'}</Text>
          <Text style={styles.detailRow}>Gender: {member.gender ?? '-'}</Text>
          <Text style={styles.detailRow}>
            Height/Weight: {member.heightCm ?? '-'} cm / {member.weightKg ?? '-'} kg
          </Text>
          <Text style={styles.detailRow}>BMI: {member.bmi ?? '-'}</Text>
          <Text style={styles.detailRow}>Goal: {member.fitnessGoal ?? '-'}</Text>
        </GlassCard>

        <GlassCard style={styles.cardSpacing}>
          <Text variant="titleMedium">Membership</Text>
          {currentMembership ? (
            <>
              <View style={styles.badgeRow} testID="membership-status-badge">
                <StatusBadge status={due?.status ?? currentMembership.status} />
              </View>
              <Text style={styles.detailRow}>
                {formatDate(currentMembership.startDate)} - {formatDate(currentMembership.endDate)}
              </Text>
              <Text style={styles.detailRow} testID="pending-amount-text">
                Pending: {formatCurrency(due?.pendingAmount)}
              </Text>
              {due?.nextDueDate ? (
                <Text style={styles.detailRow}>Next due: {formatDate(due.nextDueDate)}</Text>
              ) : null}
              <Button
                mode="contained"
                style={styles.actionButton}
                onPress={() => navigation.navigate('PaymentForm', { membershipId: currentMembership.id, memberId })}
                testID="record-payment-button"
              >
                Record Payment
              </Button>
              <Button
                mode="outlined"
                style={styles.actionButton}
                onPress={() => navigation.navigate('PaymentHistory', { membershipId: currentMembership.id })}
                testID="payment-history-button"
              >
                Payment History
              </Button>
            </>
          ) : (
            <>
              <Text style={styles.detailRow}>No membership yet.</Text>
              <Button
                mode="contained"
                style={styles.actionButton}
                onPress={() => navigation.navigate('MembershipForm', { memberId })}
                testID="add-membership-button"
              >
                Add Membership
              </Button>
            </>
          )}
        </GlassCard>

        <GlassCard style={styles.cardSpacing}>
          <Text variant="titleMedium">Attendance</Text>
          <View style={styles.attendanceRow}>
            <Button
              mode="contained"
              style={styles.halfButton}
              loading={checkInMutation.isPending}
              onPress={handleCheckIn}
              testID="detail-check-in-button"
            >
              Check In
            </Button>
            <Button
              mode="outlined"
              style={styles.halfButton}
              loading={checkOutMutation.isPending}
              onPress={handleCheckOut}
              testID="detail-check-out-button"
            >
              Check Out
            </Button>
          </View>
        </GlassCard>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#F8FAFC' },
  centered: { flex: 1, justifyContent: 'center' },
  section: { padding: 16 },
  cardSpacing: { marginTop: 16 },
  detailRow: { marginTop: 6, color: '#334155' },
  badgeRow: { marginTop: 8, marginBottom: 4 },
  actionButton: { marginTop: 12, borderRadius: 10 },
  attendanceRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 12 },
  halfButton: { flexBasis: '48%' },
});
