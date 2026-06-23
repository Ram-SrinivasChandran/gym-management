import { useQuery } from '@tanstack/react-query';
import { fetchDashboardSummary } from './api';

export function useDashboardSummary() {
  return useQuery({
    queryKey: ['dashboard', 'summary'],
    queryFn: fetchDashboardSummary,
    staleTime: 60 * 1000,
  });
}
