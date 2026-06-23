import { API_BASE_URL } from './env';

async function request<T>(path: string, init?: RequestInit & { token?: string }): Promise<T> {
  const { token, ...rest } = init ?? {};
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(rest.headers as Record<string, string> | undefined),
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}/api/v1${path}`, { ...rest, headers });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`API request failed: ${rest.method ?? 'GET'} ${path} -> ${response.status} ${body}`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export function login(email: string, password: string): Promise<TokenResponse> {
  return request<TokenResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export function onboardGym(gymName: string, adminEmail: string, adminPassword: string, superAdminToken: string) {
  return request<{ id: string; name: string }>('/gyms', {
    method: 'POST',
    token: superAdminToken,
    body: JSON.stringify({
      gymName,
      firstBranchName: 'Main Branch',
      firstBranchAddress: '1 Fitness Way',
      admin: {
        fullName: 'E2E Gym Admin',
        email: adminEmail,
        password: adminPassword,
        phone: '5550000000',
      },
    }),
  });
}

export function createPlan(token: string, payload: { name: string; planType: string; durationDays: number; price: number }) {
  return request<{ id: string }>('/membership-plans', {
    method: 'POST',
    token,
    body: JSON.stringify(payload),
  });
}

export function createMember(token: string, branchId: string, payload: { fullName: string; phone: string }) {
  return request<{ id: string; memberCode: string }>('/members', {
    method: 'POST',
    token,
    body: JSON.stringify({ branchId, ...payload }),
  });
}

export function listBranches(token: string) {
  return request<Array<{ id: string; name: string }>>('/branches', { token });
}

export function createMembership(token: string, payload: { memberId: string; planId: string; startDate: string }) {
  return request<{ id: string }>('/memberships', {
    method: 'POST',
    token,
    body: JSON.stringify(payload),
  });
}
