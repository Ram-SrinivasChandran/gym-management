import { RefreshControl, ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Text } from 'react-native-paper';
import GradientHeader from '../../components/GradientHeader';
import StatCard from '../../components/StatCard';
import { statusColors } from '../../theme/colors';
import { useDashboardSummary } from '../../features/dashboard/useDashboard';
import { formatCurrency } from '../../utils/format';

export default function DashboardScreen() {
  const { data, isLoading, isError, refetch, isRefetching } = useDashboardSummary();

  if (isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (isError || !data) {
    return (
      <View style={styles.centered}>
        <Text>Unable to load dashboard. Pull to retry.</Text>
      </View>
    );
  }

  const byStatus = data.membershipsByStatus ?? {};

  return (
    <ScrollView
      style={styles.flex}
      refreshControl={<RefreshControl refreshing={isRefetching} onRefresh={refetch} />}
    >
      <GradientHeader title="Dashboard" subtitle="Today's overview" />
      <View style={styles.grid}>
        <StatCard label="Total Members" value={data.totalMembers} />
        <StatCard label="New This Month" value={data.newRegistrationsThisMonth} />
        <StatCard
          label="Active Memberships"
          value={byStatus.ACTIVE ?? 0}
          color={statusColors.ACTIVE}
        />
        <StatCard
          label="Expired Memberships"
          value={byStatus.EXPIRED ?? 0}
          color={statusColors.EXPIRED}
        />
        <StatCard label="Due Today" value={data.dueTodayCount} color={statusColors.DUE_SOON} />
        <StatCard label="Due This Week" value={data.dueThisWeekCount} color={statusColors.DUE_SOON} />
        <StatCard label="Overdue" value={data.overdueCount} color={statusColors.OVERDUE} />
        <StatCard label="Renewals (Month)" value={data.renewalsThisMonth} color={statusColors.RENEWED} />
        <StatCard
          label="Revenue (Month)"
          value={formatCurrency(data.revenueThisMonth)}
          color={statusColors.ACTIVE}
        />
        <StatCard label="Payments (Month)" value={data.paymentCountThisMonth} />
        <StatCard label="Attendance Today" value={data.attendanceTodayCount} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 16,
  },
});
