import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createMember, deactivateMember, getMember, searchMembers, updateMember } from './api';

export function useMembersSearch(params) {
  return useQuery({
    queryKey: ['members', 'search', params],
    queryFn: () => searchMembers(params),
  });
}

export function useMember(memberId) {
  return useQuery({
    queryKey: ['members', memberId],
    queryFn: () => getMember(memberId),
    enabled: Boolean(memberId),
  });
}

export function useCreateMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createMember,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['members', 'search'] }),
  });
}

export function useUpdateMember(memberId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => updateMember(memberId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members', memberId] });
      queryClient.invalidateQueries({ queryKey: ['members', 'search'] });
    },
  });
}

export function useDeactivateMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deactivateMember,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['members', 'search'] }),
  });
}
