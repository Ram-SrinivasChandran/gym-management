import { test } from '../fixtures/gymFixture';
import { createMember, createMembership, createPlan } from '../utils/apiClient';
import { MembersPage } from '../pages/MembersPage';
import { MemberDetailPage } from '../pages/MemberDetailPage';

function isoDaysAgo(days: number) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
}

test.describe('Due Tracking (Smart Due Engine)', () => {
  test('a fully paid membership well before expiry shows ACTIVE', async ({ authenticatedPage: page, gym }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Active Due Target ${Date.now()}`,
      phone: '5556667777',
    });
    const plan = await createPlan(gym.adminToken, {
      name: `Active Plan E2E ${Date.now()}`,
      planType: 'MONTHLY',
      durationDays: 30,
      price: 60,
    });
    await createMembership(gym.adminToken, {
      memberId: member.id,
      planId: plan.id,
      startDate: isoDaysAgo(1),
    });

    await page.getByText('Members', { exact: true }).first().click();
    await new MembersPage(page).openMember(member.id);

    await new MemberDetailPage(page).expectStatus('ACTIVE');
  });

  test('a membership past its expiry date with an unpaid balance shows OVERDUE', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Overdue Target ${Date.now()}`,
      phone: '5558889999',
    });
    const plan = await createPlan(gym.adminToken, {
      name: `Short Plan E2E ${Date.now()}`,
      planType: 'CUSTOM',
      durationDays: 5,
      price: 40,
    });
    // Started 10 days ago with a 5-day plan and no payment recorded -> already overdue.
    await createMembership(gym.adminToken, {
      memberId: member.id,
      planId: plan.id,
      startDate: isoDaysAgo(10),
    });

    await page.getByText('Members', { exact: true }).first().click();
    await new MembersPage(page).openMember(member.id);

    const detailPage = new MemberDetailPage(page);
    await detailPage.expectStatus('OVERDUE');
    await detailPage.expectPendingAmount('$40.00');
  });
});
