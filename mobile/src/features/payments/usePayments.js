import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getDueStatus, getPaymentHistory, recordPayment } from './api';

export function usePaymentHistory(membershipId) {
  return useQuery({
    queryKey: ['payments', 'history', membershipId],
    queryFn: () => getPaymentHistory(membershipId),
    enabled: Boolean(membershipId),
  });
}

export function useDueStatus(membershipId) {
  return useQuery({
    queryKey: ['payments', 'due', membershipId],
    queryFn: () => getDueStatus(membershipId),
    enabled: Boolean(membershipId),
  });
}

export function useRecordPayment(membershipId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: recordPayment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments', 'history', membershipId] });
      queryClient.invalidateQueries({ queryKey: ['payments', 'due', membershipId] });
      queryClient.invalidateQueries({ queryKey: ['dashboard', 'summary'] });
    },
  });
}
