import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createMembership, getMembershipHistory, listPlans, renewMembership } from './api';

export function usePlans() {
  return useQuery({ queryKey: ['plans'], queryFn: listPlans, staleTime: 5 * 60 * 1000 });
}

export function useMembershipHistory(memberId) {
  return useQuery({
    queryKey: ['memberships', 'history', memberId],
    queryFn: () => getMembershipHistory(memberId),
    enabled: Boolean(memberId),
  });
}

export function useCreateMembership(memberId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createMembership,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['memberships', 'history', memberId] }),
  });
}

export function useRenewMembership(memberId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ membershipId, newPlanId }) => renewMembership(membershipId, newPlanId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['memberships', 'history', memberId] }),
  });
}
