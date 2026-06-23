import { useQuery } from '@tanstack/react-query';
import { listBranches } from './api';

export function useBranches() {
  return useQuery({
    queryKey: ['branches'],
    queryFn: listBranches,
    staleTime: 5 * 60 * 1000,
  });
}
