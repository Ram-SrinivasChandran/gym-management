import { test, expect } from '../fixtures/gymFixture';
import { createMember, createPlan } from '../utils/apiClient';
import { MembersPage } from '../pages/MembersPage';
import { MemberDetailPage, MembershipFormPage } from '../pages/MemberDetailPage';
import { MorePage, PlansPage } from '../pages/MorePage';

test.describe('Membership Management', () => {
  test('gym admin can create a membership plan', async ({ authenticatedPage: page }) => {
    await page.getByText('More', { exact: true }).first().click();
    const morePage = new MorePage(page);
    await morePage.goToPlans();

    const plansPage = new PlansPage(page);
    const planName = `Monthly E2E ${Date.now()}`;
    await plansPage.createPlan({ name: planName, durationDays: '30', price: '49.99' });

    await plansPage.expectPlanVisible(planName);
  });

  test('assigns a membership to a member and shows ACTIVE status', async ({ authenticatedPage: page, gym }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Membership Target ${Date.now()}`,
      phone: '5551112222',
    });
    const planName = `Annual E2E ${Date.now()}`;
    await createPlan(gym.adminToken, { name: planName, planType: 'ANNUAL', durationDays: 365, price: 499 });

    await page.getByText('Members', { exact: true }).first().click();
    const membersPage = new MembersPage(page);
    await membersPage.openMember(member.id);

    const detailPage = new MemberDetailPage(page);
    await detailPage.openAddMembership();

    const membershipForm = new MembershipFormPage(page);
    await membershipForm.selectPlan(planName);
    await membershipForm.submit();

    await detailPage.expectStatus('ACTIVE');
  });
});
