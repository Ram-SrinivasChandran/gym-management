import { test, expect } from '../fixtures/gymFixture';
import { createMember } from '../utils/apiClient';
import { DashboardPage } from '../pages/DashboardPage';

test.describe('Dashboard', () => {
  test('reflects the total member count for a freshly onboarded gym', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.expectLoaded();
    expect(await dashboardPage.getStatValue('total-members')).toBe('0');

    await createMember(gym.adminToken, gym.branchId, { fullName: `Dash A ${Date.now()}`, phone: '5551110000' });
    await createMember(gym.adminToken, gym.branchId, { fullName: `Dash B ${Date.now()}`, phone: '5551110001' });

    await page.getByText('Dashboard', { exact: true }).first().click();
    await page.reload();
    await dashboardPage.expectLoaded();
    expect(await dashboardPage.getStatValue('total-members')).toBe('2');
  });
});
