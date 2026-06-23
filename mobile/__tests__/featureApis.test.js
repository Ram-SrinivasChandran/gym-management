jest.mock('../src/api/client', () => ({
  apiClient: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
  },
}));

import { apiClient } from '../src/api/client';
import { login, logout } from '../src/features/auth/api';
import { listBranches } from '../src/features/branches/api';
import { fetchDashboardSummary } from '../src/features/dashboard/api';
import {
  createMember,
  deactivateMember,
  getMember,
  searchMembers,
  updateMember,
} from '../src/features/members/api';
import {
  createMembership,
  getMembershipHistory,
  listPlans,
  renewMembership,
} from '../src/features/memberships/api';
import { getDueStatus, getPaymentHistory, recordPayment } from '../src/features/payments/api';
import {
  checkIn,
  checkOut,
  getAttendanceReport,
  getMemberAttendanceHistory,
} from '../src/features/attendance/api';

beforeEach(() => {
  jest.clearAllMocks();
});

describe('auth api', () => {
  it('login posts credentials and returns token payload', async () => {
    apiClient.post.mockResolvedValue({ data: { accessToken: 'a', refreshToken: 'r' } });

    const result = await login('admin@gym.com', 'secret123');

    expect(apiClient.post).toHaveBeenCalledWith('/auth/login', { email: 'admin@gym.com', password: 'secret123' });
    expect(result).toEqual({ accessToken: 'a', refreshToken: 'r' });
  });

  it('logout posts the refresh token', async () => {
    apiClient.post.mockResolvedValue({});
    await logout('refresh-token');
    expect(apiClient.post).toHaveBeenCalledWith('/auth/logout', { refreshToken: 'refresh-token' });
  });
});

describe('branches api', () => {
  it('lists branches', async () => {
    apiClient.get.mockResolvedValue({ data: [{ id: '1', name: 'Main' }] });
    const result = await listBranches();
    expect(apiClient.get).toHaveBeenCalledWith('/branches');
    expect(result).toEqual([{ id: '1', name: 'Main' }]);
  });
});

describe('dashboard api', () => {
  it('fetches dashboard summary', async () => {
    apiClient.get.mockResolvedValue({ data: { totalMembers: 5 } });
    const result = await fetchDashboardSummary();
    expect(apiClient.get).toHaveBeenCalledWith('/dashboard/summary');
    expect(result).toEqual({ totalMembers: 5 });
  });
});

describe('members api', () => {
  it('searches members with query params', async () => {
    apiClient.get.mockResolvedValue({ data: { content: [] } });
    await searchMembers({ search: 'john', branchId: 'b1', page: 1, size: 10 });
    expect(apiClient.get).toHaveBeenCalledWith('/members', {
      params: { search: 'john', branchId: 'b1', page: 1, size: 10 },
    });
  });

  it('gets a member by id', async () => {
    apiClient.get.mockResolvedValue({ data: { id: 'm1' } });
    const result = await getMember('m1');
    expect(apiClient.get).toHaveBeenCalledWith('/members/m1');
    expect(result).toEqual({ id: 'm1' });
  });

  it('creates a member', async () => {
    apiClient.post.mockResolvedValue({ data: { id: 'm1' } });
    await createMember({ fullName: 'John' });
    expect(apiClient.post).toHaveBeenCalledWith('/members', { fullName: 'John' });
  });

  it('updates a member', async () => {
    apiClient.patch.mockResolvedValue({ data: { id: 'm1' } });
    await updateMember('m1', { fullName: 'Jane' });
    expect(apiClient.patch).toHaveBeenCalledWith('/members/m1', { fullName: 'Jane' });
  });

  it('deactivates a member', async () => {
    apiClient.delete.mockResolvedValue({ data: { id: 'm1', status: 'INACTIVE' } });
    await deactivateMember('m1');
    expect(apiClient.delete).toHaveBeenCalledWith('/members/m1');
  });
});

describe('memberships api', () => {
  it('lists plans', async () => {
    apiClient.get.mockResolvedValue({ data: [] });
    await listPlans();
    expect(apiClient.get).toHaveBeenCalledWith('/membership-plans');
  });

  it('gets membership history for a member', async () => {
    apiClient.get.mockResolvedValue({ data: [] });
    await getMembershipHistory('m1');
    expect(apiClient.get).toHaveBeenCalledWith('/memberships/member/m1/history');
  });

  it('creates a membership', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await createMembership({ memberId: 'm1', planId: 'p1', startDate: '2026-06-20' });
    expect(apiClient.post).toHaveBeenCalledWith('/memberships', {
      memberId: 'm1',
      planId: 'p1',
      startDate: '2026-06-20',
    });
  });

  it('renews a membership', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await renewMembership('ms1', 'p2');
    expect(apiClient.post).toHaveBeenCalledWith('/memberships/ms1/renew', null, { params: { newPlanId: 'p2' } });
  });
});

describe('payments api', () => {
  it('records a payment', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await recordPayment({ membershipId: 'ms1', amount: 50 });
    expect(apiClient.post).toHaveBeenCalledWith('/payments', { membershipId: 'ms1', amount: 50 });
  });

  it('gets payment history for a membership', async () => {
    apiClient.get.mockResolvedValue({ data: [] });
    await getPaymentHistory('ms1');
    expect(apiClient.get).toHaveBeenCalledWith('/payments/membership/ms1/history');
  });

  it('gets due status for a membership', async () => {
    apiClient.get.mockResolvedValue({ data: { status: 'ACTIVE' } });
    const result = await getDueStatus('ms1');
    expect(apiClient.get).toHaveBeenCalledWith('/payments/membership/ms1/due');
    expect(result).toEqual({ status: 'ACTIVE' });
  });
});

describe('attendance api', () => {
  it('checks in a member with default method', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await checkIn('m1');
    expect(apiClient.post).toHaveBeenCalledWith('/attendance/check-in', { memberId: 'm1', method: 'MANUAL' });
  });

  it('checks in a member with QR method', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await checkIn('m1', 'QR');
    expect(apiClient.post).toHaveBeenCalledWith('/attendance/check-in', { memberId: 'm1', method: 'QR' });
  });

  it('checks out a member', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    await checkOut('m1');
    expect(apiClient.post).toHaveBeenCalledWith('/attendance/check-out/m1');
  });

  it('gets member attendance history', async () => {
    apiClient.get.mockResolvedValue({ data: [] });
    await getMemberAttendanceHistory('m1');
    expect(apiClient.get).toHaveBeenCalledWith('/attendance/member/m1/history');
  });

  it('gets attendance report for a date range', async () => {
    apiClient.get.mockResolvedValue({ data: [] });
    await getAttendanceReport('2026-06-01', '2026-06-30');
    expect(apiClient.get).toHaveBeenCalledWith('/attendance/report', {
      params: { startDate: '2026-06-01', endDate: '2026-06-30' },
    });
  });
});
