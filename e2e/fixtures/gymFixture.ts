import { test as base } from '@playwright/test';
import { createMember, createPlan, listBranches, login, onboardGym } from '../utils/apiClient';
import { LoginPage } from '../pages/LoginPage';

const SUPER_ADMIN_EMAIL = process.env.E2E_SUPER_ADMIN_EMAIL ?? 'superadmin@platform.local';
const SUPER_ADMIN_PASSWORD = process.env.E2E_SUPER_ADMIN_PASSWORD ?? 'SuperAdmin123!';

export interface GymContext {
  gymId: string;
  branchId: string;
  adminEmail: string;
  adminPassword: string;
  adminToken: string;
}

type Fixtures = {
  gym: GymContext;
  authenticatedPage: import('@playwright/test').Page;
};

/**
 * Each test gets its own freshly-onboarded gym (own admin, own branch) so parallel workers
 * never collide on shared data. Onboarding happens via direct API calls — fast, and keeps the
 * UI-driven part of each test focused on what it's actually verifying.
 */
export const test = base.extend<Fixtures>({
  gym: async ({}, use, testInfo) => {
    const unique = `${Date.now()}-${testInfo.workerIndex}-${Math.floor(Math.random() * 10000)}`;
    const adminEmail = `gymadmin-${unique}@e2e.test`;
    const adminPassword = 'GymAdmin123!';

    const { accessToken: superAdminToken } = await login(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD);
    const gym = await onboardGym(`E2E Gym ${unique}`, adminEmail, adminPassword, superAdminToken);

    const { accessToken: adminToken } = await login(adminEmail, adminPassword);
    const branches = await listBranches(adminToken);
    const branchId = branches[0].id;

    await use({ gymId: gym.id, branchId, adminEmail, adminPassword, adminToken });
  },

  authenticatedPage: async ({ page, gym }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(gym.adminEmail, gym.adminPassword);
    await page.getByTestId('stat-value-total-members').waitFor({ state: 'visible' });
    await use(page);
  },
});

export { expect } from '@playwright/test';
export { createMember, createPlan, listBranches };
