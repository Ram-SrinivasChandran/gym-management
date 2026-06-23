import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { checkIn, checkOut, getAttendanceReport, getMemberAttendanceHistory } from './api';

export function useCheckIn() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ memberId, method }) => checkIn(memberId, method),
    onSuccess: (_, { memberId }) => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'history', memberId] });
      queryClient.invalidateQueries({ queryKey: ['dashboard', 'summary'] });
    },
  });
}

export function useCheckOut() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (memberId) => checkOut(memberId),
    onSuccess: (_, memberId) => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'history', memberId] });
    },
  });
}

export function useMemberAttendanceHistory(memberId) {
  return useQuery({
    queryKey: ['attendance', 'history', memberId],
    queryFn: () => getMemberAttendanceHistory(memberId),
    enabled: Boolean(memberId),
  });
}

export function useAttendanceReport(startDate, endDate) {
  return useQuery({
    queryKey: ['attendance', 'report', startDate, endDate],
    queryFn: () => getAttendanceReport(startDate, endDate),
    enabled: Boolean(startDate && endDate),
  });
}
